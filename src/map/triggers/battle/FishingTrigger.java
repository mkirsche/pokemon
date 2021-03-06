package map.triggers.battle;

import main.Game;
import map.condition.Condition;
import map.overworld.OverworldTool;
import map.overworld.WildEncounter;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import pattern.map.FishingMatcher;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;
import util.RandomUtils;
import util.SerializationUtils;

public class FishingTrigger extends Trigger {
    public static final String FISHING_GLOBAL = "isFishing";

    private final WildEncounter[] wildEncounters;

    public FishingTrigger(String matcherJson, String condition) {
        super(TriggerType.FISHING, matcherJson, Condition.and(condition, OverworldTool.FISH.getGlobalName()));

        FishingMatcher matcher = SerializationUtils.deserializeJson(matcherJson, FishingMatcher.class);
        this.wildEncounters = matcher.getWildEncounters();
    }

    protected void executeTrigger() {
        ActivePokemon front = Game.getPlayer().front();
        int chance = front.hasAbility(AbilityNamesies.SUCTION_CUPS) || front.hasAbility(AbilityNamesies.STICKY_HOLD)
                ? 75 // I made up this number since I couldn't find it
                : 50;

        if (RandomUtils.chanceTest(chance)) {
            WildEncounter wildPokemon = WildEncounter.getWildEncounter(this.wildEncounters);
            String pokemonJson = SerializationUtils.getJson(wildPokemon);

            GroupTriggerMatcher matcher = new GroupTriggerMatcher(
                    "FishingBite_" + pokemonJson,
                    TriggerType.DIALOGUE.createTrigger("Oh! A bite!", null).getName(),
                    TriggerType.GLOBAL.createTrigger(FISHING_GLOBAL, null).getName(),
                    TriggerType.WILD_BATTLE.createTrigger(pokemonJson, null).getName(),
                    TriggerType.GLOBAL.createTrigger("!" + FISHING_GLOBAL, null).getName()
            );

            Trigger group = TriggerType.GROUP.createTrigger(SerializationUtils.getJson(matcher), null);
            Messages.add(new MessageUpdate().withTrigger(group.getName()));
        }
        else {
            Messages.add(new MessageUpdate().withTrigger(TriggerType.DIALOGUE.createTrigger("No dice.", null).getName()));
        }
    }
}
