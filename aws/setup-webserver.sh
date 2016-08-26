#!/bin/bash

echo khallware: init script
#-------------------------------------------------------------------------------
REPO=khallware
REPOTOP=/opt/khallware/gitrepo
AWSREGION=$(curl http://169.254.169.254/latest/meta-data/placement/availability-zone |sed 's#.$##')
EC2HOST=$(curl http://169.254.169.254/latest/meta-data/public-hostname)

echo "khallware: setup system"
#-------------------------------------------------------------------------------
cp $REPOTOP/$REPO/aws/khall-prefs.sh /etc/profile.d/
echo set editing-mode vi >>/etc/inputrc

echo khallware: start git daemon
#-------------------------------------------------------------------------------
#touch $REPOTOP/$REPO/.git/git-daemon-export-ok
#nohup git daemon --reuseaddr --verbose --base-path=$REPOTOP/$REPO \
#   --enable=receive-pack -- $REPOTOP &

echo khallware: configure nginx
#-------------------------------------------------------------------------------
cp $REPOTOP/$REPO/aws/index.html /usr/share/nginx/html/
cp $REPOTOP/$REPO/aws/nginx-khallware.conf /etc/nginx/conf.d/
sed -i -e "s#XXXX#$EC2HOST#g" /etc/nginx/conf.d/nginx-khallware.conf
sed -ni -e 1,14p -e 14i'\    server_names_hash_bucket_size 128;' -e 15,\$p /etc/nginx/nginx.conf
nginx -s reload

echo khallware: populate database
#-------------------------------------------------------------------------------
DBHOST=$(aws rds describe-db-instances --region $AWSREGION |jq '.DBInstances[] |select(.DBName == "website") | .Endpoint | .Address' |sed 's#"##g')
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_schema.sql
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_load.sql
mysql -u api -pkhallware -h $DBHOST website <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, _group) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
EOF

DBHOST=$(aws rds describe-db-instances --region $AWSREGION |jq '.DBInstances[] |select(.DBName == "devwebsite") | .Endpoint | .Address' |sed 's#"##g')
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_schema.sql
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_load.sql
mysql -u api -pkhallware -h $DBHOST website <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, _group) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
EOF

DBHOST=$(aws rds describe-db-instances --region $AWSREGION |jq '.DBInstances[] |select(.DBName == "qawebsite") | .Endpoint | .Address' |sed 's#"##g')
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_schema.sql
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_load.sql
mysql -u api -pkhallware -h $DBHOST website <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, _group) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
EOF
