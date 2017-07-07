#/bin/bash

set -e

## TODO DW - remove debug flag
set -x

REPLACE_REPLACEMENT_TOKEN_EXTRACTOR="s/^REPLACE('\(.*\)')$/\1/g"

MASK_REPLACEMENT_INDEX_EXTRACTOR="s/^MASK(\([0-9]\+\),[ ]*'\(.\)')$/\1/g"
MASK_REPLACEMENT_TOKEN_EXTRACTOR="s/^MASK([0-9]\+,[ ]*'\(.\)')$/\1/g"

INTEGER_REPLACEMENT_TOKEN_EXTRACTOR="s/^INTEGER(\(.*\))$/\1/g"

DIFFERENT_IF_NUMBER_REPLACEMENT_TOKEN_EXTRACTOR="s/^DIFFERENT_IF_NUMBER(\(.*\))$/\1/g"

# This function is able to generate SQL rewrite statements for common anonymisation techniques e.g. from the
# rewrite rule "MASK(2, 'X')", this function will generate the SQL necessary to reproduce the masking of
# all characters past the 2nd character with 'X'.
#
# If the rewrite rule passed in is not recognised as a common anonymisation technique, the unchanged rewrite rule
# will just be returned as-is
function generate_rewrite_from_rule() {

    column_name="$1"
    replacement="$2"

    # this case generates the SQL from a rewrite rule like "REPLACE('X')"
    replace_test=$(echo "$replacement" | sed "$REPLACE_REPLACEMENT_TOKEN_EXTRACTOR")
    if [[ "$replace_test" != "$replacement" ]]; then
        mask_token=$(echo "$replacement" | sed "$REPLACE_REPLACEMENT_TOKEN_EXTRACTOR")
        mask_token_length=$(expr length "${mask_token}")
        echo "SUBSTR(REPEAT('$mask_token', ((CHAR_LENGTH($column_name) / $mask_token_length) + 1)), 1, CHAR_LENGTH($column_name))"
        exit 0
    fi

    # this case generates the SQL from a rewrite rule like "MASK(1, 'X')"
    replace_test=$(echo "$replacement" | sed "$MASK_REPLACEMENT_INDEX_EXTRACTOR")
    if [[ "$replace_test" != "$replacement" ]]; then
        mask_index=$(echo "$replacement" | sed "$MASK_REPLACEMENT_INDEX_EXTRACTOR")
        mask_token=$(echo "$replacement" | sed "$MASK_REPLACEMENT_TOKEN_EXTRACTOR")
        echo "CONCAT(SUBSTR($column_name, 1, $mask_index), REPEAT('$mask_token', CHAR_LENGTH($column_name) - $mask_index))"
        exit 0
    fi

    # this case generates the SQL from a rewrite rule like "INTEGER(0.5)"
    replace_test=$(echo "$replacement" | sed "$INTEGER_REPLACEMENT_TOKEN_EXTRACTOR")
    if [[ "$replace_test" != "$replacement" ]]; then

        deviation_percent=$(echo "$replacement" | sed "$INTEGER_REPLACEMENT_TOKEN_EXTRACTOR")
        deviation_percent_times_two=$((deviation_percent * 2))

        echo "ROUND($column_name * (1 + ((RAND() * $deviation_percent_times_two) / 100) - ($deviation_percent / 100)), 0)"
        exit 0
    fi

    # this case generates the SQL from a rewrite rule like "INTEGER(0.5)"
    replace_test=$(echo "$replacement" | sed "$DIFFERENT_IF_NUMBER_REPLACEMENT_TOKEN_EXTRACTOR")
    if [[ "$replace_test" != "$replacement" ]]; then

        deviation_percent=$(echo "$replacement" | sed "$DIFFERENT_IF_NUMBER_REPLACEMENT_TOKEN_EXTRACTOR")
        deviation_percent_times_two=$((deviation_percent * 2))

        echo "IF($column_name REGEXP '^[0-9.]+$', ROUND($column_name * (1 + ((RAND() * $deviation_percent_times_two) / 100) - ($deviation_percent / 100)), 0), $column_name)"
        exit 0
    fi

    echo "$replacement"
}

# A function to generate a set of query_rules for proxysql to rewrite data as it is being selected by mysqldump.
# This takes rules defined in files in the /dump/rewrites folder and builds a set of proxysql configuration to apply
# those rewrites.  These rules will be written to /dump/query_rules
function generate_query_rules_for_proxysql() {

    files=(/dump/rewrites/*)

    # for each file within the /dump/rewrites folder
    rule_id=$((1))

    for i in "${files[@]}"; do

        # the table name is the name of the file e.g. "user"
        table_name=$(echo $i | sed "s/.*\///" | sed "s/.*\///" | sed "s/\..*//")

        # the column_array is an array of the column names that are rewrite candidates for this table e.g. "first_name"
        mapfile -t column_array < <(sed 's/^\(.*\)|.*$/\1/g' $i)

        # the column_rewrite_array is an array of the rewrite rules against each corresponding column name in the
        # column_array array e.g. "CONCAT(first_name, 'XXX')"
        mapfile -t column_rewrite_array < <(sed 's/^.*|\(.*\)$/\1/g' $i)

        # a query to find the base select statement that we wish mysqldump to issue against this table when running a
        # dump of its data
        full_select_statement_query="SELECT CONCAT('SELECT SQL_NO_CACHE ', \
            (SELECT GROUP_CONCAT(t.col) FROM \
               (SELECT COLUMN_NAME as col FROM INFORMATION_SCHEMA.COLUMNS \
                WHERE TABLE_NAME = '$table_name' \
                AND TABLE_SCHEMA = '$DB_NAME') t \
             WHERE t.col IS NOT NULL) , ' FROM $table_name' );"

        # the base select statement that we wish mysqldump to issue against this table when running a dump of its data
        full_select_statement_result=$(mysql -h$DB_HOST -u$DB_USER -p$DB_PASS -P$DB_PORT $DB_NAME -N -s -e "$full_select_statement_query")

        # now we replace every column name that we wish to replace with its replacement i.e. replace every entry from
        # column_array (e.g. "user") with its rewrite from column_rewrite_array (e.g. "CONCAT(first_name, 'XXX')")
        replacement_pattern=$full_select_statement_result
        for j in "${!column_array[@]}"; do

            # get the replacement rule as a SQL statement (if it is not one already e.g. MASK(1, 'X') will be looked
            # up and replaced with a masking SQL statement)
            final_rewrite=$(generate_rewrite_from_rule "${column_array[j]}" "${column_rewrite_array[j]}")

            # and swap out the original column in the select statement with this replacement (taking care to only
            # replace exact column names and not partial substrings of other column names!)
            replacement_pattern=$( echo "$replacement_pattern" | sed "s#\([ ,]\+\)${column_array[j]}\([ ,]\+\)#\1$final_rewrite\2#g" )
        done

        # and finally output this table's rewrite rule to /dump/query_rules
        if [ "$rule_id" -gt "1" ]; then
           echo '    ,' >> /dump/query_rules
        fi

        echo '    {' >> /dump/query_rules
        echo "        rule_id=$rule_id" >> /dump/query_rules
        echo '        active=1' >> /dump/query_rules
        match_pattern="^SELECT /\*!40001 SQL_NO_CACHE \*/ \* FROM \`$table_name\`"
        echo "        match_pattern=\"$match_pattern\"" >> /dump/query_rules
        echo "        replace_pattern=\"$replacement_pattern\"" >> /dump/query_rules
        echo '        destination_hostgroup=0' >> /dump/query_rules
        echo '        apply=1' >> /dump/query_rules
        echo '    }' >> /dump/query_rules

        rule_id=$((rule_id + 1))

    done
}

# a function to replace the <<QUERY_RULES>> token in proxysql.cnf with the rewrite rules stored within /dump/query_rules
function inject_query_rules_into_proxysql_cnf() {
    sed -i "/<<QUERY_RULES>>/{
        s/<<QUERY_RULES>>//g
        r /dump/query_rules
    }" /etc/proxysql.cnf
}

# a function to replace database configuration replacement tokens in proxysql.cnf with real values
function inject_db_configuration_into_proxysql_cnf() {
    sed -i "s#<<DB_USER>>#$DB_USER#g;s#<<DB_PASS>>#$DB_PASS#g;s#<<DB_NAME>>#$DB_NAME#g;s#<<DB_HOST>>#$DB_HOST#g;s#<<DB_PORT>>#$DB_PORT#g" /etc/proxysql.cnf
}

# the entrypoint into this script
generate_query_rules_for_proxysql
inject_query_rules_into_proxysql_cnf
inject_db_configuration_into_proxysql_cnf

## TODO DW - remove cat
cat /etc/proxysql.cnf