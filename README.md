Khallware (Mobile Computing Services)
=================
Overview
---------------
Serve the electronic content you might be tempted to put on facebook from
this android and web-based application that groups entities with tags (photos,
bookmarks, music, video, kml, etc.).  Your friends and relatives can register
and be placed into groups provisioning them with the content.  They may also
upload some of their own up to an individually customized limit and secure it
by group.  One may browse new content when posted via RSS.  Contact lists and
calendar entries may be managed from any android based phone.  Videos, photos
and playlists are also available.

Khallware utilizes: Android, Bootstrap, JQuery, Javascript, HTTP, Mime, Json,
Java8, REST/JAX-RS, Jackson, ORMLite, JDBC, c3p0 and Mysql.  Testing is
performed with fitnesse and jmeter.  Builds and deployments are made with
maven and tomcat or Docker.

Usage
---------------
### music streaming...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/sounds/playlist.m3u?tagId=35' -o /tmp/playlist.m3u 
mplayer -noconsolecontrols -user guest -passwd guest -shuffle -prefer-ipv4 -playlist /tmp/playlist.m3u
```

### geo-location...
```
- open google earth, add network link location: http://tomcat-server:8080/apis/v1/locations?tagId=35
```

### address book...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/contacts/cards.vcf?tagId=5' -o /tmp/cards.vcf
```

### calendering...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/events/calendar.ics?tagId=35' -o /tmp/calendar.ics
- import into rainlendar2
```

### upload content...
```shell
curl -i -X POST -H "Accept:application/json" -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" -F "filecomment=selfie" -F "image=@$HOME/tmp/photo.jpg" http://localhost:8080/apis/v1/upload?tagId=5
```

Web Application (back-end)
---------------
###Docker
```shell
docker run -i -t khall/khallware
```

###Build
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

* use maven
```shell
mvn package
```

### Deployment (manual)
* copy to tomcat8 webapps
* database from scratch
```shell
mysql -uroot -pmypasswd website < db_schema.sql
mysql -uroot -pmypasswd website < db_load.sql
```


Android (front-end)
---------------
###Google Play
* search for "Khallware"

###Build
* use maven
```shell
export ANDROID_HOME=$HOME/Android/Sdk/
cd android && mvn package && ls -ld target/Khallware.apk
```

* One-Time : install android studio
```shell
export ANDROID_HOME=$HOME/Android/Sdk/
mvn install:install-file -Dfile=$ANDROID_HOME/add-ons/addon-google_apis-google-22/libs/maps.jar -DgroupId=google.apis -DartifactId=google.maps -Dversion=2.2 -Dpackaging=jar
```

* via the IDE
```shell
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
select File, then Import project
select android/pom.xml
```
