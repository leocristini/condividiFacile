package io.condividifacile.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by leonardo on 23/08/17.
 */

public class Group {

    private String name;
    private HashMap<String,String> members;
    private ArrayList<Expense> expenses;
    private ArrayList<String> categories;

    public Group(String name,HashMap<String,String> members){
        this.name = name;
        this.members = members;
    }

    public Group(String name, HashMap<String,String> members, ArrayList<Expense> expenses, ArrayList<String> categories) {

        this.name = name;
        this.members = members;
        this.expenses = expenses;
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getMembers() {
        return members;
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }

    public void addExpenses(Expense expenses){
        this.expenses.add(expenses);
    }


    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }


}
