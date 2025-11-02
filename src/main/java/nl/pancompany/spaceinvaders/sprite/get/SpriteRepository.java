package nl.pancompany.spaceinvaders.sprite.get;

import nl.pancompany.spaceinvaders.shared.ReadModelNotFoundException;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.HashMap;
import java.util.Optional;

import static java.lang.String.format;

public class SpriteRepository {

    HashMap<SpriteId, SpriteReadModel> sprites = new HashMap<>();

    public Optional<SpriteReadModel> findById(SpriteId id) {
        return Optional.ofNullable(sprites.get(id));
    }

    public SpriteReadModel findByIdOrThrow(SpriteId id) {
        return findById(id).orElseThrow(() -> new ReadModelNotFoundException(format("Sprite with id %s not found.", id)));
    }

    public void save(SpriteReadModel sprite) {
        sprites.put(sprite.spriteId(), sprite);
    }

}
