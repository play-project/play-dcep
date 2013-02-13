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
Tested on `CentOS release 6.3 (Final)`.

### Runtime Requirements:
#### Java
#### SWI Prolog
```
yum install readline-devel libjpeg
cd /tmp
wget http://kojipkgs.fedoraproject.org//packages/pl/5.10.2/3.fc15/x86_64/pl-static-5.10.2-3.fc15.x86_64.rpm
wget http://kojipkgs.fedoraproject.org//packages/pl/5.10.2/3.fc15/x86_64/pl-jpl-5.10.2-3.fc15.x86_64.rpm
wget http://kojipkgs.fedoraproject.org//packages/pl/5.10.2/3.fc15/x86_64/pl-5.10.2-3.fc15.x86_64.rpm
rpm -i --nodeps pl-*.rpm
```
#### Tomcat
```
yum install tomcat6 tomcat6-admin-webapps
```
#### PLAY DSB

### Build Requirements:
* Maven 3.x
* Git

Issues
------
For issues and bug reporting, please go to https://github.com/play-project/play/issues?labels=dcep&amp;page=1&amp;state=open
