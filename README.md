# thermostat-vm-decompiler
Runtime decompiler Thermostat plugin

This plugin is developed in cooperation with Thermostat team and intention to once become part of Thermostat. For it's correct run, it needs Thermostat 1.99.12 version or higher. Lower versions were not tested and some of the APIs used are definitely not supported in Thermostat 1.6.

This plugin is able to use existing decompilers to decompile bytecode loaded in a running JVM. It can list all classes loaded in the JVM and then extract and decompile the bytes back to source code. The quality of the decompilation depends heavily on the used decompiler. This decompiler can be used on any JVM-based language, if there is correct decompiler loaded.

Recommended decompiler for decompiling bytecode back to Java is Proycon decompiler (https://bitbucket.org/mstrobel/procyon/wiki/Java%20Decompiler) as it provides good quality of decompiled code.

How to run the decompiler:

Check whether you got installed following packages: maven javapackages-tools javapackages-tools maven-dependency-plugin  maven-shade-plugin maven-surefire-plugin  maven-war-plugin maven-clean-plugin maven-assembly-plugin maven-plugin-bundle maven-javadoc-plugin maven-antrun-plugin maven-archetype-packaging libsecret-devel libgnome-keyring-devel autoconf automake libtool gtk2-devel tomcat javapackages-tools java-devel java-1.8.0-openjdk-devel mongodb-server  mongodb  libsecret libgnome-keyring systemd 

Those packages might be different on various linux distribution, in that case, follow the maven error output until you successfuly build thermostat.

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

This should create a distribution directory in the cloned thermostat directory(THERMOSTAT_CLONE):

4. Go to 
 - cd THERMOSTAT_CLONE/distribution/target/image/plugins
 - mkdir thermostat-vm-decompiler
 - copy the decompiler plugin jar and thermostat-plugin.xml from thermostat-vm-decompiler to this folder
 - copy the decompiler plugin jar to directory THERMOSTAT_CLONE/distribution/target/image/webapp/WEB-INF/lib

5. Run thermostat setup
- cd THERMOSTAT_CLONE/distribution/target/image/bin 
- sh thermostat setup

5. Edit configuration
 - go to THERMOSTAT_CLONE/distribution/target/image/etc
 - edit thermostat-roles.properties file: 
 - add the line: "thermostat-cmdc-grant-vm-decompiler-get-bytecode" (without "") into Recursive role granting all CMD-channel actions. Do not forget to separate last role with ", \" like others.

Now, the plugin should be set, last, you need the decompiler to perform the decompilation.
Default decompiler is Proycon decompiler: https://bitbucket.org/mstrobel/procyon/downloads/ , but any decompiler that takes a class file as input and results into string of source code should be working with this decompiler. 
 - download jar
 - set environment variable PATH_TO_GIVEN_DECOMPILER_JAR to absolute path to the decompiler jar

Now, you are all set and can start thermostat:

- cd THERMOSTAT_CLONE/distribution/target/image/bin
- sh thermostat 

After selecting a VM, you should see a Decompiler tab. Click on it and you are all set. Note, that not all the listed tabs are instrumentable, so not all of them can use the decompiler feature. Try e.g. thermostat local or thermostat gui JVM.

If you got any trouble running Thermostat, try to run thermostat setup, check if you got all the packages (especially mongodb and mongodb-server) and if it does not work, try Thermostat wiki. https://icedtea.classpath.org/wiki/Thermostat

