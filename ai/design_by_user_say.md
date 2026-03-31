# ResurrectionMoeyu 需求规格说明书

**版本**: v1.0  
**创建日期**: 2026-03-27  
**分析代理**: requirement-analyst  
**任务 ID**: ANALYSIS-001-FUNC  
**基于分析**: 
- ai/analysis/overview.md (项目概览)
- ai/dev/project.md (架构文档)

---

## 一、文档概述

### 1.1 文档目的

本文档基于 ResurrectionMoeyu 项目的逆向分析结果，梳理完整的功能清单、业务流程和用户交互逻辑，为后续开发、测试和维护提供标准化的需求规格说明。

### 1.2 适用范围

- **项目类型**: Android Live2D 交互式应用
- **目标平台**: Android 5.0+ (API Level 19+)
- **核心功能**: Live2D 虚拟角色交互、Gacha 抽卡系统、内购计费、收藏管理
- **用户群体**: 虚拟角色爱好者、移动端用户

### 1.3 术语定义

| 术语 | 英文 | 说明 |
|------|------|------|
| **Live2D** | Live2D | 二维模型三维渲染技术 |
| **MOC** | Model Container | Live2D 模型文件格式 |
| **Gacha** | Gacha | 抽卡系统 (源自日语"ガチャ") |
| **SKU** | Stock Keeping Unit | 内购商品单元 |
| **Nonce** | Number Used Once | 一次性随机数，防重放攻击 |
| **Parts** | Parts | Live2D 模型的独立图层组件 |

---

## 二、功能清单总表

### 2.1 功能模块分类

| 模块编号 | 模块名称 | 功能描述 | 优先级 | 关联任务 |
|---------|---------|---------|--------|---------|
| **F01** | Live2D 交互系统 | 虚拟角色渲染、触摸交互、动画管理 | P0 | ANALYSIS-001-FUNC |
| **F02** | 语音系统 | 事件驱动语音播放、中日双语支持 | P0 | ANALYSIS-001-FUNC |
| **F03** | Gacha 抽卡系统 | 三阶金币管理、概率抽卡、结果展示 | P0 | ANALYSIS-001-FUNC |
| **F04** | Billing 内购系统 | Google Play 支付集成、安全验证 | P0 | ANALYSIS-001-FUNC |
| **F05** | Collection 收藏系统 | 物品/语音/笔记收藏管理 | P1 | ANALYSIS-001-FUNC |
| **F06** | 用户账户系统 | 登录注册、数据持久化、等级系统 | P0 | ANALYSIS-001-FUNC |
| **F07** | 设置与配置 | 声音/相机开关、个性化设置 | P2 | ANALYSIS-001-FUNC |

### 2.2 功能详细清单

#### F01: Live2D 交互系统

| 功能 ID | 功能名称 | 功能描述 | 输入定义 | 输出定义 | 验收标准 |
|--------|---------|---------|---------|---------|---------|
| **F01-01** | 模型加载与渲染 | 从 Assets 加载 MOC 模型文件和纹理贴图，OpenGL ES 渲染 | MOC 文件路径、纹理文件列表 | 渲染完成的 Live2D 角色视图 | 模型加载时间 < 3s，渲染帧率 ≥ 30fps |
| **F01-02** | 触摸区域划分 | 将屏幕划分为 Face、Head、Body、Arm、Belly 等交互区域 | 触摸坐标 (PointF) | 区域枚举值 (Scene) | 区域划分准确，响应延迟 < 100ms |
| **F01-03** | 触摸事件处理 | 处理 touchesBegan、touchesMoved、tapEvent 等触摸事件 | 触摸事件类型、坐标、时间戳 | 动画触发、参数更新 | 触摸响应流畅，无卡顿 |
| **F01-04** | Spring-Damper 面部跟随 | 基于物理模拟实现面部跟随触摸点的平滑运动 | 目标位置、当前帧时间 | PARAM_ANGLE_X/Y、PARAM_EYE_BALL_X/Y参数更新 | 面部跟随自然，加速度限制合理 |
| **F01-05** | Flip 翻转机制 | 拖拽距离超过阈值时触发头部/身体翻转效果 | 起始坐标、拖拽距离 (>500px) | Flip 动画触发 | Flip 触发准确，动画流畅 |
| **F01-06** | Idle 循环动画 | 4 组场景的随机 Idle 动作循环播放，带淡入淡出 | 场景枚举 (bath_a/bath_b) | 持续的背景动画 | Idle 动画随机性良好，无重复感 |
| **F01-07** | Touch 交互动画 | 触摸时触发特定的 Touch 动画响应 | 触摸区域、事件类型 | 对应的 Touch Motion 播放 | Touch 动画与触摸区域匹配准确 |
| **F01-08** | Expression 表情系统 | 通过 LAppExpressionMotion 管理表情队列 | 表情 ID、持续时间 | 表情动画播放 | 表情切换自然，队列管理正确 |
| **F01-09** | EyeBlink 自动眨眼 | 根据状态调整眨眼间隔 (正常 4s/触摸 1.5s) | 当前状态 (Normal/Touching) | 眨眼参数更新 | 眨眼频率符合设定，自然不机械 |
| **F01-10** | 场景切换 | 支持 bath_a、bath_b 等场景的背景和模型切换 | 目标场景枚举 | 背景图片更新、Parts 透明度调整 | 场景切换流畅，无闪烁 |
| **F01-11** | 加速度传感器集成 | 通过 AccelHelper 监听设备加速度，映射到模型参数 | 加速度数据 (X/Y/Z) | PARAM_ANGLE_X/Y、PARAM_BASE_X/Y更新 | 设备倾斜时角色姿态跟随自然 |
| **F01-12** | 震动检测 | 加速度差值 >1.5 时触发 shakeEvent 震动效果 | 加速度变化量 | shakeEvent 回调触发 | 震感触发灵敏，阈值合理 |

#### F02: 语音系统

| 功能 ID | 功能名称 | 功能描述 | 输入定义 | 输出定义 | 验收标准 |
|--------|---------|---------|---------|---------|---------|
| **F02-01** | voice.json 映射管理 | 解析 voice.json 配置表，建立语音与事件的映射关系 | voice.json 文件路径 | 语音映射表 (Map 结构) | 映射表加载成功，无解析错误 |
| **F02-02** | VoiceManager 语音播放 | 管理语音资源的加载、播放、队列控制 | 语音文件名、播放模式 | 音频输出、播放状态 | 语音播放流畅，无延迟 |
| **F02-03** | 中日双语支持 | 支持 voice/ (日语) 和 voice_cn/ (中文) 双语言资源 | 语言类型枚举 | 对应语言的语音文件 | 语言切换准确，资源加载正确 |
| **F02-04** | 事件驱动语音触发 | 触摸特定区域时自动触发对应的语音播放 | 触摸区域、事件类型 | 语音文件名、播放指令 | 语音触发与触摸事件同步 |
| **F02-05** | 语音队列管理 | 支持多个语音的排队播放，避免重叠冲突 | 语音列表、优先级 | 队列状态、播放顺序 | 队列调度合理，无语音丢失 |

#### F03: Gacha 抽卡系统

| 功能 ID | 功能名称 | 功能描述 | 输入定义 | 输出定义 | 验收标准 |
|--------|---------|---------|---------|---------|---------|
| **F03-01** | 三阶金币管理 | 管理 BRONZE、GOLD、PLATINUM 三种金币类型和数量 | 金币类型枚举 | 各类型金币数量、余额 | 金币计数准确，上限保护生效 |
| **F03-02** | 每日登录奖励 | 用户每日首次登录自动发放 BRONZE 金币 | 最后登录时间、当前日期 | 奖励金币数量、bonus 标记更新 | 每日奖励发放准确，无重复发放 |
| **F03-03** | 金币上限保护 | 每种金币数量上限 MAX_COIN_COUNT=99，超出时保护 | 当前金币数、新增数量 | 保护后的金币数量 | 上限保护逻辑正确，无溢出 |
| **F03-04** | GachaTask 异步抽卡 | 基于 AsyncTask 实现异步抽卡任务处理 | userId、coinType (BRONZE/GOLD/PLATINUM) | GachaResult (包含 itemId 和更新后的 UserData) | 抽卡过程异步执行，UI 不阻塞 |
| **F03-05** | 概率判定逻辑 | 根据 rate(2/3/4) 进行概率计算，决定抽卡结果 | rate 值、随机数 | 概率判定结果 (布尔值) | 概率分布符合设计 (10%/50%/90%) |
| **F03-06** | 未收集物品优先 | 优先抽取用户尚未收集的 25 种物品之一 | noHolds 列表、已收集 items 列表 | 优先判定的 itemId | 未收集物品优先策略生效 |
| **F03-07** | GachaResult 结果展示 | 在 GachaResultActivity 展示抽卡获得的物品详情 | GachaResult 对象 | UI 展示的物品信息、动画效果 | 结果展示清晰，用户体验良好 |
| **F03-08** | 用户数据更新 | 抽卡后自动更新 UserData 并持久化保存 | 新的 itemId、更新后的 items 列表 | 持久化的 UserData.dat | 数据更新和保存成功，可恢复 |

#### F04: Billing 内购系统

| 功能 ID | 功能名称 | 功能描述 | 输入定义 | 输出定义 | 验收标准 |
|--------|---------|---------|---------|---------|---------|
| **F04-01** | BillingService 初始化 | 管理 Google Play Billing Service 连接和请求队列 | Context、服务配置 | 服务连接状态、请求队列 | 服务初始化成功，队列管理正常 |
| **F04-02** | SKU 商品管理 | 定义和管理内购商品 SKU、价格、描述等信息 | productId、developerPayload | SKU 商品信息对象 | SKU 信息完整，可查询 |
| **F04-03** | Google Play 支付集成 | 集成 Google Play Billing V1 API，实现购买流程 | PendingIntent、购买请求 | 支付界面、购买状态 | 支付流程完整，用户可完成购买 |
| **F04-04** | 购买状态处理 | 处理 PURCHASED、RESTORED、CANCELLED 三种状态 | PurchaseState 枚举 | 状态对应的 UI 反馈和业务逻辑 | 状态处理准确，用户反馈清晰 |
| **F04-05** | RSA-2048 公钥验证 | 使用 RSA-2048 算法验证购买签名的真实性 | signedData、signature | 验证结果 (布尔值) | 签名验证准确，安全性高 |
| **F04-06** | Base64 编码传输 | 购买数据和签名使用 Base64 编码进行传输 | 原始数据/签名 | Base64 编码字符串 | 编码解码正确，无数据丢失 |
| **F04-07** | Nonce 重放攻击防护 | 生成和验证 Nonce，防止购买请求的重放攻击 | Nonce 值、已知 Nonce 集合 | Nonce 验证结果 | 重放攻击防护生效，Nonce 管理正确 |
| **F04-08** | 服务器端确认流程 | 购买验证后向服务器发送确认请求，更新用户数据 | signedData、signature、userId | 服务器响应、更新后的 UserData | 服务器确认成功，数据同步准确 |
| **F04-09** | PurchaseObserver 观察者 | 通过观察者模式监听购买状态变化并触发相应处理 | PurchaseState、VerifiedPurchase | 回调通知、业务逻辑执行 | 观察者模式工作正常，回调及时 |

#### F05: Collection 收藏系统

| 功能 ID | 功能名称 | 功能描述 | 输入定义 | 输出定义 | 验收标准 |
|--------|---------|---------|---------|---------|---------|
| **F05-01** | 物品收藏状态管理 | 管理 25 种物品的收集状态和展示 | items 列表 (Integer) | 收集进度、物品详情 | 25 种物品状态管理准确 |
| **F05-02** | ItemCollectionActivity 展示 | 在专用 Activity 中展示所有收藏物品的列表和详情 | UserData.items | UI 展示的收藏品网格 | 展示布局合理，信息完整 |
| **F05-03** | ItemGridAdapter 适配器 | 使用 GridAdapter 实现物品列表的适配和渲染 | items 数据源、布局参数 | GridView 展示效果 | 适配器工作正常，列表流畅 |
| **F05-04** | 语音收藏列表管理 | 在 VoiceCollectionActivity 中管理和播放收藏的语音 | voice.json 映射表、语音文件列表 | 语音列表 UI、播放控制 | 语音列表完整，播放功能正常 |
| **F05-05** | voice.json 数据映射 | 将 voice.json 配置与语音收藏进行数据映射 | voice.json 结构 | 映射后的语音元数据 | 数据映射准确，可查询 |
| **F05-06** | 笔记收藏记录管理 | 在 NoteCollectionActivity 中管理和展示用户笔记 | 笔记数据、时间戳 | 笔记列表、详情展示 | 笔记管理功能完整 |

#### F06: 用户账户系统

| 功能 ID | 功能名称 | 功能描述 | 输入定义 | 输出定义 | 验收标准 |
|--------|---------|---------|---------|---------|---------|
| **F06-01** | LoginFragment 登录交互 | 提供用户登录界面和交互逻辑 | userId、password | 登录结果、UserData | 登录流程完整，验证准确 |
| **F06-02** | SignupFragment 注册流程 | 提供新用户注册界面和数据提交 | 用户信息 (userId、password 等) | 注册结果、新 UserData | 注册流程顺畅，数据保存成功 |
| **F06-03** | MoeyuAPIClient 认证流程 | 通过 API Client 实现用户认证和数据获取 | 认证凭证、请求参数 | 认证令牌、用户数据 | 认证机制安全，数据获取准确 |
| **F06-04** | UserData 本地持久化 | 使用对象序列化将 UserData 保存至 userData.dat | UserData 对象 | 持久化的文件 (userData.dat) | 持久化成功，可恢复数据 |
| **F06-05** | 等级系统设计 | 实现 Level 1-6 的六级用户等级体系 | level、exp(经验值) | 当前等级、升级进度 | 等级系统逻辑正确，升级判定准确 |
| **F06-06** | EXP 经验值累积 | 通过各项活动累积经验值，支持等级提升 | exp 增量、当前 exp | 更新后的 exp、等级变化 | 经验值累积准确，可追溯 |
| **F06-07** | EventData 等级提升事件 | 等级提升时触发 EventData 事件，记录升级信息 | 旧 level、新 level、时间戳 | EventData 对象、升级通知 | 事件触发及时，数据完整 |
| **F06-08** | Local 离线模式支持 | 支持 userId="local"的离线用户模式 | userId="local"标记 | 本地用户数据、离线功能可用 | 离线模式工作正常，数据独立 |

#### F07: 设置与配置

| 功能 ID | 功能名称 | 功能描述 | 输入定义 | 输出定义 | 验收标准 |
|--------|---------|---------|---------|---------|---------|
| **F07-01** | PreferenceActivity 设置界面 | 提供统一的设置界面，管理应用各项配置 | 配置项列表 | UI 设置页面、SharedPreferences | 设置界面完整，配置可保存 |
| **F07-02** | 相机开关控制 | 支持 AR 模式的相机开启/关闭控制 | 开关状态 (Boolean) | CameraPreview 状态、AR 模式切换 | 相机控制响应及时，AR 模式正常 |
| **F07-03** | 声音设置管理 | 管理语音播放、背景音乐等声音相关设置 | 音量值、静音状态 | 音频输出配置 | 声音设置生效，用户可调节 |
| **F07-04** | Config 单例配置管理 | 通过 Config 单例管理应用全局配置 | 配置参数、环境标志 (isProd) | 全局配置对象 | 配置管理集中，环境切换支持 |

---

## 三、核心业务流程

### 3.1 Live2D 交互流程

```
┌─────────────────────────────────────────────────────────────┐
│                    Live2D 交互完整流程                        │
└─────────────────────────────────────────────────────────────┘

开始
  ↓
[BathActivity 启动]
  ↓
[LAppLive2DManager 初始化]
  ├─ createView() 创建 GLSurfaceView
  ├─ setupModel() 加载 MOC 模型和纹理
  └─ startAnimation() 启动动画系统
  ↓
[用户触摸屏幕]
  ↓
[onTouchEvent() 接收触摸事件]
  ↓
[getRegion(pointF) 判定触摸区域]
  ├─ Face 区域 → 触发面部语音 + 表情
  ├─ Head 区域 → 触发头部语音 + Flip 检测
  ├─ Body 区域 → 触发身体语音 + 姿态变化
  ├─ Arm 区域 → 触发手臂语音 + 动作
  └─ Belly 区域 → 触发腹部语音 + 效果
  ↓
[LAppAnimation.touchesBegan() 处理触摸开始]
  ├─ 记录 flipStartX/Y 起始坐标
  ├─ 计算 mouseX/Y 面部目标位置
  └─ eyeMotion.setInterval(EYE_INTERVAL_TOUCHING) 调整眨眼频率
  ↓
[用户拖拽移动 (可选)]
  ↓
[LAppAnimation.touchesMoved() 处理拖拽]
  ├─ 计算总拖拽距离 _totalD
  ├─ _totalD > 500px → 触发 Flip 翻转
  └─ 更新面部跟随目标位置
  ↓
[updateDragMotion() Spring-Damper 物理模拟]
  ├─ 计算加速度和速度限制
  ├─ 更新 faceX/Y 当前位置
  └─ 映射到 PARAM_ANGLE_X/Y、PARAM_EYE_BALL_X/Y
  ↓
[LAppAnimation.updateParam() 参数更新]
  ├─ mainMotionMgr.updateParam() 主运动队列更新
  ├─ expressionMgr.updateParam() 表情队列更新
  └─ eyeMotion.setParam() 眨眼控制更新
  ↓
[Live2DModelAndroid.update() + draw()]
  ↓
[LAppRenderer.onDrawFrame() OpenGL 渲染]
  ├─ 绘制背景纹理
  ├─ 应用坐标变换 (translate + scale)
  └─ 渲染 Live2D 模型
  ↓
[GLSurfaceView 显示更新画面]
  ↓
[VoiceManager.playSound() 播放对应语音]
  ↓
结束 (返回 Idle 循环)
```

### 3.2 Gacha 抽卡完整流程

```
┌─────────────────────────────────────────────────────────────┐
│                      Gacha 抽卡完整流程                       │
└─────────────────────────────────────────────────────────────┘

开始
  ↓
[用户进入 GatyaActivity]
  ↓
[显示当前金币余额 (BRONZE/GOLD/PLATINUM)]
  ↓
[用户选择金币类型并点击抽卡按钮]
  ↓
[GachaFragment 接收抽卡请求]
  ↓
[GachaTask.execute() 启动异步任务]
  ↓
[doInBackground() 后台执行]
  ↓
[MoeyuAPIClient.userGatya(userId, coinType)]
  ↓
[步骤 1: 构建未持有物品列表 noHolds]
  ├─ 遍历 25 种物品 (ID 1-25)
  ├─ 检查 userData.hasItem(i)
  └─ 将未收集物品加入 noHolds 列表
  ↓
[步骤 2: 根据金币类型设置 rate 并扣除]
  ├─ BRONZE → rate=2, bronzeCoin--
  ├─ GOLD → rate=3, goldCoin--
  └─ PLATINUM → rate=4, platinumCoin--
  ↓
[步骤 3: getGachaResult(rate, noHolds) 概率计算]
  ↓
[生成随机数 randomResult = abs(random.nextInt() % 10)]
  ↓
[概率判定：rate > randomResult?]
  ├─ YES (或 items 为空) → 未收集物品优先策略
  │   └─ 从 noHolds 中随机选择 itemId
  │       └─ userData.addItem(itemId)
  └─ NO → 已收集物品随机策略
      └─ 从 userData.getItems() 中随机选择 itemId
  ↓
[步骤 4: saveUserData() 持久化保存]
  ├─ ObjectOutputStream 序列化 UserData
  └─ 写入 localUserData.dat 文件
  ↓
[GachaResult 封装结果 (userData + itemId)]
  ↓
[onPostExecute() 主线程回调]
  ↓
[MoeyuAPITaskListener.onSuccess(result) 成功处理]
  ↓
[storeUserData() 存储用户数据]
  ├─ UserDataManager.saveUserData(userData)
  └─ ItemTableController.update() 更新物品表 UI
  ↓
[跳转至 GachaResultActivity 展示结果]
  ↓
[显示获得的物品详情和动画效果]
  ↓
[更新 UI 显示的金币余额和收集进度]
  ↓
结束
```

### 3.3 Billing 购买完整流程

```
┌─────────────────────────────────────────────────────────────┐
│                      Billing 购买完整流程                     │
└─────────────────────────────────────────────────────────────┘

开始
  ↓
[用户在 GatyaActivity 点击购买按钮]
  ↓
[BillingService.requestPurchase(productId, developerPayload)]
  ↓
[RequestPurchase 请求对象创建]
  ├─ mProductId = productId
  └─ mDeveloperPayload = payload
  ↓
[runRequest() 执行请求]
  ↓
[检查 BillingService 连接状态]
  ├─ 已连接 → runIfConnected() 直接执行
  └─ 未连接 → bindToMarketBillingService() 建立连接
      └─ 加入 mPendingRequests 待处理队列
  ↓
[run() 执行购买请求]
  ↓
[构建 REQUEST_PURCHASE 请求 Bundle]
  ├─ BILLING_REQUEST_ITEM_ID = productId
  └─ BILLING_REQUEST_DEVELOPER_PAYLOAD = payload
  ↓
[mService.sendBillingRequest() 发送请求]
  ↓
[获取 PendingIntent (BILLING_RESPONSE_PURCHASE_INTENT)]
  ↓
[ResponseHandler.buyPageIntentResponse(pendingIntent)]
  ↓
[启动 Google Play Store 支付界面]
  ↓
[用户完成支付操作]
  ↓
[BroadcastReceiver → BillingReceiver 接收购买广播]
  ↓
[BillingService.purchaseStateChanged() 状态变化处理]
  ↓
[Security.generateNonce() 生成 Nonce]
  ↓
[GetPurchaseInformation 获取购买信息]
  ↓
[收到 signedData 和 signature]
  ↓
[Security.verifyPurchase(signedData, signature) 安全验证]
  ↓
[步骤 1: RSA-2048 公钥生成]
  ├─ Base64.decode(encodedPublicKey)
  └─ KeyFactory.generatePublic(keySpec)
  ↓
[步骤 2: 签名验证]
  ├─ Signature.getInstance("SHA1withRSA")
  ├─ sig.initVerify(publicKey)
  ├─ sig.update(signedData)
  └─ sig.verify(Base64.decode(signature))
  ↓
[步骤 3: JSON 数据解析]
  ├─ JSONObject(signedData)
  ├─ 提取 nonce、orders 数组
  └─ 解析每个 order 的详细信息
  ↓
[步骤 4: Nonce 重放检查]
  ├─ isNonceKnown(nonce) 检查是否已知
  ├─ YES → 通过验证，继续处理
  └─ NO → 可能是重放攻击，返回 null
  ↓
[构建 VerifiedPurchase 对象列表]
  ├─ purchaseState (PURCHASED/RESTORED/CANCELLED)
  ├─ productId、orderId、purchaseTime
  └─ developerPayload、notificationId
  ↓
[removeNonce(nonce) 清理已使用的 Nonce]
  ↓
[MoeyuAPIClient.userBilling() 服务器端确认]
  ├─ HttpPost(BASE_URL + "user/billing")
  ├─ 参数：inapp_signed_data、inapp_signature
  └─ 执行 HTTP 请求
  ↓
[服务器响应处理]
  ↓
[UserData.updateItems() 更新用户数据]
  ├─ 根据购买内容更新金币数量
  └─ 添加获得的物品到 items 列表
  ↓
[ItemTableController.update() → Live2D 奖励动画]
  ↓
[ResponseHandler.purchaseResponse() UI 反馈]
  ├─ PURCHASED → 显示购买成功 Toast，跳转结果页
  ├─ RESTORED → 显示恢复成功提示
  └─ CANCELLED → 显示取消提示，返回主界面
  ↓
[PurchaseObserver.onPurchaseStateChanged() 观察者通知]
  ↓
结束
```

### 3.4 用户数据持久化流程

```
┌─────────────────────────────────────────────────────────────┐
│                    用户数据持久化完整流程                     │
└─────────────────────────────────────────────────────────────┘

开始 (应用启动)
  ↓
[MoeyuAPIClient(Context) 初始化]
  ↓
[检查 userDataFile.exists()]
  ↓
[文件存在？]
  ├─ YES → readUserData() 读取已有数据
  │   ↓
  │   [ObjectInputStream 反序列化]
  │   ↓
  │   [加载 UserData 对象]
  │   ↓
  │   [检查每日登录奖励]
  │   ├─ 计算 lastLoginTime 与当前日期差值
  │   ├─ 跨天且 bonus=true → 发放 BRONZE 金币
  │   │   └─ bronzeCoin += 奖励数量
  │   └─ 更新 bonus=false, lastLoginTime=currentTime
  │   ↓
  │   [saveUserData() 保存更新后的数据]
  │   ↓
  │   [UserData 加载完成，进入主界面]
  │
  └─ NO → createLocal() 创建本地用户
      ↓
      [初始化默认 UserData]
      ├─ userId = "local"
      ├─ bronzeCoin = 10 (初始青铜币)
      ├─ level = 1, exp = 0
      ├─ bonus = true
      └─ lastLoginTime = System.currentTimeMillis()
      ↓
      [saveUserData() 保存初始数据]
      ↓
      [进入主界面 (离线模式)]
  ↓
[各 Activity/Fragment 使用 UserData]
  ↓
[发生数据变更 (抽卡/购买/升级等)]
  ↓
[storeUserData(userData) 存储更新]
  ↓
[ObjectOutputStream 序列化]
  ↓
[写入 localUserData.dat 文件]
  ↓
结束 (数据持久化完成)
```

---

## 四、用户交互说明

### 4.1 UI 组件架构

#### Activity 组件清单

| Activity | 主要功能 | 关键 UI 元素 | 交互逻辑 |
|---------|---------|-----------|---------|
| **MainActivity** | 应用入口，片头播放 | Splash 界面、片头视频 | 启动 → 播放片头 → 跳转 TitleActivity |
| **TitleActivity** | 标题页、登录注册入口 | 标题视图、登录/注册按钮、UserData 显示 | 显示用户信息 → 提供登录/注册入口 → 导航至主功能 |
| **BathActivity** | Live2D 核心交互界面 | GLSurfaceView、触摸区域、场景切换按钮、语音控制 | 实时渲染 Live2D 模型 → 处理触摸事件 → 触发语音和动画 → 支持场景切换 |
| **GatyaActivity** | Gacha 抽卡主界面 | 金币显示区、抽卡转盘、购买按钮、历史记录 | 展示三阶金币余额 → 用户选择金币类型 → 执行抽卡 → 显示结果 → 提供内购入口 |
| **GatyaResultActivity** | 抽卡结果展示 | 物品展示区、动画效果、详情信息、返回按钮 | 展示获得的物品 → 播放获得动画 → 显示物品详情 → 更新收集进度 |
| **CollectionRoomActivity** | 收藏房间管理入口 | 分类导航 (物品/语音/笔记)、概览统计 | 提供三大收藏分类入口 → 显示收藏统计 → 导航至对应 Activity |
| **ItemCollectionActivity** | 物品收藏展示 | ItemGridView、物品图标、收集进度条、详情对话框 | Grid 布局展示 25 种物品 → 已收集/未收集状态区分 → 点击查看详情 |
| **VoiceCollectionActivity** | 语音收藏列表 | ListView、语音条目 (名称/时长/语言)、播放控件 | 列表展示所有语音 → 支持播放/暂停 → 显示语音元数据 |
| **NoteCollectionActivity** | 笔记收藏管理 | 笔记列表、时间戳、编辑/删除操作 | 展示用户笔记 → 支持增删改查 → 时间排序 |
| **PreferenceActivity** | 设置界面 | 开关控件 (相机/声音)、配置项列表、保存按钮 | 提供各项设置选项 → 实时应用配置 → 持久化保存 |

#### Fragment 组件清单

| Fragment | 所属 Activity | 功能描述 | 交互特点 |
|---------|-------------|---------|---------|
| **LoginFragment** | TitleActivity | 用户登录交互 | 表单输入 → 异步验证 → 成功跳转/错误提示 |
| **SignupFragment** | TitleActivity | 新用户注册流程 | 多字段输入 → 数据验证 → 注册提交 → 自动登录 |
| **NetworkBaseFragment** | 基类 | 网络请求基础 Fragment | 提供统一的网络加载、错误处理机制 |
| **GachaFragment** | GatyaActivity | 抽卡操作集成 | 金币选择交互 → 抽卡动画 → 结果过渡 |

### 4.2 关键交互场景

#### 场景 1: Live2D 触摸交互

**用户操作流程**:
1. 用户进入 BathActivity，看到 Live2D 角色在浴场场景中
2. 用户触摸屏幕不同区域 (Face/Head/Body/Arm/Belly)
3. 角色根据触摸位置产生相应的语音回应和表情变化
4. 用户拖拽屏幕，角色的面部会跟随手指移动 (Spring-Damper 效果)
5. 当拖拽距离超过 500px 时，角色头部或身体会发生 Flip 翻转

**交互反馈**:
- **视觉反馈**: 角色姿态变化、表情切换、Flip 动画
- **听觉反馈**: 对应区域的语音播放
- **触觉反馈**: (可选) 震动效果

**边界条件处理**:
- 快速点击 vs 长按：通过时间阈值区分
- 多指触摸：支持主触摸点优先处理
- 边缘区域：扩大有效触摸区域，避免误判

#### 场景 2: Gacha 抽卡体验

**用户操作流程**:
1. 用户进入 GatyaActivity，查看当前 BRONZE/GOLD/PLATINUM 金币余额
2. 选择想要使用的金币类型 (点击对应图标)
3. 点击抽卡按钮，触发抽卡动画
4. 观看抽卡转盘动画和概率计算过程
5. 获得物品后，跳转至 GatyaResultActivity 查看详情
6. 返回主界面，查看更新后的金币余额和收集进度

**交互反馈**:
- **视觉反馈**: 金币选择高亮、抽卡转盘动画、结果展示特效
- **状态提示**: 金币不足提示、每日奖励提示、收集完成庆祝
- **导航流畅**: 页面间过渡动画，返回路径清晰

**异常处理**:
- 金币不足：Toast 提示 + 引导至内购界面
- 网络异常：错误对话框 + 重试机制
- 数据保存失败：本地缓存 + 同步提示

#### 场景 3: Billing 购买流程

**用户操作流程**:
1. 用户在 GatyaActivity 点击购买按钮
2. 选择要购买的 SKU 商品 (不同金币包)
3. 跳转至 Google Play Store 支付界面
4. 完成支付 (信用卡/账户余额等)
5. 返回应用，显示购买成功提示
6. 自动更新用户数据，金币余额增加
7. 可立即使用新增金币进行抽卡

**交互反馈**:
- **流程引导**: 清晰的购买步骤提示
- **安全提示**: RSA 验证过程说明 (可选展示)
- **结果确认**: 购买成功/取消/恢复的明确反馈
- **数据同步**: 实时显示更新后的用户数据

**状态处理**:
- **PURCHASED**: 新购买成功，更新数据并庆祝动画
- **RESTORED**: 已购买项目恢复，提示用户
- **CANCELLED**: 用户取消或支付失败，友好提示

### 4.3 导航结构

```
应用启动
  ↓
MainActivity (片头播放)
  ↓
TitleActivity
  ├─ LoginFragment (登录)
  ├─ SignupFragment (注册)
  └─ 导航至主功能
      ↓
  ┌─────────────────────────────────────┐
  │          主功能导航中心               │
  └─────────────────────────────────────┘
      ↓
  ├──────────────────┬──────────────────┬──────────────────┐
  ↓                  ↓                  ↓                  ↓
BathActivity     GatyaActivity    CollectionRoom   PreferenceActivity
(Live2D 交互)     (抽卡系统)        Activity (收藏)   (设置)
      ↓               ↓                  ↓                  ↓
  - 触摸交互       - 金币管理         - ItemCollection  - 相机开关
  - 语音播放       - Gacha 抽卡       - VoiceCollection  - 声音设置
  - 场景切换       - 内购购买         - NoteCollection   - 其他配置
      ↓               ↓                  ↓
  (循环交互)     GatyaResultActivity    (浏览管理)
                 (结果展示)
```

---

## 五、数据模型定义

### 5.1 核心数据模型

#### UserData (用户数据模型)

**模型定义**:
```java
public class UserData implements Serializable {
    // ========== 用户标识 ==========
    String userId;              // 用户唯一标识 (如 "user001" 或 "local")
    String state;               // 用户状态 (如 "active", "pending")
    
    // ========== 三阶金币系统 ==========
    int bronzeCoin;             // 青铜币数量 (每日奖励获得，用于基础抽卡)
    int goldCoin;               // 黄金币数量 (内购获得，用于标准抽卡)
    int platinumCoin;           // 白金币数量 (高级内购，用于高级抽卡)
    
    // ========== 物品收集系统 ==========
    List<Integer> items;        // 已收集的物品 ID 列表 (共 25 种，ID 1-25)
    static final int MAX_ITEM_COUNT = 25;  // 物品总数常量
    
    // ========== 等级系统 ==========
    int level;                  // 当前等级 (1-6 级)
    int exp;                    // 当前经验值
    static final int MAX_LEVEL = 6;  // 最高等级常量
    
    // ========== 奖励系统 ==========
    boolean bonus;              // 每日奖励标记 (true=可领取)
    long lastLoginTime;         // 最后登录时间戳
    
    // ========== 金币上限保护 ==========
    static final int MAX_COIN_COUNT = 99;  // 每种金币的数量上限
}
```

**字段说明**:

| 字段名 | 类型 | 必填 | 默认值 | 约束条件 | 说明 |
|--------|------|------|--------|---------|------|
| userId | String | 是 | - | 非空，唯一 | 用户标识，"local"表示离线模式 |
| state | String | 是 | "active" | 枚举值 | 用户账户状态 |
| bronzeCoin | int | 是 | 10 | 0-99 | 青铜币数量，上限 99 |
| goldCoin | int | 是 | 0 | 0-99 | 黄金币数量，上限 99 |
| platinumCoin | int | 是 | 0 | 0-99 | 白金币数量，上限 99 |
| items | List<Integer> | 是 | [] | ID 范围 1-25 | 已收集物品列表 |
| level | int | 是 | 1 | 1-6 | 用户等级 |
| exp | int | 是 | 0 | >= 0 | 累积经验值 |
| bonus | boolean | 是 | true | - | 每日奖励可领取标记 |
| lastLoginTime | long | 是 | 当前时间戳 | - | 最后登录时间 (毫秒) |

**数据关系**:
- `items` 列表与 25 种物品一一对应
- `level` 与 `exp` 关联，经验值累积触发等级提升
- `bonus` 与 `lastLoginTime` 配合实现每日奖励逻辑

#### GachaResult (抽卡结果模型)

**模型定义**:
```java
public class GachaResult {
    private UserData userData;  // 更新后的用户数据 (包含新的金币余额和物品列表)
    private int itemId;         // 本次抽卡获得的物品 ID (1-25)
    
    // Getter/Setter 方法
    public UserData getUserData() { return userData; }
    public void setUserData(UserData userData) { this.userData = userData; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
}
```

**字段说明**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userData | UserData | 是 | 抽卡后更新的用户完整数据 |
| itemId | int | 是 | 本次获得的物品 ID，用于结果展示 |

**使用场景**:
- GachaTask 异步任务执行后的返回结果
- GachaResultActivity 的数据源
- UI 展示和动画触发的依据

#### EventData (事件数据模型)

**模型定义**:
```java
public class EventData implements Serializable {
    private String eventType;      // 事件类型 (如 "LEVEL_UP", "ITEM_OBTAINED")
    private String userId;         // 关联用户 ID
    private long timestamp;        // 事件发生时间戳
    private Object eventData;      // 事件具体数据 (根据类型不同)
    
    // 等级提升事件示例
    public static class LevelUpEvent {
        private int oldLevel;       // 原等级
        private int newLevel;       // 新等级
        private int expGained;      // 获得的经验值
    }
    
    // Getter/Setter 方法...
}
```

**事件类型枚举**:

| 事件类型 | 常量值 | 说明 | 触发条件 |
|---------|--------|------|---------|
| LEVEL_UP | "LEVEL_UP" | 等级提升事件 | exp 累积达到升级阈值 |
| ITEM_OBTAINED | "ITEM_OBTAINED" | 获得新物品事件 | Gacha 抽到未收集物品 |
| DAILY_BONUS | "DAILY_BONUS" | 每日奖励发放事件 | 登录且跨天，bonus=true |
| PURCHASE_COMPLETE | "PURCHASE_COMPLETE" | 购买完成事件 | Billing 流程成功完成 |

#### VerifiedPurchase (已验证的购买信息)

**模型定义**:
```java
public class VerifiedPurchase {
    public String developerPayload;   // 开发者自定义 payload
    public String notificationId;     // 通知 ID
    public String orderId;            // 订单 ID
    public String productId;          // 商品 SKU ID
    public PurchaseState purchaseState; // 购买状态枚举
    public long purchaseTime;         // 购买时间戳 (秒)
    
    // 构造函数和 Getter/Setter...
}

// 购买状态枚举
public enum PurchaseState {
    PURCHASED,    // 已购买 (新购买成功)
    CANCELLED,    // 已取消 (用户取消或支付失败)
    RESTORED      // 已恢复 (已购买项目的恢复)
}
```

**字段说明**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| developerPayload | String | 否 | 开发者自定义数据，用于业务逻辑 |
| notificationId | String | 是 | Google Play 通知标识 |
| orderId | String | 是 | 唯一订单编号 |
| productId | String | 是 | SKU 商品 ID |
| purchaseState | PurchaseState | 是 | 购买状态枚举 |
| purchaseTime | long | 是 | Unix 时间戳 (秒) |

### 5.2 数据模型关系图

```
┌─────────────────────────────────────────────────────────────┐
│                      核心数据模型关系                        │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┐
│   UserData       │  (核心用户数据)
├──────────────────┤
│ - userId         │
│ - bronzeCoin     │◄──────┐
│ - goldCoin       │◄──────┼────── F03 Gacha 抽卡
│ - platinumCoin   │◄──────┼────── F04 Billing 购买
│ - items (List)   │──────►┤
│ - level          │──────►┤
│ - exp            │──────►┘
│ - bonus          │
│ - lastLoginTime  │
└──────────────────┘
         │
         │ 1:N
         ↓
┌──────────────────┐
│   GachaResult    │  (抽卡结果)
├──────────────────┤
│ - userData       │──────► 引用 UserData
│ - itemId         │──────► 关联 items[0-24]
└──────────────────┘

┌──────────────────┐
│   EventData      │  (事件数据)
├──────────────────┤
│ - eventType      │
│ - userId         │──────► 关联 UserData.userId
│ - timestamp      │
│ - eventData      │──────► LevelUpEvent / 其他
└──────────────────┘

┌──────────────────┐
│  VerifiedPurchase│  (购买验证信息)
├──────────────────┤
│ - productId      │
│ - purchaseState  │──────► F04 Billing 状态处理
│ - orderId        │
│ - purchaseTime   │
│ - developerPayload│
└──────────────────┘
         │
         │ 触发更新
         ↓
┌──────────────────┐
│   UserData       │◄────── 更新 coins/items
└──────────────────┘
```

### 5.3 配置数据模型

#### voice.json 结构定义

**JSON 结构示例**:
```json
{
  "voice_list": [
    {
      "id": "face_touch_01",
      "name": "面部触摸语音 1",
      "file_ja": "voice/face_01.mp3",
      "file_cn": "voice_cn/face_01.mp3",
      "region": "Face",
      "duration": 2500
    },
    {
      "id": "head_touch_01",
      "name": "头部触摸语音 1",
      "file_ja": "voice/head_01.mp3",
      "file_cn": "voice_cn/head_01.mp3",
      "region": "Head",
      "duration": 3000
    }
    // ... 更多语音配置
  ]
}
```

**字段说明**:

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 语音唯一标识 |
| name | String | 是 | 语音名称 (用于管理) |
| file_ja | String | 是 | 日语语音文件路径 |
| file_cn | String | 是 | 中文语音文件路径 |
| region | String | 是 | 关联的触摸区域 (Face/Head/Body/Arm/Belly) |
| duration | int | 否 | 语音时长 (毫秒) |

---

## 六、接口需求规范

### 6.1 API 接口规范

#### MoeyuAPIClient 核心接口

**Base URL**: `http://api.moeapk.com/third_party/moeyu/`

#### 接口 1: 用户数据获取

**接口路径**: `GET /user/data`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| user_id | String | 是 | 用户唯一标识 |
| app_id | String | 是 | 应用 ID ("MOEYU_001") |
| app_version | String | 是 | 应用版本 ("1") |
| nonce | long | 是 | 一次性随机数 |
| timestamp | long | 是 | 请求时间戳 |
| signature | String | 是 | SHA-1 签名 |

**响应格式**:
```json
{
  "user_id": "user001",
  "state": "active",
  "bronze_coin": 25,
  "gold_coin": 10,
  "platinum_coin": 5,
  "items": [1, 3, 5, 8, 12],
  "level": 2,
  "exp": 1500,
  "bonus": true,
  "last_login_time": 1710000000000
}
```

**错误码**:
| 状态码 | 说明 | 处理方式 |
|--------|------|---------|
| 200 | 成功 | 解析响应，更新 UserData |
| 400 | 请求参数错误 | 检查参数完整性 |
| 401 | 认证失败 | 验证 signature 和 nonce |
| 500 | 服务器内部错误 | 记录日志，友好提示 |

#### 接口 2: Gacha 抽卡

**接口路径**: `POST /user/gatya`

**请求体**:
```json
{
  "user_id": "user001",
  "coin_type": "BRONZE",
  "no_holds": [2, 4, 6, 7, 9],
  "signature": "sha1_signature_string",
  "timestamp": 1710000000000
}
```

**响应格式**:
```json
{
  "user_id": "user001",
  "bronze_coin": 24,
  "gold_coin": 10,
  "platinum_coin": 5,
  "items": [1, 3, 5, 8, 12, 2],
  "level": 2,
  "exp": 1520,
  "obtained_item_id": 2
}
```

**业务逻辑**:
1. 验证请求签名和时间戳
2. 根据 coin_type 扣除对应金币
3. 执行概率计算 (rate 判定)
4. 应用未收集物品优先策略
5. 更新用户数据并持久化
6. 返回 GachaResult

#### 接口 3: Billing 购买确认

**接口路径**: `POST /user/billing`

**请求体**:
```json
{
  "user_id": "user001",
  "inapp_signed_data": "base64_encoded_json_data",
  "inapp_signature": "base64_encoded_rsa_signature",
  "nonce": 123456789,
  "timestamp": 1710000000000
}
```

**响应格式**:
```json
{
  "user_id": "user001",
  "state": "active",
  "bronze_coin": 50,
  "gold_coin": 30,
  "platinum_coin": 10,
  "items": [1, 3, 5, 8, 12, 15, 18],
  "level": 3,
  "exp": 2000,
  "purchase_status": "SUCCESS"
}
```

**安全机制**:
- **RSA-2048 验证**: 服务器端使用公钥验证签名
- **Nonce 重放防护**: 检查 nonce 是否已使用
- **时间戳有效性**: 请求时间戳需在有效窗口内 (±5 分钟)

### 6.2 Google Play Billing 接口规范

#### BillingService 核心方法

**方法 1: requestPurchase**
```java
/**
 * 发起购买请求
 * @param productId SKU 商品 ID
 * @param developerPayload 开发者自定义 payload
 * @return 请求是否成功提交
 */
public boolean requestPurchase(String productId, String developerPayload);
```

**方法 2: verifyPurchase**
```java
/**
 * 验证购买信息
 * @param signedData Base64 编码的购买数据 JSON
 * @param signature Base64 编码的 RSA 签名
 * @return 验证通过的购买列表，失败返回 null
 */
public ArrayList<VerifiedPurchase> verifyPurchase(String signedData, String signature);
```

#### PurchaseObserver 接口

```java
/**
 * 购买状态观察者接口
 */
public interface PurchaseObserver {
    /**
     * 购买状态变化回调
     * @param state 购买状态 (PURCHASED/CANCELLED/RESTORED)
     * @param purchase 验证通过的购买信息
     */
    void onPurchaseStateChanged(PurchaseState state, VerifiedPurchase purchase);
}
```

### 6.3 Live2D 引擎接口规范

#### LAppLive2DManager 核心接口

**模型管理**:
```java
/**
 * 设置并加载 Live2D 模型
 * @return 模型加载成功返回 true
 */
public boolean setupModel();

/**
 * 释放模型资源
 */
public void releaseModel();
```

**动画控制**:
```java
/**
 * 启动动画系统
 */
public void startAnimation();

/**
 * 停止动画系统
 */
public void stopAnimation();
```

**回调接口**:
```java
/**
 * 模型加载完成监听器
 */
public interface FinishListener {
    /**
     * 模型设置完成回调
     */
    void onFinishSetupModel();
}
```

---

## 七、非功能需求

### 7.1 性能需求

#### 响应时间要求

| 操作类型 | 目标响应时间 | 可接受上限 | 测量条件 |
|---------|-------------|-----------|---------|
| Live2D 模型加载 | < 3 秒 | 5 秒 | 冷启动，中档设备 |
| 触摸事件响应 | < 100ms | 200ms | 从触摸到动画/语音触发 |
| Gacha 抽卡执行 | < 2 秒 | 3 秒 | 网络良好环境 |
| Billing 购买流程 | < 5 秒 | 10 秒 | 含 Google Play 交互时间 |
| 页面切换导航 | < 500ms | 1 秒 | Activity/Fragment间切换 |

#### 资源占用要求

| 资源类型 | 目标值 | 说明 |
|---------|--------|------|
| **内存占用** | < 200MB | 正常运行时 (含 Live2D 纹理) |
| **CPU 使用率** | < 40% | 单核，Idle 动画状态 |
| **渲染帧率** | ≥ 30fps | Live2D 渲染，目标 60fps |
| **网络流量** | < 5MB/次 | 完整抽卡 + 数据同步流程 |

#### 并发处理能力

- **异步任务并发**: BaseTask 支持多个 AsyncTask 并行执行
- **请求队列管理**: BillingService 的 mPendingRequests 支持请求排队
- **动画队列**: MotionQueueManager 支持多运动队列并发更新

### 7.2 安全需求

#### 数据安全

| 安全项 | 实现机制 | 说明 |
|--------|---------|------|
| **传输加密** | HTTPS (建议) / HTTP + 签名 | API 通信数据保护 |
| **签名验证** | SHA-1 + Nonce + Timestamp | 防止重放攻击和篡改 |
| **购买验证** | RSA-2048 公钥加密 | Google Play 购买数据完整性 |
| **本地存储** | Java Serialization + 文件权限 | userData.dat 持久化安全 |

#### 认证与授权

- **用户认证**: LoginTask/SignupTask实现用户名密码认证
- **请求授权**: 每个 API 请求携带 signature 进行授权验证
- **会话管理**: userId 作为会话标识，支持离线模式 (userId="local")

### 7.3 兼容性需求

#### 平台兼容

| 兼容项 | 要求 | 说明 |
|--------|------|------|
| **Android 版本** | Android 5.0+ (API 19+) | 最低支持版本 |
| **目标 SDK** | Android 9.0 (API 28) | 开发目标版本 |
| **屏幕适配** | 多种分辨率 | 支持不同 DPI 和屏幕尺寸 |
| **OpenGL ES** | 2.0+ | Live2D 渲染依赖 |

#### 语言兼容

- **多语言支持**: 
  - 语音资源：日语 (voice/) + 中文 (voice_cn/)
  - UI 文本：(建议扩展) 支持 i18n 资源文件

### 7.4 可靠性需求

#### 容错机制

| 场景 | 容错措施 | 用户反馈 |
|------|---------|---------|
| **网络异常** | 超时重试、错误码处理 | Toast 提示 + 重试按钮 |
| **数据加载失败** | 本地缓存回退 | 显示本地数据 + 同步提示 |
| **购买流程中断** | 状态恢复 (RESTORED) | 恢复已购买项目 |
| **Live2D 渲染异常** | 异常捕获 + 降级方案 | 错误提示 + 重新加载 |

#### 数据持久化

- **自动保存**: 关键操作后自动保存 UserData
- **崩溃恢复**: 应用重启后恢复用户状态和数据
- **版本兼容**: 数据格式支持版本升级和迁移

### 7.5 可维护性需求

#### 代码质量

- **设计模式应用**: Singleton、Factory、Observer、MVC 等模式规范使用
- **接口抽象**: 核心功能通过接口定义，便于扩展和测试
- **日志系统**: (建议完善) 结构化日志，支持问题追踪

#### 文档要求

- **API 文档**: 完整的接口说明和示例
- **数据字典**: 数据结构、枚举值、错误码的详细定义
- **部署文档**: 构建、配置、部署的标准化流程说明

---

## 八、验收标准总览

### 8.1 功能验收标准

#### F01: Live2D 交互系统验收标准

| 测试项 | 验收条件 | 测试方法 |
|--------|---------|---------|
| 模型加载 | 3 秒内完成加载，纹理无缺失 | 性能测试 + 视觉检查 |
| 触摸响应 | 触摸后 100ms 内触发反馈 | 时间测量 + 用户测试 |
| 面部跟随 | Spring-Damper 效果平滑自然 | 拖拽测试 + 参数验证 |
| Flip 触发 | 拖拽>500px 时准确触发翻转 | 距离阈值测试 |
| Idle 动画 | 4 组场景随机播放，无重复感 | 长时间观察测试 |
| 眨眼控制 | 正常 4s、触摸 1.5s 间隔准确 | 计时验证 |

#### F03: Gacha 抽卡系统验收标准

| 测试项 | 验收条件 | 测试方法 |
|--------|---------|---------|
| 金币管理 | 三阶金币计数准确，上限 99 保护生效 | 边界值测试 |
| 每日奖励 | 跨天登录自动发放 BRONZE，无重复 | 模拟多日登录测试 |
| 概率分布 | rate=2/3/4对应10%/50%/90%触发率 | 大样本统计测试 (N>=1000) |
| 未收集优先 | 未收集物品时优先抽取新物品 | 逻辑验证 + 用户测试 |
| 异步执行 | 抽卡过程 UI 不阻塞，响应流畅 | 用户体验测试 |

#### F04: Billing 内购系统验收标准

| 测试项 | 验收条件 | 测试方法 |
|--------|---------|---------|
| 支付集成 | Google Play 支付流程完整可用 | 真机测试 (TestTrack) |
| RSA 验证 | 签名验证准确率 100% | 单元测试 + 集成测试 |
| Nonce 防护 | 重放请求被正确识别和拒绝 | 模拟重放攻击测试 |
| 状态处理 | PURCHASED/RESTORED/CANCELLED处理正确 | 场景测试 |
| 数据同步 | 购买后用户数据准确更新 | 数据一致性验证 |

### 8.2 性能验收标准

| 性能指标 | 目标值 | 测试环境 | 验收方法 |
|---------|--------|---------|---------|
| 启动时间 | 冷启动 < 5 秒 | 中档 Android 设备 | 多次测量取平均 |
| 帧率稳定性 | Live2D 渲染 ≥ 30fps | OpenGL ES 2.0+ | 性能监控工具 |
| 内存占用 | 正常运行 < 200MB | 同上 | Memory Profiler |
| 网络响应 | API 平均响应 < 1 秒 | 良好网络环境 | 网络模拟测试 |

### 8.3 安全验收标准

| 安全项 | 验收条件 | 验证方法 |
|--------|---------|---------|
| API 签名验证 | SHA-1 签名生成和验证正确 | 单元测试 + 渗透测试 |
| RSA 购买验证 | 公钥验证通过率 100% | 自动化测试套件 |
| Nonce 有效性 | 重放攻击检测成功率 >= 99% | 模拟攻击测试 |
| 数据持久化安全 | userData.dat 读写和恢复正确 | 异常中断恢复测试 |

---

## 九、变更日志

| 日期 | 版本 | 变更类型 | 变更描述 | 变更人 |
|------|------|---------|---------|--------|
| 2026-03-27 | v1.0 | 初始版本 | 基于项目逆向分析生成完整需求规格说明书，包含功能清单、业务流程、数据模型、接口规范和非功能需求 | requirement-analyst (ANALYSIS-001-FUNC) |

---

## 十、附录

### 附录 A: 功能 - 任务映射表

| 功能模块 | 关联任务 ID | 优先级 | 预计开发工作量 |
|---------|-----------|--------|---------------|
| F01: Live2D 交互系统 | ANALYSIS-001-FUNC | P0 | 已完成 (逆向分析) |
| F02: 语音系统 | ANALYSIS-001-FUNC | P0 | 已完成 (逆向分析) |
| F03: Gacha 抽卡系统 | ANALYSIS-001-FUNC | P0 | 已完成 (逆向分析) |
| F04: Billing 内购系统 | ANALYSIS-001-FUNC | P0 | 已完成 (逆向分析) |
| F05: Collection 收藏系统 | ANALYSIS-001-FUNC | P1 | 已完成 (逆向分析) |
| F06: 用户账户系统 | ANALYSIS-001-FUNC | P0 | 已完成 (逆向分析) |
| F07: 设置与配置 | ANALYSIS-001-FUNC | P2 | 已完成 (逆向分析) |

### 附录 B: 术语 Glossary

| 英文术语 | 中文翻译 | 说明 |
|---------|---------|------|
| Live2D | Live2D | 二维模型三维渲染技术 |
| MOC | 模型容器 | Live2D 模型文件格式 |
| Gacha | 抽卡系统 | 源自日语"ガチャ"，概率获得物品机制 |
| SKU | 库存量单位 | 内购商品的最小单元 |
| Nonce | 一次性随机数 | 用于防止重放攻击的随机值 |
| Parts | 部件/图层 | Live2D 模型中可独立控制的图层组件 |
| Spring-Damper | 弹簧 - 阻尼器 | 物理模拟算法，用于平滑运动 |
| OpenGL ES | OpenGL for Embedded Systems | 嵌入式系统的 OpenGL 图形 API |
| AsyncTask | 异步任务 | Android 后台任务执行框架 |
| RSA-2048 | RSA-2048 | 2048 位 RSA 非对称加密算法 |

### 附录 C: 参考文档

1. **项目概览**: `ai/analysis/overview.md`
2. **架构文档**: `ai/dev/project.md`
3. **任务 YAML**: `ai/tasks/active/task-ANALYSIS-001.yaml`

---

**文档版本**: v1.0  
**最后更新**: 2026-03-27  
**文档状态**: 已完成  
**下一步建议**: 委托 @code-review-qa 进行代码质量评估
