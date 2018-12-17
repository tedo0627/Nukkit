call mvn package
mkdir "run\deploy"
cd "target"
copy /Y "nukkit-1.0-SNAPSHOT.jar" "..\run\deploy\nukkit.jar"
cd "..\run\deploy"
start java -jar nukkit.jar