@echo off
for /d %%B in (C:\Users\vovak\myGit\Multithreaded-programming\*) do (
	cd %%B
	rd /q /s .git
	cd ..
)