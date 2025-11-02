package nl.pancompany.spaceinvaders.events;

import lombok.Data;

@Data
public class SpriteMoved {

    private final int newX;
    private final int newY;
}
