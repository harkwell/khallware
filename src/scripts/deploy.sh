#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export CATALINA_HOME=/var/lib/tomcat8
su khall -c /usr/local/bin/build.sh
rm -rf $CATALINA_HOME/webapps/*
cp ~khall/tmp/blah/khallware/target/apis.war $CATALINA_HOME/webapps/
service mysql start
mysql -uroot mysql <<EOF
CREATE DATABASE website;
CREATE USER 'api'@'localhost' IDENTIFIED BY 'api';
GRANT ALL PRIVILEGES ON website.* TO 'api'@'localhost' WITH GRANT OPTION;
EOF
mysql -uapi -papi website <~khall/tmp/blah/khallware/src/scripts/db_schema.sql
mysql -uapi -papi website <~khall/tmp/blah/khallware/src/scripts/db_load.sql
mysql -uapi -papi website <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO groups (name, description) VALUES ('friends', 'friends group');
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, _group) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@myhost.com',(SELECT id FROM groups WHERE name = 'guest'));
INSERT INTO landing (url, _group) VALUES ("/apis/v1/static/family.html",(SELECT id FROM groups WHERE name = 'family'));
INSERT INTO landing (url, _group) VALUES ("/apis/v1/static/friends.html",(SELECT id FROM groups WHERE name = 'friends'));
INSERT INTO quota (user, available, used) SELECT id AS user, 102400000, 0 FROM credentials;
EOF
$CATALINA_HOME/bin/startup.sh
