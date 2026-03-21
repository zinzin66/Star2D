package com.star4droid.star2d.editor.ui.scripting;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeTreeParser extends ApplicationAdapter {

    private Stage stage;
    public VisTable treeTable; // Replacing VisTree
    private Map<String, VisTable> treeCategories; // Each category has a nested table
    private UiStage uiStage;
    private NodeEditor editorStage;
    private StringBuilder code;

    public NodeTreeParser(UiStage uiStage, NodeEditor editorStage) {
        this.editorStage = editorStage;
        this.uiStage = uiStage;
    }

    @Override
    public void create() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        treeCategories = new HashMap<>();
        parseNodesFile();
        createUI();
    }

    /**
     * Reload all nodes (built-in + custom) into the existing treeTable. Called
     * by UiStage.loadFrom() so custom nodes show up without restarting.
     */
    public void refresh() {
        if (treeTable == null) {
            return;
        }
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                treeTable.clearChildren();
                treeTable.add().height(200).row();   // restore top spacer
                treeCategories.clear();
                categoryEntries.clear();             // also reset animated entries
                parseNodesFile();
                treeTable.invalidateHierarchy();
            }
        });
    }

    private void parseNodesFile() {
        FileHandle file = Gdx.files.internal("java/nodes.java");
        String fileContent = file.readString();
        String[] lines = fileContent.split("\n");

        String currentCategory = null;
        code = new StringBuilder();
        Color currentColor = null;
        boolean inCodeSection = false;
        List<String> fields;
        String nodeDetails = null;

        for (String line : lines) {
            if (line.trim().equals("split")) {
                if (currentColor != null && nodeDetails != null) {
                    String[] split = nodeDetails.split(" ");
                    String nodeName = split[0];
                    fields = new ArrayList<>();
                    for (int i = 1; i < split.length; i++) {
                        fields.add(split[i]);
                    }
                    addNodeToTable(currentCategory, nodeName, code.toString(), currentColor, fields);
                }
                inCodeSection = false;
                code = new StringBuilder();
                currentColor = null;
                nodeDetails = null;
                continue;
            }
            if (inCodeSection) {
                code.append(line).append("\n");
                continue;
            }
            if (line.startsWith("--")) {
                currentCategory = line.replace("--", "");
                continue;
            }
            if (line.startsWith("-color:")) {
                currentColor = parseColor(line);
                continue;
            }
            if (line.startsWith("<<=>>")) {
                inCodeSection = true;
                continue;
            }
            nodeDetails = line;
        }

        // ---- Load user's custom library from external storage ----
        try {
            List<CustomNodeSection> customSections = CustomNodeLibraryManager.load();
            for (CustomNodeSection section : customSections) {
                for (CustomNodeDef nodeDef : section.nodes) {
                    List<String> nodeFields = new ArrayList<>();
                    for (CustomNodeDef.FieldDef fd : nodeDef.fields) {
                        nodeFields.add(fd.name);
                    }
                    String nodeName = nodeDef.isBooleanNode
                            ? nodeDef.title + "__star__if__"
                            : (nodeDef.title.isEmpty() ? nodeDef.name : nodeDef.title);
                    addNodeToTable(section.name, nodeName, nodeDef.code,
                            com.badlogic.gdx.graphics.Color.WHITE, nodeFields);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("NodeTreeParser", "Failed to load custom nodes: " + e.getMessage());
        }
    }

    private Color parseColor(String colorLine) {
        String hexColor = colorLine.substring(colorLine.indexOf("#") + 1, colorLine.indexOf("•"));
        return Color.valueOf(hexColor);
    }

    /**
     * Category entry stored in treeCategories, bundling the nodes table and its
     * live animated height so we can expand/collapse smoothly.
     */
    private static final class CategoryEntry {

        final VisTable nodesTable;      // raw content table (full height)
        final VisTable animWrapper;     // clipped wrapper whose getPrefHeight() is animated
        final float[] animHeight;       // {currentAnimatedHeight, fullMeasuredHeight}
        final boolean[] expanded;       // {isExpanded}
        final VisTextButton header;

        CategoryEntry(VisTable nodesTable, VisTable animWrapper,
                float[] animHeight, boolean[] expanded, VisTextButton header) {
            this.nodesTable = nodesTable;
            this.animWrapper = animWrapper;
            this.animHeight = animHeight;
            this.expanded = expanded;
            this.header = header;
        }
    }

    // Use CategoryEntry map instead of plain VisTable map
    private Map<String, CategoryEntry> categoryEntries = new HashMap<>();

    private void addNodeToTable(String category, String nodeName, String code,
            Color nodeColor, List<String> fields) {

        CategoryEntry entry = categoryEntries.get(category);

        if (entry == null) {
            // ── Header button ───────────────────────────────────────────────
            VisTextButton header = new VisTextButton("▶  " + category);
            header.getLabel().setAlignment(Align.left);

            // ── Nodes container (the actual content) ─────────────────────
            VisTable nodesTable = new VisTable();
            nodesTable.top().left();
            nodesTable.padLeft(16).padBottom(4);

            // ── Animated height state ────────────────────────────────────
            final float[] animHeight = {0f, 0f}; // [0]=current, [1]=full
            final boolean[] expanded = {false};

            // ── Clipped wrapper whose prefHeight is driven by animHeight[0] ─
            final VisTable animWrapper = new VisTable() {
                @Override
                public float getPrefHeight() {
                    return animHeight[0];
                }

                @Override
                public float getMinHeight() {
                    return animHeight[0];
                }
            };
            animWrapper.setClip(true);
            animWrapper.top().left();
            animWrapper.add(nodesTable).growX().top().left();

            // ── Toggle listener ──────────────────────────────────────────
            header.addListener(new ClickListener() {
                @Override
                public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                        float x, float y) {
                    boolean toExpand = !expanded[0];
                    expanded[0] = toExpand;
                    header.setText(toExpand ? "▼  " + category : "▶  " + category);

                    // Measure full height if we don't know it yet
                    if (animHeight[1] == 0f) {
                        nodesTable.pack();
                        animHeight[1] = nodesTable.getPrefHeight();
                    }

                    final float from = animHeight[0];
                    final float target = toExpand ? animHeight[1] : 0f;

                    animWrapper.clearActions();
                    final float spanAmt = target - from;
                    TemporalAction anim = new TemporalAction() {
                        @Override
                        protected void update(float percent) {
                            animHeight[0] = from + spanAmt * percent;
                            animWrapper.invalidateHierarchy();
                        }
                    };
                    anim.setDuration(0.28f);
                    anim.setInterpolation(Interpolation.smooth);
                    animWrapper.addAction(anim);
                }
            });

            // ── Assemble the outer category row ─────────────────────────
            VisTable categoryRow = new VisTable();
            categoryRow.top().left();
            categoryRow.add(header).growX().height(52).padBottom(2).row();
            categoryRow.add(animWrapper).growX().row();

            if (treeTable == null) {
                treeTable = new VisTable();
                treeTable.add().height(200).row();
            }
            treeTable.add(categoryRow).growX().row();

            entry = new CategoryEntry(nodesTable, animWrapper, animHeight, expanded, header);
            categoryEntries.put(category, entry);
            // keep treeCategories in sync (used by refresh() logic)
            treeCategories.put(category, nodesTable);
        }

        // ── Add node label to the nodes table ──────────────────────────────
        String nodeN = nodeName.contains("__star__if__")
                ? nodeName.replace("__star__if__", "")
                : nodeName;
        VisLabel nodeLabel = new VisLabel("  " + nodeN.split(" ")[0]);
        nodeLabel.setColor(Color.WHITE);

        final CategoryEntry finalEntry = entry;
        nodeLabel.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                createVisualNode(nodeName, code, fields);
            }
        });

        entry.nodesTable.add(nodeLabel)
                .left().growX()
                .padTop(6).padBottom(6)
                .minHeight(42)
                .row();

        // Invalidate stored full-height so it gets re-measured on next open
        entry.animHeight[1] = 0f;
    }

    private void createVisualNode(String nodeName, String code, List<String> fields) {
        VisualNode node;
        if (nodeName.contains("__star__if__")) {
            node = new VisualNode(nodeName.replace("__star__if__", ""), editorStage);
            node.setIsBooleanNode(true);
        } else {
            node = new VisualNode(nodeName, editorStage);
        }

        for (String field : fields) {
            node.add(new VisualNode.NodeField(field, "", uiStage));
        }

        editorStage.addActor(node);
        node.setCode(code);
        node.setPosition(editorStage.getCamera().position.x - node.getWidth() * 0.5f, editorStage.getCamera().position.y - node.getHeight() * 0.5f);
    }

    private void createUI() {
        VisTable mainTable = new VisTable();
        mainTable.setFillParent(true);
        if (treeTable == null) {
            treeTable = new VisTable();
            treeTable.add().height(200).row();
        }
        treeTable.top().left();
        treeTable.pad(15);

        // 🔧 Optional: scale everything in one go
        // treeTable.setTransform(true);
        // treeTable.setScale(1.5f);
        ScrollPane scrollPane = new ScrollPane(treeTable);
        scrollPane.setFadeScrollBars(false);

        VisLabel titleLabel = new VisLabel("Node Tree Parser (VisUI - Table Version)");
        titleLabel.setColor(Color.CYAN);
        titleLabel.setFontScale(1.5f);

        mainTable.add(titleLabel).pad(15).row();
        mainTable.add(scrollPane).expand().fill().pad(15);

        stage.addActor(mainTable);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
