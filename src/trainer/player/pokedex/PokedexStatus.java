package trainer.player.pokedex;

import java.io.Serializable;

enum PokedexStatus implements Serializable {
    NOT_SEEN(0),
    SEEN(1),
    CAUGHT(2);

    private final int weight;

    PokedexStatus(int weight) {
        this.weight = weight;
    }

    int getWeight() {
        return weight;
    }
}
