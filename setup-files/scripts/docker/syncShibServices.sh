#!/bin/bash
function executeMySQLCommand {
    mysql ifs -uroot -ppassword -hifs-database -N -s -e "$1" 2>/dev/null
}

export -f executeMySQLCommand

function addUserToShibboleth {
    emailAddress=$1

    system_user=$(executeMySQLCommand "select system_user from user where email='${emailAddress}';")

    if [ "${system_user}" == "1" ]; then

      echo "Skipping adding user ${emailAddress} to Shibboleth as they are a System User and as such have no login"


    else

      echo "Adding User ${emailAddress} from MySQL in Shibboleth"

      response=$(curl -s -k -d "{\"email\": \"${emailAddress}\",\"password\": \"Passw0rd\"}" -H 'Content-type: application/json' -H "api-key: 1234567890" https://auth.local-dev:9443/regapi/identities/)
      uuid=$(echo ${response} | sed 's/.*"uuid":"\([^"]*\)".*/\1/g')
      executeMySQLCommand "update user set uid='${uuid}' where email='${emailAddress}';"

      userStatus=$(executeMySQLCommand "select status from user where email='${emailAddress}';")

      if [ "${userStatus}" == "ACTIVE" ]; then
        echo "User ${emailAddress} is active in MySQL, so activating them in Shibboleth"
        curl -s -X PUT -k -H 'Content-type: application/json' -H "api-key: 1234567890" https://auth.local-dev:9443/regapi/identities/${uuid}/activateUser
      fi

    fi
}

export -f addUserToShibboleth

cat <<'END'
              SINKING THE SHIB!!!
                   ,:',:`,:'
                __||_||_||_||___
           ____[""""""""""""""""]___
           \ " '''''''''''''''''''' \
    ~~^~^~^~^~^^~^~^~^~^~^~^~^~~^~^~^~^~~^~^
END

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $BASEDIR

docker-compose -f ../../../docker-compose-services.yml exec -T ldap /usr/local/bin/ldap-delete-all-users.sh

mysql ifs -uroot -ppassword -hifs-database -N -s -e "select email from user;" 2>/dev/null | xargs -I{} bash -c "addUserToShibboleth {}"

cat <<'END'

          ____
     ,' ._|    \
     :__: :    |
      --: :    |\o
~~^~^~~~^~^~^^~~~~^~^~^~~

       SHIB SUNK! 

END
