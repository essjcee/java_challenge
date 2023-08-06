package org.example;
import com.mongodb.MongoClientException;
import com.mongodb.client.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.bson.Document;

import java.util.*;
import java.util.logging.Logger;

public class Main {
    private static BankAccount account;
    private static List<BankAccount> accounts = new ArrayList<>();
    private static final Scanner sc = new Scanner(System.in);
    private static int listIndex = 0;
    public static void main(String[] args) {
        try {
            //This for MongoDB logging
            Logger logger = Logger.getLogger("Main.class");
            BasicConfigurator.configure();
            getAccountsFromMongoDB();
            String choice;
            do {
                System.out.println("Choose an option (a)Add Account (l)Display all Accounts " +
                        "(w)Withdraw funds (d)Deposit funds (m)Save to MongoDB (r)Read from MongoDB (q)Quit");
                choice = sc.nextLine().toUpperCase();
                switch (choice) {
                    case "A":
                        AddAccount();
                        break;
                    case "Q":
                        System.out.println("Goodbye :)");
                        break;
                    case "L":
                        System.out.println("***** All Bank Accounts *****");
                        displayAllAccounts();
                        break;
                    case "W":
                        withdrawFunds();
                        break;
                    case "D":
                        depositFunds();
                        break;
                    case "M":
                        saveAccountsToMongoDB();
                        break;
                    case "R":
                        getAccountsFromMongoDB();
                        break;
                    default:
                        System.out.println("Not a valid option");
                }
            } while(!choice.toUpperCase().equals("Q"));
            saveAccountsToMongoDB();
            sc.close();
        }
        catch(Exception ex){
            System.out.println("Exception occured " + ex.getMessage());
        }
    }


    private static void withdrawFunds() {
        try {
            displayAllAccounts();
            System.out.println("Enter the number of your account:");
            int index = Integer.parseInt(sc.nextLine());
            getAccountFromList(index);
            System.out.println("How much would you like to withdraw:");
            double amount = Double.parseDouble(sc.nextLine());
            accounts.get(listIndex).withdraw(amount);
            getAccountFromList(index);
        }
        catch(NumberFormatException ex){
            System.out.println("You must enter a number for the account or for the withdrawal amount");
        }
        catch(IndexOutOfBoundsException ex){
            System.out.println("You entered an invalid account number");
        }
    }

    private static void depositFunds() {
        try {
            displayAllAccounts();
            System.out.println("Enter the number of your account:");
            int index = Integer.parseInt(sc.nextLine());
            getAccountFromList(index);
            System.out.println("How much would you like to deposit:");
            double amount = Double.parseDouble(sc.nextLine());
            accounts.get(listIndex).deposit(amount);
            getAccountFromList(index);
        }
        catch(NumberFormatException ex){
            System.out.println("You must enter a number for the account or the deposit amount");
        }
        catch(IndexOutOfBoundsException ex){
            System.out.println("You entered an invalid account number");
        }
    }

    private static void AddAccount(){
        try{
            System.out.println("Enter the account holder: ");
            String holder = sc.nextLine();
            if(holder.length() == 0){
                System.out.println("You must enter an account name");
                return;
            }
            System.out.println("Enter the account opening balance: ");
            double balance = Double.parseDouble(sc.nextLine());
            System.out.println("Enter the Account type (s)Savings Account (a)Normal Account");
            String choice = sc.nextLine();

            if(choice.toUpperCase().equals("S")){
                account = new SavingsAccount(holder,balance);
            }
            else if(choice.toUpperCase().equals("A")){
                account = new BankAccount(holder,balance);
            }
            else{
                System.out.println("Invalid Account type");
                return;
            }
            displayAccount(account);
        }
        catch(NumberFormatException ex) {
            System.out.println("You must enter a number for the balance");
        }

    }

    private static void displayAccount(BankAccount account){
        if(account instanceof SavingsAccount){
            System.out.printf("Savings Account %s has balance %.2f\n", account.getAccount(),account.getBalance());
        }
        else{
            System.out.printf("Current Account %s has balance %.2f\n",account.getAccount(),account.getBalance());
        }
        addToList(account);
    }

    private static void addToList(BankAccount account){
        accounts.add(account);
    }

    private static void displayAllAccounts(){
        int index = 0;
        for(BankAccount account: accounts){
            if(account instanceof SavingsAccount){
                System.out.printf("(%d)Savings Account %s has balance %.2f\n",++index,account.getAccount(),account.getBalance());
            }
            else{
                System.out.printf("(%d)Current Account %s has balance %.2f\n",++index,account.getAccount(),account.getBalance());
            }
        }
    }

    private static void saveAccountsToMongoDB(){
        try {
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("test");
            database.getCollection("accounts").drop();
            MongoCollection<Document> bankaccounts = database.getCollection("accounts");
            for (int i = 0; i < accounts.size(); i++) {
                Document acDoc = new Document();
                acDoc.put("_id", i + 1);
                acDoc.put("account", accounts.get(i).getAccount());
                acDoc.put("balance", accounts.get(i).getBalance());
                if (accounts.get(i) instanceof SavingsAccount) {
                    acDoc.put("actype", "savings");
                } else {
                    acDoc.put("actype", "current");
                }
                bankaccounts.insertOne(acDoc);
            }
            mongoClient.close();
        }
        catch(MongoClientException ex){
            System.out.println("Database Exception " + ex.getMessage());
        }
    }

    private static void getAccountsFromMongoDB(){
        try {
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("test");
            MongoCollection<Document> bankaccounts = database.getCollection("accounts");
            FindIterable<Document> cursor = bankaccounts.find();
            for(Document account : cursor){
                if(account.getString("actype").equals("savings")){
                    accounts.add(new SavingsAccount( account.getString("account"), account.getDouble("balance")));
                }
                else{
                    accounts.add(new BankAccount( account.getString("account"), account.getDouble("balance")));
                }
            }
            mongoClient.close();
        }
        catch(MongoClientException ex){
            System.out.println("Database Exception " + ex.getMessage());
        }
    }

    private static void getAccountFromList(int index){
        try {
            System.out.println("***** Your current details are ***** ");
            System.out.printf("%s has balance %.2f\n", accounts.get(index - 1).getAccount(), accounts.get(index - 1).getBalance());
            listIndex = index - 1;
        }
        catch(IndexOutOfBoundsException ex){
            throw ex;

        }
    }

}