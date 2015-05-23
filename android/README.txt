export ANDROID_HOME=$HOME/Android/Sdk/
# or add <android.sdk.path>...</android.sdk.path> property to ~/.m2/settings.xml
cd android && mvn package && ls -ld target/Khallware.apk

# one-time
mvn install:install-file -Dfile=$ANDROID_HOME/add-ons/addon-google_apis-google-22/libs/maps.jar -DgroupId=google.apis -DartifactId=google.maps -Dversion=2.2 -Dpackaging=jar


######################
### ANDROID STUDIO ###
######################
# git checkout
L='com/google/android/support-v4/r6/support-v4-r6.jar
   org/slf4j/slf4j-android/1.6.1-RC1/slf4j-android-1.6.1-RC1.jar
   org/apache/httpcomponents/httpclient/4.3.5/httpclient-4.3.5.jar
   org/apache/httpcomponents/httpmime/4.0-alpha3/httpmime-4.0-alpha3.jar
   org/apache/james/apache-mime4j/0.3/apache-mime4j-0.3.jar'
L='com/google/android/support-v4/r6/support-v4-r6.jar
   org/slf4j/slf4j-android/1.6.1-RC1/slf4j-android-1.6.1-RC1.jar
   google/apis/google.maps/2.2/google.maps-2.2.jar'
mkdir android/libs

for f in $L; do
    cp ~/.m2/repository/$f android/libs
done
bash ~/3rdParty/android-studio/bin/studio.sh

Import the project:
* File -> Import project
* Select android/pom.xml
