# 编码翻译器 Android App

中文双向编码/翻译工具，支持编码与解码、中外互译。

## 界面预览

```
┌───────────────────────────┐
│     中文 / 原文（可编辑）    │  ← 输入或显示中文
└───────────────────────────┘
    [ ↓ 编码 ]    [ ↑ 解码 ]    ← 双向转换
┌───────────────────────────┐
│     编码 / 译文（可编辑）    │  ← 输入或显示编码内容
└───────────────────────────┘
   [复制中文]      [复制编码]
```

- **↓ 编码**：读取上方中文 → 编码后写入下方
- **↑ 解码**：读取下方编码 → 解码后写入上方
- 两个输入框均可手动编辑和粘贴

## 功能列表

### 编码类（本地处理，无需网络）

| 编码方式 | 编码（中文→编码） | 解码（编码→中文） |
|---------|-----------------|-----------------|
| **Base64** | 你好 → `5L2g5aW9` | `5L2g5aW9` → 你好 |
| **URL编码** | 你好 → `%E4%BD%A0%E5%A5%BD` | `%E4%BD%A0%E5%A5%BD` → 你好 |
| **Unicode转义** | 你好 → `你好` | `你好` → 你好 |
| **HTML实体** | 你好 → `&#x4F60;&#x597D;` | `&#x4F60;&#x597D;` → 你好 |
| **XML文本** | 你好 → `<content>你好</content>` | `<content>你好</content>` → 你好 |
| **摩尔斯电码** | Hello → `.... . .-.. .-.. ---` | `.... . .-.. .-.. ---` → HELLO |

### 翻译类（需要网络，使用 MyMemory API）

| 语言 | 编码方向 | 解码方向 |
|------|---------|---------|
| **英语** | 中文 → English | English → 中文 |
| **冰岛语** | 中文 → Íslenska | Íslenska → 中文 |

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
│   ├── MainActivity.kt        # 双向编解码界面逻辑
│   ├── Encoders.kt            # 6种编码 + 6种解码算法
│   └── TranslationApi.kt      # MyMemory 双向翻译API
├── res/
│   ├── layout/activity_main.xml   # 深色主题双向布局
│   ├── color/                     # Chip选中状态颜色选择器
│   ├── drawable/                  # 矢量图标
│   ├── mipmap-anydpi-v26/         # 自适应图标
│   └── values/                    # 颜色、字符串、主题
└── AndroidManifest.xml
```

## 下载安装

前往 [Releases](../../releases) 页面，下载最新的 **app-debug.apk** 直接安装。

> Release 版（app-release-unsigned.apk）未签名，无法直接安装，仅供参考。

## 编译方式

### GitHub Actions 自动编译

每次推送到 `main` 分支会自动触发：
1. 编译 Debug APK 和 Release APK
2. 上传 APK 为 Artifact
3. 自动创建 GitHub Release 并附带 APK 下载

### 本地编译

```bash
gradle assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 开发过程与编译问题记录

### 提交历史

| # | 提交 | 内容 | 编译结果 |
|---|------|------|---------|
| 1 | `34ebf4c` | 初始项目搭建，8种编码功能 | ❌ 编译失败 |
| 2 | `1d5560f` | 修复图标/XML/颜色资源 | ⚠️ APK成功，Release失败 |
| 3 | `e7537a9` | 修复 Release 权限 | ✅ 全部成功 |
| 4 | `f899f76` | 添加解码功能 | ✅ 成功 |
| 5 | `746367e` | 双向编解码界面重构 | ✅ 成功 |

### 踩坑记录

| # | 问题 | 原因 | 修复 |
|---|------|------|------|
| 1 | adaptive-icon 放错目录 | 应放 `mipmap-anydpi-v26/` 而非 `mipmap-hdpi/` | 移动目录 + 添加 vector foreground |
| 2 | xmlns:app 多处重复声明 | 应在根元素统一声明一次 | 移到根 LinearLayout |
| 3 | Chip 颜色类型不匹配 | `chipBackgroundColor` 需要 ColorStateList | 创建 selector XML |
| 4 | Release 步骤无权限 | GITHUB_TOKEN 缺少写权限 | 添加 `permissions: contents: write` |
| 5 | action-gh-release 过旧 | v1 使用已弃用的 Node.js | 升级到 v2 |

详细教程参见 [GitHub_Actions_安卓编译教程.md](GitHub_Actions_安卓编译教程.md)
