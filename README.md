# diversitype
Randomly diversifies types of polymorphic variables in application code when running a test suite

##Command line usage
The following command line replaces 3 constructor calls by equivalent ones according to the type system and according to the test suite. The resulting application is provided in output-folder. 

`java -cp $CLASSPATH diversitype.Main --source-folder src/main/java --test-folder src/test/java --output-folder DIRECTORY --replacement-file foo.txt --number-replacement 3`

Example of foo.txt
```
toto.java:342 new LinkedList replaced  by new ArrayList
testCaseToto1 and testCaseToto2 pass
```
