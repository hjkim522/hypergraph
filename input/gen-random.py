#simple hypergraph generator

import sys
import random
import array

#parameters
outputFile = "output.txt"
numNodes = 100
numHyperedges = 100
numStartable = 10
sourceSetSizeMax = 3
avgPathLen = 5

#not used
avgIndegree = 3
avgOutdegree = 3

class Hyperedge:
    sourceSet = set()
    targetNode = None

def parseParam():
    global outputFile
    global numNodes
    global numHyperedges
    global numStartable

    #parse command ling args
    if len(sys.argv) > 1:
        numNodes = int(sys.argv[1])
        numHyperedges = numNodes
        numStartable = numNodes / 10
    if len(sys.argv) > 2:
        numHyperedges = int(sys.argv[2])
    if len(sys.argv) > 3:
        numStartable = int(sys.argv[3])
    if len(sys.argv) > 4:
        outputFile = sys.argv[4]

    print("numNodes: " + str(numNodes))
    print("numHyperedges: " + str(numHyperedges))
    print("numStartable: " + str(numStartable))
    print("output: " + outputFile)


def generate():
    global outputFile
    global numNodes
    global numHyperedges
    global numStartable
    global sourceSetSizeMax
    global avgPathLen #XXX: use range

    #degree count
    indegree = [0] * numNodes
    outdegree = [0] * numNodes

    #write output file
    f = open(outputFile, "w")
    f.write(str(numNodes) + "\n")

    #generate path segments
    for i in range(numHyperedges / avgPathLen):
        sourceSet = set()
        targetNode = None
        
        for j in range(avgPathLen):
            #generate source set
            sourceSetSize = random.randint(1, sourceSetSizeMax)
            while len(sourceSet) < sourceSetSize:            
                s = random.randint(0, numNodes-1)
                sourceSet.add(s)
                outdegree[s] = outdegree[s] + 1

            #generate target node
            targetNode = random.randint(0, numNodes-1)
            indegree[targetNode] = indegree[targetNode] + 1

            #write edge
            for s in sourceSet:
                f.write(str(s) + ",")
            f.write(" -> " + str(targetNode) + "\n")

            #connect path
            sourceSet = set()
            sourceSet.add(targetNode)

    #mark startables
    startable = 0
    f.seek(0, 0)
    for i in range(numNodes):
        if indegree[i] == 0:
            f.write(str(i) + ",")
            startable = startable + 1
    f.write("\n")

    #close file
    f.close()
    print("DONE")

    #print graph statistic
    print("startables: " + str(startable))
    print("indegree: " + str(reduce(lambda x, y: x+ y, indegree) / float(numNodes)))
    print("outdegree: " + str(reduce(lambda x, y: x+ y, outdegree) / float(numNodes)))

def main():
    parseParam()
    generate()

  
main()
