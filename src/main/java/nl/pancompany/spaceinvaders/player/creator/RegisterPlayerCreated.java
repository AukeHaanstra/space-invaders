package nl.pancompany.spaceinvaders.player.creator;

import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.RegisterSpriteCreated;

public class RegisterPlayerCreated extends RegisterSpriteCreated {

    public RegisterPlayerCreated(String imagePath, int startX, int startY, int speed, Direction direction) {
        super(imagePath, startX, startY, speed, direction);
    }
}
