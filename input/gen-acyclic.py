#simple hypergraph generator

import sys
import random
import array

#parameters
outputFile = "input/hypergraph.txt"
numNodes = 100
numHyperedges = 200
numStartable = 20
sourceSetSizeMin = 2
sourceSetSizeMax = 3
avgPathLen = 5

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

    #parse command ling args
    if len(sys.argv) > 1:
        numNodes = int(sys.argv[1])
        numHyperedges = numNodes * 2
        numStartable = numNodes / 5
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
    global sourceSetSizeMin
    global sourceSetSizeMax
    global avgPathLen

    #degree count
    indegree = [0] * numNodes
    outdegree = [0] * numNodes

    #hyperedges
    hyperedges = []

    #generate path segments
    avgPathLen = 1 #XXX: not used for acyclic hypergraph
    for i in range(numHyperedges / avgPathLen):
        h = Hyperedge()
        
        for j in range(avgPathLen):
            #generate source set
            sourceSetSize = random.randint(sourceSetSizeMin, sourceSetSizeMax)
            while len(h.sourceSet) < sourceSetSize:            
                s = random.randint(0, numNodes - 2)
                h.sourceSet.add(s)
                outdegree[s] = outdegree[s] + 1

            #generate target node
            #assuming nodes are topologically sorted from 0 to n-1
            #do not allow edges from big number to smaller number
            t = random.randint(max(h.sourceSet)+1, numNodes-1)
            h.targetNode = t
            indegree[t] = indegree[t] + 1

            hyperedges.append(h)

            #continue path
            h = Hyperedge()
            if t < numNodes-1:
                h.sourceSet.add(t)
                outdegree[t] = outdegree[t] + 1

    #pick startables
    startables = set()
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

def main():
    parseParam()
    generate()

  
main()
