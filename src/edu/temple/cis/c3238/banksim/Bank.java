package edu.temple.cis.c3238.banksim;

import java.util.concurrent.locks.ReentrantLock;
/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 */

public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long ntransacts = 0;
    private final int initialBalance;
    private final int numAccounts;
    private boolean open;
    
    public ReentrantLock aLock = new ReentrantLock();
    
    
    public Bank(int numAccounts, int initialBalance) {
        
        open = true;
        
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(this, i, initialBalance);
        }
        ntransacts = 0;
    }

    public void transfer(int from, int to, int amount) {
        accounts[from].waitForSufficientFunds(amount);
        if(!open){
            return;
        }
        
        aLock.lock();
        try{
            
            if (accounts[from].withdraw(amount)){
                accounts[to].deposit(amount);
            }
            
        }
        finally{
         aLock.unlock();
        }
        if(shouldTest()) test();
        
        /*
        if (accounts[from].withdraw(amount)) {
            accounts[to].deposit(amount);
        }
        if (shouldTest()) test();
        */
}

    public void test() {
        int sum = 0;
        
        aLock.lock();
        try{
            
            
        for (Account account : accounts) {
            System.out.printf("%s %s%n", 
                    Thread.currentThread().toString(), account.toString());
            sum += account.getBalance();
        }//end of for
        
        }
        
        finally{
            aLock.unlock();
        }
        
        System.out.println(Thread.currentThread().toString() + 
                " Sum: " + sum);
        if (sum != numAccounts * initialBalance) {
            System.out.println(Thread.currentThread().toString() + 
                    " Money was gained or lost");
            System.exit(1);
        } else {
            System.out.println(Thread.currentThread().toString() + 
                    " The bank is in balance");
        }
    }

    public int size() {
        return accounts.length;
    }
    
    
    public synchronized boolean shouldTest() {
        return ++ntransacts % NTEST == 0;
    }

    //is the bank account open
    public synchronized boolean isOpen(){
        return open;
    }
    
    //closing the bank
    public void closeBank(){
        synchronized(this){
            open = false;
        }
        for (Account account : accounts){
            synchronized(account){
                account.notifyAll();
            }
            
        }//end of for
    }
    
    
    
    
    
    
}
