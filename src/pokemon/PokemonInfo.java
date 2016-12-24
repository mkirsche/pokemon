package pokemon;

import battle.attack.AttackNamesies;
import item.Item;
import item.ItemNamesies;
import item.hold.HoldItem;
import item.hold.IncenseItem;
import main.Global;
import main.Type;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.EggGroup;
import pokemon.evolution.Evolution;
import pokemon.evolution.EvolutionType;
import util.FileIO;
import util.FileName;
import util.GeneralUtils;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PokemonInfo implements Serializable, Comparable<PokemonInfo> {
	private static final long serialVersionUID = 1L;

	public static final int NUM_POKEMON = 719;
	public static final int EGG_IMAGE = 0x10000;

	private static Map<PokemonNamesies, PokemonInfo> map;
	private static List<PokemonNamesies> baseEvolution;
	private static Set<PokemonNamesies> incenseBabies;

	private final int number;
	private final String name;
	private final PokemonNamesies namesies;
	private final int[] baseStats;
	private final int baseExp;
	private final GrowthRate growthRate;
	private final Type[] type;
	private final Map<Integer, Set<AttackNamesies>> levelUpMoves;
	private final Set<AttackNamesies> learnableMoves;
	private final int catchRate;
	private final int[] givenEVs;
	private final Evolution evolution;
	private final List<WildHoldItem> wildHoldItems;
	private final AbilityNamesies[] abilities;
	private final int maleRatio;
	private final String classification;
	private final int height;
	private final double weight;
	private final String flavorText;
	private final int eggSteps;
	private final EggGroup[] eggGroups;

	public PokemonInfo(
			int number,
			String name,
			int[] baseStats,
			int baseExp,
			String growthRate,
			String type1,
			String type2,
			int catchRate,
			int[] givenEVs,
			Evolution evolution,
			List<WildHoldItem> wildHoldItems,
			int genderRatio,
			String ability1,
			String ability2,
			String classification,
			int height,
			double weight,
			String flavorText,
			int eggSteps,
			String eggGroup1,
			String eggGroup2,
			Map<Integer, Set<AttackNamesies>> levelUpMoves,
			Set<AttackNamesies> tmMoves,
			Set<AttackNamesies> eggMoves,
			Set<AttackNamesies> tutorMoves
	) {
		this.number = number;
		this.name = name;
		this.namesies = PokemonNamesies.getValueOf(this.name);
		this.baseStats = baseStats;
		this.baseExp = baseExp;
		this.growthRate = GrowthRate.valueOf(growthRate);
		this.type = new Type[] { Type.valueOf(type1.toUpperCase()), Type.valueOf(type2.toUpperCase()) };
		this.levelUpMoves = levelUpMoves;
		this.learnableMoves = new HashSet<>();
		this.learnableMoves.addAll(tmMoves);
		this.learnableMoves.addAll(eggMoves);
		this.learnableMoves.addAll(tutorMoves);
		this.catchRate = catchRate;
		this.givenEVs = givenEVs;
		this.evolution = evolution;
		this.wildHoldItems = wildHoldItems;
		this.abilities = new AbilityNamesies[] { AbilityNamesies.getValueOf(ability1), AbilityNamesies.getValueOf(ability2) };
		this.maleRatio = genderRatio;
		this.classification = classification;
		this.height = height;
		this.weight = weight;
		this.flavorText = flavorText;
		this.eggSteps = eggSteps;
		this.eggGroups = new EggGroup[] { EggGroup.valueOf(eggGroup1), EggGroup.valueOf(eggGroup2) };
	}

	public Type[] getType() {
		return type;
	}

	public Map<Integer, Set<AttackNamesies>> getLevelUpMoves() {
		return levelUpMoves;
	}

	public int getStat(int index) {
		return baseStats[index];
	}

	public GrowthRate getGrowthRate() {
		return growthRate;
	}

	public int getEggSteps() {
		return eggSteps;
	}

	public AbilityNamesies[] getAbilities() {
		return abilities;
	}

	public boolean hasAbility(AbilityNamesies s) {
		return abilities[0] == s || abilities[1] == s;
	}

	public int getCatchRate() {
		return catchRate;
	}

	public int getBaseEXP() {
		return baseExp;
	}

	public int[] getGivenEVs() {
		return givenEVs;
	}

	int getMaleRatio() {
		return maleRatio;
	}

	public String getHeightString() {
		return String.format("%d'%02d\"", height/12, height%12);
	}

	public double getWeight() {
		return weight;
	}

	public String getClassification() {
		return classification;
	}

	public String getFlavorText() {
		return flavorText;
	}

	public PokemonNamesies namesies() {
		return namesies;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	public Evolution getEvolution() {
		return evolution;
	}

	public List<WildHoldItem> getWildItems() {
		return wildHoldItems;
	}

	public int getImageNumber() {
		return this.getImageNumber(false, true);
	}

	public int getImageNumber(boolean shiny) {
		return this.getImageNumber(shiny, true);
	}

	public int getImageNumber(boolean shiny, boolean front) {
		return getImageNumber(this, shiny, front);
	}

	public static int getImageNumber(PokemonInfo pokemonInfo, boolean shiny, boolean front) {
		int imageNumber = 4*pokemonInfo.getNumber() + (front ? 0 : 1);
		if (shiny) {
			imageNumber += 2;
		}

		return imageNumber;
	}

	public static PokemonInfo getPokemonInfo(PokemonNamesies pokemon) {
		if (map == null) {
			loadPokemonInfo();
		}

		return map.get(pokemon);
	}

	public static PokemonInfo getPokemonInfo(int index) {
		return getPokemonInfo(PokemonNamesies.values()[index]);
	}

	public static PokemonNamesies getRandomBaseEvolution() {
		if (baseEvolution == null) {
			baseEvolution = new ArrayList<>();
			Scanner in = new Scanner(FileIO.readEntireFileWithReplacements(FileName.BASE_EVOLUTIONS, false));
			while (in.hasNext()) {
				PokemonNamesies namesies = PokemonNamesies.getValueOf(in.nextLine().trim());
				baseEvolution.add(namesies);
			}

			in.close();
		}

		return RandomUtils.getRandomValue(baseEvolution);
	}

	public int compareTo(PokemonInfo p) {
		return number - p.number;
	}

	// Create and load the Pokemon info map if it doesn't already exist
	public static void loadPokemonInfo() {
		if (map != null) {
			return;
		}

		map = new HashMap<>();

		Scanner in = new Scanner(FileIO.readEntireFileWithReplacements(FileName.POKEMON_INFO, false));
		while (in.hasNext()) {
			PokemonInfo pokemonInfo = new PokemonInfo(
					in.nextInt(),
					in.nextLine().trim() + in.nextLine().trim(),
					GeneralUtils.sixIntArray(in),
					in.nextInt(),
					in.nextLine().trim() + in.nextLine().trim(),
					in.next(),
					in.next(),
					in.nextInt(),
					GeneralUtils.sixIntArray(in),
					EvolutionType.getEvolution(in),
					WildHoldItem.createList(in),
					in.nextInt(),
					in.nextLine().trim() + in.nextLine().trim(),
					in.nextLine().trim(),
					in.nextLine().trim(), in.nextInt(),
					in.nextDouble(),
					in.nextLine().trim(),
					in.nextInt(),
					in.next(),
					in.next(),
					createLevelUpMoves(in),
					createMovesSet(in),
					createMovesSet(in),
					createMovesSet(in)
			);

			map.put(pokemonInfo.namesies, pokemonInfo);
		}

		in.close();
	}

	private static Map<Integer, Set<AttackNamesies>> createLevelUpMoves(Scanner in) {
		Map<Integer, Set<AttackNamesies>> levelUpMoves = new TreeMap<>();
		int numMoves = in.nextInt();

		for (int i = 0; i < numMoves; i++) {
			int level = in.nextInt();
			if (!levelUpMoves.containsKey(level)) {
				levelUpMoves.put(level, new TreeSet<>());
			}

			String attackName = in.nextLine().trim();
			AttackNamesies namesies = AttackNamesies.getValueOf(attackName);

			if (level < 0 || level > ActivePokemon.MAX_LEVEL) {
				Global.error("Invalid level " + level + " (Move: " + attackName + ")");
			}

			levelUpMoves.get(level).add(namesies);
		}

		return levelUpMoves;
	}

	private static Set<AttackNamesies> createMovesSet(Scanner in) {
		Set<AttackNamesies> tmMoves = new HashSet<>();
		int numMoves = in.nextInt();
		in.nextLine();

		for (int i = 0; i < numMoves; i++) {
			String attackName = in.nextLine().trim();

			AttackNamesies namesies = AttackNamesies.getValueOf(attackName);
			tmMoves.add(namesies);
		}

		return tmMoves;
	}

	public Set<AttackNamesies> getMoves(int level) {
		if (levelUpMoves.containsKey(level)) {
			return levelUpMoves.get(level);
		}

		return new TreeSet<>();
	}

	public boolean canBreed() {
		return eggGroups[0] != EggGroup.UNDISCOVERED;
	}

	// TODO: new file
	public static class WildHoldItem implements Serializable {
		private static final long serialVersionUID = 1L;

		private HoldItem item;
		private int chance;
		
		public WildHoldItem(int chance, String itemName) {
			item = (HoldItem) ItemNamesies.getValueOf(itemName).getItem();
			this.chance = chance;
		}
		
		public static List<WildHoldItem> createList(Scanner in) {
			List<WildHoldItem> list = new ArrayList<>();
			int num = in.nextInt();
			for (int i = 0; i < num; i++) list.add(new WildHoldItem(in.nextInt(), in.nextLine().trim()));
			return list;
		}
		
		public static HoldItem getWildHoldItem(List<WildHoldItem> list) {
			int random = RandomUtils.getRandomInt(100);
			int sum = 0;

			for (WildHoldItem i : list) {
				sum += i.chance;
				if (random < sum) {
					return i.item;
				}
			}
			
			return (HoldItem)ItemNamesies.NO_ITEM.getItem();
		}
	}
	
	public PokemonInfo getBaseEvolution() {
		return getBaseEvolution(this);
	}

	// TODO: Instead of generating this on the fly should just be added to the text file and stored
	public static PokemonInfo getBaseEvolution(PokemonInfo targetPokes) {
		if (targetPokes.namesies() == PokemonNamesies.MANAPHY) {
			return PokemonInfo.getPokemonInfo(PokemonNamesies.PHIONE);
		}

		Set<PokemonNamesies> allPokes = map.keySet();
		while (true) {
			boolean changed = false;
			for (PokemonNamesies pokesName : allPokes) {
				PokemonInfo pokes = map.get(pokesName);
				PokemonNamesies[] evolutionNamesies = pokes.getEvolution().getEvolutions();
				for (PokemonNamesies namesies : evolutionNamesies) {
					if (namesies == targetPokes.namesies()) {
						targetPokes = pokes;
						changed = true;
						break;
					}
				}
				
				if (changed) {
					break;
				}
			}
			
			if (!changed) {
				return targetPokes;
			}
		}
	}
	
	public EggGroup[] getEggGroups() {
		return eggGroups;
	}

	private static void loadIncenseBabies() {
		incenseBabies = new HashSet<>();
		for (ItemNamesies itemNamesies : ItemNamesies.values()) {
			Item item = itemNamesies.getItem();
			if (item instanceof IncenseItem) {
				incenseBabies.add(((IncenseItem)item).getBaby());
			}
		}
	}
	
	public boolean isIncenseBaby() {
		if (incenseBabies == null) {
			loadIncenseBabies();
		}

		return incenseBabies.contains(namesies);
	}
	
	// Returns what level the Pokemon will learn the given attack, returns -1 if they cannot learn it by level up
	public int levelLearned(AttackNamesies attack) {
		for (Integer level : getLevelUpMoves().keySet()) {
			for (AttackNamesies levelUpMove : getLevelUpMoves().get(level)) {
				if (attack == levelUpMove) {
					return level;
				}
			}
		}
		
		return -1;
	}
	
	public boolean canLearnMove(AttackNamesies attack) {
		return levelLearned(attack) != -1 || canLearnByBreeding(attack);
	}

	public boolean canLearnByBreeding(AttackNamesies attack) {
		return this.learnableMoves.contains(attack);
	}
}
