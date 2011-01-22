copy /V /Y sootOutput\sample\*.class build\classes\sample
copy /V /Y sootOutput\sample\*\*.class build\classes\sample
java -classpath build\classes\ sample.Sample
erase build\classes\sample\Sample.class

