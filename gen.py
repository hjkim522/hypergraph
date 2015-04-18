#simple hypergraph generator

import random

def main():
    print("hypergraph generator")

    #parameters
    outputFile = "output.txt"
    numNodes = 20
    numStartable = 10

    #write number of nodes
    f = open(outputFile, "w")
    f.write(str(numNodes) + "\n")

    #startable
    startable = set()
    while len(startable) < numStartable:
        startable.add(random.randint(0, numNodes))

    #write startable
    for s in startable:
        f.write(str(s) + ",")
    f.write("\n")

    f.close()

main()
