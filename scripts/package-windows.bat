@echo off
setlocal
cd /d "%~dp0.."

echo [1/3] Maven ile uygulama ve bagimliliklar derleniyor...
call mvn clean package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/package-input
if errorlevel 1 goto :error

if not exist target\package-input mkdir target\package-input
copy /Y target\wordreminder-3.4.0.jar target\package-input\wordreminder-3.4.0.jar >nul
if errorlevel 1 goto :error

if not exist target\installer mkdir target\installer

echo [2/3] Windows kurulum paketi olusturuluyor...
jpackage --type exe --name WordReminder --app-version 3.4.0 --vendor "WordReminder" --input target/package-input --main-jar wordreminder-3.4.0.jar --main-class com.wordreminder.MainApplication --dest target/installer --win-menu --win-shortcut --win-dir-chooser
if errorlevel 1 goto :error

echo [3/3] Tamamlandi. Kurulum paketi target\installer klasorunde.
exit /b 0

:error
echo.
echo Islem basarisiz. Maven, JDK 26 ve (jpackage icin) WiX Toolset kurulumunu kontrol et.
exit /b 1
