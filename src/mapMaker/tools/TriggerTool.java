package mapMaker.tools;

import mapMaker.MapMaker;
import mapMaker.data.PlaceableTrigger;
import mapMaker.model.EditMode.EditType;
import mapMaker.model.TriggerModel.TriggerModelType;
import util.DrawUtils;
import util.Point;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

// TODO: This tool doesn't work and doesn't look like it did previously either
public class TriggerTool extends Tool {
    private JPopupMenu triggerListPopup;
    private JPopupMenu triggerOptionsPopup;

    private PlaceableTrigger[] triggers;
    private PlaceableTrigger selectedTrigger;

    public TriggerTool(MapMaker mapMaker) {
        super(mapMaker);

        selectedTrigger = null;
        triggerListPopup = new JPopupMenu();
        triggerOptionsPopup = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Edit");
        triggerOptionsPopup.add(editItem);

        editItem.addActionListener(event -> mapMaker.getTriggerData().editTrigger(selectedTrigger));

        JMenuItem moveItem = new JMenuItem("Move");
        triggerOptionsPopup.add(moveItem);
        moveItem.addActionListener(event -> {
            mapMaker.setTool(ToolType.SINGLE_CLICK);
            TriggerModelType triggerModelType = mapMaker.getTriggerData().getTriggerModelType(selectedTrigger);
            if (triggerModelType != null) {
                mapMaker.setEditType(EditType.TRIGGERS);

                mapMaker.getTriggerData().moveTrigger(selectedTrigger);
                mapMaker.triggerToolMoveSelected = true;

                mapMaker.setSelectedTileIndex(triggerModelType.ordinal());
            }
        });

        JMenuItem removeItem = new JMenuItem("Remove");
        triggerOptionsPopup.add(removeItem);
        removeItem.addActionListener(event -> mapMaker.getTriggerData().removeTrigger(selectedTrigger));
    }

    @Override
    public void click(Point clickLocation) {
        if (!mapMaker.hasMap()) {
            return;
        }

        Point location = DrawUtils.getLocation(clickLocation, mapMaker.getMapLocation());

        System.out.println("Trigger click: " + clickLocation);

        triggers = mapMaker.getTriggerData().getTrigger(location);
        triggerListPopup.removeAll();

        for (PlaceableTrigger trigger : triggers) {
            JMenuItem menuItem = new JMenuItem(trigger.name + " (" + trigger.triggerType + ")");
            triggerListPopup.add(menuItem);
            menuItem.addActionListener(event -> {
                Component[] components = triggerListPopup.getComponents();
                // TODO: If someone reads this, please suggest a better way to find the index of the selected item...
                for (Component component : components) {
                    if (((JMenuItem) component).getText().equals(event.getActionCommand())) {
                        for (PlaceableTrigger trigger1 : triggers) {
                            if (event.getActionCommand().equals(trigger1.name + " (" + trigger1.triggerType + ")")) {
                                //System.out.println("Clicked " + e.getActionCommand());
                                selectedTrigger = trigger1;
                                break;
                            }
                        }
                    }
                }

                //triggerListPopup.removeAll();
                triggerOptionsPopup.show(mapMaker.canvas, clickLocation.x, clickLocation.y);
            });
        }

        triggerListPopup.show(mapMaker.canvas, clickLocation.x, clickLocation.y);
    }

    @Override
    public void draw(Graphics g) {
        Point mouseHoverLocation = DrawUtils.getLocation(mapMaker.getMouseHoverLocation(), mapMaker.getMapLocation());
        DrawUtils.outlineTile(g, mouseHoverLocation, mapMaker.getMapLocation(), Color.BLUE);
    }

    public String toString() {
        return "Trigger";
    }
}