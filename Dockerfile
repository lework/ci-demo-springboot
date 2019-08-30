FROM lework/oraclejdk:v8.131.11-slim

LABEL maintainer="lework <lework@yeah.net>"

ENV JAVA_OPTS="" \
    WORKSPACE=/src \
	SERVER_PORT=80

RUN mkdir ${WORKSPACE}

COPY target/demo.jar ${WORKSPACE}/app.jar

WORKDIR ${WORKSPACE}

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["/bin/sh", "-c", "java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Djava.security.egd=file:/dev/./urandom ${JAVA_OPTS} -jar ${WORKSPACE}/app.jar --server.port=${SERVER_PORT}"]
  