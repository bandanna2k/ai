#!/bin/bash
set -e

docker run -d \
  --rm \
  --name runner-app-db \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=mydb \
  -p 3306:3306 \
  mysql

echo "Waiting for MySQL..."
sleep 10

echo "Unzipping dump files"
unzip -n dump.zip

echo "Uploading mysql dump to the database"
docker exec -i runner-app-db mysql -uroot -prootpass mydb < mysqldump.parkrun_stats.sql
docker exec -i runner-app-db mysql -uroot -prootpass mydb < mysqldump.parkrun_stats_NZ.sql

# To login to the MySQL database, run the following command:
# docker exec -it runner-app-db mysql -uroot -prootpass

#+-------------------------+
#| Tables_in_parkrun_stats |
#+-------------------------+
#| athlete                 |
#| course                  |
#+-------------------------+

#+----------------------------+
#| Tables_in_parkrun_stats_NZ |
#+----------------------------+
#| course_event_summary       |
#| event_volunteer            |
#| result                     |
#+----------------------------+