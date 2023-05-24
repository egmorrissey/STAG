package edu.uob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    @Test
    void simpleTests() {

        Location location = new Location("Cursed_Village", "Village engulfed in fog");
        assertEquals(location.getName(), "Cursed_Village");
        assertEquals(location.getDescription(), "Village engulfed in fog");

    }

    @Test
    void addEntityTests() {

        Location location = new Location("cursed_village", "Village engulfed in fog");
        assertEquals(location.getName(), "cursed_village");
        assertEquals(location.getDescription(), "Village engulfed in fog");

        location.addEntity("characters", "ghost", "A figure dressed in white");
        location.addEntity("characters", "cat", "A black cat");
        location.addEntity("artefacts", "chalk", "A piece of chalk");
        location.addEntity("furniture", "candle", "An unlit candle");

        assertEquals(location.getCharacterByName("ghost"),0);
        assert(location.getCharacterByID(location.getCharacterByName("ghost")).getName().equals("ghost"));
        assert(location.getCharacterByID(location.getCharacterByName("ghost")).getDescription().equals("A figure dressed in white"));
        assert(location.getEntities().get(0).equals("ghost"));
        assertEquals(location.getCharacterByName("cat"),1);
        assert(location.getCharacterByID(location.getCharacterByName("cat")).getName().equals("cat"));
        assert(location.getCharacterByID(location.getCharacterByName("cat")).getDescription().equals("A black cat"));
        assert(location.getEntities().get(1).equals("cat"));

        assertEquals(location.getArtefactsByName("chalk"),0);
        assert(location.getArtefactsByID(location.getArtefactsByName("chalk")).getName().equals("chalk"));
        assert(location.getArtefactsByID(location.getArtefactsByName("chalk")).getDescription().equals("A piece of chalk"));
        assert(location.getEntities().get(2).equals("chalk"));

        assertEquals(location.getFurnitureByName("candle"),0);
        assert(location.getFurnitureByID(location.getFurnitureByName("candle")).getName().equals("candle"));
        assert(location.getFurnitureByID(location.getFurnitureByName("candle")).getDescription().equals("An unlit candle"));
        assert(location.getEntities().get(3).equals("candle"));

    }

    @Test
    void addPathTests() {

        Location location = new Location("cursed_village", "Village engulfed in fog");
        assertEquals(location.getName(), "cursed_village");
        assertEquals(location.getDescription(), "Village engulfed in fog");

        location.addPath("old_well");
        assertEquals(location.getPathByName("old_well"),0);
        assert(location.getPathByID(location.getPathByName("old_well")).equals("old_well"));

        location.addPath("shrine");
        assertEquals(location.getPathByName("shrine"),1);
        assert(location.getPathByID(location.getPathByName("shrine")).equals("shrine"));

    }

    @Test
    void removeEntityStringTests() {

        Location location = new Location("cursed_village", "Village engulfed in fog");
        assertEquals(location.getName(), "cursed_village");
        assertEquals(location.getDescription(), "Village engulfed in fog");

        location.addEntity("characters", "ghost", "A figure dressed in white");
        assertEquals(location.getEntities().size(), 1);

        location.removeEntityString("ghost");
        assertEquals(location.getEntities().size(), 0);

    }

}