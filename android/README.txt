export ANDROID_HOME=/usr/local/adt-bundle-linux-x86_64-20140702/sdk/
svn export https://github.com/harkwell/khallware/android && cd android
grep $ANDROID_HOME local.properties || echo edit local.properties
grep $ANDROID_HOME ~/.m2/settings.xml || echo add maven android.sdk.path
# or add <android.sdk.path>...</android.sdk.path> property to ~/.m2/settings.xml
mvn package && ls -ld target/Khallware.apk

# one-time
# install android sdk and set ANDROID_HOME
$ANDROID_HOME/tools/android # install v22 items
# Android SDK build-tools v22.0.1
# Android 5.1.1 - SDK Platform v2
# Android 5.1.1 - Google APIs v1
# Android 5.1.1 - Intel x86 Atom_64 System Image
# Extras - Android Support Library
mvn install:install-file -Dfile=$ANDROID_HOME/add-ons/addon-google_apis-google-22/libs/maps.jar -DgroupId=google.apis -DartifactId=google.maps -Dversion=2.2 -Dpackaging=jar

######################
### ANDROID STUDIO ###
######################
export ANDROID_HOME=/usr/local/adt-bundle-linux-x86_64-20140702/sdk/
export PATH=$PATH:$ANDROID_HOME/tools
android list targets
android create avd -n khallware --force -t "Google Inc.:Google APIs:22" --abi google_apis/x86_64
mksdcard 256M ~/tmp/sdcard1.iso
emulator -sdcard ~/tmp/sdcard1.iso -avd fovea
svn export https://github.com/harkwell/khallware/android
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
* File -> Import project
* Select android/pom.xml

# MAY BE NEEDED cd android1 && sed -ni -e '1,18p' -e '18a packagingOptions { exclude "META-INF/DEPENDENCIES"\nexclude "META-INF/NOTICE"\nexclude "META-INF/LICENSE" }' -e '19,28p' app/build.gradle
