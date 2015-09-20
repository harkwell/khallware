#!/bin/bash
REPOTOP=$HOME/projects/
REPOURL=https://github.com/harkwell/khallware.git
REPO=khallware
TOP=/home/khall/tmp/blah/$REPO

mkdir -p $REPOTOP && cd $REPOTOP
[ ! -e $REPO ] && git clone $REPOURL $REPO
cd $REPO && git pull
rm -rf $TOP && mkdir -p $TOP && cp -r $REPOTOP/$REPO/* $TOP
cd $TOP && mvn package 2>&1 >/tmp/build.out && ls -ld target/apis.war
