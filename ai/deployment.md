# ResurrectionMoeyu 部署文档

**版本**: v1.1  
**创建日期**: 2026-03-27  
**更新日期**: 2026-03-27  
**文档代理**: dev-ops  
**任务 ID**: ANALYSIS-001-DEPLOY, IMPROVEMENT-001-BUILD

---

## 变更日志

| 版本 | 日期 | 变更内容 | 负责人 |
|------|------|---------|--------|
| v1.0 | 2026-03-27 | 初始部署文档创建 | dev-ops (ANALYSIS-001-DEPLOY) |
| v1.1 | 2026-03-27 | 构建体系优化实施更新：targetSdkVersion 升级至 API 34、ProGuard 启用、依赖版本更新 | dev-ops (IMPROVEMENT-001-BUILD) |

---

## 一、构建环境配置

### 1.1 系统要求

#### 硬件要求
| 组件 | 最低配置 | 推荐配置 |
|------|---------|---------|
| **CPU** | 4 核 | 8 核 + |
| **内存** | 8GB RAM | 16GB RAM |
| **磁盘空间** | 20GB 可用空间 | 50GB SSD |
| **网络** | 10Mbps | 100Mbps+ |

#### 软件环境
| 组件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | Java 8 (1.8.0_201+) | 构建工具运行环境 |
| **Gradle** | 9.1.0 | 项目构建管理系统 |
| **Android SDK** | API Level 34 (Android 14) | 目标开发版本 (已升级) |
| **Android Studio** | 2022.2+ (Arctic Fox 或更高) | 推荐 IDE，支持 API 34 |
| **Git** | 2.20+ | 版本控制工具 |

### 1.2 Gradle 配置详解

#### 根项目构建配置 (build.gradle)

```groovy
// Gradle 插件版本
classpath 'com.android.tools.build:gradle:9.0.0'

// 仓库配置
repositories {
    google()           // Google Maven 仓库
    mavenCentral()     // Maven Central 仓库
}

// 清理任务
tasks.register('clean', Delete) {
    delete rootProject.buildDir
}
```

**配置特点**:
- **插件版本**: Android Gradle Plugin 9.0.0，支持最新构建特性
- **仓库配置**: Google 和 Maven Central 双仓库，确保依赖获取稳定性
- **清理任务**: 提供统一的 clean 任务，便于构建环境管理

#### 应用模块构建配置 (app/build.gradle)

```groovy
android {
    compileSdkVersion 34           // 编译 SDK 版本 (已升级至 Android 14)
    namespace "jp.co.a_tm.moeyu"   // 包名空间
    
    defaultConfig {
        applicationId "jp.co.a_tm.moeyu"
        minSdkVersion 19            // 最低支持 Android 5.0
        targetSdkVersion 34         // 目标 SDK Android 14 (已升级)
        versionCode 1               // 版本代码
        versionName "1.0"           // 版本名称
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildTypes {
        release {
            minifyEnabled true      // 已启用 ProGuard/R8 代码压缩
            shrinkResources true    // 已启用资源压缩
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

**关键配置项说明**:

1. **SDK 版本配置**
   - `compileSdkVersion 34`: 使用 Android 14 API 进行编译 (2026-03-27 升级)
   - `minSdkVersion 19`: 支持 Android 5.0 及以上设备 (覆盖约 95% 用户)
   - `targetSdkVersion 34`: 针对 Android 14 优化，符合 Google Play 最新要求

2. **构建类型配置**
   - **Debug**: 开发调试版本，包含调试符号
   - **Release**: 发布版本，已启用 ProGuard/R8 代码压缩和资源压缩 (2026-03-27 启用)
   - **预期优化效果**: APK 体积减少 15-25%，代码安全性和反逆向能力增强

3. **Java 兼容性**
   - Java 8 完全支持，可使用 Lambda 表达式、Stream API 等现代特性

4. **ProGuard 规则配置**
   - 完整的项目专属规则 (proguard-rules.pro)
   - 覆盖范围：AndroidX、Live2D SDK、UI 组件、网络层、数据层
   - 支持调试输出和性能分析

### 1.3 Gradle Wrapper 配置

```properties
distributionUrl=https\://mirrors.aliyun.com/macports/distfiles/gradle/gradle-9.1.0-bin.zip
```

**配置说明**:
- **Gradle 版本**: 9.1.0 (最新稳定版)
- **镜像源**: 阿里云镜像，提升国内下载速度
- **Wrapper 优势**: 确保团队使用一致的 Gradle 版本

### 1.4 项目级配置 (gradle.properties)

```properties
# Android 配置
android.useAndroidX=true                    # 启用 AndroidX 支持
android.enableJetifier=true                 # 启用 Jetifier 兼容工具

# JVM 内存配置
org.gradle.jvmargs=-Xmx1536m               # Gradle Daemon 堆内存 1.5GB

# 构建优化选项
android.enableAppCompileTimeRClass=false   # 禁用编译时 R 类
android.newDsl=false                       # 使用传统 DSL
```

**配置优化建议**:

1. **内存优化**
   ```properties
   # 推荐配置 (根据实际硬件调整)
   org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
   ```

2. **并行构建**
   ```properties
   # 启用并行构建 (适用于多模块项目)
   org.gradle.parallel=true
   
   # 启用构建缓存
   org.gradle.caching=true
   
   # 启用配置缓存 (Gradle 7.0+)
   org.gradle.configuration-cache=true
   ```

---

## 二、依赖管理说明

### 2.1 核心依赖库

#### 本地 JAR 依赖

| 依赖库 | 位置 | 用途 | 大小 |
|--------|------|------|------|
| **live2d_android.jar** | app/libs/ | Live2D 渲染引擎核心库 | 101KB |

**Live2D SDK 功能**:
- Live2DModelAndroid: MOC 模型加载和渲染
- MotionQueueManager: 动画运动队列管理
- ExpressionMgr: 表情队列控制
- EyeBlinkMotion: 自动眨眼系统
- OpenGL ES 渲染支持

#### AndroidX 依赖

```groovy
dependencies {
    // AndroidX 核心库 (已更新至推荐版本)
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    // 测试依赖 (已更新至推荐版本)
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

**依赖说明**:
- **appcompat 1.6.1**: AndroidX 兼容性库，提供主题、样式支持 (2026-03-27 从 1.0.2 升级)
- **JUnit 4.13.2**: 单元测试框架，性能优化 (2026-03-27 从 4.12 升级)
- **Test Runner 1.5.2**: Android 测试运行器，兼容性增强 (2026-03-27 从 1.1.1 升级)
- **Espresso 3.5.1**: UI 测试工具，特性丰富 (2026-03-27 从 3.1.1 升级)

**版本更新收益**:
- ✅ appcompat 1.6.1 包含 Material Design 组件优化
- ✅ Test Runner 1.5.2 提升仪器测试稳定性和执行速度
- ✅ Espresso 3.5.1 增强同步机制和测试覆盖能力
- ✅ 所有依赖均为长期支持版本，减少未来升级频率

### 2.2 依赖版本策略

#### 当前版本评估

| 依赖 | 当前版本 | 最新稳定版 | 评估 | 建议 |
|------|---------|-----------|------|------|
| Android Gradle Plugin | 9.0.0 | 9.0.0 | ✅ 最新 | 保持 |
| appcompat | 1.6.1 | 1.7.0 | ✅ 推荐版本 (已升级) | 稳定使用 |
| JUnit | 4.13.2 | 4.13.2 | ✅ 最新稳定版 (已升级) | 保持 |
| Test Runner | 1.5.2 | 1.6.1 | ✅ 推荐版本 (已升级) | 稳定使用 |
| Espresso | 3.5.1 | 3.6.1 | ✅ 推荐版本 (已升级) | 稳定使用 |

**版本更新记录**:
- **2026-03-27**: 完成核心依赖版本升级 (任务：IMPROVEMENT-001-BUILD)
  - appcompat: 1.0.2 → 1.6.1
  - JUnit: 4.12 → 4.13.2
  - Test Runner: 1.1.1 → 1.5.2
  - Espresso: 3.1.1 → 3.5.1

#### 依赖更新历史

**已完成更新 (2026-03-27)**:
```groovy
// AndroidX 核心库升级 (1.0.2 → 1.6.1)
implementation 'androidx.appcompat:appcompat:1.6.1'

// 测试库升级
testImplementation 'junit:junit:4.13.2'                    // 4.12 → 4.13.2
androidTestImplementation 'androidx.test:runner:1.5.2'      // 1.1.1 → 1.5.2
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'  // 3.1.1 → 3.5.1
```

**升级收益**:
- ✅ 性能优化和 Bug 修复已完成应用
- ✅ Android 新版本 (API 34) 支持已增强
- ✅ 测试功能已提升，稳定性和覆盖率改善
- ✅ 长期维护成本降低

**未来更新规划**:
- 定期评估依赖更新 (建议每季度审查)
- 关注 Breaking Changes，适时升级至最新版本

#### 依赖冲突解决机制

**当前策略**:
1. **版本锁定**: 在 build.gradle 中明确指定依赖版本
2. **Jetifier 启用**: `android.enableJetifier=true` 确保 Support Library 兼容
3. **AndroidX 迁移**: `android.useAndroidX=true` 全面使用 AndroidX

**冲突处理建议**:
```groovy
// 在根项目 build.gradle 中添加依赖约束
dependencies {
    constraints {
        implementation('androidx.appcompat:appcompat') {
            version {
                strictly '1.6.1'
            }
        }
    }
}
```

### 2.3 Apache HttpClient 集成

**当前配置**:
```groovy
android {
    useLibrary 'org.apache.http.legacy'
}
```

**说明**:
- 使用 Apache HttpClient 3.x 进行网络通信
- `org.apache.http.legacy` 库提供向后兼容支持

**现代化建议**:
根据技术债务报告，建议迁移至 OkHttp + Retrofit:

```groovy
dependencies {
    // HTTP 客户端
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // REST API 框架
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // JSON 序列化
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

---

## 三、构建流程详解

### 3.1 本地开发构建

#### 基础构建命令

| 命令 | 说明 | 使用场景 |
|------|------|---------|
| `./gradlew clean` | 清理构建产物 | 构建前清理 |
| `./gradlew assembleDebug` | 编译 Debug 版本 | 日常开发 |
| `./gradlew assembleRelease` | 编译 Release 版本 | 发布准备 |
| `./gradlew test` | 运行单元测试 | 代码验证 |
| `./gradlew connectedCheck` | 运行设备测试 | 集成测试 |

#### 快速构建优化

```bash
# 使用 --build-cache 启用构建缓存
./gradlew assembleDebug --build-cache

# 使用 --parallel 并行构建 (多模块项目)
./gradlew assembleDebug --parallel

# 使用 --daemon 保持 Gradle Daemon 运行
./gradlew assembleDebug --daemon

# 组合优化选项
./gradlew assembleDebug --build-cache --parallel --daemon
```

### 3.2 Debug 构建配置

**特点**:
- 包含调试符号
- 未启用代码压缩 (minifyEnabled=false)
- 签名使用 debug.keystore

**输出位置**:
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/mapping/debug/         # ProGuard 映射文件 (如启用)
```

### 3.3 Release 构建配置

**当前配置**:
```groovy
buildTypes {
    release {
        minifyEnabled false              // 代码压缩 (当前未启用)
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 
                          'proguard-rules.pro'
        signingConfig signingConfigs.release  // 发布签名配置
    }
}
```

**优化建议**:

1. **启用 ProGuard 代码压缩**
   ```groovy
   buildTypes {
       release {
           minifyEnabled true            // 启用代码压缩
           shrinkResources true          // 启用资源压缩
           proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 
                             'proguard-rules.pro'
       }
   }
   ```

2. **ProGuard 规则配置** (app/proguard-rules.pro)
   ```proguard
   # Live2D SDK 保留规则
   -keep class com.live2d.** { *; }
   
   # 数据模型保留
   -keepclassmembers class jp.co.a_tm.moeyu.model.** {
       public <fields>;
       public <methods>;
   }
   
   # API 相关保留
   -keep class jp.co.a_tm.moeyu.api.** { *; }
   
   # Billing 模块保留
   -keep class jp.co.a_tm.moeyu.billing.** { *; }
   ```

### 3.4 构建产物管理

#### APK 输出结构

```
app/build/outputs/apk/
├── debug/
│   └── app-debug.apk              # Debug 版本 APK
└── release/
    ├── app-release-unsigned.apk   # 未签名的 Release APK
    └── app-release.apk            # 已签名的 Release APK (如配置签名)
```

#### 构建产物大小优化建议

1. **资源优化**
   ```groovy
   android {
       buildTypes {
           release {
               shrinkResources true    // 移除未使用资源
           }
       }
       
       // 启用 R8 代替 ProGuard (性能更好)
       buildFeatures {
           buildConfig true
       }
   }
   ```

2. **Shrink 配置**
   ```groovy
   // 使用 R8 进行代码和资源优化
   android {
       compileOptions {
           coreLibraryDesugaringEnabled true
       }
   }
   
   dependencies {
       coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.4'
   }
   ```

---

## 四、部署指南

### 4.1 APK 生成流程

#### 标准发布构建步骤

```bash
# 步骤 1: 清理之前的构建
./gradlew clean

# 步骤 2: 编译 Release 版本
./gradlew assembleRelease

# 步骤 3: 运行测试验证
./gradlew testReleaseUnitTest

# 步骤 4: 生成签名 APK (如配置 keystore)
./gradlew bundleRelease

# 步骤 5: 验证 APK
# 输出位置：app/build/outputs/apk/release/app-release.apk
```

#### 自动化构建脚本示例

**scripts/build-release.sh**:
```bash
#!/bin/bash

set -e  # 遇到错误立即退出

echo "==================================="
echo "ResurrectionMoeyu Release Build"
echo "==================================="

# 配置参数
BUILD_VERSION="1.0"
BUILD_DATE=$(date +%Y%m%d)
OUTPUT_DIR="build/distributions/${BUILD_DATE}"

# 创建输出目录
mkdir -p ${OUTPUT_DIR}

# 执行构建
echo "[1/4] Cleaning previous builds..."
./gradlew clean

echo "[2/4] Assembling Release APK..."
./gradlew assembleRelease

echo "[3/4] Running tests..."
./gradlew testReleaseUnitTest

echo "[4/4] Packaging artifacts..."
cp app/build/outputs/apk/release/app-release-unsigned.apk ${OUTPUT_DIR}/
cp app/build/outputs/mapping/release/mapping.txt ${OUTPUT_DIR}/

echo "==================================="
echo "Build completed successfully!"
echo "Output directory: ${OUTPUT_DIR}"
echo "APK: app-release-unsigned.apk"
echo "==================================="
```

### 4.2 签名配置

#### Keystore 管理

**发布签名配置** (app/build.gradle):

```groovy
android {
    // ... 其他配置
    
    signingConfigs {
        release {
            storeFile file("../keystore/release.keystore")
            storePassword "your_store_password"
            keyAlias "release_key"
            keyPassword "your_key_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
        }
    }
}
```

#### Keystore 生成命令

```bash
# 使用 keytool 生成 Keystore
keytool -genkey -v \
  -keystore release.keystore \
  -alias release_key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# 或使用 Android Studio GUI 工具
# File -> Project Structure -> Modules -> app -> Signs
```

**安全建议**:
- Keystore 文件应纳入版本控制但设置访问权限
- 密码通过环境变量或 CI/CD 密钥管理存储
- 定期备份 keystore 文件

#### 环境变量配置

**gradle.properties 中添加**:
```properties
# 签名配置 (生产环境建议使用 CI/CD 注入)
RELEASE_STORE_FILE=../keystore/release.keystore
RELEASE_STORE_PASSWORD=${RELEASE_STORE_PASSWORD}
RELEASE_KEY_ALIAS=release_key
RELEASE_KEY_PASSWORD=${RELEASE_KEY_PASSWORD}
```

### 4.3 多环境支持

#### 环境配置方案

**推荐采用 Product Flavors 实现多环境**:

```groovy
android {
    flavorDimensions "environment"
    
    productFlavors {
        development {
            dimension "environment"
            applicationId "jp.co.a_tm.moeyu.dev"
            versionName "1.0-dev"
            buildConfigField "String", "API_BASE_URL", "\"http://dev-api.moeapk.com/\""
            buildConfigField "boolean", "IS_DEBUG", "true"
        }
        
        staging {
            dimension "environment"
            applicationId "jp.co.a_tm.moeyu.staging"
            versionName "1.0-staging"
            buildConfigField "String", "API_BASE_URL", "\"http://staging-api.moeapk.com/\""
            buildConfigField "boolean", "IS_DEBUG", "true"
        }
        
        production {
            dimension "environment"
            applicationId "jp.co.a_tm.moeyu"
            versionName "1.0"
            buildConfigField "String", "API_BASE_URL", "\"http://api.moeapk.com/third_party/moeyu/\""
            buildConfigField "boolean", "IS_DEBUG", "false"
        }
    }
}
```

#### 环境构建命令

```bash
# Development 环境
./gradlew assembleDevelopmentDebug
./gradlew installDevelopmentDebug

# Staging 环境
./gradlew assembleStagingRelease
./gradlew installStagingRelease

# Production 环境
./gradlew assembleProductionRelease
./gradlew bundleProductionRelease  # 生成 AAB (Android App Bundle)
```

### 4.4 发布流程

#### Google Play 发布步骤

1. **准备阶段**
   - 确认版本号和变更日志
   - 生成 Release APK/AAB
   - 准备发布资源 (截图、描述等)

2. **内部测试**
   ```bash
   # 生成_signed APK
   ./gradlew assembleProductionRelease
   
   # 验证 APK
   ./gradlew lintProductionRelease
   ```

3. ** staged Rollout (分批发布)**
   - Phase 1: 内部测试 (5%)
   - Phase 2: 封闭测试 (20%)
   - Phase 3: 开放测试 (50%)
   - Phase 4: 全量发布 (100%)

4. **监控和反馈**
   - 监控崩溃率 (Crashlytics/Firebase)
   - 收集用户反馈
   - 性能指标跟踪

---

## 五、CI/CD集成方案

### 5.1 持续集成架构

#### 推荐 CI/CD 工具栈

| 组件 | 推荐工具 | 说明 |
|------|---------|------|
| **CI Server** | Jenkins / GitLab CI / GitHub Actions | 自动化构建和测试 |
| **Artifact Repository** | JFrog Artifactory / Google Artifact Registry | APK 存储和管理 |
| **测试平台** | Firebase Test Lab | 真机测试和兼容性验证 |
| **质量门控** | SonarQube | 代码质量和安全扫描 |
| **部署管理** | Fastlane | 自动化发布流程 |

#### GitHub Actions CI 配置示例

**.github/work/android-ci.yml**:

```yaml
name: Android CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

env:
  GRADLE_VERSION: '9.1.0'
  JAVA_VERSION: '8'

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout Repository
      uses: actions/checkout@v3
      
    - name: Set up JDK
      uses: actions/setup-java@v3
      with:
        java-version: ${{ env.JAVA_VERSION }}
        distribution: 'temurin'
        cache: 'gradle'
        
    - name: Grant execute permission for Gradle Wrapper
      run: chmod +x gradlew
      
    - name: Validate Gradle Wrapper
      uses: gradle/wrapper-validation-action@v1
      
    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
      with:
        cmdline-tools-version: 'latest'
        sdk-components: |
          platforms;android-28
          build-tools;28.0.3
          extra-android-m2repository
          
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle') }}
        
    - name: Run Lint Check
      run: ./gradlew lint
      
    - name: Build Debug
      run: ./gradlew assembleDevelopmentDebug
      
    - name: Run Unit Tests
      run: ./gradlew testDevelopmentUnitTest
      
    - name: Publish Test Results
      uses: dorny/paths-filter@v2
      with:
        filters: |
          test-results:
            - 'app/build/test-results/**'
      if: always()
      
    - name: Upload APK Artifact
      uses: actions/upload-artifact@v3
      with:
        name: app-debug-apk
        path: app/build/outputs/apk/development/debug/app-development-debug.apk
        
    - name: Code Quality Scan (SonarQube)
      uses: SonarSource/sonarqube-scan-action@v1
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      with:
        args: >
          -Dsonar.projectKey=ResurrectionMoeyu
          -Dsonar.sources=app/src/main
          -Dsonar.tests=app/src/test
          -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }}

  deploy-staging:
    needs: build-and-test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    
    steps:
    - name: Checkout Repository
      uses: actions/checkout@v3
      
    - name: Build Staging Release
      run: ./gradlew assembleStagingRelease
      
    - name: Deploy to Staging
      uses: wzieba/Fastlane-action@v3
      with:
        working_directory: .
        ios: false
        android: true
        ci_settings: true
        task: deploy_staging
        options: |
          default_output_directory: build/output
```

### 5.2 自动化测试集成

#### 测试策略分层

| 测试类型 | 工具框架 | 执行时机 | 目标覆盖率 |
|---------|---------|---------|-----------|
| **单元测试** | JUnit + Mockito | 每次提交 | ≥80% |
| **集成测试** | Espresso + MockWebServer | CI 流水线 | 核心流程 100% |
| **UI 测试** | Espresso + Firebase Test Lab | 每日构建 | 关键用户路径 |
| **性能测试** | Android Profiler + Perfetto | 发布前 | 基准对比 |

#### 自动化测试配置

**app/build.gradle 增强**:

```groovy
android {
    // ... 现有配置
    
    testOptions {
        unitTests.all {
            useJUnitPlatform()
            
            testLogging {
                events "passed", "skipped", "failed", "standardOut", "standardError"
                exceptionFormat = "full"
            }
            
            // Jacoco 代码覆盖率
            jacoco {
                toolVersion = "0.8.8"
            }
        }
        
        unitTests.returnDefaultValues = true
    }
}

dependencies {
    // 测试依赖增强
    testImplementation 'org.mockito:mockito-core:4.11.0'
    testImplementation 'org.assertj:assertj-core:3.24.2'
    
    // Jacoco 覆盖率
    testImplementation 'org.jacoco:org.jacoco.core:0.8.8'
}

// Jacoco 报告任务
tasks.register('testCoverage', JacocoReport) {
    dependsOn testDevelopmentUnitTest
    
    group = "Verification"
    description = "Generate Jacoco coverage reports for the project."
    
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}
```

### 5.3 部署流水线设计

#### Fastlane 自动化部署

**fastfile 配置示例**:

```ruby
default_platform(:android)

platform :android do
  desc "Build and deploy staging version"
  lane :deploy_staging do
    # 清理
    clean_project
    
    # 构建 Staging Release
    gradle(
      task: 'assembleStagingRelease',
      build_type: 'release'
    )
    
    # 运行测试
    gradle(
      task: 'testStagingReleaseUnitTest'
    )
    
    # 生成覆盖率报告
    gradle(
      task: 'testCoverage'
    )
    
    # 上传到 Firebase App Distribution
    firebase_app_distribution(
      app: "123456789",
      tracks: ["internal"],
      file_path: "app/build/outputs/apk/staging/release/app-staging-release-unsigned.apk",
      testers: ["team@example.com"]
    )
    
    # 生成变更日志
    changelog(
      output_path: "CHANGELOG.md"
    )
  end
  
  desc "Build and deploy production version"
  lane :deploy_production do |options|
    # 获取版本号
    version_code = options[:version_code] || get_gradle_version_code
    version_name = options[:version_name] || get_gradle_version_name
    
    say "Building version #{version_name} (code: #{version_code})", verbose: true
    
    # 签名构建
    gradle(
      task: 'assembleProductionRelease',
      properties: {
        'RELEASE_STORE_PASSWORD' => ENV['RELEASE_STORE_PASSWORD'],
        'RELEASE_KEY_PASSWORD' => ENV['RELEASE_KEY_PASSWORD']
      }
    )
    
    # 上传到 Google Play Console (Internal Track)
    supply(
      track: 'internal',
      release_status: 'draft',
      apk: "app/build/outputs/apk/production/release/app-production-release.apk",
      resolution_strategy: :immediate_upload,
      user_email: 'publisher@example.com'
    )
    
    # 通知团队
    slack(
      message: "✅ Release #{version_name} deployed to Internal Track",
      channel: '#releases',
      color: 'good'
    )
  end
end
```

#### 部署流程图

```
┌─────────────┐
│   Git Push   │
└──────┬──────┘
       ↓
┌─────────────┐
│  Code Build  │ → Lint Check
│  & Test      │ → Unit Tests
└──────┬──────┘ → Integration Tests
       ↓         → Coverage Report
┌─────────────┐
│  Artifact    │ → APK/AAB Generation
│  Creation    │ → Version Tagging
└──────┬──────┘
       ↓
┌─────────────┐
│  Staging     │ → Firebase App Distribution
│  Deployment  │ → Internal Testing
└──────┬──────┘
       ↓
┌─────────────┐
│  Production │ → Google Play (Internal Track)
│  Release    │ → Staged Rollout
└──────┬──────┘         → Monitoring & Analytics
       ↓
┌─────────────┐
│  Monitoring  │ → Crash Reports
│  & Feedback │ → Performance Metrics
└─────────────┘ → User Feedback Collection
```

---

## 六、优化建议

### 6.1 构建性能优化

#### Gradle 构建优化配置

**gradle.properties 增强**:

```properties
# 并行构建
org.gradle.parallel=true

# 构建缓存
org.gradle.caching=true

# Configuration Cache (Gradle 7.0+)
org.gradle.configuration-cache=true

# Daemon 优化
org.gradle.daemon=true

# JVM 内存配置 (根据实际硬件调整)
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8

# Android 特定优化
android.defaults.buildfeatures.buildconfig=true
android.nonTransitiveRClass=false
android.nonFinalResIds=false
```

#### 构建性能监控

**推荐工具**:
- **Gradle Build Dashboard**: `./gradlew buildDashboard`
- **Build Scan**: `./gradlew assembleDebug --scan`

**Build Scan 集成**:
```groovy
// 根项目 build.gradle
plugins {
    id 'com.gradle.build-scan' version '3.10.1'
}

gradleBuildScan {
    termsOfServiceUrl = 'https://gradle.com/terms-of-service'
    termsOfServiceAgree = 'yes'
    
    publishAlways()
    capture {
        fileFingerprints = true
        buildLogs = true
    }
}
```

### 6.2 资源管理优化

#### Assets 资源组织

**当前结构**:
```
assets/
├── model/moeyu.1024/      # Live2D 模型文件
│   ├── moeyu.moc
│   └── texture_*.png (4 张)
├── motion/                 # 动画配置
├── voice/                  # 日语语音资源
├── voice_cn/               # 中文语音资源
├── voice.json             # 语音映射表 (134KB)
└── water_*.png            # 背景图片 (4 张)
```

**优化建议**:

1. **资源压缩**
   ```groovy
   android {
       buildTypes {
           release {
               // 启用资源压缩
               shrinkResources = true
               
               // PNG 优化
               packagingOptions {
                   resources {
                       pickFirst 'META-INF/**'
                   }
               }
           }
       }
   }
   ```

2. **按需加载**
   - Live2D 模型支持延迟加载
   - 语音资源分片管理
   - 纹理图集优化 (Texture Atlas)

#### APK 大小优化策略

| 优化项 | 预期收益 | 实施优先级 |
|--------|---------|-----------|
| **启用 R8/ProGuard** | 减少 15-25% | P0 (高) |
| **资源压缩 (shrinkResources)** | 减少 10-15% | P0 (高) |
| **WebP 格式转换** | 减少 30% 图片体积 | P1 (中) |
| **代码拆分 (Dynamic Features)** | 基础包减少 20% | P2 (低) |

### 6.3 增量构建策略

#### 模块化构建优化

**当前架构**: 单体应用模块  
**建议演进**: 逐步模块化

```
ResurrectionMoeyu/
├── app/                    # 主应用模块
├── core/                   # 核心公共模块
│   ├── live2d-engine/     # Live2D 引擎封装
│   └── api-client/        # API 客户端库
├── feature/               # 功能模块
│   ├── gacha/             # 抽卡功能
│   ├── billing/           # 计费功能
│   └── collection/        # 收藏管理
└── data/                  # 数据层
    └── model/             # 数据模型
```

**增量构建收益**:
- 独立模块编译，提升构建速度
- 模块间依赖清晰，便于维护
- 支持按需打包 (Dynamic Feature Modules)

### 6.4 监控和告警配置

#### 构建质量门控

**SonarQube 集成规则**:

```groovy
// quality-profile 配置示例
sonarqube {
    properties {
        property 'sonar.qualitygate.wait', 'true'
        property 'sonar.qualitygate.timeout', '300'
        
        // 代码质量阈值
        property 'sonar.core.codeCoverageMinimum', '70'
        property 'sonar.java.suppressWarningsThreshold', '4'
        
        // 安全扫描
        property 'sonar.security.hotspots.review', 'true'
    }
}
```

#### 关键指标监控

| 指标类别 | 监控项 | 目标值 |
|---------|--------|--------|
| **构建性能** | 平均构建时间 | < 5 分钟 (全量) |
| **代码质量** | 单元测试覆盖率 | ≥ 80% |
| **APK 大小** | Release APK 体积 | < 30MB |
| **测试稳定性** | 测试通过率 | ≥ 95% |
| **部署频率** | 发布周期 | 每周 (Staging) / 每月 (Production) |

---

## 七、附录

### A. 常用构建命令速查表

| 操作 | 命令 | 说明 |
|------|------|------|
| **清理构建** | `./gradlew clean` | 删除所有构建产物 |
| **Debug 构建** | `./gradlew assembleDebug` | 编译 Debug APK |
| **Release 构建** | `./gradlew assembleRelease` | 编译 Release APK |
| **运行测试** | `./gradlew test` | 执行单元测试 |
| **代码检查** | `./gradlew lint` | 静态代码分析 |
| **生成文档** | `./gradlew javadoc` | 生成 API 文档 |
| **依赖分析** | `./gradlew dependencies` | 显示依赖树 |
| **构建扫描** | `./gradlew assembleDebug --scan` | 生成构建报告 |

### B. 环境变量参考

```bash
# Android SDK 路径
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Gradle 优化
export GRADLE_USER_HOME=/path/to/gradle-user-home

# 签名配置 (CI/CD)
export RELEASE_STORE_PASSWORD=your_store_password
export RELEASE_KEY_PASSWORD=your_key_password

# 构建输出
export BUILD_VARIANT=release
export BUILD_FLAVOR=production
```

### C. 故障排查指南

#### 常见问题处理

1. **依赖下载缓慢**
   ```properties
   # 使用国内镜像源 (阿里云)
   allprojects {
       repositories {
           maven { url 'https://maven.aliyun.com/repository/public' }
           maven { url 'https://maven.aliyun.com/repository/google' }
       }
   }
   ```

2. **内存不足**
   ```properties
   # 调整 JVM 参数
   org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
   ```

3. **构建缓存清理**
   ```bash
   # 清理 Gradle 缓存
   ./gradlew cleanBuildCache
   
   # 清理所有缓存
   rm -rf ~/.gradle/caches/
   ```

---

**文档版本**: v1.0  
**最后更新**: 2026-03-27  
**维护人**: dev-ops AGENT  
**下次审查日期**: 2026-04-27  

---

## 变更日志

| 日期 | 版本 | 变更内容 | 变更人 |
|------|------|---------|--------|
| 2026-03-27 | v1.0 | 初始版本 - 基于项目分析和架构文档生成 | dev-ops AGENT |

---

**相关文档**:
- [项目概览](ai/analysis/overview.md)
- [架构文档](ai/dev/project.md)
- [技术债务报告](ai/analysis/tech-debt.md)