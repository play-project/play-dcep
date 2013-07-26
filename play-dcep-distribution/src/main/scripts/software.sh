#! /bin/sh
#Scipt to install requierd software to run PLAY cep-components on a Debian machine.

TOOL_URL=https://raw.github.com/play-project/play-dcep/master/play-dcep-distribution/src/main/scripts
PROLOG_SRC_URL=http://www.home.hs-karlsruhe.de/~obst1011/fzi/play/
INSTALL_DIR=/opt/play/
IPv6_PREFIX=2001:6f8:100d:b::

installBasicSoftware(){
	#Add non-free repositories.
	cd /etc/apt/sources.list.d/
	wget $TOOL_URL/non-free-sources.list
	apt-get -y update
	
	#Install tools.
	apt-get -y install screen vim maven2 subversion sun-java6-jdk ntpdate unzip
	update-alternatives --set java /usr/lib/jvm/java-6-sun/jre/bin/java
	export JAVA_HOME=/usr/lib/jvm/java-6-sun-1.6.0.26/
}

installCEP_Engine(){
	# Install SWI-Prolog
	  apt-get --yes install gcc make
	# Install dependencies to compile prolog sources.
	  apt-get --yes install build-essential autoconf curl chrpath ncurses-dev libreadline-dev libunwind7-dev libxext-dev libice-dev libjpeg-dev libxinerama-dev libxft-dev libxpm-dev libxt-dev pkg-config libssl-dev unixodbc-dev junit zlib1g-dev libarchive-dev

	# Compile and install SWI-Prolog
	cd /tmp/
	wget $PROLOG_SRC_URL/pl-5.10.2.tar.gz
	tar -xzf pl-5.10.2.tar.gz
	cd pl-5.10.2
	./build.templ

	# Get and start CEP-Engine
	mkdir $INSTALL_DIR
	cd    $INSTALL_DIR
	wget  $TOOL_URL/dEtalis.jar
	chmod u+x cep-engine.jar
	wget $TOOL_URL/proactive.java.policy
	wget $TOOL_URL/prologMethods.tar
	tar -xf prologMethods.tar
	wget $TOOL_URL/cep-engine.sh
	chmod u+x cep-engine.sh
	screen ./cep-engine.sh etalis$2.dcep.s-node.de
}

installProActive(){
	cd $INSTALL_DIR
	# wget http://www.activeeon.com/public_content/releases/ProActive/3.3.2/ProActiveProgramming-5.3.2_core_bin.zip
	# unzip ProActiveProgramming-5.3.2_core_bin.zip
	
	wget www.home.hs-karlsruhe.de/~obst1011/fzi/play/software/ProActiveProgramming-EC_5.4.0-7b8befc7fd37b4c2a479ec1b3f3ae14c36d41aac.tar.gz
	tar -xzf ProActiveProgramming-EC_5.4.0-7b8befc7fd37b4c2a479ec1b3f3ae14c36d41aac.tar.gz
	
	# Delete bad slf4j version.
	rm $INSTALL_DIR/ProActiveProgramming/dist/lib/slf4j-log4j12-1.5.3.jar

}

setEnvironemenVariables(){
	echo TODO setEnvironemenVariables
	# TODO Update Bash environment variables
	# .bashrc
	# For ssh login.
}

getTool(){
	mkdir $INSTALL_DIR
	cd    $INSTALL_DIR
	wget  $TOOL_URL/dEtalis.jar.jar
	wget  $TOOL_URL/${1}.sh
	wget  $TOOL_URL/proactive.java.policy
	chmod u+x $INSTALL_DIR/${1}.sh
}

setIP_Adress(){
        ip=$IPv6_PREFIX${1}/64
        ip -6 addr add ${ip} dev eth0
        ip -6 route add default via 2001:6f8:100d:b::ffff
}


case "$1" in
  publisher)
	installBasicSoftware
	getTool "publisher"
	setIP_Adress "$2"
  ;;
  subscriber)
	installBasicSoftware
	getTool "subscriber"
	setIP_Adress "$2"
  ;;
  cep-engine)
	installBasicSoftware
	installCEP_Engine
	setIP_Adress "$2"
  ;;
  ProActiveNode)
	installBasicSoftware
	installProActive
	setEnvironemenVariables
	installCEP_Engine
	setIP_Adress "$2"
  ;;
 
 
 *)
	echo "Configure PLAY-Node: {cep-engine|publisher|subscriber|ProActiveNode} ip-postfix"
	exit 1
  ;;
esac

exit 0
