# thermostat-vm-decompiler
Runtime decompiler Thermostat plugin

This plugin is developed in cooperation with Thermostat team and intention to once become part of Thermostat. For it's correct run, it needs Thermostat 1.99.12 version or higher. Lower versions were not tested and some of the APIs used are definitely not supported in Thermostat 1.6.

This plugin is able to use existing decompilers to decompile bytecode loaded in a running JVM. It can list all classes loaded in the JVM and then extract and decompile the bytes back to source code. The quality of the decompilation depends heavily on the used decompiler. This decompiler can be used on any JVM-based language, if there is correct decompiler loaded.

Recommended decompiler for decompiling bytecode back to Java is Proycon decompiler (https://bitbucket.org/mstrobel/procyon/wiki/Java%20Decompiler) as it provides good quality of decompiled code.

How to run the decompiler:
Check whether you got installed following packages: maven, 

1. You need thermostat built and installed
 - hg clone http://icedtea.classpath.org/hg/thermostat/
 - cd thermostat
 - mvn clean install

2. Clone and build this decompiler plugin:
 - git clone https://github.com/pmikova/thermostat-vm-decompiler
 - cd thermostat-vm-decompiler
 - mvn clean install
 - the decompiler jar should be now in target directory

3. Clone and build decompiler's agent
 - git clone https://github.com/pmikova/thermostat-decompiler-agent
 - cd thermostat-decompiler-agent
 - mvn clean install
 - set environment variable THERMOSTAT_DECOMPILER_AGENT_JAR to absolute path to the decompiler agent jar

3. You need thermostat built and installed
 - hg clone http://icedtea.classpath.org/hg/thermostat/
 - cd thermostat
 - mvn clean install

This should create a distribution directory, where thermostat is installed:
4. Go to 
 - cd THERMOSTAT_HOME/distribution/target/image/plugins
 - mkdir thermostat-vm-decompiler
 - copy the decompiler jar and thermostat-plugin.xml to this folder
 - copy the decompiler jar to directory THERMOSTAT_HOME/distribution/target/image/webapp/WEB-INF/lib
5. Edit configuration
 - go to THERMOSTAT_HOME/distribution/target/image/etc
 - edit thermostat-roles.properties file: 
       - add the line: thermostat-cmdc-grant-vm-decompiler-get-bytecode into Recursive role granting all CMD-channel actions.

Now, the plugin should be set, last, you need the decompiler to perform the decompilation.
Default decompiler is Proycon decompiler: https://bitbucket.org/mstrobel/procyon/overview , but any decompiler that takes a class file as input and results into string of source code should be working with this decompiler. Build the decompiler, if neccessary.
 - set environment variable PATH_TO_GIVEN_DECOMPILER_JAR to absolute path to the decompiler jar

Now, you are all set and can start thermostat:

- cd THERMOSTAT_HOME/distribution/target/image/bin
- sh thermostat local

After selecting a VM, you should see a Decompiler tab. Click on it and you are all set.

