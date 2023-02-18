#!/bin/bash


# Change this to your netid
netid=sxg122830

#
# Root directory of your project
PROJDIR=$HOME/TestProj

#
# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

n=0

# shellcheck disable=SC2002
cat "$CONFIGLOCAL" | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    # shellcheck disable=SC2162
    read i
    echo "$i"
    while [[ $n -lt $i ]]
    do
    	read line
        # shellcheck disable=SC2086
        host=$( echo $line | awk '{ print $2 }' )

        echo "$host"
        gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host killall -u $netid" &
        sleep 1

        n=$(( n + 1 ))
    done
   
)


echo "Cleanup complete"
