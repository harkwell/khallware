#!/bin/bash
REPOTOP=$HOME/projects/
REPOURL=https://gitlab.com/harkwell/khallware.git
REPO=khallware
TOP=$HOME/tmp/build/$REPO

mkdir -p $REPOTOP && cd $REPOTOP || { echo build failed; exit 1; }
[ ! -e $REPO ] && git clone $REPOURL $REPO
cd $REPO && git pull
rm -rf $TOP && mkdir -p $TOP && cp -r $REPOTOP/$REPO/* $TOP
cd $TOP && mvn package 2>&1 >/tmp/build.out && ls -ld target/apis.war
