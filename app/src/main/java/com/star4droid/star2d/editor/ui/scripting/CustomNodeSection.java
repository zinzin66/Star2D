package com.star4droid.star2d.editor.ui.scripting;

import java.util.ArrayList;
import java.util.List;

/**
 * A named group of custom node definitions.
 */
public class CustomNodeSection {

    public String name = "";
    public List<CustomNodeDef> nodes = new ArrayList<>();

    public CustomNodeSection() {
    }

    public CustomNodeSection(String name) {
        this.name = name;
    }
}
