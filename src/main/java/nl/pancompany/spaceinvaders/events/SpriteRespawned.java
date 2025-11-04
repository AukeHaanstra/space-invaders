package nl.pancompany.spaceinvaders.events;

import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

public record SpriteRespawned(SpriteId id, int startX, int startY) {
}
