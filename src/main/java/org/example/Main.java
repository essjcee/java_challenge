package org.example;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main {
    private static BankAccount account;
    private static List<BankAccount> accounts = new ArrayList<>();
    private static final Scanner sc = new Scanner(System.in);
    private static int listIndex = 0;
    public static void main(String[] args) {
        try {
            readAccountsFromFile();
            String choice;
            do {
                System.out.println("Choose an option (a)Add Account (l)Display all Accounts (s)Save to File " +
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
                    case "S":
                        writeAccountsToFile();
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
            writeAccountsToFile();
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
            getAccountFromArray(index);
            System.out.println("How much would you like to withdraw:");
            double amount = Double.parseDouble(sc.nextLine());
            accounts.get(listIndex).withdraw(amount);
            getAccountFromArray(index);
        }
        catch(NumberFormatException ex){
            System.out.println("You must enter a number for the account");
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
            getAccountFromArray(index);
            System.out.println("How much would you like to deposit:");
            double amount = Double.parseDouble(sc.nextLine());
            accounts.get(listIndex).deposit(amount);
            getAccountFromArray(index);
        }
        catch(NumberFormatException ex){
            System.out.println("You must enter a number for the account");
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

    private static void writeAccountsToFile(){
        try{
            FileWriter fileWriter = new FileWriter("C:/data/accounts.csv");
            for(int i = 0; i < accounts.size();i++){
                fileWriter.write(String.format("%s,%.2f",accounts.get(i).getAccount(),accounts.get(i).getBalance()));
                if(accounts.get(i) instanceof SavingsAccount){
                    fileWriter.write(",saver");
                }
                fileWriter.write("\n");
            }
            fileWriter.close();
        }
        catch(IOException ex){
            System.out.println("IO Exception " + ex.getMessage());
        }
    }

    private static void saveAccountsToMongoDB(){
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("test");
        database.getCollection("accounts").drop();
        MongoCollection<Document> bankaccounts = database.getCollection("accounts");
        for(int i = 0; i < accounts.size(); i++){
            Document acDoc = new Document();
            acDoc.put("_id",i+1);
            acDoc.put("account",accounts.get(i).getAccount());
            acDoc.put("balance",accounts.get(i).getBalance());
            if(accounts.get(i) instanceof SavingsAccount){
                acDoc.put("actype","savings");
            }
            else{
                acDoc.put("actype","current");
            }
            bankaccounts.insertOne(acDoc);
        }
        mongoClient.close();
    }

    private static void getAccountsFromMongoDB(){
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> bankaccounts = database.getCollection("accounts");
        MongoCursor<Document> cursor = bankaccounts.find().iterator();
        while(cursor.hasNext()){
            Document account = cursor.next();
            var items = new ArrayList<>(account.values());
            System.out.printf("Account is a %s %s account and has balance %.2f\n",items.get(1),items.get(3),items.get(2));
        }
        mongoClient.close();
    }



    private static void readAccountsFromFile(){
        try{
            File accountsFile = new File("C:/data/accounts.csv");
            if(accountsFile.exists()){
                Scanner sc = new Scanner(accountsFile);
                while(sc.hasNextLine()){
                    String line = sc.nextLine();
                    String[] lineArray = line.split(",");
                    if(lineArray.length == 3){
                        accounts.add(new SavingsAccount(lineArray[0],Double.parseDouble(lineArray[1])));
                    }
                    else{
                        accounts.add(new BankAccount(lineArray[0],Double.parseDouble(lineArray[1])));
                    }
                }
                sc.close();
            }
        }
        catch(IOException ex){
            System.out.println("");
        }
    }

    private static void getAccountFromArray(int index){
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