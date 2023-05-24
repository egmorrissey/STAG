package edu.uob;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class MultiplePlayerTest {

    GameServer server;

    String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    void extendedCommand() {

        //read files into server
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        server = new GameServer(entitiesFile, actionsFile);

        //send commands to server
        String response;

        //new player is added to starting room
        response = sendCommandToServer("lag: look");
        assertTrue(response.contains("cabin"));
        assertFalse(response.contains("froy"));
        //player cannot see themselves
        assertFalse(response.contains("lag"));

        //only one player in server
        assertEquals(server.players.size(), 1);
        assertEquals(server.currentPlayer, 0);

        //second player is added to starting room
        response = sendCommandToServer("froy: look");
        assertTrue(response.contains("cabin"));
        assertFalse(response.contains("froy"));
        //second player can see the first player
        assertTrue(response.contains("lag"));

        //two players now in server
        assertEquals(server.players.size(), 2);
        //current player is set according to who made the last command
        assertEquals(server.currentPlayer, 1);

        //first player adds potion to their inventory
        response = sendCommandToServer("lag: look");
        assertTrue(response.contains("potion"));
        sendCommandToServer("lag: get potion");
        response = sendCommandToServer("lag: inv");
        assertTrue(response.contains("potion"));

        //second player cannot see potion
        response = sendCommandToServer("froy: look");
        assertFalse(response.contains("potion"));

        //first player drops potion
        sendCommandToServer("lag: drop potion");
        response = sendCommandToServer("lag: inv");
        assertFalse(response.contains("potion"));

        //second player can pick up potion
        sendCommandToServer("froy: get potion");
        response = sendCommandToServer("froy: inv");
        assertTrue(response.contains("potion"));

        //first player moves to forest
        response = sendCommandToServer("lag: goto forest");
        assertTrue(response.contains("forest"));
        //second player cannot be seen
        assertFalse(response.contains("froy"));

        //second player is still in cabin
        response = sendCommandToServer("froy: look");
        assertTrue(response.contains("cabin"));
        //first player cannot be seen
        assertFalse(response.contains("lag"));

    }

}