// 检查并优化 nginx 配置
import { Client } from 'ssh2';

const config = {
  host: '8.148.144.146',
  port: 22,
  username: 'root',
  password: 'Abc223344',
};

function execCommand(conn, cmd) {
  return new Promise((resolve, reject) => {
    conn.exec(cmd, (err, stream) => {
      if (err) return reject(err);
      let stdout = '', stderr = '';
      stream.on('close', (code) => resolve({ stdout, stderr, code }));
      stream.on('data', (data) => (stdout += data.toString()));
      stream.stderr.on('data', (data) => (stderr += data.toString()));
    });
  });
}

async function main() {
  const conn = new Client();
  conn.connect(config);

  await new Promise((resolve, reject) => {
    conn.on('ready', resolve);
    conn.on('error', reject);
  });

  try {
    console.log('📋 现有 nginx 配置文件:\n');

    // 查看主配置
    const mainConf = await execCommand(conn, 'cat /etc/nginx/nginx.conf');
    console.log('=== nginx.conf ===');
    console.log(mainConf.stdout);

    // 查看 kuaima.conf
    const kuaimaConf = await execCommand(conn, 'cat /etc/nginx/conf.d/kuaima.conf');
    console.log('\n=== kuaima.conf ===');
    console.log(kuaimaConf.stdout);

    // 查看 admin-web.conf
    const adminConf = await execCommand(conn, 'cat /etc/nginx/conf.d/admin-web.conf 2>/dev/null');
    console.log('\n=== admin-web.conf ===');
    console.log(adminConf.stdout || '(文件不存在)');

    // 查看 /root/html 下的 index.html 头部
    const indexCheck = await execCommand(conn, 'head -20 /root/html/index.html');
    console.log('\n=== /root/html/index.html 内容预览 ===');
    console.log(indexCheck.stdout);

    // 用 curl 测试 nginx 是否能正确返回
    const curlTest = await execCommand(conn, 'curl -s -o /dev/null -w "HTTP 状态码: %{http_code}\\nContent-Type: %{content_type}\\n" http://localhost/');
    console.log('\n=== curl 测试本地访问 ===');
    console.log(curlTest.stdout || curlTest.stderr);

    const curlBody = await execCommand(conn, 'curl -s http://localhost/ | head -20');
    console.log('\n=== curl 返回内容预览 ===');
    console.log(curlBody.stdout);
  } finally {
    conn.end();
  }
}

main().catch((e) => {
  console.error('❌ 错误:', e.message);
  process.exit(1);
});
