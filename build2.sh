#!/bin/sh -ex
./build.sh -s 6700 -o 6633 -r 8080 -d 7888 -y -t $1
./build.sh -s 6800 -o 6634 -r 8081 -d 7889 -y -t $1
if !(test -z $1)
then
rm -rf release-$1
cp -rf release release-$1;
fi



