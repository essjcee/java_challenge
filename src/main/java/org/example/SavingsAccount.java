package org.example;

public class SavingsAccount extends BankAccount{

    public SavingsAccount(String name, double balance){
        super(name, balance);
    }

    @Override
    public void withdraw(double amount) {
        if(getBalance() - amount < 100){
            System.out.println("You have insufficient funds");
        }
        else{
            super.withdraw(amount);
        }
    }
}
