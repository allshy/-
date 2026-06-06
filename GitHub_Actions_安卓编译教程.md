# GitHub Actions 安卓 APK 自动编译教程

> 写给完全零基础的小白，从"这是什么"到"能跑起来"，结合我真实踩过的坑。

---

## 一、GitHub Actions 是什么？

简单理解：**GitHub 提供的免费云电脑**，帮你自动执行任务。

你把代码推送到 GitHub，它就自动帮你：
- 编译代码 → 生成 APK
- 跑测试 → 检查有没有 bug
- 发布版本 → 把 APK 放到 Release 页面供下载

**你不需要在自己电脑上装 Android Studio，GitHub 的云服务器帮你编译。**

---

## 二、核心概念（只需要记住4个词）

| 概念 | 类比 | 说明 |
|------|------|------|
| **Workflow（工作流）** | 一份说明书 | 一个 `.yml` 文件，告诉 GitHub "要做什么" |
| **Trigger（触发器）** | 开关 | 什么时候启动？推送代码时？手动点？ |
| **Job（任务）** | 一个工人 | 在一台独立的云电脑上执行 |
| **Step（步骤）** | 工人的每一个动作 | 装 JDK → 装 Gradle → 编译 → 上传 |

它们的关系：
```
Workflow（工作流）
  └── 触发条件：push 到 main 分支
  └── Job（任务）
        ├── Step 1: 拉取代码
        ├── Step 2: 安装 JDK
        ├── Step 3: 安装 Gradle
        ├── Step 4: 编译 APK
        └── Step 5: 上传 APK
```

---

## 三、从零开始写一个工作流

### 第1步：创建文件

在项目根目录创建：
```
.github/
  workflows/
    build.yml    ← 就是这个文件
```

**注意**：`.github` 前面有个点，`workflows` 有个 s，这两个写错了 GitHub 识别不到。

### 第2步：写配置文件

下面是一个**最简单能跑的**安卓编译工作流，我逐行解释：

```yaml
# ===== 第一部分：基本信息 =====
name: Build APK                    # 工作流名称，显示在 Actions 页面

# ===== 第二部分：什么时候触发？=====
on:
  push:
    branches: [ main ]             # 推送到 main 分支时触发
  workflow_dispatch:               # 允许在网页上手动触发（推荐加上）

# ===== 第三部分：权限 =====
permissions:
  contents: write                  # 允许创建 Release（重要！后面讲坑）

# ===== 第四部分：具体要做什么 =====
jobs:
  build:                           # 任务名，可以随便起
    runs-on: ubuntu-latest         # 用最新的 Ubuntu 云电脑

    steps:
    # 步骤1：把代码拉到云电脑上
    - uses: actions/checkout@v4

    # 步骤2：安装 JDK 17（安卓编译需要）
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    # 步骤3：安装 Gradle（安卓的编译工具）
    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        gradle-version: 8.2

    # 步骤4：编译！
    - name: Build Debug APK
      run: gradle assembleDebug

    # 步骤5：把 APK 上传为 Artifact（可下载的附件）
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

这就够了！推送代码后，去仓库的 **Actions** 页面就能看到编译过程，编译成功后在 **Artifact** 里下载 APK。

---

## 四、进阶：自动发布到 Release 页面

Artifact 需要登录 GitHub 才能下载，不方便分享。想让别人也能下载？加一个 Release 步骤：

```yaml
    # 步骤6：自动创建 Release 并附带 APK
    - name: Create Release
      if: github.event_name == 'push'     # 只在推送时发布，手动触发不发
      uses: softprops/action-gh-release@v2
      with:
        tag_name: v${{ github.run_number }}          # 自动递增版本号 v1, v2, v3...
        name: Release v${{ github.run_number }}
        files: app/build/outputs/apk/debug/app-debug.apk
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}    # GitHub 自动提供的令牌
```

发布后，任何人都可以在仓库的 **Releases** 页面直接下载 APK。

---

## 五、完整的生产级工作流

把上面的基础版和进阶版合在一起，就是我们项目最终使用的版本：

```yaml
name: Build APK

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        gradle-version: 8.2

    - name: Build Debug APK
      run: gradle assembleDebug

    - name: Build Release APK
      run: gradle assembleRelease

    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk

    - name: Upload Release APK
      uses: actions/upload-artifact@v4
      with:
        name: app-release
        path: app/build/outputs/apk/release/app-release-unsigned.apk

    - name: Create Release
      if: github.event_name == 'push' && (github.ref == 'refs/heads/main' || github.ref == 'refs/heads/master')
      uses: softprops/action-gh-release@v2
      with:
        tag_name: v${{ github.run_number }}
        name: Release v${{ github.run_number }}
        files: |
          app/build/outputs/apk/debug/app-debug.apk
          app/build/outputs/apk/release/app-release-unsigned.apk
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## 六、本次项目踩过的坑（血泪经验）

### 坑1：安卓图标资源路径放错

**现象**：`Build Debug APK` 步骤直接报错

**原因**：
```
错误写法 ❌  mipmap-hdpi/ic_launcher.xml      （放了 adaptive-icon XML）
正确写法 ✅  mipmap-anydpi-v26/ic_launcher.xml （adaptive-icon 必须在这里）
```

`adaptive-icon`（自适应图标）是 Android 8.0 (API 26) 引入的，所以 XML 定义必须放在带 `-v26` 后缀的目录。`mipmap-hdpi`、`mipmap-xxhdpi` 这些是放 PNG 图片的。

**教训**：安卓资源目录的命名规则非常严格，放错文件夹就编译不过。

---

### 坑2：XML 命名空间重复声明

**现象**：编译报 XML 解析错误

**原因**：
```xml
<!-- 错误写法 ❌：在子元素里多次声明 xmlns:app -->
<LinearLayout xmlns:android="...">
    <ChipGroup xmlns:app="..." app:singleSelection="true">   <!-- 声明了一次 -->
        ...
    </ChipGroup>
    <Button xmlns:app="..." app:strokeColor="..." />          <!-- 又声明了一次 -->
</LinearLayout>

<!-- 正确写法 ✅：在根元素统一声明一次 -->
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">      <!-- 这里声明一次就够了 -->
    <ChipGroup app:singleSelection="true">
        ...
    </ChipGroup>
    <Button app:strokeColor="..." />
</LinearLayout>
```

**教训**：`xmlns:app` 在布局 XML 的根元素声明一次，所有子元素都能用。

---

### 坑3：Material Chip 颜色类型不匹配

**现象**：编译报资源类型错误

**原因**：
```xml
<!-- 错误写法 ❌：直接引用普通颜色 -->
<Chip app:chipBackgroundColor="@color/green" />

<!-- 正确写法 ✅：引用 ColorStateList（选择器 XML）-->
<Chip app:chipBackgroundColor="@color/chip_bg_selector" />
```

`chipBackgroundColor` 期望的是 `ColorStateList`（能根据选中/未选中等状态变色），不是普通的单色值。需要在 `res/color/` 目录创建选择器：

```xml
<!-- res/color/chip_bg_selector.xml -->
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="#00E676" android:state_checked="true" />  <!-- 选中 -->
    <item android:color="#3A3A3A" />                                <!-- 未选中 -->
</selector>
```

**教训**：Material 组件的颜色属性很多需要 `ColorStateList`，不是随便给个颜色就行。注意看文档里参数类型是 `color` 还是 `ColorStateList`。

---

### 坑4：GitHub Release 步骤无权限（最隐蔽的坑）

**现象**：APK 编译成功了，Artifact 也上传了，但 Release 页面是空的，Actions 显示失败

**原因**：GitHub Actions 的 `GITHUB_TOKEN` 默认权限是只读的，`softprops/action-gh-release` 需要写入权限才能创建 Release。

**修复**：在工作流文件顶层加上：
```yaml
permissions:
  contents: write          # ← 就是这一行，没有它 Release 就创建不了
```

**教训**：如果你的工作流需要"往仓库里写东西"（创建 Release、推送代码、打 Tag），都需要 `contents: write` 权限。编译和上传 Artifact 不需要，因为 Artifact 是存在 Actions 自己的存储空间里的。

---

### 坑5：action-gh-release 版本过旧

**现象**：即使加了权限，Release 步骤仍有警告

**原因**：`softprops/action-gh-release@v1` 使用的是已弃用的 Node.js 版本

**修复**：
```yaml
# 旧版 ❌
uses: softprops/action-gh-release@v1

# 新版 ✅
uses: softprops/action-gh-release@v2
```

**教训**：GitHub Actions 的第三方 Action 要用最新大版本号。旧版本可能因为 Node.js 运行时过期而出问题。可以去 Action 的 GitHub 页面看最新版本。

---

## 七、常用的排查方法

### 怎么看编译日志？

1. 打开仓库 → 点顶部 **Actions** 标签
2. 点击失败的那次运行（红色叉号的）
3. 点击 Job 名称（比如 `build`）
4. 展开失败的步骤，看具体报错

### 常见报错速查

| 报错关键词 | 原因 | 解决 |
|-----------|------|------|
| `Could not find method` | Gradle 版本和插件版本不兼容 | 检查 Gradle 和 AGP 版本对应关系 |
| `resource not found` | 引用了不存在的资源 | 检查 `@color/`、`@drawable/` 引用是否拼写正确 |
| `AAPT2 error` | XML 资源文件有语法错误 | 检查 XML 闭合标签、命名空间 |
| `403 Forbidden` | 权限不够 | 添加 `permissions: contents: write` |
| `Node.js 16 actions are deprecated` | Action 版本太旧 | 升级 `@v1` → `@v2` 或更高 |

### Debug 和 Release 的区别

| | Debug | Release |
|---|-------|---------|
| **签名** | 自动用调试签名 | 需要你自己的签名证书 |
| **能否安装** | ✅ 直接装 | ❌ 未签名装不了 |
| **体积** | 较大（6MB） | 较小（5MB） |
| **适用场景** | 测试、自用 | 上架应用商店 |

**小白建议**：只编译 Debug 版本就够了，Release 签名比较复杂，等需要上架时再研究。

---

## 八、最小模板（复制即用）

如果你有一个新的安卓项目，把这个文件放到 `.github/workflows/build.yml`：

```yaml
name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4

    - uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - uses: gradle/actions/setup-gradle@v4
      with:
        gradle-version: 8.2

    - run: gradle assembleDebug

    - uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk

    - uses: softprops/action-gh-release@v2
      if: github.event_name == 'push'
      with:
        tag_name: v${{ github.run_number }}
        files: app/build/outputs/apk/debug/app-debug.apk
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

推送代码 → 自动编译 → 自动发布 APK，三步到位。

---

## 九、免费额度说明

GitHub Actions 对公开仓库**完全免费**，不限时长。

私有仓库每月有 2000 分钟免费额度（GitHub Free 账户），一次安卓编译大约 2-3 分钟，够用大约 700 次编译。

---

*本教程基于「编码翻译器」项目的实际开发经验编写，3次提交、5个坑，全是真实踩出来的。*
