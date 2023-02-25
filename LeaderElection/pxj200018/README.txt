Author : Puru Jaiswal 
NETID : PXJ200018

Compile : 
$ javac pxj200018/server/Node.java

After this is compiled then scripts can be used to run the program.
$ cd scripts
$ ./launcher.sh

If want to run the program on remote system then create a directory structure as below with ".class" files.

README.txt	algorithm	config		launch		message		server

./algorithm:
Peleg.class	SynchBFS.class

./config:
Config.class

./launch:
config.txt

./message:
BFSMessage.class MessageType.class PelegMessage.class

./server:
ClientHandlerPeleg.class 	ClientHandlerSynchBFS.class	Node.class

Now if one wants to change the graph topology then replace the config.txt with the same format as provided in the project desscription,
if the format is provided as given the project, the execution might have problems.

The launcher.sh file if programmed to open terminals in MACOS operating system.
