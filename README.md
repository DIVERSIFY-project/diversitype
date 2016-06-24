

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
+ The replacement of the concrete type (ex: java.util.LinkedList)

It indicate the test results too:
+ The failed tests before the project's mutation
+ Failed tests added by mutation
+ Failed tests in common between project before and after the mutation.
+ Failed tests resolved by the mutation

**3.2- listStatistic.txt**

This report is the result of the source code analyse.
It indicate:
+ Where the interface choose for the mutation is used
+ Which concrete type instantiate it


## Version

### Version 1.0

1. This version impose mutation strategy and selection strategy for the mutation point.

The selection strategy is "internal". Which means that the plugin will analyse the source code of your project
and record the hierarchy of your classes.
So, the mutation will be on your interfaces.

The mutation strategy is "random". The plugin find all possible constructor calls for the mutation and take one randomly.

2. The nchange is fix to 1

For the moment, the number of mutation for test phase is limited to 1 change.

### Version 1.1

1. Improve the interface selection algorithm

This version learn which interface or constructor call have not possibility of mutation.
It record the project's hierarchy too.
This information are recording in files target/diversiType_learning. 

If your project's source code change, you can delete this directory.
But, in this case, all the learning is delete and the plugin restart to the beginning.
If you think that your project's hierarchy have no change, don't delete the directory.

### Version 1.2

The version generate a HTML report into target/diversiType_report.

## In the future...

+ Add different strategies (internal or external - random or chosen)
+ Give the possibility to do more change ( >1)
+ Improve the interface selection algorithm
+ Choose precisely the interface to mutate
+ Become less dependant to maven project structure
+ Improve the result analyse (why the test fail with the mutation)
+ Add the plugin to maven central














