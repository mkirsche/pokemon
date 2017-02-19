package map.overworld;

import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.WildPokemon;
import util.GeneralUtils;
import util.RandomUtils;

import java.util.Arrays;

// TODO: Not exactly sure if this is the best location for this class
public class WildEncounter {
    private PokemonNamesies pokemon;

    private int minLevel;
    private int maxLevel;

    private int probability;

    private Integer level;

    public WildEncounter(String pokemon, String minLevel, String maxLevel, String probability) {
        this(
                PokemonNamesies.getValueOf(pokemon),
                Integer.parseInt(minLevel),
                Integer.parseInt(maxLevel),
                Integer.parseInt(probability)
        );
    }

    public WildEncounter(PokemonNamesies pokemon, int level) {
        this(pokemon, level, level, 100);
    }

    public WildEncounter(PokemonNamesies pokemon, int minLevel, int maxLevel, int probability) {
        this.pokemon = pokemon;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.probability = probability;
    }

    public PokemonNamesies getPokemonName() {
        return this.pokemon;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getProbability() {
        return this.probability;
    }

    public int getLevel() {
        if (this.level == null) {
            this.level = RandomUtils.getRandomInt(this.minLevel, this.maxLevel);
        }

        return this.level;
    }

    public WildPokemon getWildPokemon() {
        return new WildPokemon(new ActivePokemon(this.pokemon, this.getLevel(), true, false));
    }

    public static WildEncounter getWildEncounter(WildEncounter[] wildEncounters) {
        return wildEncounters[getRandomEncounterIndex(wildEncounters)];
    }

    private static int getRandomEncounterIndex(WildEncounter[] wildEncounters) {
        return GeneralUtils.getPercentageIndex(
                Arrays.stream(wildEncounters)
                        .map(WildEncounter::getProbability)
                        .mapToInt(Integer::intValue)
                        .toArray()
        );
    }
}
