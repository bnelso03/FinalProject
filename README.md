# README

Not sure if it's already executable, but just in case; when you pull this make sure that you run 
    sudo chmod +x run.sh 
in the FinalProject dir just to be able to run it.

Running the script is a pretty easy way to make incremental changes and see if what you're doing works. Right now though, it's not the best way to 
see the concurrency in action. If you wanna see it work as intended, I'd make about 6 different terminals.

Two for Client
One for Master
Three for Utility

NOTE: 

First, generate the testData.txt file. The generator is in misc, and it generates to res/testData.txt. Don't open that file in a VM if you know what's good for you.

Be sure not to push any updates while still having testData.txt existing lol. 

Make sure you run all three Utility servers first, THEN the Master, THEN your clients. You'll see them all interact and concurrently handle multiple clients. The only thing is I haven't really attempted doing a ton of clients at the same time, because I can only really run it as fast as I can ctrl+shift+c ctrl+shift+v

Here's an example of how I'd do it (All on separate terminals):

java Utility 32006
java Utility 32007
java Utility 32008

java Master 3000

java Client ../res/testData.txt merge
java Client ../res/testData.txt merge

(twice just to show concurrency)