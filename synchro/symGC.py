import subprocess
import re
import csv
from time import strftime, time, localtime
import os
#env
const = ['env', 'LD_PRELOAD=libjemalloc.so', 'numactl', '--interleave=all']
const += ['java', '-cp', 'bin', 'contention.benchmark.Test', '-f', '1', '-d', '5000', '-s', '0', '-a', '0', '-W', '0','-gc' ,'true']
match_number = re.compile('-?\ *[0-9]+\.?[0-9]*(?:[Ee]\ *-?\ *[0-9]+)?')

def runSync(u,i,b,t):

    args = const + ['-u',str(u),'-i',str(i),'-r',str(i*2),'-b',str(b),'-t',str(t)]
    print("Running:\n%s" % " ".join(args))
    out = [str(line) for line in subprocess.check_output(args).splitlines()]

    foundline = ""
    for line in out:
        if "Throughput" in line:
            foundline = line
            break

    assert(foundline!="")


    return float(re.findall(match_number,foundline)[0])



t_arg = [1,2,4,8,16,32]
i_arg = [#16384,
         32768]#,
         #65536]

binaryTrees = ["trees.lockfree.NonBlockingTorontoBSTMap",
               "trees.lockbased.LockBasedFriendlyTreeMap",
               "trees.lockbased.LogicalOrderingAVL",
               "trees.lockbased.LockBasedStanfordTreeMap"]

hashTables = ["hashtables.lockfree.NonBlockingFriendlyHashMap",
              "hashtables.lockfree.NonBlockingCliffHashMap",
              #"hashtables.lockfree.JavaHashIntSet",
              "hashtables.lockbased.LockBasedJavaHashMap",
              #"hashtables.transactional.TransactionalBasicHashSet"
              ]

linkedLists = [#"linkedlists.lockfree.NonBlockingLinkedListSetRTTI",
               "linkedlists.lockbased.LockCouplingListIntSet",
               #"linkedlists.lockbased.LazyLinkedListSortedSet",
               "linkedlists.transactional.ElasticLinkedListIntSet",
               "linkedlists.transactional.ReusableLinkedListIntSet"]

skipLists = ["skiplists.lockfree.NonBlockingFriendlySkipListMap",
             "skiplists.lockfree.NonBlockingJavaSkipListMap"]

structTypes = [binaryTrees,hashTables,linkedLists,skipLists]
structTypesNames = ["Binary Trees", "Hash Tables", "Linked Lists", "Skip Lists"]

upd_arg = [#0,
           50]
name = "RunResults " + strftime("%Y.%m.%d %H-%M", localtime(time())) + ".csv"
if os.name == 'nt':
    path = "C:/mpp/data/mpp/synchro/WindowsResults/" + name
else:
    path = "/specific/a/home/cc/students/cs/zeltsman/mpp/synchro/results/" + name
index = 0
f = open(path, 'w+', newline='')
writer = csv.writer(f)
writer.writerow(["ID","Structure Type","Update Ratio","Initial Size","Class Name", "Threads",\
                 "Throughput1","Throughput2","Throughput3","Throughput4"])

print("Synchrobench Simulator")
print("Writing to file:",name)
print("")

for structType in enumerate(structTypes):
    for u in upd_arg:
        for i in i_arg:
            for b in structType[1]:
                for t in t_arg:
                    results = []
                    print(str(index)+". results: ",end="")
                    for rep in range(5):
                        res = str(runSync(u,i,b,t))
                        results.append(res)
                        print(res,end=", ")
                    print("")
                    row = [index,structTypesNames[structType[0]],str(u)+"%",str(i),str(b),str(t)] + results
                    #print(str(index)+"%\t",row)
                    writer.writerow(row)
                    index += 1
f.close()
print("\nFINISHED SUCCESSFULLY!")

