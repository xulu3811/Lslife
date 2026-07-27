import 'dotenv/config';
import { PrismaClient } from '@prisma/client';
// 自包含的商家种子数据 (后端独立可部署)
import { merchantsData } from './seed-data.js';

const prisma = new PrismaClient();

const categoryTreeSeed = [
  // 1. 个人闲置（全栈升级：8大二级大类及三级叶子分类）
  {
    id: 'cat_idle',
    name: '个人闲置',
    icon: 'shopping-bag',
    iconUrl: '/assets/icons/3d_flat_secondhand.png',
    sortOrder: 1,
    isLeaf: false,
    isActive: true,
    isHot: true,
    children: [
      // 1.1 数码 3C（核心高频交易区）
      {
        id: 'cat_3c',
        name: '数码 3C',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'second_hand', // 兼容历史手机数码 key
            name: '手机 / 平板电脑',
            icon: '📱',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'brand', label: '品牌', fieldType: 'SELECT', required: true, options: ['Apple/苹果', '小米', '华为', '荣耀', 'OPPO', 'vivo', '三星', '联想/荣耀平板', '其他'] },
              { key: 'model', label: '具体型号', fieldType: 'TEXT', required: true, placeholder: '例: iPhone 15 Pro / iPad Air 5 / 小米14' },
              { key: 'storage', label: '存储容量', fieldType: 'SELECT', required: true, options: ['64GB', '128GB', '256GB', '512GB', '1TB及以上'] },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新未拆', '99新充新', '95新轻微痕迹', '9成新', '8成新及以下'] },
            ]),
          },
          {
            id: 'cat_3c_pc',
            name: '电脑 / 电脑配件',
            icon: '💻',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '硬件类型', fieldType: 'SELECT', required: true, options: ['笔记本电脑', '台式机/整机', '显卡/显卡拓展', 'CPU/主板/内存/硬盘', '显示器/键盘/鼠标/外设'] },
              { key: 'brand', label: '品牌型号', fieldType: 'TEXT', required: true, placeholder: '例: 联想ThinkPad / MacBook Pro / RTX 4060 / AOC显示器' },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新', '99新充新', '95新正常使用', '9成新', '8成新及以下'] },
            ]),
          },
          {
            id: 'cat_3c_camera',
            name: '摄影 / 相机无人机',
            icon: '📷',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'brand', label: '品牌', fieldType: 'SELECT', required: true, options: ['索尼 Sony', '佳能 Canon', '尼康 Nikon', '富士 Fujifilm', '大疆 DJI', '理光 / 徕卡 / 其他'] },
              { key: 'type', label: '器械品类', fieldType: 'SELECT', required: true, options: ['微单相机/单反机身', '相机镜头/变焦定焦', '无人机/航拍器', '运动相机/GoPro/Osmo', '三脚架/闪光灯等摄影配件'] },
              { key: 'condition', label: '成色与快门数', fieldType: 'SELECT', required: true, options: ['全新箱全', '99新仅拆快门极少', '95新无无霉无雾', '9成新有正常磨损'] },
            ]),
          },
          {
            id: 'cat_3c_audio',
            name: '影音 / 智能数码',
            icon: '🎧',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '智能设备品类', fieldType: 'SELECT', required: true, options: ['蓝牙耳机/AirPods/头戴式耳机', '智能手表/运动手环/Apple Watch', '蓝牙音箱/智能音箱/HomePod', '路由器/NAS网络存储/投影仪', 'VR/AR头显眼镜'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: false, placeholder: '例: Apple / 索尼 / 华为 / 哈曼卡顿 / 小米' },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新未拆封', '99新箱全', '95新功能完好', '9成新及以下'] },
            ]),
          },
        ],
      },
      // 1.2 服饰箱包（高频且易分类）
      {
        id: 'cat_clothing',
        name: '服饰箱包',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_dress', // 兼容原 ID
            name: '女装 / 男装服饰',
            icon: '👗',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'gender', label: '适用对象', fieldType: 'SELECT', required: true, options: ['时尚女装 (外套/连衣裙/上衣/裤装)', '潮流男装 (夹克/卫衣/T恤/休闲裤/西装)', '情侣装/中性潮流服饰'] },
              { key: 'size', label: '尺码', fieldType: 'SELECT', required: true, options: ['XS', 'S', 'M', 'L', 'XL', '2XL', '3XL及以上', '均码'] },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新带吊牌/水洗未穿', '99新仅试穿/洗涤一次', '9成新无污渍破损', '8成新及以下'] },
            ]),
          },
          {
            id: 'cat_shoes',
            name: '鞋靴 / 运动休闲鞋',
            icon: '👟',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '鞋类款式', fieldType: 'SELECT', required: true, options: ['运动鞋/跑鞋/篮球鞋', '休闲鞋/板鞋/帆布鞋', '皮鞋/正装鞋/马丁靴', '凉鞋/拖鞋/高跟鞋'] },
              { key: 'shoeSize', label: '鞋码 (EUR码)', fieldType: 'SELECT', required: true, options: ['35及以下', '36', '37', '38', '39', '40', '41', '42', '43', '44', '45及以上'] },
              { key: 'brand', label: '品牌', fieldType: 'TEXT', required: false, placeholder: '例: Nike / Adidas / New Balance / 亚瑟士 / 百丽' },
              { key: 'condition', label: '上脚成色', fieldType: 'SELECT', required: true, options: ['全新原盒带吊牌', '99新仅室内试穿', '95新轻微踩地痕迹', '9成新及以下'] },
            ]),
          },
          {
            id: 'cat_bag',
            name: '箱包 / 皮具拉杆箱',
            icon: '👜',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '箱包款式', fieldType: 'SELECT', required: true, options: ['双肩背包/电脑包', '单肩斜挎包/链条包', '手提包/托特包/公文包', '旅行箱/拉杆箱(20寸/24寸/28寸)', '零钱包/卡包/腰包'] },
              { key: 'brand', label: '品牌', fieldType: 'TEXT', required: false, placeholder: '例: 小米 / 新秀丽 / Coach / 蔻驰 / MK / 蔻驰' },
              { key: 'condition', label: '五金与皮质成色', fieldType: 'SELECT', required: true, options: ['全新带吊牌', '99新无磨损完美', '95新轻微正常使用痕迹', '9成新及以下'] },
            ]),
          },
          {
            id: 'cat_luxury',
            name: '配饰 / 腕表首饰',
            icon: '⌚',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '饰品分类', fieldType: 'SELECT', required: true, options: ['手表/机械表/石英表', '项链/吊坠/手链/手镯', '戒指/耳环/首饰盒', '太阳镜/墨镜/近视镜框', '帽子/围巾/皮带配饰'] },
              { key: 'source', label: '来源及凭证', fieldType: 'SELECT', required: true, options: ['专柜购买(发票保卡齐全)', '国内平台购买有订单凭证', '免税店/海外直邮凭证', '闲置转让/礼品无需凭证'] },
              { key: 'condition', label: '成色', fieldType: 'SELECT', required: true, options: ['全新未戴', '99新全套靓品', '95新微痕', '9成新'] },
            ]),
          },
        ],
      },
      // 1.3 居家日用 & 家电（本地社区刚需）
      {
        id: 'cat_home_goods',
        name: '居家家电',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_home_appliance',
            name: '家用电器 (大/小家电)',
            icon: '📺',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '家电品类', fieldType: 'SELECT', required: true, options: ['冰箱/冰柜/冷饮机', '洗衣机/烘干机/洗烘套装', '液晶电视/投影仪/回音壁', '空调(挂机/立柜式/移动空调)', '微波炉/烤箱/空气炸锅/咖啡机', '吸尘器/扫地机器人/空气净化器'] },
              { key: 'brand', label: '品牌型号', fieldType: 'TEXT', required: true, placeholder: '例: 美的 / 格力 / 海尔 / 小米 / 西门子' },
              { key: 'condition', label: '年限与运行状态', fieldType: 'SELECT', required: true, options: ['全新未安装(带保修)', '使用1年内(功能完美在保)', '使用1-3年正常无修', '使用3年以上正常运转'] },
              { key: 'delivery', label: '交接提货', fieldType: 'SELECT', required: true, options: ['买家同城上门自提', '卖家包送或协助找货运拉车'] },
            ]),
          },
          {
            id: 'cat_home_furniture',
            name: '家具 / 桌椅床柜',
            icon: '🛋️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '家具款式', fieldType: 'SELECT', required: true, options: ['床/床垫/榻榻米', '沙发(布艺/真皮/科技布)/茶几', '餐桌/书桌/电脑桌/电竞椅/办公椅', '衣柜/书柜/鞋柜/储物置物架'] },
              { key: 'material', label: '主要材质', fieldType: 'SELECT', required: false, options: ['实木/整木', '布艺/绒面', '头层真皮/仿皮', '优质板式/密度板', '金属/铁艺/钢化玻璃'] },
              { key: 'delivery', label: '搬运提示', fieldType: 'SELECT', required: true, options: ['同城买家自提(需自行拆卸搬运)', '买家自提(卖家协助拆卸电梯房方便)', '卖家协商包货运包送'] },
            ]),
          },
          {
            id: 'cat_home_daily',
            name: '日用 / 厨具收纳',
            icon: '🧹',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '日杂品类', fieldType: 'SELECT', required: true, options: ['厨具餐具/锅具刀具/水杯茶具', '收纳整理箱/衣架/储物盒', '床上四件套/夏凉被/枕头毛毯', '灯具/装饰摆件/绿植花盆/挂画', '闲置节庆礼品/家庭日杂小件'] },
              { key: 'condition', label: '成色与卫生', fieldType: 'SELECT', required: true, options: ['全新带原包装/未洗未使用', '99新仅拆封清洗', '9成新干净好用'] },
            ]),
          },
        ],
      },
      // 1.4 美妆个护
      {
        id: 'cat_beauty',
        name: '美妆个护',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        isHot: false,
        children: [
          {
            id: 'cat_beauty_skin',
            name: '护肤 / 彩妆美妆',
            icon: '💄',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '美妆护肤品类', fieldType: 'SELECT', required: true, options: ['面部精华/乳液/面霜/眼霜', '爽肤水/精粹水/防晒隔离', '口红/唇釉/润唇膏', '粉底液/气垫/遮瑕/定妆散粉', '眼影盘/腮红/高光修容盘', '护肤彩妆精美礼盒套装'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: true, placeholder: '例: 雅诗兰黛 / 兰蔻 / 海蓝之谜 / YSL / 珀莱雅' },
              { key: 'expiry', label: '保质期与余量', fieldType: 'SELECT', required: true, options: ['全新未拆封(保质期1年以上)', '全新未拆封(保质期1年内近期无过期)', '仅手背试色/喷试(余量99%)', '已拆封使用(余量80%以上卫生完好)'] },
            ]),
          },
          {
            id: 'cat_beauty_care',
            name: '香水 / 洗护美发',
            icon: '🧴',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '洗护与仪器品类', fieldType: 'SELECT', required: true, options: ['大牌香水/淡香水/身体香氛喷雾', '洗发水/护发素/精油/身体乳', '戴森吹风机/卷发棒/直发梳', '美容仪/射频导入仪/洁面仪', '化妆刷具套装/收纳包/日用个护工具'] },
              { key: 'condition', label: '状态说明', fieldType: 'SELECT', required: true, options: ['全新未拆未用(全套在盒)', '99新仅试用1-2次无划痕', '95新日常自用功能完美'] },
            ]),
          },
        ],
      },
      // 1.5 母婴儿童
      {
        id: 'cat_baby',
        name: '母婴儿童',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_baby_clothes',
            name: '童装 / 童鞋配饰',
            icon: '🧦',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'age', label: '适合年龄段', fieldType: 'SELECT', required: true, options: ['0-1岁婴儿宝宝', '1-3岁幼儿', '3-6岁学龄前小童', '6-12岁大童'] },
              { key: 'type', label: '款式类别', fieldType: 'SELECT', required: true, options: ['童装连体衣/爬服套装', '外套/棉服/羽绒服', '学步鞋/儿童运动鞋/凉鞋', '配饰/睡袋/围巾帽子'] },
              { key: 'condition', label: '卫生与成色', fieldType: 'SELECT', required: true, options: ['全新带吊牌/下水洗净未穿', '99新穿过1-2次极新', '9成新干净整洁无污渍无破损'] },
            ]),
          },
          {
            id: 'cat_baby_stroller',
            name: '婴儿车 / 床与安全座椅',
            icon: '🍼',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '大件母婴品类', fieldType: 'SELECT', required: true, options: ['婴儿推车/高景观车/遛娃神车/折叠轻便车', '儿童汽车安全座椅(3C欧盟认证)', '实木婴儿床/拼接床/便携折叠床', '婴儿餐椅/摇摇椅/学步车'] },
              { key: 'brand', label: '品牌', fieldType: 'TEXT', required: false, placeholder: '例: 好孩子 / 宝得适 Britax / Stokke / 巧儿宜 / 康贝' },
              { key: 'condition', label: '使用成色', fieldType: 'SELECT', required: true, options: ['全新箱全', '95新极干净无破损件全', '9成新正常痕迹件完好', '8成新及以下'] },
              { key: 'delivery', label: '交易方式', fieldType: 'SELECT', required: true, options: ['同城上门看货自提', '协商车送或快递寄送'] },
            ]),
          },
          {
            id: 'cat_baby_toy',
            name: '儿童玩具 / 绘本早教',
            icon: '🧸',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '玩具绘本品类', fieldType: 'SELECT', required: true, options: ['乐高拼装积木/益智拼图/拼装玩具', '中外经典儿童绘本/分级阅读英语/立体书', '儿童电动四轮车/滑板车/无脚踏平衡车', '点读笔(毛毛虫/小达人等)/早教故事机', '毛绒公仔/安抚玩具/过家家玩具组'] },
              { key: 'condition', label: '完整度与成色', fieldType: 'SELECT', required: true, options: ['全新未拆盒装', '99新全套无缺件无涂画', '9成新有正常玩耍翻书痕迹'] },
            ]),
          },
          {
            id: 'cat_baby_care',
            name: '孕产 / 喂养洗护',
            icon: '🤰',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '护理用品类别', fieldType: 'SELECT', required: true, options: ['吸奶器(美德乐/新安怡等)/暖奶器/恒温调奶器', '消毒锅/辅食机/宝宝餐具及洗浴盆', '孕妇装/防辐射服/哺乳衣/收腹带托腹带', '全新未拆封待产包配件及消耗品'] },
              { key: 'condition', label: '卫生与使用说明', fieldType: 'SELECT', required: true, options: ['全新未拆封(涉及直接进嘴用品建议全新)', '主机99新极少用(配全新配件/已严格蒸汽消毒)', '95新整洁卫生无污染'] },
            ]),
          },
        ],
      },
      // 1.6 运动户外 & 交通工具
      {
        id: 'cat_sports',
        name: '运动出行',
        sortOrder: 6,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_sports_bike',
            name: '自行车 / 二手电动车',
            icon: '🚴',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '车辆品类 (同城极高频需求)', fieldType: 'SELECT', required: true, options: ['两轮电动车/电瓶车 (雅迪/九号/小牛/爱玛等)', '山地自行车/公路车/折叠变速车 (捷安特/美利达/大行等)', '电动滑板车/平衡车/代驾折叠车', '三轮电动车/老年代步车'] },
              { key: 'brandModel', label: '品牌与型号', fieldType: 'TEXT', required: true, placeholder: '例: 九号F90M / 捷安特ATX777 / 雅迪冠能 / 小牛NQI' },
              { key: 'batteryInfo', label: '电池与续航 (电动车必填)', fieldType: 'SELECT', required: false, options: ['原装锂电池(续航60km以上)', '原装铅酸电池(续航40-60km)', '近期更换新电池(动力强劲)', '人力自行车无需电池'] },
              { key: 'licenseInfo', label: '上牌与证照情况', fieldType: 'SELECT', required: true, options: ['有正规收据发票/已上同城合法白牌或绿牌可过户', '有发票合格证/未上牌可直接上牌', '闲置车转让验车无误当面交接'] },
              { key: 'condition', label: '车况与成色', fieldType: 'SELECT', required: true, options: ['全新充新车99新', '95新车况优秀刹车轮胎完美', '9成新正常实用无大修无水泡', '8成新实用代步车'] },
            ]),
          },
          {
            id: 'cat_sports_gym',
            name: '运动 / 健身球类',
            icon: '🏋️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '器材运动品类', fieldType: 'SELECT', required: true, options: ['跑步机/椭圆机/划船机/动感单车', '哑铃/杠铃/杠铃凳/壶铃/拉力器', '羽毛球拍/网球拍/乒乓球拍/高尔夫球杆', '篮球/足球/排球/滑板/轮滑鞋', '瑜伽垫/泡沫轴/健身护具衣物'] },
              { key: 'condition', label: '器械成色', fieldType: 'SELECT', required: true, options: ['全新未拆', '95新使用正常无故障', '9成新实用好用'] },
              { key: 'delivery', label: '交接方式', fieldType: 'SELECT', required: true, options: ['重型健身器械需买家上门自提', '同城轻物可面交或快递', '卖家协商包运送'] },
            ]),
          },
          {
            id: 'cat_sports_camp',
            name: '露营 / 渔具户外',
            icon: '⛺',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '户外装备品类', fieldType: 'SELECT', required: true, options: ['帐篷/天幕/露营推车营地车', '户外折叠椅/蛋卷桌/睡袋/防潮垫', '台钓竿/路亚竿/溪流竿/渔轮/钓箱渔具', '卡式炉/户外气炉/钛杯茶具餐具', '滑雪板/滑雪服/雪镜登山杖背包'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: false, placeholder: '例: 挪客 Naturehike / 牧高笛 / 探险者 / 光威 / 达瓦' },
              { key: 'condition', label: '成色与完整度', fieldType: 'SELECT', required: true, options: ['全新未拆', '99新仅户外使用一次无破损无破洞', '95新成色良好', '9成新正常痕迹'] },
            ]),
          },
        ],
      },
      // 1.7 文娱爱好
      {
        id: 'cat_hobby',
        name: '文娱爱好',
        sortOrder: 7,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_hobby_figure',
            name: '游戏 / 盲盒手办',
            icon: '🎮',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '游戏与手办品类', fieldType: 'SELECT', required: true, options: ['Switch/PS5/SteamDeck等游戏主机器械', 'Switch/PS4/PS5游戏卡带与光盘', '泡泡玛特/盲盒/隐藏款手办摆件', '高达模型/动漫正比例手办/粘土人景品', '二次元谷子/徽章/亚克力/同人周边', '宝可梦/奥特曼卡牌/桌游棋牌'] },
              { key: 'version', label: '版本属性', fieldType: 'SELECT', required: true, options: ['国行/日版/港版官方正品(盒证齐全)', '官方正品(无盒仅本体)', '开盒未拆袋确认款(盲盒适用)', '高性价散货/高仿请如实声明'] },
              { key: 'condition', label: '成色说明', fieldType: 'SELECT', required: true, options: ['全新未拆封', '99新仅拆封试玩/拆摆好品无瑕疵', '95新无缺件功能正常', '微瑕/有缺件(请描述详情)'] },
            ]),
          },
          {
            id: 'cat_hobby_book',
            name: '图书 / 教材文具',
            icon: '📚',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '图书文具类别', fieldType: 'SELECT', required: true, options: ['考研考公/法考/教资等职业资格复习资料教材', '大学各学科专业教材/参考书', '中外文学小说/散文/历史传记/心理管理书籍', '绝版连环画/经典漫画全集/杂志丛书', '钢笔/凌美/计算器等电子或文具用品'] },
              { key: 'condition', label: '书况与涂写情况', fieldType: 'SELECT', required: true, options: ['全新塑封/未折未写', '99新极干净无划线无笔记', '95新轻微翻阅无破损有少量铅笔勾画', '85新有正常笔记或重点划线(适合备考使用)'] },
            ]),
          },
          {
            id: 'cat_hobby_music',
            name: '乐器 / 饭圈周边',
            icon: '🎸',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '乐器或周边品类', fieldType: 'SELECT', required: true, options: ['木吉他/民谣吉他/电吉他/尤克里里', '电钢琴/电子琴/传统钢琴/合成器', '古筝/二胡/笛箫/小提琴等管弦乐器', '乐器音箱/吉他包/调音台乐谱等配件', '饭圈偶像应援棒/官方小卡/专辑CD/明信片周边'] },
              { key: 'brand', label: '品牌名称', fieldType: 'TEXT', required: false, placeholder: '例: 雅马哈 / 卡西欧 / 芬达 / 罗兰 / 泰勒 / 星海' },
              { key: 'condition', label: '乐器成色与音准', fieldType: 'SELECT', required: true, options: ['全新未拆封', '99新琴体完美音准好音色靓', '95新轻微正常使用无磕碰', '9成新正常演奏'] },
            ]),
          },
          {
            id: 'cat_hobby_pet',
            name: '宠物 / 闲置用品(不含活体)',
            icon: '🐱',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'notice', label: '特别提示', fieldType: 'SELECT', required: true, options: ['本分类仅限闲置用品及合封日粮转让(严禁发布活体猫犬交易避免纠纷)'] },
              { key: 'type', label: '宠物用品品类', fieldType: 'SELECT', required: true, options: ['猫砂盆/自动猫砂机/猫爬架/猫抓板/猫别墅', '狗笼/航空箱/外带便携包/宠物推车', '鱼缸/水族箱/加热棒/水泵过滤器造景', '自动喂食器/智能饮水机/宠物烘干箱/吹水机', '全新未拆封猫粮狗粮/罐头零食/常备品'] },
              { key: 'condition', label: '成色与清洁消毒说明', fieldType: 'SELECT', required: true, options: ['全新未拆封未使用', '99新使用极短已彻底清洗无异味消毒完好', '95新使用正常卫生良好'] },
            ]),
          },
        ],
      },
      // 1.8 票务卡券（虚拟或权益类）
      {
        id: 'cat_ticket',
        name: '票务卡券',
        sortOrder: 8,
        isLeaf: false,
        isActive: true,
        isHot: true,
        children: [
          {
            id: 'cat_ticket_shop',
            name: '购物卡 / 消费券与健身卡',
            icon: '🎟️',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: true,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '卡券品类', fieldType: 'SELECT', required: true, options: ['商超购物卡 (京东E卡/猫超卡/沃尔玛/大润发等提货券)', '餐饮美食卡券 (星巴克/瑞幸/喜茶/麦当劳/肯德基/同城餐厅卷)', '同城健身房年卡/季卡/月卡/次卡转让', '中石化/中石油加油卡及其他实体代金券'] },
              { key: 'valueInfo', label: '卡面总额或剩余次数/期限', fieldType: 'TEXT', required: true, placeholder: '例: 100元面值 / 健身卡剩8个月 / 游泳剩15次' },
              { key: 'expiry', label: '到期期限', fieldType: 'TEXT', required: true, placeholder: '例: 2026-12-31 前有效 / 长期有效不过期' },
              { key: 'tradeWay', label: '交接与过户方式', fieldType: 'SELECT', required: true, options: ['线上发送电子兑换码/二维码激活', '实体卡券同城当面交易面交/快递包邮', '到门店办理过户(卖家承担过户费/通用非实名)'] },
            ]),
          },
          {
            id: 'cat_ticket_movie',
            name: '演出 / 电影与景区门票',
            icon: '🎬',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'type', label: '门票类型', fieldType: 'SELECT', required: true, options: ['同城电影通兑券/选座代下', '演唱会/音乐会/话剧/脱口秀门票', '同城景区门票/游乐园/水上乐园/温泉门票', '漫展/车展/博览会通票'] },
              { key: 'dateInfo', label: '演出或使用具体日期时间', fieldType: 'TEXT', required: true, placeholder: '例: 2026-08-20 晚19:30 / 周末法定节假日通用' },
              { key: 'realName', label: '实名入场规则', fieldType: 'SELECT', required: true, options: ['非实名通用电子券/纸质票(直接出示转赠即可)', '实名转赠票(通过官方小程序转赠给买家身份证)', '需协助录入实名信息入场'] },
            ]),
          },
        ],
      },
      // 1.9 其它兜底
      {
        id: 'cat_other',
        name: '其它兜底',
        sortOrder: 9,
        isLeaf: false,
        isActive: true,
        isHot: false,
        children: [
          {
            id: 'cat_other_idle',
            name: '其他闲置 / 冷门物品',
            icon: '📦',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            isHot: false,
            attributeSchema: JSON.stringify([
              { key: 'itemType', label: '物品名称/类别描述', fieldType: 'TEXT', required: true, placeholder: '例: 闲置工艺收藏摆件 / 冷门五金五金工具 / DIY手工材料 / 闲置办公用品' },
              { key: 'condition', label: '成色与状态说明', fieldType: 'SELECT', required: true, options: ['全新未拆未使用', '99新充新好品', '9成新正常功能完好', '8成新及以下实用级'] },
              { key: 'tradeWay', label: '交接方式', fieldType: 'SELECT', required: true, options: ['同城当面交易面交自提', '快递寄送/运费协商', '双方协商'] },
            ]),
          },
        ],
      },
    ],
  },
  // 2. 房屋租售
  {
    id: 'cat_house',
    name: '房屋租售',
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
        isHot: node.isHot !== undefined ? node.isHot : false,
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
        isHot: node.isHot !== undefined ? node.isHot : false,
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

  if (process.env.SEED_MOCK === 'true') {
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
  } else {
    console.log('跳过模拟商家与商品数据导入 (如需导入模拟数据请设置 SEED_MOCK=true)');
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
