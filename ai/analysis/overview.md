# ResurrectionMoeyu 项目概览

**版本**: v1.0  
**创建日期**: 2026-03-27  
**分析代理**: project-analyzer  
**任务 ID**: ANALYSIS-001

---

## 一、项目基本信息

### 1.1 项目概述

ResurrectionMoeyu 是一个基于 **Live2D 技术**的 Android 应用程序，实现了虚拟角色的交互式展示系统。项目包含完整的用户交互、内购计费、抽卡系统和数据持久化功能。

### 1.2 技术栈

| 类别 | 技术/框架 | 版本/说明 |
|------|----------|-----------|
| **平台** | Android | SDK 28 (Android 9.0) |
| **最低支持** | Android 5.0+ | API Level 19 |
| **开发语言** | Java | Java 8 |
| **构建工具** | Gradle | 9.0.0 |
| **核心引擎** | Live2D Android SDK | live2d_android.jar |
| **网络层** | Apache HttpClient | 3.x (旧版) |
| **UI 渲染** | OpenGL ES | GLSurfaceView |
| **计费系统** | Google Play Billing | V1 API |

### 1.3 项目结构

```
ResurrectionMoeyu/
├── app/                          # 主应用模块
│   ├── src/main/
│   │   ├── java/jp/co/a_tm/moeyu/
│   │   │   ├── MainActivity.java           # 应用入口
│   │   │   ├── api/                        # API 网络层 (16 文件)
│   │   │   ├── billing/                    # 计费模块 (9 文件)
│   │   │   ├── live2d/                     # Live2D 核心引擎 (9 文件)
│   │   │   ├── model/                      # 数据模型层
│   │   │   ├── util/                       # 工具类
│   │   │   └── [Activity/Fragment]         # UI 组件 (17 文件)
│   │   ├── assets/                         # Live2D 资源
│   │   │   ├── model/moeyu.1024/          # MOC 模型文件
│   │   │   ├── motion/                    # 动画配置
│   │   │   ├── voice/                     # 语音资源
│   │   │   └── voice.json                 # 语音映射表
│   │   ├── res/                            # Android 资源
│   │   └── libs/live2d_android.jar        # Live2D 依赖库
│   └── build.gradle                        # 模块构建配置
├── build/                              # 根构建配置
├── dev_tools/                          # 开发工具
└── gradle/                             # Gradle 配置
```

### 1.4 规模统计

| 指标 | 数量 |
|------|------|
| **Java 源文件** | 74 个 |
| **Activity 组件** | 13 个 |
| **Fragment 组件** | 4 个 |
| **API Task** | 5 个 |
| **Model 类** | 6 个 |
| **包路径** | jp.co.a_tm.moeyu |

---

## 二、核心模块分析

### 2.1 Live2D 引擎模块 (live2d/)

**职责**: Live2D 模型的加载、渲染、交互控制

#### 核心组件

```
LAppLive2DManager (控制器)
    ├── LAppGLView (视图容器)
    │   └── LAppRenderer (OpenGL 渲染器)
    ├── LAppModel (模型管理器)
    │   └── Live2DModelAndroid (第三方引擎)
    └── LAppAnimation (动画系统)
        ├── MotionQueueManager (主运动队列)
        ├── ExpressionMgr (表情队列)
        └── EyeBlinkMotion (眨眼控制)
```

#### 关键技术特性

1. **模型加载流程**
   - 从 Assets 读取 `moeyu.moc` 二进制模型文件
   - 加载 4 张纹理贴图 (`texture_00~03.png`)
   - 支持 27 个 Parts 的透明度映射

2. **动画系统**
   - **Idle 循环**: 4 组场景 × 随机 Idle 动作，带淡入淡出
   - **Touch 交互**: 
     - `touchesBegan`: 记录触摸起点、切换眨眼间隔 (1.5s→0.4s)
     - `touchesMoved`: 计算拖拽距离 → 触发 Head/Body Flip
     - `tapEvent`: 双击/单击事件回调
   - **物理模拟**: Spring-Damper 模型的 Face Drag（面部跟随）

3. **传感器集成**
   - 加速度传感器监听 (`AccelHelper`)
   - 一阶低通滤波平滑加速度值
   - 映射到 Live2D 参数：`PARAM_ANGLE_X/Y`, `PARAM_BASE_X/Y`
   - 震动检测：加速度差值 >1.5 → 触发 `shakeEvent()`

4. **OpenGL 渲染**
   - 正交投影矩阵设置
   - 背景纹理绘制（浴场场景）
   - Model 坐标变换：`(x=-80, y=-20, scale=0.13)`
   - 支持 AR 模式切换

---

### 2.2 API 网络层模块 (api/)

**职责**: 处理所有 HTTP 请求、用户数据管理、Gacha 抽卡逻辑

#### 技术架构

```java
// 异步任务框架
BaseTask<Params, Progress, Result> extends AsyncTask
    ├── LoginTask (登录)
    ├── SignupTask (注册)
    ├── GachaTask (抽卡)
    └── BillingTask (计费回调)
```

#### 核心功能

1. **网络请求实现**
   - 基于 `Apache HttpClient 3.x`
   - Base URL: `http://api.moeapk.com/third_party/moeyu/`
   - SHA-1 签名算法防止重放攻击

2. **用户数据管理** (`UserData.java`)
   ```java
   public class UserData implements Serializable {
       // 金币系统 (三阶)
       int bronzeCoin;      // 青铜币 (每日奖励)
       int goldCoin;        // 黄金币 (内购)
       int platinumCoin;    // 白金币 (高级)
       
       // 物品收集 (25 种)
       List<Integer> items;
       
       // 等级系统
       int level;           // 1-6 级
       int exp;             // 经验值
   }
   ```

3. **Gacha 抽卡系统**
   - **概率分层**:
     - BRONZE: rate=2 (10% 触发)
     - GOLD: rate=3 (50% 触发)
     - PLATINUM: rate=4 (90% 触发)
   - 物品上限保护：`MAX_COIN_COUNT = 99`
   - 未收集物品优先判定

4. **数据持久化**
   - `UserData.dat` 本地序列化存储
   - 支持 "local" userId 离线模式
   - 每日登录奖励自动发放

---

### 2.3 Billing 计费模块 (billing/)

**职责**: Google Play 内购集成、安全验证、购买流程管理

#### 完整购买流程

```
用户点击购买
    ↓
BillingService.requestPurchase()
    ↓
Google Play Store (PendingIntent)
    ↓
Payment Complete → BroadcastReceiver
    ↓
Security.verifyPurchase() (RSA 验证)
    ├─ RSA-2048 公钥验证
    └─ Base64 签名数据解析
    ↓
MoeyuAPIClient.userBilling() (服务器确认)
    ↓
UserData.update() + Live2D 奖励发放
```

#### 核心组件

1. **BillingService** - Service 层封装
   - 管理 `Google Play Billing Service` 连接
   - 请求队列管理 (`PendingRequestQueue`)
   - 购买状态观察者模式

2. **Security** - 安全验证核心
   ```java
   // RSA 公钥 (硬编码)
   generatePublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtvGL...");
   
   // 验证逻辑
   public VerifiedPurchase verifyPurchase(String signedData, String signature);
   ```

3. **ResponseHandler** - 响应处理
   - 处理 `PURCHASED/RESTORED/CANCELLED` 状态
   - UI 跳转和 Toast 提示

4. **PurchaseObserver** - 观察者模式
   - 购买状态变化回调
   - 与 API 层的 `BillingTask` 集成

---

### 2.4 UI 组件层

#### Activity 架构 (13 个)

| Activity | 功能描述 | 关键交互 |
|----------|---------|---------|
| **MainActivity** | 应用入口点 | 片头播放 → TitleActivity |
| **TitleActivity** | 标题/登录页 | 登录注册、UserData 保存 |
| **GatyaActivity** | 抽卡主界面 | 金币选择、转盘动画、Billing 集成 |
| **BathActivity** | 浴室 Live2D 交互 | Live2D 渲染、触摸事件、场景切换 |
| **CollectionRoomActivity** | 收藏房间管理 | 物品/语音/笔记分类 |
| **ItemCollectionActivity** | 物品收藏 | 25 种物品收集状态展示 |
| **VoiceCollectionActivity** | 语音收藏 | 语音列表管理 (`voice.json` 映射) |
| **GatyaResultActivity** | 抽卡结果展示 | GachaResult 详情、UserData 更新 |
| **PreferenceActivity** | 设置界面 | 相机开关、声音设置等 |

#### Fragment 架构 (4 个)

- `LoginFragment` - 登录交互
- `SignupFragment` - 注册流程
- `NetworkBaseFragment` - 网络请求基类
- `GachaFragment` - 抽卡操作集成

---

## 三、模块间依赖关系

### 3.1 整体架构视图

```
┌─────────────────────────────────────────┐
│           UI 层 (Activity/Fragment)        │
│  ┌─────────────┬──────────────┬────────┐ │
│  │ BathActivity│ GatyaActivity│ 其他   │ │
│  └─────────────┴──────────────┴────────┘ │
└───────────┬─────────────────┬────────────┘
            │                 │
            ↓                 ↓
    ┌───────────────┐   ┌───────────────┐
    │  Live2D 引擎层 │   │   API 网络层   │
    │  (live2d/)    │   │   (api/)      │
    └───────┬───────┘   └───────┬───────┘
            │                   │
            └─────────┬─────────┘
                      ↓
              ┌───────────────┐
              │  Billing 计费层 │
              │   (billing/)   │
              └───────┬─────────┘
                      ↓
              ┌───────────────┐
              │  Model 数据层   │
              │ (UserData等)   │
              └───────────────┘
```

### 3.2 关键交互点

#### Live2D ↔ API

```java
// BathActivity.java - Live2D 触摸事件触发语音
LAppLive2DManager.getTouchPosition()
    ↓
getRegion(pointF) → Scene 枚举判定
    ↓
startVoiceAndAnimation(voiceName)
    ├─ LAppAnimation.startTouchMotion()
    └─ VoiceManager.playSound()
```

#### API ↔ Billing

```java
// MoeyuAPIClient.java - 计费回调处理
public void userBilling(String signedData, String signature) {
    // RSA 验证后的服务器确认请求
    HttpPost post = new HttpPost(BASE_URL + "user/billing");
    // ...
}
```

#### Billing ↔ Live2D (通过 API)

```java
// 购买成功后触发 Live2D 奖励动画
BillingService.purchaseStateChanged(PURCHASED)
    ↓
MoeyuAPIClient.userBilling()
    ↓
UserData.updateItems()
    ↓
ItemTableController.update() → Live2D 模型更新
```

---

## 四、资源文件结构

### 4.1 Assets 目录

```
assets/
├── model/moeyu.1024/
│   ├── moeyu.moc              # Live2D 模型文件 (二进制)
│   └── texture_00~03.png      # 4 张纹理贴图
├── motion/
│   ├── idle/                  # Idle 动画目录
│   └── touch/                 # Touch 交互动画
├── voice/                     # 日语语音资源
├── voice_cn/                  # 中文语音资源
├── voice.json                # 语音映射表 (134KB)
└── water_*.png               # 浴场场景背景图 (4 张)
```

### 4.2 依赖库

- **live2d_android.jar** - Live2D Android SDK 核心库
  - 提供 `Live2DModelAndroid`、`MotionQueueManager`、`ExpressionMgr` 等类
  - OpenGL ES 渲染支持
  - MOC 模型加载和动画管理

---

## 五、关键技术特点

### 5.1 架构设计模式

| 模式 | 应用场景 | 实现类 |
|------|---------|--------|
| **MVC** | Live2D 引擎层 | Manager-Model-View |
| **Observer** | Billing 购买状态 | PurchaseObserver |
| **AsyncTask** | 网络请求处理 | BaseTask |
| **Singleton** | 管理器实例 | LAppLive2DManager |
| **Factory** | 模型创建 | FileManager |

### 5.2 数据流设计

```
用户操作 (UI) 
    ↓
异步任务 (AsyncTask)
    ↓
网络请求 (HttpClient) / 本地文件 (UserData.dat)
    ↓
数据处理 (Model/Service)
    ↓
状态更新 (LiveData/回调)
    ↓
UI 刷新 (Activity/Fragment)
```

### 5.3 安全机制

- **API 签名**: SHA-1 + Nonce + Timestamp
- **Billing 验证**: RSA-2048 公钥验证
- **本地缓存**: Object Serialization + 文件存储

---

## 六、待确认事项

| 项目 | 说明 | 优先级 |
|------|------|--------|
| **API 接口文档** | 需要确认完整的 API 端点和请求/响应格式 | 高 |
| **ItemTableController** | 需要了解物品更新如何触发 Live2D 模型变化 | 中 |
| **VoiceManager 实现** | 语音队列管理和播放逻辑需详细分析 | 中 |
| **Scene 状态机** | 场景切换的完整状态流转需要梳理 | 低 |
| **测试覆盖率** | 当前项目缺少单元测试和集成测试代码 | 高 |

---

## 七、下一步分析计划

根据 AGENT 系统标准流程，后续将进行：

1. **架构逆向分析** (@code-framework) - 深入分析核心组件设计模式
2. **功能清单梳理** (@requirement-analyst) - 明确所有功能点和业务流程
3. **代码质量评估** (@code-review-qa) - 识别技术债务和潜在风险
4. **构建部署分析** (@dev-ops) - 理解构建流程和依赖管理
5. **开发任务规划** (@product-manager) - 制定后续改进计划

---

**文档版本**: v1.0  
**最后更新**: 2026-03-27  
**分析进度**: 阶段一完成 (项目概览)
