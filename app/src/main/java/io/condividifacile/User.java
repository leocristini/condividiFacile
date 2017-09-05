package io.condividifacile;

import java.util.ArrayList;

/**
 * Created by leonardo on 28/08/17.
 */

public class User {

    private String name;
    private String email;
    private ArrayList<String> groups;
    private double saldo;

    public User(String name, String email, ArrayList<String> groups, double saldo) {
        this.name = name;
        this.email = email;
        this.groups = groups;
        this.saldo = saldo;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}
