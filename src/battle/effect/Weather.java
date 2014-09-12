package battle.effect;

import item.Item;

import java.util.HashMap;

import main.Global;
import main.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import battle.Battle;

public abstract class Weather extends BattleEffect implements EndTurnEffect 
{
	private static final long serialVersionUID = 1L;
	private static HashMap<String, Weather> map;
	
	protected Type weatherElement;
	
	public Weather(Namesies namesies, Type weather)
	{
		super(namesies, -1, -1, true);
		weatherElement = weather;
	}
	
	public Type getElement()
	{
		return weatherElement;
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast)
	{
		super.cast(b, caster, victim, source, printCast);
		b.getWeather().setTurns(getTurns(b, caster));
	}
	
	private int getTurns(Battle b, ActivePokemon caster)
	{
		Item i = caster.getHeldItem(b);
		if (i instanceof WeatherExtendingEffect && this.namesies == ((WeatherExtendingEffect)i).getWeatherType()) 
		{
			return 8;
		}
		
		return 5;
	}
	
	protected void buffet(Battle b, ActivePokemon p, Type[] immunees, String message)
	{
		for (Type type : immunees)
			if (p.isType(b, type))
				return;
		
		Object[] list = b.getEffectsList(p);
		Object checkeroo = Global.checkInvoke(true, list, WeatherBlockerEffect.class, "block", weatherElement);
		if (checkeroo != null)
		{
			return;
		}

		// Buffety buffety buffet
		b.addMessage(message);
		p.reduceHealthFraction(b, 1/16.0);
	}
	
	public abstract Weather newInstance();
	
	public static Weather getEffect(Namesies name)
	{
		String e = name.getName();
		
		if (map == null) 
		{
			loadEffects();
		}
		
		if (map.containsKey(e))
		{
			return map.get(e);
		}
	
		Global.error("No such Effect " + e);
		return null;
	}

	// Create and load the effects map if it doesn't already exist
	public static void loadEffects()
	{
		if (map != null) 
		{
			return;
		}
		
		map = new HashMap<>();
		
		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("ClearSkies", new ClearSkies());
		map.put("Raining", new Raining());
		map.put("Sunny", new Sunny());
		map.put("Sandstorm", new Sandstorm());
		map.put("Hailing", new Hailing());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class ClearSkies extends Weather 
	{
		private static final long serialVersionUID = 1L;

		public ClearSkies()
		{
			super(Namesies.CLEAR_SKIES_EFFECT, Type.NORMAL);
		}

		public ClearSkies newInstance()
		{
			return (ClearSkies)(new ClearSkies().activate());
		}

		public void apply(ActivePokemon victim, Battle b)
		{
		}
	}

	private static class Raining extends Weather implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Raining()
		{
			super(Namesies.RAINING_EFFECT, Type.WATER);
		}

		public Raining newInstance()
		{
			return (Raining)(new Raining().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "It started to rain!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The rain stopped.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			b.addMessage("The rain continues to pour.");
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK)
			{
				Type t = p.getAttack().getType(b, p);
				if (t == Type.WATER) return (int)(stat*1.5);
				if (t == Type.FIRE) return (int)(stat*.5);
			}
			
			return stat;
		}
	}

	private static class Sunny extends Weather implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;

		public Sunny()
		{
			super(Namesies.SUNNY_EFFECT, Type.FIRE);
		}

		public Sunny newInstance()
		{
			return (Sunny)(new Sunny().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "The sunlight turned harsh!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The sunlight faded.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			b.addMessage("The sunlight is strong.");
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			if (s == Stat.ATTACK || s == Stat.SP_ATTACK)
			{
				Type t = p.getAttack().getType(b, p);
				
				// Fire is fiddy percent stronger in tha sun
				if (t == Type.FIRE)
				{
					stat *= 1.5;
				}
				
				// Water is fiddy percent weaker in tha sun
				if (t == Type.WATER)
				{
					stat *= .5;
				}
			}
			
			return stat;
		}
	}

	private static class Sandstorm extends Weather implements StatChangingEffect
	{
		private static final long serialVersionUID = 1L;
		private static Type[] immunees = new Type[] {Type.ROCK, Type.GROUND, Type.STEEL};
		
		private String getBuffetMessage(ActivePokemon p)
		{
			return p.getName() + " is buffeted by the sandstorm!";
		}

		public Sandstorm()
		{
			super(Namesies.SANDSTORM_EFFECT, Type.ROCK);
		}

		public Sandstorm newInstance()
		{
			return (Sandstorm)(new Sandstorm().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "A sandstorm kicked up!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The sandstorm subsided.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			b.addMessage("The sandstorm rages.");
			
			ActivePokemon other = b.getOtherPokemon(victim.user());
			buffet(b, victim, immunees, getBuffetMessage(victim));
			buffet(b, other, immunees, getBuffetMessage(other));
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b)
		{
			int stat = statValue;
			return (int)(stat*(s == Stat.SP_DEFENSE && p.isType(b, Type.ROCK) ? 1.5 : 1));
		}
	}

	private static class Hailing extends Weather 
	{
		private static final long serialVersionUID = 1L;
		private static Type[] immunees = new Type[] {Type.ICE};
		
		private String getBuffetMessage(ActivePokemon p)
		{
			return p.getName() + " is buffeted by the hail!";
		}

		public Hailing()
		{
			super(Namesies.HAILING_EFFECT, Type.ICE);
		}

		public Hailing newInstance()
		{
			return (Hailing)(new Hailing().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source)
		{
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			return "It started to hail!";
		}

		public String getSubsideMessage(ActivePokemon victim)
		{
			return "The hail stopped.";
		}

		public void apply(ActivePokemon victim, Battle b)
		{
			b.addMessage("The hail continues to fall.");
			
			ActivePokemon other = b.getOtherPokemon(victim.user());
			buffet(b, victim, immunees, getBuffetMessage(victim));
			buffet(b, other, immunees, getBuffetMessage(other));
		}
	}
}
