# ResurrectionMoeyu — AI Agent 项目指南

> 本文档面向 AI Coding Agent。阅读前默认你对本项目一无所知。
> 项目主要注释和文档语言为 **中文**。代码内保留部分日文资源与包名。

---

## 项目概述

**ResurrectionMoeyu**（包名 `jp.co.a_tm.moeyu`）是一款基于 Live2D 的 Android 角色互动应用，源自对日本 a_tm 公司「もえゆ(MoeYu)」应用的逆向重建与本地化改造。应用核心是在"浴室"场景中展示 Live2D 角色模型，玩家可通过触摸不同区域触发语音与动画，使用扭蛋系统收集物品，并解锁语音、笔记等收藏内容。

项目已完全脱离远程服务器，所有原 HTTP API 逻辑均已改为本地实现，可离线运行。

| 属性 | 值 |
|------|-----|
| 应用包名 | `jp.co.a_tm.moeyu` |
| 开发语言 | Java（纯 Java，无 Kotlin） |
| 构建工具 | Gradle（Groovy DSL） |
| Android Gradle Plugin | 9.0.0 |
| Gradle Wrapper | 9.1.0（阿里云镜像分发） |
| compileSdk / targetSdk | 34（Android 14） |
| minSdk | 19（Android 4.4） |
| Java 兼容性 | 1.8 |
| 版本号 | versionCode 1 / versionName "1.0" |

---

## 技术栈与依赖

| 分类 | 技术/库 | 说明 |
|------|---------|------|
| UI 兼容库 | `androidx.appcompat:appcompat:1.6.1` | AndroidX 基础兼容 |
| 2D 渲染引擎 | Live2D SDK (`libs/live2d_android.jar`) | 本地 JAR，角色模型渲染 |
| OpenGL | OpenGL ES | 自定义 `GLSurfaceView` + `Renderer` |
| 网络（遗留） | Apache HttpClient (legacy) | `useLibrary` 引入，原远程 API 已本地化 |
| 数据库 | SQLite (`SQLiteOpenHelper`) | `collection.db`，3 张表 |
| 序列化 | Java `Serializable` | `UserData` 本地持久化 |
| 音频播放 | `android.media.MediaPlayer` | OGG 格式语音 |
| 相机 | 旧版 `android.hardware.Camera` | AR 模式背景（已废弃 API） |
| 支付（已废弃） | Google Play Billing v2 | 源码内嵌，Google 已停止支持 |
| 测试 | JUnit 4.13.2、Espresso 3.5.1 | 测试覆盖极低 |

**依赖风险提示：**
- `live2d_android.jar` 为本地二进制，版本未知，不可随意替换。
- Apache HttpClient legacy 与 Billing v2 均为已废弃 API，仅作遗留代码兼容。
- `app/build.gradle` 中通过 `fileTree(dir: 'libs', include: ['*.jar'])` 与 `files('libs/live2d_android.jar')` 重复引用了 Live2D JAR，属冗余但无害。

---

## 项目结构与模块划分

```
ResurrectionMoeyu/
├── build.gradle                    # 根构建配置
├── settings.gradle                 # 仅包含 :app 模块
├── gradle.properties               # Gradle / Android 构建属性
├── gradlew / gradlew.bat           # Gradle Wrapper
├── local.properties                # SDK 路径（gitignore，本地生成）
├── versionFile.log                 # 独立版本标记（当前值为 7）
├── dev_tools/
│   └── okk/                        # 语音 XOR 加密/解密工具（C + CMake）
│       ├── ogg2okk.c / .exe
│       ├── okk2ogg.c / .exe
│       └── CMakeLists.txt
├── ai/                             # AI 分析文档（非源码）
│   ├── analysis/overview.md
│   ├── dev/project.md
│   ├── deployment.md
│   ├── design_by_user_say.md
│   └── tasks/                      # 任务跟踪 YAML
└── app/
    ├── build.gradle
    ├── proguard-rules.pro          # ProGuard / R8 混淆规则
    ├── libs/live2d_android.jar     # Live2D SDK
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/             # 加密语音、Live2D 模型、voice.json
        │   ├── java/jp/co/a_tm/moeyu/
        │   └── res/                # 布局、图片、动画、raw、values
        └── test/java/              # 仅 SecurityUtilsTest（几乎无测试）
```

### Java 源码包结构

| 包路径 | 职责 |
|--------|------|
| `jp.co.a_tm.moeyu` | Activity 层、数据库控制、核心业务类（硬币、好感度、事件等） |
| `jp.co.a_tm.moeyu.api` | API 客户端（`MoeyuAPIClient`），已完全本地化 |
| `jp.co.a_tm.moeyu.api.fragment` | `NetworkBaseFragment` 及其子类（登录、注册、扭蛋、支付） |
| `jp.co.a_tm.moeyu.api.task` | `AsyncTask` 封装（`BaseTask`、`LoginTask`、`GachaTask` 等） |
| `jp.co.a_tm.moeyu.api.listener` | 异步任务回调接口 |
| `jp.co.a_tm.moeyu.api.model` | `GachaResult` 等网络层模型 |
| `jp.co.a_tm.moeyu.live2d` | Live2D 管理器、文件管理、加速度辅助 |
| `jp.co.a_tm.moeyu.live2d.model` | `LAppModel`（模型加载） |
| `jp.co.a_tm.moeyu.live2d.motion` | `LAppAnimation`、`LAppExpressionMotion` |
| `jp.co.a_tm.moeyu.live2d.util` | `AccelHelper` |
| `jp.co.a_tm.moeyu.live2d.view` | `LAppGLView`、`LAppRenderer`（OpenGL） |
| `jp.co.a_tm.moeyu.model` | `UserData`、`EventData` |
| `jp.co.a_tm.moeyu.security` | `SecurityUtils`（SHA-1/SHA-256 签名） |
| `jp.co.a_tm.moeyu.util` | `Config`、`Logger`、`UserDataManager` |

---

## 构建与运行命令

### 环境前置要求
- Android Studio（Ladybug 或更新）
- JDK 17+
- Android SDK API 34 Platform + Build Tools
- CMake 3.10+（仅当需要重新编译 `okk` 工具时）

### 常用 Gradle 命令

```bash
# Debug 构建
./gradlew assembleDebug
# 产物: app/build/outputs/apk/debug/app-debug.apk

# Release 构建（需先配置签名）
./gradlew assembleRelease
# 产物: app/build/outputs/apk/release/app-release.apk

# 运行单元测试
./gradlew test
# 当前仅 SecurityUtilsTest 一个测试类

# Lint 检查
./gradlew lint

# 清理构建产物
./gradlew clean
```

### Release 签名配置（待补充）

当前 `app/build.gradle` **未配置** `signingConfigs`。Release 构建会使用 debug 签名，无法上架。如需发布，需创建 keystore 并在 `app/build.gradle` 中添加：

```groovy
android {
    signingConfigs {
        release {
            storeFile file("../release.keystore")
            storePassword "..."
            keyAlias "moeyu"
            keyPassword "..."
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

> **安全提示**：密码不要明文写入版本控制，建议通过 `local.properties` 或环境变量注入。

---

## 架构与导航模式

### Activity 驱动的单体架构

项目**没有采用 MVP、MVVM 或 MVI**。所有业务逻辑直接写在 Activity 中，属于典型的 Activity 驱动单体架构。

### 中央路由器模式

`MainActivity` 作为**永不销毁**的路由中枢：
- 所有子 Activity 均通过 `startActivityForResult` 启动。
- 子 Activity 返回时在 `Intent` 中放置导航码（`EXTRA_NEXT_ACTIVITY`）。
- `MainActivity.onActivityResult` 根据导航码 `switch` 分发到下一个 Activity。

**导航码常量（定义于 `BaseActivity`）**：

| 码值 | 常量 | 目标 Activity |
|------|------|---------------|
| -1 | `NEXT_EXIT` | 退出应用 |
| 0 | `NEXT_ACTIVITY_TITLE` | `TitleActivity` |
| 1 | `NEXT_ACTIVITY_GACHA` | `GatyaActivity` |
| 2 | `NEXT_ACTIVITY_BATH` | `BathActivity` |
| 3 | `NEXT_ACTIVITY_PREFERENCE` | `PreferenceActivity` |
| 4 | `NEXT_ACTIVITY_PREFERENCE_FROM_BATH` | `PreferenceActivity`（从浴室进入） |
| 5 | `NEXT_ACTIVITY_GACHA_RESULT` | `GatyaResultActivity` |
| 6 | `NEXT_ACTIVITY_COLLECTION` | `CollectionRoomActivity` |
| 7 | `NEXT_ACTIVITY_ITEM_COLLECTION` | `ItemCollectionActivity` |
| 8 | `NEXT_ACTIVITY_VOICE_COLLECTION` | `VoiceCollectionActivity` |
| 9 | `NEXT_ACTIVITY_ROOM` | `MomorisRoomActivity` |
| 10 | `NEXT_ACTIVITY_DIARY` | `NoteCollectionActivity` |

### 关键 Activity 职责

| Activity | 职责 |
|----------|------|
| `MainActivity` | 应用入口、播放开场视频 (`R.raw.ateam_moive`)、屏幕修正值计算、路由分发 |
| `TitleActivity` | 标题页、首次运行数据初始化（语音解密）、本地登录/注册、DeepLink 入口 (`moeyu://moe-yu.com`) |
| `BathActivity` | **核心互动页**（约 1080 行）。Live2D 展示、触摸区域识别、语音/动画联动、场景切换、AR 相机、物品使用 |
| `GatyaActivity` | 扭蛋/抽卡、硬币管理、支付对话框、转盘手势识别 |
| `GatyaResultActivity` | 抽卡结果、经验条动画、等级提升/全收集事件提示 |
| `PreferenceActivity` | 设置页（AR 相机开关） |
| `CollectionRoomActivity` | 收藏系统导航 |
| `ItemCollectionActivity` | 25 个物品收集状态与详情 |
| `VoiceCollectionActivity` | 语音列表（普通/物品/事件 三标签） |
| `NoteCollectionActivity` | 日记/笔记解锁与阅读 |
| `MomorisRoomActivity` | 桃璃房间（根据日记解锁状态动态装饰） |

---

## 数据持久化

项目使用**三种混合持久化**方式，无统一 Repository 层：

| 存储方式 | 位置/文件 | 管理类 | 内容 |
|----------|-----------|--------|------|
| Java 序列化 | `cacheDir/localUserData.dat` | `MoeyuAPIClient` | 用户数据（金币、经验、等级、物品列表） |
| Java 序列化 | `fileDir/userData.dat` | `UserDataManager` | 用户数据（另一份拷贝） |
| SQLite | `collection.db`（版本 1） | `DatabaseOpenHelper` + TableController | 物品/语音/笔记的解锁状态 |
| SharedPreferences | 系统默认 | `PreferencesHelper` | 首次运行标记、相机开关、引导标记等 |

**已知问题**：`UserData` 同时存储在 `localUserData.dat` 与 `userData.dat` 两份文件中，存在数据不一致风险。

### SQLite 表结构

| 表名 | 字段 | 初始化数据来源 |
|------|------|----------------|
| `ItemTable` | `_id`, `name`, `opened` | 硬编码 01-25（`DatabaseOpenHelper`） |
| `VoiceTable` | `_id`, `name`, `opened`, `title` | `R.raw.voice`（CSV） |
| `NoteTable` | `_id`, `name`, `opened`, `term` | `R.raw.note`（CSV） |

---

## 语音系统与资源加密

### 语音解密
- 首次运行时，`TitleActivity` 的 `InitializeDataTask` 调用 `Decryption.execute()`。
- 将 `assets/voice/*.okk` 与 `assets/voice_cn/*.okk` 解密为应用私有目录下的 `.ogg` 文件。
- **加密算法**：XOR 58（每个字节与 `58` 异或）。安全性极低。
- 已解密的文件不会重复处理。

### 语音选择
- 配置源：`assets/voice.json`
- 层级：`scene → region → item → level → [{voice, probability}]`
- 中文语音优先（`VoiceManager.useCN` 默认为 `true`）：优先查找 `{voiceName}_cn.ogg`，不存在则回退到 `{voiceName}.ogg`。

### OKK 工具（`dev_tools/okk/`）
如需添加或提取语音：
```bash
# 加密
ogg2okk.exe input.ogg output.okk

# 解密
okk2ogg.exe input.okk output.ogg
```
C 源码提供 `CMakeLists.txt`，可用 CMake 重新编译。

---

## 代码风格与开发约定

### 注释语言
- **代码注释使用中文**。近期提交大量进行了"日文全面中文化"重构。
- 类头部通常包含 `@author Attect`、`@date` 等 Javadoc 标签。
- 方法注释使用标准 Javadoc 格式，说明参数与用途。

### 命名与组织
- 包名沿用日文原应用结构：`jp.co.a_tm.moeyu`
- Activity/Fragment/View 类名遵循 Android 惯例（`XxxActivity`、`XxxFragment`、`XxxView`）。
- 常量全部大写 + 下划线（如 `NEXT_ACTIVITY_BATH`、`EXTRA_NEXT_ACTIVITY`）。
- 字符串资源中同时存在日文与中文内容；应用内文本以中文为主。

### 屏幕适配
- `MainActivity.FIX_HEIGHT` 为全局静态变量，计算公式：`屏幕高度 - (屏幕宽度 / 9 * 16)`，用于非 16:9 屏幕的 UI 修正。
- 多 Activity 直接读取该静态值设置 Padding，**属于全局状态共享**。

### 状态共享方式
- 大量依赖**静态变量**在 Activity 间传递状态（如 `FIX_HEIGHT`、`MoeyuAPIClient.userData`）。
- 页面间数据也通过 `Intent` Extra 传递（`Serializable` 对象如 `UserData`、`EventData`、`GachaResult`）。

---

## 测试策略

**当前测试覆盖极差**，`app/src/test/` 下仅有：
- `jp.co.a_tm.moeyu.security.SecurityUtilsTest.java`

无集成测试、无 UI 测试（虽然依赖中引入了 Espresso，但无实际用例）。

### 建议的测试优先级
如需补充测试，建议优先覆盖：
1. `VoiceManager.getVoiceName()` — 语音选择概率算法
2. `MoeyuAPIClient.userGatya()` — 扭蛋概率与硬币扣除逻辑
3. `LovePoint.currentLevel(exp)` — 好感度等级计算
4. `EventController.checkEvent()` — 事件触发条件
5. `Decryption.execute()` — 文件解密结果正确性

---

## 安全注意事项

| 风险点 | 详情 | 建议 |
|--------|------|------|
| 弱加密 | 语音文件使用 XOR 58 单字节异或 | 如需保护资源，应升级为 AES 或移至 NDK |
| 硬编码密钥 | `local_secret`、`appId=MOEYU_001` 等写死在源码 | 考虑使用 Android Keystore 或 NDK 层隐藏 |
| 明文 HTTP | `android:usesCleartextTraffic="true"` | 原 API 已本地化，如无远程通信可关闭 |
| 废弃 API | `AsyncTask`、Apache HttpClient、旧 Camera API、Billing v2 | 逐步替换为现代替代方案 |
| 权限审核 | `READ_PHONE_STATE` 仍在 Manifest 中 | 确认是否实际使用，未使用则移除，避免商店审核问题 |
| 数据一致性 | `UserData` 双文件序列化 | 统一为单一数据源 |

---

## 已知技术债务与限制

1. **无架构分层**：`BathActivity` 约 1080 行，直接混合 UI、交互逻辑、语音管理、动画协调。
2. **已废弃 API 使用**：`AsyncTask`（API 异步任务）、旧 `Camera` API、Google Play Billing v2。
3. **静态变量共享状态**：`FIX_HEIGHT`、`userData` 等静态字段易导致内存泄漏与状态不一致。
4. **ProGuard 规则过于宽泛**：`-keep class androidx.** { *; }` 会显著增大 Release APK 体积。
5. **无签名配置**：Release 构建不可直接用于发布。
6. **Billing 功能已废弃**：`BillingService`、`PurchaseObserver` 等内嵌的 v2 支付代码无法在 Google Play 正常工作。
7. **兼容屏幕限制过严**：`compatible-screens` 仅声明 `small`/`normal` + 到 `xhdpi`，可能过滤平板与高分辨率设备。

---

## 扩展点速查

| 扩展需求 | 涉及文件/位置 |
|----------|---------------|
| 新增 Live2D 场景 | `Scene.java` 枚举 + `LAppAnimation` 闲置动画组 + `LAppRenderer` 背景纹理 |
| 新增触摸区域 | `Region.java` 枚举 + `BathActivity.createCommonRegionMap()` 坐标范围 |
| 新增物品 | `UserData.MAX_ITEM_COUNT` + `DatabaseOpenHelper.initItemRows()` + drawable 资源 |
| 新增语音 | `assets/voice.json` + `assets/voice/` 或 `assets/voice_cn/` 下的 `.okk` 文件 |
| 新增事件类型 | `EventData.Type` 枚举 + `EventController.checkEvent()` |
| 更换 Live2D 模型 | `LAppModel.setupModel()`（当前模型路径硬编码为 `assets/live2d/model/moeyu.moc`） |

---

## 相关文档索引

项目 `ai/` 目录下已有更详细的分析文档，供深入参考：
- `ai/analysis/overview.md` — 项目概览与模块划分
- `ai/dev/project.md` — 架构文档、数据流、设计模式、类签名索引
- `ai/design_by_user_say.md` — 功能规格说明书（13 个模块、41 项功能）
- `ai/deployment.md` — 构建配置详解、依赖评估、部署流程建议
- `ai/tasks/` — 活跃任务跟踪（YAML 格式）

---

*本文档基于项目实际源码与配置文件生成，随项目演进应及时更新。*
