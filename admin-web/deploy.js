// 部署脚本：将 dist 目录上传到远程服务器
import SftpClient from 'ssh2-sftp-client';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const config = {
  host: '8.148.144.146',
  port: 22,
  username: 'root',
  password: 'Abc223344',
};

const localDist = path.join(__dirname, 'dist');
const remoteDir = '/root/html';

async function deploy() {
  const sftp = new SftpClient();

  try {
    console.log(`🔌 正在连接服务器 ${config.host}:${config.port} ...`);
    await sftp.connect(config);
    console.log('✅ SSH 连接成功！');

    // 1. 创建远程目录（如果不存在）
    console.log(`📁 确保远程目录存在: ${remoteDir}`);
    try {
      await sftp.stat(remoteDir);
      console.log('   远程目录已存在');
    } catch {
      await sftp.mkdir(remoteDir, true);
      console.log('   远程目录创建成功');
    }

    // 2. 清空远程目录旧文件
    console.log('🗑️  清空远程目录旧文件...');
    const existingFiles = await sftp.list(remoteDir);
    for (const file of existingFiles) {
      const remotePath = `${remoteDir}/${file.name}`;
      try {
        if (file.type === 'd') {
          await sftp.rmdir(remotePath, true);
        } else {
          await sftp.delete(remotePath);
        }
      } catch (e) {
        console.warn(`   跳过删除 ${file.name}: ${e.message}`);
      }
    }
    console.log('   旧文件清理完成');

    // 3. 上传 dist 目录
    console.log(`⬆️  正在上传本地 dist -> 远程 ${remoteDir}`);
    await sftp.uploadDir(localDist, remoteDir);
    console.log('   文件上传完成！');

    // 4. 列出远程目录确认
    const uploaded = await sftp.list(remoteDir);
    console.log(`✅ 部署完成！远程目录 ${remoteDir} 包含 ${uploaded.length} 项：`);
    uploaded.forEach((f) => {
      console.log(`   ${f.type === 'd' ? '📁' : '📄'} ${f.name}`);
    });

    console.log('\n🎉 部署成功！请通过 http://8.148.144.146/ 访问站点。');
  } catch (err) {
    console.error('❌ 部署失败:', err.message);
    process.exitCode = 1;
  } finally {
    try {
      await sftp.end();
    } catch {
      /* ignore */
    }
  }
}

deploy();
