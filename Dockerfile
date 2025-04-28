#syntax=harbor.leops.local/library/docker/dockerfile:1

FROM harbor.leops.local/common/tools/maven:3 AS builder

ARG APP_ENV=test \
    APP=undefine \
    GIT_BRANCH= \
    GIT_COMMIT_ID=

ENV APP_ENV=$APP_ENV \
    APP=$APP \
    GIT_BRANCH=$GIT_BRANCH \
    GIT_COMMIT_ID=$GIT_COMMIT_ID

WORKDIR /app_build

# Maven dependencies
ADD pom.xml ./pom.xml
RUN --mount=type=cache,id=${APP}-maven-repo,target=/usr/share/maven/ref/repository \
    mvn -B clean install -DskipTests -Dcheckstyle.skip -Dasciidoctor.skip -Djacoco.skip -Dmaven.gitcommitid.skip -Dspring-boot.repackage.skip -Dmaven.exec.skip=true -Dmaven.test.skip=true -Dmaven.compile.skip=true -Dmaven.resources.skip=true -Dmaven.javadoc.skip=true -Dmaven.install.skip=true -Dmaven.jar.skip=true

# Build
COPY ./ .
RUN --mount=type=cache,id=${APP}-maven-repo,target=/usr/share/maven/ref/repository \
    mvn clean package -Dmaven.test.skip=true 

#
# ---- 运行环境 ----
FROM harbor.leops.local/common/runtime/openjdk:24-debian11 AS running

ARG APP_ENV=test \
    APP=undefine
ENV APP_ENV=$APP_ENV \
    APP=$APP

WORKDIR /app

COPY --from=builder /app_build/target/*.jar /app/$APP.jar

ENTRYPOINT ["/bin/bash", "-c", "exec java -Djava.security.egd=file:/dev/./urandom ${JAVA_OPTS} -Dfile.encoding=UTF8 -jar /app/${APP}.jar --spring.profiles.actvie=${APP_ENV} --server.port=${SERVER_PORT:-8080}"]