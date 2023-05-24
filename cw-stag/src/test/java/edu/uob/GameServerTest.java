package edu.uob;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class GameServerTest {

    GameServer server;

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void locationTest() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //assert there are four locations
        assertEquals(server.getLocations().size(), 4);

        //assert each name and location has been added to a separate location class
        assert(server.getLocations().get(0).getName().equals("cabin"));
        assert(server.getLocations().get(0).getDescription().equals("A log cabin in the woods"));

        assert(server.getLocations().get(1).getName().equals("forest"));
        assert(server.getLocations().get(1).getDescription().equals("A dark forest"));

        assert(server.getLocations().get(2).getName().equals("cellar"));
        assert(server.getLocations().get(2).getDescription().equals("A dusty cellar"));

    }

    @Test
    void actionTest() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        assertTrue(server.getActionHashList().containsKey("open"));
        assertTrue(server.getActionHashList().containsKey("unlock"));
        assertTrue(server.getActionHashList().containsKey("chop"));
        assertTrue(server.getActionHashList().containsKey("cut"));
        assertTrue(server.getActionHashList().containsKey("cutdown"));
        assertTrue(server.getActionHashList().containsKey("drink"));
        assertTrue(server.getActionHashList().containsKey("fight"));
        assertTrue(server.getActionHashList().containsKey("hit"));
        assertTrue(server.getActionHashList().containsKey("attack"));

        //Iteration version
        Iterator<GameAction> it = server.getActionHashList().get("open").iterator();
        assert (it.next().getSubjects().get(0).equals("trapdoor"));

        //Arraylist version
        ArrayList<GameAction> list = new ArrayList<>(server.getActionHashList().get("open"));
        assert (list.get(0).getSubjects().get(0).equals("trapdoor"));
        assert (list.get(0).getSubjects().get(1).equals("key"));
        assert (list.get(0).getConsumed().get(0).equals("key"));
        assert (list.get(0).getProduced().get(0).equals("cellar"));
        assert (list.get(0).getNarration().equals("You unlock the trapdoor and see steps leading down into a cellar"));

        //Array version
        GameAction[] array = server.getActionHashList().get("open").toArray(new GameAction[0]);
        assert (array[0].getSubjects().get(0).equals("trapdoor"));
        assert (array[0].getSubjects().get(1).equals("key"));
        assert (array[0].getConsumed().get(0).equals("key"));
        assert (array[0].getProduced().get(0).equals("cellar"));
        assert (array[0].getNarration().equals("You unlock the trapdoor and see steps leading down into a cellar"));

    }

    @Test
    void entityTest() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //assert each entity has been added to the correct class in the correct location class
        assert(server.getLocations().get(0).getArtefactsByID(0).getName().equals("potion"));
        assert(server.getLocations().get(0).getArtefactsByID(0).getDescription().equals("Magic potion"));
        assert(server.getLocations().get(0).getFurnitureByID(0).getName().equals("trapdoor"));
        assert(server.getLocations().get(0).getFurnitureByID(0).getDescription().equals("Wooden trapdoor"));

        assert(server.getLocations().get(1).getArtefactsByID(0).getName().equals("key"));
        assert(server.getLocations().get(1).getArtefactsByID(0).getDescription().equals("Brass key"));
        assert(server.getLocations().get(1).getFurnitureByID(0).getName().equals("tree"));
        assert(server.getLocations().get(1).getFurnitureByID(0).getDescription().equals("A big tree"));

        assert(server.getLocations().get(2).getCharacterByID(0).getName().equals("elf"));
        assert(server.getLocations().get(2).getCharacterByID(0).getDescription().equals("Angry Elf"));

    }

    @Test
    void pathTest() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //assert each path has been added to the correct location class
        assert(server.getLocations().get(0).getPathByID(0).equals("forest"));
        assert(server.getLocations().get(1).getPathByID(0).equals("cabin"));
        assert(server.getLocations().get(2).getPathByID(0).equals("cabin"));

    }

    @Test
    void basicCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send commands to server
        String response;
        sendCommandToServer("beebo: get potion");
        response = sendCommandToServer("beebo: inv");
        assertTrue(response.contains("potion"));

        response =sendCommandToServer("beebo: drink potion");
        assertTrue(response.contains("You drink the potion and your health improves"));

        response = sendCommandToServer("beebo: inv");
        assertFalse(response.contains("potion"));

        sendCommandToServer("beebo: goto forest");
        response = sendCommandToServer("beebo: chop");
        assertFalse(response.contains("You cut down a tree with the axe"));

        response = sendCommandToServer("beebo: inv");
        assertFalse(response.contains("log"));

        sendCommandToServer("beebo: get key");
        sendCommandToServer("beebo: goto cabin");
        response = sendCommandToServer("beebo: unlock trapdoor");
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("beebo: inv");
        assertFalse(response.contains("key"));

        response = sendCommandToServer("beebo: look");
        assertTrue(response.contains("cellar"));

        sendCommandToServer("beebo: goto cellar");
        response = sendCommandToServer("beebo: fight elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"));

    }

    @Test
    void extendedCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send commands to server
        String response;
        response = sendCommandToServer("ape: look");
        assertTrue(response.contains("potion"));
        assertTrue(response.contains("axe"));
        assertTrue(response.contains("coin"));

        sendCommandToServer("ape: get potion");
        response = sendCommandToServer("ape: inv");
        assertTrue(response.contains("potion"));

        sendCommandToServer("ape: get axe");
        response = sendCommandToServer("ape: inv");
        assertTrue(response.contains("axe"));

        sendCommandToServer("ape: get coin");
        response = sendCommandToServer("ape: inv");
        assertTrue(response.contains("coin"));

        response = sendCommandToServer("ape: look");
        assertFalse(response.contains("potion"));
        assertFalse(response.contains("axe"));
        assertFalse(response.contains("coin"));

        sendCommandToServer("ape: goto forest");
        sendCommandToServer("ape: get key");
        response = sendCommandToServer("ape: inv");
        assertTrue(response.contains("key"));

        sendCommandToServer("ape: goto cabin");
        response = sendCommandToServer("ape: open trapdoor");
        assertTrue(response.contains("You unlock the door and see steps leading down into a cellar"));

        response = sendCommandToServer("ape: inv");
        assertFalse(response.contains("key"));
        assertFalse(response.contains("shovel"));

        sendCommandToServer("ape: goto cellar");
        response = sendCommandToServer("ape: pay elf");
        assertTrue(response.contains("You pay the elf your silver coin and he produces a shovel"));

        response = sendCommandToServer("ape: inv");
        assertTrue(response.contains("shovel"));
        assertFalse(response.contains("coin"));

        sendCommandToServer("ape: goto cabin");
        sendCommandToServer("ape: goto forest");
        response = sendCommandToServer("ape: look");
        assertTrue(response.contains("tree"));

        response = sendCommandToServer("ape: chop tree");
        assertTrue(response.contains("You cut down the tree with the axe"));

        response = sendCommandToServer("ape: inv");
        assertTrue(response.contains("log"));
        assertTrue(response.contains("axe"));

        response = sendCommandToServer("ape: look");
        assertFalse(response.contains("tree"));

        sendCommandToServer("ape: goto riverbank");
        response = sendCommandToServer("ape: look");
        assertFalse(response.contains("clearing"));

        response = sendCommandToServer("ape: bridge river");
        assertTrue(response.contains("You bridge the river with the log and can now reach the other side"));

        response = sendCommandToServer("ape: inv");
        assertFalse(response.contains("log"));

        response = sendCommandToServer("ape: look");
        assertTrue(response.contains("clearing"));

        sendCommandToServer("ape: get horn");
        response = sendCommandToServer("ape: inv");
        assertTrue(response.contains("horn"));

        response = sendCommandToServer("ape: look");
        assertFalse(response.contains("lumberjack"));

        response = sendCommandToServer("ape: blow horn");
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"));

        response = sendCommandToServer("ape: look");
        assertTrue(response.contains("lumberjack"));

        sendCommandToServer("ape: goto clearing");
        response = sendCommandToServer("ape: look");
        assertTrue(response.contains("ground:"));
        assertFalse(response.contains("hole"));

        response = sendCommandToServer("ape: dig ground");
        assertTrue(response.contains("You dig into the soft ground and unearth a pot of gold !!!"));

        response = sendCommandToServer("ape: look");
        assertTrue(response.contains("hole"));
        assertFalse(response.contains("ground:"));

        response = sendCommandToServer("ape: blow horn");
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"));

        response = sendCommandToServer("ape: look");
        assertTrue(response.contains("lumberjack"));

        sendCommandToServer("ape: goto riverbank");
        response = sendCommandToServer("ape: look");
        assertFalse(response.contains("lumberjack"));

    }

    @Test
    void basicAlternateCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send alternate commands to server
        String response;
        sendCommandToServer("chinmo: get potion");
        response = sendCommandToServer("chinmo: inv");
        assertTrue(response.contains("potion"));

        sendCommandToServer("chinmo: goto forest");
        response = sendCommandToServer("chinmo: cut");
        assertFalse(response.contains("You cut down a tree with the axe"));

        response = sendCommandToServer("chinmo: inv");
        assertFalse(response.contains("log"));

        sendCommandToServer("chinmo: goto forest");
        sendCommandToServer("chinmo: get key");
        sendCommandToServer("chinmo: goto cabin");
        response = sendCommandToServer("chinmo: open trapdoor");
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("chinmo: inv");
        assertFalse(response.contains("chinmo: key"));

        response = sendCommandToServer("chinmo: look");
        assertTrue(response.contains("cellar"));

        sendCommandToServer("chinmo: goto cellar");
        response = sendCommandToServer("chinmo: hit elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"));

        response = sendCommandToServer("chinmo: attack elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"));

    }

    @Test
    void extendedAlternateCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send commands to server
        String response;
        response = sendCommandToServer("jonas: look");
        assertTrue(response.contains("potion"));
        assertTrue(response.contains("axe"));
        assertTrue(response.contains("coin"));

        sendCommandToServer("jonas: get potion");
        response = sendCommandToServer("jonas: inv");
        assertTrue(response.contains("potion"));

        sendCommandToServer("jonas: get axe");
        response = sendCommandToServer("jonas: inv");
        assertTrue(response.contains("axe"));

        sendCommandToServer("jonas: get coin");
        response = sendCommandToServer("jonas: inv");
        assertTrue(response.contains("coin"));

        response = sendCommandToServer("jonas: look");
        assertFalse(response.contains("potion"));
        assertFalse(response.contains("axe"));
        assertFalse(response.contains("coin"));

        sendCommandToServer("jonas: goto forest");
        sendCommandToServer("jonas: get key");
        response = sendCommandToServer("jonas: inv");
        assertTrue(response.contains("key"));

        sendCommandToServer("jonas: goto cabin");
        response = sendCommandToServer("jonas: unlock trapdoor");
        assertTrue(response.contains("You unlock the door and see steps leading down into a cellar"));

        response = sendCommandToServer("jonas: inv");
        assertFalse(response.contains("key"));
        assertFalse(response.contains("shovel"));

        sendCommandToServer("jonas: goto cellar");
        response = sendCommandToServer("jonas: pay coin");
        assertTrue(response.contains("You pay the elf your silver coin and he produces a shovel"));

        response = sendCommandToServer("jonas: inv");
        assertTrue(response.contains("shovel"));
        assertFalse(response.contains("coin"));

        sendCommandToServer("jonas: goto cabin");
        sendCommandToServer("jonas: goto forest");
        response = sendCommandToServer("jonas: look");
        assertTrue(response.contains("tree"));

        response = sendCommandToServer("jonas: cut tree");
        assertTrue(response.contains("You cut down the tree with the axe"));

        response = sendCommandToServer("jonas: inv");
        assertTrue(response.contains("log"));
        assertTrue(response.contains("axe"));

        response = sendCommandToServer("jonas: look");
        assertFalse(response.contains("tree"));

        sendCommandToServer("jonas: goto riverbank");
        response = sendCommandToServer("jonas: look");
        assertFalse(response.contains("clearing"));

        response = sendCommandToServer("jonas: bridge log");
        assertTrue(response.contains("You bridge the river with the log and can now reach the other side"));

        response = sendCommandToServer("jonas: inv");
        assertFalse(response.contains("log"));

        response = sendCommandToServer("jonas: look");
        assertTrue(response.contains("clearing"));

        sendCommandToServer("jonas: get horn");
        response = sendCommandToServer("jonas: inv");
        assertTrue(response.contains("horn"));

        response = sendCommandToServer("jonas: look");
        assertFalse(response.contains("lumberjack"));

        response = sendCommandToServer("jonas: blow horn");
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"));

        response = sendCommandToServer("jonas: look");
        assertTrue(response.contains("lumberjack"));

        sendCommandToServer("jonas: goto clearing");
        response = sendCommandToServer("jonas: look");
        assertTrue(response.contains("ground:"));
        assertFalse(response.contains("hole"));

        response = sendCommandToServer("jonas: dig shovel");
        assertTrue(response.contains("You dig into the soft ground and unearth a pot of gold !!!"));

        response = sendCommandToServer("jonas: look");
        assertTrue(response.contains("hole"));
        assertFalse(response.contains("ground:"));

        response = sendCommandToServer("jonas: blow horn");
        assertTrue(response.contains("You blow the horn and as if by magic, a lumberjack appears !"));

        response = sendCommandToServer("jonas: look");
        assertTrue(response.contains("lumberjack"));

        sendCommandToServer("jonas: goto riverbank");
        response = sendCommandToServer("jonas: look");
        assertFalse(response.contains("lumberjack"));

    }

    @Test
    void decoratedCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send decorated commands to server
        String response;
        sendCommandToServer("bimblo: get a potion");
        response = sendCommandToServer("bimblo: open inv");
        assertTrue(response.contains("potion"));

        response =sendCommandToServer("bimblo: have a drink of the potion");
        assertTrue(response.contains("You drink the potion and your health improves"));

        response = sendCommandToServer("bimblo: check your inventory");
        assertFalse(response.contains("potion"));

        sendCommandToServer("bimblo: goto the forest");
        sendCommandToServer("bimblo: get the key you can see");
        sendCommandToServer("bimblo: goto the path leading to the cabin");
        response = sendCommandToServer("bimblo: unlock the trapdoor with the key you picked up");
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("bimblo: inv");
        assertFalse(response.contains("key"));

        response = sendCommandToServer("bimblo: look");
        assertTrue(response.contains("cellar"));

        sendCommandToServer("bimblo: goto cellar");
        response = sendCommandToServer("bimblo: fight elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"));

    }

    @Test
    void wordOrderCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send commands in a different order to server
        String response;
        sendCommandToServer("yasopp: get potion");
        response =sendCommandToServer("yasopp: with potion take a drink");
        assertTrue(response.contains("You drink the potion and your health improves"));

        response = sendCommandToServer("yasopp: inv");
        assertFalse(response.contains("potion"));

        sendCommandToServer("yasopp: goto forest");
        sendCommandToServer("yasopp: get key");
        sendCommandToServer("yasopp: goto cabin");
        response = sendCommandToServer("yasopp: use the key to unlock the trapdoor");
        assertTrue(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("yasopp: inv");
        assertFalse(response.contains("key"));

        response = sendCommandToServer("yasopp: look");
        assertTrue(response.contains("cellar"));

        sendCommandToServer("yasopp: goto cellar");
        response = sendCommandToServer("yasopp: see the elf and fight");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"));

    }

    @Test
    void extaneousEntitiesCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send commands with wrong subject to server
        String response;
        sendCommandToServer("ophelia: get potion");
        response = sendCommandToServer("ophelia: inv");
        assertTrue(response.contains("potion"));

        response =sendCommandToServer("ophelia: drink key");
        assertFalse(response.contains("You drink the potion and your health improves"));

        response =sendCommandToServer("ophelia: open potion");
        assertFalse(response.contains("You drink the potion and your health improves"));

        sendCommandToServer("ophelia: goto forest");
        sendCommandToServer("ophelia: get key");
        sendCommandToServer("ophelia: goto cabin");
        response = sendCommandToServer("ophelia: drink key");
        assertFalse(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("ophelia: drink trapdoor");
        assertFalse(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("ophelia: use key");
        assertFalse(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

    }

    @Test
    void notCompleteCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send incomplete commands to server
        String response;
        sendCommandToServer("klaus: get potion");
        response = sendCommandToServer("klaus: inv");
        assertTrue(response.contains("potion"));

        response =sendCommandToServer("klaus: drink");
        assertFalse(response.contains("You drink the potion and your health improves"));

        response =sendCommandToServer("klaus: potion");
        assertFalse(response.contains("You drink the potion and your health improves"));

        sendCommandToServer("klaus: goto forest");
        sendCommandToServer("klaus: get key");
        sendCommandToServer("klaus: goto cabin");
        response = sendCommandToServer("klaus: unlock");
        assertFalse(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("klaus: key");
        assertFalse(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

        response = sendCommandToServer("klaus: trapdoor");
        assertFalse(response.contains("You unlock the trapdoor and see steps leading down into a cellar"));

    }

    @Test
    void playerLocationArrayPositionTest() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);
        sendCommandToServer("rudy: look");
        assert(server.players.get(0).getCurrentLocation().equals("cabin"));
        assertEquals(server.playerLocationArrayPosition(), 0);

        //move player
        sendCommandToServer("rudy: goto forest");
        //assert(server.player.getCurrentLocation().equals("forest"));
        assertEquals(server.playerLocationArrayPosition(), 1);

    }

    @Test
    void potentialErrorTest() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "basic-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "basic-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send incorrect commands to server
        sendCommandToServer("Pie: chop");

        server = new GameServer(entitiesFile, actionsFile);

        sendCommandToServer("Pie:");

        sendCommandToServer("");

    }

    @Test
    void healthTest() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send commands to server
        String response;
        sendCommandToServer("zatch: get potion");
        sendCommandToServer("zatch: get axe");
        sendCommandToServer("zatch: get coin");
        sendCommandToServer("zatch: goto forest");
        sendCommandToServer("zatch: get key");
        sendCommandToServer("zatch: goto cabin");
        sendCommandToServer("zatch: unlock trapdoor");
        sendCommandToServer("zatch: goto cellar");
        response = sendCommandToServer("zatch: fight elf");
        assertTrue(response.contains("You attack the elf, but he fights back and you lose some health"));
        assertEquals(server.players.get(0).getHealth(), 2);

        response =sendCommandToServer("zatch: drink potion");
        assertTrue(response.contains("You drink the potion and your health improves"));
        assertEquals(server.players.get(0).getHealth(), 3);

        response = sendCommandToServer("zatch: inv");
        assertTrue(response.contains("axe"));
        assertTrue(response.contains("coin"));

        response = sendCommandToServer("zatch: look");
        assertTrue(response.contains("cellar"));
        assertEquals("cellar", server.players.get(0).getCurrentLocation());

        sendCommandToServer("zatch: fight elf");
        assertEquals(server.players.get(0).getHealth(), 2);

        sendCommandToServer("zatch: fight elf");
        assertEquals(server.players.get(0).getHealth(), 1);

        sendCommandToServer("zatch: fight elf");
        assertEquals(server.players.get(0).getHealth(), 3);

        assertEquals("cabin", server.players.get(0).getCurrentLocation());

        response = sendCommandToServer("zatch: inv");
        assertFalse(response.contains("axe"));
        assertFalse(response.contains("coin"));

        sendCommandToServer("zatch: goto cellar");
        response = sendCommandToServer("zatch: look");
        assertTrue(response.contains("axe"));
        assertTrue(response.contains("coin"));

    }

}