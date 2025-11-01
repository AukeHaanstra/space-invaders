package nl.pancompany.spaceinvaders.events;

import lombok.Data;

@Data
public class SpriteCreated {

    private final String imagePath;
    private final int startX;
    private final int startY;
    private final int speed;
}
