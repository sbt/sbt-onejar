[sbt-onejar][1] is a plug-in for Simple Build Tool that packages your application and its
dependencies in a single, executable JAR file using [One-JAR™](http://one-jar.sourceforge.net)*

The executable JAR contains your third party libraries as nested JARs, much like a WAR file.

Alternatives ways to package your application:
 * As an über-JAR with [sbt-assembly][2]
 * Obfuscated and trimmed of fat with [xsbt-proguard-plugin][3]
 * With a grahical installer via [sbt-izpack][4]

[1]: https://github.com/retronym/sbt-onejar
[2]: https://github.com/eed3si9n/sbt-assembly
[3]: https://github.com/siasia/xsbt-proguard-plugin
[4]: http://software.clapper.org/sbt-izpack/