CALL mvn clean package

PUSHD target
REN *.jar app.jar
POPD

CALL docker image build -t spring-boot-3-font -f Dockerfile-openjdk.txt .

PAUSE