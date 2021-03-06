TermWallet
==========

TermWallet is a bitcoinj-based wallet tailormade for the command line.  It does not download the whole blockchain, and as a result only take up about a megabyte of space (not accounting for its own size, roughly 18mb).  Small servers with limited HDD space are thus the ideal use-case for TermWallet.  You can run it with only terminal access.

TermWallet includes a novel adaption of bitcoinj's CoinSelector method, IndividualCoinSelector.  Previously, bitcoinj (and subsequently bitcoinj-based wallets such as Multibit) has not been able to create transactions from a single address's outputs.  IndividualCoinSelector provides this capability. Use the ```-f (--from)``` option while using the ```send``` command to designate the address to send from.

An encryption function is included, currently only encrypting private keys. Further, TermWallet provides a 'panic' command that can send all the BTC's in the wallet to an external address and, optionally, delete the wallet (including all files). This should be used if the computer is compromised or at risk.  No private keys will be easily recoverable.

An experimental Tor function can be used to connect to the blockchain.  Use ```-t``` or ```--tor``` to use it.

To view the usage menu, use ```termwallet -h```

Instructions
============

Requirements:
OSX or Linux
Java 1.6 (bitcoinj requirement)

Quick Install: 
```
wget -O - https://raw.githubusercontent.com/grayleonard/termwallet/master/install.sh | sh
```

Or, if you want to build from source:
```
git clone https://github.com/grayleonard/termwallet.git
cd termWallet
./build.sh
```

Usage
========
```
Usage: <main class> [options] [command] [command options]
  Options:
    -h, --help

       Default: false
    -l, --listen
       Passively listen for transactions
       Default: false
        --testnet
       Use testnet network
       Default: false
    -t, --tor
       Use Tor to connect to bitcoin network
       Default: false
    -v, --verbose
       Verbose output
       Default: false
  Commands:
    status      Prints wallet addresses and balances
      Usage: status [options]

    new      Create a new address with purpose 'change' or 'receive', defaults to 'receive'
      Usage: new [options]
        Options:
          -p, --purpose
             Address purpose
             Default: RECEIVE_FUNDS

    import      Add a private key to your wallet
      Usage: import [options]

    send        Add file contents to the index
      Usage: send [options]
        Options:
        * -a, --amount
             Amount to Send (in BTC)
          -c, --change
             Change address
          -f, --from
             Address to send from
        * -t, --to
             Address to send to

    encrypt      Encrypt private keys and seed in wallet
      Usage: encrypt [options]

    decrypt      Decrypt private keys and seed in wallet
      Usage: decrypt [options]

    export      Export your wallet
      Usage: export [options]

    maintenance      (WIP) Clean up transactions in wallet
      Usage: maintenance [options]

    delete      Remove a key from your watched addresses - destroy imported private keys
      Usage: delete [options]
        Options:
        * -a, --address
             Address to delete

    panic      Send all BTCs in wallet to address, optionally delete wallet
      Usage: panic [options]
        Options:
          -sd, --self-destruct
             Self-Destruct; Erase all private keys and wallet file
             Default: false
          -t, --to
             Address to send to

    restore      Create a wallet based on a BIP-39 mnemonic seed
      Usage: restore [options]
        Options:
          -t, --time
             Replay blockchain starting from this time (UNIX epoch time)
             Default: 1421548754
```
