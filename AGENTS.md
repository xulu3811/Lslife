# 连山壮瑶同城 (LianShan Local Services) - V3.5 存档与交接准则

## 项目状态与客观现实 (Project State & Objective Reality)

经过 V1.x 至 V3.5 阶段的持续精细化演进，本项目已构建起一套**高性能、高安防、前后端分离且全链路闭环**的本地同城生活服务交易与通讯平台（包含“1:1 闲鱼商品发布 + 本地同城服务 + 即时交易聊天存证 IM”）。现阶段系统已稳定发版至 **v3.5**（归档于 `D:\LsLife\releases\LsLife-v3.5-release.apk`）。

---

## 核心模块、技术架构与业务逻辑 (Core Architecture & Logic)

### 1. 即时通讯与交易存证体系 (IM & Transaction Evidence System - V3.5 重点)
- **后台守护与保活 (Foreground Service Persistence)**：
  - 客户端通过独立 Android 前台服务 `LsLifeImService.kt` 独占接管 WebSocket (`RealtimeClient`)。当用户登录后自动启服，绑定常驻低优先级系统通知，赋予应用在后台运行时最高的进程优先级，彻底杜绝被系统省电或内存管理策略误杀。
- **高优横幅通知与直达 (Heads-up Notification & Deep Linking)**：
  - 注册 Android 高重要度渠道 `lslife_im_channel` (`IMPORTANCE_HIGH`)，并在 `MainActivity` 适配 Android 13+ (`API 33+`) 的 `POST_NOTIFICATIONS` 动态权限申请。
  - 应用在后台或静音状态下收到新消息时，系统顶栏会弹出横幅消息提示；用户点击该横幅，调用封装有会话附加参数的 `PendingIntent`，通过 Compose `LaunchedEffect` 无缝拉起应用并直达对应的私聊窗口。
- **永久加密与防篡改存证链 (Encryption & Tamper-Proof Chain)**：
  - 为保护商户与客户交易隐私并保留法律存证，后端 `hub.ts` 对所有常规私聊消息自动执行 **AES-256-CBC 对称加密** 落盘 (`isEncrypted: true`)。
  - **区块式存证链算法**：每一条入库消息均基于前一条消息的哈希值、当前内容、发送人与时间戳，计算 **SHA-256 哈希值** (`hash = SHA256(prevMsg.hash + content + senderId + timestamp)`)，实现防删改追溯。
- **消息撤回机制 (Recall Mechanism)**：
  - 开放 1 分钟（60秒）内的消息撤回权限。前后端同步校验 `Date.now() - msg.createdAt <= 60000ms`，撤回后通过 WebSocket 及 RESTful 双路对齐，将数据库内容更为 `对方撤回了一条消息` 且不可逆。
- **多网自愈与离线增量补发 (Offline Sync & Network Resilience)**：
  - `LsLifeImService` 内置 `ConnectivityManager.NetworkCallback`，监听 Wi-Fi / 4G / 5G 的切换与网络中断重建。
  - 在 WebSocket 连接建立 (`onOpen`) 时，客户端自动发送全局同步请求 `{"action":"sync_offline","sessionId":"all"}`；后端扫描当前用户全部未读会话，增量将积压的加密消息安全解密并下发到客户端 Flow 进行重绘。

### 2. 动态发布引擎与权限限额 (Dynamic Publish Engine)
- **千人千面动态表单**：基于 `CategoryConfig.kt` 驱动条件渲染，选择“个人闲置”展开品牌成色填写，选择“家政/招聘”呈现专属选项卡。
- **AI 智能提取**：整合 DeepSeek AI 引擎，对用户随手输入的草稿一键提炼品牌、成色与购买日期并自动回填。
- **发贴额度算法与白名单**：
  - 普通账号最大限制发布 **10 个** 有效商品/服务；后端 `publish.ts` 通过 `prisma.post.count` 进行自动校验与防刷拦截。
  - 特殊特权账号白名单（`19926387658` 与 `13828577665`）解除限制，支持无限次发布。
- **图片沙盒缓存与并发无损压缩**：
  - 采用 **Cache-on-Select (选定即落盘)** 策略，在选取图片的瞬间将其写入本地沙盒临时文件，彻底解决 Android 13+ 跨页 URI 权限截断问题。
  - 发布前通过携程并发执行原生 ImageCompressor，把高清图进行视觉无损压缩至 1MB 内，突破服务器上传瓶颈。

### 3. 主页分类与展示流 (Home & Category Feed)
- 统一定义并修正了全局品类名称（如“房屋租售”），修正数据库与各页面路由。
- 首页默认“推荐”模块完整聚合并展示全部有效发布的商品/服务；分栏选项卡即时切换筛选，配合骨架屏 (`SkeletonCard`) 提供高丝滑体验。

---

## 技术栈底座 (Technology Stack)

### Android 客户端 (Frontend)
- **语言 / SDK**: Kotlin / Min SDK 24 / Target & Compile SDK 34-36
- **UI 框架 / 导航**: Jetpack Compose / AndroidX Navigation / Coroutines & Flow (SharedFlow/StateFlow)
- **架构 / 依赖注入**: MVVM / Dagger Hilt
- **网络与通信**: Retrofit2 / OkHttp3 / WebSocket / Kotlinx Serialization (完善适配 ProGuard 混淆保护)
- **图片处理**: Coil 结合 Android BitmapFactory 与 File 沙盒流转

### 服务端与数据存储 (Backend & Database)
- **环境 / 框架**: Node.js / Fastify / TypeScript / PM2 热载托管
- **数据库 / ORM**: PostgreSQL 关系型数据库 + Prisma ORM 映射建模
- **通信与安防**: WebSockets (ws) / Node Crypto (AES-256-CBC + SHA-256)
- **AI 引擎**: 接入 DeepSeek LLM 处理非结构化文字解析与智能表单填充

---

## 数据库模型架构 (Database Schema - Prisma)
1. **`User`**: 用户核心主表（账号、手机号、加密密码、自定义头像路径、角色等）。
2. **`Post`**: 服务/商品发布表（关联发起人，含标题、价格、品类 `category`、成色、参数及JSON图集）。
3. **`ChatSession`**: 聊天会话映射表（复合唯一键 `user1Id_user2Id`，独立记录双向未读消息数 `unread1`, `unread2`）。
4. **`ChatMessage`**: 聊天明细与存证记录表（关联会话与发送者，含消息类型 `text/image/recalled`、内容 `content`、加密标记 `isEncrypted: Boolean`、当前防篡改哈希 `hash` 及时间戳）。

---

## 自动化发版与归档规范 (Build & Archiving Rules)
- **自动递增版本机制**：在 `android/app/build.gradle.kts` 中绑定了 `version.properties` 自增脚本。每一次执行 Release 编译时，系统会自动升阶版本号（目前已升至 **v3.5** / versionCode 26）。
- **标准编译指令**：若需编译 Release 版本，务必配置 Java 17/21 环境（如使用 Android Studio JBR）并执行：
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew.bat assembleRelease
  ```
- **归档路径规范**：编译产出的 APK 自动存放在 `D:\LsLife\releases` 目录下，**严格遵循版本编号命名格式 (`LsLife-vX.Y-release.apk`) 并按序归档**，严禁在该目录下遗留无版本编号的杂乱 APK 文件。

---

## 下阶段开发接力指引 (Next Stage Handover Guidelines)
1. **后续架构维护**：任何业务模块的拓展必须严格遵循现有的 MVVM + Repository 与 Kotlinx Serialization 模式；修改接口时保持 `.nullable().optional()` 的数据兼容宽容度。
2. **即时通讯拓展**：若需开发聊天语音、视频或发送定位功能，请在 `ChatMessage.type` 中拓展枚举，并在 `LsLifeImService` 顶栏横幅与后端加密存证算法中同步做好兼容。
3. **数据查询优化**：后续开发“全局搜索与多维度筛选 (Search & Filter)”功能时，可充分利用后端 Prisma 的聚合索引与分页查询优化。
