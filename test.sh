#!/bin/bash

### Settings

NUMBER=79604984718
PIN=1234
TIMEOUT=20

### /Settings

NORMAL=$(tput sgr0)
GREEN=$(tput setaf 2; tput bold)
YELLOW=$(tput setaf 3)
RED=$(tput setaf 1)

function red() {
    echo -e "$RED$*$NORMAL"
}

function green() {
    echo -e "$GREEN$*$NORMAL"
}

function yellow() {
    echo -e "$YELLOW$*$NORMAL"
}

# Exclude: wipe, wipesd, fullwipe, brick, softbrick
CMDS[1]="status"
CMDS[2]="info"
CMDS[3]="locate"
CMDS[4]="camera front"
CMDS[5]="camera back"
CMDS[6]="screenshot"
CMDS[7]="dial $NUMBER"
#CMDS[8]="sms $NUMBER hi there"
CMDS[9]="alarm"
CMDS[10]="audio 10"
CMDS[11]="lock $PIN"
CMDS[12]="lock off"
CMDS[13]="hide on"
CMDS[14]="hide off"
CMDS[15]="cmd uname -a"
CMDS[16]="dumpsys"
CMDS[17]="lost 1234"
CMDS[18]="lost off"

TMP=/tmp/antrack_test
rm -rf $TMP
mkdir $TMP

for cmd in "${CMDS[@]}"; do
    cp -r * $TMP

    green "COMMAND: $cmd"
    echo $cmd > ./control

    sleep $TIMEOUT
    
    green -n "RESULT: "
    cat ./result

    green "DIFF: "
    diff -r $TMP .
   
    green "Press ENTER..."
    read
done
