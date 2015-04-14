# Input file format
```
# num nodes
7

# hyperedges (source set -> target)
1,2,3 -> 4
4,5 -> 6
7 -> 3
7 -> 5

# startable set
1,2,3,7

# targets (optional)
6
```

# JSON format
```
{
  "numNodes": 7,
  "hyperedges": [
     { "source": [1,2,3], "target": 4 },
     { "source": [1,2,3], "target": 4 },
  ],
  "startables": [1,2,3,7],
  "targets": [6]
}
```
