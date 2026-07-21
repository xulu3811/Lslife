# 连山壮瑶同城 (LianShan Local Services) - V2.3 存档

## 项目状态与客观现实 (Project State & Objective Reality)

经过 V1.x 阶段的高速演进，本项目已从早期的前端原型升级为**真正的前后端分离架构**，具备完整的本地同城生活服务发布与用户管理能力。V2.0-V2.3 确立了全新的技术栈、设计美学、智能化的核心业务流转体系，以及健壮的底层机制。

### 核心模块与功能 (Core Modules & Features)

1. **主题美学与设计系统 (Theming & UI)**
   - 全局采用 **乳白色 (Milk White) 与鲜红色 (Bright Red)** 为主色调，实现了“3D扁平化，简约大方”的全新程序图标与界面语言。
   - 深入支持系统级的浅色/深色 (Light/Dark) 主题切换。
   - 底部导航栏等核心组件已彻底扁平化，比例匀称，观感极为清爽。

2. **身份与用户管理 (Auth & Profile)**
   - 实现了基于真实后端数据库的注册与登录模块，完成前端（Jetpack Compose）与后端（Node.js）的全链路联通。
   - **自定义头像 (Avatar Customization)**：用户支持通过本地相册上传自定义头像；客户端采用 **Cache-on-Select (选定即落盘)** 策略，在选取图片的瞬间将流写入本地沙盒缓存文件 (`avatar_temp.jpg`)，通过无参安全路由 (`crop_avatar`) 流转，并经过内置 ImageCompressor 极速压缩后直接上传服务器，彻底解决了 Android 13+ 跨页面 URI 权限丢失和 Jetpack Navigation URL 截断黑屏问题。

3. **动态发布模块（1:1 闲鱼复刻） (Dynamic Publish Engine)**
   - **千人千面表单**：根据 `CategoryConfig` 提供条件渲染能力。当选择“个人闲置”时展开“品牌/成色/参数”填写区；选择“家政”、“招聘”等其他服务时，呈现动态的专属选项卡（Pills）。
   - **AI 智能提取**：深度整合 DeepSeek AI，用户随手输入商品草稿，AI 可瞬间提炼并回填【品牌】、【参数】和【购买日期】，大幅提升发布体验。
   - **全画幅图片并发压缩**：彻底突破 5MB 服务器上传限制。前端实现了对图片选择的即时沙盒缓存，在安全读取高分辨率图片后，使用并发携程进行无损视觉压缩至 1MB 内再行上传。同时修复了协程 `async` 中的异常击穿逃逸问题，保证了高并发上传的稳定性。
   - **高容错后端**：修复了早期发布空字段导致的 HTTP 400 Bad Request 问题，实现了数据表单 `null` 穿透的最高兼容性。代码混淆规则也已完全适配 `ApiEnvelope` 和协程的 Release 包序列化。

4. **主页与展示流 (Home & Feed)**
   - 首页精准呈现区域分类，如“房屋出租”等核心版块均已搭载匹配的高质量占位图与定制化价格展示。

### 技术栈底座 (Tech Stack)

#### Android 客户端 (Frontend)
- **语言 / UI 框架**: Kotlin / Jetpack Compose 
- **架构 / 注入**: MVVM / Dagger Hilt
- **网络 / 序列化**: Retrofit2 / OkHttp3 / Kotlinx Serialization (搭配 ProGuard 混淆保护)
- **图片处理**: Coil 结合 Android 原生 BitmapFactory 与 File 沙盒读写，实现健壮的流转。

#### 服务端 (Backend)
- **环境 / 框架**: Node.js / Fastify / TypeScript
- **数据库 / ORM**: PostgreSQL / Prisma ORM
- **进程管理**: PM2 (部署与热重载)
- **AI 引擎**: 接入 DeepSeek 大语言模型处理非结构化文字解析。

## 后续开发指引 (Next Stage Guidelines)
- 继续深入业务模块扩展时，必须遵循当前的 MVVM 与 Repository 模式，保持前后端接口的 `.nullable().optional()` 灵活宽容度。
- 涉及所有系统级 URI (如 PhotoPicker) 的传递时，**严禁通过 Jetpack Navigation 路由传递字符串 URI**，必须采用 **Cache-on-Select (写入缓存文件后读取)** 机制。
- 绝不为了适配单一品类而硬编码 UI，请复用并扩充 `CategoryConfig` 以驱动所有的表单设计。
- 开发下一个大阶段如“搜索与筛选系统 (Search & Filter)”或“管理员后台 (Admin Web)”时，应在确保当前核心骨架绝对稳定的基础上向外延伸。
- **自动编译机制**：每一次 Android 代码改动完成后，务必自动执行 `assembleRelease` 编译并拷贝发布完整的 `app-release.apk` 供测试。
