    ...........................................................................
    ........:$$$7..............................................................
    .....==7$$$I~~...........MMMMMMMMM....DMM..........MM,........MM7......MM..
    ...,?+Z$$$?=~,,:.........MMM,,,?MMM+..MMM.........,MMMM,......7MM,....MMM..
    ..:+?$ZZZ$+==:,:~........MMM.....MMM..MMM.........,MMDMMM:.....,MMI..MMM...
    ..++7ZZZZ?+++====,.......MMM....~MMM..MMM.........,MM??DMMM:....?MM,MMM....
    ..?+OZZZ7~~~~OOI=:.......MMMMMMMMMM...MMM.........,MM?II?MMM~....DMMMM.....
    ..+7OOOZ?+==+7Z$Z:.......MMM$$$I,.....MMM.........,MM??8MMM~......NMM......
    ..:OOOOO==~~~+OZ+........MMM..........MMM.........,MMDMMM~........NMM......
    ..,8OOOO+===+$$?,........MMM..........MMM.,,,,,...,MMMM:..........NMM......
    ,,+8OOOZIIIIII=,,,,,,,,,,MMM,,,,,,,,,,NMMMMMMMMM=,,MM:,,,,........8MM......
    ,,,:O8OO~+~:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
                                                      ASCII Art: GlassGiant.com

PLAY Distributed Complex Event Processing (DCEP) INSTALLATION
=============================================================
DCEP is the complex event processor in PLAY. Our approach is based on declarative
(logic) rules. We will bring this approach to the cloud creating a large-scale,
elastic CEP service which dynamically adapts to fluctuating event frequencies.

DCEP is a native RDF event processor, which means RDF can be used directly for
events and there is no need to model mappings to binary formats.

Installation
------------
Tested on `CentOS release 6.3 (Final)`:

### Runtime Requirements:
#### Java (>=1.6)
#### SWI Prolog (5.10.2)
```
yum install readline-devel libjpeg
cd /tmp
wget http://kojipkgs.fedoraproject.org//packages/pl/5.10.2/3.fc15/x86_64/pl-static-5.10.2-3.fc15.x86_64.rpm
wget http://kojipkgs.fedoraproject.org//packages/pl/5.10.2/3.fc15/x86_64/pl-jpl-5.10.2-3.fc15.x86_64.rpm
wget http://kojipkgs.fedoraproject.org//packages/pl/5.10.2/3.fc15/x86_64/pl-5.10.2-3.fc15.x86_64.rpm
rpm -i pl-*.rpm
```
Add `--nodeps` to the rpm command if there is a problem with an old version of libjpeg which is actually on your system already.

#### Tomcat (>=6)
```
yum install tomcat6 tomcat6-admin-webapps
chkconfig tomcat6 on
```
#### Virtuoso (>=6.1.7, optional)
I created an SRPM based on 6.1.6 myself using instructions from http://wiki.centos.org/HowTos/RebuildSRPM 
```
# Install build dependencies as root:
sudo yum install gperf htmldoc pkgconfig pkgconfig mock libxml2-devel libiodbc-devel
sudo useradd -s /sbin/nologin mockbuild
# Build Virtuoso as non-root user:
mkdir -p ~/rpmbuild/{BUILD,RPMS,SOURCES,SPECS,SRPMS}
echo '%_topdir %(echo $HOME)/rpmbuild' > ~/.rpmmacros
rpm -i http://ftp.pbone.net/mirror/download.fedora.redhat.com/pub/fedora/linux/updates/16/SRPMS/virtuoso-opensource-6.1.6-1.fc16.src.rpm
# Replace content of tarball ~/rpmbuild/SOURCES with latest code from https://github.com/openlink/virtuoso-opensource branch develop/6 and then:
cd ~/rpmbuild/SPECS
rpmbuild -ba virtuoso-opensource.spec
cd ~/rpmbuild/RPMS/

# Install Virtuoso as root:
sudo rpm -Uvh */virtuoso-opensource-conductor-6.1.6*.rpm */virtuoso-opensource-6.1.6*.rpm */virtuoso-opensource-utils-6.1.6*.rpm
```

If the newer Virtuoso builds should appear e.g. in EPEL then the procedure becomes easier:
```
rpm -i http://ftp-stud.hs-esslingen.de/pub/epel/6/i386/epel-release-6-8.noarch.rpm
yum install virtuoso-opensource virtuoso-opensource-conductor virtuoso-opensource-utils
```
Use the init.d start script from here: http://tw2.tw.rpi.edu/zhengj3/virtuoso/virtuoso-opensource-6.1.1/debian/init.d
```
chkconfig virtuoso on
```
#### PLAY DSB
https://github.com/PetalsLinkLabs/petals-dsb/

### Build Requirements:
#### Maven (3.x)
#### Git
```
yum install git
```

Issues
------
For issues and bug reporting, please go to https://github.com/play-project/play/issues?labels=dcep&amp;page=1&amp;state=open
