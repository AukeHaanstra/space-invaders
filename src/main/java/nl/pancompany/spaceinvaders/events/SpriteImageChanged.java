package nl.pancompany.spaceinvaders.events;

import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record SpriteImageChanged(SpriteId id, String imagePath) {
}
