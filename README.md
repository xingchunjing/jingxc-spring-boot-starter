# 项目说明文档

- 接入之前需要先安装并配置好nacos，否则无法启动或者先将common中的nacos相关的配置去掉，移除相关依赖包

## 1.简介：

* jingxc-boot-paypal:paypal api接入项目
* jingxc-boot-xsolla:xsolla api接入项目
* jingxc-boot-google:google api接入项目

## 2.部署

- 接入文档参见博客

### 2.1 项目导入流程eclipse

- 1.下载最新版的lombok文件: [https://projectlombok.org/downloads/lombok.jar](url)
- 2.配置.ini文件，例：

```
-Xbootclasspath/a:lombok.jar
-javaagent:/Applications/SpringToolSuite4.app/Contents/Eclipse/lombok.jar
```

- 3.导入maven项目

### 2.2 项目导入流程idea

- 1.直接从软件下载install，lombok即可，导入maven项目

### 2.3 项目版本

	master分支为项目常用分支，springboot，springcloud依赖版本较低，线上所有项目均使用此版本
	更新所有版本依赖分支为最新springboot，springcloud最新稳定版本，只有测试环境网关服务在用

## 3.查看alibaba相关以来版本

https://github.com/alibaba/spring-cloud-alibaba/wiki/版本说明

### 备忘：mac启动nacos可能会报错

* /Library/Internet: No such file or directory
* 查看java_home: /usr/libexec/java_home -V
* 修改start.sh配置78行export JAVA_HOME=""