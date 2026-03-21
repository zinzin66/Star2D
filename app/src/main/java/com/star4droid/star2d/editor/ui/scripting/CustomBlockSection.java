package com.star4droid.star2d.editor.ui.scripting;

import java.util.ArrayList;
import java.util.List;

public class CustomBlockSection {

    public String name = "";
    public List<CustomBlockDef> blocks = new ArrayList<>();

    public CustomBlockSection() {
    }

    public CustomBlockSection(String name) {
        this.name = name;
    }
}
