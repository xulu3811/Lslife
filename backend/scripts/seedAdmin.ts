import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  const username = process.argv[2] || 'root';
  const password = process.argv[3] || 'Maxence2468;'; // Using the password the user provided as default

  const hashedPassword = await bcrypt.hash(password, 10);

  const admin = await prisma.adminUser.upsert({
    where: { username },
    update: { password: hashedPassword, role: 'superadmin' },
    create: {
      username,
      password: hashedPassword,
      role: 'superadmin',
    },
  });

  console.log(`\n✅ 管理员账户配置成功`);
  console.log(`  用户名: ${admin.username}`);
  console.log(`  角色:   ${admin.role}\n`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
