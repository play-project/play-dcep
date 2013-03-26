cd /opt/cep-engine

#Start CEP-Engine
export SWI_HOME_DIR=/usr/lib/swipl-5.10.2

export PATH=$PATH:/usr/lib/swipl-5.10.2/lib/x86_64-linux/
export PATH=$PATH:/usr/lib/swipl-5.10.2/lib/
export PATH=$PATH:/usr/lib/swipl-5.10.2/library/
export SWI_HOME_DIR=/usr/lib/swipl-5.10.2

lib_path=$PATH:$SWI_LIB_PATH:$SWI_BIN_PATH
export LD_LIBRARY_PATH=$lib_path
export DYLD_LIBRARY_PATH=$lib_path

java -Djava.security.manager -Djava.security.policy=proactive.java.policy -Djavlibrary.path=.:sr/lib/swipl-5.10.2/lib/x86_64-linux/ -Dproactive.net.interface=eth0  -Dproactiveet.noprivate=true -Dproactive.net.disableIPv6=false -Dproactive.hostname=$1 -jar cep-engine.jar
