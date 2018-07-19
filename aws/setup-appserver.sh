#!/bin/bash

echo "khallware: init script"
#-------------------------------------------------------------------------------
export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk.x86_64/
RAWROOT=https://gitlab.com/harkwell/khallware/raw/master/aws/


echo "khallware: setup system"
#-------------------------------------------------------------------------------
yum install -y wget mysql jq java-1.8.0-openjdk-devel git
update-alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java
curl -sS $RAWROOT/khall-prefs.sh >/etc/profile.d/khall-prefs.sh
echo set editing-mode vi >>/etc/inputrc


echo "khallware: setup tomcat8"
#-------------------------------------------------------------------------------
wget --quiet -c 'http://mirrors.ocf.berkeley.edu/apache/tomcat/tomcat-8/v8.5.4/bin/apache-tomcat-8.5.4.tar.gz' -O /tmp/tomcat8.tgz
mkdir -p /var/lib/tomcat8
tar zxvf /tmp/tomcat8.tgz -C /var/lib/tomcat8 --strip-components=1
rm tmp/tomcat8.tgz
/var/lib/tomcat8/bin/startup.sh


echo "khallware: clone khallware git project"
#-------------------------------------------------------------------------------
mkdir -p /opt/khallware/gitrepo && cd /opt/khallware/gitrepo
git clone https://gitlab.com/harkwell/khallware.git && cd khallware
git checkout dev
REPO=/opt/khallware/gitrepo/khallware


echo "khallware: setup jenkins"
#-------------------------------------------------------------------------------
wget --quiet -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat/jenkins.repo
rpm --import https://jenkins-ci.org/redhat/jenkins-ci.org.key
yum install -y jenkins
sed -i -e 's#^JENKINS_PORT="8080"#JENKINS_PORT="8081"#' /etc/sysconfig/jenkins
\cp -fr $REPO/aws/jenkins/* /var/lib/jenkins/
chown -R jenkins:jenkins /var/lib/jenkins/jobs/ /var/lib/jenkins/users/
service jenkins start
sleep 20  # give it time to start-up and provision resources
wget -c 'http://localhost:8081/jnlpJars/jenkins-cli.jar' -O /tmp/jenkins-cli.jar
java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ install-plugin --username jenkins --password khallware -restart git
#java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ restart --username jenkins --password khallware


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
chown -R jenkins:jenkins /usr/share/maven-repo/ /var/lib/tomcat8/webapps/


echo "khallware: build webapp artifacts (apis.war, apis-dev.war, apis-qa.war)"
#-------------------------------------------------------------------------------
java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ build --username jenkins --password khallware khallware
java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ build --username jenkins --password khallware khallware-DEV
java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ build --username jenkins --password khallware khallware-QA


echo "khallware: setup fitnesse"
#-------------------------------------------------------------------------------
mkdir ~/tmp/ && cd ~/tmp/
wget -c 'http://fitnesse.org/fitnesse-standalone.jar?responder=releaseDownload&release=20131110' -O fitnesse.jar
nohup java -jar fitnesse.jar -p 8000 -d /root/tmp/fitnesse/ -l /root/tmp/fitnesse/ &
wget -c 'https://github.com/smartrics/RestFixture/archive/3.0.tar.gz' -O 3.0.tgz
tar zxvf 3.0.tgz && cd RestFixture-3.0
mvn clean package
cp -r $REPO/fitnesse/* ~/tmp/fitnesse/FitNesseRoot
cat <<'EOF' >/root/tmp/fitnesse/FitNesseRoot/content.txt
!define TEST_SYSTEM {slim}
!path /root/tmp/RestFixture-3.0/target/dependencies/*.jar
!path /root/tmp/RestFixture-3.0/target/smartrics-RestFixture-3.0.jar
EOF
touch /tmp/photo1.jpg /tmp/video1.mp4


echo "khallware: setup jmeter"
#-------------------------------------------------------------------------------
wget -c 'http://download.nextag.com/apache/jmeter/binaries/apache-jmeter-3.0.tgz' -O /tmp/jmeter.tgz
mkdir -p /usr/local/jmeter && tar zxvf /tmp/jmeter.tgz -C /usr/local/jmeter --strip-components=1
cat <<'EOF' >/etc/profile.d/jmeter.sh
export PATH=$PATH:/usr/local/jmeter/bin
export JVM_ARGS='-Xms290m -Xmx1G'
EOF
rm /tmp/jmeter.tgz
source /etc/profile.d/jmeter.sh
# /usr/local/jmeter/bin/jmeter -n -t /opt/khallware/gitrepo/khallware/src/scripts/apis.jmx
