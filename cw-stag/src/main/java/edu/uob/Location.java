package edu.uob;

import java.util.ArrayList;

public class Location extends GameEntity{

    ArrayList<Artefact> artefacts;
    ArrayList<Furniture> furniture;
    ArrayList<Character> characters;
    ArrayList<String> entities;
    ArrayList<String> paths;

    public Location(String name, String description) {

        super(name, description);
        artefacts = new ArrayList<>();
        furniture = new ArrayList<>();
        characters = new ArrayList<>();
        entities = new ArrayList<>();

        paths = new ArrayList<>();

    }

    public void addEntity(String type, String name, String description){

        switch (type) {
            case "artefacts" -> {
                Artefact newArtefact = new Artefact(name, description);
                artefacts.add(newArtefact);
            }
            case "furniture" -> {
                Furniture newFurniture = new Furniture(name, description);
                furniture.add(newFurniture);
            }
            case "characters" -> {
                Character newCharacter = new Character(name, description);
                characters.add(newCharacter);
            }
        }
        entities.add(name);

    }

    public ArrayList<String> getEntities() {

        return entities;

    }

    public void removeEntityString(String entity) {

        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).equals(entity)) {
                entities.remove(i);
                return;
            }
        }

    }

    public void addPath(String path){

        paths.add(path);
    }

    public String getPathByID(int id){

        return paths.get(id);
    }

    public Integer getPathByName(String name){

        for (int i = 0; i < paths.size(); i++){
            if (paths.get(i).equals(name)) {
                return i;
            }
        }
        return null;
    }

    public ArrayList<String> getPaths() {

        return paths;

    }

    public Artefact getArtefactsByID(int id) {

        return artefacts.get(id);
    }

    public Integer getArtefactsByName(String name){

        for (int i = 0; i < artefacts.size(); i++){
            if (artefacts.get(i).getName().equals(name)) {
                return i;
            }
        }
        return null;
    }

    public ArrayList<Artefact> getArtefacts() {

        return artefacts;

    }

    public ArrayList<Furniture> getFurniture() {

        return furniture;

    }

    public ArrayList<Character> getCharacters() {

        return characters;

    }

    public Character getCharacterByID(int id) {

        return characters.get(id);
    }

    public Integer getCharacterByName(String name){

        for (int i = 0; i < characters.size(); i++){
            if (characters.get(i).getName().equals(name)) {
                return i;
            }
        }
        return null;
    }

    public Furniture getFurnitureByID(int id) {

        return furniture.get(id);
    }

    public Integer getFurnitureByName(String name){

        for (int i = 0; i < furniture.size(); i++){
            if (furniture.get(i).getName().equals(name)) {
                return i;
            }
        }
        return null;
    }

}
