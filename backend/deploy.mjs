import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function main() {
  try {
    console.log('Connecting to server...');
    await ssh.connect({
      host: '115.191.6.95',
      username: 'root',
      password: 'Maxence2468;',
    });
    const targetDir = '/home/lslife/lslife-backend';

    console.log('Uploading backend files...');
    await ssh.putFiles([
      { local: '../schema.prisma', remote: `${targetDir}/prisma/schema.prisma` },
      { local: 'src/app.ts', remote: `${targetDir}/src/app.ts` },
      { local: 'src/lib/jwt.ts', remote: `${targetDir}/src/lib/jwt.ts` },
      { local: 'src/modules/admin.ts', remote: `${targetDir}/src/modules/admin.ts` },
      { local: 'scripts/seedAdmin.ts', remote: `${targetDir}/scripts/seedAdmin.ts` }
    ]);
    
    /*
    console.log('Generating Prisma Client...');
    let result = await ssh.execCommand(`su - lslife -c 'cd ${targetDir} && npx prisma generate'`, {});
    console.log('Prisma Generate:', result.stdout || result.stderr);

    console.log('Pushing schema...');
    result = await ssh.execCommand(`su - lslife -c 'cd ${targetDir} && npx prisma db push --accept-data-loss'`, {});
    console.log('Prisma Push:', result.stdout || result.stderr);

    console.log('Building backend on server...');
    result = await ssh.execCommand(`su - lslife -c 'cd ${targetDir} && npm run build'`, {});
    console.log('Backend build:', result.stdout || result.stderr);

    console.log('Seeding admin...');
    result = await ssh.execCommand(`su - lslife -c 'cd ${targetDir} && npx tsx scripts/seedAdmin.ts'`, {});
    console.log('Seed:', result.stdout || result.stderr);

    console.log('Restarting PM2...');
    result = await ssh.execCommand(`su - lslife -c 'cd ${targetDir} && pm2 restart all'`, {});
    console.log('PM2:', result.stdout || result.stderr);
    */
    let result;
    console.log('Inspecting Nginx configs...');
    result = await ssh.execCommand('ls -la /etc/nginx/sites-available/ && ls -la /etc/nginx/conf.d/');
    console.log('Config dirs:', result.stdout);

    result = await ssh.execCommand('cat /etc/nginx/sites-available/lslife');
    console.log('lslife config:', result.stdout);

    result = await ssh.execCommand('find /etc/letsencrypt/live/ -name "mentalhlp.site"');
    console.log('Certbot certs:', result.stdout);

    // Let's try to restore the SSL config if we can find the certs
    const certPath = '/etc/letsencrypt/live/mentalhlp.site';
    const nginxConf = `
server {
    listen 80;
    server_name mentalhlp.site 115.191.6.95;
    
    # Redirect HTTP to HTTPS
    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl;
    server_name mentalhlp.site;

    ssl_certificate ${certPath}/fullchain.pem;
    ssl_certificate_key ${certPath}/privkey.pem;

    location /admin-web/ {
        alias ${targetDir}/admin-web/dist/;
        try_files $uri $uri/ /admin-web/index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:4000/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /ws {
        proxy_pass http://127.0.0.1:4000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
    }
    
    location / {
        proxy_pass http://127.0.0.1:4000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
    }
}
`;
    await ssh.execCommand(`cat << 'EOF' > /etc/nginx/sites-available/lslife\n${nginxConf}\nEOF`);
    await ssh.execCommand('nginx -t && systemctl reload nginx');
    console.log('Nginx reloaded with SSL!');

  } catch (err) {
    console.error('Error:', err);
  } finally {
    ssh.dispose();
  }
}

main();
