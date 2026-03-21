package com.star4droid.star2d.editor.ui.scripting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CustomBlockLibraryManager {

    public static final String FILE_PATH = "Nodes/blocks.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<CustomBlockSection> load() {
        try {
            FileHandle fh = Gdx.files.external(FILE_PATH);
            if (!fh.exists()) {
                return new ArrayList<>();
            }
            String json = fh.readString("UTF-8");
            Type listType = new TypeToken<List<CustomBlockSection>>() {
            }.getType();
            List<CustomBlockSection> result = GSON.fromJson(json, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            Gdx.app.error("BlockLibrary", "Load failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void save(List<CustomBlockSection> sections) {
        try {
            FileHandle fh = Gdx.files.external(FILE_PATH);
            fh.writeString(GSON.toJson(sections), false);
        } catch (Exception e) {
            Gdx.app.error("BlockLibrary", "Save failed: " + e.getMessage());
        }
    }

    public static String exportSectionJson(CustomBlockSection section) {
        List<CustomBlockSection> list = new ArrayList<>();
        list.add(section);
        return GSON.toJson(list);
    }

    public static String exportAllJson(List<CustomBlockSection> sections) {
        return GSON.toJson(sections);
    }

    public static String importFromJson(String json, List<CustomBlockSection> target) {
        try {
            Type listType = new TypeToken<List<CustomBlockSection>>() {
            }.getType();
            List<CustomBlockSection> incoming = GSON.fromJson(json, listType);
            if (incoming == null || incoming.isEmpty()) {
                return "No data found in JSON.";
            }

            int addedSections = 0, addedBlocks = 0;
            for (CustomBlockSection inSection : incoming) {
                CustomBlockSection existing = findSection(target, inSection.name);
                if (existing == null) {
                    target.add(inSection);
                    addedSections++;
                    addedBlocks += inSection.blocks.size();
                } else {
                    for (CustomBlockDef block : inSection.blocks) {
                        existing.blocks.add(block);
                        addedBlocks++;
                    }
                }
            }
            return "Imported: " + addedSections + " new section(s), " + addedBlocks + " block(s).";
        } catch (Exception e) {
            return "Import failed: " + e.getMessage();
        }
    }

    private static CustomBlockSection findSection(List<CustomBlockSection> list, String name) {
        for (CustomBlockSection s : list) {
            if (s.name.equals(name)) {
                return s;
            }
        }
        return null;
    }
}
