javac src\sena\*.java -d bin\sena
cd bin
jar cf "%FIJI%\plugins\Sena_Plugin.jar" *
cd ..
%RUNFIJI%