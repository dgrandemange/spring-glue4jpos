spring-glue4jpos
================

Dedicated to Spring dependency injection of Q2 components among transaction participants and NameRegistrar entries.

Adding spring-glue4jpos to your mavenized jPos project :
---------------------------------------------------------
Simply add the following snippet to your pom.xml's dependencies section :

    <repositories>
    
      <!-- ... your specific repositories if there are any ... -->
    
      <repository>
    		<id>dgrandemange-mvn-repo-releases</id>
    		<name>dgrandemange GitHub Maven Repository releases</name>
    		<url>https://github.com/dgrandemange/dgrandemange-mvn-repo/raw/master/releases/</url>
    	</repository>
    
    </repositories>

    <dependencies>
    
        <!-- ... your project dependencies -->
    
    		<dependency>
        	<groupId>org.jpos.jposext</groupId>
        	<artifactId>springglue</artifactId>
        	<version>1.0.1</version>
        </dependency>        
    </dependencies>

Running the demo :
------------------ 
First, 'mvn -Pdemo install'
Then, under runtime directory 'java -jar q2.jar'

Check 'cfg/spring' and 'deploy' dirs configs for more info