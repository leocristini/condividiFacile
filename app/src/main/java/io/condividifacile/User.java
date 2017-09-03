package io.condividifacile;

import java.util.ArrayList;

/**
 * Created by leonardo on 28/08/17.
 */

public class User {

    private String name;
    private String mail;
    private ArrayList<String> groups;
    private double saldo;

    public User(String name, String mail, ArrayList<String> groups, double saldo) {
        this.name = name;
        this.mail = mail;
        this.groups = groups;
        this.saldo = saldo;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
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
