import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
// 自包含的商家种子数据 (后端独立可部署)
import { merchantsData } from './seed-data.js';

const prisma = new PrismaClient();

async function main() {
  console.log('开始导入商家与商品数据...');

  for (const m of merchantsData) {
    const merchant = await prisma.merchant.upsert({
      where: { externalId: m.id },
      update: {},
      create: {
        externalId: m.id,
        name: m.name,
        rating: m.rating,
        distance: m.distance,
        sales: m.sales,
        avgPrice: m.avgPrice,
        tags: JSON.stringify(m.tags),
        deliveryFee: m.deliveryFee,
        deliveryTime: m.deliveryTime,
        logo: m.logo,
        banner: m.banner,
        isFood: m.isFood,
        category: m.category,
        latitude: m.latitude,
        longitude: m.longitude,
        description: m.description,
        address: m.address,
        phone: m.phone,
      },
    });

    for (const item of m.items) {
      await prisma.product.upsert({
        where: { externalId: item.id },
        update: {},
        create: {
          externalId: item.id,
          merchantId: merchant.id,
          name: item.name,
          price: item.price,
          originalPrice: item.originalPrice,
          desc: item.desc,
          sales: item.sales,
          image: item.image,
          category: item.category,
          rating: item.rating,
        },
      });
    }
  }

  const merchantCount = await prisma.merchant.count();
  const productCount = await prisma.product.count();
  console.log(`导入完成: ${merchantCount} 个商家, ${productCount} 个商品`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
