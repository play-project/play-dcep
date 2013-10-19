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

Dependencies
------------
Tested on `CentOS release 6.3 (Final) 64bit`:

### Runtime Requirements:
#### Java (>=1.6)
#### SWI Prolog (5.10.2)
```
yum install readline-devel libjpeg
cd /tmp
wget http://kojipkgs.fedoraproject.org/packages/pl/5.10.2/3.fc15/x86_64/pl-static-5.10.2-3.fc15.x86_64.rpm
wget http://kojipkgs.fedoraproject.org/packages/pl/5.10.2/3.fc15/x86_64/pl-jpl-5.10.2-3.fc15.x86_64.rpm
wget http://kojipkgs.fedoraproject.org/packages/pl/5.10.2/3.fc15/x86_64/pl-5.10.2-3.fc15.x86_64.rpm
rpm -i pl-*.rpm
```
Add `--nodeps` to the rpm command if there is a problem with an old version of libjpeg which is actually on your system already.


Add JPL library (`libjpl.so`) to `$LD_LIBRARY_PATH` (do this permanently by adding to `/etc/profile.d/swipl.sh`):
```
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib64/swipl-jpl:/usr/lib/
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
Use the init.d start script from here: [RedHat, CentOS](https://gist.github.com/stuehmer/5481356), [Debian](http://tw2.tw.rpi.edu/zhengj3/virtuoso/virtuoso-opensource-6.1.1/debian/init.d)
```
chkconfig virtuoso on
```

#### 4store (>=1.1.5, optional)
See http://4store.org/trac/wiki/Download or build 4store form source to get the newest bugfixes and SPARQL 1.1 features.
```
yum install mhash yajl mpfr redland rasqal avahi-glib
rpm --import http://repo.sparql.pro/RPM-GPG-KEY-SPARQL-PRO
rpm -Uvh http://repo.sparql.pro/centos/6/x86_64/4store-1.1.5-4.x86_64.rpm
```
* `--nodeps` can be added to the last command if the dependencies are not properly recognized.
* This might also be needed to fix the dependencies:
```
ln -s /usr/lib64/libyajl.so.2 /usr/lib64/libyajl.so.1
ln -s /usr/lib64/libmpfr.so.4 /usr/lib64/libmpfr.so.1
```

Build
-----
To build DCEP in `/tmp`:
```
cd /tmp
git clone https://github.com/play-project/play-dcep.git
cd play-dcep/
mvn install -DskipTests
```
The installer package `dcep-install.zip` will be in `/tmp/play-dcep/play-dcep-distribution/target/`

Unpack/Configure
----------------
1. Download or build (see above) the file `dcep-install.zip`
2. Unzip
3. `cd dcep`
4. Edit `conf/play-commons-constants.properties` for endpoints where events can be received and sent. Adapt the file from these defaults: [default properties](https://github.com/play-project/play-commons/blob/master/play-commons-constants/src/main/resources/play-commons-constants-defaults.properties)
5. Edit `conf/play-dcep-distribution.properties` for startup behaviour. Adapt the file from these defaults: [default properties](https://github.com/play-project/play-dcep/blob/master/play-dcep-api/src/main/resources/play-dcep-distribution-defaults.properties)

Run
---
1. Run `bin/dcep start` on Unix
2. The program can be terminated using `bin/dcep stop`