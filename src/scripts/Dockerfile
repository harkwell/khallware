#-------------------------------------------------------------------------------
# khallware.com build host based on centos7 linux
#-------------------------------------------------------------------------------
FROM centos:7
MAINTAINER Kevin@khallware.com
ADD build.sh /usr/local/bin/build.sh
RUN yum install -y epel-release \
   && yum install -y java-1.8.0-openjdk-devel wget git maven unzip \
   && chmod 777 /usr/local/bin/build.sh \
   && wget -c 'https://jvorbiscomment.googlecode.com/files/jvorbiscomment-1.0.3.zip' -O /tmp/jvorbiscomment-1.0.3.zip \
   && unzip -d /tmp -j /tmp/jvorbiscomment-1.0.3.zip jvorbiscomment-1.0.3/jvorbiscomment-1.0.3.jar \
   && mvn install:install-file -Dfile=/tmp/jvorbiscomment-1.0.3.jar -DgroupId=adamb.vorbis -DartifactId=jvorbis -Dversion=1.0.3 -Dpackaging=jar \
   && echo export JAVA_HOME=/usr/lib/jvm/java-1.8.0 >/etc/profile.d/maven.sh
VOLUME /root/tmp/build/khallware/target
CMD /usr/local/bin/build.sh
