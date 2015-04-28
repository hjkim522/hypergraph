#simple hypergraph generator

import sys
import random
import array

def main():
    #parameters
    outputFile = "output.txt"
    numNodes = 10000
    numStartable = 10
    numHyperedges = 10000

    #other parameters
    sourceSetSizeMax = 3
    avgIndegree = 3
    avgOutdegree = 3
    avgPathLen = 10

    #count occuerence
    indegree = [0] * numNodes

    #parse command ling args
    if len(sys.argv) > 1:
    	numNodes = int(sys.argv[1])
    	numStartable = numNodes / 10
    	numHyperedges = numNodes
    if len(sys.argv) > 2:
    	numStartable = int(sys.argv[2])
    if len(sys.argv) > 3:
    	numHyperedges = int(sys.argv[3])
    if len(sys.argv) > 4:
    	outputFile = sys.argv[4]

    print("numNodes: " + str(numNodes))
    print("numStartable: " + str(numStartable))
    print("numHyperedges: " + str(numHyperedges))

    #write number of nodes
    f = open(outputFile, "w")
    f.write(str(numNodes) + "\n")

    #write startable
    startable = range(numStartable)
    for s in startable:
        f.write(str(s) + ",")
    f.write("\n")

    #generate hyperedge by hops
    sourcePool = set(startable)
    for i in range(numHyperedges):
        sourceSetSize = random.randint(1, sourceSetSizeMax)
        sourceSet = set()
        while len(sourceSet) < sourceSetSize:
            #idx = random.randint(0, len(sourcePool)-1)
            #sourceList = list(sourcePool)
            #sourceSet.add(sourceList[idx])
            s = random.randint(0, numNodes-1)
            sourceSet.add(s)
        targetNode = random.randint(0, numNodes-1)
        #sourcePool.add(targetNode)
        indegree[targetNode] = indegree[targetNode] + 1

        #write edge
        for s in sourceSet:
            f.write(str(s) + ",")
        f.write(" -> " + str(targetNode) + "\n")

    #write startables
    count = 0
    for i in range(numNodes):
        if indegree[i] == 0:
            f.write(str(i) + ",")
            count = count + 1
    f.write("\n")
    print(str(count) + " startables\n")

    #close file
    f.close()
    print("DONE > " + outputFile)
    
main()