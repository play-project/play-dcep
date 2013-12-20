#!/bin/sh
INSTALL_DIR=/opt/play/

# Update time.
ntpdate 0.de.pool.ntp.org 1.de.pool.ntp.org 2.de.pool.ntp.org

# Start publisher.
cd $INSTALL_DIR
java -Djava.security.manager -Djava.security.policy=proactive.java.policy  -Dproactive.net.interface=eth0  -Dproactive.net.noprivate=true -cp .:dEtalis.jar eu.play_project.dcep.distribution.tests.srbench.performance.SingleDistributedEtalisInstancePublisher  $1 $2 $3 $4
