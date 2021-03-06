package map.triggers.battle;

import battle.effect.generic.EffectInterfaces.EncounterRateMultiplier;
import battle.effect.generic.EffectInterfaces.RepellingEffect;
import main.Game;
import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pattern.map.WildBattleMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.player.Player;
import util.RandomUtils;
import util.SerializationUtils;

public class WalkingWildBattleTrigger extends Trigger {
    private final WildEncounter[] wildEncounters;
    private final EncounterRate encounterRate;

    public WalkingWildBattleTrigger(String matcherJson, String condition) {
        super(TriggerType.WALKING_WILD_BATTLE, matcherJson, condition);

        WildBattleMatcher matcher = SerializationUtils.deserializeJson(matcherJson, WildBattleMatcher.class);
        this.wildEncounters = matcher.getWildEncounters();
        this.encounterRate = matcher.getEncounterRate();
    }

    protected void executeTrigger() {
        Player player = Game.getPlayer();
        ActivePokemon front = player.front();

        // TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
        double rand = Math.random()*187.5/encounterRate.getRate()*EncounterRateMultiplier.getModifier(front);
        if (rand < 1) {
            WildEncounter wildPokemon = getWildEncounter();

            // Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
            if ((wildPokemon.getLevel() <= front.getLevel() && player.getRepelInfo().isUsingRepel())
                    || RepellingEffect.checkRepellingEffect(front, wildPokemon)) {
                return;
            }

            Trigger wildBattle = TriggerType.WILD_BATTLE.createTrigger(SerializationUtils.getJson(wildPokemon), null);
            Messages.add(new MessageUpdate().withTrigger(wildBattle.getName()));
        }
    }

    private WildEncounter getWildEncounter() {
        final WildEncounter legendaryEncounter = this.getLegendaryEncounter();
        if (legendaryEncounter != null) {
            return legendaryEncounter;
        }

        return WildEncounter.getWildEncounter(this.wildEncounters);
    }

    // Returns a legendary encounter if applicable and null otherwise
    private WildEncounter getLegendaryEncounter() {
        if (RandomUtils.chanceTest(1, 1024) && !Game.getPlayer().getPokedex().isCaught(PokemonNamesies.MEW)) {
            return new WildEncounter(PokemonNamesies.MEW, 5);
        }

        return null;
    }
}
