package com.star4droid.star2d.editor.ui.scripting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;

import java.util.List;

public class ParameterSuggestionDialog extends VisWindow {

    public interface OnBlockSelected {

        void onEditValue();

        void onBlockSelected(String template, String displayTemplate);
    }

    private final OnBlockSelected listener;
    private final VisTable listTable;

    public static void show(Stage stage, OnBlockSelected listener) {
        ParameterSuggestionDialog dialog = new ParameterSuggestionDialog(listener);
        dialog.pack();
        dialog.setSize(350, stage.getHeight());
        dialog.setPosition(stage.getWidth(), 0);
        stage.addActor(dialog);

        // Animate in from right (ensure width is correct)
        dialog.addAction(Actions.moveTo(stage.getWidth() - 350, 0, 0.4f, Interpolation.smooth));
    }

    public ParameterSuggestionDialog(OnBlockSelected listener) {
        super("");
        this.listener = listener;

        setModal(false);
        setMovable(false);
        addCloseButton();

        listTable = new VisTable(true);
        listTable.defaults().growX().pad(4);
        listTable.top();

        // Edit Value
        Section valueSection = new Section("Value");
        valueSection.addItem("Edit raw value", () -> {
            closeDialog();
            listener.onEditValue();
        });
        listTable.add(valueSection.getRoot()).growX().row();

        // Operators
        Section opSection = new Section("Operators");
        opSection.addBlockItem("___ + ___", "(%1$s + %2$s)", listener, this);
        opSection.addBlockItem("___ - ___", "(%1$s - %2$s)", listener, this);
        opSection.addBlockItem("___ * ___", "(%1$s * %2$s)", listener, this);
        opSection.addBlockItem("___ / ___", "(%1$s / %2$s)", listener, this);
        opSection.addBlockItem("___ == ___", "(%1$s == %2$s)", listener, this);
        opSection.addBlockItem("___ != ___", "(%1$s != %2$s)", listener, this);
        opSection.addBlockItem("___ < ___", "(%1$s < %2$s)", listener, this);
        opSection.addBlockItem("___ > ___", "(%1$s > %2$s)", listener, this);
        opSection.addBlockItem("___ <= ___", "(%1$s <= %2$s)", listener, this);
        opSection.addBlockItem("___ >= ___", "(%1$s >= %2$s)", listener, this);
        listTable.add(opSection.getRoot()).growX().row();

        // Strings
        Section strSection = new Section("Strings");
        strSection.addBlockItem("___ + ___", "(String.valueOf(%1$s) + String.valueOf(%2$s))", listener, this);
        listTable.add(strSection.getRoot()).growX().row();

        // Convert
        Section convSection = new Section("Convert");
        convSection.addBlockItem("to int ___", "((int)(%1$s))", listener, this);
        convSection.addBlockItem("to float ___", "((float)(%1$s))", listener, this);
        convSection.addBlockItem("to string ___", "String.valueOf(%1$s)", listener, this);
        listTable.add(convSection.getRoot()).growX().row();

        // Engine - PlayerItem
        Section piSection = new Section("PlayerItem");
        piSection.addBlockItem("___ .getX()", "%1$s.getX()", listener, this);
        piSection.addBlockItem("___ .getY()", "%1$s.getY()", listener, this);
        piSection.addBlockItem("___ .getZIndex()", "%1$s.getZIndex()", listener, this);
        piSection.addBlockItem("___ .getBody()", "%1$s.getBody()", listener, this);
        piSection.addBlockItem("___ .getActor()", "%1$s.getActor()", listener, this);
        listTable.add(piSection.getRoot()).growX().row();

        // Engine - Body
        Section bodySection = new Section("Box2D Body");
        bodySection.addBlockItem("___ .getLinearVelocity()", "%1$s.getLinearVelocity()", listener, this);
        bodySection.addBlockItem("___ .getAngularVelocity()", "%1$s.getAngularVelocity()", listener, this);
        bodySection.addBlockItem("___ .getPosition()", "%1$s.getPosition()", listener, this);
        bodySection.addBlockItem("___ .getMass()", "%1$s.getMass()", listener, this);
        bodySection.addBlockItem("___ .applyForceToCenter(___, ___, ___)", "%1$s.applyForceToCenter(%2$s, %3$s, %4$s)", listener, this);
        bodySection.addBlockItem("___ .setLinearVelocity(___, ___)", "%1$s.setLinearVelocity(%2$s, %3$s)", listener, this);
        bodySection.addBlockItem("___ .setTransform(___, ___, ___)", "%1$s.setTransform(%2$s, %3$s, %4$s)", listener, this);
        listTable.add(bodySection.getRoot()).growX().row();

        // Engine - Stage
        Section stageSection = new Section("Stage / Scene");
        stageSection.addBlockItem("stage.getWidth()", "stage.getWidth()", listener, this);
        stageSection.addBlockItem("stage.getHeight()", "stage.getHeight()", listener, this);
        stageSection.addBlockItem("stage.getRoot()", "stage.getRoot()", listener, this);
        stageSection.addBlockItem("stage.getActors()", "stage.getActors()", listener, this);
        stageSection.addBlockItem("stage.getCamera()", "stage.getCamera()", listener, this);
        listTable.add(stageSection.getRoot()).growX().row();

        // --- Custom User Blocks ---
        List<CustomBlockSection> customSections = CustomBlockLibraryManager.load();
        for (CustomBlockSection sec : customSections) {
            Section uiSec = new Section(sec.name);
            for (CustomBlockDef def : sec.blocks) {
                uiSec.addBlockItem(def.title, def.code, listener, this);
            }
            listTable.add(uiSec.getRoot()).growX().row();
        }

        ScrollPane scrollPane = new VisScrollPane(listTable);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(scrollPane).grow().pad(10);
    }

    public void closeDialog() {
        addAction(Actions.sequence(
                Actions.moveTo(getStage().getWidth(), 0, 0.3f, Interpolation.pow3In),
                Actions.removeActor()
        ));
    }

    private static class Section {

        private final VisTextButton headerButton;
        private final VisTable contentTable;
        private final CollapsibleWidget collapsible;
        private final VisTable root;
        private boolean expanded = false;

        public Section(String title) {
            root = new VisTable(true);
            headerButton = new VisTextButton(title);
            headerButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    expanded = !expanded;
                    collapsible.setCollapsed(!expanded, true);
                }
            });

            contentTable = new VisTable(true);
            collapsible = new CollapsibleWidget(contentTable);
            collapsible.setCollapsed(true, false);

            root.add(headerButton).expandX().fillX().row();
            root.add(collapsible).expandX().fillX().row();
        }

        public void addItem(String text, Runnable action) {
            VisTable row = new VisTable();
            row.setBackground("list-selection");
            VisLabel lbl = new VisLabel(text);
            row.add(lbl).expandX().left().pad(8);

            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    action.run();
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    row.setColor(1, 1, 1, 0.2f);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    row.setColor(Color.WHITE);
                }
            });
            contentTable.add(row).growX().padBottom(4).row();
        }

        public void addBlockItem(String displayTemplate, String template, OnBlockSelected listener, ParameterSuggestionDialog dialog) {
            VisTable row = new VisTable();
            row.setBackground("list-selection");

            // Dummy parameter block
            ParameterBlock dummyBlock = new ParameterBlock(null, template, displayTemplate);
            row.add(dummyBlock).expandX().left().pad(8);

            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.closeDialog();
                    listener.onBlockSelected(template, displayTemplate);
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    row.setColor(1, 1, 1, 0.2f);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    row.setColor(Color.WHITE);
                }
            });
            contentTable.add(row).growX().padBottom(4).row();
        }

        public VisTable getRoot() {
            return root;
        }
    }
}
