package gui.view;

import gui.GameData;
import gui.TileSet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.InputControl;
import main.InputControl.Control;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.PokemonInfo;
import trainer.CharacterData;
import trainer.Pokedex.PokedexStatus;

public class EvolutionView extends View
{	
	private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
	private int animationEvolve;

	private CharacterData player;
	private ActivePokemon evolvingPokemon;
	private PokemonInfo preEvolution;
	private PokemonInfo postEvolution;
	
	private boolean isEgg;
	
	private State state;
	
	private String message;
		
	private enum State
	{
		START, EVOLVE, END
	}

	public EvolutionView(CharacterData data)
	{
		player = data;
	}
	
	public void update(int dt, InputControl input, Game game)
	{
		switch (state)
		{
			case START:
				if(message != null)
				{
					if (input.mouseDown)
					{
						input.consumeMousePress();
						message = null;
					}
					if (input.isDown(Control.SPACE))
					{
						input.consumeKey(Control.SPACE);
						message = null;
					}
				}
				else
				{
					state = State.EVOLVE;
				}
				break;
			case EVOLVE:
				if(animationEvolve < 0)
				{
					state = State.END;
					setFinalMessage();
					addToPokedex();
				}
				break;
			case END:
				if(message != null)
				{
					if (input.mouseDown)
					{
						input.consumeMousePress();
						message = null;
					}
					if (input.isDown(Control.SPACE))
					{
						input.consumeKey(Control.SPACE);
						message = null;
					}
				}
				else
				{
					if(!isEgg)
					{
						evolvingPokemon.evolve(null, player.evolution);
						game.setViewMode(ViewMode.BAG_VIEW);
					}
					else
					{
						game.setViewMode(ViewMode.MAP_VIEW);	
					}
				}
				break;
		}
		
	}

	public void draw(Graphics g, GameData data)
	{
		TileSet tiles = data.getMenuTiles();
		TileSet battleTiles = data.getBattleTiles();
		TileSet pokemonTiles = data.getPokemonTilesMedium();
		
		g.drawImage(tiles.getTile(0x2), 0, 0, null);
		
		g.setFont(Global.getFont(30));
		g.setColor(Color.WHITE);
		
		int preIndex = isEgg ? 0x10000 : preEvolution.getImageNumber(evolvingPokemon.isShiny());
		int postIndex = isEgg ? preEvolution.getImageNumber(evolvingPokemon.isShiny()) : postEvolution.getImageNumber(evolvingPokemon.isShiny());
		
		BufferedImage currEvolution = pokemonTiles.getTile(preIndex);
		BufferedImage nextEvolution = pokemonTiles.getTile(postIndex);
		
		int drawWidth = Global.GAME_SIZE.width/2;
		int drawHeight = Global.GAME_SIZE.height*5/8;
		
		switch (state)
		{
			case START:
				g.drawImage(currEvolution, drawWidth-currEvolution.getWidth()/2, drawHeight-currEvolution.getHeight(), null);
				break;
			case EVOLVE:
				evolveAnimation(g, currEvolution, nextEvolution, drawWidth, drawHeight);
				break;
			case END:
				g.drawImage(nextEvolution, drawWidth-nextEvolution.getWidth()/2, drawHeight-nextEvolution.getHeight(), null);
				break;
		}
		
		if (message != null)
		{
			g.drawImage(battleTiles.getTile(0x3), 0, 440, null);
			Global.drawWrappedText(g, message, 30, 490, 750);
		}
	}
	
	private void evolveAnimation(Graphics g, BufferedImage currEvolution, BufferedImage nextEvolution, int px, int py)
	{
		Graphics2D g2d = (Graphics2D)g;
		
		float[] prevEvolutionScales = { 1f, 1f, 1f, 1f };
		float[] prevEvolutionOffsets = { 255f, 255f, 255f, 0f };
		float[] evolutionScales = { 1f, 1f, 1f, 1f };
		float[] evolutionOffsets = { 255f, 255f, 255f, 0f };
		
		// Turn white
		if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.7)
		{
			prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255*(1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.7f)/(EVOLVE_ANIMATION_LIFESPAN*(1 - 0.7f)));
			evolutionScales[3] = 0;
		}
		// Change form
		else if (animationEvolve > EVOLVE_ANIMATION_LIFESPAN*0.3)
		{
			prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255;
			prevEvolutionScales[3] = ((animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
			evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255;
			evolutionScales[3] = (1 - (animationEvolve - EVOLVE_ANIMATION_LIFESPAN*0.3f)/(EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
		}
		// Restore color
		else
		{
			prevEvolutionScales[3] = 0;
			evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255*(animationEvolve)/(EVOLVE_ANIMATION_LIFESPAN*(1-0.7f));
		}
		
		animationEvolve -= Global.MS_BETWEEN_FRAMES;
		
		g2d.drawImage(Global.colorImage(nextEvolution, evolutionScales, evolutionOffsets), px-nextEvolution.getWidth()/2, py-nextEvolution.getHeight(), null);
		g2d.drawImage(Global.colorImage(currEvolution, prevEvolutionScales, prevEvolutionOffsets), px-currEvolution.getWidth()/2, py-currEvolution.getHeight(), null);
	}

	public ViewMode getViewModel()
	{
		return ViewMode.EVOLUTION_VIEW;
	}

	private void setPokemon(ActivePokemon pokemon, BaseEvolution evolve)
	{
		evolvingPokemon = pokemon;
		preEvolution = pokemon.getPokemonInfo();
		
		if (evolve != null)
			postEvolution = evolve.getEvolution();
		else
			isEgg = true;
	}
	
	private void setInitialMessage()
	{
		if(isEgg)
		{
			message = "Your egg is hatching!";
		}
		else
		{
			message = "Your " + preEvolution.getName() + " is evolving!";
		}
	}
	
	private void addToPokedex()
	{	
		if (isEgg)
		{
			player.getPokedex().setStatus(preEvolution, PokedexStatus.CAUGHT);
		}
		else
		{
			player.getPokedex().setStatus(postEvolution, PokedexStatus.CAUGHT);			
		}
	}
	
	private void setFinalMessage()
	{
		if (isEgg)
		{
			message = "Your egg hatched into a " + preEvolution.getName()  +"!";
		}
		else
		{
			message = "Your " + preEvolution.getName() + " evolved into a " + postEvolution.getName()+"!";
		}
	}
	
	public void movedToFront(Game game) 
	{
		state = State.START;
		
		setPokemon(player.evolvingPokemon, player.evolution);
		setInitialMessage();
		
		animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
		
		// TODO: Save current sound for when transitioning to the bag view.
		//Global.soundPlayer.playMusic(SoundTitle.EVOLUTION_VIEW);
	}
}