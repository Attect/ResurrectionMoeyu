# ResurrectionMoeyu 项目架构文档

> 文档版本: 1.0
> 分析日期: 2026-04-10
> 分析者: code-framework (架构师AGENT)
> 任务ID: ARCH-001

---

## 变更日志

| 日期 | 变更人 | 变更内容 |
|------|--------|----------|
| 2026-04-10 | code-framework | 初版：基于源码逆向分析生成完整架构文档 |

---

## 1. 架构概述

### 1.1 整体架构风格

ResurrectionMoeyu 采用 **Activity驱动的单体架构（Monolithic Activity-Driven Architecture）**，没有使用MVP、MVVM或MVI等现代架构模式。所有业务逻辑直接内嵌于Activity类中，数据访问通过工具类和数据库帮助类实现。

### 1.2 架构特征总结

| 特征 | 描述 |
|------|------|
| 架构模式 | Activity驱动单体架构（无分层） |
| 导航模式 | 中央路由器模式（MainActivity作为路由中枢） |
| 数据持久化 | 混合持久化：Java序列化 + SQLite + SharedPreferences |
| 异步处理 | AsyncTask（已废弃API） |
| 网络层 | 已本地化，原HTTP客户端保留但主要逻辑在本地执行 |
| 渲染层 | OpenGL ES + Live2D SDK |
| 依赖注入 | 无DI框架，直接在Activity中new对象 |

### 1.3 架构分层（实际状况）

```
┌─────────────────────────────────────────────┐
│            Activity层（UI + 业务逻辑）        │
│  BaseActivity → MainActivity / BathActivity  │
│  / TitleActivity / GatyaActivity / ...       │
├─────────────────────────────────────────────┤
│          API Fragment层（异步任务协调）        │
│  NetworkBaseFragment → LoginFragment /       │
│  GachaFragment / SignupFragment / ...        │
├─────────────────────────────────────────────┤
│         AsyncTask层（后台执行）               │
│  BaseTask → LoginTask / GachaTask /          │
│  SignupTask / BillingTask                    │
├─────────────────────────────────────────────┤
│        服务/数据层（数据访问与业务计算）        │
│  MoeyuAPIClient / VoiceManager /             │
│  DatabaseOpenHelper / UserDataManager         │
├─────────────────────────────────────────────┤
│          Live2D渲染引擎层                     │
│  LAppLive2DManager → LAppModel →             │
│  LAppAnimation → LAppRenderer → LAppGLView   │
└─────────────────────────────────────────────┘
```

> **说明**：上述分层是基于代码实际职责的描述，项目本身并未显式地进行分层设计。Activity直接包含了大量业务逻辑。

---

## 2. 模块架构图

### 2.1 模块依赖关系

```
┌────────────────────────────────────────────────────────────┐
│                        Activity 模块                        │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌──────────┐  │
│  │ Main     │→│ Title     │→│ Bath     │  │ Gatya    │  │
│  │ Activity │  │ Activity  │  │ Activity │  │ Activity │  │
│  └────┬─────┘  └─────┬─────┘  └────┬─────┘  └────┬─────┘  │
│       │              │              │              │         │
│       │  (中央路由器，所有Activity通过   │              │         │
│       │   startActivityForResult启动) │              │         │
└───────┼──────────────┼──────────────┼──────────────┼─────────┘
        │              │              │              │
        v              v              v              v
┌────────────────────────────────────────────────────────────┐
│                      API 客户端模块                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ MoeyuAPI     │  │ Fragment层   │  │ AsyncTask层      │  │
│  │ Client       │←─│ LoginFragment│←─│ LoginTask        │  │
│  │ (本地化)     │  │ GachaFragment│  │ GachaTask        │  │
│  └──────┬───────┘  └──────────────┘  └──────────────────┘  │
│         │                                                    │
└─────────┼──────────────────────────────────────────────────┘
          │
          v
┌────────────────────────────────────────────────────────────┐
│                      数据持久层                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ UserData     │  │ SQLite       │  │ SharedPrefs      │  │
│  │ (Java序列化) │  │ (collection  │  │ (Preferences     │  │
│  │ .dat文件     │  │  .db)        │  │  Helper)         │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                     Live2D 渲染模块                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ LAppLive2D   │→│ LAppModel    │→│ Live2D SDK       │  │
│  │ Manager      │  │ (模型加载)   │  │ (live2d_android  │  │
│  │ (生命周期)   │  │              │  │  .jar)           │  │
│  └──────┬───────┘  └──────────────┘  └──────────────────┘  │
│         │                                                   │
│         v                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ LAppAnimation│  │ LAppRenderer │  │ LAppGLView       │  │
│  │ (动画控制)   │  │ (OpenGL渲染) │  │ (GLSurfaceView)  │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                      辅助模块                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ VoiceManager │  │ Decryption   │  │ SecurityUtils    │  │
│  │ (语音选择)   │  │ (XOR解密)    │  │ (SHA签名)        │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Event        │  │ LovePoint    │  │ CoinController   │  │
│  │ Controller   │  │ (好感度计算) │  │ (货币管理)        │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

### 2.2 模块职责表

| 模块 | 包路径 | 核心类 | 职责 |
|------|--------|--------|------|
| Activity模块 | `jp.co.a_tm.moeyu` (根包) | BaseActivity, MainActivity, BathActivity 等 | 页面展示、用户交互、业务逻辑编排 |
| API客户端模块 | `jp.co.a_tm.moeyu.api` | MoeyuAPIClient | 用户数据管理、扭蛋逻辑（已本地化） |
| API异步任务 | `jp.co.a_tm.moeyu.api.task` | BaseTask, LoginTask, GachaTask 等 | AsyncTask封装，后台执行API调用 |
| API Fragment | `jp.co.a_tm.moeyu.api.fragment` | NetworkBaseFragment, LoginFragment 等 | UI片段，协调异步任务与Activity |
| API回调 | `jp.co.a_tm.moeyu.api.listener` | MoeyuAPITaskListener, GachaResultListener 等 | 异步任务结果回调接口 |
| API模型 | `jp.co.a_tm.moeyu.api.model` | GachaResult | 扭蛋结果数据模型 |
| Live2D模块 | `jp.co.a_tm.moeyu.live2d` | LAppLive2DManager, LAppModel, LAppAnimation 等 | Live2D角色模型加载、动画、渲染 |
| 数据模型 | `jp.co.a_tm.moeyu.model` | UserData, EventData | 核心数据实体 |
| 数据库 | `jp.co.a_tm.moeyu` (根包) | DatabaseOpenHelper, DatabaseTableController | SQLite数据库管理 |
| 安全 | `jp.co.a_tm.moeyu.security` | SecurityUtils | SHA-1/SHA-256签名工具 |
| 支付 | `jp.co.a_tm.moeyu.billing` | BillingService, PurchaseObserver | Google Play Billing v2 |
| 工具 | `jp.co.a_tm.moeyu.util` | Config, Logger, UserDataManager | 通用工具类 |

---

## 3. 核心模块说明

### 3.1 Activity导航模块

#### BaseActivity（基类）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/BaseActivity.java`
- **职责**: 所有Activity的抽象基类，提供：
  - 全屏UI控制（隐藏状态栏）
  - 统一的页面跳转方法（通过 `setResult` + `EXTRA_NEXT_ACTIVITY` 模式）
  - 资源释放钩子（`release()`）
- **核心常量**: 定义了12个页面跳转码（NEXT_ACTIVITY_*）

#### MainActivity（路由中枢）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/MainActivity.java`
- **职责**: 
  - 应用入口，Launcher Activity
  - 播放开场视频
  - **中央路由器**: 所有子Activity通过 `startActivityForResult` 启动，返回时由 `onActivityResult` 中的 `switch` 语句分发到下一个Activity
  - 屏幕比例修正值计算（`FIX_HEIGHT`）
- **关键逻辑**: `onActivityResult` 方法（第116-172行）是整个应用的路由核心

#### BathActivity（核心交互页面）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/BathActivity.java`
- **职责**: 浴室互动场景，应用核心功能页面
- **核心功能**:
  - Live2D模型展示与触摸交互
  - 场景切换（bath_a → head → body → bath_b 循环）
  - 触摸区域识别（face/head/brest/belly/arm/none）
  - 语音播放与动画联动
  - 物品选择与使用
  - 事件处理（升级事件、物品收集完成事件）
  - AR模式（相机预览）
- **代码量**: 1080行，是项目中最大的类，承载了过多职责

#### TitleActivity（标题页）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/TitleActivity.java`
- **职责**: 登录/注册、数据初始化（首次运行时解密语音文件）、页面导航入口
- **关键流程**: 首次运行 → `InitializeDataTask` → `Decryption.execute()` → 登录/注册

#### GatyaActivity（扭蛋页）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/GatyaActivity.java`
- **职责**: 扭蛋抽卡交互、金币管理、Google Play支付集成
- **交互模式**: 转盘旋转手势识别（自定义触摸角度计算）

### 3.2 Live2D渲染模块

#### 模块架构

```
BathActivity
    │
    ├── 创建 LAppLive2DManager (管理器)
    │       ├── FileManager (资源文件管理)
    │       ├── AccelHelper (加速度传感器)
    │       └── FinishListener (模型加载完成回调)
    │
    ├── 创建 LAppGLView (GLSurfaceView)
    │       └── LAppRenderer (OpenGL渲染器)
    │               ├── 场景状态 (Scene枚举)
    │               ├── 纹理管理 (墙壁/水面/前景)
    │               └── 加速度数据处理
    │
    └── 加载 LAppModel (Live2D模型)
            ├── Live2DModelAndroid (SDK核心)
            ├── LAppAnimation (动画管理)
            │       ├── 闲置动画 (4组，按场景)
            │       ├── 触摸动画 (动态加载)
            │       ├── 表情系统 (expression.json)
            │       └── 眨眼动画
            └── LAppExpressionMotion (表情动画)
```

#### LAppLive2DManager

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/live2d/LAppLive2DManager.java`
- **职责**: Live2D模块的总管理器
- **核心方法**:
  - `createView()`: 创建GLSurfaceView和渲染器
  - `setupModel()` / `setupModel_later()`: 初始化Live2D模型（异步）
  - `startAnimation()` / `stopAnimation()`: 控制动画循环
  - `releaseModel()` / `releaseView()`: 资源释放

#### LAppModel

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/live2d/model/LAppModel.java`
- **职责**: Live2D模型封装
- **模型资源**: 
  - 模型文件: `assets/live2d/model/moeyu.moc`
  - 纹理贴图: `assets/live2d/model/moeyu.1024/texture_00~03.png`
- **硬编码参数**: 模型名称 "moeyu" 和纹理路径直接写在代码中

#### LAppAnimation

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/live2d/motion/LAppAnimation.java`
- **职责**: 动画控制系统
- **动画分类**:
  - **闲置动画**: 按场景分组，bath_a有3个、head有2个、body有1个、bath_b有3个
  - **触摸动画**: 从 `assets/live2d/motion/touch/` 目录动态加载所有 `.mtn` 文件
  - **表情系统**: 从 `expression.json` 加载
  - **眨眼动画**: `EyeBlinkMotion`，正常间隔4000ms，触摸时间隔1500ms
- **场景切换**: `setScene()` 方法切换动画组和渲染背景

#### LAppRenderer

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/live2d/view/LAppRenderer.java`
- **职责**: OpenGL ES渲染器
- **渲染层次**（从底到顶）:
  1. 墙壁背景纹理（bath_a/bath_b 用 `water_wall00.png`，head/body 用 `water_wall01.png`）
  2. 水面后层纹理（仅 bath 场景，`water_back00.png`）
  3. Live2D角色模型
  4. 水面前层纹理（仅 bath 场景，`water_front00.png`）
- **AR模式**: 启用时跳过背景渲染，由 CameraPreview 提供背景

### 3.3 API客户端模块（已本地化）

#### MoeyuAPIClient

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/api/MoeyuAPIClient.java`
- **职责**: 原本为远程API客户端，已改为本地实现
- **本地化特征**:
  - `userSignUp()`: 直接返回本地 UserData
  - `userData()`: 直接返回本地 UserData
  - `userGatya()`: 本地实现扭蛋逻辑（随机数 + 概率计算）
  - `userBilling()`: 仍然保留远程HTTP调用（未本地化）
- **数据持久化**: 使用 `localUserData.dat`（Java序列化），位于缓存目录
- **扭蛋逻辑**:
  - 铜币：rate=2（20%概率获得新物品）
  - 金币：rate=3（30%概率获得新物品）
  - 白金币：rate=4（40%概率获得新物品）
  - 无物品时100%获得新物品

#### API调用链（以扭蛋为例）

```
GatyaActivity.executeGachaTask()
    → GachaFragment.gacha()
        → GachaTask.doInBackground() [AsyncTask]
            → MoeyuAPIClient.userGatya() [本地计算]
                → GachaResult (包含新UserData和itemId)
        → GachaTask.onPostExecute()
            → BaseTask.storeUserData() [保存用户数据到文件+数据库]
            → MoeyuAPITaskListener.onSuccess()
                → GachaFragment回调
                    → GatyaActivity跳转到GatyaResultActivity
```

### 3.4 语音系统模块

#### VoiceManager

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/VoiceManager.java`
- **职责**: 根据场景、区域、物品、等级选择语音
- **语音选择算法**:
  1. 从 `voice.json` 中查询: `scene → region → item → level`
  2. 获取语音列表，每条有概率权重（probability）
  3. 生成1-100的随机数，累减概率直到命中
- **语音文件获取**:
  - 优先尝试中文语音: `voiceName_cn.ogg`（应用私有目录）
  - 回退到日文语音: `voiceName.ogg`（应用私有目录）
  - 原始加密文件: `assets/voice/*.okk` 和 `assets/voice_cn/*.okk`

#### Decryption（语音解密）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/Decryption.java`
- **职责**: 将加密的 `.okk` 文件解密为 `.ogg` 文件
- **加密算法**: XOR 58（每个字节与58异或）
- **执行时机**: 首次运行时由 `TitleActivity.InitializeDataTask` 调用
- **优化**: 已解密的文件不会重复处理

#### SpecialEvent（特殊事件语音链）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/SpecialEvent.java`
- **职责**: 特定物品+场景+区域的组合触发链式语音事件
- **硬编码规则**:
  - 物品20 + bath_b + belly → 语音 227,228,229,230
  - 物品1 + head + head → 语音 143,222,223,201
  - 物品13 + bath_a + face → 语音 157,186,187,188
  - 物品16 + bath_b + face → 语音 206,207,208

### 3.5 数据持久层

#### 三层持久化架构

| 存储方式 | 文件/位置 | 内容 | 管理类 |
|----------|-----------|------|--------|
| Java序列化 | `cacheDir/localUserData.dat` (MoeyuAPIClient) | 用户数据(金币/等级/物品/经验) | MoeyuAPIClient |
| Java序列化 | `fileDir/userData.dat` (UserDataManager) | 用户数据(同上，另一份拷贝) | UserDataManager |
| SQLite | `collection.db` | 物品/语音/笔记收藏状态 | DatabaseOpenHelper + TableController |
| SharedPreferences | 系统默认 | 首次运行标记/相机设置等 | PreferencesHelper |

#### UserData（用户数据模型）

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/model/UserData.java`
- **字段**:
  - `userId`: 用户标识（本地模式为 "local"）
  - `bronzeCoin / goldCoin / platinumCoin`: 三种货币
  - `items`: 已获取物品ID列表（List\<Integer\>，最多25个）
  - `exp / level`: 经验值和等级（最高6级）
  - `bonus`: 每日登录奖励标记
  - `state`: 用户状态
  - `lastLoginTime`: 上次登录时间戳
- **序列化**: 实现 `Serializable` 接口，通过 `ObjectOutputStream`/`ObjectInputStream` 持久化

> **问题**: UserData存在两份序列化存储（MoeyuAPIClient的 `localUserData.dat` 和 UserDataManager的 `userData.dat`），可能导致数据不一致。

#### DatabaseOpenHelper

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/DatabaseOpenHelper.java`
- **数据库**: `collection.db`（版本1，无升级逻辑）
- **三张表**:

| 表名 | 字段 | 初始行数 | 初始化数据来源 |
|------|------|----------|----------------|
| ItemTable | _id, name, opened | 25行 | 硬编码01-25 |
| VoiceTable | _id, name, opened, title | 动态 | CSV文件(R.raw.voice) |
| NoteTable | _id, name, opened, term | 动态 | CSV文件(R.raw.note) |

#### DatabaseTableController体系

```
DatabaseTableController (抽象基类)
    ├── ItemTableController (物品收藏)
    ├── VoiceTableController (语音收藏)
    └── NoteTableController (笔记收藏)
```

- **源文件**: `DatabaseTableController.java`
- **设计模式**: 简化的Template Method模式，子类通过设置 `TABLE_NAME` 区分表
- **功能**: 统一的CRUD操作（open/update/isOpened/count）

### 3.6 事件系统

#### EventController

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/EventController.java`
- **职责**: 检测并管理游戏事件
- **事件类型**:
  - Level2~Level6: 等级升级事件
  - Complete: 物品全收集完成事件
- **检测逻辑**: 检查用户等级和对应语音是否已解锁，未解锁则生成事件
- **FIFO队列**: 使用ArrayList模拟事件队列（push/pop）

#### EventData

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/model/EventData.java`
- **内容**: 事件类型 + 语音列表（从strings.xml资源数组加载）

### 3.7 好感度系统

#### LovePoint

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/LovePoint.java`
- **等级阈值**: [0, 2, 14, 44, 100, 188]
- **等级**: 1~6级
- **硬币对应的经验值**: 铜币=1, 金币=3, 白金币=30

### 3.8 安全模块

#### SecurityUtils

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/security/SecurityUtils.java`
- **功能**: SHA-1和SHA-256签名生成与验证
- **用途**: 原API请求签名验证（本地化后仅签名方法保留，无实际验证）

#### Decryption

- **源文件**: `app/src/main/java/jp/co/a_tm/moeyu/Decryption.java`
- **算法**: XOR 58 对称加密
- **用途**: 语音文件(.okk → .ogg)解密
- **安全性**: 极低，简单的单字节XOR

---

## 4. 数据流说明

### 4.1 应用启动数据流

```
App启动
  │
  v
MainActivity.onCreate()
  ├── 计算屏幕修正值 (FIX_HEIGHT)
  └── 设置Logger配置
  │
  v (onResume, 首次运行)
MainActivity.playOpeningMovie()
  │ 播放开场视频
  v
MainActivity.startTitleActivity()
  │ startActivityForResult(TitleActivity, REQUEST_CODE_NAV)
  v
TitleActivity.onResume()
  ├── [首次运行] MoeyuApplication.isFirstRun() == true
  │     ├── [需要初始化] InitializeDataTask
  │     │     └── Decryption.execute() ← 解密所有语音文件
  │     │           ├── assets/voice/*.okk → appFiles/*.ogg
  │     │           └── assets/voice_cn/*.okk → appFiles/*_cn.ogg
  │     └── login()
  │           ├── [有用户数据] LoginFragment.login()
  │           │     └── LoginTask → MoeyuAPIClient.userData() → 返回本地UserData
  │           └── [无用户数据] SignupFragment.signup()
  │                 └── SignupTask → MoeyuAPIClient.userSignUp() → 创建本地UserData
  │
  └── [非首次运行] 直接加载用户数据
```

### 4.2 浴室互动数据流（核心业务流）

```
BathActivity.onCreate()
  ├── 初始化VoiceManager (加载voice.json)
  ├── 初始化LAppLive2DManager (加载Live2D模型)
  ├── 加载UserData (UserDataManager.loadUserData())
  ├── 创建物品列表 (根据UserData.isItemGet判断灰度)
  └── 恢复场景状态 (从Intent读取scene和item)
  │
  v (用户触摸屏幕)
BathActivity.onTouch()
  ├── getRegion() ← 根据触摸坐标判断区域(Region枚举)
  ├── [有链式事件] SpecialEvent.get() ← 检查特殊事件
  │     └── 返回链式语音列表
  ├── [无链式事件] VoiceManager.getVoiceName()
  │     ├── 查询 voice.json: scene → region → item → level
  │     └── 概率加权随机选择语音名
  │
  v
startVoiceAndAnimation(voiceName)
  ├── VoiceTableController.isOpened() ← 检查语音是否已解锁
  ├── [未解锁] VoiceTableController.update() ← 标记为已解锁
  │           └── updateVoiceNum() ← 更新界面上的语音计数
  ├── LAppAnimation.startTouchMotion(voiceName + sceneSuffix)
  │     └── 从motionTouchMap中查找并播放对应.mtn动画
  └── VoiceManager.getVoiceFileDescripter(voiceName)
        ├── [优先] voiceName_cn.ogg (中文语音)
        └── [回退] voiceName.ogg (日文语音)
        └── MediaPlayer播放
```

### 4.3 扭蛋数据流

```
GatyaActivity.onCreate()
  ├── 加载UserData → 获取userId
  └── executeLogin() → LoginFragment → LoginTask
        └── MoeyuAPIClient.userData() → 获取最新本地数据
              └── 每日登录奖励检查（bronzeCoin +1）
  │
  v (用户旋转转盘满360度)
GatyaActivity.executeGachaTask()
  → GachaFragment.gacha()
    → GachaTask.doInBackground()
      → MoeyuAPIClient.userGatya(userId, coin)
        ├── 收集未拥有物品列表(noHolds)
        ├── 根据coin类型确定rate(2/3/4)
        ├── 扣除对应金币
        ├── 随机决定是否获得新物品:
        │     ├── rate > random(0-9) → 从noHolds中随机选一个
        │     └── 否则 → 从已拥有items中随机选一个
        ├── UserData.addItem(itemId)
        ├── saveUserData() → 序列化到localUserData.dat
        └── 返回 GachaResult(userData, itemId)
    → GachaTask.onPostExecute()
      ├── BaseTask.storeUserData() 
      │     ├── UserDataManager.saveUserData() → userData.dat
      │     └── ItemTableController.update() → SQLite ItemTable
      └── listener.onSuccess(result)
        → GatyaActivity跳转到GatyaResultActivity
```

### 4.4 Live2D渲染数据流

```
[每帧渲染循环]
LAppRenderer.onDrawFrame(GL10)
  ├── updateAccel() ← 更新加速度传感器数据
  ├── [非AR模式] 绘制背景层:
  │     ├── 墙壁纹理 (根据Scene选择)
  │     └── [bath场景] 水面后层
  ├── 绘制Live2D模型:
  │     ├── LAppLive2DManager.getModel(gl)
  │     │     └── [首次] setupModel_later() → 加载.moc和纹理
  │     ├── LAppModel.setAccelarationValue(accel)
  │     └── LAppModel.drawModel(gl)
  │           └── LAppAnimation.updateParam(model)
  │                 ├── [动画完成] startMainMotion() → 播放闲置动画
  │                 ├── [动画进行中] mainMotionMgr.updateParam()
  │                 ├── expressionMgr.updateParam() → 表情
  │                 ├── updateDragMotion() → 追踪触摸/加速度
  │                 ├── 呼吸动画 (sin波)
  │                 └── 身体/头部微动 (sin波)
  └── [非AR模式] 绘制前景层:
        └── [bath场景] 水面前层
```

### 4.5 场景自动切换数据流

```
BathActivity.onCreate()
  └── startWhitein()
        └── [淡入动画结束]
              └── changeSceneAuto()
                    └── Timer定时器
                          └── [超时] Scene.next()
                                │ bath_a → head → body → bath_b → null
                                └── [next非null] changeScene(nextScene)
                                      ├── startWhiteOutAndIn() → 淡出淡入动画
                                      │     └── LAppRenderer.setScene(scene)
                                      │           └── LAppAnimation.setScene(scene)
                                      │                 └── startMainMotion() → 播放对应场景闲置动画
                                      └── 切换BGM
```

---

## 5. Activity导航架构

### 5.1 中央路由器模式

项目采用 **Central Router Pattern（中央路由器模式）**，MainActivity充当路由中枢：

```
                    ┌──────────────┐
                    │  MainActivity │ (中央路由器)
                    │  (永不销毁)   │
                    └──────┬───────┘
                           │
         onActivityResult()│ switch(nextPageType)
                           │
    ┌──────────┬───────────┼───────────┬──────────┐
    v          v           v           v          v
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│ Title  │ │ Bath   │ │ Gatya  │ │ Prefer │ │Collect │
│Activty │ │Activity│ │Activity│ │Activity│ │RoomAct │
└───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
    │          │          │          │          │
    │ setResult(EXTRA_NEXT_ACTIVITY, code)       │
    │ finish()                                   │
    └──────────┴──────────┴──────────┴──────────┘
```

### 5.2 导航码映射表

| 导航码 | 常量名 | 目标Activity | 触发场景 |
|--------|--------|-------------|----------|
| -1 | NEXT_EXIT | 退出应用 | 用户选择退出 |
| 0 | NEXT_ACTIVITY_TITLE | TitleActivity | 返回标题页 |
| 1 | NEXT_ACTIVITY_GACHA | GatyaActivity | 进入扭蛋 |
| 2 | NEXT_ACTIVITY_BATH | BathActivity | 进入浴室 |
| 3 | NEXT_ACTIVITY_PREFERENCE | PreferenceActivity | 进入设置 |
| 4 | NEXT_ACTIVITY_PREFERENCE_FROM_BATH | PreferenceActivity | 从浴室进入设置 |
| 5 | NEXT_ACTIVITY_GACHA_RESULT | GatyaResultActivity | 扭蛋结果 |
| 6 | NEXT_ACTIVITY_COLLECTION | CollectionRoomActivity | 收藏房间 |
| 7 | NEXT_ACTIVITY_ITEM_COLLECTION | ItemCollectionActivity | 物品收藏 |
| 8 | NEXT_ACTIVITY_VOICE_COLLECTION | VoiceCollectionActivity | 语音收藏 |
| 9 | NEXT_ACTIVITY_ROOM | MomorisRoomActivity | 桃璃房间 |
| 10 | NEXT_ACTIVITY_DIARY | NoteCollectionActivity | 笔记收藏 |

### 5.3 导航数据传递

页面间数据通过Intent Extra传递：

| 数据 | Key（字符串资源） | 类型 | 使用场景 |
|------|-------------------|------|----------|
| 下一个页面 | `extra_next_activity` | int | 所有页面（路由码） |
| 场景 | `intent_scene` (R.string.intent_scene) | String | → BathActivity |
| 物品ID | `intent_item` (R.string.intent_item) | int | → BathActivity |
| 事件数据 | `intent_event` (R.string.intent_event) | EventData(Serializable) | → BathActivity |
| 抽卡前用户数据 | `EXTRA_PRE_USER_DATA` | UserData(Serializable) | → GatyaResultActivity |
| 抽卡结果 | `EXTRA_GACHA_RESULT` | GachaResult(Serializable) | → GatyaResultActivity |

### 5.4 请求码

| 请求码 | 常量名 | 用途 |
|--------|--------|------|
| 1 | REQUEST_CODE_NAV | 常规导航请求 |
| 2 | REQUEST_CODE_PREFERENCE | 从浴室进入设置后的返回处理 |

### 5.5 设置页面返回的特殊处理

从浴室进入设置(码=4)后，设置页面返回时不经过路由分发，直接重新启动BathActivity（无数据），这确保了从设置返回后回到浴室场景。

---

## 6. 设计模式识别

### 6.1 已识别的设计模式

| 模式 | 应用位置 | 说明 |
|------|----------|------|
| **中央路由器模式** | MainActivity + BaseActivity | 所有页面跳转通过MainActivity中转，子页面通过setResult+导航码告知下一步 |
| **模板方法模式** | BaseActivity → 各子Activity | 基类定义导航方法(toBath/toGacha等)，子类直接调用 |
| **观察者模式** | MoeyuAPITaskListener体系 | 异步任务通过回调接口通知调用方（onSuccess/onError/onCancel） |
| **单例模式** | Config.getInstance() | 应用配置类的懒加载单例 |
| **命令模式** | BaseTask → LoginTask/GachaTask等 | 将API调用封装为AsyncTask命令对象 |
| **策略模式(简化)** | DatabaseTableController子类 | 通过TABLE_NAME字段区分不同表的访问策略 |
| **门面模式** | LAppLive2DManager | 封装Live2D SDK的复杂初始化和渲染流程 |
| **工厂方法** | UserData.createLocal() / UserData.fromJson() | 静态工厂方法创建UserData实例 |
| **队列模式** | EventController (push/pop) | FIFO事件队列 |
| **链式处理** | BathActivity.mChainEvent (LinkedList) | 特殊事件的链式语音播放 |

### 6.2 反模式识别

| 反模式 | 位置 | 说明 |
|--------|------|------|
| **God Activity** | BathActivity (1080行) | 单个Activity承担了UI、交互逻辑、语音管理、动画协调等多重职责 |
| **硬编码魔法值** | SpecialEvent, BathActivity | 物品ID、语音名、区域坐标等直接写在代码中 |
| **重复数据存储** | UserData双重序列化 | localUserData.dat和userData.dat存储相同的用户数据 |
| **静态变量共享** | MainActivity.FIX_HEIGHT, MoeyuAPIClient.userData | 通过静态变量在Activity间共享数据 |
| **内联匿名类** | BathActivity | 大量匿名OnCompletionListener/AnimationListener，降低可读性 |
| **AsyncTask滥用** | api.task包 | 使用已废弃的AsyncTask进行异步操作 |

---

## 7. 架构优缺点评估

### 7.1 优点

| 优点 | 说明 | 来源 |
|------|------|------|
| **导航机制清晰** | 中央路由器模式使页面跳转逻辑集中在一处（MainActivity.onActivityResult），便于理解整体流程 | BaseActivity.java, MainActivity.java |
| **Live2D模块封装良好** | LAppLive2DManager作为门面类，有效封装了Live2D SDK的复杂性 | LAppLive2DManager.java |
| **本地化改造完整** | API客户端已完全本地化，应用可离线运行，无需服务端 | MoeyuAPIClient.java |
| **语音系统设计灵活** | 基于JSON配置的概率权重语音选择机制，支持多语言（日文/中文） | VoiceManager.java, voice.json |
| **数据初始化策略合理** | 首次运行时异步解密语音文件，并缓存到本地避免重复解密 | Decryption.java, TitleActivity.java |
| **事件系统解耦** | EventController独立检测游戏事件，通过EventData传递，不耦合具体Activity | EventController.java |

### 7.2 缺点

| 缺点 | 严重程度 | 说明 | 来源 |
|------|----------|------|------|
| **无架构分层** | 高 | 业务逻辑直接写在Activity中，无Presenter/ViewModel层，BathActivity达1080行 | BathActivity.java |
| **数据一致性风险** | 高 | UserData同时存储在两个文件中（localUserData.dat和userData.dat），可能数据不一致 | MoeyuAPIClient.java, UserDataManager.java |
| **静态变量状态共享** | 中 | FIX_HEIGHT和userData等通过静态变量共享，可能导致内存泄漏和状态不一致 | MainActivity.java:41, MoeyuAPIClient.java:60-61 |
| **已废弃API使用** | 中 | AsyncTask、Apache HttpClient、Camera API、Billing v2均已废弃 | api/task/, api/MoeyuAPIClient.java, billing/ |
| **硬编码配置** | 中 | 模型路径、触摸区域坐标、特殊事件规则、加密密钥等硬编码在源码中 | LAppModel.java:30, BathActivity.java:178-191, SpecialEvent.java |
| **无依赖注入** | 低 | 所有依赖直接new创建，不利于测试和替换 | 所有Activity |
| **无测试覆盖** | 高 | test目录基本为空（仅SecurityUtils有一个测试），无单元测试和集成测试 | app/src/test/ |
| **线程安全隐患** | 中 | MoeyuAPIClient.userData为静态变量，在AsyncTask中读写无同步保护 | MoeyuAPIClient.java:61 |
| **资源管理问题** | 中 | MediaPlayer在BathActivity中手动管理生命周期，容易泄漏 | BathActivity.java |
| **无ProGuard混淆** | 低 | 逆向重建项目，不需要混淆 | proguard-rules.pro |

---

## 8. 改进建议

### 8.1 架构层面

| 优先级 | 建议 | 说明 |
|--------|------|------|
| P0 | **统一UserData存储** | 合并localUserData.dat和userData.dat为单一数据源，消除数据不一致风险 |
| P0 | **引入MVVM架构** | 将BathActivity等巨型Activity拆分为ViewModel+Repository，分离UI逻辑和业务逻辑 |
| P1 | **替换AsyncTask** | 使用Kotlin协程或RxJava替代已废弃的AsyncTask |
| P1 | **引入依赖注入** | 使用Hilt/Dagger管理依赖，便于测试和解耦 |
| P2 | **替换Billing库** | 从Google Play Billing v2升级到BillingClient |

### 8.2 代码层面

| 优先级 | 建议 | 说明 |
|--------|------|------|
| P0 | **添加单元测试** | 至少覆盖VoiceManager语音选择、MoeyuAPIClient扭蛋逻辑、LovePoint等级计算 |
| P1 | **外部化硬编码配置** | 将触摸区域坐标、特殊事件规则、模型路径等提取到JSON配置文件 |
| P1 | **消除静态变量** | 将FIX_HEIGHT和userData改为通过Intent或ViewModel传递 |
| P2 | **资源管理自动化** | 使用Lifecycle感知组件管理MediaPlayer等资源 |

### 8.3 安全层面

| 优先级 | 建议 | 说明 |
|--------|------|------|
| P1 | **升级加密算法** | XOR 58安全性极低，建议使用AES等标准加密算法 |
| P2 | **移除硬编码密钥** | 将API密钥和签名密钥移至NDK层或使用Android Keystore |

### 8.4 扩展点识别

| 扩展点 | 位置 | 说明 |
|--------|------|------|
| 新场景 | Scene枚举 | 添加新枚举值，需同步更新LAppAnimation的闲置动画组和LAppRenderer的渲染逻辑 |
| 新触摸区域 | Region枚举 + BathActivity.createCommonRegionMap() | 添加新区域枚举和坐标范围 |
| 新物品 | UserData.MAX_ITEM_COUNT | 修改最大物品数，需同步更新DatabaseOpenHelper的initItemRows |
| 新事件类型 | EventData.Type枚举 + EventController | 添加新事件类型和检测逻辑 |
| 新语音 | voice.json + assets/voice/ | 在JSON中添加语音映射，在assets中添加加密语音文件 |
| 新Live2D模型 | LAppModel.setupModel() | 当前模型路径硬编码，需要参数化 |

---

## 附录A: 核心类签名索引

### Activity类

```
abstract class BaseActivity extends AppCompatActivity
  ├── static EXTRA_NEXT_ACTIVITY: String
  ├── static NEXT_ACTIVITY_*: int (12个导航码)
  ├── onCreate(Bundle): void
  ├── release(): void
  ├── exit(): void
  ├── toTitle(): void
  ├── toGacha(): void
  ├── toBath(): void / toBath(String, int): void / toBath(EventData): void
  ├── toPreference(): void
  ├── toPreferenceFromBath(): void
  ├── toCollection(): void
  ├── toItemCollection(): void
  ├── toVoiceCollection(): void
  ├── toRoom(): void
  └── toDiary(): void

class MainActivity extends BaseActivity
  ├── static FIX_HEIGHT: int
  ├── onActivityResult(int, int, Intent): void
  └── [12个start*Activity()私有方法]

class BathActivity extends BaseActivity implements OnTouchListener
  ├── mLive2dManager: LAppLive2DManager
  ├── mVoiceManager: VoiceManager
  ├── mUserData: UserData
  ├── mScene: Scene
  ├── mRenderer: LAppRenderer
  ├── onTouch(View, MotionEvent): boolean
  ├── startVoiceAndAnimation(String): void
  └── changeScene(Scene): void
```

### 数据模型类

```
class UserData implements Serializable
  ├── userId: String
  ├── bronzeCoin / goldCoin / platinumCoin: int
  ├── items: List<Integer>
  ├── exp / level: int
  ├── bonus: boolean
  ├── lastLoginTime: long
  ├── static createLocal(): UserData
  ├── static fromJson(JSONObject): UserData
  ├── static restore(InputStream): UserData
  └── store(OutputStream): boolean

class EventData implements Serializable
  ├── mType: Type (枚举: Level2~6, Complete)
  └── mVoiceList: ArrayList<String>

class GachaResult implements Serializable
  ├── itemId: int
  └── userData: UserData
```

### 枚举类

```
enum Scene
  ├── bath_a(0), head(1), body(2), bath_b(3)
  ├── number: int
  └── next(): Scene

enum Region
  └── head, face, belly, arm, brest, none
```

### Live2D核心类

```
class LAppLive2DManager implements LAppDefine
  ├── myModel: LAppModel
  ├── glView: LAppGLView
  ├── fileManager: FileManager
  ├── accelHelper: AccelHelper
  ├── interface FinishListener { onFinishSetupModel(): void }
  ├── createView(Activity, Rect): LAppGLView
  ├── setupModel(): boolean
  ├── getAnimation(): LAppAnimation
  └── releaseModel(): void

class LAppModel
  ├── live2DModel: Live2DModelAndroid
  ├── live2dAnimation: LAppAnimation
  ├── setupModel(LAppLive2DManager, GL10): void
  ├── drawModel(GL10): void
  └── getAnimation(): LAppAnimation

class LAppAnimation
  ├── mainMotionMgr: MotionQueueManager
  ├── expressionMgr: MotionQueueManager
  ├── motionIdle: List<Live2DMotion[]> (按场景分组)
  ├── motionTouchMap: Map<String, Live2DMotion>
  ├── setScene(Scene): void
  ├── startTouchMotion(String): void
  └── updateParam(ALive2DModel): void
```

### API客户端类

```
class MoeyuAPIClient
  ├── static userDataFile: File
  ├── static userData: UserData
  ├── enum GachaCoin { BRONZE, GOLD, PLATINUM, None }
  ├── userSignUp(): UserData
  ├── userData(String): UserData
  ├── userGatya(String, GachaCoin): GachaResult
  └── userBilling(String, String): UserData [远程调用]

abstract class BaseTask<Params, Progress, Result> extends AsyncTask
  ├── mApiClient: MoeyuAPIClient
  ├── mListener: MoeyuAPITaskListener<Result>
  ├── storeUserData(UserData): void
  └── onPostExecute(Result): void
```

---

## 附录B: 文件清单与代码行数统计

| 文件 | 预估行数 | 主要职责 |
|------|----------|----------|
| BathActivity.java | 1080 | 浴室互动（核心页面） |
| GatyaActivity.java | 719 | 扭蛋抽卡 |
| LAppAnimation.java | 319 | Live2D动画控制 |
| LAppRenderer.java | 289 | OpenGL渲染器 |
| BaseActivity.java | 270 | Activity基类+导航 |
| MainActivity.java | 284 | 路由中枢+入口 |
| TitleActivity.java | 368 | 标题页+登录注册 |
| MoeyuAPIClient.java | 350 | API客户端（本地化） |
| DatabaseOpenHelper.java | 332 | SQLite数据库 |
| LAppLive2DManager.java | 182 | Live2D管理器 |
| UserData.java | 192 | 用户数据模型 |
| LAppModel.java | 94 | Live2D模型 |
| Decryption.java | 104 | 语音解密 |
| VoiceManager.java | 64 | 语音选择 |
| BaseTask.java | 59 | 异步任务基类 |
| EventController.java | 74 | 事件控制 |
| 其他文件(约50个) | ~1200 | 辅助功能 |

> 总计约68个Java文件，预估总代码量约6000行。

---

*文档结束*
