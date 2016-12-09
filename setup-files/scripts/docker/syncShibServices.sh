#!/bin/bash
function executeMySQLCommand {
    mysql ifs -uroot -ppassword -hifs-database -N -s -e "$1" 2>/dev/null
}

export -f executeMySQLCommand

function addUserToShibboleth {
    emailAddress=$1

    system_user=$(executeMySQLCommand "select system_user from user where email='${emailAddress}';")

    if [ "${system_user}" == "1" ]; then

      #printf "Skipping adding user ${emailAddress} to Shibboleth as they are a System User and as such have no login\n"
      printf "\r"

    else

      #printf "\033[K${num}/${NUMUSERS} Adding User ${emailAddress} from MySQL in Shibboleth\r"

      response=$(curl -s -k -d "{\"email\": \"${emailAddress}\",\"password\": \"Passw0rd\"}" -H 'Content-type: application/json' -H "api-key: 1234567890" https://ifs-local-dev/regapi/identities/)
      uuid=$(echo ${response} | sed 's/.*"uuid":"\([^"]*\)".*/\1/g')
      executeMySQLCommand "update user set uid='${uuid}' where email='${emailAddress}';"

      userStatus=$(executeMySQLCommand "select status from user where email='${emailAddress}';")

      if [ "${userStatus}" == "ACTIVE" ]; then
        #printf "\033[K${num}/${NUMUSERS} User ${emailAddress} is active in MySQL, so activating them in Shibboleth\r"
        curl -s -X PUT -k -H 'Content-type: application/json' -H "api-key: 1234567890" https://ifs-local-dev/regapi/identities/${uuid}/activateUser
      fi

    fi
    echo 1
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

for item in $( docker-compose ps -q shib ); do
    docker cp _delete-shib-users-remote.sh ${item}:/tmp/_delete-shib-users-remote.sh
done

docker-compose exec -T shib /tmp/_delete-shib-users-remote.sh

NUMUSERS=`mysql ifs -uroot -ppassword -hifs-database -N -s -e "select count(email) from user;" 2>/dev/null`

mysql ifs -uroot -ppassword -hifs-database -N -s -e "select email from user;" 2>/dev/null | xargs -I{} bash -c "addUserToShibboleth {}" | pv -s $((2+(NUMUSERS*2))) -i 0.1 >/dev/null

cat <<'END'

          ____
     ,' ._|    \
     :__: :    |
      --: :    |\o
~~^~^~~~^~^~^^~~~~^~^~^~~

       SHIB SUNK! 

END
