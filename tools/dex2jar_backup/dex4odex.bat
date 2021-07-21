REM SET version=1.3.2
SET version=2.0b4

rmdir smali_out /S /Q
java -Xmx1024m -jar baksmali-%version%.jar -x %1 --api-level 17 -d lg-g2-framework -o smali_out
java -jar smali-%version%.jar -o %1.dex smali_out
rmdir smali_out /S /Q
dex2jar.bat %1.dex
