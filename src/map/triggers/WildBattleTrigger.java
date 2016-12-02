package map.triggers;

import battle.Battle;
import battle.effect.RepellingEffect;
import item.Item;
import main.Game;
import main.Global;
import map.EncounterRate;
import map.WildEncounter;
import pokemon.PokemonNamesies;
import pattern.map.WildBattleMatcher;
import pokemon.ActivePokemon;
import trainer.CharacterData;
import trainer.WildPokemon;
import util.JsonUtils;
import util.RandomUtils;

class WildBattleTrigger extends Trigger {

	private final WildEncounter[] wildEncounters;
	private final EncounterRate encounterRate;

	WildBattleTrigger(String matcherJson, String condition) {
		super(TriggerType.WILD_BATTLE, matcherJson, condition);

		WildBattleMatcher matcher = JsonUtils.deserialize(matcherJson, WildBattleMatcher.class);
		this.wildEncounters = matcher.getWildEncounters();
		this.encounterRate = matcher.getEncounterRate();

		int totalProbability = 0;
		for (WildEncounter wildEncounter : this.wildEncounters) {
			totalProbability = totalProbability + wildEncounter.getProbability();
		}
		
		if (totalProbability != 100) {
			Global.error("Wild battle trigger probabilities add up to " + totalProbability + ", not 100.");
		}
	}
	
	protected void executeTrigger() {
		// TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
		double rand = Math.random()*187.5/encounterRate.getRate();
				
		if (rand < 1) {
			CharacterData player = Game.getPlayer();
			WildPokemon wildPokemon = getWildPokemon();

			// TODO: This should be in a separate method
			// Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
			if (player.front().getLevel() >= wildPokemon.front().getLevel()) {
				if (player.isUsingRepel()) {
					return;
				}

				// TODO: Make the chance method return an int instead of a double
				Item item = player.front().getActualHeldItem();
				if (item instanceof RepellingEffect && RandomUtils.chanceTest((int)(100*((RepellingEffect)item).chance()))) {
					return;
				}
			}

			boolean seenWildPokemon = player.getPokedex().isNotSeen(wildPokemon.front().getPokemonInfo().namesies());
			
			// Let the battle begin!!
			Battle battle = new Battle(wildPokemon);
			Game.setBattleViews(battle, seenWildPokemon);
		}
	}

	private WildPokemon getWildPokemon() {
		final WildPokemon legendaryEncounter = this.getLegendaryEncounter();
		if (legendaryEncounter != null) {
			return legendaryEncounter;
		}

		return this.wildEncounters[getRandomEncounterIndex()].getWildPokemon();
	}
	
	// Returns a legendary encounter if applicable and null otherwise
	private WildPokemon getLegendaryEncounter() {
		if (RandomUtils.chanceTest(1, 1024) && !Game.getPlayer().getPokedex().isCaught(PokemonNamesies.MEW)) {
			return new WildPokemon(new ActivePokemon(PokemonNamesies.MEW, 5, true, false));
		}
		
		return null;
	}

	// TODO: I think there might be a method in Global that does something like this already if not try and make one and include the wild hold items also
	private int getRandomEncounterIndex() {
		int sum = 0, random = RandomUtils.getRandomInt(100);
		for (int i = 0; i < wildEncounters.length; i++) {
			sum += wildEncounters[i].getProbability();

			if (random < sum) {
				return i;
			}
		}

		Global.error("Probabilities don't add to 100 for wild battle trigger.");
		return -1;
	}
}
