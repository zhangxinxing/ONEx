#!/bin/sh -e

HOME_DIR=`pwd`

# for floodlight only
SYNC_PORT=6788
OPENFLOW_PORT=6633
REST_PORT=8080
DP_PATH='\/tmp\/123'
CONTROLLER_IP=1

# COMMON
ONEX_DAEMON_PORT=7888

# for ONEx
GATEWAY_SERVER_PORT=7999

# dir and files
FLOODLIGHT_ROOT_DIR='/Users/Fan/dev/floodlight'
ONEX_ROOT_DIR="/Users/Fan/dev/ONEx/ONEx"
# files
FLOODLIGHT_PROP_FILE=$FLOODLIGHT_ROOT_DIR"/src/main/resources/floodlightdefault.properties"
FLOODLIGHT_TEST_SETTING_FILE=$FLOODLIGHT_ROOT_DIR"/src/main/java/ONExProtocol/TestSetting.java"
ONEX_CONFIG_FILE=$ONEX_ROOT_DIR"/src/config.properties"

echo "ONEx config.properties>>"
echo "GATEWAY_SERVER_PORT=$GATEWAY_SERVER_PORT"
l=`grep -n GATEWAY_SERVER_PORT $ONEX_CONFIG_FILE | awk -F : '{print $1}'`
sed -i.bak $l's/.*/GATEWAY_SERVER_PORT '$GATEWAY_SERVER_PORT'/' $ONEX_CONFIG_FILE

echo "DAEMON_PORT=$ONEX_DAEMON_PORT"
l=`grep -n DAEMON_PORT $ONEX_CONFIG_FILE | awk -F: '{print $1}'`
sed  -i.bak $l's/.*/DAEMON_PORT '$ONEX_DAEMON_PORT'/' $ONEX_CONFIG_FILE

echo "floodlightdefault.properties>>"
echo "+openflowport=$OPENFLOW_PORT"
javal=`grep -n openflowport $FLOODLIGHT_PROP_FILE`
javaln=`echo $javal | awk -F : '{ print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`$OPENFLOW_PORT
sed -i.bak $javaln's/.*/'"$javalnew"'/' $FLOODLIGHT_PROP_FILE

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
echo ""

echo "TestSetting.java>>"
echo "+controllerIP=$CONTROLLER_IP"
javal=`grep -n controllerIP $FLOODLIGHT_TEST_SETTING_FILE`
javaln=`echo $javal | awk -F : '{print $1;}'`
javalnew=`echo $javal | cut -d: -f2- | awk -F = '{print $1"="}'`"$CONTROLLER_IP;"
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
	clear && vim $ONEX_CONFIG_FILE
	clear && vim $FLOODLIGHT_PROP_FILE
	clear && vim $FLOODLIGHT_TEST_SETTING_FILE
}

build(){
	clear && echo "building..."
	cd $FLOODLIGHT_ROOT_DIR
	ant dist && cp $FLOODLIGHT_ROOT_DIR/target/floodlight.jar \
	$HOME_DIR/"floodlight-$ONEX_DAEMON_PORT(daemon)-$OPENFLOW_PORT(openflow).jar"
	
	cd $ONEX_ROOT_DIR
	ant && cp $ONEX_ROOT_DIR/build/jar/*.jar \
		$HOME_DIR/"daemon-$ONEX_DAEMON_PORT(daemon)-$OPENFLOW_PORT(openflow).jar"
	cp $ONEX_ROOT_DIR/lib/*.so $HOME_DIR
	cp $ONEX_ROOT_DIR/lib/*.dll $HOME_DIR
	cp $ONEX_ROOT_DIR/lib/*.dylib $HOME_DIR

}

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



