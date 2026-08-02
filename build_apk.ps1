$ErrorActionPreference = "Stop"
Write-Host "========================================="
Write-Host "   LsLife V6.0 商业版 APK 自动化构建引擎   "
Write-Host "========================================="

$projectDir = "D:\LsLife\android"
$releaseDir = "D:\LsLife\releases"

Write-Host "1. 配置构建环境..."
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
Set-Location -Path $projectDir

Write-Host "2. 启动 Gradle AssembleRelease 编译过程 (这可能需要几分钟)..."
.\gradlew.bat assembleRelease

Write-Host "3. 检查构建产物..."
if (Test-Path "$releaseDir\LsLife-v6.0.0-release.apk") {
    Write-Host "✅ 构建成功！" -ForegroundColor Green
    Write-Host "产物已归档至: $releaseDir\LsLife-v6.0.0-release.apk" -ForegroundColor Green
    
    # 自动打开输出目录
    Invoke-Item $releaseDir
} else {
    Write-Host "❌ 构建失败或未能找到预期产物，请检查上方 Gradle 日志。" -ForegroundColor Red
}

Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
