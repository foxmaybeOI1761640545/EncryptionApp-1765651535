; 脚本由 Trae 针对 Inno Setup 6.4.2 优化
; 请确保 Inno Setup 已安装
; 使用方法：用 Inno Setup 打开此文件并点击 "Compile"

#define MyAppName "String Encryptor"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Nick Wilde"
#define MyAppURL "https://github.com/foxmaybeOI1761640545/"
#define MyAppExeName "StringEncryptor.exe"
#define MyAppFolder "dist\StringEncryptor"

[Setup]
; NOTE: AppId 的值用于唯一标识此应用程序。
; 不要在其他应用程序的安装程序中使用相同的 AppId 值。
; (要生成新的 GUID，请在 Inno Setup IDE 中点击 Tools | Generate GUID。)
AppId={{ACC7A1A7-6D93-410E-B400-1C8B6F72FACD}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}

; 默认安装目录：Program Files (64-bit)
DefaultDirName={autopf64}\{#MyAppName}
DisableProgramGroupPage=yes

; 权限设置：
; lowest - 安装到用户目录（不需要管理员权限）
; admin  - 安装到 Program Files（需要管理员权限）
; 建议使用 admin 以便安装到 Program Files
PrivilegesRequired=admin

; 64位架构设置 (关键：适配 jpackage 生成的 64位 JDK 运行时)
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

OutputDir=install_output
OutputBaseFilename=StringEncryptor_Setup
; 使用更高压缩率
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern

; 卸载图标
UninstallDisplayIcon={app}\{#MyAppExeName}
SetupIconFile=app.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "chinesesimplified"; MessagesFile: "ChineseSimplified.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; 包含 jpackage 生成的所有文件
; 注意：确保在此脚本运行前，dist\StringEncryptor 文件夹已存在且完整
Source: "{#MyAppFolder}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
