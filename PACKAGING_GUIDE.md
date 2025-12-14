# 应用程序打包指南

本文档详细说明了如何将 JavaFX 应用程序打包为 Windows 单文件安装程序 (`.exe`) 的全流程。

## 1. 环境准备

在开始之前，请确保你的开发环境满足以下要求：

*   **Java JDK 17+**: 建议使用 Microsoft Build of OpenJDK 或 Eclipse Temurin。
*   **Maven**: 用于构建 Java 项目。
*   **Inno Setup 6.x**: 用于制作安装包 (推荐 6.2+ 版本)。
*   **PowerShell 5.0+**: Windows 自带，用于运行自动化脚本。

## 2. 关键文件说明

| 文件路径 | 说明 |
| :--- | :--- |
| `src/main/resources/com/encryption/icon.png` | **应用图标**。脚本会自动将其转换为 `.ico` 格式供打包使用。 |
| `package_app.ps1` | **自动化打包脚本**。负责生成自带运行时环境的程序文件夹。 |
| `setup.iss` | **安装包配置文件**。Inno Setup 使用此文件生成最终的 `Setup.exe`。 |
| `pom.xml` | **Maven 配置**。已配置 `maven-shade-plugin` 以生成 Fat JAR。 |

## 3. 打包流程 (三步走)

### 第一步：构建 JAR 文件
在项目根目录打开终端，运行 Maven 打包命令：

```bash
mvn clean package
```
*   **作用**：清理旧文件，编译代码，并将所有依赖项打包进 `target/string-encryptor-1.0-SNAPSHOT.jar`。
*   **注意**：构建过程中，根目录下会自动生成一个 `dependency-reduced-pom.xml` 文件。这是 Maven Shade 插件的副产品，用于排除已打包的依赖。**你可以忽略此文件**，它不参与后续打包。

### 第二步：生成自带环境的程序
运行 PowerShell 脚本：

```powershell
powershell -ExecutionPolicy Bypass -File package_app.ps1
```
*   **脚本执行的动作**：
    1.  自动检测 `icon.png` 并转换为 Windows 需要的 `app.ico`。
    2.  调用 `jpackage` 命令，将 JAR 文件与精简版 JDK 捆绑。
    3.  在 `dist/StringEncryptor` 目录下生成可直接运行的程序。
*   **验证**：你可以进入 `dist/StringEncryptor` 目录，双击 `StringEncryptor.exe` 验证程序能否运行且图标是否正确。

### 第三步：制作单文件安装包
1.  安装并打开 **Inno Setup Compiler**。
2.  打开项目根目录下的 `setup.iss` 文件。
3.  点击工具栏上的 **Run** (绿色三角形) 或 **Compile** 按钮。
4.  **结果**：生成的安装包将位于 `install_output` 文件夹中，文件名为 `StringEncryptor_Setup.exe`。

## 4. 参数详解

### 4.1 jpackage 参数 (位于 `package_app.ps1`)

| 参数 | 值 | 说明 |
| :--- | :--- | :--- |
| `--name` | `StringEncryptor` | 生成的 exe 文件名和程序名称。 |
| `--app-version` | `1.0.0` | 内部版本号。 |
| `--input` | `temp_build` | 包含 JAR 文件的输入目录。 |
| `--main-jar` | `...SNAPSHOT.jar` | 主 JAR 文件名。 |
| `--main-class` | `com.encryption.Launcher` | **重要**：使用独立的 Launcher 类而非继承 Application 的类，防止 JavaFX 模块错误。 |
| `--type` | `app-image` | 生成解压后的程序文件夹 (便于调试和二次打包)。 |
| `--dest` | `dist` | 输出目录。 |
| `--icon` | `app.ico` | 应用程序图标 (脚本自动生成)。 |

### 4.2 Inno Setup 参数 (位于 `setup.iss`)

| 指令 | 说明 |
| :--- | :--- |
| `AppId` | 程序的唯一标识符。如果发布新软件，请在 Inno Setup 中生成新的 GUID。 |
| `DefaultDirName` | `{autopf64}\{#MyAppName}`。`{autopf64}` 确保安装到 `Program Files` (64位)。 |
| `PrivilegesRequired` | `admin`。请求管理员权限，以便写入 Program Files。 |
| `ArchitecturesAllowed` | `x64`。限制仅能在 64 位 Windows 上运行 (因为捆绑的 JDK 是 64 位的)。 |
| `Compression` | `lzma2/ultra64`。最高压缩率，减小安装包体积。 |
| `SetupIconFile` | `app.ico`。安装包文件本身的图标。 |
| `UninstallDisplayIcon` | `{app}\StringEncryptor.exe`。在“控制面板-卸载程序”中显示的图标。 |

## 5. 常见问题

*   **图标不显示？**
    *   Windows 缓存可能会导致图标更新延迟。尝试重启资源管理器或将文件复制到其他位置查看。
    *   确保 `icon.png` 是标准的 PNG 图片。
*   **安装后无法运行？**
    *   请先在第二步生成的 `dist/StringEncryptor` 目录中手动运行 `.exe` 测试。如果那里报错，说明是 `jpackage` 或代码问题，而不是 Inno Setup 的问题。
