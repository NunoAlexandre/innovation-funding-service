echo "**This script requires / does the following:"
echo "**A local database with username: root and password: password"
echo "**An export from the live database called ifs_current_live.sql"
echo "Creating the database if it does not exist"
echo "CREATE SCHEMA IF NOT EXISTS ifs_current_migrated" | mysql -uroot -ppassword 
echo "**Clearing the ifs_current_live_migrated database"
cd ../ifs-data-service
./gradlew flywayClean -Dflyway.schemas=ifs_current_live_migrated
echo "**Importing the live database into the ifs_current_live_migrated"
mysql -uroot -ppassword ifs_current_live < ../baseline/ifs_current_live.sql
echo "**Applying the latest patches to the live database"
./gradlew flywayMigrate -Dflyway.schemas=ifs_current_live_migrated -Dflyway.locations="db/migration,db/setup"
echo "**Exporting the minimum data from the live"
echo "**TODO Knowing what needs to be here will require trial and error and we will need to check how it compares to UAT"
cd ../baseline
## TODO remove spend_profile when we find a table we actually need
mysqldump -uroot -ppassword ifs_current_live_migrated \
			    spend_profile \
                            --ignore-table=ifs_current_live.schema_version \
                            --no-create-info --extended-insert=false > "generated/db/setup/V0_1_4__Minimum_data.sql"




