// 检查服务器 Web 服务状态并配置 nginx
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
    console.log('🔍 检查服务器环境...\n');

    // 检查 nginx
    const nginxStatus = await execCommand(conn, 'which nginx && nginx -v 2>&1');
    console.log('Nginx:', nginxStatus.stdout.trim() || '未安装');

    // 检查 nginx 是否在运行
    const nginxRun = await execCommand(conn, 'systemctl is-active nginx 2>/dev/null || service nginx status 2>/dev/null | head -3');
    console.log('Nginx 运行状态:', nginxRun.stdout.trim() || nginxRun.stderr.trim() || '未知');

    // 检查 nginx 配置目录
    const confCheck = await execCommand(conn, 'ls -la /etc/nginx/conf.d/ 2>/dev/null; ls -la /etc/nginx/sites-enabled/ 2>/dev/null');
    console.log('\nNginx 配置目录:\n', confCheck.stdout || '(无 conf.d/sites-enabled)');

    // 检查是否有其他 Web 服务 (Apache)
    const apacheStatus = await execCommand(conn, 'which httpd && which apache2 2>/dev/null; systemctl is-active httpd 2>/dev/null; systemctl is-active apache2 2>/dev/null');
    console.log('Apache:', apacheStatus.stdout.trim() || '未运行');

    // 检查 80 端口
    const port80 = await execCommand(conn, 'ss -tlnp | grep :80 || netstat -tlnp 2>/dev/null | grep :80');
    console.log('\n80 端口监听:', port80.stdout.trim() || '无');

    // 检查 /root/html 目录
    const htmlDir = await execCommand(conn, 'ls -la /root/html/');
    console.log('\n/root/html 目录内容:\n', htmlDir.stdout);

    // 如果 nginx 存在但没有配置 /root/html 的站点，自动创建配置
    if (nginxStatus.stdout.trim()) {
      console.log('⚙️  配置 nginx 指向 /root/html ...\n');

      const nginxConf = `server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;
    root /root/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }
}`;

      // 备份旧配置
      await execCommand(conn, 'cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak 2>/dev/null; true');

      // 写入站点配置
      await execCommand(conn, `cat > /etc/nginx/conf.d/admin-web.conf << 'EOF'
${nginxConf}
EOF`);

      // 删除默认站点（如果存在冲突）
      await execCommand(conn, 'rm -f /etc/nginx/sites-enabled/default 2>/dev/null; true');
      await execCommand(conn, 'rm -f /etc/nginx/conf.d/default.conf 2>/dev/null; true');

      // 测试 nginx 配置
      const testResult = await execCommand(conn, 'nginx -t 2>&1');
      console.log('nginx -t 测试:', testResult.stdout.trim(), testResult.stderr.trim());

      if (testResult.stderr.includes('test is successful') || testResult.stdout.includes('test is successful')) {
        // 重载 nginx
        await execCommand(conn, 'systemctl reload nginx 2>/dev/null || service nginx reload 2>/dev/null || nginx -s reload 2>/dev/null || nginx');
        console.log('✅ Nginx 重载成功！');
      } else {
        // 尝试启动 nginx
        await execCommand(conn, 'nginx 2>&1 || systemctl start nginx 2>&1');
        console.log('Nginx 启动尝试完成');
      }

      // 再次检查端口
      const portCheck = await execCommand(conn, 'ss -tlnp | grep :80');
      console.log('\n配置后 80 端口:', portCheck.stdout.trim() || '请检查防火墙');
    }

    // 如果没有 nginx，安装并配置
    if (!nginxStatus.stdout.trim()) {
      console.log('📦 开始安装 nginx ...');
      const install = await execCommand(conn, `
        cat /etc/os-release | grep -iE 'centos|rhel|alma|rocky|fedora' && { yum install -y nginx; systemctl enable nginx; } || \
        cat /etc/os-release | grep -iE 'ubuntu|debian' && { apt-get update -qq && apt-get install -y nginx; systemctl enable nginx; }
      `);
      console.log('安装结果:', install.stderr.trim() || install.stdout.trim().slice(-200));
    }

    console.log('\n🌐 访问地址: http://8.148.144.146/');
  } finally {
    conn.end();
  }
}

main().catch((e) => {
  console.error('❌ 错误:', e.message);
  process.exit(1);
});
