### 一键升级与自定义 YOLO 模型适配指南（Android + NCNN）

本仓库已升级：
- Gradle Wrapper 7.5、Android Gradle Plugin 7.4.2
- `compileSdkVersion/targetSdkVersion = 34`
- AndroidX（已启用 `android.useAndroidX=true` 与 `android.enableJetifier=true`）
- 动态模型扫描与按名称加载：新增 `Yolov8Ncnn.loadModelByName()`，`MainActivity` 自动扫描 `assets` 下 `yolov8*.param`
- 可配置 CMake：支持通过环境变量或 CMake 变量覆写 OpenCV、ncnn 路径

#### 目录结构关键点
- `app/src/main/assets/` 放置模型文件：`yolov8{token}.param` 与 `yolov8{token}.bin`
- Java 入口：`com.tencent.yolov8ncnn.MainActivity`、`Yolov8Ncnn`
- JNI 推理：`app/src/main/jni/yolo.cpp`、`yolov8ncnn.cpp`
- CMake：`app/src/main/jni/CMakeLists.txt`

---

### 一、准备依赖
1) 下载/放置 OpenCV Android SDK（建议使用 opencv-mobile 或官方 SDK）：
- 期望目录（可自定义）：`app/src/main/jni/../jni/opencv-mobile/sdk/native/jni`

2) 下载/放置 ncnn 预编译包（Android，含 Vulkan 版更佳）：
- 期望目录（可自定义）：`app/src/main/jni/../jni/ncnn/${ABI}/lib/cmake/ncnn`
- 常见 ABI：`arm64-v8a`、`armeabi-v7a`

你也可以在构建时通过 CMake 变量覆盖：
```
-Dncnn_DIR=/absolute/path/to/ncnn/${ABI}/lib/cmake/ncnn -DOpenCV_DIR=/absolute/path/to/opencv/sdk/native/jni
```

---

### 二、一键升级脚本
执行：
```
bash scripts/upgrade_android.sh
```
功能：
- 确保 Gradle/AGP/AndroidX/Manifest 已对齐新版
- 可选：同步子模块或下载 opencv-mobile/ncnn 至默认位置

---

### 三、添加自训练 YOLO 模型（零代码更改）
1) 将导出的 NCNN 模型拷贝到 `app/src/main/assets/`：
- `yolov8my.param`
- `yolov8my.bin`

2) 启动 App：
- `MainActivity` 会自动扫描到 `my` 这个 token，并在模型下拉框中展示
- 选择 `my`，选择 CPU/GPU，点击切换摄像头进行实时推理

说明：
- 我们在 `yolo.cpp` 中根据输出张量宽度自动推断类别数（`num_class = pred.w - 4*16`），适配不同类别数量
- 若你的模型有不同输入尺寸，默认使用 640，可按需在 JNI `loadModelByName` 调整 `target_size`

---

### 四、构建与运行
本地构建（推荐 Android Studio Bumblebee+）：
1) 打开根目录 `ncnn-android-yolov8`
2) 连接设备或启动模拟器
3) 选择目标 ABI（如 `arm64-v8a`）并构建运行

命令行：
```
./gradlew :app:assembleDebug
```

可通过 `local.properties` 或环境变量传入 NDK 路径。CMake 查找 ncnn/OpenCV 支持变量覆盖。

---

### 五、常见问题
- 构建报找不到 ncnn/OpenCV：请按上文提供 `ncnn_DIR`/`OpenCV_DIR`，或将它们放在默认第三方目录。
- 运行时 GPU 选项不可用：设备无 Vulkan 或未启用 Vulkan 权限；将下拉选择为 CPU。
- 类别名称：默认以占位名 `class0..class79` 展示。如需自定义名称，可在前端自行映射或扩展 JNI 以加载 `classes.txt`。

---

### 六、接口变更
- Java 新增：`boolean loadModelByName(AssetManager mgr, String modelToken, int cpugpu)`
- C++ JNI：实现了按 token 加载 `yolov8{token}.param/bin`
- 推理：自动推断类数，更通用

---

### 七、模型导出到 NCNN 小贴士
- 使用官方 `onnx -> ncnn` 转换工具；确保 `images` 输入名、`output` 输出名与本工程保持一致
- 若输出名不同，可在 `yolo.cpp` 中调整 `ex.input("images", ...)` 和 `ex.extract("output", ...)`

完成。

