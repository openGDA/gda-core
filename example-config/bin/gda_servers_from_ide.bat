set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_35
set JAVA_OPTS=-Xms128m -Xmx1024m -XX:MaxPermSize=128m
c:\python27\python.exe  %1/bin/gda  --config=%2 --smart --trace servers -v --mode=%3 %4 %5 %6 %7