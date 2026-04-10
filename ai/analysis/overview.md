# ResurrectionMoeyu 项目概览分析

> 分析日期: 2026-04-10
> 分析者: project-analyzer
> 任务ID: ANALYSIS-001

---

## 1. 项目基本信息

| 项目属性 | 值 |
| --- | --- |
| 项目名称 | ResurrectionMoeyu |
| 应用包名 | jp.co.a_tm.moeyu |
| 应用ID | jp.co.a_tm.moeyu |
| 原始来源 | 疑似日本a_tm公司的もえゆ(MoeYu)应用逆向重建 |
| 项目类型 | 单体Android应用项目 |
| 版本号 | 1.0 (versionCode: 1) |
| 构建版本日志 | 7 (versionFile.log) |

## 2. 技术栈

| 分类 | 技术 | 说明 |
| --- | --- | --- |
| 开发语言 | Java | 纯Java项目，无Kotlin |
| 构建工具 | Gradle (Groovy DSL) | AGP 9.0.0 |
| Android SDK | compileSdk 34 / targetSdk 34 / minSdk 19 | 支持Android 4.4+ |
| 兼容库 | AndroidX AppCompat 1.6.1 | |
| 2D渲染 | Live2D SDK (live2d_android.jar) | 本地JAR依赖，位于libs/ |
| HTTP客户端 | Apache HttpClient (legacy) | 使用useLibrary 'org.apache.http.legacy' |
| 数据库 | SQLite (SQLiteOpenHelper) | 本地数据库 |
| 序列化 | Java Serializable | 用户数据持久化 |
| 加密 | 自定义XOR加密 + SHA-1/SHA-256签名 | 语音文件解密和API签名 |
| 网络通信 | HTTP (非HTTPS) | usesCleartextTraffic=true |
| 音频 | MediaPlayer (android.media) | OGG格式语音 |
| 相机 | Camera API | AR模式支持 |
| 支付 | Google Play Billing (v2) | 应用内购买 |

## 3. 项目结构

```
ResurrectionMoeyu/
├── .gitignore
├── build.gradle                    # 根项目构建配置
├── settings.gradle                 # 包含 :app 模块
├── gradle.properties               # Gradle属性配置
├── gradle/                         # Gradle Wrapper
├── gradlew / gradlew.bat           # Gradle执行脚本
├── local.properties                # SDK路径(已gitignore)
├── versionFile.log                 # 版本号日志(值为7)
├── dev_tools/                      # 开发工具
│   └── okk/                        # 语音加密工具
│       ├── ogg2okk.c / ogg2okk.exe # OGG转OKK加密工具
│       ├── okk2ogg.c / okk2ogg.exe # OKK转OGG解密工具
│       ├── CMakeLists.txt          # CMake构建配置
│       └── 276.ogg                 # 示例OGG文件
└── app/                            # 主应用模块
    ├── build.gradle                # 应用模块构建配置
    ├── proguard-rules.pro          # ProGuard混淆规则
    ├── libs/
    │   └── live2d_android.jar      # Live2D SDK
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/             # 资源文件
        │   │   ├── voice.json      # 语音配置(场景/区域/物品/等级映射)
        │   │   ├── voice/          # 日文语音(.okk加密格式)
        │   │   ├── voice_cn/       # 中文语音(.okk加密格式)
        │   │   ├── *.png           # Live2D水纹理等
        │   │   └── live2d/         # Live2D模型资源
        │   ├── java/               # Java源码
        │   │   └── jp/co/a_tm/moeyu/
        │   └── res/                # Android资源
        └── test/                   # 测试代码(基本为空)
```

## 4. 代码模块划分

### 4.1 Activity层(页面)

| Activity | 功能 | 说明 |
| --- | --- | --- |
| `MainActivity` | 主入口/导航控制器 | 播放开场视频，作为所有Activity跳转的中枢 |
| `TitleActivity` | 标题页面 | 支持DeepLink(moeyu://moe-yu.com) |
| `BathActivity` | 浴室互动(核心页面) | Live2D模型展示、触摸互动、语音播放、物品使用 |
| `GatyaActivity` | 扭蛋/抽卡页面 | 使用铜/金/白金币抽取物品 |
| `GatyaResultActivity` | 抽卡结果页面 | 展示抽到的物品 |
| `MomorisRoomActivity` | 桃璃房间页面 | 角色房间展示 |
| `CollectionRoomActivity` | 收藏房间页面 | 收藏品总览 |
| `ItemCollectionActivity` | 物品收藏页面 | 已获取物品列表 |
| `VoiceCollectionActivity` | 语音收藏页面 | 已解锁语音列表 |
| `NoteCollectionActivity` | 笔记收藏页面 | 日记/笔记收藏 |
| `PreferenceActivity` | 设置页面 | 应用偏好设置 |

### 4.2 核心功能模块

| 包/类 | 功能 | 说明 |
| --- | --- | --- |
| `live2d/` | Live2D渲染引擎集成 | 包含模型管理、动画、渲染、视图等 |
| `api/` | API客户端 | 网络请求(注册/登录/扭蛋/支付)，已改为本地实现 |
| `billing/` | Google Play支付 | v2版Billing服务 |
| `model/` | 数据模型 | UserData、EventData、GachaResult |
| `util/` | 工具类 | Config、Logger、UserDataManager、AspectRatioUtils |
| `security/` | 安全工具 | SecurityUtils签名工具 |

### 4.3 核心业务类

| 类名 | 职责 |
| --- | --- |
| `BaseActivity` | 所有Activity基类，提供页面跳转路由和全屏UI控制 |
| `MoeyuApplication` | Application类，管理首次运行标记 |
| `Decryption` | 语音文件解密(XOR 58加密) |
| `VoiceManager` | 语音管理，根据场景/区域/物品/等级选择语音 |
| `VoiceTableController` | 语音收藏表数据库控制器 |
| `ItemTableController` | 物品收藏表数据库控制器 |
| `NoteTableController` | 笔记收藏表数据库控制器 |
| `DatabaseOpenHelper` | SQLite数据库帮助类(3张表) |
| `CoinController` | 货币(铜/金/白金币)管理 |
| `EventController` | 事件控制器 |
| `LovePoint` | 好感度管理 |
| `SpecialEvent` | 特殊事件触发逻辑 |
| `Scene` | 场景枚举(bath_a/bath_b/head/body) |
| `Region` | 触摸区域枚举(face/head/brest/belly/arm/none) |
| `CSV` | CSV文件加载(语音和笔记标题) |
| `PreferencesHelper` | SharedPreferences帮助类 |
| `TweetDialog` | Twitter分享对话框 |

## 5. 入口点分析

### 5.1 应用启动入口
- **Launcher Activity**: `MainActivity`
  - 来源: `AndroidManifest.xml` 第52-56行
  - 功能: 播放开场视频 → 跳转TitleActivity

### 5.2 DeepLink入口
- **Scheme**: `moeyu://moe-yu.com`
  - 来源: `AndroidManifest.xml` 第61-70行
  - 目标: `TitleActivity`

### 5.3 导航架构
- **模式**: 单Activity路由(MainActivity) + 多子Activity
- 所有子Activity通过`startActivityForResult`启动
- 返回时通过`EXTRA_NEXT_ACTIVITY`整型值决定下一个页面
- 页面跳转码定义在`BaseActivity`中

## 6. 数据存储

### 6.1 本地数据库(SQLite)
- 数据库名: `collection.db` (版本1)
- 表结构:

| 表名 | 字段 | 说明 |
| --- | --- | --- |
| ItemTable | _id, name, opened | 物品收藏(25行) |
| VoiceTable | _id, name, opened, title | 语音收藏(动态行数) |
| NoteTable | _id, name, opened, term | 笔记收藏(动态行数) |

### 6.2 本地文件存储
- **用户数据**: 缓存目录下的`localUserData.dat`(Java序列化)
- **解密语音**: 应用私有目录下的`.ogg`文件
- **SharedPreferences**: 应用偏好设置(相机开关等)

### 6.3 资源文件
- **加密语音**: assets/voice/和assets/voice_cn/下的`.okk`文件
- **语音配置**: assets/voice.json(场景/区域/物品/等级→语音映射)
- **Live2D模型**: assets/live2d/目录下

## 7. 关键发现与风险

### 7.1 技术债务
1. **已废弃API**: 使用Apache HttpClient(legacy)、Camera API(旧版)
2. **明文HTTP**: API地址`http://api.moeapk.com`(非HTTPS)，但已改为本地实现
3. **Google Billing v2**: 已被Google Play废弃，需要升级到BillingClient
4. **最低SDK 19**: 支持非常老的Android版本，可能限制了新API使用
5. **无测试代码**: test目录基本为空

### 7.2 安全相关
1. **XOR 58加密**: 语音文件使用简单异或加密，安全性低
2. **API密钥硬编码**: `local_secret`、`appId=MOEYU_001`等直接写在代码中
3. **签名算法**: SHA-1/SHA-256用于API签名验证

### 7.3 架构特征
1. **单Activity路由模式**: 所有页面跳转通过MainActivity中转
2. **已脱机化**: API客户端已改为本地实现，不依赖远程服务器
3. **中文适配**: 存在voice_cn目录，支持中文语音
4. **版本控制**: git仓库已初始化，有提交历史

## 8. 总结

ResurrectionMoeyu是一个基于原有日本もえゆ(MoeYu)应用进行逆向重建的Android项目。核心功能是展示Live2D角色模型，用户可以在"浴室"场景中与角色互动（触摸不同区域触发语音和动画），通过扭蛋系统获取物品，收集语音和笔记等。

项目采用纯Java编写，使用较老的Android技术栈，已将服务端逻辑本地化（不依赖远程API）。代码结构为典型的Activity驱动模式，无MVP/MVVM架构。

---

*下一步: 委托@code-framework进行架构逆向分析, 委托@requirement-analyst梳理功能清单, 委托@dev-ops分析构建部署流程。*
