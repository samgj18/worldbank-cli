#!/bin/bash

echo "I'm compiling, please be patient..."

sleep 3
echo "If by any mean I fail, run this file with '-force' flag"

sbt compile
sbt pack
sbt packInstall

echo "I finished, please go to your terminal and write ./run.sh"


if [ $1 = "-force" ]
then
    echo "Please bare with me"
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    brew install sbt
    sbt compile
    sbt pack
    sbt packInstall
    echo "Hey seems like everything worked."
fi