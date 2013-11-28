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

Developing DCEP with Eclipse
================================================
We are mostly using Eclipse for developing DCEP. We are heavily relying on the
[M2E Eclipse Plugin](http://www.eclipse.org/m2e/) for Maven integration so that
Eclipse projects are generated from what's in our POM files. You should import 
the DCEP source into Eclipse using `File -> Import... -> Existing Maven Projects`.

To manage code styling in Eclipse and M2E we use the
[maven-m2e-codestyle plugin](https://github.com/germanklf/maven-m2e-codestyle)
also documented [here](http://stackoverflow.com/questions/14008733/how-to-use-maven-m2e-codestyle-connector).
The plugin should automatically load code styles and Eclipse Save Actions while
you import the DCEP sources into Eclipse.

If you use [EGit](http://www.eclipse.org/egit/) in Eclipse to commit changes you
must set `Preferences -> Team -> Git -> Configuration` to:
```
[core]
	autocrlf = true
```
... because EGit ignores the `.gitattributes` files which we use to make these
settings. You can track these problems here: [EGit bug](https://bugs.eclipse.org/bugs/show_bug.cgi?id=342372) and
[EGit bug](https://bugs.eclipse.org/bugs/show_bug.cgi?id=421364).

Optionally you may want to use the [GitHub Mylyn Connector](http://eclipse.github.com/) to display DCEP issues from 
Github.com within Eclipse.