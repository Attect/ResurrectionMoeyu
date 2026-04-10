# ResurrectionMoeyu 构建部署分析文档

> 分析日期: 2026-04-10
> 分析者: dev-ops
> 项目: ResurrectionMoeyu (jp.co.a_tm.moeyu)

---

## 1. 构建配置详解

### 1.1 构建工具版本

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Android Gradle Plugin (AGP) | 9.0.0 | 根build.gradle中声明 |
| Gradle Wrapper | 9.1.0 | gradle-wrapper.properties中配置 |
| Gradle分发源 | 阿里云镜像 | `mirrors.aliyun.com/macports/distfiles/gradle/` |
| Java兼容版本 | 1.8 (Java 8) | compileOptions中配置 |

### 1.2 根项目配置 (build.gradle)

```
构建脚本仓库: google(), mavenCentral()
AGP依赖: com.android.tools.build:gradle:9.0.0
项目结构: 单模块 (:app)
```

**注意事项:**
- AGP 9.0.0 配合 Gradle 9.1.0 属于较新版本组合
- 仓库源使用默认的google()和mavenCentral()，在国内网络环境下可能需要配置代理或镜像

### 1.3 应用模块配置 (app/build.gradle)

#### Android SDK版本

| 配置项 | 值 | 说明 |
| --- | --- | --- |
| compileSdkVersion | 34 | 对应Android 14 |
| minSdkVersion | 19 | 支持Android 4.4+ |
| targetSdkVersion | 34 | 目标Android 14 |
| namespace | jp.co.a_tm.moeyu | 在build.gradle中声明 |

#### 版本信息

| 配置项 | 值 | 说明 |
| --- | --- | --- |
| applicationId | jp.co.a_tm.moeyu | 应用包名 |
| versionCode | 1 | 版本号(整数) |
| versionName | 1.0 | 版本名称(字符串) |
| versionFile.log | 7 | 独立版本日志，与versionCode不一致，待确认用途 |

#### 构建类型 (buildTypes)

| 构建类型 | 配置 | 说明 |
| --- | --- | --- |
| **release** | minifyEnabled: true, shrinkResources: true | 开启代码混淆和资源压缩 |
| **debug** | 未显式配置(使用默认值) | 默认不混淆 |

**release构建详细配置:**
- ProGuard规则文件: `proguard-android-optimize.txt`(AGP默认) + `proguard-rules.pro`(自定义)
- 资源压缩: 开启(shrinkResources true)
- 代码混淆: 开启(minifyEnabled true)

#### 签名配置

**当前状态: 未配置签名**

- build.gradle中未定义`signingConfigs`块
- release构建类型未引用任何签名配置
- 项目中未找到`.jks`或`.keystore`签名文件
- .gitignore中`*.jks`行被注释掉了

**影响:**
- release构建将使用默认的debug签名，无法发布到Google Play
- 需要创建签名密钥库并配置签名信息才能生成可发布的APK

### 1.4 Gradle属性配置 (gradle.properties)

| 属性 | 值 | 说明 |
| --- | --- | --- |
| android.builtInKotlin | false | 不使用Kotlin |
| android.defaults.buildfeatures.resvalues | true | 启用资源值生成 |
| android.dependency.useConstraints | true | 使用依赖约束 |
| android.enableAppCompileTimeRClass | false | 禁用编译时R类 |
| android.enableJetifier | true | 启用Jetifier(AndroidX迁移支持) |
| android.newDsl | false | 不使用新版DSL |
| android.r8.optimizedResourceShrinking | false | R8优化资源压缩关闭 |
| android.r8.strictFullModeForKeepRules | false | R8严格模式关闭 |
| android.useAndroidX | true | 使用AndroidX |
| android.usesSdkInManifest.disallowed | false | 允许Manifest中声明SDK版本 |
| org.gradle.jvmargs | -Xmx1536m | JVM最大堆内存1536MB |

**潜在问题:**
- `android.enableJetifier=true` 适用于仍在使用Support Library的项目，本项目已使用AndroidX，理论上可以关闭以加快构建速度
- `org.gradle.jvmargs=-Xmx1536m` 堆内存设置偏小，对于大型项目可能导致OOM，但本项目规模较小，影响不大
- `android.newDsl=false` 在AGP 9.0下是否兼容待确认

### 1.5 ProGuard混淆规则分析 (proguard-rules.pro)

#### 保留规则概览

| 保留目标 | 规则 | 说明 |
| --- | --- | --- |
| AndroidX | `-keep class androidx.** { *; }` | 保留所有AndroidX类 |
| Live2D SDK | `-keep class com.github.hiroshi_nakamura.live2d.**` 和 `jp.co.live2d.**` | 保留Live2D所有类 |
| Activity类 | `-keep public class jp.co.a_tm.moeyu.**.*Activity` | 保留所有Activity |
| Fragment类 | `-keep public class jp.co.a_tm.moeyu.**.*Fragment` | 保留所有Fragment |
| View类 | `-keep public class jp.co.a_tm.moeyu.**.*View` | 保留所有View |
| Apache HttpClient | `-keep class org.apache.http.** { *; }` | 保留HTTP客户端类 |
| 网络层 | `-keep class jp.co.a_tm.moeyu.network.** { *; }` | 保留网络相关类 |
| 数据模型 | `-keep class jp.co.a_tm.moeyu.model.**` 和 `jp.co.a_tm.moeyu.data.**` | 保留数据模型类 |

#### 通用属性保留

```
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClass, SourceFile, LineNumberTable
```

**潜在问题:**
1. `-keep class androidx.** { *; }` 过于宽泛，会保留大量不必要的AndroidX类，增大APK体积
2. 调试选项被注释掉(`-printseeds`, `-printusage`, `-printmapping`)，如果需要排查混淆问题需手动开启
3. 项目源码中`jp.co.a_tm.moeyu.network`和`jp.co.a_tm.moeyu.data`包可能不存在，这些规则为冗余配置

---

## 2. 依赖清单及版本评估

### 2.1 依赖列表

| 依赖 | 类型 | 版本 | 用途 | 最新稳定版(估算) | 状态评估 |
| --- | --- | --- | --- | --- | --- |
| `androidx.appcompat:appcompat` | implementation | 1.6.1 | AndroidX兼容库 | 1.7.x | 偏旧 |
| `live2d_android.jar` | 本地JAR(libs/) | 未知 | Live2D SDK渲染引擎 | 待确认 | 无法评估 |
| `junit:junit` | testImplementation | 4.13.2 | 单元测试框架 | 4.13.2 | 正常 |
| `androidx.test:runner` | androidTestImplementation | 1.5.2 | AndroidX测试运行器 | 1.6.x | 偏旧 |
| `androidx.test.espresso:espresso-core` | androidTestImplementation | 3.5.1 | UI测试框架 | 3.6.x | 偏旧 |
| Apache HttpClient | useLibrary | legacy(系统内置) | HTTP客户端 | 已废弃 | 已废弃 |
| Google Play Billing | 源码内嵌 | v2 | 应用内支付 | v7+ | 已废弃 |

### 2.2 依赖管理方式

**当前方式:** 混合模式
- 远程依赖: 通过Maven仓库自动解析(appcompat, junit等)
- 本地依赖: `fileTree(include: ['*.jar'], dir: 'libs')` + `files('libs/live2d_android.jar')`
  - **注意:** live2d_android.jar被声明了两次(通过fileTree通配和通过files显式声明)，存在重复引用

**缺失的依赖管理:**
- 无版本目录(Version Catalog)配置
- 无依赖约束统一管理
- 无BOM(Bill of Materials)对齐

### 2.3 依赖风险评估

| 风险项 | 严重程度 | 说明 |
| --- | --- | --- |
| Apache HttpClient(legacy) | 高 | Android 9+已标记为废弃，使用`useLibrary`强制引入，仅用于兼容旧代码 |
| Google Play Billing v2 | 高 | Google已停止支持v2版本API，新应用无法使用，BillingService和BillingReceiver需要重写 |
| Live2D SDK版本未知 | 中 | 本地JAR无法得知确切版本，可能存在已知漏洞或兼容性问题 |
| appcompat 1.6.1 | 低 | 非最新但仍在维护线内，功能正常 |

---

## 3. 构建问题清单及建议

### 3.1 严重问题

#### 问题1: AGP 9.0.0 + Gradle 9.1.0 兼容性

- **描述:** AGP 9.0.0 是非常新的版本(截至分析日期)，部分Gradle属性和API可能已变更
- **影响:** `gradle.properties`中的部分属性(如`android.newDsl=false`, `android.enableAppCompileTimeRClass=false`)可能不再被识别或行为已改变
- **建议:** 执行一次完整构建验证，确认所有配置项在新版AGP下仍有效

#### 问题2: 缺少签名配置

- **描述:** build.gradle中未配置release签名，无法生成可发布的APK
- **影响:** 无法发布到应用商店，release构建使用debug签名
- **建议:** 创建签名密钥库并在build.gradle中配置signingConfigs

#### 问题3: Google Play Billing v2已废弃

- **描述:** Manifest中声明了BillingService和BillingReceiver，使用v2版Billing API
- **影响:** Google Play已停止支持v2 Billing API，支付功能无法正常工作
- **建议:** 如需保留支付功能，需升级到Google Play Billing Library(BillingClient)

### 3.2 一般问题

#### 问题4: Gradle Wrapper使用阿里云镜像

- **描述:** `gradle-wrapper.properties`中distributionUrl指向阿里云镜像
- **影响:** 在非中国大陆网络环境下可能导致下载失败；镜像同步可能存在延迟
- **建议:** 根据团队网络环境选择合适的分发源，或配置本地Gradle分发

#### 问题5: 本地JAR重复声明

- **描述:** `fileTree(include: ['*.jar'], dir: 'libs')`已经包含了libs/下所有JAR，又额外通过`files('libs/live2d_android.jar')`显式声明
- **影响:** 虽然Gradle会自动去重不会导致编译错误，但属于冗余配置
- **建议:** 保留fileTree即可，移除显式的`files()`声明

#### 问题6: ProGuard规则过于宽泛

- **描述:** `-keep class androidx.** { *; }`保留所有AndroidX类
- **影响:** 混淆效果减弱，APK体积增大
- **建议:** 精确指定需要保留的AndroidX类，或让AGP自动处理AndroidX的保留规则

#### 问题7: JVM堆内存设置偏小

- **描述:** `org.gradle.jvmargs=-Xmx1536m`
- **影响:** 构建速度可能受限，但对于本项目的规模影响有限
- **建议:** 可适当增大到2048m或4096m

### 3.3 轻微问题

#### 问题8: compileSdkVersion/targetSdkVersion写法

- **描述:** 使用`compileSdkVersion 34`而非AGP新DSL的`compileSdk 34`
- **影响:** 旧写法在当前版本仍可工作，但在未来AGP版本中可能被移除
- **建议:** 待确认AGP 9.0是否仍支持旧DSL

#### 问题9: AndroidManifest中声明了不必要的前置权限

- **描述:** `READ_PHONE_STATE`权限在新版Android中可能触发敏感权限审核
- **影响:** 可能影响应用在Google Play上的审核流程
- **建议:** 确认该权限是否实际使用，如未使用应移除

#### 问题10: compatible-screens限制过严

- **描述:** Manifest中的`compatible-screens`只声明了small和normal屏幕，且密度只覆盖到xhdpi
- **影响:** 平板设备和高密度屏幕设备可能被过滤
- **建议:** 评估是否需要支持平板设备，至少添加large和xlarge屏幕以及xxhdpi密度

---

## 4. 开发工具使用说明

### 4.1 OKK语音加密工具 (dev_tools/okk/)

#### 功能概述

OKK工具用于语音文件的加密和解密，采用XOR 58算法(密钥值: 58)。项目中的`.okk`文件是OGG音频文件经过XOR加密后的产物，应用运行时由`Decryption`类在内存中解密还原为OGG格式播放。

#### 工具组成

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `ogg2okk.c` | C源码 | OGG → OKK加密工具 |
| `okk2ogg.c` | C源码 | OKK → OGG解密工具 |
| `ogg2okk.exe` | Windows可执行文件 | 已编译的加密工具 |
| `okk2ogg.exe` | Windows可执行文件 | 已编译的解密工具 |
| `CMakeLists.txt` | CMake构建配置 | 用于编译C源码 |
| `276.ogg` | 示例文件 | 用于测试的OGG音频文件 |
| `build/` | 构建目录 | CMake生成的构建缓存 |

#### 使用方法

**加密(OGG → OKK):**
```bash
# Windows环境
ogg2okk.exe <输入.ogg> <输出.okk>

# 示例
ogg2okk.exe 276.ogg 276.okk
```

**解密(OKK → OGG):**
```bash
# Windows环境
okk2ogg.exe <输入.okk> <输出.ogg>

# 示例
okk2ogg.exe 276.okk 276.ogg
```

**从源码编译:**
```bash
# 需要安装CMake和C编译器(MinGW/MSVC)
mkdir build && cd build
cmake ..
cmake --build .
```

#### 加密原理

```
加密/解密算法: XOR 58
对每个字节: output_byte = input_byte XOR 58
XOR运算的对称性使得加密和解密使用相同的操作
```

#### 应用场景

1. **添加新语音文件:** 将OGG音频通过ogg2okk加密后放入`app/src/main/assets/voice/`(日文)或`app/src/main/assets/voice_cn/`(中文)
2. **提取现有语音:** 使用okk2ogg将assets中的.okk文件解密为OGG音频
3. **语音配置更新:** 同时需要更新`app/src/main/assets/voice.json`配置文件

---

## 5. 部署流程建议

### 5.1 开发环境搭建

#### 前置要求

| 工具 | 最低版本要求 | 推荐版本 |
| --- | --- | --- |
| Android Studio | Ladybug+ | 最新稳定版 |
| JDK | 17+ | JDK 21 |
| Android SDK | compileSdk 34 | 最新 |
| Gradle | (通过Wrapper自动下载) | 9.1.0 |
| CMake(可选,用于okk工具) | 3.10+ | 最新 |

#### 环境搭建步骤

1. **安装Android Studio:** 下载安装最新版Android Studio
2. **配置SDK:** 通过SDK Manager安装Android 14(API 34) SDK Platform和Build Tools
3. **克隆项目:** `git clone <仓库地址>`
4. **打开项目:** Android Studio → Open → 选择项目根目录
5. **Gradle同步:** 等待Gradle自动下载依赖和构建配置
6. **配置签名(发布构建时):** 创建keystore文件并配置到build.gradle

### 5.2 Debug构建

```bash
# 使用Gradle Wrapper构建debug APK
./gradlew assembleDebug

# 产物位置
# app/build/outputs/apk/debug/app-debug.apk
```

Debug构建使用Android SDK自动生成的debug签名，可直接安装到设备调试。

### 5.3 Release构建

#### 前置准备: 配置签名

1. **生成密钥库:**
```bash
keytool -genkey -v -keystore release.keystore -alias moeyu -keyalg RSA -keysize 2048 -validity 10000
```

2. **在app/build.gradle中添加签名配置:**
```groovy
android {
    signingConfigs {
        release {
            storeFile file("../release.keystore")
            storePassword "待配置"
            keyAlias "moeyu"
            keyPassword "待配置"
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

> **安全提示:** 密码不应明文写入build.gradle，建议使用`local.properties`或环境变量存储

3. **执行Release构建:**
```bash
./gradlew assembleRelease

# 产物位置
# app/build/outputs/apk/release/app-release.apk
```

### 5.4 版本管理建议

#### 版本号规范

当前版本: `versionCode: 1, versionName: "1.0"`

建议遵循语义化版本(SemVer):
- `versionName`: MAJOR.MINOR.PATCH (如 1.0.0)
- `versionCode`: 递增整数，每次发布+1

#### versionFile.log

当前值为`7`，与build.gradle中的versionCode(1)不一致。此文件的用途待确认，可能是:
- 原始应用的内部版本标记
- 构建系统自动生成的版本递增记录
- 资源版本标记

### 5.5 构建验证清单

| 检查项 | 命令/方法 | 预期结果 |
| --- | --- | --- |
| Gradle同步 | Android Studio Sync | 无错误 |
| Debug构建 | `./gradlew assembleDebug` | BUILD SUCCESSFUL |
| Release构建 | `./gradlew assembleRelease` | BUILD SUCCESSFUL(需先配置签名) |
| 单元测试 | `./gradlew test` | SecurityUtilsTest全部通过 |
| Lint检查 | `./gradlew lint` | 无严重警告 |
| APK安装 | adb install app.apk | 成功安装 |
| 启动运行 | adb shell am start -n jp.co.a_tm.moeyu/.MainActivity | 正常启动 |

### 5.6 CI/CD注意事项

如需配置持续集成:
1. 需要配置Android SDK环境
2. Gradle Wrapper需要可执行权限(`chmod +x gradlew`)
3. 签名密钥需要通过安全方式注入(环境变量/密钥管理服务)
4. 注意阿里云Gradle镜像在CI环境中可能需要替换

---

## 6. 构建兼容性总结

### 6.1 当前构建可行性评估

| 构建场景 | 可行性 | 阻塞因素 |
| --- | --- | --- |
| Debug构建 | 可行(需验证AGP 9.0兼容性) | AGP 9.0.0较新，需确认本地环境支持 |
| Release构建 | 需配置 | 缺少签名配置 |
| 单元测试 | 可行 | 仅SecurityUtilsTest一个测试类 |
| 发布到Google Play | 不可行 | Billing v2已废弃、缺少签名、READ_PHONE_STATE权限审核问题 |

### 6.2 推荐的构建配置优化路径

> 注意: 以下为参考路径，实际修改需由code-builder或架构师评估后执行

1. **优先级P0 - 签名配置:** 创建release签名并配置到构建系统
2. **优先级P1 - AGP兼容性验证:** 确认AGP 9.0.0与当前配置的完整兼容性
3. **优先级P2 - Billing升级:** 如需支付功能，升级到Google Play Billing Library
4. **优先级P3 - 依赖清理:** 移除重复JAR声明、精简ProGuard规则

---

*文档变更记录:*
- [2026-04-10] [dev-ops] [初始版本] 项目构建部署分析文档创建
