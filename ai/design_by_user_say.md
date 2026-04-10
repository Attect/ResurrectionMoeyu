# ResurrectionMoeyu 功能规格说明书

> 文档版本: 1.0  
> 创建日期: 2026-04-10  
> 创建者: requirement-analyst  
> 任务ID: ANALYSIS-001  
> 项目: ResurrectionMoeyu (jp.co.a_tm.moeyu)

---

## 目录

1. [启动流程模块](#1-启动流程模块)
2. [浴室互动模块](#2-浴室互动模块)
3. [物品系统模块](#3-物品系统模块)
4. [扭蛋/抽卡模块](#4-扭蛋抽卡模块)
5. [收藏系统模块](#5-收藏系统模块)
6. [好感度/等级系统模块](#6-好感度等级系统模块)
7. [事件系统模块](#7-事件系统模块)
8. [设置功能模块](#8-设置功能模块)
9. [支付系统模块](#9-支付系统模块)
10. [社交分享模块](#10-社交分享模块)
11. [数据持久化模块](#11-数据持久化模块)
12. [语音系统模块](#12-语音系统模块)
13. [导航路由模块](#13-导航路由模块)

---

## 1. 启动流程模块

### 1.1 开场视频播放

- **功能描述**: 应用启动后播放a_tm公司片头视频动画，播放完毕后自动跳转至标题页
- **触发条件**: 应用冷启动（MainActivity首次onResume）
- **业务规则**:
  - 仅首次onResume时播放，后续从其他Activity返回不重复播放
  - 通过`MoeyuApplication.setFirstRun(true)`标记首次运行
  - 视频资源: `R.raw.ateam_moive`
  - 播放完成后回调`onCompletion`触发跳转到TitleActivity
- **涉及页面/类**: `MainActivity`（第67-72行, 第91-105行）
- **涉及数据**: `MoeyuApplication.mFirstRun`（内存状态标记）
- **完成状态**: ✅ 已完成

### 1.2 屏幕适配

- **功能描述**: 计算非16:9屏幕的修正值，确保Live2D等内容在非标准比例屏幕上正确显示
- **触发条件**: MainActivity.onCreate时计算
- **业务规则**:
  - 公式: `FIX_HEIGHT = 屏幕高度 - (屏幕宽度 / 9 * 16)`
  - 结果为全局静态变量`MainActivity.FIX_HEIGHT`
  - 被多个Activity用于设置View的上下Padding
- **涉及页面/类**: `MainActivity`（第79-85行）
- **涉及数据**: `MainActivity.FIX_HEIGHT`（静态全局变量）
- **完成状态**: ✅ 已完成

### 1.3 首次运行数据初始化

- **功能描述**: 首次安装后进行数据初始化，包括语音文件解密和数据库创建
- **触发条件**: `PreferencesHelper.isInitBoot()`返回true时
- **业务规则**:
  - 通过`InitializeDataTask`异步执行（AsyncTask子类）
  - 初始化内容: 调用`Decryption.execute()`解密所有语音文件
  - 初始化完成后设置`InitBoot=false`
  - 初始化期间显示加载指示器
  - 初始化完成后执行登录流程
- **涉及页面/类**: `TitleActivity.InitializeDataTask`（第42-95行）, `Decryption`
- **涉及数据**: SharedPreferences `InitBoot`键（默认true）
- **完成状态**: ✅ 已完成

### 1.4 用户注册/登录

- **功能描述**: 应用启动后自动进行用户注册或登录（已本地化，不依赖远程API）
- **触发条件**: 进入TitleActivity.onResume
- **业务规则**:
  - 检查是否有已保存的UserData（通过`UserDataManager.isSavedUserData()`）
  - 有保存数据 → 调用`LoginFragment.login()`登录
  - 无保存数据 → 调用`SignupFragment.signup()`注册
  - 注册时自动创建本地用户: userId="local", bronzeCoin=10, goldCoin=0, platinumCoin=0, level=1, exp=0
  - 登录成功后播放标题语音"002_2b.ogg"
  - 登录成功后检查是否有每日奖励（bonus标记）
- **涉及页面/类**: `TitleActivity`（第222-347行）, `MoeyuAPIClient`, `LoginFragment`, `SignupFragment`
- **涉及数据**: 本地文件`localUserData.dat`（UserData序列化对象）
- **完成状态**: ✅ 已完成（已本地化）

### 1.5 DeepLink入口

- **功能描述**: 支持通过`moeyu://moe-yu.com`协议直接打开标题页
- **触发条件**: 外部Intent使用moeyu scheme
- **业务规则**:
  - AndroidManifest中配置了TitleActivity的intent-filter
  - scheme: `moeyu`, host: `moe-yu.com`
- **涉及页面/类**: `TitleActivity`, `AndroidManifest.xml`（第61-70行）
- **涉及数据**: 无额外数据
- **完成状态**: ✅ 已完成

---

## 2. 浴室互动模块

### 2.1 Live2D角色展示

- **功能描述**: 在浴室场景中展示Live2D角色模型，支持动画和渲染
- **触发条件**: 进入BathActivity
- **业务规则**:
  - 使用Live2D SDK（live2d_android.jar）
  - 通过`LAppLive2DManager`管理模型生命周期
  - 模型视图尺寸: 480x480
  - 背景色: 黑色（Color.BLACK）
  - 模型加载完成后通过`FinishListener`回调通知
  - 加载期间显示指示器，完成后隐藏
- **涉及页面/类**: `BathActivity`（第277-304行）, `LAppLive2DManager`, `LAppGLView`, `LAppRenderer`
- **涉及数据**: assets/live2d/（Live2D模型资源）
- **完成状态**: ✅ 已完成

### 2.2 场景系统

- **功能描述**: 浴室分为4个场景视角，支持自动/手动切换
- **触发条件**: 进入浴室后自动切换，或用户手动选择
- **业务规则**:
  - 4个场景枚举（Scene.java）:
    - `bath_a`(0): 浴缸全景A
    - `head`(1): 头部特写
    - `body`(2): 身体特写
    - `bath_b`(3): 浴缸全景B
  - 自动切换: 定时器触发，按 bath_a → head → body → bath_b 顺序循环
  - 定时器间隔: `R.integer.scene_change_interval_ms`
  - 手动切换: 通过菜单中的"Skip"按钮
  - 场景切换动画: 白屏淡出 → 切换 → 白屏淡入
  - 每次切换场景时更换背景音乐
  - 各场景有独立的BGM配置:
    - bath_a/bath_b: se354/se355/se356/se357（随机）
    - body: se361/se363/se364（随机）
    - head: se359/se360/se361（随机）
  - 各场景可切换的目标场景不同（通过Skip按钮配置）
- **涉及页面/类**: `BathActivity`（第124-168行, 第658-676行, 第711-808行）, `Scene.java`
- **涉及数据**: `Scene`枚举, `R.integer.scene_change_interval_ms`, BGM资源
- **完成状态**: ✅ 已完成

### 2.3 触摸区域检测

- **功能描述**: 根据用户在Live2D模型上的触摸位置判断触摸区域，触发对应语音和动画
- **触发条件**: 用户触摸Live2D视图区域
- **业务规则**:
  - 触摸坐标归一化为百分比坐标（0.0-1.0）
  - 6个触摸区域枚举（Region.java）: face, head, brest, belly, arm, none
  - 每个区域由左上角和右下角两个坐标点定义矩形范围
  - 区域检测采用优先级匹配（按LinkedHashMap插入顺序）:
    - face: (0.27, 0.394) → (0.666, 0.564)
    - head: (0.083, 0.196) → (0.854, 0.577)
    - brest: (0.229, 0.656) → (0.75, 0.866)
    - belly: (0.333, 0.853) → (0.625, 1.0)
    - arm: (0.187, 0.603) → (0.791, 1.0)
    - none: (0.0, 0.0) → (1.0, 1.0)（兜底）
  - head场景额外配置arm区域: (0.0, 0.196) → (1.0, 0.656)
  - 触摸时若正在播放语音则忽略
  - 触摸none区域不触发语音
  - 仅在MotionEvent.ACTION_UP时处理触摸
- **涉及页面/类**: `BathActivity`（第176-251行, 第818-851行）, `Region.java`
- **涉及数据**: `mRegions` HashMap（Scene → LinkedHashMap<Region, PointF[]>）
- **完成状态**: ✅ 已完成

### 2.4 触摸语音播放

- **功能描述**: 根据触摸区域、当前场景、使用的物品和等级选择语音文件并播放
- **触发条件**: 用户触摸非none区域且无正在播放的语音
- **业务规则**:
  - 语音选择优先级: 链式事件（SpecialEvent）> 普通语音（VoiceManager）
  - 普通语音选择逻辑（VoiceManager.getVoiceName）:
    1. 从voice.json读取配置: scene → region → item → level
    2. 如果item配置有"0"键，使用"0"配置（通用配置，不区分等级）
    3. 否则使用当前等级配置
    4. 在配置的语音列表中按概率随机选择
    5. 概率为累加制（probability值递增），random范围1-99
  - 语音文件播放流程: reset → setDataSource → prepare → start
  - 首次播放的语音自动标记为已打开（VoiceTableController.update）
  - 标记后更新UI上的语音数量显示
  - 弹出"New"提示2秒后自动消失
  - 语音012和013不标记为"新语音"（特殊处理）
- **涉及页面/类**: `BathActivity`（第863-882行）, `VoiceManager`（第50-63行）, `VoiceTableController`
- **涉及数据**: `voice.json`（语音配置文件）, VoiceTable数据库, 解密后的.ogg文件
- **完成状态**: ✅ 已完成

### 2.5 触摸动画触发

- **功能描述**: 触摸时触发对应的Live2D动画
- **触发条件**: 与语音播放同步
- **业务规则**:
  - 动画名称 = voiceName + motionSuffix
  - motionSuffix根据场景确定: bath_b场景使用bath_a的编号，其他场景使用自身编号
  - 通过`LAppAnimation.startTouchMotion()`触发
- **涉及页面/类**: `BathActivity`（第874-878行, 第988-990行）, `LAppAnimation`
- **涉及数据**: Live2D模型动画数据
- **完成状态**: ✅ 已完成

### 2.6 烟雾效果

- **功能描述**: 浴室场景中持续显示烟雾/热气动画效果
- **触发条件**: 进入BathActivity.onResume时启动
- **业务规则**:
  - 在独立线程中运行，每100ms更新一次
  - 透明度从255递减5至0，然后反向从-255递增至0，循环往复
  - 同时更新Padding值产生位移效果
  - Activity暂停时停止线程（mFlag=false）
  - 烟雾ImageView: `R.id.smoke`
- **涉及页面/类**: `BathActivity`（第578-609行）
- **涉及数据**: 无持久化数据
- **完成状态**: ✅ 已完成

### 2.7 背景音乐播放

- **功能描述**: 浴室场景中循环播放水声等环境音效
- **触发条件**: 进入BathActivity时播放初始BGM，之后循环随机切换
- **业务规则**:
  - 初始BGM: `R.raw.se353`
  - 每首BGM播放完毕后随机选择当前场景配置的BGM继续播放
  - 切换场景时播放场景对应的过渡BGM:
    - bath_a/bath_b/body → se362
    - head → se358
  - BGM使用MediaPlayer，播放完毕释放再创建新的MediaPlayer
- **涉及页面/类**: `BathActivity`（第264-266行, 第474-500行, 第762-768行）
- **涉及数据**: BGM资源文件（se353-se364）
- **完成状态**: ✅ 已完成

### 2.8 物品使用效果

- **功能描述**: 在浴室中使用物品时，在触摸位置显示物品图标并播放淡出动画
- **触发条件**: 触摸时已选中非空物品（mSelectedItem != bath_item_null）
- **业务规则**:
  - 物品图标ImageView: `R.id.use_item`
  - 位置: 以触摸点为中心显示
  - 动画: `R.anim.item_fadeout`（淡出效果）
- **涉及页面/类**: `BathActivity`（第997-1006行）
- **涉及数据**: 物品drawable资源
- **完成状态**: ✅ 已完成

### 2.9 AR相机模式

- **功能描述**: 开启后使用设备后置摄像头作为浴室背景，Live2D角色叠加在相机画面上
- **触发条件**: 设置中开启AR相机且非事件模式
- **业务规则**:
  - 使用旧版Camera API（android.hardware.Camera）
  - 相机预览作为FrameLayout最底层
  - 设置`mRenderer.isAr = true`使Live2D背景透明
  - onPause时停止并移除相机预览
  - 事件模式下不启用AR（PreferenceActivity.isEnableCamera检查）
- **涉及页面/类**: `BathActivity`（第568-572行）, `CameraPreview`, `PreferenceActivity`（第60-62行）
- **涉及数据**: SharedPreferences `Camera`键（默认false）
- **完成状态**: ✅ 已完成（使用已废弃的Camera API）

### 2.10 首次浴室引导

- **功能描述**: 首次进入浴室时显示操作引导提示
- **触发条件**: `PreferencesHelper.isInitBath()`为true
- **业务规则**:
  - 显示推荐引导图: `R.drawable.recommend_plate01a`
  - 第一次点击切换为: `R.drawable.recommend_plate01b`
  - 第二次点击关闭引导并设置`InitBath=false`
- **涉及页面/类**: `BathActivity`（第459-464行, 第1047-1055行）
- **涉及数据**: SharedPreferences `InitBath`键（默认true）
- **完成状态**: ✅ 已完成

### 2.11 语音数量显示

- **功能描述**: 在浴室页面显示已解锁语音数量/总语音数量
- **触发条件**: 进入浴室和每次播放新语音时更新
- **业务规则**:
  - 格式: XXX/YYY（百位/十位/个位分离显示）
  - 分子: VoiceTableController.countOpened()
  - 分母: VoiceTableController.countRows()
  - 使用图片数字（bath_voice_num00~09）显示各位
- **涉及页面/类**: `BathActivity`（第889-922行）
- **涉及数据**: VoiceTable数据库
- **完成状态**: ✅ 已完成

---

## 3. 物品系统模块

### 3.1 物品总览

- **功能描述**: 共25种物品（编号1-25），通过扭蛋获取，在浴室中使用
- **业务规则**:
  - 物品存储在UserData.items列表中（List<Integer>）
  - 物品ID范围: 1-25
  - `isItemGet(id)`: 检查物品是否已获取
  - `isItemComplete()`: 检查是否已收集全部25个物品
  - 每个物品关联一个推荐使用的场景（ItemCollectionActivity.sScenes数组）
- **涉及页面/类**: `UserData`（第17行, 第135-143行）, `ItemCollectionActivity`（第30行）
- **涉及数据**: UserData.items, ItemTable数据库
- **完成状态**: ✅ 已完成

### 3.2 物品图标显示

- **功能描述**: 已获取的物品显示彩色图标，未获取的显示灰色图标
- **触发条件**: 物品选择网格、物品收藏列表中显示
- **业务规则**:
  - 已获取: `itemXX`（彩色图标）
  - 未获取: `gray_itemXX`（灰色图标）
  - 占位物品: `bath_item_null`（索引0，表示不使用物品）
- **涉及页面/类**: `BathActivity.createItemList()`（第200-208行）, `ItemCollectionActivity`
- **涉及数据**: drawable资源
- **完成状态**: ✅ 已完成

### 3.3 物品选择

- **功能描述**: 在浴室中通过网格视图选择要使用的物品
- **触发条件**: 点击浴室菜单中的物品按钮
- **业务规则**:
  - 以Dialog方式弹出物品网格（3列布局）
  - 物品0为"不使用物品"选项
  - 仅已获取的物品可选择（`isItemGet`检查或position==0）
  - 选择后更新右上角物品图标并关闭Dialog
- **涉及页面/类**: `BathActivity`（第311-334行, 第645-649行）
- **涉及数据**: `mItemList`, `mSelectedItem`
- **完成状态**: ✅ 已完成

### 3.4 物品场景关联

- **功能描述**: 每个物品有推荐使用的场景，物品收藏页点击"去浴室"会跳转到对应场景
- **触发条件**: 物品收藏页点击已获取物品的"浴室"按钮
- **业务规则**:
  - 场景映射表（sScenes数组）:
    - 物品1 → bath_a, 物品2 → body, 物品3-18 → bath_a, 物品19 → head
    - 物品20 → body, 物品21 → bath_a, 物品22 → body, 物品23 → bath_a
    - 物品24 → body, 物品25 → bath_a
- **涉及页面/类**: `ItemCollectionActivity`（第30行, 第248-260行）
- **涉及数据**: `sScenes`静态数组
- **完成状态**: ✅ 已完成

---

## 4. 扭蛋/抽卡模块

### 4.1 扭蛋入口

- **功能描述**: 从标题页或浴室菜单进入扭蛋页面
- **触发条件**: 点击标题页/浴室菜单中的扭蛋按钮
- **业务规则**:
  - 进入时检查是否有已保存的UserData
  - 无数据则直接finish退出
  - 自动执行login获取最新用户数据
  - 显示三种货币数量
- **涉及页面/类**: `GatyaActivity`（第228-247行）
- **涉及数据**: UserData（货币数量）
- **完成状态**: ✅ 已完成

### 4.2 货币系统

- **功能描述**: 三种货币（铜币/金币/白金币），用于不同等级的扭蛋
- **业务规则**:
  - 铜币(BRONZE): 初始10枚，每日登录+1，最大99枚
  - 金币(GOLD): 初始0枚，通过购买获得，最大99枚
  - 白金币(PLATINUM): 初始0枚，通过购买获得，最大99枚
  - 货币上限: CoinController.MAX_COIN = 99
  - 货币显示: 文本形式直接显示数量
  - 自动选择逻辑: 有铜币→铜币，否则有金币→金币，否则有白金币→白金币，否则无
- **涉及页面/类**: `CoinController`, `MoeyuAPIClient`（第32-43行）, `GatyaActivity`（第328-344行）
- **涉及数据**: UserData.bronzeCoin/goldCoin/platinumCoin
- **完成状态**: ✅ 已完成

### 4.3 扭蛋操作

- **功能描述**: 选择货币类型后旋转转盘进行扭蛋抽卡
- **触发条件**: 选择货币并顺时针旋转转盘360度以上后松手
- **业务规则**:
  - 选择货币: 点击对应货币按钮（铜/金/白金）
  - 仅在未投入硬币时（!isInputCoinStart）可切换货币
  - 转盘操作: 触摸并顺时针旋转，角度累加至>=360度触发抽卡
  - 硬币投入动画: `R.anim.input_coin`，投完后隐藏
  - 转盘旋转: 通过Matrix.setRotate实现
  - 角度计算: 使用atan2计算两个触摸点相对转盘中心的角度差
  - 完成旋转后播放扭蛋弹出动画 `R.anim.output_gachapon`
  - 动画结束后执行抽卡任务
- **涉及页面/类**: `GatyaActivity`（第362-435行, 第469-524行）
- **涉及数据**: mSelectedCoin, mCurrentDegrees, mTotteMatrix
- **完成状态**: ✅ 已完成

### 4.4 抽卡概率机制

- **功能描述**: 不同货币对应不同的获得新物品概率
- **触发条件**: 扭蛋动画完成后
- **业务规则**:
  - 抽卡逻辑（MoeyuAPIClient.userGatya）:
    1. 优先从未拥有的物品中抽取
    2. 如果全部25个物品已拥有，从已拥有列表中抽取
    3. 铜币: rate=2（20%获得新物品）
    4. 金币: rate=3（30%获得新物品）
    5. 白金币: rate=4（40%获得新物品），但如果用户物品列表为空则100%获得
    6. rate > randomResult(0-9) → 获得新物品
    7. 否则获得已有物品的重复
    8. 扣除对应货币1枚
    9. 获得新物品时添加到items列表
  - 返回GachaResult: 包含更新后的UserData和抽到的物品ID
- **涉及页面/类**: `MoeyuAPIClient.userGatya()`（第144-175行）, `MoeyuAPIClient.getGachaResult()`（第178-194行）
- **涉及数据**: UserData.items, GachaResult
- **完成状态**: ✅ 已完成（已本地化）

### 4.5 抽卡结果展示

- **功能描述**: 展示抽到的物品、经验条变化、等级提升、事件触发
- **触发条件**: 扭蛋完成后自动跳转
- **业务规则**:
  - 显示抽到的物品图片和名称
  - 新物品显示"NEW"标记，已有物品不显示
  - 背景图根据新/旧物品不同（result_back_new_X / result_back_X）
  - 经验条动画: 使用TranslateAnimation，从上次经验位置滑动到当前经验位置
  - 等级提升提示: 比较前后UserData.level，如果提升且`isInitLevelUp`为true则显示
  - 物品完成提示: 当所有25个物品集齐时显示
  - 事件提示: 通过EventController.pop()检查是否有待处理事件
  - 首次扭蛋引导: `isInitGatyaResult`为true时显示
- **涉及页面/类**: `GatyaResultActivity`（第54-251行）, `EventController`
- **涉及数据**: GachaResult, UserData(前后对比), SharedPreferences
- **完成状态**: ✅ 已完成

### 4.6 首次扭蛋引导

- **功能描述**: 首次进入扭蛋页面时显示操作引导
- **触发条件**: `PreferencesHelper.isInitGatya()`为true
- **业务规则**:
  - 显示引导对话框
  - 引导完成后设置`InitGatya=false`
- **涉及页面/类**: `GatyaActivity`
- **涉及数据**: SharedPreferences `InitGatya`键（默认true）
- **完成状态**: ✅ 已完成

---

## 5. 收藏系统模块

### 5.1 收藏房间总览

- **功能描述**: 收藏系统的导航页面，提供到各子收藏页面的入口
- **触发条件**: 点击标题页的收藏房间按钮
- **业务规则**:
  - 三个子页面入口: 物品收藏、语音收藏、桃璃的房间
  - 标题按钮返回上一级
- **涉及页面/类**: `CollectionRoomActivity`
- **完成状态**: ✅ 已完成

### 5.2 物品收藏列表

- **功能描述**: 展示全部25个物品的获取状态，支持查看详情和跳转使用
- **触发条件**: 从收藏房间或浴室菜单进入
- **业务规则**:
  - 以ListView展示，每行3个物品
  - 第一行特殊: 仅显示物品1（左右位置为空）
  - 已获取物品显示彩色图标和名称，未获取显示灰色
  - 点击物品弹出详情Dialog:
    - 已获取: 显示物品名称、大图、描述文字、"浴室"按钮
    - 未获取: 显示灰色名称、灰色图标、默认描述
    - 点击"浴室"按钮跳转到对应场景使用该物品
  - 底部显示收集进度: `已获取数/25`
- **涉及页面/类**: `ItemCollectionActivity`
- **涉及数据**: ItemTable数据库, `sScenes`场景映射
- **完成状态**: ✅ 已完成

### 5.3 语音收藏列表

- **功能描述**: 展示已解锁的语音列表，支持分类查看和播放
- **触发条件**: 从收藏房间或浴室菜单进入
- **业务规则**:
  - 三个分类标签页:
    - 普通语音(NORMAL): 编号1-108，共108条
    - 物品语音(ITEM): 编号109-230，共122条
    - 事件语音(EVENT): 编号231-274，共44条
  - 已解锁语音显示标题文本，未解锁显示"？？？？？？"
  - 点击已解锁语音播放对应.ogg文件
  - 底部显示收集进度: `已解锁数/总数`
  - 使用MediaPlayer播放语音
  - onPause时释放播放器
- **涉及页面/类**: `VoiceCollectionActivity`
- **涉及数据**: VoiceTable数据库, 解密后的.ogg文件
- **完成状态**: ✅ 已完成

### 5.4 笔记/日记收藏

- **功能描述**: 展示角色的秘密日记，通过达成条件解锁
- **触发条件**: 从桃璃房间或底部导航进入
- **业务规则**:
  - 日记解锁条件（奇偶交替）:
    - 奇数编号: 已解锁语音数 >= 该笔记要求的阈值
    - 偶数编号: 已解锁物品数 >= 该笔记要求的阈值
  - 前置条件: 笔记N-2必须已解锁才能显示N的解锁进度
  - 显示内容:
    - 左侧: 日记编号和锁定图标
    - 右侧: 解锁条件数字（语音数或物品数）
    - 底部: 阅读图标
  - 已解锁的日记点击阅读图标可查看内容（diary_XX drawable图片）
  - 日记内容通过NoteTable记录opened状态
  - 点击阅读后将笔记标记为已读（NoteTableController.update）
- **涉及页面/类**: `NoteCollectionActivity`（第259-285行）
- **涉及数据**: NoteTable数据库, ItemTable/VoiceTable计数, drawable资源（diary_XX）
- **完成状态**: ✅ 已完成

### 5.5 桃璃的房间

- **功能描述**: 展示桃璃角色的房间，根据日记解锁状态动态装饰
- **触发条件**: 从收藏房间进入
- **业务规则**:
  - 房间装饰与日记解锁关联:
    - 笔记3解锁 → 显示room_right01_02
    - 笔记6解锁 → 显示room_right01_03
    - 笔记18解锁 → 显示room_right01_04
    - 笔记10解锁 → 显示room_right01_05
    - 笔记14解锁 → 显示room_right01_06
  - 底部导航: 笔记、浴室、扭蛋、收藏、标题
- **涉及页面/类**: `MomorisRoomActivity`
- **涉及数据**: NoteTable数据库, drawable资源
- **完成状态**: ✅ 已完成

---

## 6. 好感度/等级系统模块

### 6.1 等级体系

- **功能描述**: 用户好感度等级系统，共6个等级
- **业务规则**:
  - 等级经验阈值（LovePoint.TERM数组）:
    - 等级1: 0点
    - 等级2: 2点
    - 等级3: 14点
    - 等级4: 44点
    - 等级5: 100点
    - 等级6: 188点（满级）
  - 等级显示: 浴室页面左上角显示当前等级图片
  - 等级图标: bath_level_num01~06
- **涉及页面/类**: `LovePoint`（第3-14行）, `BathActivity`（第308行, 第963-980行）
- **涉及数据**: UserData.level, UserData.exp
- **完成状态**: ✅ 已完成

### 6.2 经验值获取

- **功能描述**: 通过使用不同类型的硬币扭蛋获取不同数量的经验值
- **业务规则**:
  - 铜币扭蛋: +1经验值
  - 金币扭蛋: +3经验值
  - 白金币扭蛋: +30经验值
  - 经验值在MoeyuAPIClient.userGatya中扣币但未显式计算
  - 等级提升判定: LovePoint.currentLevel(exp)
- **涉及页面/类**: `LovePoint.getPoint()`（第52-60行）, `MoeyuAPIClient`
- **涉及数据**: LovePoint.IN_COIN数组
- **完成状态**: ⚠️ 部分完成（LovePoint定义了经验值获取规则，但实际调用链待确认）

### 6.3 等级解锁内容

- **功能描述**: 达到特定等级后解锁新的语音和事件
- **业务规则**:
  - 等级影响语音选择: VoiceManager根据当前等级选择不同的语音配置
  - voice.json中每个物品配置支持"0"（通用）和等级编号（1-6）的key
  - 等级2-6各触发一个等级提升事件
- **涉及页面/类**: `VoiceManager.getVoiceName()`（第50-63行）, `EventController`（第28-45行）
- **涉及数据**: voice.json, EventData
- **完成状态**: ✅ 已完成

---

## 7. 事件系统模块

### 7.1 等级提升事件

- **功能描述**: 达到特定等级时触发剧情事件，播放对应语音序列
- **触发条件**: 扭蛋后等级提升（EventController.checkEvent检测）
- **业务规则**:
  - 5个等级事件类型: Level2, Level3, Level4, Level5, Level6
  - 触发条件: 用户等级 >= 对应等级 且 事件的最后一条语音未被解锁
  - 每个事件关联一组语音文件（来自strings.xml的event_levelup_X数组）
  - 事件触发后在浴室页面播放:
    - 显示事件标题图片（spa_event_lv02~06）
    - 按顺序播放事件语音队列
    - 语音播放完毕后淡出事件标题
  - 物品完成事件(Complete): 所有25个物品收集完毕时触发
- **涉及页面/类**: `EventController`（第21-51行）, `EventData`（第15-38行）, `BathActivity.onFinishSetup()`（第355-465行）
- **涉及数据**: EventData（Type, VoiceList）, strings.xml中的event_levelup_X数组
- **完成状态**: ✅ 已完成

### 7.2 特殊物品事件

- **功能描述**: 特定物品在特定场景+区域的组合触发链式语音事件
- **触发条件**: 在浴室中用特定物品触摸特定区域
- **业务规则**:
  - 4个特殊事件（SpecialEvent.get）:
    - 物品20 + bath_b场景 + belly区域 → 语音["227","228","229","230"]
    - 物品1 + head场景 + head区域 → 语音["143","222","223","201"]
    - 物品13 + bath_a场景 + face区域 → 语音["157","186","187","188"]
    - 物品16 + bath_b场景 + face区域 → 语音["206","207","208"]
  - 链式事件播放完后才恢复正常语音选择
- **涉及页面/类**: `SpecialEvent`（第8-19行）, `BathActivity.onTouch()`（第831-838行）
- **涉及数据**: UserData, Scene, Region
- **完成状态**: ✅ 已完成

### 7.3 事件控制器

- **功能描述**: 管理事件队列，在扭蛋结果页和浴室页之间传递事件
- **业务规则**:
  - EventController构造时自动检查待触发事件（checkEvent）
  - 提供push/pop接口管理事件队列
  - 在GatyaResultActivity中pop事件，若有则跳转到浴室携带事件数据
  - 事件数据通过Intent传递（Serializable）
- **涉及页面/类**: `EventController`, `GatyaResultActivity.showEventViews()`（第229-251行）
- **涉及数据**: EventData列表
- **完成状态**: ✅ 已完成

---

## 8. 设置功能模块

### 8.1 AR相机开关

- **功能描述**: 控制浴室中AR相机功能的启用/关闭
- **触发条件**: 进入设置页面
- **业务规则**:
  - 使用ToggleButton控件
  - 状态存储在SharedPreferences `Camera`键（默认false）
  - 设置变更立即生效
  - 从浴室进入设置时返回浴室会应用新设置
- **涉及页面/类**: `PreferenceActivity`（第29-41行）, `PreferencesHelper`（第64-72行）
- **涉及数据**: SharedPreferences `Camera`键
- **完成状态**: ✅ 已完成

### 8.2 服务条款

- **功能描述**: 显示服务条款网页
- **触发条件**: 标题页菜单中选择"利用规约"
- **业务规则**:
  - 打开浏览器: http://www.moe-yu.com/kiyaku.html
- **涉及页面/类**: `TitleActivity.showTerms()`（第202-204行）
- **涉及数据**: 外部URL
- **完成状态**: ⚠️ 存根（外部链接可能已失效）

### 8.3 咨询邮件

- **功能描述**: 发送咨询邮件
- **触发条件**: 标题页菜单中选择"お問い合わせ"
- **业务规则**:
  - 使用Intent.ACTION_SENDTO打开邮件应用
  - 收件人: moe-yu_support@a-tm.co.jp
  - 主题: "お問い合わせ {userId}"
- **涉及页面/类**: `TitleActivity.sendInquiryMail()`（第209-215行）
- **涉及数据**: 邮件地址、用户ID
- **完成状态**: ⚠️ 存根（邮件地址可能已失效）

---

## 9. 支付系统模块

### 9.1 Google Play支付

- **功能描述**: 通过Google Play Billing购买金币和白金币
- **触发条件**: 扭蛋页面点击购买按钮
- **业务规则**:
  - 使用Google Play Billing v2（已废弃）
  - 三个商品SKU:
    - `gold_coin_3`: 购买3枚金币
    - `gold_coin_10`: 购买10枚金币
    - `platinum_coin_1`: 购买1枚白金币
  - 购买金币时检查是否超过上限99枚
  - 购买流程: 弹出选择对话框 → 选择数量 → 请求购买 → 购买完成回调 → 刷新货币数量
  - 白金币: 直接购买1枚
  - 购买成功: Toast提示"購入完了！"
  - 购买不支持: Toast提示"お客様の端末では購入ができません"
  - 超过上限: Toast提示"これ以上コインを購入できません"
- **涉及页面/类**: `GatyaActivity`（第531-665行）, `BillingService`, `PurchaseObserver`, `ResponseHandler`
- **涉及数据**: Google Play Billing SKU, UserData货币字段
- **完成状态**: ❌ 废弃（Billing v2 API已被Google Play停用）

---

## 10. 社交分享模块

### 10.1 Twitter分享

- **功能描述**: 分享游戏进度到Twitter
- **触发条件**: 标题页或物品收藏页点击Twitter按钮
- **业务规则**:
  - 使用TweetDialog弹出分享确认框
  - 首次使用显示对话框，之后根据勾选的"自动分享"决定是否直接分享
  - 分享内容格式: `{URL}{等级文案}{收集进度}%{Hashtag}{URL}`
    - 等级文案: 从tweet_level_array数组获取
    - 收集进度: 物品收集百分比
    - Hashtag: R.string.tweet_hash
    - URL: R.string.tweet_url
  - 通过Intent打开浏览器跳转Twitter分享页面
  - 可勾选"下次自动分享"
- **涉及页面/类**: `TweetDialog`（第15-83行）
- **涉及数据**: UserData.level, ItemTableController.getOpenedPercent(), SharedPreferences `InitTweet`/`Twitter`键
- **完成状态**: ⚠️ 部分完成（功能完整但外部Twitter链接可能需要更新）

---

## 11. 数据持久化模块

### 11.1 SQLite数据库

- **功能描述**: 本地SQLite数据库存储收藏状态
- **业务规则**:
  - 数据库名: `collection.db`，版本1
  - 三张表:
    - **ItemTable**: _id, name, opened — 25行（预填物品编号01-25）
    - **VoiceTable**: _id, name, opened, title — 动态行数（从CSV初始化）
    - **NoteTable**: _id, name, opened, term — 动态行数（从CSV初始化）
  - 初始化: 首次创建数据库时从CSV文件加载语音标题和笔记数据
  - voice表数据源: `R.raw.voice`（CSV格式: 文件名,标题）
  - note表数据源: `R.raw.note`（CSV格式: 阈值数字）
  - opened字段: 初始为"false"，解锁后更新为"true"
- **涉及页面/类**: `DatabaseOpenHelper`, `CSV`, `VoiceTitle`
- **涉及数据**: collection.db数据库
- **完成状态**: ✅ 已完成

### 11.2 UserData序列化存储

- **功能描述**: 用户核心数据通过Java序列化存储到本地文件
- **业务规则**:
  - 存储路径: `{cacheDir}/localUserData.dat`
  - 使用Java ObjectOutputStream/ObjectInputStream序列化
  - 包含: userId, bronzeCoin, goldCoin, platinumCoin, items列表, exp, level, state, bonus, lastLoginTime
  - 每次API操作后自动保存
- **涉及页面/类**: `UserData.store()`/`restore()`, `MoeyuAPIClient`, `UserDataManager`
- **涉及数据**: localUserData.dat文件
- **完成状态**: ✅ 已完成

### 11.3 每日登录奖励

- **功能描述**: 每日首次登录赠送1枚铜币
- **触发条件**: 读取UserData时检测日期变化
- **业务规则**:
  - 比较当前日期与lastLoginTime的日期（年月日）
  - 非同一天: 设置bonus=true, bronzeCoin+1
  - 同一天: 设置bonus=false
  - 在标题页登录成功后检查bonus标记显示奖励图标
  - 奖励图标点击后隐藏
- **涉及页面/类**: `MoeyuAPIClient.readUserData()`（第94-123行）, `TitleActivity.executeLogin()`（第312-314行）
- **涉及数据**: UserData.lastLoginTime, UserData.bonus
- **完成状态**: ✅ 已完成

### 11.4 语音文件解密

- **功能描述**: 首次启动时将加密的.okk文件解密为.ogg文件
- **触发条件**: 首次运行数据初始化（InitBoot=true）
- **业务规则**:
  - 加密算法: XOR 58（每个字节与58异或）
  - 日文语音: assets/voice/*.okk → {fileDir}/XXX.ogg
  - 中文语音: assets/voice_cn/*.okk → {fileDir}/XXX_cn.ogg
  - 已解密的文件不重复解密（检查文件是否已存在）
  - 使用512字节缓冲区读取
- **涉及页面/类**: `Decryption`（第44-66行, 第76-89行）
- **涉及数据**: assets/voice/, assets/voice_cn/（.okk文件）
- **完成状态**: ✅ 已完成

---

## 12. 语音系统模块

### 12.1 语音配置管理

- **功能描述**: 通过voice.json配置文件管理场景/区域/物品/等级到语音文件的映射
- **业务规则**:
  - voice.json层级结构: scene → region → item → level → [{probability, voice}]
  - 语音选择: 根据场景+区域+物品+等级查找配置，按概率随机选择
  - 概率为累加制（random值递减直到<=probability）
  - "0"等级键为通用配置（不区分等级）
  - 支持中文语音优先: useCN=true时优先查找XXX_cn.ogg
- **涉及页面/类**: `VoiceManager`（第21-63行）
- **涉及数据**: assets/voice.json
- **完成状态**: ✅ 已完成

### 12.2 多语言语音支持

- **功能描述**: 支持日文和中文两种语音
- **业务规则**:
  - VoiceManager.useCN默认为true
  - 中文语音文件命名: {voiceName}_cn.ogg
  - 日文语音文件命名: {voiceName}.ogg
  - 优先播放中文语音，若不存在则回退到日文
- **涉及页面/类**: `VoiceManager.getVoiceFileDescripter()`（第38-48行）
- **涉及数据**: assets/voice/（日文）, assets/voice_cn/（中文）
- **完成状态**: ✅ 已完成

---

## 13. 导航路由模块

### 13.1 单Activity路由模式

- **功能描述**: 所有页面跳转通过MainActivity中转，使用startActivityForResult模式
- **业务规则**:
  - BaseActivity定义了页面跳转码:
    | 编码 | 目标页面 |
    | --- | --- |
    | -1 | 退出应用 |
    | 0 | 标题页 (TitleActivity) |
    | 1 | 扭蛋页 (GatyaActivity) |
    | 2 | 浴室页 (BathActivity) |
    | 3 | 设置页 (PreferenceActivity) |
    | 4 | 浴室来的设置页 |
    | 5 | 扭蛋结果页 (GatyaResultActivity) |
    | 6 | 收藏房间 (CollectionRoomActivity) |
    | 7 | 物品收藏 (ItemCollectionActivity) |
    | 8 | 语音收藏 (VoiceCollectionActivity) |
    | 9 | 桃璃房间 (MomorisRoomActivity) |
    | 10 | 笔记收藏 (NoteCollectionActivity) |
  - 每个Activity通过setResult返回下一页面编码
  - MainActivity.onActivityResult根据编码启动对应Activity
  - 跳转时传递Intent数据（事件数据、场景、物品等）
- **涉及页面/类**: `BaseActivity`, `MainActivity.onActivityResult()`（第116-172行）
- **涉及数据**: EXTRA_NEXT_ACTIVITY常量
- **完成状态**: ✅ 已完成

### 13.2 全屏沉浸模式

- **功能描述**: 所有Activity隐藏状态栏实现全屏显示
- **触发条件**: BaseActivity.onCreate时设置
- **业务规则**:
  - 使用SYSTEM_UI_FLAG_IMMERSIVE + LAYOUT_FULLSCREEN + FULLSCREEN
  - 同时设置FLAG_FULLSCREEN窗口标志
- **涉及页面/类**: `BaseActivity.hideSystemUI()`（第116-124行）
- **涉及数据**: 无
- **完成状态**: ✅ 已完成

---

## 功能完成状态汇总

| 状态 | 数量 | 说明 |
| --- | --- | --- |
| ✅ 已完成 | 36 | 功能完整可运行 |
| ⚠️ 部分完成 | 2 | 功能存在但可能有细节问题 |
| ⚠️ 存根 | 2 | 功能存在但依赖外部资源可能已失效 |
| ❌ 废弃 | 1 | Google Play Billing v2已停用 |
| **合计** | **41** | |

---

## 变更日志

| 日期 | 变更类型 | 变更描述 | 变更人 |
| --- | --- | --- | --- |
| 2026-04-10 | 初始版本 | 基于源码逆向分析创建完整功能规格说明书，覆盖13个功能模块41项功能 | requirement-analyst |
