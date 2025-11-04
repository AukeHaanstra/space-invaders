package nl.pancompany.spaceinvaders.sprite.get;

import nl.pancompany.spaceinvaders.shared.ReadModelNotFoundException;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

public class SpriteRepository {

    Map<SpriteId, SpriteReadModel> sprites = new ConcurrentHashMap<>();
    Map<String, Set<SpriteId>> spriteIdsByEntityName = new ConcurrentHashMap<>();

    public Optional<SpriteReadModel> findById(SpriteId id) {
        return Optional.ofNullable(sprites.get(id));
    }

    public List<SpriteReadModel> findAllByEntityName(String entityName) {
        return spriteIdsByEntityName.get(entityName).stream()
                .map(spriteId -> sprites.get(spriteId))
                .toList();
    }

    public SpriteReadModel findByIdOrThrow(SpriteId id) {
        return findById(id).orElseThrow(() -> new ReadModelNotFoundException(format("Sprite with id %s not found.", id)));
    }

    public void save(SpriteReadModel sprite) {
        sprites.put(sprite.spriteId(), sprite);
        spriteIdsByEntityName.computeIfAbsent(sprite.entityName(), entityName -> new HashSet<>())
                .add(sprite.spriteId());
    }

    public void deleteAll() {
        sprites.clear();
        spriteIdsByEntityName.clear();
    }
}
