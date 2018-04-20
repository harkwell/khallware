#!/bin/bash

[ $1 ] || { echo "$0: <mysql script>"; exit 1; }

OUTFILE=$1.hsqldb.rc
cp $1 $OUTFILE


sed -i -e 's#^SET#-- SET#g' $OUTFILE
sed -i -e 's#^DROP#-- DROP#g' $OUTFILE
sed -i -e 's#^-- $Id.*$#SET DATABASE SQL SYNTAX MYS TRUE;#g' $OUTFILE
sed -i -e 's#OR REPLACE ##g' $OUTFILE
sed -i -e 's#^ALTER.*AUTO_INCR.*$##g' $OUTFILE

# POC_MAVEN_REPO=/tmp/foo
# rm -rf $POC_MAVEN_REPO
# mvn -Dmaven.repo.local=$POC_MAVEN_REPO \
#     org.apache.maven.plugins:maven-dependency-plugin:2.1:get \
#     -DrepoUrl=https://mvnrepository.com/ \
#     -Dartifact=org.hsqldb:hsqldb:2.4.0
# HSQLDB_JAR=$(find $POC_MAVEN_REPO -name \*hsqldb\*jar)
# mvn -Dmaven.repo.local=$POC_MAVEN_REPO \
#     org.apache.maven.plugins:maven-dependency-plugin:2.1:get \
#     -DrepoUrl=https://mvnrepository.com/ \
#     -Dartifact=org.hsqldb:sqltool:2.4.0
# SQLTOOL_JAR=$(find $POC_MAVEN_REPO -name \*sqltool\*jar)
# export CLASSPATH=$CLASSPATH:$HSQLDB_JAR:$SQLTOOL_JAR
# java org.hsqldb.cmdline.SqlTool --autoCommit --inlineRc=url=jdbc:hsqldb:file:db,user=webapp ~/projects/khallware/src/scripts/db_schema.sql.hsqldb.rc
