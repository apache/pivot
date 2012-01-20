System Requirements
===================

* JDK 1.6 or greater (http://java.sun.com/)
* JUnit 4.8.2 or greater (http://www.junit.org/)
* Maven 2.2.1 or greater (http://maven.apache.org/), recommended the latest 3.0.x

Project Set-Up
==============

* Ensure that Maven is on your classpath (set the PATH environment variable accordingly)

Building Apache Pivot Quickstart Archetype
==========================================

* To install the archetype in a local Maven repository:

  $ cd pivot-archetype-quickstart
  $ mvn -U install
  $ cd ..

* To Test, generate a Pivot Project (called "myapp") with it 
  (and give to it the desired package name when requested, like "com.mycompany.myapp", 
  note that usually the ".myapp" part in the package is written only if overridden here):
  $ mvn archetype:generate -U -DarchetypeGroupId=org.apache.pivot -DarchetypeArtifactId=pivot-archetype-quickstart -DarchetypeVersion=1.0.1 -DgroupId=com.mycompany -DartifactId=myapp -Dversion=1.0
  then, compile the project and run it.

* Note that all maven -U flags shown here are optional, but useful to ensure that latest versions will be used.
