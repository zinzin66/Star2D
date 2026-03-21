package com.star4droid.star2d.editor.ui.scripting;

import java.util.ArrayList;
import java.util.List;

/**
 * A single custom node definition stored in the user's library.
 */
public class CustomNodeDef {

    /**
     * Internal name used as the node's editor identifier (e.g. "setString").
     */
    public String name = "";

    /**
     * Title displayed on the visual block header.
     */
    public String title = "";

    /**
     * Code template used for code generation. Uses Java String.format
     * placeholders: %1$s, %2$s, etc. The last placeholder is always the "next"
     * node code. Example: "%1$s.setString(%2$s)\n%3$s"
     */
    public String code = "";

    /**
     * Whether this node has true/false (boolean) outputs.
     */
    public boolean isBooleanNode = false;

    /**
     * Field definitions. Each entry is a {name, value} pair.
     */
    public List<FieldDef> fields = new ArrayList<>();

    public CustomNodeDef() {
    }

    public CustomNodeDef(String name, String title, String code) {
        this.name = name;
        this.title = title;
        this.code = code;
    }

    // ---- nested ----
    public static class FieldDef {

        public String name = "";
        public String value = "";

        public FieldDef() {
        }

        public FieldDef(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
