#!/bin/bash

echo khallware: init script
#-------------------------------------------------------------------------------
REPO=khallware
REPOTOP=/opt/khallware/gitrepo
AWSREGION=us-west-2
DBHOST=$(aws rds describe-db-instances --region $AWSREGION |grep Address |head -1 |cut -d\" -f4)
EC2HOST=$(curl http://169.254.169.254/latest/meta-data/public-hostname)

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
nginx -s reload

echo khallware: populate database
#-------------------------------------------------------------------------------
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_schema.sql
mysql -u api -pkhallware -h $DBHOST website <$REPOTOP/$REPO/src/scripts/db_load.sql
