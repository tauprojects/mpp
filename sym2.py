import subprocess
import re
import csv
from time import strftime, time, localtime
import os

const = ['java', '-cp', 'bin', 'contention.benchmark.Test', '-f', '1', '-d', '5000', '-s', '0', '-a', '0', '-W', '0']
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
i_arg = [16384, 32768, 65536]

binaryTrees = ["trees.lockfree.NonBlockingTorontoBSTMap",
               "trees.lockbased.LockBasedFriendlyTreeMap",
               "trees.lockbased.LogicalOrderingAVL"]

hashTables = ["hashtables.lockfree.NonBlockingFriendlyHashMap",
              "hashtables.lockfree.NonBlockingCliffHashMap",
              #"hashtables.lockfree.JavaHashIntSet",
              "hashtables.lockbased.LockBasedJavaHashMap",
              "hashtables.transactional.TransactionalBasicHashSet"]

linkedLists = ["linkedlists.lockfree.NonBlockingLinkedListSetRTTI",
               "linkedlists.lockbased.LockCouplingListInSet",
               "linkedlists.lockbased.LazyLinkedListSortedSet",
               "linkedlists.transactional.ElasticLinkedListIntSet",
               "linkedlists.transactional.ReusableLinkedListIntSet"]

skipLists = ["skiplists.lockfree.NonBlockingFriendlySkipListMap",
             "skiplists.lockfree.NonBlockingJavaSkipListMap"]

structTypes = [binaryTrees,hashTables,linkedLists,skipLists]
structTypesNames = ["Binary Trees", "Hash Tables", "Linked Lists", "Skip Lists"]

upd_arg = [0,50]
name = "RunResults " + strftime("%Y.%m.%d %H-%M", localtime(time())) + ".csv"
path = "/home/matanalmog/synchrobench/Results/" + name
index = 0

writer = csv.writer(open(path, 'w+', newline=''))
writer.writerow(["Structure Type","Update Ratio","Initial Size","Class Name", "Threads","Throughput"])

print("Synchrobench Simulator")
print("Writing to file:",name)
print("")


for u in upd_arg:
    for i in i_arg:
        b = "trees.lockbased.LockBasedStanfordTreeMap"
        for t in t_arg:
            result = runSync(u,i,b,t)
            row = ["Binary Trees",str(u)+"%",str(i),str(b),str(t),str(result)]
            print(str(round((index/467)*100,1))+"%\t",row)
            writer.writerow(row)
            index += 1

print("\nFINISHED SUCCESSFULLY!")

