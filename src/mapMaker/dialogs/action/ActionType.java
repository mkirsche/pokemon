package mapMaker.dialogs.action;

public enum ActionType {
    TRIGGER(dialog -> new TriggerActionPanel()),
    BATTLE(BattleActionPanel::new),
    CHOICE(dialog -> new ChoiceActionPanel()),
    UPDATE(dialog -> new BasicActionPanel("Update Name")),
    GROUP_TRIGGER(dialog -> new BasicActionPanel("Trigger Name")),
    GLOBAL(dialog -> new BasicActionPanel("Global Name"));

    private final ActionDataCreator actionDataCreator;

    ActionType(ActionDataCreator actionDataCreator) {
        this.actionDataCreator = actionDataCreator;
    }

    private interface ActionDataCreator {
        ActionPanel createData(ActionDialog actionDialog);
    }

    public ActionPanel createActionData(ActionDialog actionDialog) {
        return this.actionDataCreator.createData(actionDialog);
    }
}
