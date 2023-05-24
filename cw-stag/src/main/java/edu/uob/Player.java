package edu.uob;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;

public class Player {


    String name;
    ArrayList<Artefact> inventory;
    String currentLocation;
    Integer health;

    public Player(String name) {

        this.name = name;
        inventory = new ArrayList<>();
        this.health = 3;

    }

    public Integer getHealth() {

        return health;

    }

    public void consumeHealth() {

        health--;

    }

    public void produceHealth() {

        if (health < 3) {
            health++;
        }

    }

    public void resetHealth() {

        this.health = 3;

    }

    public String getName() {

        return name;

    }

    public void setCurrentLocation(String location){

        this.currentLocation = location;

    }

    public String getCurrentLocation() {

        return currentLocation;

    }

    public String getInventoryList() {
        String message = "Inventory:";
        for (int i = 0; i < inventory.size(); i++){
            String item = inventory.get(i).getName();
            message = message.concat("\n" + item);
        }
        return message;
    }

    public ArrayList<Artefact> getInventory() {

        return inventory;

    }

    public void addInventory(Artefact artefact){

        inventory.add(artefact);

    }

    public void removeInventory(String entity){

        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getName().equals(entity)){
                inventory.remove(i);
                return;
            }
        }

    }

}
