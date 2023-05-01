### Pelegs Algorithm for leader election and Synch BFS

```This project consists of three parts: 
(a) build a message-passing synchronous distributed system in which nodes are arranged in a certain topology (given in a configuration file)

(b) implement Peleg’s leader election algorithm to select a distinguished node, and 

(c) build a breadth first spanning (BFS) tree rooted at the distinguished node.

You can assume that all links are bidirectional. You will need to use a synchronizer to simulate a synchronous system. Details of a simple synchronizer will be discussed in the class.

Output: Each node should print the following information to the screen when appropriate: (i) UIDs of its parent and children nodes in the BFS tree, and (ii) its degree in the BFS tree.

Listing 1: Example configuration file
# number of nodes in the system
5
# nodeUID hostName listeningPort
123   dc02  2234
5     dc03  3233
23    dc04  5217
1047  dc05  2432
89    dc06  6221
# space delimited list of neighbors for each node
5     23
123   1047
123   1047     89
5 23 89 
23 1047
```
