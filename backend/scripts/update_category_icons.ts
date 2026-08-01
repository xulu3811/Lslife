import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const updates = [
    { name: '数码 3C', iconUrl: '/assets/icons/3d_flat_digital.png' },
    { name: '服饰箱包', iconUrl: '/assets/icons/3d_flat_clothing.png' },
    { name: '日用/家电', iconUrl: '/assets/icons/3d_flat_appliance.png' },
    { name: '美妆个护', iconUrl: '/assets/icons/3d_flat_beauty.png' },
    { name: '母婴儿童', iconUrl: '/assets/icons/3d_flat_maternal.png' },
    { name: '运动 & 交通工具', iconUrl: '/assets/icons/3d_flat_sports.png' },
    { name: '文娱爱好', iconUrl: '/assets/icons/3d_flat_entertainment.png' },
    { name: '其它', iconUrl: '/assets/icons/3d_flat_others.png' }
  ];

  console.log('Starting category updates...');
  for (const item of updates) {
    const res = await prisma.category.updateMany({
      where: { name: item.name },
      data: { iconUrl: item.iconUrl }
    });
    console.log(`Updated ${item.name}: ${res.count} records`);
  }
  console.log('Update complete');
}

main()
  .catch(e => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
