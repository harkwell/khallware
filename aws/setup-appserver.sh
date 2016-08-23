#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/

#echo "khallware: setup gitlab"
#-------------------------------------------------------------------------------
#curl -sS https://packages.gitlab.com/install/repositories/gitlab/gitlab-ce/script.rpm.sh |sudo bash
#yum install -y gitlab-ce
#gitlab-ctl reconfigure

echo "khallware: setup jenkins"
#-------------------------------------------------------------------------------
wget --quiet -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat/jenkins.repo
rpm --import https://jenkins-ci.org/redhat/jenkins-ci.org.key
yum install -y jenkins
sed -i -e 's#^JENKINS_PORT="8080"#JENKINS_PORT="8081"#' /etc/sysconfig/jenkins
# untar jenkins config...
service jenkins start

echo "khallware: setup maven3"
#-------------------------------------------------------------------------------
wget --quiet -O /tmp/maven.tgz http://mirrors.ocf.berkeley.edu/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
mkdir /usr/local/maven && tar zxvf /tmp/maven.tgz -C /usr/local/maven --strip-components=1
cat <<'EOF' >/etc/profile.d/maven.sh
export PATH=$PATH:/usr/local/maven/bin
EOF
rm /tmp/maven.tgz
source /etc/profile.d/maven.sh

echo "khallware: setup local maven repo"
#-------------------------------------------------------------------------------
mkdir -p /usr/share/maven-repo && cd /usr/share/maven-repo
wget --quiet -c 'https://jvorbiscomment.googlecode.com/files/jvorbiscomment-1.0.3.zip' -O /tmp/jvorbiscomment-1.0.3.zip
unzip -j /tmp/jvorbiscomment-1.0.3.zip -d /tmp jvorbiscomment-1.0.3/jvorbiscomment-1.0.3.jar
mvn install:install-file -Dmaven.repo.local=/usr/share/maven-repo -Dfile=/tmp/jvorbiscomment-1.0.3.jar -DgroupId=adamb.vorbis -DartifactId=jvorbis -Dversion=1.0.3 -Dpackaging=jar
chown -R jenkins:jenkins /usr/share/maven-repo/ /var/lib/tomcat8/webapps/
rm /tmp/jvorbiscomment*
