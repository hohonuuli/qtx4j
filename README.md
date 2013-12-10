# QTX4J

## Overview

The qtx4j project provides extensions and utilities for working with the [QuickTime for Java API](http://developer.apple.com/quicktime/qtjava/). It was originally developed by the [Monterey Bay Aquarium Research Institute](http://www.mbari.org) in support of the [Video Annotation and Reference System (VARS)](https://github.com/hohonuuli/vars).

## About

QT4J is short-hand for QuickTime for Java. This project encapsulates Java 
components that interact with Apple's QuickTime for Java SDK. In order to build 
this project you will need to install QuickTime for Java and the Java Advanced 
Imaging library. QuickTime for Java is available on Mac OS X (including the latest release - 10.9). QuickTime for Java is installed on Windows when you install QuickTime.

QT4J requires that the Java Advanced imaging library is available. 

## Disclaimer

QuickTime for Java has been deprecated by Apple and the functionality of the library is definitely degrading. With that said we are still able to use qtx4j's functionality on Windows and Apple's 32-bit JVM. QTX4J will not run on Apple's 64-bit JVM.

## Maven

QTX4J Builds are available in [MBARI's Maven repository](https://code.google.com/p/mbari-maven-repository/). To include it in your project add the following to your _pom.xml_ file:
```xml
<!-- Add the MBARI repository -->
<repository>
    <id>mbari-maven-repository</id>
    <name>MBARI Maven Repository</name>
    <url>http://mbari-maven-repository.googlecode.com/svn/repository/</url>
</repository>


<!-- Add qtx4j dependency -->
<dependency>
    <groupId>org.mbari</groupId>
    <artifactId>qtx4j</artifactId>
    <version>0.18</version>
</dependency>
```

This project is built using [Maven 2](http://maven.apache.org). To build it type `mvn package` at the command line. The resulting build will be _target/qt4j-[version].jar_

__EXAMPLE FOR RUNNING CLASS FROM COMMAND LINE__
mvn exec:java -Dexec.mainClass=org.mbari.qt.examples.TimeCodeTrackExample -Dexec.keepAlive=true

