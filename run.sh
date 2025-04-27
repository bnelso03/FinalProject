#!/bin/bash

pkill java
sleep 1

echo "Compiling source files..."
(cd src && rm *.class && javac *.java)

echo "Starting Utility Servers..."
(cd src && java Utility 32006 merge) & UTIL1_PID=$!
(cd src && java Utility 32007 merge) & UTIL2_PID=$!
(cd src && java Utility 32008 merge) & UTIL3_PID=$!

sleep 2

echo "Starting Master Server..."
(cd src && java Master 2000) & MASTER_PID=$!

sleep 2

echo "Starting Client..."
(cd src && java Client ../res/testData.txt merge)

echo "Client done."

kill $MASTER_PID
kill $UTIL1_PID
kill $UTIL2_PID
kill $UTIL3_PID

echo "All servers MURDERED."
