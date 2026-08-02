# 连山壮瑶同城 (LianShan Local Services) - V6.0 存档与交接准则

## 📌 项目状态与客观现实 (Project State & Objective Reality)
本项目是一款针对县域级市场（目标覆盖人口 3~10 万，并发支撑 3 万）量身打造的**本地同城生活服务交易与通讯平台**。
历经多个版本的爆发式演进，项目现已全面升级至 **V5.0 (Joybuy 级商业视觉体验与全品类覆盖版)**。
我们在具备“1:1 闲鱼动态发布 + 本地同城分类”底层逻辑的同时，打通了极其成熟的购物车与多模态即时通讯体系，并完成了客户端视觉体验的脱胎换骨。
现阶段系统已极其稳定，且已成功部署至阿里云生产环境。客户端核心产物归档于 `D:\LsLife\releases\`。

---

## 🏗 核心模块、业务逻辑与算法 (Core Architecture & Logic)

### 1. 交易流转与购物车引擎 (Commerce & Cart)
*   **购物车多选与防跨店算法**：购物车 (`CartViewModel`) 采用精确的条目级 ID 追踪 (`selectedEntryIds`)，支持单个商品独立多选与单选。底层算法严格校验商户归属，勾选新商铺商品时自动互斥清空其他商铺，确保订单生成的合法性。
*   **结算状态同步 (Lifecycle Sync)**：结算页 (`CheckoutScreen`) 与收货地址管理深度联动，利用 `LifecycleEventObserver` 捕获生命周期 `ON_RESUME` 事件，实现地址添加后的无缝自动回填。

### 2. 深度即时通讯与交易存证体系 (Deep IM & Transaction System)
*   **多模态并发流**：支持图文、语音、以及**商品联动卡片 (`post_card`)**，买家在商品详情页可一键静默发送商品快照。
*   **图片极速流转算法 (`image`)**：突破 HTTP 瓶颈，采用本地无损压缩 -> `base64:` 文本流 -> WebSocket 直连服务端的传输算法。客户端 (`ChatScreen`) 拦截字节流直接喂给 Coil 渲染引擎，实现毫秒级图片上屏。
*   **原生语音通信 (`voice`)**：重构底层 `AudioManager`，采用高压高音质 **AAC编码/m4a格式**，结合纯手写 `pointerInput` 手势探测，完美复刻国民级“按住说话、松开发送/上划取消”操作。
*   **区块链级防篡改存证**：后端 `hub.ts` 实施 **AES-256-CBC 对称加密** 落盘，且每条消息均基于“前一条哈希+当前明文+特征”计算 **SHA-256** 哈希，构成不可篡改的证据链。
*   **前台服务保活**：`LsLifeImService.kt` 独占 WebSocket，并在收到消息时触发 `IMPORTANCE_HIGH` 系统级通知直达私聊。

### 3. 千人千面动态发布引擎 (Dynamic Publish Engine)
*   **动态 Schema 渲染**：利用 `CategoryConfig.kt` 驱动条件渲染，房产、二手、兼职、拼车等 10+ 个顶级大类及 100+ 个二级细分类各自呈现专属字段。
*   **DeepSeek AI NLP 提取**：接入 AI 算法，针对非结构化长文本，一键智能提炼品牌、成色、价格等核心要素并自动回填。

### 4. V5.0 顶级商业化视觉体验 (Joybuy Design System)
*   **极简克制的留白**：移除了早期繁杂的块面背景，统一采用纯白 (`#FFFFFF`) 容器与浅冷灰底色，彻底突显商品图本身。
*   **3D 扁平化图标矩阵**：全站 100+ 个细分品类已全面接入高规格 3D 图标库（覆盖个人闲置、餐饮、教育、拼车租车等），由后端下发动态 URL。
*   **重构导航与 UI 组件**：利用 `URLEncoder` 修复了带特殊字符（如“拼车/租车”）的 Jetpack Compose 路由闪退。实现了纯文字 + 黑色下划线的高级 Tab、圆角优化的商品卡片（`8dp`）及醒目的红色实心 `+` 加购按钮。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend)
*   **语言 / SDK**: Kotlin / Min SDK 24 / Target SDK 34
*   **UI 框架**: Jetpack Compose / Material3 / AndroidX Navigation / URL 自动编解码路由
*   **架构 / 状态管理**: MVVM 单向数据流 / Dagger Hilt 依赖注入 / Coroutines & StateFlow
*   **网络通信**: Retrofit2 (RESTful) / OkHttp3 / WebSockets (全双工保活通信)
*   **图像与多媒体**: Coil / CameraX / Android 原生 Media 引擎

### 服务端与数据存储 (Backend)
*   **环境 / 框架**: Node.js / Express 兼容层 / TypeScript / PM2 热载托管 (`115.191.6.95`)
*   **数据库 / ORM**: PostgreSQL 关系型数据库 / Prisma ORM
*   **加密安防**: Node.js 原生 Crypto 模块 (AES-256 + SHA-256)

---

## 🗄 数据库模型架构 (Database Schema - Prisma)
1. **`User`**: 核心主表（手机号极简注册、密码加密鉴权、实名与会员权益状态）。
2. **`Post` / `Product`**: 商品服务发布表（含品类标识、JSON属性集合、图集链接，支持高度多态）。
3. **`Cart` / `CartEntry`**: 购物车拓扑结构，存储用户的多商铺商品购买意向。
4. **`Order` / `OrderItem`**: 订单流转核心表（关联收货地址、支付状态、履约生命周期）。
5. **`ChatSession` & `ChatMessage`**: 交易沟通链路表。
6. **`Category` 树**: 高度封装的分类表，已内置 `iconUrl` 指向最新的 3D 图标库。

---

## 🚀 自动化发版与部署指引 (Build & Deploy)
*   **归档规则**：以后只允许输出 `release` 版本到 `D:\LsLife\releases\` 目录，不再拷贝 debug 版本。
*   **自增版本**：执行 Release 时，项目根目录的 `version.properties` 脚本会自动升阶 VersionCode。
*   **标准编译指令** (使用 JDK 17 / Android Studio jbr):
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease
  ```
*   **服务端重置数据 (当且仅当修改了 seed.ts 时)**:
  在远程服务器执行 `npm run seed` 并通过 `pm2 restart all` 热启。

---

## 🎯 二次开发交接与下一阶段指引 (Next Stage Handover)

接手此项目进行**二次开发**的工程师/Agent，请优先查阅以下未尽事宜并基于此开启新工作：

1. **【强制】数据容灾与备份机配置**：
   项目目前已跑通上线全流程，请务必在服务器上配置数据库及静态资源（如上传的图片、语音）的全量/增量自动化 Cron 备份脚本（推荐异地双机热备），防止数据灾难。
2. **全局高级搜索与多维过滤 (Search & Filter)**：
   目前数据底座已完全结构化。接下来需要为搜索页开发联合聚合查询（如“价格区间” + “成色” + “分类标签”），建议直接利用 Prisma 的高级复合查询。
3. **地图与 O2O 闭环定位卡片**：
   IM 协议已预留。可接入高德/腾讯地图 API，在聊天中实现 `type="location"` 的地图卡片互发，并增加商品距离检索。
4. **架构严谨性与规范约束**：
   在后续二次开发中，**绝对不允许破坏现有的 MVVM + Hilt 单向数据流架构**。任何新加的 Android 权限务必通过 `ActivityResultContracts` 进行动态请求。
5. **严格的路由规范 (Navigation)**：
   任何往 Jetpack Compose 传递的 String 路径参数，**必须**使用 `java.net.URLEncoder.encode` 进行封装，以免包含特殊字符（如 `/` 或 `?`）导致路由解析崩溃。
