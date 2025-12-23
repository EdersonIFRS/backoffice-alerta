@echo off
set JAVA_HOME=C:\Program Files\java
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo
