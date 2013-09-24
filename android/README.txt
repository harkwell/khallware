export ANDROID_HOME=$HOME/Android/Sdk/
# or add <android.sdk.path>...</android.sdk.path> property to ~/.m2/settings.xml
cd android && mvn package && ls -ld target/Khallware.apk

######################
### ANDROID STUDIO ###
######################
# git checkout
mkdir android/libs
cp ~/.m2/repository/com/google/android/support-v4/r6/support-v4-r6.jar android/libs
cp ~/.m2/repository/org/slf4j/slf4j-android/1.6.1-RC1/slf4j-android-1.6.1-RC1.jar android/libs
bash ~/3rdParty/android-studio/bin/studio.sh

Import the project:
* File -> Import project
* Select android/pom.xml
