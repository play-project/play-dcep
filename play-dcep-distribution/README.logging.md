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

Logging in DCEP
===============
Logging in DCEP is done mostly though SLF4J but some dependencies require workarounds such as logging bridges.

* [DCEP itself](https://github.com/play-project/play-dcep/), [RDF2Go](http://semanticweb.org/wiki/RDF2Go), [EventCloud](https://bitbucket.org/eventcloud/eventcloud) and others log though SLF4J to logback (recommended practise)
* [jtalis](https://code.google.com/p/etalis/) and [Jersey 2.x](https://jersey.java.net/) log to `java.util.logging` (JUL) and is redirected through the `jul-to-slf4j` bridge
* [ProActive and GCM](http://proactive.activeeon.com) log to LOG4J (and should not be redirected because of some custom LOG4J appenders)

Finally:
* logback writes to STDOUT (configured in `conf/log4j.xml`)
* LOG4J writes to STDOUT (configured in `conf/logback.xml`)
* STDOUT is redirected to `log/dcep.log` in the install directory (see [README.md](README.md)).

    DCEP -------------.
	                   \
    EventCloud ---------. 
                         \
    RDF2Go -------------- SLF4J/logback --- STDOUT --- dcep.log
	                     /                 /
                        /                 /
    Jersey 2.x --- JUL ´                 /
                 /                      /
    jtalis ---- ´                      /
                                      /
                                     /
    ProActive/GCM ----------- LOG4J ´

