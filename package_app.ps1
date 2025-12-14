$ErrorActionPreference = "Stop"

$appName = "StringEncryptor"
$appVersion = "1.0.0"
$jarName = "string-encryptor-1.0-SNAPSHOT.jar"
$sourceJar = "target\$jarName"
$tempDir = "temp_build"
$destDir = "dist"
$mainClass = "com.encryption.Launcher"
$iconPng = "src\main\resources\com\encryption\icon.png"
$iconIco = "app.ico"

function Convert-PngToIco {
    param (
        [string]$PngPath,
        [string]$IcoPath
    )
    $absPng = Resolve-Path $PngPath
    $absIco = [System.IO.Path]::GetFullPath($IcoPath)
    Write-Host "Converting icon: $absPng -> $absIco"
    
    Add-Type -AssemblyName System.Drawing
    $original = [System.Drawing.Bitmap]::FromFile($absPng)
    $bitmap = New-Object System.Drawing.Bitmap(256, 256)
    $graph = [System.Drawing.Graphics]::FromImage($bitmap)
    $graph.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graph.DrawImage($original, 0, 0, 256, 256)
    $graph.Dispose()
    
    $fileStream = [System.IO.File]::OpenWrite($absIco)
    $header = [byte[]]@(0, 0, 1, 0, 1, 0)
    $fileStream.Write($header, 0, $header.Length)
    $memoryStream = New-Object System.IO.MemoryStream
    $bitmap.Save($memoryStream, [System.Drawing.Imaging.ImageFormat]::Png)
    $pngData = $memoryStream.ToArray()
    $size = $pngData.Length
    $entry = New-Object byte[] 16
    $entry[0] = 0
    $entry[1] = 0
    $entry[2] = 0
    $entry[3] = 0
    $entry[4] = 1
    $entry[5] = 0
    $entry[6] = 32
    $entry[7] = 0
    [System.BitConverter]::GetBytes([int]$size).CopyTo($entry, 8)
    [System.BitConverter]::GetBytes([int]22).CopyTo($entry, 12)
    $fileStream.Write($entry, 0, $entry.Length)
    $fileStream.Write($pngData, 0, $size)
    $fileStream.Close()
    $memoryStream.Dispose()
    $bitmap.Dispose()
    $original.Dispose()
}

Write-Host "Preparing build environment..."

if (Test-Path $tempDir) { Remove-Item -Path $tempDir -Recurse -Force }
New-Item -ItemType Directory -Path $tempDir | Out-Null

Copy-Item -Path $sourceJar -Destination "$tempDir\$jarName"

if (Test-Path $iconIco) { Remove-Item $iconIco -Force }

if (Test-Path $iconPng) {
    Convert-PngToIco -PngPath $iconPng -IcoPath $iconIco
} else {
    Write-Warning "Icon file not found at $iconPng"
}

if (Test-Path $destDir) { Remove-Item -Path $destDir -Recurse -Force }

Write-Host "Running jpackage..."

$absIco = [System.IO.Path]::GetFullPath($iconIco)

$jpackageArgs = @(
    "--name", $appName,
    "--app-version", $appVersion,
    "--input", $tempDir,
    "--main-jar", $jarName,
    "--main-class", $mainClass,
    "--type", "app-image",
    "--dest", $destDir,
    "--java-options", "-Dfile.encoding=UTF-8"
)

if (Test-Path $absIco) {
    $jpackageArgs += "--icon"
    $jpackageArgs += $absIco
}

Write-Host "Executing: jpackage $jpackageArgs"
jpackage @jpackageArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host "--------------------------------------------------"
    Write-Host "Build Success!"
    Write-Host "App generated at: $(Resolve-Path "$destDir\$appName")"
    if (Test-Path $absIco) {
        Write-Host "Icon used: $absIco"
    }
    Write-Host "Note: If icon is not updated, try moving the folder to clear cache."
    Write-Host "--------------------------------------------------"
} else {
    Write-Host "Build Failed!"
}
