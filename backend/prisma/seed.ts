import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
// 自包含的商家种子数据 (后端独立可部署)
import { merchantsData } from './seed-data.js';

const prisma = new PrismaClient();

const categoryTreeSeed = [
  // 1. 个人闲置
  {
    id: 'cat_idle',
    name: '个人闲置',
    icon: 'shopping-bag',
    iconUrl: '/assets/icons/3d_flat_secondhand.png',
    sortOrder: 1,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'second_hand', // 保持兼容 key
        name: '闲置好物/二手优品',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'brand', label: '品牌', fieldType: 'SELECT', required: true, options: ['Apple', '小米', '华为', '荣耀', 'OPPO', 'vivo', '三星', '其他'] },
          { key: 'model', label: '具体型号', fieldType: 'TEXT', required: false, placeholder: '例: iPhone 15 Pro / 14 Pro Max' },
          { key: 'storage', label: '存储容量', fieldType: 'SELECT', required: true, options: ['128G', '256G', '512G', '1TB'] },
          { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新', '99新', '95新', '9成新', '8成新及以下'] },
        ]),
      },
      {
        id: 'cat_idle_clothing',
        name: '服饰鞋帽',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'cat_dress',
            name: '女装/连衣裙',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'size', label: '尺码', fieldType: 'SELECT', required: true, options: ['XS', 'S', 'M', 'L', 'XL', '2XL', '均码'] },
              { key: 'material', label: '材质', fieldType: 'TEXT', required: false, placeholder: '例: 100%纯棉 / 真丝 / 亚麻' },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新带吊牌', '99新仅试穿', '9成新', '8成新及以下'] },
            ]),
          },
          {
            id: 'cat_shoes',
            name: '鞋靴',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'shoeSize', label: '鞋码', fieldType: 'SELECT', required: true, options: ['35', '36', '37', '38', '39', '40', '41', '42', '43', '44'] },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新盒装', '99新仅上脚', '9成新', '8成新及以下'] },
            ]),
          },
        ],
      },
    ],
  },
  // 2. 房租租售
  {
    id: 'cat_house',
    name: '房租租售',
    icon: 'home',
    iconUrl: '/assets/icons/3d_flat_housing.png',
    sortOrder: 2,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'house', // 保持兼容 key
        name: '住房出租/求租',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'rentType', label: '出租方式', fieldType: 'SELECT', required: true, options: ['整租', '合租', '单间', '商铺/办公'] },
          { key: 'layout', label: '户型结构', fieldType: 'SELECT', required: true, options: ['1室1厅', '2室1厅', '2室2厅', '3室2厅', '4室及以上'] },
          { key: 'decoration', label: '装修程度', fieldType: 'SELECT', required: false, options: ['精装修', '简装修', '毛坯房'] },
        ]),
      },
      {
        id: 'secondhand_house',
        name: '二手房源',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'houseType', label: '房屋类型', fieldType: 'SELECT', required: true, options: ['住宅小区', '自建房/独立栋', '临街商住两用', '公寓/复式'] },
          { key: 'layout', label: '户型结构', fieldType: 'SELECT', required: true, options: ['2室1厅', '3室2厅', '4室2厅及以上', '独栋楼房'] },
          { key: 'area', label: '建筑面积', fieldType: 'TEXT', required: false, placeholder: '例: 120平米 / 200平米' },
          { key: 'propertyRights', label: '产权性质', fieldType: 'SELECT', required: false, options: ['红本商品房', '宅基地/自建', '小产权房', '其他产权'] },
        ]),
      },
      {
        id: 'shop_rent',
        name: '商铺租转',
        sortOrder: 3,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'rentType', label: '出租方式', fieldType: 'SELECT', required: true, options: ['整租转让', '分租合租', '写字楼/办公', '仓库厂房'] },
          { key: 'area', label: '建筑面积', fieldType: 'TEXT', required: false, placeholder: '例: 80平米 / 150平米' },
          { key: 'location', label: '商业地段', fieldType: 'SELECT', required: true, options: ['县城中心商业街', '广场商圈', '小区临街商铺', '吉潭镇/其他乡镇'] },
          { key: 'fee', label: '转让说明', fieldType: 'SELECT', required: false, options: ['面议', '无转让费', '含转让费及设备'] },
        ]),
      },
    ],
  },
  // 3. 家政保洁
  {
    id: 'cat_service',
    name: '家政保洁',
    icon: 'cleaning-services',
    iconUrl: '/assets/icons/3d_flat_cleaning.png',
    sortOrder: 3,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'housekeeping', // 保持兼容 key
        name: '深度保洁/日常保洁',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'billingType', label: '计费方式', fieldType: 'SELECT', required: true, options: ['按小时', '按次', '按面积', '面议'] },
          { key: 'serviceScope', label: '服务区域', fieldType: 'TEXT', required: false, placeholder: '例: 连山县城全域 / 吉潭镇' },
          { key: 'serviceType', label: '服务项目', fieldType: 'SELECT', required: true, options: ['日常保洁', '深度清洁', '开荒保洁', '家电清洗'] },
        ]),
      },
      {
        id: 'moving', // 保持兼容 key
        name: '货运搬家',
        sortOrder: 2,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'vehicleType', label: '选择车型', fieldType: 'SELECT', required: true, options: ['小面包车', '中面包车', '小货车', '中货车', '4.2米大货车'] },
          { key: 'needLabor', label: '搬运服务', fieldType: 'SELECT', required: true, options: ['含全程搬运', '协助搬运', '仅拉货自搬'] },
          { key: 'serviceScope', label: '服务范围', fieldType: 'TEXT', required: false, placeholder: '例: 连山县内 / 连山至周边县市' },
        ]),
      },
    ],
  },
  // 4. 水电维修
  {
    id: 'cat_maintenance',
    name: '水电维修',
    icon: 'build',
    iconUrl: '/assets/icons/3d_flat_repair.png',
    sortOrder: 4,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'maintenance',
        name: '水电/家电上门维修',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'repairType', label: '维修项目', fieldType: 'SELECT', required: true, options: ['电路检修/安装', '水管疏通/更换', '家电上门维修', '房屋防水检修'] },
          { key: 'serviceScope', label: '服务区域', fieldType: 'TEXT', required: false, placeholder: '例: 连山县城全域 / 吉潭镇 / 永和镇' },
        ]),
      },
    ],
  },
  // 5. 水果蔬菜
  {
    id: 'cat_veggies',
    name: '水果蔬菜',
    icon: 'shopping-basket',
    iconUrl: '/assets/icons/3d_flat_produce.png',
    sortOrder: 5,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'veggies',
        name: '生鲜农副特产',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'origin', label: '产地来源', fieldType: 'SELECT', required: true, options: ['连山本地果园', '周边农场', '外地基地'] },
          { key: 'spec', label: '包装规格', fieldType: 'TEXT', required: false, placeholder: '例: 10斤箱装 / 按斤零售' },
          { key: 'deliveryWay', label: '配送方式', fieldType: 'SELECT', required: true, options: ['同城送货上门', '到店自提', '快递包邮'] },
        ]),
      },
    ],
  },
  // 6. 招聘求职
  {
    id: 'cat_job',
    name: '招聘求职',
    icon: 'work',
    iconUrl: '/assets/icons/3d_flat_jobs.png',
    sortOrder: 6,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'job',
        name: '职位招聘',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'jobType', label: '工作类型', fieldType: 'SELECT', required: true, options: ['全职', '兼职', '日结工', '实习'] },
          { key: 'experience', label: '工作经验', fieldType: 'SELECT', required: true, options: ['不限', '1年以内', '1-3年', '3-5年', '5年以上'] },
          { key: 'education', label: '学历要求', fieldType: 'SELECT', required: false, options: ['不限', '初中及以下', '高中/中专', '大专', '本科及以上'] },
        ]),
      },
    ],
  },
  // 7. 租车服务
  {
    id: 'cat_car_rental',
    name: '租车服务',
    icon: 'local-shipping',
    iconUrl: '/assets/icons/3d_flat_car_rental.png',
    sortOrder: 7,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'car_rental',
        name: '汽车/顺风车出租',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'carType', label: '车型选择', fieldType: 'SELECT', required: true, options: ['经济五座轿车', '舒适B级轿车', 'SUV越野车', '7座商务MPV', '面包车/小货车'] },
          { key: 'rentWay', label: '租用方式', fieldType: 'SELECT', required: true, options: ['日租自驾', '月租长租', '带司包车', '顺风拼车'] },
          { key: 'priceInfo', label: '租金说明', fieldType: 'TEXT', required: false, placeholder: '例: 150元/天 或 面议' },
        ]),
      },
    ],
  },
  // 8. 兼职零工
  {
    id: 'cat_part_time',
    name: '兼职零工',
    icon: 'schedule',
    iconUrl: '/assets/icons/3d_flat_parttime.png',
    sortOrder: 8,
    isLeaf: false,
    isActive: true,
    children: [
      {
        id: 'part_time',
        name: '同城临时工/钟点工',
        sortOrder: 1,
        isLeaf: true,
        isActive: true,
        attributeSchema: JSON.stringify([
          { key: 'jobType', label: '兼职岗位', fieldType: 'SELECT', required: true, options: ['小时工/钟点工', '周末兼职', '晚间兼职', '寒暑假工', '临时发传单/促销'] },
          { key: 'billingType', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '周结', '完工结', '月结'] },
          { key: 'salary', label: '报酬待遇', fieldType: 'TEXT', required: false, placeholder: '例: 15元/小时 或 150元/天' },
          { key: 'requirement', label: '简单要求', fieldType: 'SELECT', required: false, options: ['不限男女', '学生优先', '吃苦耐劳', '有相关经验'] },
        ]),
      },
    ],
  },
];

async function seedCategories(nodes: any[], parentId: string | null = null) {
  for (const node of nodes) {
    await prisma.category.upsert({
      where: { id: node.id },
      update: {
        name: node.name,
        icon: node.icon || null,
        iconUrl: node.iconUrl || null,
        parentId,
        sortOrder: node.sortOrder || 0,
        isLeaf: node.isLeaf || false,
        isActive: node.isActive !== undefined ? node.isActive : true,
        attributeSchema: node.attributeSchema || '[]',
      },
      create: {
        id: node.id,
        name: node.name,
        icon: node.icon || null,
        iconUrl: node.iconUrl || null,
        parentId,
        sortOrder: node.sortOrder || 0,
        isLeaf: node.isLeaf || false,
        isActive: node.isActive !== undefined ? node.isActive : true,
        attributeSchema: node.attributeSchema || '[]',
      },
    });

    if (node.children && node.children.length > 0) {
      await seedCategories(node.children, node.id);
    }
  }
}

async function main() {
  console.log('清空旧分类数据...');
  await prisma.post.updateMany({
    where: { categoryId: { not: null } },
    data: { categoryId: null }
  });
  await prisma.category.deleteMany({});
  console.log('旧分类数据清空完毕！');

  console.log('开始导入分类树与动态表单 Schema...');
  await seedCategories(categoryTreeSeed);
  console.log('分类树导入完成！');

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

  const categoryCount = await prisma.category.count();
  const merchantCount = await prisma.merchant.count();
  const productCount = await prisma.product.count();
  console.log(`导入完成: ${categoryCount} 个分类, ${merchantCount} 个商家, ${productCount} 个商品`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
