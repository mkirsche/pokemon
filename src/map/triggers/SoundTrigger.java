package map.triggers;

import java.util.regex.Matcher;

import sound.SoundTitle;
import main.Game;
import main.Global;

public class SoundTrigger extends Trigger 
{
	public SoundTitle music;
	public SoundTitle effect;
	
	public SoundTrigger(String name, String contents) 
	{
		super(name, contents);

		Matcher m = variablePattern.matcher(contents);
		
		while (m.find())
		{
			switch (m.group(1))
			{
				case "effectName":
					effect = SoundTitle.valueOf(m.group(2));
					break;
				case "musicName":
					music = SoundTitle.valueOf(m.group(2));
					break;
			}
		}
	}
	
	public SoundTrigger(String name, String conditionString, SoundTitle music, SoundTitle effect) 
	{
		super(name, conditionString);
		
		this.music = music;
		this.effect = effect;
	}
	
	public void execute(Game game) 
	{
		super.execute(game);
			
		if (music != null)
		{
			Global.soundPlayer.playMusic(music);
		}
		
		if(effect != null)
		{
			Global.soundPlayer.playSoundEffect(effect);
		}
	}

	public String toString() 
	{
		return "SoundTrigger: " + name + " music: " + music + " effect: " + effect;
	}
	
	public String triggerDataAsString() 
	{
		StringBuilder ret = new StringBuilder(super.triggerDataAsString());
		
		if(music != null)
		{
			ret.append("\tmusicName: " + music + "\n");
		}
		
		if(effect != null)
		{
			ret.append("\teffectName: " + effect + "\n");
		}
		
		return ret.toString();
	}
}
