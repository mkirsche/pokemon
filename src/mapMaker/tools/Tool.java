package mapMaker.tools;

import mapMaker.MapMaker;
import util.Point;

import javax.swing.DefaultListModel;
import java.awt.Graphics;

public abstract class Tool {
    final MapMaker mapMaker;
    public static Tool lastUsedTool;

    Tool(final MapMaker mapMaker) {
        this.mapMaker = mapMaker;
    }

    // Can be overridden as necessary by subclasses
    public void click(Point clickLocation) {}
    public void released(Point releasedLocation) {}
    public void pressed(Point pressedLocation) {}
    public void drag(Point dragLocation) {}
    public void draw(Graphics g) {}
    public void reset() {}

    public static void undoLastTool() {
        if (lastUsedTool != null) {
            lastUsedTool.undo();
        }
    }

    public abstract void undo();

    public static DefaultListModel<Tool> getToolListModel(MapMaker mapMaker) {
        DefaultListModel<Tool> toolListModel = new DefaultListModel<>();
        for (ToolType toolType : ToolType.values()) {
            toolListModel.addElement(toolType.toolCreator.createTool(mapMaker));
        }

        return toolListModel;
    }

    public enum ToolType {
        MOVE(MoveTool::new),
        SINGLE_CLICK(SingleClickTool::new),
        RECTANGLE(RectangleTool::new),
        TRIGGER(TriggerTool::new),
        SELECT(SelectTool::new);

        private final ToolCreator toolCreator;

        ToolType(ToolCreator toolCreator) {
            this.toolCreator = toolCreator;
        }

        private interface ToolCreator {
            Tool createTool(MapMaker mapMaker);
        }
    }
}
