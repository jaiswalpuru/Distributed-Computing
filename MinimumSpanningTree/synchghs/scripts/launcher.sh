#!/bin/bash

# Change this to your netid
netid=pxj200018

# Root directory of your project
PROJDIR=ghs/server/Node

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/Desktop/code/UTD\ Course\ Work/Distributed\ Systems\ CS\ 6380/Projects/MinimumSpanningTree/pxj200018/ghs/launch/config.txt

# Directory your java classes are in
# BINDIR=$PROJDIR/bin/pxj200018

# Your main project class
# PROG=Node

n=0

# shellcheck disable=SC2002
cat "$CONFIGLOCAL" | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    # shellcheck disable=SC2162
    read i
    echo "$i"
    while [[ $n -lt $i ]]
    do
    	# shellcheck disable=SC2162
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo "$line" | awk '{ print $2 }' )
	
    osascript -e '
                tell app "Terminal"
                    do script "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no '$netid@$host'  'java' '$PROJDIR';"
                end tell'

        n=$(( n + 1 ))
    done
)
