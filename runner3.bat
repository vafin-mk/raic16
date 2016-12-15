start /B java -cp "C:\Projects\kotlinc\lib\kotlin-runtime.jar;jars\v7.jar" Runner 127.0.0.1 31002 0000000000000000 > log2.log
timeout /t 2 /nobreak
start /B java -cp "C:\Projects\kotlinc\lib\kotlin-runtime.jar;jars\v7.jar" Runner 127.0.0.1 31003 0000000000000000 > log3.log
timeout /t 2 /nobreak
start /B java -cp "C:\Projects\kotlinc\lib\kotlin-runtime.jar;jars\v7.jar" Runner 127.0.0.1 31004 0000000000000000 > log4.log
