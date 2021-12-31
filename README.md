# About this Project
This project is another testimony to the security of bitcoin blockchain.
It generates new wallet addresses and compares them to the address known to have a decent amount of BTC.

If it finds a matching address from the known set of addresses, it will save to a json file in home dir.

The Bitcoin blockchain currently uses the RIPE-MD160 hash function, as long as this protocol is used, 2^160 Bitcoin addresses are possible. This makes exactly 1,461,501,637,330,902,918,203,684,832,716,283,019,655,932,542,976 possible Bitcoin addresses.

# Getting Started

# Download the latest addresses from blockchair
```
wget https://gz.blockchair.com/bitcoin/addresses/blockchair_bitcoin_addresses_latest.tsv.gz
gunzip blockchair_bitcoin_addresses_latest.tsv.gz
```

Place the file in the home directory of the user where you will run it

Build the code, and place jar file and run.sh in the same directory, execute it and hope to get lucky.

# How to refresh db

When you have downloaded a new file from blockchair, simply delete the chronicle.set file, and restart the program.

It will read the tsv file and re-create the chronicle.set file at startup.

# What is chronicle.set file

You can consider chronicle set to be the database for this program. It uses Chronicle set to store the keys off of RAM 
so that this program uses very little memory compared to other similar BTC colliders
