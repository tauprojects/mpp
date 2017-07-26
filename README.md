##  Evaluating Concurrent Java Data Structures 
#### Advanced Topics in Multicore Architecture and Software Systems

###### Almog Zeltsman, Matan Gizunterman
_____
## Description
Java mavan project with JMH framework
 
## Guidelines
##### Dependencies:
* Java 7 (or higher)
* git 
* mavan
#
##### Build Project:
#
    git clone https://github.com/tauprojects/mpp.git
    mvn clean
    mvn compile
    mvn package


##### Run Benchmark:
# 
Run the java *microbenchmarks.jar* package according to JMH documentation 
http://java-performance.info/jmh/
http://psy-lob-saw.blogspot.co.il/2013/04/writing-java-micro-benchmarks-with-jmh.html
###### 
    java -jar target/microbenchmarks.jar -wi 10 -i 20 -f 1 -t 2 -tu s ".*SimpleBenchmark.*"

##### Login to server:
#
    open linux
    ssh [username]@nova.cs.tau.ac.il
	login
    ssh matanalmog@rack-mad-02.tau.ac.il
    login    
    
    
Based on: https://github.com/nitsanw/jmh-samples

MD File Edited On: http://dillinger.io/

