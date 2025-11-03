package nl.pancompany.spaceinvaders.game.get;

import java.util.Optional;

public record GameReadModel(boolean inGame, Optional<String> message) {

}
