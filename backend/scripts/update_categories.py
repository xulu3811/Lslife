import re
import sys

new_categories = """
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
        id: 'cat_house_rent',
        name: '整租 / 合租',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'house_rent_full',
            name: '整套出租',
            icon: '🏠',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金', fieldType: 'SELECT', required: true, options: ['面议', '元/月', '万元'] },
              { key: 'layout', label: '户型', fieldType: 'SELECT', required: true, options: ['1室1厅1卫', '2室1厅1卫', '3室2厅2卫', '4室及以上', '其他'] },
              { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 80' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['床', '宽带', '电视', '冰箱', '独立卫生间', '洗衣机', '空调', '阳台'] },
            ]),
          },
          {
            id: 'house_rent_share',
            name: '单间合租',
            icon: '🚪',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金', fieldType: 'SELECT', required: true, options: ['面议', '元/月'] },
              { key: 'area', label: '房间面积', fieldType: 'TEXT', required: true, placeholder: '例: 15' },
              { key: 'facilities', label: '房间配套', fieldType: 'MULTI_SELECT', required: false, options: ['床', '衣柜', '书桌', '空调', '独立卫浴', '飘窗'] },
            ]),
          },
          {
            id: 'house_rent_bed',
            name: '床位出租',
            icon: '🛏️',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金', fieldType: 'SELECT', required: true, options: ['面议', '元/月'] },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['储物柜', '空调', '独立插座', '宽带'] },
            ]),
          },
        ],
      },
      {
        id: 'cat_house_sale',
        name: '二手房出售',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'house_sale_residential',
            name: '普通住宅',
            icon: '🏢',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '售价', fieldType: 'SELECT', required: true, options: ['面议', '万元'] },
              { key: 'layout', label: '户型', fieldType: 'SELECT', required: true, options: ['1室1厅1卫', '2室1厅1卫', '3室2厅2卫', '4室及以上', '其他'] },
              { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 120' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['电梯房', '满五唯一', '带车位', '精装修', '近学校'] },
            ]),
          },
          {
            id: 'house_sale_villa',
            name: '别墅/排屋',
            icon: '🏡',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '售价(万元)', fieldType: 'TEXT', required: true, placeholder: '例: 500' },
              { key: 'area', label: '建筑面积', fieldType: 'TEXT', required: true, placeholder: '例: 300' },
              { key: 'facilities', label: '特色', fieldType: 'MULTI_SELECT', required: false, options: ['带院子', '带地下室', '精装修', '双车位'] },
            ]),
          },
        ]
      },
      {
        id: 'cat_house_shop',
        name: '商铺 / 写字楼',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'house_shop_street',
            name: '临街商铺',
            icon: '🏪',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金/售价', fieldType: 'SELECT', required: true, options: ['面议', '元/月', '万元'] },
              { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 200' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['可明火', '外摆区', '带烟道', '独立水电'] },
            ]),
          },
          {
            id: 'house_shop_office',
            name: '写字楼/办公',
            icon: '💻',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金/售价', fieldType: 'SELECT', required: true, options: ['面议', '元/月', '万元'] },
              { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 150' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['中央空调', '免费车位', '拎包入住', '电梯'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_house_factory',
        name: '厂房 / 仓库 / 土地',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'house_fac_factory',
            name: '厂房',
            icon: '🏭',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金/售价', fieldType: 'SELECT', required: true, options: ['面议', '元/月', '万元'] },
              { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 500' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['标准厂房', '三相电', '带行车', '消防验收'] },
            ]),
          },
          {
            id: 'house_fac_warehouse',
            name: '仓库',
            icon: '📦',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金/售价', fieldType: 'SELECT', required: true, options: ['面议', '元/月', '万元'] },
              { key: 'area', label: '面积(平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 300' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['高台库', '恒温恒湿', '可进大车', '叉车服务'] },
            ]),
          },
          {
            id: 'house_fac_land',
            name: '土地/农林',
            icon: '🌾',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金/售价', fieldType: 'SELECT', required: true, options: ['面议', '元/年', '万元'] },
              { key: 'area', label: '面积(亩/平方米)', fieldType: 'TEXT', required: true, placeholder: '例: 10亩' },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['水源充足', '交通便利', '可建温室', '年限长'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_house_short',
        name: '日租 / 短租 / 民宿',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'house_short_homestay',
            name: '旅游民宿',
            icon: '🏖️',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '日租金', fieldType: 'SELECT', required: true, options: ['面议', '元/天'] },
              { key: 'layout', label: '房型', fieldType: 'SELECT', required: true, options: ['大床房', '双床房', '家庭套房', '整栋别墅'] },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['智能门锁', '可做饭', '可带宠物', '免费停车'] },
            ]),
          },
          {
            id: 'house_short_apt',
            name: '短租公寓',
            icon: '🏨',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'price', label: '租金', fieldType: 'SELECT', required: true, options: ['面议', '元/月', '元/周'] },
              { key: 'layout', label: '房型', fieldType: 'SELECT', required: true, options: ['单间', '1室1厅', '2室1厅'] },
              { key: 'facilities', label: '配套', fieldType: 'MULTI_SELECT', required: false, options: ['拎包入住', '洗衣机', '空调', '宽带'] },
            ]),
          }
        ]
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
        id: 'cat_service_daily',
        name: '日常 / 深度保洁',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_daily_normal',
            name: '日常保洁',
            icon: '🧹',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按小时', '按次', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '爽约包赔', '自带工具'] },
            ]),
          },
          {
            id: 'service_daily_deep',
            name: '深度保洁',
            icon: '🧽',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '按次', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['高温杀菌', '去除顽渍', '爽约包赔'] },
            ]),
          },
          {
            id: 'service_daily_glass',
            name: '擦玻璃',
            icon: '✨',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['专业工具', '双面擦拭', '安全保障'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_new',
        name: '开荒保洁',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_new_house',
            name: '新房开荒',
            icon: '🏡',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['去除甲醛', '除胶除漆', '不满意重做'] },
            ]),
          },
          {
            id: 'service_new_rent',
            name: '出租房开荒',
            icon: '🏢',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按平米', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['全面消毒', '清理杂物', '爽约包赔'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_appliance',
        name: '家电清洗',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_appliance_ac',
            name: '空调清洗',
            icon: '❄️',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['深度拆洗', '高温消毒', '不满意重做'] },
            ]),
          },
          {
            id: 'service_appliance_hood',
            name: '油烟机清洗',
            icon: '🍳',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['重油污清除', '免拆洗/深度拆洗', '爽约包赔'] },
            ]),
          },
          {
            id: 'service_appliance_washer',
            name: '洗衣机清洗',
            icon: '👕',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['内筒消毒', '除垢除味', '不满意重做'] },
            ]),
          },
          {
            id: 'service_appliance_fridge',
            name: '冰箱清洗',
            icon: '🧊',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按台', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['除冰除味', '臭氧杀菌', '爽约包赔'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_nanny',
        name: '保姆 / 钟点工',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_nanny_hourly',
            name: '钟点工保洁',
            icon: '⏱️',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按小时', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '健康证', '爽约包赔'] },
            ]),
          },
          {
            id: 'service_nanny_full',
            name: '全职保姆',
            icon: '👩‍🍳',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家保姆', '白班保姆'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '持证上岗', '包换服务'] },
            ]),
          },
          {
            id: 'service_nanny_cook',
            name: '做饭阿姨',
            icon: '🥘',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['上门做饭'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按次', '按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['实名认证', '健康证', '爽约包赔'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_maternity',
        name: '月嫂 / 育儿嫂 / 陪护',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_mat_yuesao',
            name: '月嫂',
            icon: '👶',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月(26天)', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['持母婴护理证', '金牌月嫂', '不满意重做'] },
            ]),
          },
          {
            id: 'service_mat_yuer',
            name: '育儿嫂',
            icon: '🍼',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家服务', '白班服务'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['持育婴师证', '早教经验', '实名认证'] },
            ]),
          },
          {
            id: 'service_mat_nurse',
            name: '医院陪护',
            icon: '🏥',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['医院陪护'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按天', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['持护工证', '专业护理', '实名认证'] },
            ]),
          },
          {
            id: 'service_mat_elderly',
            name: '居家养老陪护',
            icon: '👵',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['住家陪护', '白班陪护'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按月', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['经验丰富', '耐心负责', '实名认证'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_service_moving',
        name: '搬家 / 货运',
        sortOrder: 6,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'service_moving_small',
            name: '小型搬家',
            icon: '📦',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['同城搬家'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按次', '按距离', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['帮忙搬运', '不乱加价'] },
            ]),
          },
          {
            id: 'service_moving_family',
            name: '居民搬家',
            icon: '🚚',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['同城搬家', '跨城搬家'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按车', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['专业团队', '打包服务', '家具拆装'] },
            ]),
          },
          {
            id: 'service_moving_freight',
            name: '货运拉货',
            icon: '🚛',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['同城拉货', '跨城拉货'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['按次', '面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['多种车型', '按时送达'] },
            ]),
          },
          {
            id: 'service_moving_equip',
            name: '设备搬迁',
            icon: '🏗️',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'serviceType', label: '服务类型', fieldType: 'SELECT', required: true, options: ['公司搬迁', '工厂搬迁'] },
              { key: 'billingType', label: '计费标准', fieldType: 'SELECT', required: true, options: ['面议'] },
              { key: 'guarantee', label: '服务保障', fieldType: 'MULTI_SELECT', required: false, options: ['专业设备', '安全保障', '开具发票'] },
            ]),
          }
        ]
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
        id: 'cat_main_lock',
        name: '开锁 / 换锁',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_lock_open',
            name: '防盗门开锁',
            icon: '🚪',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_lock_change',
            name: '换锁芯/指纹锁',
            icon: '🔐',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_lock_car',
            name: '汽车开锁',
            icon: '🚗',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_pipe',
        name: '管道疏通',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_pipe_toilet',
            name: '马桶疏通',
            icon: '🚽',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1个月', '3个月'] },
            ]),
          },
          {
            id: 'main_pipe_kitchen',
            name: '厨房下水疏通',
            icon: '🚰',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1个月', '3个月'] },
            ]),
          },
          {
            id: 'main_pipe_sewer',
            name: '主管道/化粪池',
            icon: '🕳️',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1个月', '3个月'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_electrical',
        name: '水电维修',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_electrical_circuit',
            name: '电路维修',
            icon: '⚡',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_electrical_lamp',
            name: '灯具安装/维修',
            icon: '💡',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_electrical_water',
            name: '水管维修/安装',
            icon: '🚿',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_appliance',
        name: '家电维修',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_appliance_ac',
            name: '空调维修/加氟',
            icon: '❄️',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_appliance_fridge',
            name: '冰箱维修',
            icon: '🧊',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_appliance_washer',
            name: '洗衣机维修',
            icon: '👕',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          },
          {
            id: 'main_appliance_tv',
            name: '电视/影音维修',
            icon: '📺',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['24小时', '仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '3个月', '1年'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_main_waterproof',
        name: '房屋修缮 / 防水',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'main_waterproof_roof',
            name: '楼顶/外墙防水',
            icon: '🧱',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1年', '3年', '5年及以上'] },
            ]),
          },
          {
            id: 'main_waterproof_bath',
            name: '卫生间/阳台漏水',
            icon: '🚿',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1年', '3年', '5年及以上'] },
            ]),
          },
          {
            id: 'main_waterproof_paint',
            name: '粉刷/泥瓦/修补',
            icon: '🖌️',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'calloutFee', label: '上门费', fieldType: 'SELECT', required: true, options: ['无上门费', '有基础检测费'] },
              { key: 'serviceTime', label: '服务时间', fieldType: 'SELECT', required: true, options: ['仅白天'] },
              { key: 'warranty', label: '质保期限', fieldType: 'SELECT', required: true, options: ['无质保', '1年', '3年', '5年及以上'] },
            ]),
          }
        ]
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
        id: 'cat_veg_fresh',
        name: '新鲜水果 / 蔬菜',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'veg_fresh_fruit',
            name: '时令水果',
            icon: '🍎',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
            ]),
          },
          {
            id: 'veg_fresh_veg',
            name: '新鲜蔬菜',
            icon: '🥬',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
            ]),
          },
          {
            id: 'veg_fresh_mushroom',
            name: '菌菇类',
            icon: '🍄',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
            ]),
          },
          {
            id: 'veg_fresh_onion',
            name: '葱姜蒜/辅料',
            icon: '🧅',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 箱, 件' },
            ]),
          }
        ]
      },
      {
        id: 'cat_veg_meat',
        name: '肉禽 / 蛋奶 / 水产',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'veg_meat_pork',
            name: '猪牛羊肉',
            icon: '🥩',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 件' },
            ]),
          },
          {
            id: 'veg_meat_poultry',
            name: '活禽/白条',
            icon: '🐔',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 件' },
            ]),
          },
          {
            id: 'veg_meat_egg',
            name: '蛋奶/豆制品',
            icon: '🥚',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 件' },
            ]),
          },
          {
            id: 'veg_meat_seafood',
            name: '水产/海鲜',
            icon: '🐟',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 件' },
            ]),
          }
        ]
      },
      {
        id: 'cat_veg_grocery',
        name: '粮油 / 副食 / 干货',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'veg_grocery_grain',
            name: '米面粮油',
            icon: '🍚',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 包, 箱' },
            ]),
          },
          {
            id: 'veg_grocery_snack',
            name: '休闲副食/干货',
            icon: '🥜',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 包, 箱' },
            ]),
          },
          {
            id: 'veg_grocery_drink',
            name: '酒水饮料',
            icon: '🍻',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 箱, 件' },
            ]),
          }
        ]
      },
      {
        id: 'cat_veg_local',
        name: '连山特产 / 农资',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'veg_local_special',
            name: '本地特产/土货',
            icon: '🎁',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 盒, 件' },
            ]),
          },
          {
            id: 'veg_local_seed',
            name: '种子/种苗',
            icon: '🌱',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 斤, 颗, 亩' },
            ]),
          },
          {
            id: 'veg_local_fertilizer',
            name: '化肥/农药',
            icon: '🧪',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['门店自提', '同城配送', '快递发货'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 包, 吨' },
            ]),
          },
          {
            id: 'veg_local_machine',
            name: '农机/农具',
            icon: '🚜',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['零售', '二手转让'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['自提', '协商'] },
              { key: 'spec', label: '新旧程度', fieldType: 'TEXT', required: true, placeholder: '例如: 全新, 9成新' },
            ]),
          }
        ]
      },
      {
        id: 'cat_veg_wholesale',
        name: '批发 / 团购',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'veg_wholesale_market',
            name: '大宗批发',
            icon: '🏬',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['大宗批发'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['物流配送', '自提'] },
              { key: 'spec', label: '规格/重量', fieldType: 'TEXT', required: true, placeholder: '例如: 吨, 车, 大件' },
            ]),
          },
          {
            id: 'veg_wholesale_group',
            name: '社区团购',
            icon: '👥',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'saleType', label: '售卖方式', fieldType: 'SELECT', required: true, options: ['社区团购'] },
              { key: 'delivery', label: '配送方式', fieldType: 'SELECT', required: true, options: ['团长提货点自提', '同城配送'] },
              { key: 'spec', label: '起拼份数', fieldType: 'TEXT', required: true, placeholder: '例如: 50份起' },
            ]),
          }
        ]
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
        id: 'cat_job_hospitality',
        name: '餐饮 / 酒店 / 旅游',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_hosp_waiter',
            name: '服务员',
            icon: '🍽️',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_hosp_chef',
            name: '厨师/后厨',
            icon: '👨‍🍳',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_hosp_hotel',
            name: '酒店前台/客房',
            icon: '🏨',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_hosp_guide',
            name: '导游/计调',
            icon: '🚩',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '周末双休'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_blue_collar',
        name: '普工 / 技工 / 生产',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_blue_worker',
            name: '普工/操作工',
            icon: '👷',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '加班补助'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_blue_tech',
            name: '技工(电工/焊工等)',
            icon: '🔧',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '加班补助'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_blue_manage',
            name: '车间管理',
            icon: '📋',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K以下', '3K-5K', '5K-8K', '8K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '加班补助'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_sales',
        name: '销售 / 客服 / 市场',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_sales_rep',
            name: '销售专员',
            icon: '💼',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '底薪+提成', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_sales_cs',
            name: '客服专员',
            icon: '🎧',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '底薪+提成', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_sales_marketing',
            name: '市场拓展/运营',
            icon: '📈',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '底薪+提成', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_admin',
        name: '人事 / 行政 / 财务',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_admin_hr',
            name: '人事专员/助理',
            icon: '📝',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_admin_frontdesk',
            name: '行政/前台',
            icon: '🗂️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_admin_finance',
            name: '财务/出纳/会计',
            icon: '💰',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_admin_manager',
            name: '经理/主管',
            icon: '👨‍💼',
            sortOrder: 4,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假', '节日福利'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_logistics',
        name: '司机 / 物流 / 仓储',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_logistics_driver',
            name: '专职司机',
            icon: '🚘',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-12K', '12K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '高提成'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_logistics_delivery',
            name: '快递/外卖骑手',
            icon: '🛵',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-12K', '12K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '高提成'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          },
          {
            id: 'job_logistics_warehouse',
            name: '仓储/理货',
            icon: '📦',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-12K', '12K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['包吃', '包住', '五险一金', '高提成'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3年以上'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_job_other',
        name: '教育 / 医疗 / 其他',
        sortOrder: 6,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'job_other_edu',
            name: '教师/培训',
            icon: '👩‍🏫',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_other_med',
            name: '医生/护士',
            icon: '⚕️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          },
          {
            id: 'job_other_all',
            name: '其他职位',
            icon: '📌',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'salary', label: '薪资范围', fieldType: 'SELECT', required: true, options: ['面议', '3K-5K', '5K-8K', '8K-10K', '10K以上'] },
              { key: 'benefits', label: '福利待遇', fieldType: 'MULTI_SELECT', required: false, options: ['五险一金', '周末双休', '带薪年假'] },
              { key: 'experience', label: '经验要求', fieldType: 'SELECT', required: true, options: ['不限', '1-3年', '3-5年', '5年以上'] },
            ]),
          }
        ]
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
        id: 'cat_car_suv',
        name: '轿车 / SUV 出租',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'car_suv_sedan',
            name: '轿车',
            icon: '🚗',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['时租', '日租', '月租'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 丰田卡罗拉 / 大众速腾' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['自驾', '代驾'] },
            ]),
          },
          {
            id: 'car_suv_suv',
            name: 'SUV/越野车',
            icon: '🚙',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['时租', '日租', '月租'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 哈弗H6 / 丰田汉兰达' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['自驾', '代驾'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_car_luxury',
        name: '婚车 / 豪车租赁',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'car_luxury_wedding',
            name: '婚车车队',
            icon: '🎀',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['半日租', '日租'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 奔驰S级 / 宝马7系 / 车队打包' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['代驾 (含司机)', '自驾'] },
            ]),
          },
          {
            id: 'car_luxury_sport',
            name: '跑车/豪车',
            icon: '🏎️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['半日租', '日租'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 保时捷911 / 法拉利' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['代驾 (含司机)', '自驾'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_car_bus',
        name: '大巴 / 商务包车',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'car_bus_mpv',
            name: '商务MPV',
            icon: '🚐',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['按趟', '日租', '包月'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 别克GL8 / 传祺M8' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['代驾 (含司机)', '自驾'] },
            ]),
          },
          {
            id: 'car_bus_bus',
            name: '大巴/中巴',
            icon: '🚌',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['按趟', '日租', '包月'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 丰田考斯特 / 50座大巴' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['代驾 (含司机)'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_car_truck',
        name: '货车 / 工程车 / 农机',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'car_truck_truck',
            name: '小货车/卡车',
            icon: '🚚',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['时租/按次', '日租', '包月'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 4.2米箱货 / 蓝牌轻卡' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['自带操作员/司机', '无需司机'] },
            ]),
          },
          {
            id: 'car_truck_heavy',
            name: '工程机械',
            icon: '🏗️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['时租/按次', '日租', '包月'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 挖掘机 / 吊车 / 叉车' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['自带操作员/司机', '无需司机'] },
            ]),
          }
        ]
      },
      {
        id: 'cat_car_ebike',
        name: '电动车 / 摩托车',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'car_ebike_scooter',
            name: '电动两轮/三轮',
            icon: '🛵',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['时租', '日租', '月租'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 雅迪两轮 / 三轮车' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['自驾'] },
            ]),
          },
          {
            id: 'car_ebike_moto',
            name: '摩托车',
            icon: '🏍️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'rentType', label: '租期类型', fieldType: 'SELECT', required: true, options: ['日租', '月租'] },
              { key: 'carModel', label: '车辆品牌/型号', fieldType: 'TEXT', required: true, placeholder: '例如: 本田 / 宝马摩托' },
              { key: 'driver', label: '是否带司机', fieldType: 'SELECT', required: true, options: ['自驾'] },
            ]),
          }
        ]
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
        id: 'cat_pt_temp',
        name: '日结 / 临时工',
        sortOrder: 1,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_temp_daily',
            name: '日结临时工',
            icon: '👷',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['白天全天', '夜班'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 150元/天' },
            ]),
          },
          {
            id: 'pt_temp_hourly',
            name: '小时工',
            icon: '⏱️',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['自由时间', '指定时段'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 20元/小时' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_promo',
        name: '传单 / 促销 / 充场',
        sortOrder: 2,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_promo_flyer',
            name: '派发传单',
            icon: '📄',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '特定时间段'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 20元/小时' },
            ]),
          },
          {
            id: 'pt_promo_sales',
            name: '促销员/导购',
            icon: '📢',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '周结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '法定节假日'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 150元/天+提成' },
            ]),
          },
          {
            id: 'pt_promo_audience',
            name: '充场/会展协助',
            icon: '🎭',
            sortOrder: 3,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['特定时间段'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 50元/半天' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_hotel',
        name: '餐饮 / 客房兼职',
        sortOrder: 3,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_hotel_waiter',
            name: '餐厅服务员',
            icon: '🍽️',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '周结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '晚上', '用餐高峰期'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 18元/小时' },
            ]),
          },
          {
            id: 'pt_hotel_kitchen',
            name: '后厨帮工/洗碗',
            icon: '🧼',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['日结', '周结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['全天', '半天'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 120元/天' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_tutor',
        name: '家教 / 艺术培训',
        sortOrder: 4,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_tutor_school',
            name: '中小学家教',
            icon: '📚',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结', '月结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '晚上'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 50元/小时' },
            ]),
          },
          {
            id: 'pt_tutor_art',
            name: '艺术/特长培训',
            icon: '🎨',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结', '月结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['周末', '自由时间'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 80元/节课' },
            ]),
          }
        ]
      },
      {
        id: 'cat_pt_errand',
        name: '跑腿 / 代办',
        sortOrder: 5,
        isLeaf: false,
        isActive: true,
        children: [
          {
            id: 'pt_errand_delivery',
            name: '同城代送/代买',
            icon: '🛵',
            sortOrder: 1,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['随时', '自由时间'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 15元/单' },
            ]),
          },
          {
            id: 'pt_errand_service',
            name: '代排队/代办',
            icon: '🧍',
            sortOrder: 2,
            isLeaf: true,
            isActive: true,
            attributeSchema: JSON.stringify([
              { key: 'payMethod', label: '结算方式', fieldType: 'SELECT', required: true, options: ['完工结'] },
              { key: 'workTime', label: '工作时段', fieldType: 'SELECT', required: true, options: ['指定时间'] },
              { key: 'salary', label: '薪资单价', fieldType: 'TEXT', required: true, placeholder: '例如: 30元/小时' },
            ]),
          }
        ]
      },
    ],
  },
"""

seed_file = "d:/LsLife/backend/prisma/seed.ts"
with open(seed_file, "r", encoding="utf-8") as f:
    content = f.read()

# Find the start of // 2. 房屋租售
start_idx = content.find("  // 2. 房屋租售")
# Find the end of the array `];`
end_idx = content.find("];", start_idx)

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + new_categories + content[end_idx:]
    with open(seed_file, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Categories updated successfully.")
else:
    print("Could not find the target block.")
