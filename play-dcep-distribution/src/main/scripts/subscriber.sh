#!/bin/sh
INSTALL_DIR=/opt/play/
cd $INSTALL_DIR
java -Djava.security.manager -Djava.security.policy=proactive.java.policy  -Dproactive.net.interface=eth0  -Dproactive.net.noprivate=true -Dproactive.net.disableIPv6=false -jar subscriber.jar $1 $2 $3 $4

