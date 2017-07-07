System Requirements
===================

* JDK 1.8 or greater (http://java.sun.com/)
* Ant 1.9 or greater (http://ant.apache.org/)
* JUnit 4.11 or greater (http://www.junit.org/) (which needs the separate
  "hamcrest-core" .jar file)
* Maven Ant Tasks 2.1.3 or greater (http://maven.apache.org/) (optional)

Project Set-Up
==============

* Ensure that JUnit is on your Ant classpath (set the CLASSPATH environment
  variable accordingly):

  http://ant.apache.org/manual/Tasks/junit.html

* Ensure that the LiveConnect APIs are on your classpath (set the CLASSPATH
  environment variable accordingly):

  Windows and Linux:
  http://java.sun.com/javase/6/webnotes/6u10/plugin2/liveconnect/#COMPILING

  Mac OS X:
  http://developer.apple.com/qa/qa2004/qa1364.html

  IMPORTANT This document has not been updated for Java on Mac OS X.


  Since Java 7, ensure to have jfxrt.jar file excluded from compile classpath
  (build files are already setup for this), or build errors will happen
  (due to duplicate class in classpath).

* Ensure that the JNLP APIs are on your classpath; follow the same process as
  described in the previous step, substituting javaws.jar for plugin.jar.

* Ensure that the Maven Ant tasks are installed (optional); see:

  http://maven.apache.org/ant-tasks/installation.html


Building Pivot
==============

* To compile all Pivot source files into binary class files:

  $ ant compile

* To generate Javadoc

  $ ant doc

* To generate the Pivot source distribution

  $ ant dist

* To generate the Pivot binary distribution

  $ ant install

* To install in a local Maven repository (requires Maven Ant tasks):

  $ ant maven-install

----
