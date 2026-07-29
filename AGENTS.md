# 连山壮瑶同城 (LianShan Local Services) - V3.8 存档与交接准则

## 📌 项目状态与客观现实 (Project State & Objective Reality)

经过从初创到 V3.8 阶段的爆发式演进，本项目已构建起一套**高性能、高安防、多维交互且全链路闭环**的本地同城生活服务交易与通讯平台。目前系统不仅具备了“1:1 闲鱼商品动态发布 + 本地同城分类服务”，更深度打通了**国民级的即时交易聊天系统 (IM)**，包含了图文、语音、商品卡片等多模态通讯能力。
现阶段系统已稳定发版至 **v3.8**（归档于 `D:\LsLife\releases\LsLife-v3.8-release.apk`）。

---

## 🏗 核心模块、技术架构与业务逻辑 (Core Architecture & Logic)

### 1. 深度即时通讯与交易存证体系 (Deep IM & Transaction System - V3.8 重点)
- **多模态消息体系 (Multi-modal Messaging)**：
  - **商品联动卡片 (`post_card`)**：买家在商品详情页点击“立刻沟通”时，系统自动抓取商品元数据（首图、标题、价格）并以 JSON 格式静默发送，极大提升了交易转化率。
  - **极速图片流转 (`image`)**：为了追求极限速度，图片发送跳过传统 HTTP 接口，在本地无损压缩后转化为 `base64:` 文本流，直接通过 WebSocket 砸向服务端。Android 渲染层 (`ChatScreen`) 实施 `Base64.decode` 字节还原拦截，喂给 Coil 引擎进行极速显示。
  - **原生语音通信 (`voice`)**：重构了底层的 `AudioManager`，采用高压高音质的 **AAC编码/m4a格式**。UI 层剥离了 Compose 默认组件的事件吞噬，纯手写 `pointerInput` (拖拽与点按探测) 完美复刻了微信/闲鱼“按住说话、松开发送”的高敏国民级手势。
- **永久加密与防篡改存证链 (Encryption & Tamper-Proof Chain)**：
  - 后端 `hub.ts` 对所有聊天载体自动执行 **AES-256-CBC 对称加密** 落盘。
  - 每一条入库消息均基于“前一条哈希 + 当前明文 + 发送人 + 时间戳”，计算 **SHA-256 哈希值**，形成区块式存证链，实现法律级防删改追溯。
- **后台守护与高优通知直达 (Foreground Service & Heads-up)**：
  - 客户端通过前台服务 `LsLifeImService.kt` 独占接管 WebSocket，彻底杜绝系统杀后台。
  - 收到新消息时弹出 `IMPORTANCE_HIGH` 系统横幅，点击通过 `PendingIntent` 无缝拉起应用直达私聊窗口。

### 2. 千人千面动态发布引擎 (Dynamic Publish Engine)
- **多级联动表单与动态 Schema**：基于 `CategoryConfig.kt` 驱动条件渲染，不同分类呈现专属的字段结构。
- **DeepSeek AI 智能提取**：针对非结构化长文本，一键提炼品牌、成色等属性并自动回填。
- **发贴额度算法与白名单**：普通账号限制最大发布量（防刷拦截）；特权账号白名单解除一切限制。
- **图片沙盒 Cache-on-Select**：规避 Android 13+ 的跨页 URI 权限截断，并引入携程并发无损压缩突破上传瓶颈。

### 3. 主页展示与二级聚类导航 (Home & Sub-category Feed)
- 实现了流畅的“骨架屏 (`SkeletonCard`)”丝滑过渡。
- 支持基于业务模块（个人闲置、房屋租售等）的深度二级分类导航矩阵，数据结构高内聚。

---

## 💻 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend)
- **语言 / SDK**: Kotlin / Min SDK 24 / Target & Compile SDK 34-36
- **UI 框架 / 手势**: Jetpack Compose (深度定制 `pointerInput` 与动画) / AndroidX Navigation
- **架构 / 协程**: MVVM / Dagger Hilt / Coroutines & StateFlow
- **网络通信**: Retrofit2 (RESTful) / OkHttp3 / WebSockets (实时全双工)
- **媒体处理**: 原生 `MediaRecorder` & `MediaPlayer` / ImageCompressor / Coil

### 服务端与数据存储 (Backend)
- **环境 / 框架**: Node.js / Fastify / Express (兼容层) / TypeScript / PM2 热载托管
- **数据库 / ORM**: PostgreSQL / Prisma ORM
- **加密安防**: Node Crypto (AES-256-CBC + SHA-256)
- **多媒体流**: Multer (`/api/upload/audio` 独立管道)

---

## 🗄 数据库模型架构 (Database Schema - Prisma)
1. **`User`**: 核心主表（包含极简手机号注册、加密密码鉴权）。
2. **`Post`**: 商品/服务发布表（含品类、JSON属性图集，动态拓展）。
3. **`ChatSession`**: 会话拓扑表（维护双边未读池与高频唤醒映射）。
4. **`ChatMessage`**: 交易存证表。通过 `type` 字段（`text/image/voice/post_card/recalled`）驱动客户端多态渲染，高度解耦。

---

## 🚀 自动化发版与归档规范 (Build & Archiving Rules)
- **自增版本与归档**：执行 Release 编译时，系统 (`version.properties` 脚本) 自动升阶。
- **标准编译指令**：
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease
  ```
- **产物归档**：必须严格命名 `LsLife-vX.Y-release.apk` 存入 `D:\LsLife\releases\`。

---

## 🎯 下一阶段开发接力指引 (Next Stage Handover - V3.9+)
接手此项目的开发团队，请优先审阅以下规划：
1. **全局高级搜索与多维过滤 (Search & Filter)**：
   目前我们已经拥有了强大的结构化数据底座，下一步亟需开发一个独立的高级搜索路由，支持依据“价格区间、成色、分类标签”的联合聚合查询。请充分利用 Prisma 的复合查询索引。
2. **地图定位卡片联动**：
   在 IM 通讯协议中已预留多态空间，可引入 `type = "location"` 并接入高德地图 API，补齐最后一块 O2O 拼图。
3. **架构严谨性约束**：
   无论如何拓展 UI，必须维持 `MVVM + Hilt` 的单向数据流。所有新增权限（如定位）务必执行 `ActivityResultContracts` 动态运行时申请，不可抱有侥幸心理。

## ⏰ 开发后期与上线前强制检查单 (Pre-Launch Checklist)
- **【核心提醒】数据容灾与备份机配置**：在全部开发与测试流程结束后、正式大规模拉新上线前，**必须提醒客户配置自动化容灾备份机**（包含数据库和全量静态资源的增量/全量同步 Cron 脚本）。

