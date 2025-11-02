package nl.pancompany.spaceinvaders.shared;

import lombok.Data;

@Data
public class RegisterSpriteCreated {

    private final String imagePath;
    private final int startX;
    private final int startY;
    private final int speed;
    private final Direction direction;
}
