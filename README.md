# String Encryptor

基于 AES-GCM 与 PBKDF2 的跨平台桌面应用，支持将任意字符串加密为 Base64 并用相同口令安全解密恢复原文。采用 Java 17 + JavaFX 17 + Maven 构建，界面简洁、离线可用、易于二次开发。

![Java 17](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apache%20maven)
![JavaFX](https://img.shields.io/badge/JavaFX-17-1E90FF)
![Crypto](https://img.shields.io/badge/Crypto-AES--GCM%20%7C%20PBKDF2-4B8BBE)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-6DB33F)

---

## 目录

- [项目简介](#项目简介)
- [亮点特性](#亮点特性)
- [架构设计](#架构设计)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [使用指南](#使用指南)
- [安全说明](#安全说明)
- [配置与拓展](#配置与拓展)
- [开发与构建](#开发与构建)
- [Roadmap](#roadmap)
- [贡献指南](#贡献指南)
- [许可证](#许可证)
- [致谢](#致谢)
- [FAQ / 故障排查](#faq--故障排查)
- [曝光与传播建议](#曝光与传播建议)

## 项目简介

String Encryptor 是一个用于演示与实用的加密小工具：

- 使用用户输入的口令派生出对称密钥，采用 AES-GCM 模式加密字符串；
- 输出格式为 Base64，以自定义头标识（MAGIC）+ 盐（SALT）+ 初始向量（IV）+ 密文与认证标签的二进制拼接；
- 解密时自动解析并验证格式与完整性。

该应用适合需要在本地快速加密/解密敏感片段的场景，也可作为更大型系统中的加密模块参考实现。

## 亮点特性

- AES-GCM + PBKDF2（HMAC-SHA256）安全组合，具备保密性与完整性校验
- 随机 16 字节盐、12 字节 GCM IV，每次加密输出不同密文（抗重放/字典攻击）
- 迭代 120,000 次的 PBKDF2（可调），提升口令抗暴力破解能力
- 纯 Java 实现，跨平台运行，离线使用，不依赖外部服务
- JavaFX 现代界面，输入框支持中英文、符号、表情等
- 代码简洁清晰，易读易测，适合教学与工程落地

## 架构设计

```
┌───────────────────────────────────────────────────────────┐
│                       JavaFX UI 层                        │
│  FXML 布局 + Controller（交互逻辑、输入校验、状态提示）    │
└───────────────▲───────────────────────────────────────────┘
                │
                │ 调用
                │
┌───────────────┴───────────────────────────────────────────┐
│                    Crypto 工具层（纯 Java）                │
│  PBKDF2 口令派生 → AES-GCM 加密/解密 → Base64 编解码      │
└───────────────────────────────────────────────────────────┘
```

- 入口：`com.encryption.Main`（JavaFX 应用启动）
- UI：`main-view.fxml` + `MainController`（加密/解密/清空按钮与状态反馈）
- 核心：`CryptoUtil`（算法与数据格式）

## 项目结构

```
EncryptionApp/
├─ pom.xml                         # Maven 构建与依赖（JavaFX 17）
├─ run.bat                         # Windows 快速运行脚本
├─ src/
│  └─ main/
│     ├─ java/
│     │  └─ com/encryption/
│     │     ├─ Main.java           # JavaFX 启动入口
│     │     ├─ ui/MainController.java
│     │     └─ crypto/CryptoUtil.java
│     └─ resources/
│        └─ com/encryption/ui/main-view.fxml
└─ .gitignore
```

## 快速开始

### 环境要求

- `Java 17`（建议 OpenJDK 17）
- `Maven 3.8+`
- 首次构建需联网以下载依赖（后续离线可运行）

### 启动方式

- Windows：双击 `run.bat` 或在终端执行：

```powershell
.\run.bat
```

- 跨平台（Windows/macOS/Linux）：在项目根目录运行：

```bash
mvn clean javafx:run
```

## 使用指南

### 图形界面

1. 在“密码”框输入口令（不会被保存）；
2. 在“明文”框输入要加密的字符串，点击“加密”；
3. 复制“密文（Base64）”；
4. 在“密文（Base64）”框粘贴密文，点击“解密”，结果显示在“解密结果”。

遇到错误时，底部状态栏会给出中文提示（如“密文不是合法的 Base64”）。

### 代码调用（示例）

```java
// 加密
String cipher = CryptoUtil.encryptToBase64("Hello, 世界", "StrongPassword!".toCharArray());

// 解密
String plain = CryptoUtil.decryptFromBase64(cipher, "StrongPassword!".toCharArray());
```

## 安全说明

- 口令派生：PBKDF2(HMAC-SHA256)，默认迭代 `120_000` 次，密钥长度 256 位；
- 加密算法：AES-GCM，无填充；认证标签 128 位；
- 随机性：每次加密会生成新的盐（16B）与 IV（12B）；
- 载荷格式：`Base64(MAGIC[4] + SALT[16] + IV[12] + CIPHERTEXT_AND_TAG[N])`；
- 敏感数据：界面层使用 `char[]` 接收口令，并在 `finally` 块中清零（尽量减少驻留时间）；
- 注意事项：请使用高强度口令，避免弱口令带来的安全风险；建议在生产场景中增加口令复杂度策略与密钥轮换机制。

## 配置与拓展

- 可在 `CryptoUtil` 中调整以下参数以满足不同安全与性能需求：
  - `PBKDF2_ITERS`（默认 120,000，增大可提高暴力破解成本）
  - `KEY_BITS`（默认 256，AES-256）
  - `TAG_BITS`（默认 128，GCM 认证标签位数）
  - `SALT_LEN` / `IV_LEN`（默认 16 / 12，符合常规实践）
- 可扩展功能示例：
  - 文件加密/解密（目前为字符串）
  - 命令行模式（CLI）与批量处理
  - 导出/导入配置（如迭代次数、算法参数）
  - 多语言与深色模式

## 开发与构建

- 构建与运行：`mvn clean javafx:run`
- 使用 JDK 17 编译：`maven-compiler-plugin`（`<release>17</release>`）
- JavaFX 依赖由 Maven 自动下载，无需手动放置 `lib`
- 项目结构与依赖定义见 `pom.xml`

## Roadmap

- 增加单元测试与持续集成（CI）
- 提供命令行可执行包（跨平台）
- 支持文件与目录加解密
- 增加国际化（i18n）与界面主题
- 提供可插拔的算法策略（如 Argon2 派生）

## 贡献指南

欢迎通过 Issue / PR 提交改进：

- 保持代码风格一致与命名规范统一；
- 添加必要的单元测试与文档说明；
- 对安全相关的更改请附详细的设计与风险评估。

## 许可证

当前仓库尚未设置开源许可证。建议根据项目目标选择：MIT / Apache-2.0 / GPL-3.0 等，并在根目录添加 `LICENSE` 文件（参考 https://choosealicense.com/ ）。

## 致谢

- [OpenJFX / JavaFX](https://openjfx.io/) 提供跨平台桌面 UI 能力
- Java 标准库的加密支持（`javax.crypto`）

## FAQ / 故障排查

- 运行提示找不到 JavaFX？请确认使用 `JDK 17+` 并通过 Maven 构建（不要直接用 `java` 命令运行）。
- 解密报“MAGIC 不匹配”？确保使用本应用生成的密文（格式含 MAGIC 头标识）。
- 解密报“密文不是合法的 Base64”？检查复制/粘贴是否包含多余空白或截断。
- 输出为空或乱码？确保明文为 UTF-8 可编码字符，且口令一致。

## 曝光与传播建议

为提升项目曝光度，可考虑：

- 在 GitHub 添加项目标签与主题：`java`, `javafx`, `cryptography`, `aes-gcm`, `pbkdf2`, `security`, `desktop-app`
- 在 README 顶部放置演示截图或 GIF（操作流程：输入口令 → 加密 → 复制密文 → 解密）
- 发布一篇技术文章（掘金/知乎/简书），介绍设计思路与安全权衡，并附仓库链接
- 在相关社区（Java/安全/桌面应用）同步更新与版本发布
- 提供二进制发行版（Windows `.exe`/macOS `.app`/Linux AppImage），降低试用门槛

---

如果该项目对你有帮助，欢迎 Star 或分享给同事与朋友，感谢支持！
