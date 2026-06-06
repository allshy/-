# 编码翻译器 Android App

中文编码/翻译工具，支持将中文文本转换为多种编码格式和语言。

## 功能列表

### 编码类（本地处理，无需网络）

| 编码方式 | 说明 | 示例输入 | 示例输出 |
|---------|------|---------|---------|
| **Base64** | 二进制到文本编码 | 你好 | `5L2g5aW9` |
| **URL编码** | 百分号编码 | 你好 | `%E4%BD%A0%E5%A5%BD` |
| **Unicode转义** | Unicode码点表示 | 你好 | `你好` |
| **HTML实体** | HTML字符引用 | 你好 | `&#x4F60;&#x597D;` |
| **XML文本** | XML结构封装 | 你好 | `<request><content>你好</content></request>` |
| **摩尔斯电码** | 点划信号编码 | Hello | `.... . .-.. .-.. ---` |

### 翻译类（需要网络，使用 MyMemory API）

| 语言 | 语言代码 |
|------|---------|
| **英语** | zh → en |
| **冰岛语** | zh → is |

## 技术栈

- **语言**: Kotlin
- **UI**: Android View + Material Components
- **网络**: OkHttp 3
- **异步**: Kotlin Coroutines
- **翻译API**: [MyMemory](https://mymemory.translated.net/)（免费，无需密钥）
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)

## 项目结构

```
app/src/main/
├── java/com/encoder/translatorapp/
│   ├── MainActivity.kt        # 主界面，Chip选择 + 转换逻辑
│   ├── Encoders.kt            # 6种本地编码算法
│   └── TranslationApi.kt      # MyMemory翻译API调用
├── res/
│   ├── layout/activity_main.xml   # 深色主题布局
│   ├── color/                     # Chip选中状态颜色选择器
│   ├── drawable/                  # 矢量图标
│   ├── mipmap-anydpi-v26/         # 自适应图标
│   └── values/                    # 颜色、字符串、主题
└── AndroidManifest.xml
```

## 编译方式

### GitHub Actions 自动编译

每次推送到 `main` 分支会自动触发：
1. 编译 Debug APK 和 Release APK
2. 上传 APK 为 Artifact
3. 自动创建 GitHub Release 并附带 APK 下载

APK 下载：前往 [Releases](../../releases) 页面

### 本地编译

```bash
# Debug版本
gradle assembleDebug

# Release版本
gradle assembleRelease
```

APK 输出路径：`app/build/outputs/apk/`

## 开发过程与编译问题记录

### 第一次提交 — 初始项目搭建

**提交**: `34ebf4c` feat: 中文编码翻译器 Android App

创建完整的 Android 项目结构，包含 8 种转换功能和 GitHub Actions 工作流。

**编译结果**: ❌ 失败 — `Build Debug APK` 步骤报错

**失败原因**:
1. **图标资源路径错误** — `adaptive-icon` XML 直接放在 `mipmap-hdpi/` 目录下，应该放在 `mipmap-anydpi-v26/`，且缺少 foreground drawable 引用
2. **XML命名空间声明位置** — `xmlns:app` 在子元素（ChipGroup、MaterialButton）中重复声明，而非在根 LinearLayout 中统一声明
3. **Chip颜色资源类型** — `app:chipBackgroundColor` 需要 `ColorStateList`（selector XML），直接引用 `@color/` 普通颜色值导致类型不匹配

### 第二次提交 — 修复编译错误

**提交**: `1d5560f` fix: 修复编译错误

**修复内容**:
- 将图标移至 `mipmap-anydpi-v26/`，新增 `drawable/ic_launcher_foreground.xml` 矢量图
- `xmlns:app` 移到根元素 `<LinearLayout>` 统一声明
- 新建 `color/chip_bg_selector.xml` 和 `color/chip_text_selector.xml` 作为 ColorStateList
- 添加 `lint { abortOnError = false }` 防止 lint 警告阻断编译

**编译结果**: ⚠️ 部分成功 — Debug/Release APK 均编译成功并上传为 Artifact，但 `Create Release` 步骤失败

**Release失败原因**:
- `softprops/action-gh-release` 需要仓库的 `contents: write` 权限
- GitHub Actions 默认的 `GITHUB_TOKEN` 权限不包含写入 Release 的能力

### 第三次提交 — 修复 Release 权限

**提交**: `e7537a9` fix: 修复 GitHub Release 权限

**修复内容**:
- 在工作流顶层添加 `permissions: contents: write`
- 升级 `softprops/action-gh-release` 从 v1 到 v2

**编译结果**: ✅ 待确认

### 问题总结

| # | 问题 | 类型 | 原因 |
|---|------|------|------|
| 1 | adaptive-icon 放错目录 | 资源 | 应放在 `mipmap-anydpi-v26/` 而非 `mipmap-hdpi/` |
| 2 | xmlns:app 多处重复声明 | XML | 应在根元素统一声明命名空间 |
| 3 | Chip颜色类型不匹配 | 资源 | `chipBackgroundColor` 需要 ColorStateList 而非普通 color |
| 4 | Release 步骤无权限 | CI/CD | GITHUB_TOKEN 缺少 `contents: write` 权限 |
| 5 | action-gh-release 版本过旧 | CI/CD | v1 已过时，升级到 v2 |
