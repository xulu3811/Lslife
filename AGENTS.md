# 连山壮瑶同城 (LianShan Local Services) - V1.0 存档

## 项目状态与客观现实 (Project State & Objective Reality)

本项目是一个本地同城服务应用，V1.0 版本已经实现了核心的用户流转、会员限制、以及完整的发布与订单流程。

### 核心模块 (Core Modules)
1. **全局容器与路由 (`src/App.tsx`)**
   - 通过内部状态 (`currentScreen`) 管理页面切换。
   - 包含底部主导航：首页、订单、发布（悬浮 FAB）、购物车、我的。
   - 全局维护了会员状态：`membershipTier` (free, vip, premium) 和 `publishedCount`（已发布数量）。

2. **发布与信息流 (`src/components/PublishView.tsx`)**
   - 实现了本地信息发布流程：分类选择 -> 详情填写（图片/标题/描述/价格） -> 预览模式 -> 发布。
   - **会员限制机制**：普通用户每月可发3条，VIP可发20条，高级VIP可发50条。超额时会触发开通会员弹窗。
   - 实现了发布内容预览功能（以卡片样式展示用户即将发布的内容）。

3. **会员订阅系统 (`src/components/SubscriptionModal.tsx`)**
   - 普通会员（9.9/月）与高级会员（19.9/月）的升级展示弹窗。
   - 在用户中心、发布超额时被调用。

4. **用户中心 (`src/components/UserProfile.tsx`)**
   - 展示用户信息、会员专属皇冠/徽章。
   - 包含地址管理模块、钱包资产展示。

5. **电商与订单流 (`src/components/...`)**
   - **首页列表**：展示周边商家 (`MerchantCard`)。
   - **购物车**：管理待下单商品 (`CartView`)。
   - **下单与支付**：`OrderDetailSheet` 确认订单，`PaymentModal` 模拟收银台支付。
   - **订单列表与追踪**：`OrderListView` 查看历史订单，`DeliveryTracker` 结合 `MapContainer` 实现实时的地图配送进度。

6. **智能助手 (`src/components/AiAssistant.tsx`)**
   - 侧边栏/悬浮式的 AI 同城助手，提供智能推荐和问答。

### 技术栈与设计规范 (Tech Stack & Design Rules)
- **UI & 样式**: Tailwind CSS，全量支持 Dark Mode (深色模式)。
- **动画与交互**: 依赖 `motion/react` 实现平滑的侧滑转场 (`AnimatePresence`)、弹窗效果。
- **图标**: `lucide-react`。
- **设计风格**: 追求现代化、高对比度的界面，大量使用圆角卡片、渐变色点缀，以及无边框的轻量化设计。

## 后续开发指引 (Next Stage Guidelines)
- 在新增功能时，请继续遵循组件化的拆分策略，避免 `App.tsx` 过度臃肿。
- 涉及需要全局跨页面的状态（如当前的会员系统），可先提升到 `App.tsx`，必要时再考虑状态管理库。
- 保持界面在移动端的触控体验，确保动画过渡自然。
