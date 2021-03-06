package mapMaker.dialogs.action.trigger;

import item.ItemNamesies;
import util.ColorDocumentListener;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JComponent;
import javax.swing.JTextField;

class StringTriggerPanel extends TriggerContentsPanel {
    final JTextField textField;

    StringTriggerPanel(String label) {
        this.textField = GUIUtils.createTextField();

        GUIUtils.setVerticalLayout(this, GUIUtils.createTextFieldComponent(label, this.textField));
    }

    @Override
    protected void load(String triggerContents) {
        this.textField.setText(triggerContents);
    }

    @Override
    protected String getTriggerContents() {
        return this.textField.getText();
    }

    static class ItemTriggerPanel extends StringTriggerPanel {
        ItemTriggerPanel() {
            super("Item Name");

            ColorCondition colorCondition = () -> ItemNamesies.tryValueOf(textField.getText().trim()) != null;
            textField.getDocument().addDocumentListener(new ColorDocumentListener(colorCondition) {
                @Override
                protected JComponent colorComponent() {
                    return textField;
                }
            });
        }

        @Override
        protected void load(String triggerContents) {
            super.load(ItemNamesies.getValueOf(triggerContents).getName());
        }

        @Override
        protected String getTriggerContents() {
            return ItemNamesies.getValueOf(super.getTriggerContents()).name();
        }
    }
}
