System Requirements
===================

* JDK 1.6 or greater (http://java.sun.com/)
* JUnit 4.8 or greater (http://www.junit.org/)
* Maven 2.2.1 or greater (http://maven.apache.org/), recommended the latest 3.0.x

Project Set-Up
==============

* Ensure that Maven is on your classpath (set the PATH environment variable accordingly), 
  or if using an IDE, that is Maven compatible


Building/Using the Apache Pivot Quickstart
==========================================

* Optional, import the project inside an IDE.
  For importing it into Eclipse, you can try to Import it as a Maven Project (with maven plugin installed), 
  but you can even ask maven to generate eclipse project files, so you can Import as Standard Project, and then let it handle by Maven,
  for example using:
  $ cd myapp
  $ mvn -U eclipse:eclipse
  then open your IDE and Import the generated project.
  With other IDEs, steps shown here are similar.

* To Test, suppose that the current application is called "myapp", and all classes are under the package "com.mycompany.myapp":
  $ cd myapp
  $ mvn -U clean package
  $ mvn -U exec:java

* Note that all maven -U flags shown here are optional, but useful to ensure that latest versions will be used.


* Run the application standalone (without Maven):
  for convenience there is a main method inside the PivotApplication class), so you can even run 
  com.mycompany.myapp.PivotApplication as Standard Java Application.
