

# DiversiType
Randomly diversifies types of polymorphic variables in application code when running a test suite

## Prerequisites
To use this plugin,

### 1- Your project should use the standard maven structure:

```
projectDirectory
    /src
        /main/java
        /test/java
    /target
```

### 2- This plugin need the path to your jar project.
So, before to use it, launch the maven command:
```
mvn package
```

### 3-Your project should have dependency to the surefire plugin
Add this dependency to your pom.xml:
```
 <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <testFailureIgnore>true</testFailureIgnore>
        </configuration>
      </plugin>
```
The "testFailureIgnore" configuration is important.
Maybe your tests will failed during the mutation ;)


## How use this maven plugin

### 1- Add dependencies in the pom.xml
```
      <plugin>

        <groupId>fr.inria.diversify</groupId>
        <artifactId>DiversiType</artifactId>
        <version>1.0-SNAPSHOT</version>
        <configuration>
          <projectDirectory>/home/guerin/Documents/INRIA/ExProj/ProjA/</projectDirectory>
          <nChange>1</nChange>
          <jarLocation>/home/guerin/Documents/INRIA/ExProj/ProjA/target/ProjA-1.0-SNAPSHOT.jar</jarLocation>
        </configuration>
        <executions>
          <execution>
            <id>search execution</id>
            <phase>compile</phase>
            <goals>
              <goal>search</goal>
            </goals>
          </execution>
          <execution>
            <id>mutation exectution</id>
            <phase>test</phase>
            <goals>
              <goal>mutation</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
```
You should add, on the configuration:
+ the project directory,
+ Your project's jar location
+ the number of change you would like.

### 2- Launch Tests !
Use the command:
```
mvn clean test
```
and hope for the best!

### 3- Check reports
DiversiType plugin generate two report in the directory {your_project_directory}/target/diversitype

**3.1- diversitype.txt**

This report indicate the mutations made: (ex: for the mutation, List l=new ArrayList() --> List l=new LinkedList())
+ The analysed interface (ex: java.util.List)
+ The mutation point ( ex:{path_to_the_mutation_point}: new ArrayList())
+ The concrete type that was changed ( ex: java.util.ArrayList)
+ The concrete type's replacement (ex: java.util.LinkedList)

It indicate the test results too:
+ The failed tests before the project's mutation
+ The failed tests added by the mutation
+ The failed tests in common between project before and after the mutation
+ The failed tests resolved by the mutation

**3.2- listStatistic.txt**

This report is the result of the source code's analyse.
It indicates:
+ Where the interface chose for the mutation is used
+ Which concrete type instantiate it


## Version

### Version 1.0

1. This version don't allow the choose of the "mutation's strategy" and 
the "selection's strategy" that selects the mutation's point.

The parameter of the "selection's strategy" is "internal". Which means 
that the plugin will analyse your project's source code
and record your classes' hierarchy.
So, the mutation will be done only on your project's interfaces.

The parameter of "mutation's strategy" is "random". The plugin will find
 all of the possibles "constructor call" for the mutation and takes one randomly.

2. The nchange is fixed to 1.

So, for the moment, the number of mutations for the test's phase is limited to 1 change.

### Version 1.1

1. Improve of the algorithm of interface's selection

This version learns which interface or constructor call have no possibility of mutation.
It records the project's hierarchy too.
Those information are record in files store into the directory target/diversiType_learning. 

If your project's source code change, you can delete this directory.
But, in this case, all the learning it made will be deleted and the plugin will restart to the beginning.
If you think that your project's hierarchy have no change, don't delete the directory.

### Version 1.2

The version generate a HTML report into the directory target/diversiType_report.
This web page represent a graph of the project's hierarchy.

### Version 2
This version allow to change the number of change. You also can choose all mutation's point when you give "nchange=-1".
The mode "external" for the "selection's strategy" is now possible but you have to specify an interface.

## In the future...

+ Add different strategies ("internal" or "external" - "random" or "chosen")
+ Give the possibility to do more changes (>1)
+ Improve the algorithm of interface's selection
+ Choose precisely the interface to mutate
+ Become less dependant to maven project structure
+ Improve the result analyse (why the test fail with the mutation)
+ Add the plugin to maven central














