copy /V /Y sootOutput\sample\Sample.class build\classes\sample
java -classpath build\classes\ sample.Sample
erase build\classes\sample\Sample.class

