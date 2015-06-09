#simple hypergraph generator

import sys
import random
import array
import shutil

#parameters
outputFile = "input/hypergraph.txt"
numNodes = 100
numHyperedges = 100
numStartable = 20
sourceSetSizeMin = 1
sourceSetSizeMax = 3
avgPathLen = 1
pickStrictStartable = False

#not used
avgIndegree = 3
avgOutdegree = 3

class Hyperedge:
    def __init__(self):
        self.sourceSet = set()
        self.targetNode = None

    def toString(self):
        result = ""
        for s in self.sourceSet:
            result = result + str(s) + ","
        result = result + " -> " + str(self.targetNode)
        return result

def parseParam():
    global outputFile
    global numNodes
    global numHyperedges
    global numStartable
    global avgPathLen

    #parse command ling args
    if len(sys.argv) > 1:
        numNodes = int(sys.argv[1])
        numHyperedges = numNodes #* 2
        numStartable = numNodes / 5
    if len(sys.argv) > 2:
        numHyperedges = int(sys.argv[2])
    if len(sys.argv) > 3:
        numStartable = int(sys.argv[3])
    if len(sys.argv) > 4:
        outputFile = sys.argv[4]
    if len(sys.argv) > 5:
        avgPathLen = sys.argv[5]

    print("numNodes: " + str(numNodes))
    print("numHyperedges: " + str(numHyperedges))
    print("numStartable: " + str(numStartable))
    print("avgPathLen: " + str(avgPathLen))
    print("output: " + outputFile)


def generate():
    global outputFile
    global numNodes
    global numHyperedges
    global numStartable
    global sourceSetSizeMin
    global sourceSetSizeMax
    global avgPathLen
    global pickStrictStartable

    #degree count
    indegree = [0] * numNodes
    outdegree = [0] * numNodes

    #hyperedges
    hyperedges = []

    #generate path segments
    for i in range(numHyperedges / avgPathLen):
        h = Hyperedge()
        
        for j in range(avgPathLen):
            #generate source set
            sourceSetSize = random.randint(sourceSetSizeMin, sourceSetSizeMax)
            while len(h.sourceSet) < sourceSetSize:            
                s = random.randint(0, numNodes-1)
                h.sourceSet.add(s)
                outdegree[s] = outdegree[s] + 1

            #generate target node
            t = random.randint(0, numNodes-1)
            h.targetNode = t
            indegree[t] = indegree[t] + 1

            hyperedges.append(h)

            #connnect to the next path
            if j != avgPathLen - 1:
                h = Hyperedge()
                h.sourceSet.add(t)
                outdegree[t] = outdegree[t] + 1

    #pick startables
    startables = set()
    if pickStrictStartable:
        for i in range(numNodes):
            if indegree[i] == 0:
                startables.add(i)
    while len(startables) < numStartable:
        startables.add(random.randint(0, numNodes-1))

    #write output file
    f = open(outputFile, "w")
    f.write(str(numNodes) + "\n")

    #write startables
    for i in startables:
        f.write(str(i) + ",")
    f.write("\n")

    #write hyperedges
    for h in hyperedges:
        f.write(h.toString() + "\n")

    #close file
    f.close()
    print("DONE")

    #print graph statistic
    print("startables: " + str(len(startables)))
    print("indegree: " + str(reduce(lambda x, y: x+ y, indegree) / float(numNodes)))
    print("outdegree: " + str(reduce(lambda x, y: x+ y, outdegree) / float(numNodes)))

    #copy input file for experiment
    shutil.copyfile(outputFile, "input/hypergraph-" + str(numNodes) + ".txt")

def main():
    parseParam()
    generate()
  
main()
