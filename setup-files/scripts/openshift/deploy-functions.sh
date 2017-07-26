#!/bin/bash

function isNamedEnvironment() {

    TARGET=$1

    if [[ ${TARGET} != "production" && ${TARGET} != "demo" && ${TARGET} != "uat" && ${TARGET} != "sysint" && ${TARGET} != "perf" ]]; then
        exit 1
    else
        exit 0
    fi
}

function getProjectName() {

    PROJECT=$1
    TARGET=$2

    if $(isNamedEnvironment $TARGET); then
        echo "$TARGET"
    else
        echo "$PROJECT"
    fi
}

function getSvcAccountToken() {

    if [ -z "$bamboo_openshift_svc_account_token" ]; then
        echo "$(oc whoami -t)";
    else
        echo "${bamboo_openshift_svc_account_token}";
    fi
}

function getHost() {

    TARGET=$1

    if [[ (${TARGET} == "local") ]]; then
      echo "ifs-local"
    elif [[ ${TARGET} == "production" ]]; then
      echo "apply-for-innovation-funding.service.gov.uk"
    else
      echo "prod.ifs-test-clusters.com"
    fi
}

function getRouteDomain() {

    TARGET=$1
    HOST=$2

    if [[ ${TARGET} == "production" ]]; then
      echo "$HOST"
    else
      echo "apps.$HOST"
    fi
}

function getRegistry() {

    if [[ (${TARGET} == "local") ]]; then
        echo "$(getLocalRegistryUrl)"
    else
        echo "docker-registry-default.apps.prod.ifs-test-clusters.com"
    fi
}

function getInternalRegistry() {

    if [[ (${TARGET} == "local") ]]; then
        echo "$(getLocalRegistryUrl)"
    else
        echo "172.30.80.28:5000"
    fi
}

function getSvcAccountClause() {

    TARGET=$1
    PROJECT=$2
    SVC_ACCOUNT_TOKEN=$3

    if [[ (${TARGET} == "local") ]]; then
        SVC_ACCOUNT_CLAUSE_SERVER_PART='localhost:8443'
    else
        SVC_ACCOUNT_CLAUSE_SERVER_PART='console.prod.ifs-test-clusters.com:443'
    fi

    echo "--namespace=${PROJECT} --token=${SVC_ACCOUNT_TOKEN} --server=https://${SVC_ACCOUNT_CLAUSE_SERVER_PART} --insecure-skip-tls-verify=true"
}

function convertFileToBlock() {
    cat "$1" | tr -d '\r' | tr '\n' '^' | sed "s/\^/<<>>/g" | rev | cut -c 5- | rev
}

function injectDBVariables() {
    if [ -z "$DB_USER" ]; then echo "Set DB_USER environment variable"; exit -1; fi
    if [ -z "$DB_PASS" ]; then echo "Set DB_PASS environment variable"; exit -1; fi
    if [ -z "$DB_NAME" ]; then echo "Set DB_NAME environment variable"; exit -1; fi
    if [ -z "$DB_HOST" ]; then echo "Set DB_HOST environment variable"; exit -1; fi

    DB_PORT=${DB_PORT:-3306}

    sed -i.bak "s#<<DB-USER>>#$DB_USER#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<DB-PASS>>#$DB_PASS#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<DB-NAME>>#$DB_NAME#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<DB-HOST>>#$DB_HOST#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<DB-PORT>>#$DB_PORT#g" os-files-tmp/db-reset/*.yml

    sed -i.bak "s#<<DB-USER>>#$DB_USER#g" os-files-tmp/db-anonymised-data/*.yml
    sed -i.bak "s#<<DB-PASS>>#$DB_PASS#g" os-files-tmp/db-anonymised-data/*.yml
    sed -i.bak "s#<<DB-NAME>>#$DB_NAME#g" os-files-tmp/db-anonymised-data/*.yml
    sed -i.bak "s#<<DB-HOST>>#$DB_HOST#g" os-files-tmp/db-anonymised-data/*.yml
    sed -i.bak "s#<<DB-PORT>>#$DB_PORT#g" os-files-tmp/db-anonymised-data/*.yml
}


function injectFlywayVariables() {
    [ -z "$FLYWAY_LOCATIONS" ] && { echo "Set FLYWAY_LOCATIONS environment variable"; exit -1; }
    sed -i.bak "s#<<FLYWAY-LOCATIONS>>#${FLYWAY_LOCATIONS}#g" os-files-tmp/db-reset/*.yml
}

function injectLDAPVariables() {
    if [ -z "$LDAP_HOST" ]; then echo "Set LDAP_HOST environment variable"; exit -1; fi
    LDAP_PORT=${LDAP_PORT:-8389}
    sed -i.bak "s#<<LDAP-HOST>>#$LDAP_HOST#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<LDAP-PORT>>#$LDAP_PORT#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<LDAP-PASS>>#$LDAP_PASS#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<LDAP-DOMAIN>>#$LDAP_DOMAIN#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#<<LDAP-SCHEME>>#$LDAP_SCHEME#g" os-files-tmp/db-reset/*.yml
}

function tailorAppInstance() {
    if [ -z "$SSLCERTFILE" ]; then echo "Set SSLCERTFILE, SSLCACERTFILE, and SSLKEYFILE environment variables"; exit -1; fi
    sed -i.bak -e $"s#<<SSLCERT>>#$(convertFileToBlock $SSLCERTFILE)#g" -e 's/<<>>/\\n/g' os-files-tmp/shib/*.yml
    sed -i.bak -e $"s#<<SSLCERT>>#$(convertFileToBlock $SSLCERTFILE)#g" -e 's/<<>>/\\n/g' os-files-tmp/shib/named-envs/*.yml
    sed -i.bak -e $"s#<<SSLCACERT>>#$(convertFileToBlock $SSLCACERTFILE)#g" -e 's/<<>>/\\n/g' os-files-tmp/shib/*.yml
    sed -i.bak -e $"s#<<SSLCACERT>>#$(convertFileToBlock $SSLCACERTFILE)#g" -e 's/<<>>/\\n/g' os-files-tmp/shib/named-envs/*.yml
    sed -i.bak -e $"s#<<SSLKEY>>#$(convertFileToBlock $SSLKEYFILE)#g" -e 's/<<>>/\\n/g' os-files-tmp/shib/*.yml
    sed -i.bak -e $"s#<<SSLKEY>>#$(convertFileToBlock $SSLKEYFILE)#g" -e 's/<<>>/\\n/g' os-files-tmp/shib/named-envs/*.yml

    if [[ ${TARGET} == "production" ]]
    then
      sed -i.bak "s/<<SHIB-IDP-ADDRESS>>/auth.$ROUTE_DOMAIN/g" os-files-tmp/*.yml
      sed -i.bak "s/<<SHIB-IDP-ADDRESS>>/auth.$ROUTE_DOMAIN/g" os-files-tmp/shib/*.yml
      sed -i.bak "s/<<SHIB-IDP-ADDRESS>>/auth.$ROUTE_DOMAIN/g" os-files-tmp/shib/named-envs/*.yml

      sed -i.bak "s/<<SHIB-ADDRESS>>/$ROUTE_DOMAIN/g" os-files-tmp/*.yml
      sed -i.bak "s/<<SHIB-ADDRESS>>/$ROUTE_DOMAIN/g" os-files-tmp/shib/*.yml
      sed -i.bak "s/<<SHIB-ADDRESS>>/$ROUTE_DOMAIN/g" os-files-tmp/shib/named-envs/*.yml

    else
      sed -i.bak "s/<<SHIB-IDP-ADDRESS>>/auth-$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/*.yml
      sed -i.bak "s/<<SHIB-IDP-ADDRESS>>/auth-$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/db-reset/*.yml
      sed -i.bak "s/<<SHIB-IDP-ADDRESS>>/auth-$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/shib/*.yml
      sed -i.bak "s/<<SHIB-IDP-ADDRESS>>/auth-$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/shib/named-envs/*.yml

      sed -i.bak "s/<<SHIB-ADDRESS>>/$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/*.yml
      sed -i.bak "s/<<SHIB-ADDRESS>>/$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/db-reset/*.yml
      sed -i.bak "s/<<SHIB-ADDRESS>>/$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/shib/*.yml
      sed -i.bak "s/<<SHIB-ADDRESS>>/$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/shib/named-envs/*.yml
    fi

    sed -i.bak "s/<<MAIL-ADDRESS>>/mail-$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/mail/*.yml
    sed -i.bak "s/<<ADMIN-ADDRESS>>/admin-$PROJECT.$ROUTE_DOMAIN/g" os-files-tmp/spring-admin/*.yml

    if [[ ${TARGET} == "production" || ${TARGET} == "demo" || ${TARGET} == "uat" || ${TARGET} == "sysint" || ${TARGET} == "perf" ]]
    then
        sed -i.bak "s/claimName: file-upload-claim/claimName: ${TARGET}-file-upload-claim/g" os-files-tmp/*.yml

        if [[ ${TARGET} == "demo" ]]
        then
            if [ -z "${bamboo_demo_ldap_password}" ]; then echo "Set bamboo_${TARGET}_ldap_password environment variable"; exit -1; fi
            sed -i.bak "s/<<LDAP-PASSWORD>>/${bamboo_demo_ldap_password}/g" os-files-tmp/shib/named-envs/*.yml
        fi
        if [[ ${TARGET} == "sysint" ]]
        then
            if [ -z "${bamboo_sysint_ldap_password}" ]; then echo "Set bamboo_${TARGET}_ldap_password environment variable"; exit -1; fi
            sed -i.bak "s/<<LDAP-PASSWORD>>/${bamboo_sysint_ldap_password}/g" os-files-tmp/shib/named-envs/*.yml
        fi
        if [[ ${TARGET} == "perf" ]]
        then
            if [ -z "${bamboo_perf_ldap_password}" ]; then echo "Set bamboo_${TARGET}_ldap_password environment variable"; exit -1; fi
            sed -i.bak "s/<<LDAP-PASSWORD>>/${bamboo_perf_ldap_password}/g" os-files-tmp/shib/named-envs/*.yml
        fi
        if [[ ${TARGET} == "uat" ]]
        then
            if [ -z "${bamboo_uat_ldap_password}" ]; then echo "Set bamboo_${TARGET}_ldap_password environment variable"; exit -1; fi
            sed -i.bak "s/<<LDAP-PASSWORD>>/${bamboo_uat_ldap_password}/g" os-files-tmp/shib/named-envs/*.yml
        fi
        if [[ ${TARGET} == "production" ]]
        then
            if [ -z "${bamboo_production_ldap_password}" ]; then echo "Set bamboo_${TARGET}_ldap_password environment variable"; exit -1; fi
            sed -i.bak "s/<<LDAP-PASSWORD>>/${bamboo_production_ldap_password}/g" os-files-tmp/shib/named-envs/*.yml
        fi
    fi

    if [[ ${TARGET} == "production" || ${TARGET} == "uat" || ${TARGET} == "perf"  ]]
    then
        sed -i.bak "s/replicas: 1/replicas: 2/g" os-files-tmp/4*.yml
    fi

    if [[ ${TARGET} == "local" ]]
    then
        replacePersistentFileClaim
    fi
}

function useContainerRegistry() {

    sed -i.bak "s/imagePullPolicy: IfNotPresent/imagePullPolicy: Always/g" os-files-tmp/*.yml
    sed -i.bak "s/imagePullPolicy: IfNotPresent/imagePullPolicy: Always/g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s/imagePullPolicy: IfNotPresent/imagePullPolicy: Always/g" os-files-tmp/db-anonymised-data/*.yml
    sed -i.bak "s/imagePullPolicy: IfNotPresent/imagePullPolicy: Always/g" os-files-tmp/robot-tests/*.yml

    sed -i.bak "s# innovateuk/# ${INTERNAL_REGISTRY}/${PROJECT}/#g" os-files-tmp/*.yml
    sed -i.bak "s# innovateuk/# ${INTERNAL_REGISTRY}/${PROJECT}/#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s# innovateuk/# ${INTERNAL_REGISTRY}/${PROJECT}/#g" os-files-tmp/db-anonymised-data/*.yml
    sed -i.bak "s# innovateuk/# ${INTERNAL_REGISTRY}/${PROJECT}/#g" os-files-tmp/shib/*.yml
    sed -i.bak "s# innovateuk/# ${INTERNAL_REGISTRY}/${PROJECT}/#g" os-files-tmp/shib/named-envs/*.yml
    sed -i.bak "s# innovateuk/# ${INTERNAL_REGISTRY}/${PROJECT}/#g" os-files-tmp/robot-tests/*.yml

    sed -i.bak "s#1.0-SNAPSHOT#${VERSION}#g" os-files-tmp/*.yml
    sed -i.bak "s#1.0-SNAPSHOT#${VERSION}#g" os-files-tmp/db-reset/*.yml
    sed -i.bak "s#1.0-SNAPSHOT#${VERSION}#g" os-files-tmp/db-anonymised-data/*.yml
    sed -i.bak "s#1.0-SNAPSHOT#${VERSION}#g" os-files-tmp/shib/*.yml
    sed -i.bak "s#1.0-SNAPSHOT#${VERSION}#g" os-files-tmp/shib/named-envs/*.yml
    sed -i.bak "s#1.0-SNAPSHOT#${VERSION}#g" os-files-tmp/robot-tests/*.yml
}

function pushApplicationImages() {
    docker tag innovateuk/data-service:latest \
        ${REGISTRY}/${PROJECT}/data-service:${VERSION}
    docker tag innovateuk/project-setup-service:latest \
        ${REGISTRY}/${PROJECT}/project-setup-service:${VERSION}
    docker tag innovateuk/project-setup-management-service:latest \
        ${REGISTRY}/${PROJECT}/project-setup-management-service:${VERSION}
    docker tag innovateuk/competition-management-service:latest \
        ${REGISTRY}/${PROJECT}/competition-management-service:${VERSION}
    docker tag innovateuk/assessment-service:latest \
        ${REGISTRY}/${PROJECT}/assessment-service:${VERSION}
    docker tag innovateuk/application-service:latest \
        ${REGISTRY}/${PROJECT}/application-service:${VERSION}
    docker tag innovateuk/front-door-service:latest \
        ${REGISTRY}/${PROJECT}/front-door-service:${VERSION}
    docker tag innovateuk/sp-service:latest \
        ${REGISTRY}/${PROJECT}/sp-service:${VERSION}
    docker tag innovateuk/idp-service:latest \
        ${REGISTRY}/${PROJECT}/idp-service:${VERSION}
    docker tag innovateuk/ldap-service:latest \
        ${REGISTRY}/${PROJECT}/ldap-service:${VERSION}

    docker login -p ${REGISTRY_TOKEN} -u unused ${REGISTRY}

    docker push ${REGISTRY}/${PROJECT}/data-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/project-setup-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/project-setup-management-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/competition-management-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/assessment-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/application-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/front-door-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/sp-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/idp-service:${VERSION}
    docker push ${REGISTRY}/${PROJECT}/ldap-service:${VERSION}
}

function pushDBResetImages() {
    docker tag innovateuk/dbreset:${VERSION} \
        ${REGISTRY}/${PROJECT}/dbreset:${VERSION}

    docker login -p ${REGISTRY_TOKEN} -u unused ${REGISTRY}

    docker push ${REGISTRY}/${PROJECT}/dbreset:${VERSION}
}

function pushAnonymisedDatabaseDumpImages() {
    docker tag innovateuk/db-anonymised-data:${VERSION} \
        ${REGISTRY}/${PROJECT}/db-anonymised-data:${VERSION}

    docker login -p ${REGISTRY_TOKEN} -e unused -u unused ${REGISTRY}

    docker push ${REGISTRY}/${PROJECT}/db-anonymised-data:${VERSION}
}

function cloneConfig() {
    cp -r os-files os-files-tmp
}

function cleanUp() {
    rm -rf os-files-tmp
    rm -rf shibboleth
}

function scaleDataService() {
    oc scale dc data-service --replicas=2 ${SVC_ACCOUNT_CLAUSE}
}

function createProject() {
    until oc new-project $PROJECT ${SVC_ACCOUNT_CLAUSE}
    do
      oc delete project $PROJECT ${SVC_ACCOUNT_CLAUSE} || true
      sleep 10
    done
}

function createProjectIfNecessaryForNonNamedEnvs() {
    if ! $(isNamedEnvironment $TARGET); then
        createProject
    fi
}