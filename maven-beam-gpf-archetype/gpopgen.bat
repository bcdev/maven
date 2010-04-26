rem
rem Usage: gpopgen.bat <groupId> <artifactId>
rem
mvn archetype:generate  ^
 -DarchetypeGroupId=org.esa.beam.maven ^
 -DarchetypeArtifactId=maven-beam-gpf-archetype ^
 -DarchetypeVersion=4.8-SNAPSHOT ^
 -DarchetypeRepository=http://www.brockmann-consult.de/mvn/os ^
 -DgroupId=%1 ^
 -DartifactId=%2 ^
 -Dversion=1.0-SNAPSHOT ^
 -DpackageName=%1 ^
 -DinteractiveMode=false