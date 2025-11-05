package nl.pancompany.spaceinvaders.game.get;

import java.util.Optional;

public record GameReadModel(boolean inGame, boolean replay, Optional<String> message) {

}
