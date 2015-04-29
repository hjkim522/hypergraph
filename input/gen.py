#simple hypergraph generator

import sys
import random
import array

#parameters
outputFile = "hypergraph.txt"
numNodes = 20
numHyperedges = 40
numStartable = 5 #not used
sourceSetSizeMax = 3
avgPathLen = 5

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

    #parse command ling args
    if len(sys.argv) > 1:
        numNodes = int(sys.argv[1])
        numHyperedges = numNodes * 2
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

    #hyperedges
    hyperedges = []

    #generate path segments
    for i in range(numHyperedges / avgPathLen):
        h = Hyperedge()
        
        for j in range(avgPathLen):
            #generate source set
            sourceSetSize = random.randint(1, sourceSetSizeMax)
            while len(h.sourceSet) < sourceSetSize:            
                s = random.randint(0, numNodes-1)
                h.sourceSet.add(s)
                outdegree[s] = outdegree[s] + 1

            #generate target node
            t = random.randint(0, numNodes-1)
            h.targetNode = t
            indegree[t] = indegree[t] + 1

            hyperedges.append(h)

            #continue path
            h = Hyperedge()
            h.sourceSet.add(t)
            outdegree[t] = outdegree[t] + 1

    #write output file
    f = open(outputFile, "w")
    f.write(str(numNodes) + "\n")

    #write startables
    startable = 0
    for i in range(numNodes):
        if indegree[i] == 0:
            f.write(str(i) + ",")
            startable = startable + 1
    f.write("\n")

    #write hyperedges
    for h in hyperedges:
        f.write(h.toString() + "\n")

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
