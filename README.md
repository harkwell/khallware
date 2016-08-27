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

### Calendering...
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
### Utilize Amazon Web Services (AWS) (Optional/Complete/Easy)
```shell
chromium-browser http://aws.amazon.com/  # create a "free-tier" account
aws configure # use the AccessKey and Secret Access Key from above
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/aws/AWS-cloudformation.json' -O - |aws cloudformation create-stack --stack-name khallware --capabilities CAPABILITY_NAMED_IAM --tags 'Key=projects,Value=khallware' --template-body file:///dev/stdin
chromium-browser http://<ipaddr-of-aws-host>/index.html
```

### Run from http://hub.docker.com/  (Optional/No CI-CD/Easy)
```shell
docker run -it khall/khallware
```

### Create MySQL Docker Image (One Time Only)
```shell
mkdir -p /tmp/khallware-mysql && cd /tmp/khallware-mysql
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/Docker-mysql' -O - |docker build --no-cache -t docker-repo:5000/khallware-mysql:v1.0 -
```

### Create Tomcat8 Docker Image (One Time Only)
```shell
mkdir -p /tmp/khallware-tomcat8 && cd /tmp/khallware-tomcat8
# chromium-browser https://tomcat.apache.org/download-80.cgi
wget -q -c 'http://mirrors.gigenet.com/apache/tomcat/tomcat-8/v8.0.36/bin/apache-tomcat-8.0.36.tar.gz' -O apache-tomcat.tgz
wget -q -c 'https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/Docker-tomcat8' -O - |docker build --no-cache -t docker-repo:5000/khallware-tomcat:v1.0 -
```

### Create khallware.com Build Docker Image (One Time Only)
```shell
mkdir -p /tmp/khallware-build && cd /tmp/khallware-build

for x in build.sh Dockerfile; do
   wget -q -c "https://raw.githubusercontent.com/harkwell/khallware/github/src/scripts/$x"
done
sed -i -e 's#^rm -rf.*mkdir#mkdir#' build.sh
docker build --no-cache -t docker-repo:5000/khallware-build:v1.0 .
```

### Build Application Server (apis.war file)
```shell
mkdir -p /tmp/artifacts
docker run -h build --name khallware-build -v /tmp/artifacts:/root/tmp/build/khallware/target docker-repo:5000/khallware-build:v1.0
ls -ld /tmp/artifacts/apis.war
```

### Create MySQL Database Files (One Time Only)
```shell
mkdir -p $HOME/tmp/khallware-mysql
docker run -it -h mysql --name khallware-mysql -v $HOME/tmp/khallware-mysql:/var/lib/mysql docker-repo:5000/khallware-mysql:v1.0 bash
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

### Deploy MySQL Server as Docker Container
```shell
docker run -d -h mysql --name khallware-mysql -p 3306:3306 -v $HOME/tmp/khallware-mysql:/var/lib/mysql docker-repo:5000/khallware-mysql:v1.0
echo 'SHOW TABLES;' |mysql -uapi -pkhallware -h 127.0.0.1 website
```

### Deploy Application Server
```shell
mkdir -p /tmp/khallware/media/{thumbs,photo,uploads}
mkdir -p /tmp/khallware/share/ogg
mkdir -p /tmp/khallware/webapps && cp /tmp/artifacts/apis.war /tmp/khallware/webapps
docker run -d -h khallware --name khallware -p 8080:8080 -v /tmp/khallware/share:/usr/local/share -v /tmp/khallware/webapps:/var/lib/tomcat8/webapps --link khallware-mysql docker-repo:5000/khallware-tomcat:v1.0
```

### Prime Website with "guest" User
```shell
echo -n "guest" |sha256sum # "84983c60.."
mysql -uapi -pkhallware mysql <<EOF
INSERT INTO groups (name, description) VALUES ('root', 'root group');
UPDATE groups SET id=0 WHERE name = 'root';
INSERT INTO groups (name, description) VALUES ('guest', 'guest group');
INSERT INTO groups (name, description) VALUES ('family', 'family group');
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'root'));
INSERT INTO edges (_group, parent) VALUES ((SELECT id FROM groups WHERE name = 
'guest'), (SELECT id FROM groups WHERE name = 'family'));
INSERT INTO credentials (username, password, email, _group) VALUES ('guest', '84983c60f7daadc1cb8698621f802c0d9f9a3c3c295c810748fb048115c186ec','guest@mybox.com',(SELECT id FROM groups WHERE name = 'guest'));
INSERT INTO landing (url, _group) VALUES ("/apis/v1/static/family.html",(SELECT id FROM groups WHERE name = 'family'));
INSERT INTO landing (url, _group) VALUES ("/apis/v1/static/friends.html",(SELECT id FROM groups WHERE name = 'friends'));
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
mysql -uroot -pmypasswd website < db_schema.sql
mysql -uroot -pmypasswd website < db_load.sql
```

### Build
* via maven
```shell
svn export https://github.com/harkwell/khallware/trunk khallware
cd khallware
mvn package
```

* One-Time : jvorbiscomment (https://code.google.com/p/jvorbiscomment/)
```shell
cd /tmp/ && wget -c 'https://jvorbiscomment.googlecode.com/files/jvorbiscomment-1.0.3.zip' -O jvorbiscomment-1.0.3.zip
unzip -j ~/tmp/jvorbiscomment-1.0.3.zip jvorbiscomment-1.0.3/jvorbiscomment-1.0.3.jar
mvn install:install-file -Dfile=/tmp/jvorbiscomment-1.0.3.jar -DgroupId=adamb.vorbis -DartifactId=jvorbis -Dversion=1.0.3 -Dpackaging=jar
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
