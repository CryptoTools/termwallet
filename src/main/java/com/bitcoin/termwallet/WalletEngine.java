package com.bitcoin.termwallet;

import com.google.bitcoin.core.*;
import com.google.bitcoin.wallet.CoinSelector;
import com.google.bitcoin.params.MainNetParams;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import static com.google.common.base.Preconditions.checkNotNull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.*;

public class WalletEngine extends AbstractWalletEventListener {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Handle all incoming transactions, create TransactionFutures for each of them.
    @Override
    public void onCoinsReceived(Wallet w, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        final BigInteger value = tx.getValueSentToMe(w);
        System.out.println("Received tx for " + Utils.bitcoinValueToFriendlyString(value) + ": " + tx);
        System.out.println("Transaction will be added to your balance after it confirms! Congrats!");
	System.out.println("If you keep termWallet running a confirmation message will be displayed.");

        // Wait until it's made it into the block chain (may run immediately if it's already there).
        Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<Transaction>() {
            @Override
            public void onSuccess(Transaction result) {
                // check if the transaction is internal - if it is, handle it
                System.out.println("Transaction confirmed:" + result.toString());
            }

            @Override
            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }
	public void initiateEngine() { // Load associated addresses and start TransactionFutureProcessor.
	}

    @Override
    public void onKeysAdded(Wallet wallet, List<ECKey> keys) {
    }

    public Address newAddress() {
    	// Generates new keypair, adds private key to wallet, and then returns the corresponding address.
    	ECKey eckey = new ECKey();
    	App.getKit().wallet().addKey(eckey);
    	return eckey.toAddress(Constants.params);
    }
    public void createSendRequest(String destination, BigInteger amount) {
	try {
            System.out.println("Sending " + Utils.bitcoinValueToFriendlyString(amount) + " BTC with TX FEE: " + Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            // Account for the TX Fee
            final BigInteger amountToSend = amount.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
	    final BigInteger amountInWallet = App.getKit().wallet().getBalance();
		System.out.println("balance: " + amountInWallet);
            // Creates the send request and designates the change address (where change will be sent).
            Wallet.SendRequest sendRequest = Wallet.SendRequest.to(new Address(Constants.params, destination), amountToSend);
            sendRequest.changeAddress = App.getKit().wallet().getKeys().get(0).toAddress(Constants.params);
            final Wallet.SendResult sendResult = App.getKit().wallet().sendCoins(App.getKit().peerGroup(), sendRequest);
            checkNotNull(sendResult);  // We should never try to send more coins than we have!
            System.out.println("Sending...");
            // Register a callback that is invoked when the transaction has propagated across the network.
            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.out.println("Sent coins! Transaction hash is " + sendResult.tx.getHashAsString());
                }
            }, MoreExecutors.sameThreadExecutor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createSendRequest(Address address, String destination, BigInteger amount) {
	BigInteger amountTo = amount;
	 try {
            // Subtract from the withdrawal request your 'cut' of the deposit (CHARGE_PERCENT)
            System.out.println("Sending " + Utils.bitcoinValueToFriendlyString(amount) + " BTC to " + destination + " with TX FEE: " + Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            // Account for the TX Fee
            final BigInteger amountToSend = amountTo.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
	    final BigInteger amountInAddress = App.getKit().wallet().getBalance(new IndividualCoinSelector(address));
            // Changes the coinSelector to only pick from the address that the coins are currently at.
            App.getKit().wallet().setCoinSelector(new IndividualCoinSelector(address));
	    if(amountInAddress.compareTo(amountToSend) == -1) {
		System.out.println("You don't have enough funds! You need " + amountToSend.subtract(amountInAddress) + " satoshis more!");
	    	return;
	    }
            // Creates the send request and designates the change address (where change will be sent).
            Wallet.SendRequest sendRequest = Wallet.SendRequest.to(new Address(Constants.params, destination), amountToSend);
            sendRequest.changeAddress = address;
            final Wallet.SendResult sendResult = App.getKit().wallet().sendCoins(App.getKit().peerGroup(), sendRequest);
            checkNotNull(sendResult);  // We should never try to send more coins than we have!
            System.out.println("Sending...");
            // Register a callback that is invoked when the transaction has propagated across the network.
            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.out.println("Sent coins! Transaction hash is " + sendResult.tx.getHashAsString());
                }
            }, MoreExecutors.sameThreadExecutor());
        } catch (Exception e) {
	    System.out.println("Error sending transaction! Were you trying to send BTC you don't have?");
            e.printStackTrace();
        }
    }
    public void createSendRequest(ArrayList<Address> addresses, String destination, BigInteger amount) {
        System.out.println("handling new createSendRequest");
        try {
            // Subtract from the withdrawal request your 'cut' of the deposit (CHARGE_PERCENT)
            System.out.println("Sending " + Utils.bitcoinValueToFriendlyString(amount) + " BTC with TX FEE: " + Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            // Account for the TX Fee
            final BigInteger amountToSend = amount.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
            // Changes the coinSelector to only pick from the address that the coins are currently at.
            App.getKit().wallet().setCoinSelector(new MultipleCoinSelector(addresses));
            // Creates the send request and designates the change address (where change will be sent).
            Wallet.SendRequest sendRequest = Wallet.SendRequest.to(new Address(Constants.params, destination), amountToSend);
            sendRequest.changeAddress = addresses.get(0);
            final Wallet.SendResult sendResult = App.getKit().wallet().sendCoins(App.getKit().peerGroup(), sendRequest);
            checkNotNull(sendResult);  // We should never try to send more coins than we have!
            System.out.println("Sending...");
            // Register a callback that is invoked when the transaction has propagated across the network.
            sendResult.broadcastComplete.addListener(new Runnable() {
                @Override
                public void run() {
                    // The wallet has changed now, it'll get auto saved shortly or when the app shuts down.
                    System.out.println("Sent coins! Transaction hash is " + sendResult.tx.getHashAsString());
                }
            }, MoreExecutors.sameThreadExecutor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Get Balance of an array of bitcoin addresses that you control
    public BigInteger getCurrentBalance(ArrayList<Address> addresses) { 
        App.getKit().wallet().setCoinSelector(new MultipleCoinSelector(addresses));
        return App.getKit().wallet().getBalance();
    }
    // Get Balance of one bitcoin address that you control
    public BigInteger getCurrentBalance(Address address) {
        App.getKit().wallet().setCoinSelector(new IndividualCoinSelector(address));
        return App.getKit().wallet().getBalance();
    }
}