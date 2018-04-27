Webapp Utilities
=================

USAGE
---------------
### Validate the integrity of the data in the database
```shell
JAVA_OPTS="-Dlog4j.configuration=file:$PWD/log4j.properties"
JARFILE=/tmp/validate-n-sync.jar
APP_PROP_FILE=/tmp/main.properties

java $JAVA_OPTS -jar $JARFILE $APP_PROP_FILE
java $JAVA_OPTS -jar $JARFILE -f -tmytag -a $APP_PROP_FILE
```

### Add data
```
java $JAVA_OPTS -jar $JARFILE -f -tmytag -a $APP_PROP_FILE
```

QUICK START
---------------
### build it
```shell
git clone https://github.com/harkwell/khallware.git /tmp/khallware
export MAVEN_REPO=/tmp/delete-me-later
rm -rf $MAVEN_REPO && cd /tmp/khallware/tools
mvn -Dmaven.repo.local=$MAVEN_REPO package
ls -ld target/validate-n-sync-0.1-jar-with-dependencies.jar

vi $APP_PROP_FILE # customize for your environment like above

mv target/validate-n-sync-0.1-jar-with-dependencies.jar $JARFILE
mvn -Dmaven.repo.local=$MAVEN_REPO clean
rm -rf $MAVEN_REPO
```
