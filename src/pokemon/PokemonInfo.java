package pokemon;

import battle.attack.AttackNamesies;
import item.Item;
import item.ItemNamesies;
import item.hold.IncenseItem;
import map.overworld.WildHoldItem;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.EggGroup;
import pokemon.evolution.Evolution;
import pokemon.evolution.EvolutionType;
import type.Type;
import util.FileIO;
import util.FileName;
import util.GeneralUtils;
import util.RandomUtils;
import util.StringUtils;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class PokemonInfo implements Serializable, Comparable<PokemonInfo> {
	private static final long serialVersionUID = 1L;

	public static final int NUM_POKEMON = 817;
	public static final int EVOLUTION_LEVEL_LEARNED = -1;

	private static Map<Integer, PokemonInfo> map;
	private static Set<PokemonNamesies> incenseBabies;

	private final int number;
	private final String name;
	private final PokemonNamesies namesies;
	private final int[] baseStats;
	private final int baseExp;
	private final GrowthRate growthRate;
	private final Type[] type;
	private final List<Entry<Integer, AttackNamesies>> levelUpMoves;
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
			List<Type> type,
			int catchRate,
			int[] givenEVs,
			Evolution evolution,
			List<WildHoldItem> wildHoldItems,
			int genderRatio,
			List<AbilityNamesies> abilities,
			String classification,
			int height,
			double weight,
			String flavorText,
			int eggSteps,
			List<EggGroup> eggGroups,
			List<Entry<Integer, AttackNamesies>> levelUpMoves,
			Set<AttackNamesies> learnableMoves
	) {
		this.number = number;
		this.name = name;
		this.namesies = PokemonNamesies.getValueOf(this.name);
		this.baseStats = baseStats;
		this.baseExp = baseExp;
		this.growthRate = GrowthRate.valueOf(growthRate);
		this.type = type.toArray(new Type[0]); // TODO: Test size == 2
		this.levelUpMoves = levelUpMoves;
		this.learnableMoves = new HashSet<>(learnableMoves);
		this.catchRate = catchRate;
		this.givenEVs = givenEVs;
		this.evolution = evolution;
		this.wildHoldItems = wildHoldItems;
		this.abilities = abilities.toArray(new AbilityNamesies[0]);
		this.maleRatio = genderRatio;
		this.classification = classification;
		this.height = height;
		this.weight = weight;
		this.flavorText = flavorText;
		this.eggSteps = eggSteps;
		this.eggGroups = eggGroups.toArray(new EggGroup[0]);
	}

	public boolean isType(Type type) {
		return this.type[0] == type || this.type[1] == type;
	}

	public Type[] getType() {
		return type;
	}

	public List<Entry<Integer, AttackNamesies>> getLevelUpMoves() {
		return levelUpMoves;
	}

	public int getStat(int index) {
		return baseStats[index];
	}

	GrowthRate getGrowthRate() {
		return growthRate;
	}

	public int getEggSteps() {
		return eggSteps;
	}

	public String getAbilitiesString() {
		return abilities[0].getName() + (abilities[1] == AbilityNamesies.NO_ABILITY
						? StringUtils.empty()
						: ", " + abilities[1].getName());
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

	public int getGivenEV(int index) {
		return givenEVs[index];
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

	public String getBaseImageName() {
		return String.format("%03d", number);
	}

	public String getTinyImageName() {
		return this.getBaseImageName() + "-small";
	}

	public Evolution getEvolution() {
		return evolution;
	}

	public List<WildHoldItem> getWildItems() {
		return wildHoldItems;
	}

	public String getImageName() {
		return this.getImageName(false, true);
	}

	public String getImageName(boolean shiny) {
		return this.getImageName(shiny, true);
	}

	public String getImageName(boolean shiny, boolean front) {
		return getImageName(this, shiny, front, false);
	}

	public String getImageName(boolean shiny, boolean front, boolean form) {
		return getImageName(this, shiny, front, form);
	}

	private static String getImageName(PokemonInfo pokemonInfo, boolean shiny, boolean front, boolean form) {
		String imageName = pokemonInfo.getBaseImageName();
		if (form) {
			imageName += "b";
		}
		if (shiny) {
			imageName += "-shiny";
		}
		if (!front) {
			imageName += "-back";
		}

		return imageName;
	}

	public static PokemonInfo getPokemonInfo(PokemonNamesies pokemon) {
		return getPokemonInfo(pokemon.ordinal());
	}

	public static PokemonInfo getPokemonInfo(int index) {
		if (map == null) {
			loadPokemonInfo();
		}

		return map.get(index);
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
					in.nextInt(),									// Num
					in.nextLine().trim() + in.nextLine().trim(),	// Name
					GeneralUtils.sixIntArray(in),					// Base Stats
					in.nextInt(),									// Base EXP
					in.nextLine().trim() + in.nextLine().trim(),	// Growth Rate
					createEnumList(in, Type.class),  				// Type
					in.nextInt(),									// Catch Rate
					GeneralUtils.sixIntArray(in),					// EVs
					EvolutionType.getEvolution(in),					// Evolution
					WildHoldItem.createList(in),					// Wild Items
					Integer.parseInt(in.nextLine()),				// Male Ratio
					createEnumList(in, AbilityNamesies.class), 		// Abilities
					in.nextLine().trim(),							// Classification
					in.nextInt(),									// Height
					in.nextDouble(),								// Weight
					in.nextLine().trim(),							// Flavor Text
					Integer.parseInt(in.nextLine()),				// Egg Steps
					createEnumList(in, EggGroup.class),  			// Egg Groups
					createLevelUpMoves(in),							// Level Up Moves
					createMovesSet(in)								// Learnable Moves
			);

			map.put(pokemonInfo.getNumber(), pokemonInfo);
		}

		in.close();
	}

	private static <T extends Enum<T>> List<T> createEnumList(Scanner in, Class<T> enumType) {
		return GeneralUtils.arrayValueOf(enumType, in.nextLine().trim().split(" "));
	}

	private static List<Entry<Integer, AttackNamesies>> createLevelUpMoves(Scanner in) {
		List<Entry<Integer, AttackNamesies>> levelUpMoves = new ArrayList<>();
		int numMoves = in.nextInt();

		for (int i = 0; i < numMoves; i++) {
			int level = in.nextInt();
			String attackName = in.nextLine().trim();
			AttackNamesies namesies = AttackNamesies.valueOf(attackName);

			levelUpMoves.add(new SimpleEntry<>(level, namesies));

			// TODO: Test case for this but include -1 for evolution level
			// TODO: Test that these are always in numerical order
//			if (level < 0 || level > ActivePokemon.MAX_LEVEL) {
//				Global.error("Invalid level " + level + " (Move: " + attackName + ")");
//			}
		}

		return levelUpMoves;
	}

	private static Set<AttackNamesies> createMovesSet(Scanner in) {
		Set<AttackNamesies> tmMoves = new HashSet<>();
		int numMoves = in.nextInt();
		in.nextLine();

		for (int i = 0; i < numMoves; i++) {
			String attackName = in.nextLine().trim();

			AttackNamesies namesies = AttackNamesies.valueOf(attackName);
			tmMoves.add(namesies);
		}

		return tmMoves;
	}

	public List<AttackNamesies> getMoves(int level) {
		return levelUpMoves.stream()
				.filter(entry -> entry.getKey() == level)
				.map(Entry::getValue)
				.collect(Collectors.toList());
	}

	public boolean canBreed() {
		return eggGroups[0] != EggGroup.UNDISCOVERED;
	}

	public PokemonInfo getBaseEvolution() {
		return getBaseEvolution(this);
	}

	// TODO: Instead of generating this on the fly should just be added to the text file and stored
	private static PokemonInfo getBaseEvolution(PokemonInfo targetPokes) {
		if (targetPokes.namesies() == PokemonNamesies.MANAPHY) {
			return PokemonInfo.getPokemonInfo(PokemonNamesies.PHIONE);
		}

		if (targetPokes.namesies() == PokemonNamesies.SHEDINJA) {
			return PokemonInfo.getPokemonInfo(PokemonNamesies.NINCADA);
		}

		Set<PokemonNamesies> allPokes = EnumSet.complementOf(EnumSet.of(PokemonNamesies.NONE));

		while (true) {
			boolean changed = false;
			for (PokemonNamesies pokesName : allPokes) {
				PokemonInfo pokes = map.get(pokesName.ordinal());
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

	// Returns what level the Pokemon will learn the given attack, returns null if they cannot learn it by level up
	public Integer levelLearned(AttackNamesies attack) {
		for (Entry<Integer, AttackNamesies> entry : this.levelUpMoves) {
			if (entry.getValue() == attack) {
				return entry.getKey();
			}
		}

		return null;
	}

	public boolean canLearnMove(AttackNamesies attack) {
		return levelLearned(attack) != null || canLearnByBreeding(attack);
	}

	public boolean canLearnByBreeding(AttackNamesies attack) {
		return this.learnableMoves.contains(attack);
	}

	static PokemonNamesies getRandomStarterPokemon() {
		return RandomUtils.getRandomValue(starterPokemon);
	}

	// All starters
	private static final PokemonNamesies[] starterPokemon = new PokemonNamesies[] {
			PokemonNamesies.BULBASAUR,
			PokemonNamesies.CHARMANDER,
			PokemonNamesies.SQUIRTLE,
			PokemonNamesies.CHIKORITA,
			PokemonNamesies.CYNDAQUIL,
			PokemonNamesies.TOTODILE,
			PokemonNamesies.TREECKO,
			PokemonNamesies.TORCHIC,
			PokemonNamesies.MUDKIP,
			PokemonNamesies.TURTWIG,
			PokemonNamesies.CHIMCHAR,
			PokemonNamesies.PIPLUP,
			PokemonNamesies.SNIVY,
			PokemonNamesies.TEPIG,
			PokemonNamesies.OSHAWOTT,
			PokemonNamesies.CHESPIN,
			PokemonNamesies.FENNEKIN,
			PokemonNamesies.FROAKIE,
			PokemonNamesies.ROWLET,
			PokemonNamesies.LITTEN,
			PokemonNamesies.POPPLIO
	};
}
