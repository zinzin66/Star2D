package com.star4droid.star2d.editor.ui.scripting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles loading / saving the user's custom node library from/to
 * {@code Gdx.files.external("Nodes/nodes.json")}.
 *
 * <p>
 * The JSON format is a flat array of sections:
 * <pre>
 * [
 *   {
 *     "name": "My Section",
 *     "nodes": [
 *       {
 *         "name": "setStr",
 *         "title": "Set String",
 *         "code": "%1$s.setString(%2$s)\n%3$s",
 *         "isBooleanNode": false,
 *         "fields": [
 *           { "name": "target", "value": "" },
 *           { "name": "value",  "value": "" }
 *         ]
 *       }
 *     ]
 *   }
 * ]
 * </pre>
 */
public class CustomNodeLibraryManager {

    public static final String FILE_PATH = "Nodes/nodes.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ------------------------------------------------------------------
    //  Load
    // ------------------------------------------------------------------
    /**
     * Load the library from external storage. Returns an empty list if the file
     * doesn't exist.
     */
    public static List<CustomNodeSection> load() {
        try {
            FileHandle fh = Gdx.files.external(FILE_PATH);
            if (!fh.exists()) {
                return new ArrayList<>();
            }
            String json = fh.readString("UTF-8");
            Type listType = new TypeToken<List<CustomNodeSection>>() {
            }.getType();
            List<CustomNodeSection> result = GSON.fromJson(json, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            Gdx.app.error("NodeLibrary", "Load failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ------------------------------------------------------------------
    //  Save
    // ------------------------------------------------------------------
    /**
     * Persist the full library to external storage.
     */
    public static void save(List<CustomNodeSection> sections) {
        try {
            FileHandle fh = Gdx.files.external(FILE_PATH);
            fh.writeString(GSON.toJson(sections), false);
        } catch (Exception e) {
            Gdx.app.error("NodeLibrary", "Save failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Export helpers
    // ------------------------------------------------------------------
    /**
     * Serialize a single section to a JSON string (for clipboard sharing).
     */
    public static String exportSectionJson(CustomNodeSection section) {
        // Wrap in array so importFromJson can handle both single-section and multi-section payloads
        List<CustomNodeSection> list = new ArrayList<>();
        list.add(section);
        return GSON.toJson(list);
    }

    /**
     * Serialize the entire library to a JSON string.
     */
    public static String exportAllJson(List<CustomNodeSection> sections) {
        return GSON.toJson(sections);
    }

    // ------------------------------------------------------------------
    //  Import / merge
    // ------------------------------------------------------------------
    /**
     * Parse a JSON string (produced by export) and merge its contents into
     * {@code target}.
     * <ul>
     * <li>If a section with the same name already exists, new nodes are
     * appended.</li>
     * <li>If the section doesn't exist, it's added.</li>
     * </ul>
     *
     * @return human-readable result message
     */
    public static String importFromJson(String json, List<CustomNodeSection> target) {
        try {
            Type listType = new TypeToken<List<CustomNodeSection>>() {
            }.getType();
            List<CustomNodeSection> incoming = GSON.fromJson(json, listType);
            if (incoming == null || incoming.isEmpty()) {
                return "No data found in JSON.";
            }

            int addedSections = 0, addedNodes = 0;
            for (CustomNodeSection inSection : incoming) {
                CustomNodeSection existing = findSection(target, inSection.name);
                if (existing == null) {
                    target.add(inSection);
                    addedSections++;
                    addedNodes += inSection.nodes.size();
                } else {
                    for (CustomNodeDef node : inSection.nodes) {
                        existing.nodes.add(node);
                        addedNodes++;
                    }
                }
            }
            return "Imported: " + addedSections + " new section(s), " + addedNodes + " node(s).";
        } catch (Exception e) {
            return "Import failed: " + e.getMessage();
        }
    }

    // ------------------------------------------------------------------
    //  Utilities
    // ------------------------------------------------------------------
    private static CustomNodeSection findSection(List<CustomNodeSection> list, String name) {
        for (CustomNodeSection s : list) {
            if (s.name.equals(name)) {
                return s;
            }
        }
        return null;
    }
}
