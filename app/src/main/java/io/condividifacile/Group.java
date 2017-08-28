package io.condividifacile;

import java.util.ArrayList;

/**
 * Created by leonardo on 23/08/17.
 */

public class Group {

    private String name;
    private ArrayList<String> members;
    private ArrayList<Expense> expenses;
    private ArrayList<String> categories;

    public Group(String name,ArrayList<String> members){
        this.name = name;
        this.members = members;
    }

    public Group(String name, ArrayList<String> members, ArrayList<Expense> expenses, ArrayList<String> categories) {

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

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }
    //aggiungi utente / rimuovi utente
    public void addUser(String user){
        this.members.add(user);
    }
    public void removeUSer(String user){
        for(int i = 0; i<this.members.size();i++){
            if(this.members.get(i) == user)
                this.members.remove(i);
        }
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
