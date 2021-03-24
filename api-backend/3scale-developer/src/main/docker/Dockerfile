FROM 192.168.1.52/system_containers/oraclejdk:8u192

MAINTAINER wangjinshan jinshan.wang@tenxcloud.com

ENV PROFILE         test
ENV MYSQL1_HOST      127.0.0.1
ENV MYSQL1_PORT      3306
ENV MYSQL1_USER      root
ENV MYSQL1_PASSWORD  tenxcloud
ENV SSO_APPSECRETID  sys_idevportal_dev
ENV SSO_APPSECRETKEY  513bc0500db14e6fe13ee53a57493ada
ENV LDAP_APPSECRETID  sys_apikfptdev_user
ENV SSO_TARGETURI  https://gw-inter.3scale-admin.devapps.hisense.com
ENV SSO_CALLAPITOKENURL  https://ssodev.hisense.com/sso-ws/tokenRestfulApi/getCallApiToken?appSecretId=sys_idevportal_dev&appSecretKey=513bc0500db14e6fe13ee53a57493ada
ENV SSO_CHECKLOGINURL  https://ssodev.hisense.com/sso-ws/ssoLoginRestfulApi/checkLogin
ENV LDAP_QUERYTOKENURL  https://ssodev.hisense.com/sso-ws/tokenRestfulApi/getLdapQueryToken?appSecretId=sys_apikfptdev_user&appSecretKey=0ba85b53250f54d58746c335e051567e&eidClass=ldap
ENV LDAP_QUERYINFOPARAMSURL  https://idmdev.hisense.com/esc-ws/services/ldapQueryInfoParams
ENV SSO_FORWARDBUSINESSSYSTEMURL  https://ssodev.hisense.com/sso-ws/ssoLoginRestfulApi/forwardBusinessSystem
ENV SSO_CHECKSSOLOGINTOKENURL  https://ssodev.hisense.com/sso-ws/ssoLoginRestfulApi/checkSsoLoginToken

ADD 3scale-developer.jar /app/app.jar

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
            "-Dsso.appsecretid=${SSO_APPSECRETID}",\
            "-Dsso.appsecretkey=${SSO_APPSECRETKEY}",\
            "-Dldap.appsecretid=${LDAP_APPSECRETID}",\
            "-Dsso.targeturi=${SSO_TARGETURI}",\
            "-Dsso.callapitokenurl=${SSO_CALLAPITOKENURL}",\
            "-Dsso.checkloginurl=${SSO_CHECKLOGINURL}",\
            "-Dldap.querytokenurl=${LDAP_QUERYTOKENURL}",\
            "-Dldap.queryinfoparamsurl=${LDAP_QUERYINFOPARAMSURL}",\
            "-Dsso.forwardbusinesssystemurl=${SSO_FORWARDBUSINESSSYSTEMURL}",\
            "-Dsso.checkssologintokenurl=${SSO_CHECKSSOLOGINTOKENURL}",\
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "/app/app.jar"]
