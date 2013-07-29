cd /opt/play

#Update time
ntpdate 0.de.pool.ntp.org 1.de.pool.ntp.org 2.de.pool.ntp.org

export SWI_HOME_DIR=/usr/lib/swipl-5.10.2

# Add needet libs to library path.
export PATH=$PATH:/usr/lib/swipl-5.10.2/lib/x86_64-linux/
export PATH=$PATH:/usr/lib/swipl-5.10.2/lib/
export PATH=$PATH:/usr/lib/swipl-5.10.2/library/
export SWI_HOME_DIR=/usr/lib/swipl-5.10.2

lib_path=$PATH:$SWI_LIB_PATH:$SWI_BIN_PATH
export LD_LIBRARY_PATH=$lib_path
export DYLD_LIBRARY_PATH=$lib_path

# Start CEP-Engine
# java -Djava.security.manager -Djava.security.policy=proactive.java.policy -Djavlibrary.path=.:sr/lib/swipl-5.10.2/lib/x86_64-linux/ -Dproactive.net.interface=eth0  -Dproactiveet.noprivate=true -Dproactive.hostname=$1 -cp .:dEtalis.jar eu.play_project.dcep.distribution.tests.srbench.performance.SingleDistributedEtalisInstanceRunner
 java -Djava.security.policy=proactive.java.policy -Dproactive.home=/opt/play/ProActiveProgramming -cp '/opt/play/ProActiveProgramming/dist/lib/*:/opt/play/dcep-jar-with-dependencies.jar' eu.play_project.dcep.distribution.Main