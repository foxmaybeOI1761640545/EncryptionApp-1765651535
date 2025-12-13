@echo off
chcp 65001 >nul

nul>run.log

echo ==================
echo 正在编译并运行项目...
echo ==================

mvn clean javafx:run >> run.log