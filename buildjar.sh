KOTLIN_HOME=/c/Projects/kotlinc
JAVA_HOME="/c/Program Files/Java/jdk1.8.0_77"
"$KOTLIN_HOME"/bin/kotlinc src/main/java/model/*.kt src/main/java/Strategy.kt src/main/java/MyStrategy.kt -d classes/ > compilation.log 2>&1
"$JAVA_HOME"/bin/javac -classpath classes/ -d classes/ src/main/java/Runner.java src/main/java/RemoteProcessClient.java >> compilation.log 2>&1
"$JAVA_HOME"/bin/jar cf "./jars/debug.jar" -C "./classes" . >> compilation.log 2>&1
