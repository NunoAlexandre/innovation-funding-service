#!/usr/bin/env bash

set -e

PROJECT=$1
TARGET=$2
VERSION=$3

if [[ ${TARGET} == "production" ]]; then PROJECT="production"; fi
if [[ ${TARGET} == "demo" ]]; then PROJECT="demo"; fi
if [[ ${TARGET} == "uat" ]]; then PROJECT="uat"; fi

if [[ (${TARGET} == "local") ]]
then
    HOST=ifs-local
else
    HOST=prod.ifs-test-clusters.com
fi

ROUTE_DOMAIN=apps.$HOST
REGISTRY=docker-registry-default.apps.prod.ifs-test-clusters.com
INTERNAL_REGISTRY=172.30.80.28:5000

SVC_ACCOUNT_TOKEN=${bamboo_openshift_svc_account_token}
SVC_ACCOUNT_CLAUSE="--namespace=${PROJECT} --token=${SVC_ACCOUNT_TOKEN} --server=https://console.prod.ifs-test-clusters.com:443 --insecure-skip-tls-verify=true"

if [ -z "$SVC_ACCOUNT_TOKEN" ]; then REGISTRY_TOKEN=$(oc whoami -t); else REGISTRY_TOKEN=${SVC_ACCOUNT_TOKEN}; fi


function upgradeServices {
    # data-service
    oc apply -f os-files-tmp/31-data-service.yml ${SVC_ACCOUNT_CLAUSE}
    sleep 90
    oc rollout status dc/data-service --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}

    # services
    oc apply -f os-files-tmp/4-application-service.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/41-assessment-svc.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/42-competition-mgt-svc.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/43-project-setup-mgt-svc.yml ${SVC_ACCOUNT_CLAUSE}
    oc apply -f os-files-tmp/44-project-setup-svc.yml ${SVC_ACCOUNT_CLAUSE}

    # shib & idp
    if [[ ${TARGET} == "production" || ${TARGET} == "demo" || ${TARGET} == "uat" ]]
    then
        oc apply ${SVC_ACCOUNT_CLAUSE} -f os-files-tmp/shib/named-envs/56-${TARGET}-idp.yml
    else
        oc apply ${SVC_ACCOUNT_CLAUSE} -f os-files-tmp/shib/56-idp.yml
    fi

    oc apply ${SVC_ACCOUNT_CLAUSE} -f os-files-tmp/shib/5-shib.yml

    sleep 90
    oc rollout status dc/application-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}
    oc rollout status dc/assessment-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}
    oc rollout status dc/competition-mgt-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}
    oc rollout status dc/project-setup-mgt-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}
    oc rollout status dc/project-setup-svc --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}
    oc rollout status dc/idp --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}
    oc rollout status dc/shib --request-timeout='5m' ${SVC_ACCOUNT_CLAUSE}
}


. $(dirname $0)/deploy-functions.sh

# Entry point
cleanUp
cloneConfig
tailorAppInstance

useContainerRegistry
upgradeServices

if [[ ${TARGET} == "production" || ${TARGET} == "uat" ]]
then
    # We only scale up data-service once data-service started up and performed the Flyway migrations on one thread
    scaleDataService
fi