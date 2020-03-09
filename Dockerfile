FROM mozilla/sbt

WORKDIR /app
COPY src /app/src/
COPY build.sbt /app/
COPY project/build.properties /app/project/
COPY project/plugins.sbt /app/project/

RUN cd /app && sbt assembly

CMD ["java", "-jar", "/app/target/scala-2.13/randombackend-assembly-0.1.0-SNAPSHOT.jar"]