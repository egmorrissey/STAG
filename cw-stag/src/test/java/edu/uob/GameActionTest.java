package edu.uob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameActionTest {

    GameAction action;

    @Test
    void basicTest() {

        action = new GameAction();
        action.addSubject("wings");
        action.addProduced("feather");
        action.addConsumed("wings");
        action.addNarration("You flew");

        assert(action.getSubjects().get(0).equals("wings"));
        assert(action.getProduced().get(0).equals("feather"));
        assert(action.getConsumed().get(0).equals("wings"));
        assert(action.getNarration().equals("You flew"));

    }

}