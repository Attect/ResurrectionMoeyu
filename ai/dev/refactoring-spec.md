# ResurrectionMoeyu 代码规范化重构需求规格说明书

> 文档版本: 1.0
> 创建日期: 2026-04-10
> 创建者: requirement-analyst
> 任务ID: REFACTOR-001
> 项目: ResurrectionMoeyu (jp.co.a_tm.moeyu)

---

## 目录

1. [不规范模式清单](#1-不规范模式清单)
2. [优先级排序](#2-优先级排序)
3. [阶段1: 代码规范化具体修改范围](#3-阶段1代码规范化具体修改范围)
4. [阶段2: 在线功能本地化优化方案](#4-阶段2在线功能本地化优化方案)
5. [风险评估](#5-风险评估)

---

## 1. 不规范模式清单

### 1.1 资源ID引用方式不规范

#### 1.1.1 MotionEvent.getAction() 使用数字常量

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| BathActivity.java | 821 | `event.getAction() == 1` | 魔法数字1 | 改为 `event.getAction() == MotionEvent.ACTION_UP` |
| GatyaActivity.java | 368 | `case 0:` (switch内) | 魔法数字0 | 改为 `case MotionEvent.ACTION_DOWN:` |
| GatyaActivity.java | 380 | `case 1:` (switch内) | 魔法数字1 | 改为 `case MotionEvent.ACTION_UP:` |
| GatyaActivity.java | 407 | `case 2:` (switch内) | 魔法数字2 | 改为 `case MotionEvent.ACTION_MOVE:` |

#### 1.1.2 View.VISIBLE/INVISIBLE 使用数字常量

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| GatyaActivity.java | 409 | `getVisibility() == 0` | 魔法数字0 | 改为 `getVisibility() == View.VISIBLE` |
| GatyaActivity.java | 554 | `getVisibility() == 0` | 魔法数字0 | 改为 `getVisibility() == View.VISIBLE` |

#### 1.1.3 KeyEvent使用数字常量

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| TitleActivity.java | 192 | `keyCode != 4` | 魔法数字4 | 改为 `keyCode != KeyEvent.KEYCODE_BACK` |
| PreferenceActivity.java | 73 | `keyCode != 4` | 魔法数字4 | 改为 `keyCode != KeyEvent.KEYCODE_BACK` |

#### 1.1.4 Window Feature 使用数字常量

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| GatyaActivity.java | 534 | `requestWindowFeature(1)` | 魔法数字1 | 改为 `requestWindowFeature(Window.FEATURE_NO_TITLE)` |
| GatyaActivity.java | 608 | `requestWindowFeature(1)` | 魔法数字1 | 改为 `requestWindowFeature(Window.FEATURE_NO_TITLE)` |
| TweetDialog.java | 21 | `requestWindowFeature(1)` | 魔法数字1 | 改为 `requestWindowFeature(Window.FEATURE_NO_TITLE)` |
| ItemCollectionActivity.java | 105 | `requestWindowFeature(1)` | 魔法数字1 | 改为 `requestWindowFeature(Window.FEATURE_NO_TITLE)` |

#### 1.1.5 Toast Duration 使用数字常量

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| GatyaActivity.java | 168 | `Toast.makeText(..., 0)` | 魔法数字0 | 改为 `Toast.LENGTH_SHORT` |
| GatyaActivity.java | 637 | `Toast.makeText(..., 0)` | 魔法数字0 | 改为 `Toast.LENGTH_SHORT` |
| GatyaActivity.java | 645 | `Toast.makeText(..., 0)` | 魔法数字0 | 改为 `Toast.LENGTH_SHORT` |

#### 1.1.6 SQLiteOpenHelper版本使用硬编码数字

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| DatabaseOpenHelper.java | 39 | `super(context, DB_NAME, null, 1)` | 已定义DB_VER但未使用 | 改为 `super(context, DB_NAME, null, DB_VER)` |

#### 1.1.7 Intent Extra Key 定义方式不一致

**当前状态**: 三种不同的Key定义方式混用

| 定义方式 | 使用位置 | Key名 | 问题 |
|----------|----------|-------|------|
| Java静态常量 | BaseActivity.java:24 | `EXTRA_NEXT_ACTIVITY = "extra_next_activity"` | ✅ 正确 |
| R.string资源 | BaseActivity.java:180 | `getString(R.string.intent_scene)` | ⚠️ 与常量方式不一致 |
| R.string资源 | BaseActivity.java:195 | `getString(R.string.intent_event)` | ⚠️ 与常量方式不一致 |
| 硬编码字符串 | GatyaActivity.java:703 | `"extra_next_activity"` | ❌ 重复硬编码 |
| 硬编码字符串 | MainActivity.java:128 | `"extra_next_activity"` | ❌ 重复硬编码 |
| Java静态常量 | GatyaResultActivity.java:22 | `EXTRA_GACHA_RESULT = "extra_gacha_result"` | ⚠️ 与R.string方式不一致 |
| Java静态常量 | GatyaResultActivity.java:24 | `EXTRA_PRE_USER_DATA = "extra_pre_user_data"` | ⚠️ 与R.string方式不一致 |

**修复建议**: 统一为Java静态常量方式，移除R.string中的intent key定义，消除硬编码字符串。在BaseActivity中统一定义所有Intent Extra Key。

#### 1.1.8 Activity导航码使用硬编码数字

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| GatyaActivity.java | 703 | `data.putExtra("extra_next_activity", 5)` | 硬编码数字+字符串 | 改为使用 `EXTRA_NEXT_ACTIVITY` 和 `NEXT_ACTIVITY_GACHA_RESULT` |
| GatyaActivity.java | 706 | `setResult(-1, data)` | 硬编码-1 | 改为 `setResult(RESULT_OK, data)` |
| MainActivity.java | 128 | `data.getIntExtra("extra_next_activity", -1)` | 硬编码字符串 | 改为使用常量引用 |

#### 1.1.9 通过getResources().getIdentifier()动态获取资源ID

| 文件 | 行号 | 方法名 | 问题 | 修复建议 |
|------|------|--------|------|----------|
| BathActivity.java | 217-219 | `getResId()` | 动态资源查找，性能差且无编译时检查 | 对于固定名称模式（如item01~25），可保留但添加缓存；对于固定资源应改用R.drawable |
| GatyaActivity.java | 487 | 内联调用 | 动态获取coin图片 | 改为switch-case使用R.drawable |
| GatyaResultActivity.java | 176 | 内联调用 | 动态获取背景图片 | 建议保留动态方式（编号拼接场景），但提取为工具方法 |
| GatyaResultActivity.java | 187-188 | 内联调用 | 动态获取物品图片 | 同上，建议保留但提取工具方法 |
| ItemCollectionActivity.java | 161-163 | `getResID()` | 动态获取多种资源 | 建议保留但提取为静态工具方法 |

**说明**: 动态资源查找在物品编号拼接场景下是合理的，但应统一提取为工具类方法，并添加缓存机制。

---

### 1.2 代码编写不规范（逆向工程特征）

#### 1.2.1 硬编码的魔法值

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| MoeyuAPIClient.java | 146 | `i < 25` | 魔法数字25 | 改为 `i < UserData.MAX_ITEM_COUNT` |
| MoeyuAPIClient.java | 158 | `rate = 2` | 魔法数字2（铜币概率因子） | 定义为 `static final int RATE_BRONZE = 2` |
| MoeyuAPIClient.java | 163 | `rate = 3` | 魔法数字3（金币概率因子） | 定义为 `static final int RATE_GOLD = 3` |
| MoeyuAPIClient.java | 166 | `rate = 4` | 魔法数字4（白金币概率因子） | 定义为 `static final int RATE_PLATINUM = 4` |
| MoeyuAPIClient.java | 180 | `% 10` | 魔法数字10（概率计算范围） | 定义为 `static final int GACHA_RANDOM_RANGE = 10` |
| MoeyuAPIClient.java | 324 | `"local_secret"` | 硬编码密钥 | 提取为常量，阶段2中移除 |
| MoeyuAPIClient.java | 55 | `"MOEYU_001"` | 硬编码appId | 提取为常量，阶段2中移除 |
| MoeyuAPIClient.java | 247 | `"abcdef"` | 硬编码nonce | 提取为常量，阶段2中移除 |
| MoeyuAPIClient.java | 207 | `LAppAnimation.FLIP_START_FACE_Y` | **严重**: 用Live2D常量(值200)作为HTTP成功状态码200比较 | 改为 `statusCode == 200` |
| Decryption.java | 86 | `58` | 硬编码XOR密钥 | 定义为 `private static final int XOR_KEY = 58` |
| BathActivity.java | 591 | `254` | 烟雾效果阈值 | 定义为局部常量 |
| BathActivity.java | 593 | `5` | 烟雾步进值 | 定义为局部常量 |
| BathActivity.java | 204 | `i <= 25` | 魔法数字25 | 改为 `i <= UserData.MAX_ITEM_COUNT` |
| BathActivity.java | 869 | `"012"`, `"013"` | 硬编码特殊语音名 | 定义为静态常量 `VOICE_NO_MARK_1`, `VOICE_NO_MARK_2` |
| BathActivity.java | 292 | `new Rect(0, 0, 480, 480)` | 硬编码Live2D视图尺寸 | 定义为常量 `LIVE2D_VIEW_SIZE = 480` |
| SpecialEvent.java | 9,11,13,18 | `20`, `1`, `13`, `16` + 语音ID | 硬编码物品ID和语音名 | 提取为配置常量或JSON配置 |
| LovePoint.java | 4 | `{1, 3, 30}` | 硬编码经验值数组 | 已有，可改为带名称的常量 |
| LovePoint.java | 5 | `{0, 2, 14, 44, 100, 188}` | 硬编码等级阈值 | 已有，可添加注释说明 |
| ItemCollectionActivity.java | 30 | sScenes数组 | 硬编码场景映射 | 可保留但添加注释说明对应关系 |
| GatyaActivity.java | 561 | `> 99` | 魔法数字99 | 已有 `MAX_COIN_COUNT` 但未使用，应改为引用常量 |
| GatyaActivity.java | 602 | `== 99` | 魔法数字99 | 改为 `>= MAX_COIN_COUNT` |
| DatabaseOpenHelper.java | 294 | `i < 25` | 魔法数字25 | 使用 `ITEM_MAX_ROWS` 常量 |

#### 1.2.2 不规范的变量命名和方法命名

| 文件 | 行号 | 当前命名 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| GatyaActivity.java | 497 | `blonzButtonClick` | 拼写错误(bronze) | 改为 `onBronzeButtonClick` |
| SpecialEvent.java | 22 | `variableArray2List` | 命名不规范 | 改为 `toStringList` |
| BathActivity.java | 600 | `mN * -1` | 负数表达不规范 | 改为 `-mN` |
| MoeyuAPIClient.java | 227 | `new Integer(String.valueOf(...)).intValue()` | 过时的装箱方式 | 改为 `Integer.parseInt(...)` |
| DatabaseTableController.java | 44 | `isName(int id)` | 方法名`is`前缀暗示返回boolean但实际返回String | 改为 `getName(int id)` |
| DatabaseTableController.java | 49 | `isTitle(int id)` | 同上 | 改为 `getTitle(int id)` |
| DatabaseTableController.java | 53 | `isTerm(int id)` | 同上 | 改为 `getTerm(int id)` |
| DatabaseOpenHelper.java | 191 | `isName()` | 同上 | 改为 `getName()` |
| DatabaseOpenHelper.java | 208 | `isTitle()` | 同上 | 改为 `getTitle()` |
| DatabaseOpenHelper.java | 225 | `isTerm()` | 同上 | 改为 `getTerm()` |
| MoeyuAPIClient.java | 59-60 | `userDataFile`, `userData` | static变量使用小写命名 | 改为 `sUserDataFile`, `sUserData` 符合Java静态变量命名规范 |
| PreferencesHelper.java | 8 | `sp` | 命名过于简略 | 改为 `mPreferences` |

#### 1.2.3 逆向工程残留代码

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| GatyaActivity.java | 215 | `static /* synthetic */ float access$916(...)` | 编译器合成方法出现在源码中 | 移除，改用包级访问或直接调用 |
| TitleActivity.java | 53 | `InitializeDataTask(TitleActivity x0, InitializeDataTask x1)` | 合成构造函数 | 移除此构造函数 |
| BathActivity.java | 66-119 | `/* access modifiers changed from: private */` 等注释 | 逆向工程注释 | 移除所有此类注释 |
| GatyaActivity.java | 57-125 | `/* access modifiers changed from: ...*/` 注释 | 逆向工程注释 | 移除所有此类注释 |
| BaseTask.java | 28,44,52 | `/* access modifiers changed from: protected */` | 逆向工程注释 | 移除所有此类注释 |
| LAppAnimation.java | 111 | `/* access modifiers changed from: 0000 */` | 逆向工程注释 | 移除所有此类注释 |

#### 1.2.4 代码结构问题

| 文件 | 行号 | 问题 | 影响 | 修复建议 |
|------|------|------|------|----------|
| BathActivity.java | 全文件(1080行) | God Activity模式，承担UI/交互/语音/动画/物品/场景等过多职责 | 可维护性差 | 阶段2考虑提取VoicePlayer、SceneManager等辅助类 |
| BathActivity.java | 355-465 | onFinishSetup()方法110行，嵌套匿名类3层深 | 可读性差 | 提取事件处理、引导显示为独立方法 |
| BathActivity.java | 818-851 | onTouch()嵌套过深 | 可读性差 | 提取区域判断、链式事件处理为独立方法 |
| BathActivity.java | 474-500 | getBgmListener()匿名类过大 | 可读性差 | 提取为内部类或独立方法 |
| MoeyuAPIClient.java | 全文件(350行) | 本地逻辑与远程逻辑混合 | 职责不清 | 阶段2清理远程代码 |

#### 1.2.5 重复代码片段

| 重复模式 | 涉及文件/行号 | 重复次数 | 修复建议 |
|----------|--------------|----------|----------|
| PreferencesHelper setter模式 | PreferencesHelper.java:19-21, 33-35, 40-42, 49-51, 59-61, 69-71, 80-82, 90-92, 100-102 | 9次 | 提取通用putBoolean/putString私有方法 |
| DatabaseOpenHelper.update模式 | DatabaseOpenHelper.java:86-96, 98-108, 110-122, 124-136 | 4次 | 合并为更少的方法重载 |
| DatabaseOpenHelper数据库查询模式 | DatabaseOpenHelper.java:138-154, 156-171, 173-189, 191-206, 208-223, 225-240 | 6次 | 提取通用的queryOne/queryList方法 |
| Activity边距设置 | TitleActivity:109, GatyaResultActivity:60, ItemCollectionActivity:178 | 3次 | 在BaseActivity中添加通用方法 `applyScreenPadding(int viewId)` |
| Dialog创建模式 | BathActivity:339, GatyaActivity:533,607, ItemCollectionActivity:104, TweetDialog:19 | 5次 | 提取BaseActivity中的createDialog工具方法 |

#### 1.2.6 不规范的异常处理

| 文件 | 行号 | 当前做法 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| BathActivity.java | 270-273 | `e.printStackTrace()` 在onCreate中 | 未使用Logger | 改为 `Logger.e(TAG, "初始化语音管理器失败", e)` |
| BathActivity.java | 404-416 | 嵌套catch中4个异常都`e.printStackTrace()` | 未使用Logger | 改为Logger |
| BathActivity.java | 489-497 | getBgmListener中4个异常都`e.printStackTrace()` | 未使用Logger | 改为Logger |
| BathActivity.java | 839-847 | onTouch中4个异常都`e.printStackTrace()` | 未使用Logger | 改为Logger |
| DatabaseOpenHelper.java | 全文件 | `Log.e("ERROR", e.toString())` | 日志标签统一为ERROR，无文件上下文 | 改为 `Logger.e(TAG, "操作描述", e)` |
| MoeyuAPIClient.java | 88-91 | catch后仅Logger.e | 异常被吞没 | 评估是否需要向上传播 |
| TitleActivity.java | 243-251 | getFileDescriptor中4个异常都`e.printStackTrace()` | 未使用Logger | 改为Logger |
| VoiceManager.java | 43 | `Log.e("VOICE", ...)` | 未使用统一Logger | 改为Logger |

#### 1.2.7 已废弃API使用

| API | 涉及文件 | 行号 | 废弃级别 | 修复建议 |
|-----|----------|------|----------|----------|
| AsyncTask | api/task/*.java (BaseTask, LoginTask, GachaTask, SignupTask, BillingTask) | 全部 | API 30废弃 | 阶段2替换为协程或RxJava，阶段1仅标注 |
| Apache HttpClient | MoeyuAPIClient.java | 30-37, 202-206 | Android 9废弃 | 阶段2移除（随远程代码清理） |
| Camera API | CameraPreview.java | 全文件 | API 21废弃 | 保留，仅标注，非本次范围 |
| Google Billing v2 | billing/*.java | 全文件 | 已停用 | 阶段2处理 |
| SharedPreferences.commit() | PreferencesHelper.java | 20,35,41,51,61,71,82,91,101 | 非废弃但推荐apply() | 改为 `apply()` |
| ViewPager(deprecated) | AndroidManifest中使用 | - | API 21废弃 | 需确认具体使用位置 |

#### 1.2.8 数据一致性问题

| 问题 | 涉及文件 | 行号 | 影响 | 修复建议 |
|------|----------|------|------|----------|
| 双重UserData存储 | MoeyuAPIClient.java:127, UserDataManager.java:8 | `localUserData.dat` vs `userData.dat` | 两份数据可能不一致 | 阶段1: 确认写入顺序；阶段2: 合并为单一存储 |
| 初始铜币数量不一致 | CoinController.java:6 vs UserData.java:35 | INIT_BRONZE_COIN=3 vs bronzeCoin=10 | 常量与实际使用不符 | 统一为10（以createLocal为准），或CoinController.INIT_BRONZE_COIN改为10 |
| 静态userData无同步保护 | MoeyuAPIClient.java:60-61 | static UserData在AsyncTask中读写 | 线程安全隐患 | 阶段2: 添加同步机制或改为单线程访问 |

#### 1.2.9 逆规范的编码模式

| 文件 | 行号 | 当前代码 | 问题 | 修复建议 |
|------|------|----------|------|----------|
| BathActivity.java | 462 | `recommend.setTag("first")` | 使用字符串作为View tag | 改为布尔标记或使用view id |
| BathActivity.java | 1048 | `"first".equals(view.getTag())` | 配合上面的字符串tag | 统一修改 |
| BathActivity.java | 988 | `scene == Scene.bath_b ? Scene.bath_a.number : this.mScene.number` | 返回int但方法签名getMotionSuffix暗示返回后缀 | 改为返回String或重命名方法 |
| GatyaResultActivity.java:187-188 | `getResources().getIdentifier(..., "jp.co.a_tm.moeyu")` | 硬编码包名 | 改为 `getPackageName()` |
| MoeyuAPIClient.java | 246 | `new ArrayList()` | 未指定泛型 | 改为 `new ArrayList<>()` |
| BathActivity.java | 184 | `new LinkedHashMap()` | 未指定泛型 | 改为 `new LinkedHashMap<>()` |
| BathActivity.java | 97 | `new HashMap()` | 未指定泛型 | 改为 `new HashMap<>()` |

---

## 2. 优先级排序

### P0 - 紧急（影响功能正确性或安全性）

| 编号 | 问题 | 文件 | 影响 |
|------|------|------|------|
| P0-01 | HTTP状态码使用Live2D常量比较 | MoeyuAPIClient.java:207 | 功能逻辑错误，当前userBilling()可能因此返回异常 |
| P0-02 | 双重UserData存储可能导致数据不一致 | MoeyuAPIClient.java, UserDataManager.java | 数据丢失风险 |
| P0-03 | 静态userData无线程安全保护 | MoeyuAPIClient.java:60-61 | 并发异常风险 |
| P0-04 | 初始铜币数量常量与实际值不一致 | CoinController.java:6 vs UserData.java:35 | 代码意图混淆 |

### P1 - 高（影响代码质量和可维护性）

| 编号 | 问题 | 涉及范围 | 影响 |
|------|------|----------|------|
| P1-01 | Intent Extra Key硬编码 | GatyaActivity.java:703, MainActivity.java:128 | 导航可能因拼写错误失败 |
| P1-02 | 魔法数字（MotionEvent/KeyEvent/View/Window/Toast） | 6个文件共15处 | 可读性差，易出错 |
| P1-03 | 逆向工程残留代码（access$916、合成构造函数等） | GatyaActivity, TitleActivity等 | 代码异味 |
| P1-04 | e.printStackTrace()替代Logger | BathActivity, TitleActivity, VoiceManager | 日志系统不统一 |
| P1-05 | DatabaseOpenHelper方法命名不规范(is→get) | DatabaseOpenHelper, DatabaseTableController | 语义混淆 |
| P1-06 | 未使用泛型钻石运算符 | BathActivity, MoeyuAPIClient | 类型安全风险 |
| P1-07 | 硬编码包名 | GatyaResultActivity:187-188 | 重构时容易遗漏 |
| P1-08 | SharedPreferences使用commit()而非apply() | PreferencesHelper全文件 | 性能问题（同步写入） |

### P2 - 中（影响代码整洁度）

| 编号 | 问题 | 涉及范围 | 影响 |
|------|------|----------|------|
| P2-01 | 逆向工程注释残留 | BathActivity, GatyaActivity, BaseTask, LAppAnimation | 代码整洁度 |
| P2-02 | 魔法数字（物品数量25、概率因子、经验值等） | 多个文件 | 可读性 |
| P2-03 | 变量/方法命名不规范 | SpecialEvent, PreferencesHelper等 | 可读性 |
| P2-04 | 重复代码模式 | PreferencesHelper, DatabaseOpenHelper | 维护成本 |
| P2-05 | Activity边距设置重复 | TitleActivity, GatyaResultActivity, ItemCollectionActivity | 维护成本 |
| P2-06 | 通过getIdentifier动态查找资源（应统一工具化） | BathActivity, GatyaActivity, GatyaResultActivity, ItemCollectionActivity | 性能与规范 |
| P2-07 | onActivityResult使用硬编码switch数字 | MainActivity.java:130-170 | 可读性 |

### P3 - 低（优化建议）

| 编号 | 问题 | 涉及范围 | 影响 |
|------|------|----------|------|
| P3-01 | BathActivity God Activity模式 | BathActivity(1080行) | 阶段2处理 |
| P3-02 | 深层嵌套匿名类 | BathActivity.onFinishSetup | 阶段2处理 |
| P3-03 | Live2D视图尺寸硬编码 | BathActivity.java:292 | 低影响 |
| P3-04 | 烟雾效果魔数 | BathActivity.java:591-593 | 低影响 |

---

## 3. 阶段1: 代码规范化具体修改范围

> **原则**: 仅修改代码编写方式，不改变任何业务逻辑和功能行为。所有修改必须保证功能等价。

### 3.1 修改文件清单

以下按文件列出所有需要修改的项目，共涉及**15个文件**：

#### 3.1.1 MoeyuAPIClient.java（修改项最多）

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P0-01 | 207 | `LAppAnimation.FLIP_START_FACE_Y` → `200` | 低（该方法目前实际不走HTTP） |
| 2 | P1-06 | 246 | `new ArrayList()` → `new ArrayList<>()` | 无 |
| 3 | P2-02 | 146 | `25` → `UserData.MAX_ITEM_COUNT` | 无 |
| 4 | P2-02 | 158,163,166 | 提取概率因子为命名常量 | 无 |
| 5 | P2-02 | 180 | `% 10` 提取为命名常量 | 无 |
| 6 | P2-03 | 227 | `new Integer(String.valueOf(...)).intValue()` → `Integer.parseInt(...)` | 无 |
| 7 | P2-03 | 59-60 | `userDataFile`/`userData` → `sUserDataFile`/`sUserData` | 低（需同步修改引用） |
| 8 | P2-02 | 324,55,247 | 提取硬编码字符串为常量 | 无 |

#### 3.1.2 BathActivity.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-02 | 821 | `== 1` → `== MotionEvent.ACTION_UP` | 无 |
| 2 | P1-04 | 270-273, 404-416, 489-497, 839-847 | `e.printStackTrace()` → Logger调用 | 无 |
| 3 | P2-01 | 66,68-69,76,80-81,84-85,96-100,102-103,107-108,110-111,115-116,118-119 | 移除逆向工程注释 | 无 |
| 4 | P2-02 | 204 | `25` → `UserData.MAX_ITEM_COUNT` | 无 |
| 5 | P2-02 | 869 | `"012"`, `"013"` → 命名常量 | 无 |
| 6 | P1-06 | 97,184 | `new HashMap()` / `new LinkedHashMap()` → 加泛型 | 无 |
| 7 | P2-03 | 600 | `mN * -1` → `-mN` | 无 |

#### 3.1.3 GatyaActivity.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-01 | 703 | `"extra_next_activity"` → BaseActivity常量引用 | 无 |
| 2 | P1-01 | 703 | `5` → `NEXT_ACTIVITY_GACHA_RESULT` | 无 |
| 3 | P1-01 | 706 | `-1` → `RESULT_OK` | **需确认**: 当前用-1(resultCode非标准)，可能是有意设计 |
| 4 | P1-02 | 368,380,407 | MotionEvent数字 → 常量 | 无 |
| 5 | P1-02 | 409,554 | `== 0` → `== View.VISIBLE` | 无 |
| 6 | P1-02 | 534,608 | `requestWindowFeature(1)` → `Window.FEATURE_NO_TITLE` | 无 |
| 7 | P1-02 | 168,637,645 | Toast `0` → `Toast.LENGTH_SHORT` | 无 |
| 8 | P1-03 | 215 | 移除 `access$916` 方法，改为直接访问 | **中**: 需确认调用点 |
| 9 | P2-01 | 57-125 | 移除逆向工程注释 | 无 |
| 10 | P2-02 | 561 | `> 99` → `> MAX_COIN_COUNT` | 无 |
| 11 | P2-02 | 602 | `== 99` → `>= MAX_COIN_COUNT` | 无 |
| 12 | P2-03 | 497 | `blonzButtonClick` → `onBronzeButtonClick` | **需同步**: XML布局中的onClick引用 |

#### 3.1.4 MainActivity.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-01 | 128 | `"extra_next_activity"` → `EXTRA_NEXT_ACTIVITY` 常量 | 无 |
| 2 | P2-07 | 130-170 | switch数字添加注释或改为引用BaseActivity常量 | 无 |

#### 3.1.5 TitleActivity.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-02 | 192 | `4` → `KeyEvent.KEYCODE_BACK` | 无 |
| 2 | P1-03 | 53 | 移除合成构造函数 | **需确认**: 是否有调用 |
| 3 | P1-04 | 243-251 | `e.printStackTrace()` → Logger | 无 |
| 4 | P2-01 | 36 | 移除逆向工程注释 | 无 |

#### 3.1.6 PreferenceActivity.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-02 | 73 | `4` → `KeyEvent.KEYCODE_BACK` | 无 |
| 2 | P2-01 | 20 | 移除逆向工程注释 | 无 |

#### 3.1.7 DatabaseOpenHelper.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-02 | 39 | `1` → `DB_VER` | 无 |
| 2 | P1-05 | 191,208,225 | `isName/isTitle/isTerm` → `getName/getTitle/getTerm` | **中**: 需同步修改所有调用方 |
| 3 | P2-02 | 294 | `25` → `ITEM_MAX_ROWS` | 无 |
| 4 | P1-04 | 全文件 | `Log.e("ERROR", e.toString())` → Logger | 无 |

#### 3.1.8 DatabaseTableController.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-05 | 44,49,53 | `isName/isTitle/isTerm` → `getName/getTitle/getTerm` | **中**: 需同步修改调用方 |

#### 3.1.9 PreferencesHelper.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-08 | 全文件 | `commit()` → `apply()` | 低（异步写入，极低概率数据丢失） |
| 2 | P2-03 | 8 | `sp` → `mPreferences` | 无 |

#### 3.1.10 SpecialEvent.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P2-03 | 22 | `variableArray2List` → `toStringList` | 无 |
| 2 | P2-02 | 9-18 | 提取物品ID和语音列表为命名常量 | 无 |

#### 3.1.11 GatyaResultActivity.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-07 | 187-188 | `"jp.co.a_tm.moeyu"` → `getPackageName()` | 无 |

#### 3.1.12 ItemCollectionActivity.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-02 | 105 | `requestWindowFeature(1)` → `Window.FEATURE_NO_TITLE` | 无 |

#### 3.1.13 TweetDialog.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-02 | 21 | `requestWindowFeature(1)` → `Window.FEATURE_NO_TITLE` | 无 |

#### 3.1.14 VoiceManager.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P1-04 | 43 | `Log.e("VOICE", ...)` → Logger | 无 |

#### 3.1.15 Decryption.java

| 修改项 | 优先级 | 行号 | 修改内容 | 风险 |
|--------|--------|------|----------|------|
| 1 | P2-02 | 86 | `58` → 提取为常量 `XOR_KEY` | 无 |

### 3.2 不在阶段1修改范围内的文件

| 文件/目录 | 原因 |
|-----------|------|
| billing/ 目录 | 任务约束明确排除 |
| live2d/ 目录 | 任务约束明确排除 |
| CameraPreview.java | 使用已废弃Camera API，但修改不影响核心功能 |
| api/task/*.java | 使用已废弃AsyncTask，阶段2处理 |
| api/fragment/*.java | 与AsyncTask配合，阶段2处理 |

### 3.3 阶段1执行顺序建议

1. **第一批(P0)**: MoeyuAPIClient.java 的P0-01状态码修复、CoinController初始值确认
2. **第二批(P1-01+P1-02)**: Intent Key统一 + 魔法数字常量化（涉及最多文件，需同步修改）
3. **第三批(P1-03~P1-08)**: 逆向残留清理、Logger统一、命名修正
4. **第四批(P2)**: 其余代码整洁度改进
5. **验证**: 每批修改后执行构建验证和基本功能测试

---

## 4. 阶段2: 在线功能本地化优化方案

### 4.1 当前本地化现状评估

#### 4.1.1 已完成本地化（质量评估）

| 功能 | 文件 | 状态 | 质量评估 |
|------|------|------|----------|
| 用户注册 | MoeyuAPIClient.userSignUp() | ✅ 已本地化 | **良好**: 直接返回本地UserData |
| 用户登录/数据读取 | MoeyuAPIClient.userData() | ✅ 已本地化 | **良好**: 直接返回本地UserData，含每日登录奖励 |
| 扭蛋逻辑 | MoeyuAPIClient.userGatya() | ✅ 已本地化 | **一般**: 逻辑正确但概率因子硬编码，缺少经验值计算 |
| 数据持久化 | MoeyuAPIClient(构造函数+saveUserData+readUserData) | ✅ 已本地化 | **有问题**: 与UserDataManager双重存储 |

#### 4.1.2 未完成本地化

| 功能 | 文件 | 当前状态 | 说明 |
|------|------|----------|------|
| 支付验证 | MoeyuAPIClient.userBilling() | ❌ 仍远程调用 | HTTP POST到api.moeapk.com |
| 支付流程 | GatyaActivity + billing/ | ❌ 使用Billing v2 | Google已停用 |

#### 4.1.3 遗留的远程代码

| 代码 | 文件:行号 | 说明 |
|------|----------|------|
| BASE_URL/DEV/PROD/STAGING | MoeyuAPIClient.java:44-47 | 远程API地址 |
| Apache HttpClient相关 | MoeyuAPIClient.java:30-37 | 远程请求工具 |
| createBaseParams() | MoeyuAPIClient.java:245-254 | 远程请求参数构造 |
| checkErrorCode() | MoeyuAPIClient.java:222-243 | 远程错误处理 |
| createSignature() | MoeyuAPIClient.java:262-298 | 远程请求签名 |
| verifySignature() | MoeyuAPIClient.java:307-321 | 远程响应验证 |
| createBaseString() | MoeyuAPIClient.java:323-335 | 远程签名基础字符串 |
| responseToString() | MoeyuAPIClient.java:337-349 | 远程响应解析 |

### 4.2 阶段2改造方案

#### 4.2.1 MoeyuAPIClient 远程代码清理

**目标**: 移除所有远程API相关代码，仅保留本地化逻辑。

**具体操作**:

1. **移除远程API URL相关常量**（第44-47行）
2. **移除Apache HttpClient相关import和代码**（第30-37行，第196-243行）
3. **移除签名相关方法**（createSignature, verifySignature, createBaseString, responseToString, createBaseParams）
4. **移除checkErrorCode方法**
5. **改造userBilling()**: 改为本地实现——根据购买类型直接增加对应用户货币
6. **简化构造函数**: 移除无用的BASE_URL相关初始化
7. **统一数据存储**: 合并localUserData.dat和userData.dat为单一存储点

**保留内容**:
- userSignUp() - 本地注册
- userData() - 本地数据读取（含每日奖励）
- userGatya() - 本地扭蛋（优化概率常量）
- userBilling() - 改为本地实现
- saveUserData() / readUserData() - 本地持久化
- GachaCoin枚举

**userBilling()本地化方案**:

```java
// 本地化后的userBilling（伪代码）
public UserData userBilling(String productId) {
    switch (productId) {
        case "gold_coin_3":
            userData.setGoldCoin(roundingNumber(userData.getGoldCoin() + 3));
            break;
        case "gold_coin_10":
            userData.setGoldCoin(roundingNumber(userData.getGoldCoin() + 10));
            break;
        case "platinum_coin_1":
            userData.setPlatinumCoin(roundingNumber(userData.getPlatinumCoin() + 1));
            break;
    }
    saveUserData();
    return userData;
}
```

#### 4.2.2 Billing模块处理方案

**目标**: 移除废弃的Google Play Billing v2，改为本地免费获取货币的方式。

**方案选择**（需用户确认）:

| 方案 | 描述 | 改动量 | 用户体验 |
|------|------|--------|----------|
| 方案A: 直接赠送 | 定期自动获得各种货币 | 小 | 好 |
| 方案B: 保留购买UI但本地处理 | 保留界面，点击直接获得货币 | 中 | 保持原体验 |
| 方案C: 升级Billing Library | 使用BillingClient v7 | 大 | 可发布到Play Store |

**推荐方案B**: 保留购买UI但改为本地处理。用户点击"购买"后直接获得对应货币（userBilling本地化），无需实际支付。这样保持原有UI交互体验不变。

**涉及修改**:
- billing/ 目录: 可整体保留但不再使用，或直接删除
- GatyaActivity.java: 将BillingService调用改为本地userBilling调用
- BillingFragment.java: 不再需要
- BillingTask.java: 改为调用本地userBilling

#### 4.2.3 数据存储统一方案

**问题**: 当前存在两份UserData序列化文件。

| 存储点 | 文件路径 | 管理类 | 写入时机 |
|--------|----------|--------|----------|
| localUserData.dat | cacheDir/ | MoeyuAPIClient | 每次API操作后 |
| userData.dat | fileDir/ | UserDataManager | BaseTask.storeUserData() |

**方案**: 
1. 统一使用 `fileDir/userData.dat`（UserDataManager管理）
2. MoeyuAPIClient中移除独立的文件读写逻辑
3. MoeyuAPIClient构造函数不再读取/创建文件，改为由外部传入UserData
4. 移除 `localUserData.dat` 相关代码

#### 4.2.4 扭蛋逻辑优化

**当前问题**:
- 概率因子(rate=2/3/4)硬编码
- 缺少经验值计算（LovePoint.getPoint定义了但未在userGatya中调用）

**优化方案**:
1. 将概率因子定义为命名常量
2. 在userGatya中增加经验值计算和等级提升逻辑
3. 调用LovePoint.getPoint()根据货币类型获取经验值

#### 4.2.5 AsyncTask替换（可选，视改动范围）

**评估**: AsyncTask在API 30废弃，但本项目minSdk=19，当前仍可正常工作。建议评估是否在阶段2一并替换。

- 如替换: 使用简单的Thread+Handler或java.util.concurrent，避免引入RxJava等重依赖
- 如不替换: 仅在代码中标注@Deprecated注解

### 4.3 阶段2执行顺序建议

1. **第一步**: 统一数据存储（消除双重存储问题）
2. **第二步**: userBilling()本地化 + 移除远程代码
3. **第三步**: 扭蛋概率因子和经验值计算优化
4. **第四步**: Billing模块处理（根据用户选择的方案）
5. **验证**: 完整功能测试

---

## 5. 风险评估

### 5.1 高风险修改项

| 修改项 | 风险等级 | 风险描述 | 缓解措施 |
|--------|----------|----------|----------|
| P1-01: Intent Key统一 | 中 | GatyaActivity中resultCode=-1可能是有意设计（非RESULT_OK） | 仔细分析onActivityResult逻辑，确认-1的真实意图 |
| P1-03: 移除access$916 | 中 | 该方法被其他代码引用，移除需找到所有调用点 | 搜索 `access$916` 确认所有调用 |
| P1-05: is→get命名修改 | 中 | 多处调用需同步修改 | 全局搜索替换，编译验证 |
| P2-03: blonzButtonClick重命名 | 中 | XML布局中onClick属性引用了方法名 | 同步修改对应layout XML |
| 阶段2: 数据存储统一 | 高 | 可能导致已有用户数据丢失 | 提供数据迁移逻辑 |
| 阶段2: Billing处理 | 高 | 影响核心游戏经济系统 | 充分测试货币获取和消耗流程 |

### 5.2 安全修改项

| 修改项 | 风险等级 | 说明 |
|--------|----------|------|
| P0-01: 状态码修复 | 低 | userBilling()当前未被调用（Billing v2已停用），修复不影响运行功能 |
| P1-08: commit→apply | 低 | apply()异步写入，极端情况下应用崩溃可能丢失最后一次写入 |
| 魔法数字常量化 | 无 | 纯替换，逻辑不变 |
| 移除逆向注释 | 无 | 仅删除注释 |
| Logger统一 | 无 | 仅改变日志输出方式 |

### 5.3 不应修改的文件

| 文件/目录 | 原因 |
|-----------|------|
| live2d/ 整个目录 | Live2D SDK相关，改动风险极高 |
| billing/ 整个目录 | 阶段1排除，阶段2评估后再处理 |
| assets/ 目录 | 资源文件，不涉及代码规范 |
| res/ 目录 | 资源文件，仅XML onClick属性需同步 |

### 5.4 验证策略

1. **阶段1验证**: 每批修改后执行 `gradlew assembleDebug` 确保编译通过
2. **阶段1功能验证**: 编译通过后安装到设备，测试以下核心流程:
   - 应用启动 → 视频播放 → 标题页 → 登录
   - 浴室互动 → 触摸语音播放 → 场景切换
   - 扭蛋 → 结果展示 → 返回浴室
   - 收藏 → 物品/语音/笔记查看
3. **阶段2验证**: 在阶段1基础上额外测试:
   - 货币获取和消耗
   - 数据持久化（杀进程后重启数据不丢失）
   - 每日登录奖励

---

## 附录: 问题统计

### 按类别统计

| 类别 | 数量 |
|------|------|
| 资源ID引用不规范 | 25处 |
| 魔法值/硬编码 | 30处 |
| 逆向工程残留 | 15处 |
| 命名不规范 | 12处 |
| 异常处理不规范 | 20处 |
| 重复代码 | 5个模式 |
| 已废弃API使用 | 5个 |
| 数据一致性问题 | 3处 |
| **总计** | **约115处** |

### 按优先级统计

| 优先级 | 数量 |
|--------|------|
| P0 紧急 | 4项 |
| P1 高 | 8项 |
| P2 中 | 7项 |
| P3 低 | 4项 |

---

## 变更日志

| 日期 | 变更类型 | 变更描述 | 变更人 |
|------|----------|----------|--------|
| 2026-04-10 | 初始版本 | 基于源码扫描创建完整重构需求规格说明书，识别约115处不规范模式 | requirement-analyst |
