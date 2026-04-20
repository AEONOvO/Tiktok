# TikTok Android

这是一个基于 Kotlin 开发的高仿 TikTok (抖音) Android 应用程序。项目采用 MVVM 架构，实现了短视频应用的核心功能，包括视频推荐、播放、个人中心、点赞和评论等。

## ✨ 主要功能 (Features)

* **首页推荐 (Home Feed)**: 沉浸式短视频浏览体验（仿抖音上下滑动切换）。
* **视频播放 (Video Player)**: 基于 ExoPlayer (Media3) 实现的高性能视频播放。
* **同城页面 (Same City)**: 展示同城视频瀑布流列表。
* **个人中心 (User Profile)**: 用户信息展示，支持查看个人作品和喜欢的视频。
* **互动功能**:
  * **点赞**: 支持双击点赞和点赞动画 (Lottie)。
  * **评论**: 底部弹窗式评论列表。
* **图片编辑**: 集成 UCrop 实现头像选择与裁剪功能。
* **AI 对话 (H5)**: 内置 H5 页面直连模型 API，对话配置可在页面内完成，Home 页悬浮入口打开 WebView。

## 🛠 技术栈 (Tech Stack)

* **语言**: [Kotlin](https://kotlinlang.org/)
* **最低兼容 (Min SDK)**: Android 7.0 (API 24)
* **架构模式**: MVVM (Model-View-ViewModel)
* **UI 框架**:
  * XML Layouts & ViewBinding
  * [Material Design 3](https://m3.material.io/)
  * ViewPager2 (上下滑动视频流) & RecyclerView
  * [ImmersionBar](https://github.com/gyf-dev/ImmersionBar) (沉浸式状态栏适配)
* **多媒体**:
  * [ExoPlayer (Media3)](https://developer.android.com/media/media3/exoplayer): 专业级视频播放器
  * [Glide](https://github.com/bumptech/glide): 高效图片加载库
  * [Lottie](https://github.com/airbnb/lottie-android): 矢量动画库
  * [UCrop](https://github.com/Yalantis/uCrop): 图片裁剪
* **异步与数据**:
  * [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines) & Flow
  * [RxJava2](https://github.com/ReactiveX/RxJava)
  * [Room Database](https://developer.android.com/training/data-storage/room): 本地数据库
  * Gson: JSON 解析
* **Jetpack 组件**: Lifecycle, ViewModel, LiveData, Fragment
* **内置 H5**: WebView + assets（JSBridge 通信）

## 📂 项目结构 (Project Structure)

```
com.example.tiktok
├── application    # 全局 App 初始化
├── base           # 基础类封装 (BaseBindingActivity, BaseAdapter 等)
├── data           # 数据层 (Entity, DAO, Repository)
├── ui             # 视图层
│   ├── activity   # 包含 MainActivity, VideoPlayActivity
│   ├── adapter    # RecyclerView 适配器
│   ├── fragment   # 包含 Recommend, SameCity, PersonalHome 等
│   └── view       # 自定义 View 组件 (如 Dialog)
├── utils          # 通用工具类
└── viewmodel      # 业务逻辑 ViewModel
```

## 🤖 H5 AI 对话页面

H5 页面位于 `app/src/main/assets/ai/`，可在应用内 WebView 打开。

**入口位置**

* 首页 `HomeFragment` 的悬浮按钮打开 WebView
* 入口代码：`HomeFragment.setupAiFloat()` -> `AiWebViewActivity`

**配置项**

* Base URL：模型提供方的 API 根地址（如 `https://api.minimaxi.com`）
* Model：模型名称（如 `MiniMax-M2.7`）
* API Key：在页面内手动输入

**注意**

* 仅建议用于测试或内网环境，前端直连会暴露 Key。
* 可勾选“记住我”将配置保存到本地 `localStorage`。

**实现细节（对应代码）**

* Activity：`AiWebViewActivity` 加载 `file:///android_asset/ai/index.html`
* JSBridge：`AndroidBridge`（复制、关闭、原生 Toast 等）
* 布局：`activity_ai_webview.xml`（顶部标题 + WebView）
* Manifest：已注册 `AiWebViewActivity`

## 🚀 快速开始 (Getting Started)

1. **环境要求**:
    * Android Studio Iguana 或更高版本
    * JDK 17+
    * Android SDK API 35

2. **运行项目**:
    * 克隆本项目到本地。
    * 使用 Android Studio 打开项目根目录。
    * 等待 Gradle Sync 完成依赖下载。
    * 连接真机或模拟器 (建议 API 24+)，点击 **Run 'app'**。

## 📝 开发计划

* [x] 基础视频播放与切换
* [x] 个人主页 UI
* [x] 点赞与评论弹窗
* [ ] 视频上传功能
* [ ] 用户登录/注册系统
* [ ] 更多互动动画细节优化

## 🤝 贡献 (Contributing)

欢迎提交 Issue 或 Pull Request 来改进本项目！

## 📄 许可证 (License)

本项目仅用于学习和交流目的。
