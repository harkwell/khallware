Khallware (Mobile Computing Services)
=================
Overview
---------------
Create and maintain photo albums, play music playlists, enter GPS coordinates,
add website URL collections, note calendar events, make contact lists, write
blog entries and add to video libraries in one location organized and grouped
with tags.  Later, perform searches across all these items or browse that
favorite tag from a list.  Also, watch the RSS feed for new content posted by
others.

Serve the electronic content you might be tempted to put on facebook.  This
android and web-based application groups entities with tags (photos, bookmarks,
music, video, kml, etc.).  Your friends and relatives can register a user to be
placed into a group where content is collated.  They may also upload some of
their own files (up to a customized, set limit) and group secure it.  One may
browse new content from the RSS feed and contact lists and calendar entries may
be managed from any android based phone.  Videos, photos and playlists are also
available.

Khallware utilizes: Android, Bootstrap, JQuery, Javascript, HTTP, Mime, Json,
Java8, REST/JAX-RS, Jackson, ORMLite, JDBC, c3p0 and Mysql.  Testing is
performed with fitnesse and jmeter.  Builds and deployments are made with
maven, tomcat, Docker or Amazon Web Services.

USAGE
---------------
### Streaming music...
```shell
echo guest:guest |base64
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/sounds/playlist.m3u?tagId=35' -o /tmp/playlist.m3u 
mplayer -noconsolecontrols -user guest -passwd guest -shuffle -prefer-ipv4 -playlist /tmp/playlist.m3u
```

### GPS coordinates (maps, geo-location)...
```
- open google earth, add network link location: http://tomcat-server:8080/apis/v1/locations?tagId=35
```

### Address book...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/contacts/cards.vcf?tagId=5' -o /tmp/cards.vcf
file import, /tmp/cards.vcf
```

### Calendaring...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/events/calendar.ics?tagId=35' -o /tmp/calendar.ics
- import into rainlendar2
```

### Upload content...
```shell
feh ~/tmp/photo.jpg
curl -i -X POST -H "Accept:application/json" -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" -F "filecomment=selfie" -F "image=@$HOME/tmp/photo.jpg" http://localhost:8080/apis/v1/upload?tagId=5
```

QUICK START
---------------
### Run it locally
```shell
# download the software
DESTDIR=$HOME/tmp/apis/
mkdir -p $DESTDIR
wget -c 'http://central.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.4.9.v20180320/jetty-runner-9.4.9.v20180320.jar' -O $DESTDIR/jetty-runner.jar
wget -c 'https://github.com/harkwell/khallware/releases/download/v0.9.0/khallware-0.9.0.war' -O $DESTDIR/apis.war
wget -c 'https://github.com/harkwell/khallware/releases/download/v0.9.0/apis-0.9.0.db' -O $DESTDIR/apis.db

# download optional software
wget -c 'https://github.com/harkwell/khallware/releases/download/v0.9.0/validate-n-sync.jar' -O $DESTDIR/validate-n-sync.jar
wget -c 'http://nilhcem.github.com/FakeSMTP/downloads/fakeSMTP-latest.zip' -qO - |bsdtar -xvf - -C /tmp

# configure it to your liking:
cat <<EOF >/tmp/main.properties
images=$DESTDIR/images
thumbs=$DESTDIR/thumbs
audio=$DESTDIR/audio
upload.dir=$DESTDIR/uploads
captcha_file=$DESTDIR/captcha.png
mail.debug=true
mail.smtp.host=localhost
mail.transport.protocol=smtp
mail.smtp.port=8025
mail.smtp.starttls.enable=false
mail.smtp.starttls.required=false
jdbc_user=webapp
jdbc_pass=webapp
jdbc_url=jdbc:sqlite:$DESTDIR/apis.db
#jdbc_url=jdbc:mysql://127.0.0.1/website?autoReconnect=true
registration_url=http://localhost:8080/apis/v1/security/register/
jdbc_driver=org.sqlite.JDBC
EOF

# start up the fake email server (optional)
java -jar /tmp/fakeSMTP*.jar

# load data into the database (optional)
java -jar $DESTDIR/validate-n-sync.jar -a /tmp/main.properties

# start up khallware
java -jar $DESTDIR/jetty-runner.jar --path /apis $DESTDIR/apis.war

# begin to use it...
chromium-browser http://localhost:8080/apis/  # use guest/guest to login
```

### Or, build and run it locally
```shell
git clone https://github.com/harkwell/khallware.git /tmp/khallware
export MAVEN_REPO=/tmp/delete-me-later
rm -rf $MAVEN_REPO && cd /tmp/khallware
mvn -Dmaven.repo.local=$MAVEN_REPO package

mvn org.apache.maven.plugins:maven-dependency-plugin:2.1:get \
    -Dmaven.repo.local=$MAVEN_REPO \
    -DrepoUrl=https://mvnrepository.com/ \
    -Dartifact=org.eclipse.jetty:jetty-runner:9.4.9.v20180320
RUNNER_JAR=$(find $MAVEN_REPO -name \*runner\*jar)
java -jar $RUNNER_JAR --path /apis target/apis.war
bash src/scripts/convert-to-sqlite.sh src/scripts/db_schema.sql fixme
bash src/scripts/convert-to-sqlite.sh src/scripts/db_load.sql
sqlite3 $DESTDIR/apis.db <src/scripts/db_schema.sqlite
sqlite3 $DESTDIR/apis.db <src/scripts/db_load.sqlite
sqlite3 $DESTDIR/apis.db # prime with guest user and groups
vi /tmp/main.properties # customize for your environment like above
chromium-browser http://localhost:8080/apis/

mvn -Dmaven.repo.local=$MAVEN_REPO clean
rm -rf $MAVEN_REPO
```

### Or, run from Amazon Web Services (AWS)
```shell
chromium-browser http://aws.amazon.com/  # create a "free-tier" account
aws configure # use the AccessKey and Secret Access Key from above
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/aws/AWS-cloudformation.json' -O - |aws cloudformation create-stack --stack-name khallware --capabilities CAPABILITY_NAMED_IAM --tags 'Key=projects,Value=khallware' --template-body file:///dev/stdin
chromium-browser http://<dns-name-of-aws-ec2-host>/
```

### Or, run with comprehensive hosted docker (http://hub.docker.com/)
```shell
docker run -it khall/khallware
```

### Or, run with individual docker containers

### Create A MySQL Docker Image (One Time Only)
```shell
mkdir -p /tmp/khallware-mysql && cd /tmp/khallware-mysql
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/Docker-mysql' -O - |docker build --no-cache -t khallware-mysql:v1.0 -
```

### Create A Tomcat8 Docker Image (One Time Only)
```shell
mkdir -p /tmp/khallware-tomcat8 && cd /tmp/khallware-tomcat8
# chromium-browser https://tomcat.apache.org/download-80.cgi
wget -q -c 'http://mirrors.gigenet.com/apache/tomcat/tomcat-8/v8.0.36/bin/apache-tomcat-8.0.36.tar.gz' -O apache-tomcat.tgz
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/Docker-tomcat8' -O - |docker build --no-cache -t khallware-tomcat:v1.0 -
```

### Create A khallware.com Build Docker Image (One Time Only)
```shell
mkdir -p /tmp/khallware-build && cd /tmp/khallware-build

for x in build.sh Dockerfile; do
   wget -q -c "https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/$x"
done
sed -i -e 's#^rm -rf.*mkdir#mkdir#' build.sh
docker build --no-cache -t khallware-build:v1.0 .
```

### Build the khallware artifact utilizing the docker build image
```shell
mkdir -p /tmp/artifacts
docker run -h build --name khallware-build -v /tmp/artifacts:/root/tmp/build/khallware/target khallware-build:v1.0
ls -ld /tmp/artifacts/apis.war
```

### Create MySQL Database Files (One Time Only)
```shell
mkdir -p $HOME/tmp/khallware-mysql
docker run -it -h mysql --name khallware-mysql -v $HOME/tmp/khallware-mysql:/var/lib/mysql khallware-mysql:v1.0 bash
mysql_install_db --user=mysql --ldata=/var/lib/mysql/
/usr/bin/mysqld_safe &
mysql -uroot mysql
CREATE DATABASE website;
CREATE USER 'api'@'%' IDENTIFIED BY 'khallware';
GRANT ALL PRIVILEGES ON website.* TO 'api'@'%' WITH GRANT OPTION;
USE mysql;
SET PASSWORD FOR 'api'@'%' = PASSWORD('khallware');
exit
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/db_schema.sql' -O - |mysql -uroot website
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/db_load.sql' -O - |mysql -uroot website
exit
docker rm $(docker ps -a |grep mysql |cut -d\  -f1)
```

### Deploy a MySQL Server as Docker Container
```shell
docker run -d -h mysql --name khallware-mysql -p 3306:3306 -v $HOME/tmp/khallware-mysql:/var/lib/mysql khallware-mysql:v1.0
echo 'SHOW TABLES;' |mysql -uapi -pkhallware -h 127.0.0.1 website
```

### Deploy Application Server
```shell
mkdir -p /tmp/khallware/media/{thumbs,photo,uploads}
mkdir -p /tmp/khallware/share/ogg
mkdir -p /tmp/khallware/webapps && cp /tmp/artifacts/apis.war /tmp/khallware/webapps
docker run -d -h khallware --name khallware -p 8080:8080 -v /tmp/khallware/share:/usr/local/share -v /tmp/khallware/webapps:/var/lib/tomcat8/webapps --link khallware-mysql khallware-tomcat:v1.0
```

### Prime Website with "guest" User
```shell
echo -n "guest" |sha256sum # "84983c60.."
mysql -uapi -pkhallware mysql <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (group_, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, group_) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
INSERT INTO landing (url, group_) VALUES ("/apis/v1/static/family.html",(SELECT id FROM groups WHERE name = 'family'));
INSERT INTO landing (url, group_) VALUES ("/apis/v1/static/friends.html",(SELECT id FROM groups WHERE name = 'friends'));
INSERT INTO quota (user, available, used) SELECT id AS user, 102400000, 0 FROM credentials;
EOF
```

### Connect Web Browser
```shell
chromium-browser http://localhost:8080/apis/   # use credentials guest/guest
```

### Connect Phone
```shell
Play Store -> Search "khallware" -> download -> start, provision server
```


Web Application (back-end)
---------------
### Deployment (Docker)
```shell
docker run -it khall/khallware
```

### Deployment (manual)
* copy apis.war to the tomcat8 webapps directory, restart
* database (from scratch)
```shell
# mysql
mysql -uroot -pmypasswd website < db_schema.sql
mysql -uroot -pmypasswd website < db_load.sql

# sqlite3
bash convert-to-sqlite.sh db_schema.sql fixme
bash convert-to-sqlite.sh db_load.sql
sqlite3 apis.db <db_schema.sqlite
sqlite3 apis.db <db_load.sqlite
```

### Build
* via maven
```shell
svn export https://github.com/harkwell/khallware/trunk khallware
cd khallware
mvn package
```

* One-Time : replace Google Maps API key with your own
```shell
chromium-browser https://code.google.com/apis/console
vi android/AndroidManifest.xml android/res/layout/map.xml
```

Android (front-end)
---------------
### Google Play
* search for "Khallware"

### Web Browser
* Google Chrome
```shell
chromium-browser http://tomcat-server:8080/apis/
```

### Build
* use maven
```shell
export ANDROID_HOME=/usr/local/adt-bundle-linux-x86_64-20140702/sdk/
svn export https://github.com/harkwell/khallware/trunk/android && cd android
grep $ANDROID_HOME local.properties || echo edit local.properties
grep $ANDROID_HOME ~/.m2/settings.xml || echo add maven android.sdk.path
mvn package && ls -ld target/Khallware.apk
```

* One-Time : install android studio
```shell
# install android sdk and set ANDROID_HOME
export ANDROID_HOME=/usr/local/adt-bundle-linux-x86_64-20140702/sdk/
$ANDROID_HOME/tools/android # install v22 items
# Android SDK build-tools v22.0.1
# Android 5.1.1 - SDK Platform v2
# Android 5.1.1 - Google APIs v1
# Android 5.1.1 - Google APIs Intel x86 Atom_64 System Image
# Android 5.1.1 - Intel x86 Atom_64 System Image
# Extras - Android Support Library
mvn install:install-file -Dfile=$ANDROID_HOME/add-ons/addon-google_apis-google-22/libs/maps.jar -DgroupId=google.apis -DartifactId=google.maps -Dversion=2.2 -Dpackaging=jar
```

* via the IDE
```shell
export ANDROID_HOME=/usr/local/adt-bundle-linux-x86_64-20140702/sdk/
export PATH=$PATH:$ANDROID_HOME/tools
android list targets
android create avd -n khallware --force -t "Google Inc.:Google APIs:22" --abi google_apis/x86_64
mksdcard 256M ~/tmp/sdcard1.iso
emulator -sdcard ~/tmp/sdcard1.iso -avd khallware
svn export https://github.com/harkwell/khallware/trunk/android 
L='com/google/android/support-v4/r6/support-v4-r6.jar
   org/slf4j/slf4j-android/1.6.1-RC1/slf4j-android-1.6.1-RC1.jar
   google/apis/google.maps/2.2/google.maps-2.2.jar
   org/apache/httpcomponents/httpmime/4.4.1/httpmime-4.4.1.jar'
mkdir android/libs

for f in $L; do
    cp ~/.m2/repository/$f android/libs
done
bash ~/3rdParty/android-studio/bin/studio.sh

Import the project:
select File, Import project
select android/pom.xml
```
