#!/bin/bash

set -e

PROJECT=$1
TARGET=$2
VERSION=$3

if [[ ${TARGET} == "production" ]]; then PROJECT="production"; fi
if [[ ${TARGET} == "demo" ]]; then PROJECT="demo"; fi
if [[ ${TARGET} == "uat" ]]; then PROJECT="uat"; fi
if [[ ${TARGET} == "sysint" ]]; then PROJECT="sysint"; fi

if [[ (${TARGET} == "local") ]]; then HOST=ifs-local; else HOST=prod.ifs-test-clusters.com; fi

if [ -z "$bamboo_openshift_svc_account_token" ]; then  SVC_ACCOUNT_TOKEN=$(oc whoami -t); else SVC_ACCOUNT_TOKEN=${bamboo_openshift_svc_account_token}; fi

SVC_ACCOUNT_CLAUSE="--namespace=${PROJECT} --token=${SVC_ACCOUNT_TOKEN} --server=https://console.prod.ifs-test-clusters.com:443 --insecure-skip-tls-verify=true"
REGISTRY_TOKEN=${SVC_ACCOUNT_TOKEN};

ROUTE_DOMAIN=apps.$HOST
REGISTRY=docker-registry-default.apps.prod.ifs-test-clusters.com
INTERNAL_REGISTRY=172.30.80.28:5000

echo "Resetting the $PROJECT Openshift project"

function dbReset() {
    until oc create -f os-files-tmp/db-reset/66-dbreset.yml ${SVC_ACCOUNT_CLAUSE}
    do
      oc delete -f os-files-tmp/db-reset/66-dbreset.yml ${SVC_ACCOUNT_CLAUSE}
      sleep 10
    done

    oc rsh $(oc get pods | grep data-service | awk '{ print $1 }') /bin/bash -c 'cd /mnt/ifs_storage && ls | grep -v .trashcan | xargs rm -rf'
}

function blockUntilServiceIsUp() {
    UNREADY_PODS=1
    while [ ${UNREADY_PODS} -ne "0" ]
    do
        UNREADY_PODS=$(oc get pods  ${SVC_ACCOUNT_CLAUSE} -o custom-columns='NAME:{.metadata.name},READY:{.status.conditions[?(@.type=="Ready")].status}' | grep -v True | sed 1d | wc -l)
        oc get pods ${SVC_ACCOUNT_CLAUSE}
        echo "$UNREADY_PODS pods still not ready"
        sleep 5s
    done
    oc get routes ${SVC_ACCOUNT_CLAUSE}
}

function shibInit() {
     oc rsh ${SVC_ACCOUNT_CLAUSE} $(oc get pods  ${SVC_ACCOUNT_CLAUSE} | awk '/ldap/ { print $1 }') /usr/local/bin/ldap-sync-from-ifs-db.sh ifs-database
}

function createProject() {
    until oc new-project $PROJECT ${SVC_ACCOUNT_CLAUSE}
    do
      oc delete project $PROJECT ${SVC_ACCOUNT_CLAUSE} || true
      sleep 10
    done
}

. $(dirname $0)/deploy-functions.sh

# Entry point
cleanUp
cloneConfig
tailorAppInstance
injectDBVariables
injectLDAPVariables
injectFlywayVariables

if [[ (${TARGET} != "local") ]]
then
    useContainerRegistry
    pushDBResetImages
fi

dbReset

echo Waiting for completion
while [ "$(oc get jobs dbreset -o go-template --template '{{.status.completionTime}}' ${SVC_ACCOUNT_CLAUSE})" == '<no value>' ]
do
  echo -n .
  sleep 5
done

[ "$(oc get -o template job dbreset --template={{.status.succeeded}} ${SVC_ACCOUNT_CLAUSE})" != 1 ] && exit -1
exit 0
