FROM 192.168.1.52/system_containers/oraclejdk:8u192

MAINTAINER wangjinshan jinshan.wang@tenxcloud.com

ENV PROFILE         test
ENV MYSQL_HOST      127.0.0.1
ENV MYSQL_PORT      3306
ENV MYSQL_USER      root
ENV MYSQL_PASSWORD  tenxcloud

ADD 3scale-management.jar /app/app.jar

WORKDIR "/app"

EXPOSE 8080

ENTRYPOINT ["java", "-Xms256m", "-Xmx1g", \
            "-XX:+UnlockExperimentalVMOptions", \
            "-XX:+UseCGroupMemoryLimitForHeap", \
            "-Dspring.profiles.active=${PROFILE}",\
            "-Dmysql1.host=${MYSQL1_HOST}",\
            "-Dmysql1.port=${MYSQL1_PORT}",\
            "-Dmysql1.user=${MYSQL1_USER}",\
            "-Dmysql1.password=${MYSQL1_PASSWORD}",\
            "-Dmysql2.host=${MYSQL2_HOST}",\
            "-Dmysql2.port=${MYSQL2_PORT}",\
            "-Dmysql2.user=${MYSQL2_USER}",\
            "-Dmysql2.password=${MYSQL2_PASSWORD}",\
            "-Dpaas_url=${PAAS_URL}",\
            "-Dendpoint.sandbox=${ENDPOINT_SANDBOX}",\
            "-Dendpoint.production=${ENDPOINT_PRODUCTION}",\
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "/app/app.jar"]
