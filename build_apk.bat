@echo off
chcp 65001 >nul
echo =========================================
echo    LsLife V6.0 商业版 APK 自动化构建引擎   
echo =========================================

set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
cd /d D:\LsLife\android

echo [1/3] 正在配置构建环境...
echo [2/3] 正在编译 Release 版本 APK (这需要 1-3 分钟)...
call gradlew.bat assembleRelease

echo [3/3] 正在检查打包产物...
if exist "D:\LsLife\releases\LsLife-v6.0.0-release.apk" (
    echo.
    echo =========================================
    echo  ✅ BUILD SUCCESSFUL! 构建成功！
    echo  产物位置: D:\LsLife\releases\LsLife-v6.0.0-release.apk
    echo =========================================
    explorer "D:\LsLife\releases"
) else (
    echo.
    echo ❌ 构建可能遇到错误，请检查上方日志。
)

pause
