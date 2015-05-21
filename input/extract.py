import sys

def extract():
    inputFileName = sys.argv[1]
    outputFileName = sys.argv[2]
    originDB = "[KEGG]"

    inputFile = open(inputFile, "r")
    outputFile = open(outputFile, "w")

    while True:
        line = inputFile.readline():
        if not line: break
        if not line.contains(originDB): continue
        outputFile.write(line)
    
    print("DONE")

extract()
