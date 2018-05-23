Android (front-end)
---------------
### Web Browser
* Google Chrome
```shell
http://tomcat-server:8080/apis/v1/static/mobile/Khallware.apk
```

### Build
* use maven
```shell
export ANDROID_HOME=/usr/local/android
export MAVEN_ANDROID_REPO=/tmp/khallware-android
git clone https://github.com/harkwell/khallware && cd khallware/android
sed -i -e 's#^sdk.dir=.*$#sdk.dir='$ANDROID_HOME'#g' local.properties
mvn -Dmaven.repo.local=$MAVEN_ANDROID_REPO -Dandroid.sdk.path=$ANDROID_HOME \
    package && ls -ld target/Khallware.apk
```

* One-Time : install android studio
```shell
# install android sdk and set ANDROID_HOME
chromium-browser https://developer.android.com/studio/
ZIPFILE=$(echo ~/Downloads/android-studio*linux.zip)
[ -r $ZIPFILE ] && unzip -d /usr/local/android/ $ZIPFILE
# rm $ZIPFILE
$ANDROID_HOME/android-studio/bin/studio.sh
# do not import -> next -> standard -> darkula -> next -> finish -> OK
# $ANDROID_HOME/tools/bin/sdkmanager --list
$ANDROID_HOME/tools/bin/sdkmanager \
   'build-tools;22.0.1' \
   'platforms;android-22' \
   'system-images;android-P;google_apis;x86' \
   'system-images;android-24;google_apis;x86_64' \
   'extras;android;m2repository' \
   'add-ons;addon-google_apis-google-22'

mvn -Dmaven.repo.local=$MAVEN_ANDROID_REPO install:install-file -Dfile=$ANDROID_HOME/add-ons/addon-google_apis-google-22/libs/maps.jar -DgroupId=google.apis -DartifactId=google.maps -Dversion=2.2 -Dpackaging=jar
```

* via the IDE
```shell
export ANDROID_HOME=/usr/local/android
export PATH=$PATH:$ANDROID_HOME/tools
android list targets
android create avd -n khallware --force -k "system-images;android-24;google_apis;x86_64" --abi google_apis/x86_64
mksdcard 256M ~/tmp/sdcard1.iso
emulator -list-avds
cd $ANDROID_HOME/tools && emulator -sdcard ~/tmp/sdcard1.iso -avd khallware
git clone https://github.com/harkwell/khallware
cd khallware
L='com/google/android/support-v4/r6/support-v4-r6.jar
   org/slf4j/slf4j-android/1.6.1-RC1/slf4j-android-1.6.1-RC1.jar
   google/apis/google.maps/2.2/google.maps-2.2.jar
   org/apache/httpcomponents/httpmime/4.4.1/httpmime-4.4.1.jar'
mkdir android/libs

for f in $L; do
    cp $MAVEN_ANDROID_REPO/$f android/libs
done

Import the project (from android studio):
select File, Import project
select android/pom.xml
```
