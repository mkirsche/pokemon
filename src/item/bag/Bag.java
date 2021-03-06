package item.bag;

import battle.Battle;
import battle.attack.Move;
import item.Item;
import item.ItemNamesies;
import item.use.BallItem;
import item.use.MoveUseItem;
import item.use.PlayerUseItem;
import item.use.PokemonUseItem;
import item.use.UseItem;
import main.Game;
import main.Global;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import trainer.player.Player;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class Bag implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_FAIL_MESSAGE = "It won't have any effect.";

	private final Map<ItemNamesies, Integer> items; // Item -> quantity
	private final Map<BagCategory, Set<ItemNamesies>> bag;
	private final Map<BattleBagCategory, Set<ItemNamesies>> battleBag;

	// TODO: This doesn't work for Pokemon Use Items -- it automatically selects the front Pokemon
	private ItemNamesies lastUsedItem; // Only for battle
	
	public Bag() {
		this.items = new EnumMap<>(ItemNamesies.class);
		this.bag = new EnumMap<>(BagCategory.class);
		this.battleBag = new EnumMap<>(BattleBagCategory.class);
		this.lastUsedItem = ItemNamesies.NO_ITEM;

		for (BagCategory category : BagCategory.values()) {
			this.bag.put(category, EnumSet.noneOf(ItemNamesies.class));
		}

		for (BattleBagCategory category : BattleBagCategory.values()) {
			this.battleBag.put(category, EnumSet.noneOf(ItemNamesies.class));
		}
	}
	
	public boolean giveItem(ActivePokemon p, ItemNamesies hold) {
		if (p.isEgg()) {
			Messages.add("You can't give an item to an egg!");
			return false;
		}
		
		ItemNamesies item = p.getActualHeldItem().namesies();
		System.out.println("Holding item: " + item.getName());
		if (item != ItemNamesies.NO_ITEM) {
			addItem(item);
			p.removeItem();
			Messages.add("Took the " + item.getName() + " from " + p.getActualName() + ".");
		}
		
		p.giveItem(hold);
		removeItem(hold);
		Messages.add(p.getActualName() + " is now holding " + hold.getName() + ".");
		
		return true;
	}
	
	public boolean takeItem(ActivePokemon p) {
		if (p.isEgg()) {
			Messages.add("Eggs can't hold anything. They're eggs.");
			return false;
		}
		
		ItemNamesies item = p.getActualHeldItem().namesies();
		if (item == ItemNamesies.NO_ITEM) {
			Messages.add(p.getActualName() + " is not holding anything.");
			return false;
		}

		// Remove the item from the pokemon and add it to the bag
		p.removeItem();
		addItem(item);
		Messages.add("Took the " + item.getName() + " from " + p.getActualName() + ".");
		return true;
	}
	
	public void addItem(ItemNamesies item) {
		addItem(item, 1);
	}
	
	public void addItem(ItemNamesies item, int amount) {
		// Increment the items by the amount
		if (items.containsKey(item)) {
			items.put(item, items.get(item) + amount);
		}
		else {
			items.put(item, amount);
			Item itemValue = item.getItem();
			this.getCategory(itemValue.getBagCategory()).add(item);
			for (BattleBagCategory category : itemValue.getBattleBagCategories()) {
				this.getCategory(category).add(item);
			}
		}
	}

	public Set<ItemNamesies> getCategory(BagCategory category) {
		return bag.get(category);
	}

	public Set<ItemNamesies> getCategory(BattleBagCategory category) {
		return battleBag.get(category);
	}
	
	private void removeItem(ItemNamesies item) {
		// Trying to remove nonexistent items -- bad news
		if (!items.containsKey(item) || items.get(item) <= 0) {
			Global.error("You can't remove an item you don't have! (" + item.getName() + ")");
		}

		Item itemValue = item.getItem();
		
		// Don't decrement for TMs or KeyItems
		if (!itemValue.hasQuantity()) {
			if (items.get(item) != 1) {
				Global.error("Must only have exactly quantity per TM/KeyItem");
			}
			
			return;
		}
		
		// All other items -- decrement by one
		items.put(item, items.get(item) - 1);

		// If this was the last one, remove from maps
		if (items.get(item) == 0) {
			items.remove(item);
			this.getCategory(itemValue.getBagCategory()).remove(item);
			for (BattleBagCategory category : itemValue.getBattleBagCategories()) {
				this.getCategory(category).remove(item);
			}
		}

	}
	
	public boolean useItem(ItemNamesies item) {
		Item useItem = item.getItem();
		if (useItem instanceof PlayerUseItem && ((PlayerUseItem) useItem).use()) {
			removeItem(item);
			return true;
		}
		
		return false;
	}

	// Checks conditions, add messages, and executes the UseItem
	// Move should be null for PokemonUseItem and nonnull for MoveUseItem
	private boolean useItem(ItemNamesies item, ActivePokemon p, Move move) {

		// Eggs can't do shit
		if (p.isEgg()) {
			Messages.add(DEFAULT_FAIL_MESSAGE);
			return false;
		}

		// Check if the item is an instance of the corresponding UseItem class
		Class<? extends UseItem> useClass = move == null ? PokemonUseItem.class : MoveUseItem.class;
		Item itemValue = item.getItem();
		if (!useClass.isInstance(itemValue)) {
			Messages.add(DEFAULT_FAIL_MESSAGE);
			return false;
		}

		// Try to use the item
		UseItem useItem = (UseItem) itemValue;
		final boolean success = useItem.use(null, p, move);

		// :(
		if (!success) {
			Messages.add(DEFAULT_FAIL_MESSAGE);
			return false;
		}

		// Item successfully used -- display success messages to the user and remove this item from the bag
		Messages.addToFront(Game.getPlayer().getName() + " used the " + item.getName() + "!");
		removeItem(item);
		return true;
	}

	public boolean useItem(ItemNamesies item, ActivePokemon p) {
		return this.useItem(item, p, null);
	}

	public boolean useMoveItem(ItemNamesies item, ActivePokemon p, Move move) {
		return this.useItem(item, p, move);
	}

	public boolean battleUseItem(ItemNamesies item, ActivePokemon activePokemon, Battle battle) {
		Player player = Game.getPlayer();

		Item useItem = item.getItem();
		boolean used = false;
		if (useItem instanceof BallItem) {
			used = player.catchPokemon(battle, (BallItem) useItem);
		}
		else if (useItem.isUsable()) {
			used = ((UseItem) useItem).use(battle, activePokemon, null);
			if (used && player.front() == activePokemon) {
				Messages.add(new MessageUpdate().updatePokemon(battle, activePokemon));
			}
		}

		if (used) {
			Messages.addToFront(Game.getPlayer().getName() + " used the " + item.getName() + "!");

			if (items.get(item) > 1) {
				lastUsedItem = item;
			}
			else {
				lastUsedItem = ItemNamesies.NO_ITEM;
			}
			
			removeItem(item);
		}
		else if (useItem.isUsable()) {
			Messages.add(DEFAULT_FAIL_MESSAGE);
		}

		return used;
	}

	public ItemNamesies getLastUsedItem() {
		return lastUsedItem;
	}

	public boolean hasItem(ItemNamesies item) {
		return this.getQuantity(item) > 0;
	}

	public int getQuantity(ItemNamesies item) {
		if (items.containsKey(item)) {
			return items.get(item);
		}
		
		return 0;
	}
}
