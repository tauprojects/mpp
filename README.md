##  Evaluating Concurrent Java Data Structures 
#### Advanced Topics in Multicore Architecture and Software Systems

###### Almog Zeltsman, Matan Gizunterman
_____
## Description
Java mavan project with JMH framework
 
## Guidelines
##### Dependencies:
* **Java 7** *(synchrobench doesn't fully support Java8)*
* **git** 
* **maven**
#
##### Build Project:
#
    git clone https://github.com/tauprojects/mpp.git
    mvn clean
    mvn package


#### Running Our Benchmark Main:
# 
This command is suppose to iterate and run through all the available benchmark configurations
as were run on the Synchrobench article.
###### 
    java -cp target/microbenchmarks.jar psy.lob.saw.own.RunBenchmarks
	
	Optionaly add before line: env LD_PRELOAD=libjemalloc.so numactl --interleave=all

#### Our Benchmark main arguments:
# 
##### Running one specific configuration:
Running all of those arguments together [-t, -u, -i, -b] will run only one specific configuration. 
This configuration option can support _every_ possible value to the [-u], [-t], [-i] arguments.
##### Iterating through multiple configurations:
If **at least one** of the arguments is **not** given then the program will iterate through **all** the available values of the rest of the arguments.
The arguments and possible values are as followed (*constraints are given for running only one specific configuration as described above*):
###### 
    -t 		Number of threads to run. [Possible values: 1,2,4,8,16,32]
			General constraints: t>0
	-u 		Update ratio. Percent of operations to be add/remove. Others will be contains.
			from this percent, half will be add and half remove. [Possible values: 0,50]
			General constraints: 0<=u<=100
	-i 		Initial size. Structure will be first filled with this number of objects. 
			[Possible values: 16384, 32768, 65536]
			General constraints: i>0
	-b		Class to run benchmark on. Possible values for now (write without brackets)
				trees.lockfree.NonBlockingTorontoBSTMap
				trees.lockbased.LockBasedFriendlyTreeMap
				trees.lockbased.LogicalOrderingAVL
				trees.lockbased.LockBasedStanfordTreeMap
                
				hashmaps.lockfree.NonBlockingFriendlyHashMap
				hashmaps.lockfree.NonBlockingCliffHashMap
				hashmaps.lockbased.LockBasedJavaHashMaפ
				hashtables.transactional.TransactionalBasicHashSet
                
				linkedlists.lockbased.LockCouplingListIntSet
				linkedlists.lockbased.LazyLinkedListSortedSet
				linkedlists.transactional.ElasticLinkedListIntSet
				linkedlists.transactional.ReusableLinkedListIntSe
                
				skiplists.lockfree.NonBlockingFriendlySkipListMap
				skiplists.lockfree.NonBlockingJavaSkipListMap
	-gc		will force GC between iterations in JMH. 
			[Possible values: true/false, default value: true]
	
Private non JMH/Synchrobench arguments:
	
	-s 		Type of structure to run. [Possible values: binaryTrees,hashTables,linkedLists,skipLists]
	-r 		number of repetitions of each benchmark [default value: 5]
	-index		index of benchmark from total to start from (mainly for debugging purposes)

	
Based on: https://github.com/nitsanw/jmh-samples

Synchrobench forked repository: https://github.com/tauprojects/synchrobench

MD File Edited On: http://dillinger.io/

