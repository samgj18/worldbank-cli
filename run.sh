#!/bin/bash


if [ $1 = "--dataload" ]
then
    echo "Make sure you compiled first, otherwise this task may fail."
    export PATH=$PATH:~/local/bin
    cd $(pwd)/target/pack/bin && chmod a+x worldbank && ./worldbank run -d
    echo "Done ingestion of data..."
elif [ $1 = "--results" ]
then
    echo "Make sure you runned the command ./run.sh --dataload first, otherwise this task may fail."
    cd $(pwd)/target/pack/bin && chmod a+x worldbank && ./worldbank run -r
else
    echo "Bear with me, I'm doing both ingestions and results :)."
    cd $(pwd)/target/pack/bin && chmod a+x worldbank && ./worldbank run
fi
