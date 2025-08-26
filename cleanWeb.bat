@echo off

if exist "./webserver/out" (
    rmdir /s /q "./webserver/out"
)

if exist "./webserver/target" (
    rmdir /s /q "./webserver/target"
)

if exist "./webserver/logs" (
    rmdir /s /q "./webserver/logs"
)

pause