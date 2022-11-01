package com.ilyas.pdfbox_demo.Pdfbox;

public class Employee {
    private final String firstName;

    private final String lastName;

    private final String accountTypeName;

    private final String alias;

    private final EmployeeStatus status;

    private final double balance;

    public Employee(String firstName, String lastName, String accountTypeName, String alias, EmployeeStatus status, double balance) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountTypeName = accountTypeName;
        this.alias = alias;
        this.status = status;
        this.balance = balance;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAccountTypeName() {
        return accountTypeName;
    }

    public String getAlias() {
        return alias;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public double getBalance() {
        return balance;
    }
}
