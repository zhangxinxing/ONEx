#!/bin/sh
# Fan Zhang<hello.zhang1992@gmail.com>

echo "start test"

if [ $# -ne 2 ]
then 
	echo "Usage: ./run-test.sh [-s|-c] PORT"
	exit -1;
fi

case $1 in
	-s | --server)
		ant run-only -Dport $2 -Dpktgen false
		;;
	-c | --client)
		ant run-only -Dport $2 -Dpktgen true
		;;
	* )
		echo "Wrong arguments"
		;;
esac

