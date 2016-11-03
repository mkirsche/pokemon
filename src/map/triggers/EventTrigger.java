package map.triggers;

import message.Messages;

public class EventTrigger extends Trigger {
	// TODO: This should likely be an array of dialogue and contents should be json of an array of Strings
	private String dialogue;

	public EventTrigger(String name, String contents) {
		super(name, contents);
		this.dialogue = contents;
	}

	public void execute() {
		super.execute();
		Messages.addMessageToFront(dialogue);
	}
	
	public String toString() {
		return "EventTrigger: " + name + " dialogue:" + dialogue + " global:" + globals.toString();
	}
}
