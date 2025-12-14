@echo off
chcp 65001 >nul

nul>package.log

echo ==================
echo 正在打包项目...
echo ==================

mvn clean package >> package.log