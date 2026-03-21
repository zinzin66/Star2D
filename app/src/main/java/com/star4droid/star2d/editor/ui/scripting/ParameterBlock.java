package com.star4droid.star2d.editor.ui.scripting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.VisUI;

import java.util.ArrayList;
import java.util.List;

public class ParameterBlock extends VisTable {

    private final VisualNode.NodeField parentField;
    private final String template;
    private final String displayTemplate;
    private final List<VisualNode.NodeField> innerFields = new ArrayList<>();

    public ParameterBlock(VisualNode.NodeField parentField, String template, String displayTemplate) {
        this.parentField = parentField;
        this.template = template;
        this.displayTemplate = displayTemplate;

        try {
            setBackground("border");
        } catch (Exception e) {
            try {
                setBackground("textfield");
            } catch (Exception e2) {
                setBackground("list-selection");
            }
        }
        pad(6);

        // Remove button
        if (parentField != null) {
            VisTextButton closeBtn = new VisTextButton("x");
            closeBtn.getLabel().setFontScale(0.7f);
            closeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    parentField.removeParameterBlock();
                }
            });
            add(closeBtn).padRight(6).size(24, 24);
        }

        // Parse display template (e.g. "___ + ___" or "to int ___")
        String parseTemplate = displayTemplate.replaceAll("%[0-9]+\\$s", "___");
        String[] parts = parseTemplate.split("___", -1);
        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                String text = parts[i];
                if (text.length() > 14) {
                    text = text.substring(0, 13) + "...";
                }
                VisLabel lbl = new VisLabel(text);
                lbl.setFontScale(0.75f);
                lbl.setEllipsis(true);
                add(lbl).padRight(4).padLeft(4).minWidth(0);
            }
            if (i < parts.length - 1) { // Add a field for every '___'
                if (parentField != null) {
                    VisualNode.NodeField innerField = new VisualNode.NodeField("___", "", (UiStage) parentField.stage);
                    // Hide the name label on inline fields to save space
                    innerField.nameBtn.setVisible(false);
                    innerField.getCells().get(0).width(0).height(0).pad(0);

                    innerFields.add(innerField);
                    add(innerField).padRight(2).padLeft(2).minWidth(30);
                } else {
                    // dummy field for visual representation only
                    VisTextButton dummyBtn = new VisTextButton("___");
                    dummyBtn.getLabel().setFontScale(0.75f);
                    dummyBtn.setDisabled(true);
                    add(dummyBtn).padRight(2).padLeft(2).size(30, 24);
                }
            }
        }
    }

    public String getTemplate() {
        return template;
    }

    public String getDisplayTemplate() {
        return displayTemplate;
    }

    public List<VisualNode.NodeField> getInnerFields() {
        return innerFields;
    }

    public String generateCode() {
        Object[] args = new Object[innerFields.size()];
        for (int i = 0; i < innerFields.size(); i++) {
            args[i] = innerFields.get(i).generateCode();
        }
        return String.format(template, args);
    }

    public VisualNode getParentVisualNode() {
        Actor current = this.getParent();
        while (current != null) {
            if (current instanceof VisualNode) {
                return (VisualNode) current;
            }
            current = current.getParent();
        }
        return null;
    }

    public void packVisualNode() {
        VisualNode node = getParentVisualNode();
        if (node != null) {
            node.pack();
        }
    }
}
