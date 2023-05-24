package edu.uob;

import java.util.ArrayList;

public class GameAction {

    ArrayList<String> subjects;
    ArrayList<String> consumed;
    ArrayList<String> produced;
    String narration;

    public GameAction() {

        subjects = new ArrayList<>();
        consumed = new ArrayList<>();
        produced = new ArrayList<>();

    }

    public void addSubject(String subject){

        subjects.add(subject);

    }

    public ArrayList<String> getSubjects() {

        return subjects;

    }

    public void addConsumed(String cons){

        consumed.add(cons);

    }

    public ArrayList<String> getConsumed() {

        return consumed;

    }

    public void addProduced(String prod){

        produced.add(prod);

    }

    public ArrayList<String> getProduced() {

        return produced;

    }

    public void addNarration(String nar){

        this.narration = nar;

    }

    public String getNarration() {

        return narration;

    }
}
