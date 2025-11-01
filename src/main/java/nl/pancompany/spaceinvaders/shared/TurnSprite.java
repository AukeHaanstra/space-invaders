package nl.pancompany.spaceinvaders.shared;

import lombok.Data;
import nl.pancompany.spaceinvaders.events.SpriteTurned.TurnDirection;

@Data
public class TurnSprite {

    private final TurnDirection turnDirection;
}
