#!/bin/bash
#set -v # do not expand variables
set -x # output
set -e # stop on error
set -u # stop if you use an uninitialized variable

TODAY=`date +%Y-%m-%d-%H-%M-%S`
echo $TODAY

HACKGIT=~/hack/git

export JAVA_HOME=/usr/lib/jvm/java-8-oracle

REMOTE="ssh dantar "

cd $HACKGIT/sse-games/sse-games-rest
mvn clean install

APPNAME=dantar-sse-games

$REMOTE sudo /etc/init.d/$APPNAME stop
$REMOTE cp services/$APPNAME.jar backup/services/$APPNAME-$TODAY.jar
scp $HACKGIT/sse-games/sse-games-rest/target/sse-games-rest-0.0.1-SNAPSHOT.jar dantar:services/$APPNAME.jar
$REMOTE sudo /etc/init.d/$APPNAME start
