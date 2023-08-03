package org.example;

public class BankAccount {
    private String account;
    private double balance;

    public BankAccount(String account, double balance) {
        this.account = account;
        this.balance = balance;
    }

    public BankAccount(){

    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public double getBalance() {
        return balance;
    }

    public void withdraw(double amount){
        balance-= amount;
    }

    public void deposit(double amount){
        balance += amount;
    }
}
