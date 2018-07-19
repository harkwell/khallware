#!/bin/bash

echo "khallware: init script"
#-------------------------------------------------------------------------------
AWSREGION=$(curl http://169.254.169.254/latest/meta-data/placement/availability-zone |sed 's#.$##')
EC2HOST=$(curl http://169.254.169.254/latest/meta-data/public-hostname)
RAWROOT=https://gitlab.com/harkwell/khallware/raw/master/aws/


echo "khallware: setup system"
#-------------------------------------------------------------------------------
curl -sS $RAWROOT/khall-prefs.sh >/etc/profile.d/khall-prefs.sh
echo set editing-mode vi >>/etc/inputrc
yum install -y nginx git mysql jq
#/usr/sbin/nginx
systemctl start nginx


echo "khallware: clone khallware git project"
#-------------------------------------------------------------------------------
mkdir -p /opt/khallware/gitrepo && cd /opt/khallware/gitrepo
git clone https://gitlab.com/harkwell/khallware.git && cd khallware
REPO=/opt/khallware/gitrepo/khallware


echo "khallware: configure nginx and php"
#-------------------------------------------------------------------------------
cp $REPO/aws/index.html /usr/share/nginx/html/
cp $REPO/aws/nginx-khallware.conf /etc/nginx/conf.d/
sed -i -e "s#XXXX#$EC2HOST#g" /etc/nginx/conf.d/nginx-khallware.conf
sed -ni -e 1,14p -e 14i'\    server_names_hash_bucket_size 128;' -e 15,\$p /etc/nginx/nginx.conf
service nginx start


echo "khallware: populate databases"
#-------------------------------------------------------------------------------
DBHOST=$(aws rds describe-db-instances --region $AWSREGION |jq '.DBInstances[] |select(.DBName == "website") | .Endpoint | .Address' |sed 's#"##g')
mysql -u api -pkhallware -h $DBHOST website <$REPO/src/scripts/db_schema.sql
mysql -u api -pkhallware -h $DBHOST website <$REPO/src/scripts/db_load.sql
mysql -u api -pkhallware -h $DBHOST website <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, group_) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
EOF

DBHOST=$(aws rds describe-db-instances --region $AWSREGION |jq '.DBInstances[] |select(.DBName == "devwebsite") | .Endpoint | .Address' |sed 's#"##g')
mysql -u api -pkhallware -h $DBHOST devwebsite <$REPO/src/scripts/db_schema.sql
mysql -u api -pkhallware -h $DBHOST devwebsite <$REPO/src/scripts/db_load.sql
mysql -u api -pkhallware -h $DBHOST devwebsite <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, group_) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
EOF

DBHOST=$(aws rds describe-db-instances --region $AWSREGION |jq '.DBInstances[] |select(.DBName == "qawebsite") | .Endpoint | .Address' |sed 's#"##g')
mysql -u api -pkhallware -h $DBHOST qawebsite <$REPO/src/scripts/db_schema.sql
mysql -u api -pkhallware -h $DBHOST qawebsite <$REPO/src/scripts/db_load.sql
mysql -u api -pkhallware -h $DBHOST qawebsite <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, group_) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
EOF


echo "khallware: setup flyspray"
#-------------------------------------------------------------------------------
yum install -y php php-mysql php-fpm
sed -in -e 's#^user =.*$#user = nginx#' -e 's#^group =.*$#group = nginx#' /etc/php-fpm.d/www.conf
sed -in -e 's#^;date.timezone .*$#date.timezone = America/Chicago#' /etc/php.ini
service php-fpm restart
nginx -s reload
wget -c 'https://github.com/Flyspray/flyspray/archive/v1.0-rc1.tar.gz' -O /tmp/flyspray.tgz
mkdir -p /usr/share/nginx/html/flyspray
tar zxvf /tmp/flyspray.tgz -C /usr/share/nginx/html/flyspray --strip-components=1
cd /usr/local/bin && curl -sS https://getcomposer.org/installer | php
cd /usr/share/nginx/html/flyspray
export COMPOSER_HOME=/root
php /usr/local/bin/composer.phar install
CONF=/usr/share/nginx/html/flyspray/flyspray.conf.php
DBHOST=$(aws rds describe-db-instances --region $AWSREGION |jq '.DBInstances[] |select(.DBName == "flyspray") | .Endpoint | .Address' |sed 's#"##g')
mysql -u flyspray -pkhallware -h $DBHOST flyspray <$REPO/aws/flyspray/flyspray.mysql
sed -in -e 's#^dbtype =.*$#dbtype = "mysql"#' $CONF
sed -in -e 's#^dbhost =.*$#dbhost = "'$DBHOST'"#' $CONF
sed -in -e 's#^dbuser =.*$#dbuser = "flyspray"#' $CONF
sed -in -e 's#^dbname =.*$#dbname = "flyspray"#' $CONF
sed -in -e 's#^dbpass =.*$#dbpass = "khallware"#' $CONF
rm -f /tmp/flyspray.tgz
chown -R nginx:nginx /usr/share/nginx/html
