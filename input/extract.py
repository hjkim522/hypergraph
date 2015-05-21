import sys

def extract():
    inputFileName = sys.argv[1]
    outputFileName = sys.argv[2]
    originDB = "[KEGG]"

    inputFile = open(inputFileName, "r")
    outputFile = open(outputFileName, "w")

    while True:
        line = inputFile.readline()
        if not line: break
        if originDB not in line: continue
        outputFile.write(line)
    
    print("DONE")

extract()
