javac -source 1.8 -target 1.8 -d out src/spm/*.java
xcopy src\resources\* out\ /Y
REM java -cp out spm.SPMgui
jar cfe dist/SPM.jar spm.SPMgui -C out .
