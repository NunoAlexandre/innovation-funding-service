#!/bin/bash

# A Munro: Put users in ifs db into the local ldap.
# Ensure uids are correct.
# Wipes all users on the ldap.
# Uses the same password for all users.

# Args:
# $1: db host (default ifs-database)
# $2: db (database name; default ifs)
# $3: db user (default root)
# $4: db user password (default 'password;)
# $5: db port (default 3306)

domain="dc=nodomain"

# The infamous user password: Passw0rd
password="e1NTSEF9b2lRZUF1OHNrR0VqQmhweUpmV01hOFF3M0dNK2xRd2Q="

host=$DB_HOST
#host=mysql
db=$DB_NAME
user=$DB_USER
pass=$DB_PASS
port=$DB_PORT

ldap_host=$LDAP_HOST
ldap_port=$LDAP_PORT

[ ! -z "$1" ] && host=$1
[ ! -z "$2" ] && db=$2
[ ! -z "$3" ] && user=$3
[ ! -z "$4" ] && pass=$4
[ ! -z "$5" ] && port=$5

echo host:$host
echo database:$db
echo user:$user
echo port:$port

wipeLdapUsers() {
  [ -z "$LDAP_PORT" ] && LDAP_PORT=8389

  ldapsearch -H ldap://$LDAP_HOST:$LDAP_PORT/ -b 'dc=nodomain' -s sub '(objectClass=person)' -x \
   | grep 'dn: ' \
   | cut -c4- \
   | xargs ldapdelete -H ldap://$LDAP_HOST:$LDAP_PORT/ -D 'cn=admin,dc=nodomain' -w test
}

executeMySQLCommand() {
    mysql $db -P $port -u $user --password=$pass -h $host -N -s -e "$1"
}

addUserToShibboleth() {

  email=$1

  uid=$(executeMySQLCommand "select uid from user where email='$email';")

  echo "dn: uid=$uid,$domain"
  echo "uid: $uid"
  echo "mail: $email"
  echo "sn:: IA=="
  echo "cn:: IA=="
  echo "objectClass: inetOrgPerson"
  echo "objectClass: person"
  echo "objectClass: top"
  echo "userPassword:: $password"
  echo "structuralObjectClass: inetOrgPerson"
  echo "employeeType: active"
  echo ""

}

# Main

wipeLdapUsers

for u in $(mysql $db -P $port -u $user --password=$pass -h $host -N -s -e "select email from user where status = 'ACTIVE' and system_user = 0;")
do
  addUserToShibboleth $u 
done | slapadd -c
