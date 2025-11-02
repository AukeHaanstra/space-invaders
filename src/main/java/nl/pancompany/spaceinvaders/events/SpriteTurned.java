package nl.pancompany.spaceinvaders.events;

import lombok.Data;
import nl.pancompany.spaceinvaders.shared.Direction;

@Data
public class SpriteTurned {

    private final Direction direction;
}
