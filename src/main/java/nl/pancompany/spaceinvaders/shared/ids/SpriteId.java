package nl.pancompany.spaceinvaders.shared.ids;

public record SpriteId(String raw) {

    public static SpriteId of(String raw) {
        return new SpriteId(raw);
    }

    @Override
    public String toString() { // used for tag value
        return raw;
    }
}
