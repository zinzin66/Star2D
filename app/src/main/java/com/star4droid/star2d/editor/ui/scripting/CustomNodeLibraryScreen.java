package com.star4droid.star2d.editor.ui.scripting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;

import java.util.ArrayList;
import java.util.List;

public class CustomNodeLibraryScreen extends VisWindow {

    private List<CustomNodeSection> nodeSections;
    private List<CustomBlockSection> blockSections;
    private final Stage ownerStage;

    private boolean isParametersMode = false;

    // Persistent top-bar widgets
    private VisTable contentArea;
    private VisLabel topTitle;
    private VisTextButton backBtn;

    // Toggle buttons
    private VisTextButton nodesTabBtn;
    private VisTextButton paramsTabBtn;

    public CustomNodeLibraryScreen(Stage owner) {
        super("");
        this.ownerStage = owner;
        setFillParent(true);
        setMovable(false);
        getTitleTable().clearChildren();

        nodeSections = CustomNodeLibraryManager.load();
        blockSections = CustomBlockLibraryManager.load();

        buildShell();
        showSectionsView();
    }

    private void buildShell() {
        VisTable topBar = new VisTable();
        topBar.setBackground(VisUI.getSkin().getDrawable("window-bg"));
        topBar.pad(6, 10, 6, 10);

        backBtn = new VisTextButton("< Back");
        backBtn.setVisible(false);

        topTitle = new VisLabel("Library");
        topTitle.setFontScale(1.1f);
        topTitle.setColor(Color.WHITE);

        nodesTabBtn = new VisTextButton("Nodes Mode");
        paramsTabBtn = new VisTextButton("Parameters Mode");
        updateTabStyles();

        VisTextButton closeBtn = new VisTextButton("X");
        closeBtn.setColor(Color.RED);

        topBar.add(backBtn).size(110, 48).padRight(10);
        topBar.add(topTitle).expandX().left();
        topBar.add(nodesTabBtn).height(48).padRight(8);
        topBar.add(paramsTabBtn).height(48).padRight(16);
        topBar.add(closeBtn).size(60, 48);

        add(topBar).growX().row();

        contentArea = new VisTable();
        contentArea.top().left();
        add(contentArea).grow().row();

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (isParametersMode) {
                    showBlockSectionsView();
                } else {
                    showSectionsView();
                }
            }
        });

        nodesTabBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                isParametersMode = false;
                updateTabStyles();
                showSectionsView();
            }
        });

        paramsTabBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                isParametersMode = true;
                updateTabStyles();
                showBlockSectionsView();
            }
        });

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                CustomNodeLibraryScreen.this.remove();
            }
        });
    }

    private void updateTabStyles() {
        nodesTabBtn.setColor(isParametersMode ? Color.GRAY : Color.valueOf("44bb44ff"));
        paramsTabBtn.setColor(isParametersMode ? Color.valueOf("44bb44ff") : Color.GRAY);
    }

    // =========================================================================
    //  NODES SECTIONS VIEW
    // =========================================================================
    private void showSectionsView() {
        contentArea.clearChildren();
        backBtn.setVisible(false);
        topTitle.setText("Node Library");
        nodesTabBtn.setVisible(true);
        paramsTabBtn.setVisible(true);

        VisTable actions = new VisTable();
        actions.pad(8, 10, 4, 10);
        VisTextButton newSectionBtn = new VisTextButton("+ New Section");
        VisTextButton importBtn = new VisTextButton("Import JSON");
        VisTextButton exportAllBtn = new VisTextButton("Export All");
        actions.add(newSectionBtn).height(52).padRight(8);
        actions.add(importBtn).height(52).padRight(8);
        actions.add(exportAllBtn).height(52);
        contentArea.add(actions).left().row();

        VisTable listTable = new VisTable();
        listTable.top().left();
        listTable.pad(10);
        ScrollPane sp = new ScrollPane(listTable, VisUI.getSkin());
        sp.setFadeScrollBars(false);
        contentArea.add(sp).grow().row();

        populateSectionsList(listTable);

        newSectionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showSectionNameDialog(null, () -> showSectionsView());
            }
        });
        importBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showImportDialog();
            }
        });
        exportAllBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showTextDialog("Export All", CustomNodeLibraryManager.exportAllJson(nodeSections));
            }
        });
    }

    private void populateSectionsList(VisTable listTable) {
        listTable.clearChildren();
        if (nodeSections.isEmpty()) {
            VisLabel empty = new VisLabel("No node sections yet.  Tap  [ + New Section ]");
            empty.setColor(Color.LIGHT_GRAY);
            listTable.add(empty).expand().center().pad(40).row();
            return;
        }
        for (CustomNodeSection sec : nodeSections) {
            listTable.add(buildSectionCard(sec, listTable)).growX().padBottom(12).row();
        }
    }

    private VisTable buildSectionCard(CustomNodeSection section, VisTable listTable) {
        VisTable card = new VisTable();
        card.setBackground(VisUI.getSkin().getDrawable("window-bg"));
        card.pad(12);

        VisLabel name = new VisLabel(section.name);
        name.setFontScale(1.05f);
        name.setColor(Color.WHITE);
        card.add(name).expandX().left().padBottom(10).colspan(3).row();

        VisTextButton openBtn = new VisTextButton("  Open  ");
        VisTextButton renameBtn = new VisTextButton(" Rename ");
        VisTextButton delBtn = new VisTextButton(" Delete ");
        openBtn.setColor(Color.valueOf("44bb44ff"));
        delBtn.setColor(Color.valueOf("cc4444ff"));

        card.add(openBtn).height(52).padRight(8);
        card.add(renameBtn).height(52).padRight(8);
        card.add(delBtn).height(52);

        openBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showNodesView(section);
            }
        });
        renameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showSectionNameDialog(section, () -> populateSectionsList(listTable));
            }
        });
        delBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showConfirm("Delete section \"" + section.name + "\" and all its nodes?", () -> {
                    nodeSections.remove(section);
                    CustomNodeLibraryManager.save(nodeSections);
                    populateSectionsList(listTable);
                });
            }
        });
        return card;
    }

    // =========================================================================
    //  NODES VIEW
    // =========================================================================
    private void showNodesView(CustomNodeSection section) {
        contentArea.clearChildren();
        backBtn.setVisible(true);
        topTitle.setText(section.name);
        nodesTabBtn.setVisible(false);
        paramsTabBtn.setVisible(false);

        VisTable actions = new VisTable();
        actions.pad(8, 10, 4, 10);
        VisTextButton newNodeBtn = new VisTextButton("+ New Node");
        VisTextButton exportBtn = new VisTextButton("Export Section");
        actions.add(newNodeBtn).height(52).padRight(8);
        actions.add(exportBtn).height(52);
        contentArea.add(actions).left().row();

        VisTable listTable = new VisTable();
        listTable.top().left();
        listTable.pad(10);
        ScrollPane sp = new ScrollPane(listTable, VisUI.getSkin());
        sp.setFadeScrollBars(false);
        contentArea.add(sp).grow().row();

        rebuildNodesList(listTable, section);

        newNodeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showNodeEditDialog(null, section, () -> rebuildNodesList(listTable, section));
            }
        });
        exportBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showTextDialog("Export: " + section.name,
                        CustomNodeLibraryManager.exportSectionJson(section));
            }
        });
    }

    private void rebuildNodesList(VisTable listTable, CustomNodeSection section) {
        listTable.clearChildren();
        if (section.nodes.isEmpty()) {
            VisLabel empty = new VisLabel("No nodes yet.  Tap  [ + New Node ]");
            empty.setColor(Color.LIGHT_GRAY);
            listTable.add(empty).expand().center().pad(40).row();
            return;
        }
        for (CustomNodeDef node : section.nodes) {
            listTable.add(buildNodeCard(node, section, listTable)).growX().padBottom(8).row();
        }
    }

    private VisTable buildNodeCard(CustomNodeDef node, CustomNodeSection section, VisTable listTable) {
        VisTable card = new VisTable();
        card.setBackground(VisUI.getSkin().getDrawable("window-bg"));
        card.pad(10, 14, 10, 14);

        VisLabel titleLbl = new VisLabel(node.title.isEmpty() ? node.name : node.title);
        titleLbl.setFontScale(1.0f);
        titleLbl.setColor(Color.WHITE);

        VisLabel nameLbl = new VisLabel("  [" + node.name + "]");
        nameLbl.setColor(Color.LIGHT_GRAY);
        nameLbl.setFontScale(0.8f);

        VisTextButton editBtn = new VisTextButton("Edit");
        editBtn.setColor(Color.valueOf("3399ffff"));
        VisTextButton delBtn = new VisTextButton("X");
        delBtn.setColor(Color.valueOf("cc4444ff"));

        card.add(titleLbl).expandX().left();
        card.add(nameLbl).expandX().left();
        card.add(editBtn).size(90, 48).padRight(6);
        card.add(delBtn).size(48, 48);

        editBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showNodeEditDialog(node, section, () -> rebuildNodesList(listTable, section));
            }
        });
        delBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String label = node.title.isEmpty() ? node.name : node.title;
                showConfirm("Delete node \"" + label + "\"?", () -> {
                    section.nodes.remove(node);
                    CustomNodeLibraryManager.save(nodeSections);
                    rebuildNodesList(listTable, section);
                });
            }
        });
        return card;
    }

    // =========================================================================
    //  BLOCK SECTIONS VIEW (Parameters Mode)
    // =========================================================================
    private void showBlockSectionsView() {
        contentArea.clearChildren();
        backBtn.setVisible(false);
        topTitle.setText("Parameter Blocks Library");
        nodesTabBtn.setVisible(true);
        paramsTabBtn.setVisible(true);

        VisTable actions = new VisTable();
        actions.pad(8, 10, 4, 10);
        VisTextButton newSectionBtn = new VisTextButton("+ New Section");
        VisTextButton importBtn = new VisTextButton("Import JSON");
        VisTextButton exportAllBtn = new VisTextButton("Export All");
        actions.add(newSectionBtn).height(52).padRight(8);
        actions.add(importBtn).height(52).padRight(8);
        actions.add(exportAllBtn).height(52);
        contentArea.add(actions).left().row();

        VisTable listTable = new VisTable();
        listTable.top().left();
        listTable.pad(10);
        ScrollPane sp = new ScrollPane(listTable, VisUI.getSkin());
        sp.setFadeScrollBars(false);
        contentArea.add(sp).grow().row();

        populateBlockSectionsList(listTable);

        newSectionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showBlockSectionNameDialog(null, () -> showBlockSectionsView());
            }
        });
        importBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showBlockImportDialog();
            }
        });
        exportAllBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showTextDialog("Export All Blocks", CustomBlockLibraryManager.exportAllJson(blockSections));
            }
        });
    }

    private void populateBlockSectionsList(VisTable listTable) {
        listTable.clearChildren();
        if (blockSections.isEmpty()) {
            VisLabel empty = new VisLabel("No block sections yet.  Tap  [ + New Section ]");
            empty.setColor(Color.LIGHT_GRAY);
            listTable.add(empty).expand().center().pad(40).row();
            return;
        }
        for (CustomBlockSection sec : blockSections) {
            listTable.add(buildBlockSectionCard(sec, listTable)).growX().padBottom(12).row();
        }
    }

    private VisTable buildBlockSectionCard(CustomBlockSection section, VisTable listTable) {
        VisTable card = new VisTable();
        card.setBackground(VisUI.getSkin().getDrawable("window-bg"));
        card.pad(12);

        VisLabel name = new VisLabel(section.name);
        name.setFontScale(1.05f);
        name.setColor(Color.WHITE);
        card.add(name).expandX().left().padBottom(10).colspan(3).row();

        VisTextButton openBtn = new VisTextButton("  Open  ");
        VisTextButton renameBtn = new VisTextButton(" Rename ");
        VisTextButton delBtn = new VisTextButton(" Delete ");
        openBtn.setColor(Color.valueOf("44bb44ff"));
        delBtn.setColor(Color.valueOf("cc4444ff"));

        card.add(openBtn).height(52).padRight(8);
        card.add(renameBtn).height(52).padRight(8);
        card.add(delBtn).height(52);

        openBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showBlocksView(section);
            }
        });
        renameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showBlockSectionNameDialog(section, () -> populateBlockSectionsList(listTable));
            }
        });
        delBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showConfirm("Delete section \"" + section.name + "\" and all its blocks?", () -> {
                    blockSections.remove(section);
                    CustomBlockLibraryManager.save(blockSections);
                    populateBlockSectionsList(listTable);
                });
            }
        });
        return card;
    }

    // =========================================================================
    //  BLOCKS VIEW
    // =========================================================================
    private void showBlocksView(CustomBlockSection section) {
        contentArea.clearChildren();
        backBtn.setVisible(true);
        topTitle.setText(section.name + " (Blocks)");
        nodesTabBtn.setVisible(false);
        paramsTabBtn.setVisible(false);

        VisTable actions = new VisTable();
        actions.pad(8, 10, 4, 10);
        VisTextButton newBlockBtn = new VisTextButton("+ New Block");
        VisTextButton exportBtn = new VisTextButton("Export Section");
        actions.add(newBlockBtn).height(52).padRight(8);
        actions.add(exportBtn).height(52);
        contentArea.add(actions).left().row();

        VisTable listTable = new VisTable();
        listTable.top().left();
        listTable.pad(10);
        ScrollPane sp = new ScrollPane(listTable, VisUI.getSkin());
        sp.setFadeScrollBars(false);
        contentArea.add(sp).grow().row();

        rebuildBlocksList(listTable, section);

        newBlockBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showBlockEditDialog(null, section, () -> rebuildBlocksList(listTable, section));
            }
        });
        exportBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showTextDialog("Export: " + section.name,
                        CustomBlockLibraryManager.exportSectionJson(section));
            }
        });
    }

    private void rebuildBlocksList(VisTable listTable, CustomBlockSection section) {
        listTable.clearChildren();
        if (section.blocks.isEmpty()) {
            VisLabel empty = new VisLabel("No blocks yet.  Tap  [ + New Block ]");
            empty.setColor(Color.LIGHT_GRAY);
            listTable.add(empty).expand().center().pad(40).row();
            return;
        }
        for (CustomBlockDef block : section.blocks) {
            listTable.add(buildBlockCard(block, section, listTable)).growX().padBottom(8).row();
        }
    }

    private VisTable buildBlockCard(CustomBlockDef block, CustomBlockSection section, VisTable listTable) {
        VisTable card = new VisTable();
        card.setBackground(VisUI.getSkin().getDrawable("window-bg"));
        card.pad(10, 14, 10, 14);

        VisLabel titleLbl = new VisLabel(block.title);
        titleLbl.setFontScale(1.0f);
        titleLbl.setColor(Color.WHITE);

        VisLabel nameLbl = new VisLabel("  [" + block.name + "]");
        nameLbl.setColor(Color.LIGHT_GRAY);
        nameLbl.setFontScale(0.8f);

        VisTextButton editBtn = new VisTextButton("Edit");
        editBtn.setColor(Color.valueOf("3399ffff"));
        VisTextButton delBtn = new VisTextButton("X");
        delBtn.setColor(Color.valueOf("cc4444ff"));

        card.add(titleLbl).expandX().left();
        card.add(nameLbl).expandX().left();
        card.add(editBtn).size(90, 48).padRight(6);
        card.add(delBtn).size(48, 48);

        editBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showBlockEditDialog(block, section, () -> rebuildBlocksList(listTable, section));
            }
        });
        delBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showConfirm("Delete block \"" + block.name + "\"?", () -> {
                    section.blocks.remove(block);
                    CustomBlockLibraryManager.save(blockSections);
                    rebuildBlocksList(listTable, section);
                });
            }
        });
        return card;
    }

    // =========================================================================
    //  DIALOGS FOR NODES
    // =========================================================================
    private void showSectionNameDialog(CustomNodeSection existing, Runnable onDone) {
        VisDialog dialog = new VisDialog(existing == null ? "New Section" : "Rename Section");
        dialog.setMovable(true);

        VisTable content = new VisTable();
        content.pad(20, 20, 10, 20);

        VisLabel lbl = new VisLabel("Section name:");
        lbl.setColor(Color.WHITE);
        content.add(lbl).left().padBottom(8).row();

        VisTextField field = new VisTextField(existing == null ? "" : existing.name);
        content.add(field).width(420).padBottom(16).row();

        VisTextButton okBtn = new VisTextButton("Save");
        VisTextButton cancelBtn = new VisTextButton("Cancel");
        content.add(okBtn).size(140, 52).padRight(10);
        content.add(cancelBtn).size(140, 52);

        dialog.getContentTable().add(content).pad(10);

        okBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String name = field.getText().trim();
                if (name.isEmpty()) {
                    return;
                }

                if (existing == null) {
                    nodeSections.add(new CustomNodeSection(name));
                } else {
                    existing.name = name;
                }
                CustomNodeLibraryManager.save(nodeSections);
                dialog.hide();
                onDone.run();
            }
        });
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialog.hide();
            }
        });
        dialog.show(ownerStage);
    }

    private void showNodeEditDialog(CustomNodeDef existing, CustomNodeSection section, Runnable onDone) {
        boolean isNew = (existing == null);
        CustomNodeDef working = isNew ? new CustomNodeDef() : copyNode(existing);

        VisDialog dialog = new VisDialog(isNew ? "New Node" : "Edit Node");
        dialog.setFillParent(true);
        dialog.setMovable(false);

        VisTable mainContent = new VisTable();
        mainContent.top().left().pad(16);

        mainContent.add(label("Name (editor):")).left().padBottom(4).row();
        VisTextField nameField = new VisTextField(working.name);
        mainContent.add(nameField).growX().padBottom(14).row();

        mainContent.add(label("Title (shown on block):")).left().padBottom(4).row();
        VisTextField titleField = new VisTextField(working.title);
        mainContent.add(titleField).growX().padBottom(14).row();

        VisCheckBox boolCheck = new VisCheckBox("  Boolean Node  (true / false outputs)");
        boolCheck.setChecked(working.isBooleanNode);
        mainContent.add(boolCheck).left().padBottom(14).row();

        mainContent.add(label("Code Template:")).left().padBottom(4).row();
        TextArea codeArea = new TextArea(working.code, VisUI.getSkin());
        codeArea.setPrefRows(5);
        ScrollPane codeScroll = new ScrollPane(codeArea, VisUI.getSkin());
        codeScroll.setFadeScrollBars(false);
        mainContent.add(codeScroll).growX().height(135).padBottom(6).row();

        VisLabel hint = new VisLabel("Placeholders: %1$s … %N$s = field values, last = next node.\nBoolean: second-to-last = true branch, last-1 = false, last = next.");
        hint.setColor(Color.GRAY);
        hint.setFontScale(0.75f);
        hint.setWrap(true);
        mainContent.add(hint).growX().padBottom(12).row();

        mainContent.add(label("Fields  (name → default value):")).left().padBottom(4).row();
        VisTable fieldsContainer = new VisTable();
        fieldsContainer.top().left();
        ScrollPane fieldsScroll = new ScrollPane(fieldsContainer, VisUI.getSkin());
        fieldsScroll.setFadeScrollBars(false);
        mainContent.add(fieldsScroll).growX().height(155).padBottom(8).row();

        List<VisTextField[]> fieldWidgets = new ArrayList<>();
        for (CustomNodeDef.FieldDef fd : working.fields) {
            addFieldRow(fieldsContainer, fieldWidgets, fd.name, fd.value);
        }

        VisTextButton addFieldBtn = new VisTextButton("+ Add Field");
        mainContent.add(addFieldBtn).left().padBottom(8).row();
        addFieldBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                addFieldRow(fieldsContainer, fieldWidgets, "", "");
                fieldsContainer.invalidateHierarchy();
            }
        });

        ScrollPane mainScroll = new ScrollPane(mainContent, VisUI.getSkin());
        mainScroll.setFadeScrollBars(false);
        dialog.getContentTable().add(mainScroll).grow().pad(10).row();

        VisTextButton saveBtn = new VisTextButton("Save");
        VisTextButton cancelBtn = new VisTextButton("Cancel");
        dialog.getContentTable().add(saveBtn).size(160, 55).padRight(12);
        dialog.getContentTable().add(cancelBtn).size(160, 55);

        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showInfo("Name cannot be empty.");
                    return;
                }

                List<CustomNodeDef.FieldDef> fds = new ArrayList<>();
                for (VisTextField[] pair : fieldWidgets) {
                    String fn = pair[0].getText().trim();
                    if (!fn.isEmpty()) {
                        fds.add(new CustomNodeDef.FieldDef(fn, pair[1].getText().trim()));
                    }
                }

                if (isNew) {
                    CustomNodeDef nd = new CustomNodeDef(name, titleField.getText().trim(), codeArea.getText());
                    nd.isBooleanNode = boolCheck.isChecked();
                    nd.fields = fds;
                    section.nodes.add(nd);
                } else {
                    existing.name = name;
                    existing.title = titleField.getText().trim();
                    existing.code = codeArea.getText();
                    existing.isBooleanNode = boolCheck.isChecked();
                    existing.fields = fds;
                }
                CustomNodeLibraryManager.save(nodeSections);
                dialog.hide();
                onDone.run();
            }
        });
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialog.hide();
            }
        });

        dialog.show(ownerStage);
    }

    private void addFieldRow(VisTable container, List<VisTextField[]> list, String name, String value) {
        VisTable row = new VisTable();
        row.pad(4, 2, 4, 2);

        VisTextField nameField = new VisTextField(name);
        nameField.setMessageText("field name");
        VisTextField valueField = new VisTextField(value);
        valueField.setMessageText("default");
        VisTextButton removeBtn = new VisTextButton("X");
        removeBtn.setColor(Color.valueOf("cc4444ff"));

        row.add(new VisLabel("#" + (list.size() + 1))).width(28).padRight(6);
        row.add(nameField).width(220).padRight(8);
        row.add(valueField).width(220).padRight(8);
        row.add(removeBtn).size(44, 42);

        VisTextField[] pair = {nameField, valueField};
        list.add(pair);

        removeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                list.remove(pair);
                row.remove();
            }
        });
        container.add(row).growX().row();
    }

    // =========================================================================
    //  DIALOGS FOR BLOCKS (Parameters Mode)
    // =========================================================================
    private void showBlockSectionNameDialog(CustomBlockSection existing, Runnable onDone) {
        VisDialog dialog = new VisDialog(existing == null ? "New Block Section" : "Rename Block Section");
        dialog.setMovable(true);

        VisTable content = new VisTable();
        content.pad(20, 20, 10, 20);

        VisLabel lbl = new VisLabel("Section name:");
        lbl.setColor(Color.WHITE);
        content.add(lbl).left().padBottom(8).row();

        VisTextField field = new VisTextField(existing == null ? "" : existing.name);
        content.add(field).width(420).padBottom(16).row();

        VisTextButton okBtn = new VisTextButton("Save");
        VisTextButton cancelBtn = new VisTextButton("Cancel");
        content.add(okBtn).size(140, 52).padRight(10);
        content.add(cancelBtn).size(140, 52);

        dialog.getContentTable().add(content).pad(10);

        okBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String name = field.getText().trim();
                if (name.isEmpty()) {
                    return;
                }

                if (existing == null) {
                    blockSections.add(new CustomBlockSection(name));
                } else {
                    existing.name = name;
                }
                CustomBlockLibraryManager.save(blockSections);
                dialog.hide();
                onDone.run();
            }
        });
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialog.hide();
            }
        });
        dialog.show(ownerStage);
    }

    private void showBlockEditDialog(CustomBlockDef existing, CustomBlockSection section, Runnable onDone) {
        boolean isNew = (existing == null);
        CustomBlockDef working = isNew ? new CustomBlockDef() : new CustomBlockDef(existing.name, existing.title, existing.code);

        VisDialog dialog = new VisDialog(isNew ? "New Block" : "Edit Block");
        dialog.setFillParent(true);
        dialog.setMovable(false);

        VisTable mainContent = new VisTable();
        mainContent.top().left().pad(16);

        mainContent.add(label("Name (editor):")).left().padBottom(4).row();
        VisTextField nameField = new VisTextField(working.name);
        mainContent.add(nameField).growX().padBottom(14).row();

        mainContent.add(label("Title (like %1$s + %2$s):")).left().padBottom(4).row();
        VisTextField titleField = new VisTextField(working.title);
        mainContent.add(titleField).growX().padBottom(14).row();

        mainContent.add(label("Code (like (%1$s + %2$s)):")).left().padBottom(4).row();
        TextArea codeArea = new TextArea(working.code, VisUI.getSkin());
        codeArea.setPrefRows(5);
        ScrollPane codeScroll = new ScrollPane(codeArea, VisUI.getSkin());
        codeScroll.setFadeScrollBars(false);
        mainContent.add(codeScroll).growX().height(135).padBottom(6).row();

        VisLabel hint = new VisLabel("Use ___ inside `Title` to create inner slots. Example: to int ___\n%1$s refers to the 1st parameter slot.");
        hint.setColor(Color.GRAY);
        hint.setFontScale(0.75f);
        hint.setWrap(true);
        mainContent.add(hint).growX().padBottom(12).row();

        ScrollPane mainScroll = new ScrollPane(mainContent, VisUI.getSkin());
        mainScroll.setFadeScrollBars(false);
        dialog.getContentTable().add(mainScroll).grow().pad(10).row();

        VisTextButton saveBtn = new VisTextButton("Save");
        VisTextButton cancelBtn = new VisTextButton("Cancel");
        dialog.getContentTable().add(saveBtn).size(160, 55).padRight(12);
        dialog.getContentTable().add(cancelBtn).size(160, 55);

        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showInfo("Name cannot be empty.");
                    return;
                }

                if (isNew) {
                    CustomBlockDef nd = new CustomBlockDef(name, titleField.getText().trim(), codeArea.getText());
                    section.blocks.add(nd);
                } else {
                    existing.name = name;
                    existing.title = titleField.getText().trim();
                    existing.code = codeArea.getText();
                }
                CustomBlockLibraryManager.save(blockSections);
                dialog.hide();
                onDone.run();
            }
        });
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialog.hide();
            }
        });

        dialog.show(ownerStage);
    }

    // =========================================================================
    //  IMPORT/EXPORT DIALOGS
    // =========================================================================
    private void showImportDialog() {
        VisDialog dialog = new VisDialog("Import Nodes JSON");
        dialog.setFillParent(true);
        dialog.setMovable(false);

        VisTable content = new VisTable();
        content.pad(16);

        VisLabel hint = new VisLabel("Paste exported nodes JSON below.");
        hint.setColor(Color.LIGHT_GRAY);
        content.add(hint).growX().padBottom(10).row();

        TextArea textArea = new TextArea("", VisUI.getSkin());
        textArea.setPrefRows(20);
        ScrollPane sp = new ScrollPane(textArea, VisUI.getSkin());
        sp.setFadeScrollBars(false);
        content.add(sp).grow().row();

        dialog.getContentTable().add(content).grow().pad(10).row();

        VisTextButton pasteBtn = new VisTextButton("Paste Clipboard");
        VisTextButton importBtn = new VisTextButton("Import");
        VisTextButton cancelBtn = new VisTextButton("Cancel");

        dialog.getContentTable().add(pasteBtn).size(210, 55).padRight(8);
        dialog.getContentTable().add(importBtn).size(140, 55).padRight(8);
        dialog.getContentTable().add(cancelBtn).size(140, 55);

        pasteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String clip = Gdx.app.getClipboard().getContents();
                if (clip != null) {
                    textArea.setText(clip);
                }
            }
        });
        importBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String result = CustomNodeLibraryManager.importFromJson(textArea.getText(), nodeSections);
                CustomNodeLibraryManager.save(nodeSections);
                dialog.hide();
                showSectionsView();
                showInfo(result);
            }
        });
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialog.hide();
            }
        });
        dialog.show(ownerStage);
    }

    private void showBlockImportDialog() {
        VisDialog dialog = new VisDialog("Import Blocks JSON");
        dialog.setFillParent(true);
        dialog.setMovable(false);

        VisTable content = new VisTable();
        content.pad(16);

        VisLabel hint = new VisLabel("Paste exported blocks JSON below.");
        hint.setColor(Color.LIGHT_GRAY);
        content.add(hint).growX().padBottom(10).row();

        TextArea textArea = new TextArea("", VisUI.getSkin());
        textArea.setPrefRows(20);
        ScrollPane sp = new ScrollPane(textArea, VisUI.getSkin());
        sp.setFadeScrollBars(false);
        content.add(sp).grow().row();

        dialog.getContentTable().add(content).grow().pad(10).row();

        VisTextButton pasteBtn = new VisTextButton("Paste Clipboard");
        VisTextButton importBtn = new VisTextButton("Import");
        VisTextButton cancelBtn = new VisTextButton("Cancel");

        dialog.getContentTable().add(pasteBtn).size(210, 55).padRight(8);
        dialog.getContentTable().add(importBtn).size(140, 55).padRight(8);
        dialog.getContentTable().add(cancelBtn).size(140, 55);

        pasteBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String clip = Gdx.app.getClipboard().getContents();
                if (clip != null) {
                    textArea.setText(clip);
                }
            }
        });
        importBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                String result = CustomBlockLibraryManager.importFromJson(textArea.getText(), blockSections);
                CustomBlockLibraryManager.save(blockSections);
                dialog.hide();
                showBlockSectionsView();
                showInfo(result);
            }
        });
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialog.hide();
            }
        });
        dialog.show(ownerStage);
    }

    private void showTextDialog(String title, String text) {
        VisDialog dialog = new VisDialog(title);
        TextArea ta = new TextArea(text, VisUI.getSkin());
        ta.setPrefRows(18);
        ScrollPane sp = new ScrollPane(ta, VisUI.getSkin());
        sp.setFadeScrollBars(false);
        dialog.getContentTable().add(sp).size(680, 460).pad(10).row();

        VisTextButton copyBtn = new VisTextButton("Copy to Clipboard");
        VisTextButton closeBtn = new VisTextButton("Close");
        copyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                Gdx.app.getClipboard().setContents(text);
                showInfo("Copied!");
            }
        });
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getContentTable().add(copyBtn).size(220, 55).padRight(10);
        dialog.getContentTable().add(closeBtn).size(140, 55);
        dialog.show(ownerStage);
    }

    private void showInfo(String message) {
        VisDialog d = new VisDialog("Info");
        d.text(message);
        d.button("OK");
        d.show(ownerStage);
    }

    private void showConfirm(String message, Runnable onConfirm) {
        VisDialog d = new VisDialog("Confirm");
        d.text(message);
        VisTextButton yes = new VisTextButton("Yes");
        VisTextButton no = new VisTextButton("No");
        yes.setColor(Color.valueOf("cc4444ff"));
        d.getContentTable().row();
        d.getContentTable().add(yes).size(120, 52).padRight(10);
        d.getContentTable().add(no).size(120, 52);
        yes.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                onConfirm.run();
                d.hide();
            }
        });
        no.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                d.hide();
            }
        });
        d.show(ownerStage);
    }

    private VisLabel label(String text) {
        VisLabel lbl = new VisLabel(text);
        lbl.setColor(Color.WHITE);
        return lbl;
    }

    private CustomNodeDef copyNode(CustomNodeDef src) {
        CustomNodeDef copy = new CustomNodeDef(src.name, src.title, src.code);
        copy.isBooleanNode = src.isBooleanNode;
        for (CustomNodeDef.FieldDef fd : src.fields) {
            copy.fields.add(new CustomNodeDef.FieldDef(fd.name, fd.value));
        }
        return copy;
    }
}
