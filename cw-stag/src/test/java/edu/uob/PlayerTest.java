package edu.uob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    Player player;

    @Test
    void inventoryTest() {

        player = new Player("egg");
        Artefact hand = new Artefact("hand", "A prop hand... did I see it move?");
        Artefact pie = new Artefact("pie", "Just a plain piano. Nothing funny here. I hope.");
        player.addInventory(hand);
        player.addInventory(pie);
        assertEquals(player.getInventory().size(), 2);
        assert(player.getInventory().get(0).getName().equals("hand"));
        assert(player.getInventory().get(0).getDescription().equals("A prop hand... did I see it move?"));
        assert(player.getInventory().get(1).getName().equals("pie"));
        assert(player.getInventory().get(1).getDescription().equals("Just a plain piano. Nothing funny here. I hope."));

        player.removeInventory("pie");
        assertEquals(player.getInventory().size(), 1);

    }

    @Test
    void healthTest() {

        //new player starts with three health
        player = new Player("bobobo");
        assertEquals(player.getHealth(), 3);

        //health is consumed
        player.consumeHealth();
        assertEquals(player.getHealth(), 2);

        //health won't increase beyond 3
        player.produceHealth();
        assertEquals(player.getHealth(), 3);
        player.produceHealth();
        assertEquals(player.getHealth(), 3);


    }

}