package nl.pancompany.spaceinvaders.events;

import lombok.Data;

@Data
public class SpriteTurned {

    public enum TurnDirection {
        LEFT, RIGHT
    }

    private final TurnDirection turnDirection;
}
