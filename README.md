Khallware (Mobile Computing Services)
=================
A web-based application that groups entities (photos, bookmarks, music, video,
kml, etc.) with tags.  It utilizes Android, Bootstrap, JQuery, Javascript,
HTTP, Mime, Json, Java8, REST/JAX-RS, Jackson, ORMLite, JDBC and Mysql.

Usage
---------------
* music streaming...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/sounds/playlist.m3u?tagId=35' -o /tmp/playlist.m3u 
mplayer -noconsolecontrols -user guest -passwd guest -shuffle -prefer-ipv4 -playlist /tmp/playlist.m3u
```

* geo-location...
- open google earth, add network link location: http://tomcat-server:8080/apis/v1/locations?tagId=35

* address book...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/contacts/cards.vcf?tagId=5' -o /tmp/cards.vcf
```

* calendering...
```shell
curl -s -X GET -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" 'http://tomcat-server:8080/apis/v1/events/calendar.ics?tagId=35' -o /tmp/calendar.ics
```
- import into rainlendar2

* upload content...
```shell
curl -i -X POST -H "Accept:application/json" -H "Authorization:Basic Z3Vlc3Q6Z3Vlc3QK=" -F "filecomment=my photo" -F "image=@$HOME/tmp/x.jpg" http://localhost:8080/apis/v1/upload?tagId=5
```

Docker
---------------
```shell
docker run -i -t khall/khallware
```

Build
---------------
* One-Time
- jvorbiscomment (https://code.google.com/p/jvorbiscomment/)
```shell
cd /tmp/ && wget -c 'https://jvorbiscomment.googlecode.com/files/jvorbiscomment-1.0.3.zip' -O jvorbiscomment-1.0.3.zip
unzip -j ~/tmp/jvorbiscomment-1.0.3.zip jvorbiscomment-1.0.3/jvorbiscomment-1.0.3.jar
mvn install:install-file -Dfile=/tmp/jvorbiscomment-1.0.3.jar -DgroupId=adamb.vorbis -DartifactId=jvorbis -Dversion=1.0.3 -Dpackaging=jar
```

* use maven
mvn package



Deploy
---------------
* copy to tomcat8 webapps
* from scratch
```shell
mysql -uroot -pmypasswd website < db_schema.sql
mysql -uroot -pmypasswd website < db_load.sql
```
