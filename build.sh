#!/bin/sh -ex
HOME_DIR=`pwd`

LOCAL_IP=`ifconfig en0 | grep inet | grep -v inet6 | awk '{print $2}'`
LOCAL_IP='192.168.190.1'

# dir and files
FLOODLIGHT_ROOT_DIR='/Users/Fan/dev/floodlight'
ONEX_ROOT_DIR="/Users/Fan/dev/ONEx/ONEx"
# files
FLOODLIGHT_PROP_FILE=$FLOODLIGHT_ROOT_DIR"/src/main/resources/floodlightdefault.properties"
FLOODLIGHT_TEST_SETTING_FILE=$FLOODLIGHT_ROOT_DIR"/src/main/java/ONExProtocol/TestSetting.java"
FLOODLIGHT_LOG4J_CONFIG_FILE=$FLOODLIGHT_ROOT_DIR"/src/main/resources/log4j.properties"


while getopts "s:o:r:d:yt:" optname
do
    case "$optname" in
    "s")
	SYNC_PORT=$OPTARG
        ;;
    "o")
	OPENFLOW_PORT=$OPTARG
        ;;
    "r")
	REST_PORT=$OPTARG
        ;;
    "d")
	ONEX_DAEMON_PORT=$OPTARG
        ;;
    "y")
	yes_mode="yes"
		;;
	"t")
		
		case $OPTARG in
	
	"discard")
		# discard
		javal=`grep -n 'isDiscard' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"true;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
	
		javal=`grep -n 'isStrictLearn' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"true;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
	
		javal=`grep -n 'threshold' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"2;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
		;;
	
	"flood")
		# flood
		javal=`grep -n 'isDiscard' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"false;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
	
		javal=`grep -n 'isStrictLearn' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"true;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
	
		javal=`grep -n 'threshold' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"2;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
		;;
	
	"coop")
	# coop
		javal=`grep -n 'isDiscard' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"false;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
	
		javal=`grep -n 'isStrictLearn' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"false;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
	
		javal=`grep -n 'threshold' $FLOODLIGHT_TEST_SETTING_FILE`
		javaln=`echo $javal | awk -F : '{ print $1;}'`
		javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"2;"
		sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE
		;;
		
		*) ;;
	
	esac
	;;
	
    *)
        echo "Unknown error while processing options"
        ;;
    esac
done

# for floodlight only

SYNC_PORT=${SYNC_PORT-6700}
OPENFLOW_PORT=${OPENFLOW_PORT-6633}
REST_PORT=${REST_PORT-8080}
ONEX_DAEMON_PORT=${ONEX_DAEMON_PORT-7888}

temp=`mktemp -d /tmp/XXXXXX | cut -d '/' -f 3`
CONTROLLER_IP=$LOCAL_IP
DP_PATH='\/tmp\/'$temp
# COMMON



echo "+openflowport=$OPENFLOW_PORT"
javal=`grep -n openflowport $FLOODLIGHT_PROP_FILE`
javaln=`echo $javal | awk -F : '{ print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`$OPENFLOW_PORT
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_PROP_FILE

javal=`grep -n log4j.appender.FILE.File $FLOODLIGHT_LOG4J_CONFIG_FILE`
javaln=`echo $javal | awk -F : '{ print $1;}'`
sed -i.bak $javaln's/.*/'"log4j.appender.FILE.File=ONExBox"$OPENFLOW_PORT".log"'/' $FLOODLIGHT_LOG4J_CONFIG_FILE

echo "+RestApiServer.port=$REST_PORT"
javal=`grep -n 'RestApiServer.port' $FLOODLIGHT_PROP_FILE`
javaln=`echo $javal | awk -F : '{ print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`$REST_PORT
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_PROP_FILE

echo "+dbPath=$DP_PATH"
javal=`grep -n 'dbPath' $FLOODLIGHT_PROP_FILE`
javaln=`echo $javal | awk -F : '{ print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`$DP_PATH
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_PROP_FILE

echo "+controllerIP=$CONTROLLER_IP"
javal=`grep -n controllerIP $FLOODLIGHT_TEST_SETTING_FILE`
javaln=`echo $javal | awk -F : '{print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"\"$CONTROLLER_IP\";"
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE

echo "+syncPort=$SYNC_PORT"
javal=`grep -n syncPort $FLOODLIGHT_TEST_SETTING_FILE`
javaln=`echo $javal | awk -F : '{print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"$SYNC_PORT;"
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE

echo "+OPENFLOW_PORT=$OPENFLOW_PORT"
javal=`grep -n OPENFLOW_PORT $FLOODLIGHT_TEST_SETTING_FILE`
javaln=`echo $javal | awk -F : '{print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"$OPENFLOW_PORT;"
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE

echo "+REST_PORT=$REST_PORT"
javal=`grep -n REST_PORT $FLOODLIGHT_TEST_SETTING_FILE`
javaln=`echo $javal | awk -F : '{print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"$REST_PORT;"
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE

echo "+ONExDAEMON_PORT=$ONEX_DAEMON_PORT"
javal=`grep -n ONExDAEMON_PORT $FLOODLIGHT_TEST_SETTING_FILE`
javaln=`echo $javal | awk -F : '{print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"$ONEX_DAEMON_PORT;"
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_TEST_SETTING_FILE

confirm(){
	vim $FLOODLIGHT_PROP_FILE
	vim $FLOODLIGHT_TEST_SETTING_FILE
	vim $FLOODLIGHT_LOG4J_CONFIG_FILE
}

build(){
	echo "building..."
	TARGET_DIR=$HOME_DIR/release
	mkdir -p $TARGET_DIR
	
	cd $FLOODLIGHT_ROOT_DIR
	rm -rf target
	ant && cp $FLOODLIGHT_ROOT_DIR/target/floodlight.jar \
	$TARGET_DIR/"floodlight-o$OPENFLOW_PORT-d$ONEX_DAEMON_PORT.jar"
	
	if test $yes_mode
	then yn="Yes"
	else
		read -p "recompile ONExService?[y/n]" yn
	fi
	case $yn in 
	[Yy]* )		
		cd $ONEX_ROOT_DIR
		ant && cp $ONEX_ROOT_DIR/build/jar/*.jar \
			$TARGET_DIR/"ONExService-pd-ps.jar"
	
		cd $ONEX_ROOT_DIR/lib/;
		for i in `find . -name "libsigar*"`;
		do
			cp $i $TARGET_DIR
		done;
		break;;
	*) echo "Bye!";
	esac
}

if test $yes_mode
then 
	build;
	echo "Enjoy!"
	exit;
fi

while true; do
    read -p "Confirm? [y/n]" yn
    case $yn in
        [Yy]* ) confirm; break;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

while true; do
    read -p "Build? [y/n]" yn
    case $yn in
        [Yy]* ) build; break ;;
        [Nn]* ) break;;
        * ) echo "Please answer yes or no.";;
    esac
done

echo "Enjoy!"
