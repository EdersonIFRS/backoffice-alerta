@echo off
set JAVA_HOME=C:\Program Files\java
cd /d C:\Users\ederson.santos\Documents\backoffice-alerta
start "Backoffice Alerta API" cmd /k mvnw.cmd spring-boot:run
