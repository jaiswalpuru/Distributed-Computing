***************************** README *********************************

Name: Puru Jaiswal &  Utsav Adhikari & Gregory Hinkson
UTD NETID: PXJ200018 & UXA200002 & GKH210000 
Assignment: CS6380 Project 2

Link to the demo video : https://cometmail-my.sharepoint.com/:v:/g/personal/pxj200018_utdallas_edu/EanxrXPBpVpJuXRQ_HeoOr8BLOndyDPqFxtL-QfXTHAZqA?e=GxxhCf

Files:
	1) MinimumSpanningTree/synchghs/ghs/algorithm/SynchGHS.java - the main program which executes SynchGHS algorithm for Minimum Spanning Tree construction 
	2) MinimumSpanningTree/synchghs/ghs/Config/Config.java - Auxillary class for parsing the config file
	3) MinimumSpanningTree/synchghs/ghs/launch/config.txt - the config file containing node hostnames, uids, ports and edge weights
    4) MinimumSpanningTree/synchghs/ghs/Message/Edge.java - class definition of edge 
    5) MinimumSpanningTree/synchghs/ghs/Message/EdgeType.java - definition of enum Edge types
    6) MinimumSpanningTree/synchghs/ghs/Message/GHSMessage.java - class to support sending and processing of messages between nodes
    7) MinimumSpanningTree/synchghs/ghs/Message/MessageType.java - definition of enum Message types 
    8) MinimumSpanningTree/synchghs/ghs/Message/NodeType.java - definition of enum Node types 
    9) MinimumSpanningTree/synchghs/ghs/scripts/cleanup.sh - kills  processes initiated on dc machines from previous execution for user
    10) MinimumSpanningTree/synchghs/ghs/scripts/launcher.sh - launcher script to automate execution of SynchGHS
    11) README.txt - contains file definitions and how to execute program

How to run:
    1) Manually
        * open N terminals (one on each Node)
        * For each terminal, SSH into one of the machines outlined in the configfile.txt
        * in one of the N terminals execute:
            > make
        * in all N terminals, in any order:
            > cd to MinimumSpanningTree directory
            > javac synchghs/ghs/Node.java 
            > java synchghs/ghs/Node.java <uid>

    2) Automatically
        * open a terminal and execute:
            > scp * <UTD_ID>@dcXX.utdallas.edu:<PATH_TO_PROJECT_FILES_ON_dcXX_MACHINE/MinimumSpanningTree>
            - in order to copy all files over to dcXX machine where XX is valid machine
        * SSH into one of the machines and naviate to <PATH_TO_PROJECT_FILES_ON_dcXX_MACHINE/MinimumSpanningTree>, then:
            > cd to MinimumSpanningTree directory
            > javac synchghs/ghs/Node.java 
            > java synchghs/ghs/Node.java <uid>
        * Open launcher.sh and edit as follows:
            - edit line 4 "netid=" to be valid netid
            - edit linen 11 "CONFIGLOCAL=" to be path of config file on local machine
            

Notes:
    1) To run automatically, passwordless login must be setup for dcXX machines
	
