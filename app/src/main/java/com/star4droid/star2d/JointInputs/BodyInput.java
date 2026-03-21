package com.star4droid.star2d.JointInputs;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.star4droid.star2d.editor.LibgdxEditor;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;

public class BodyInput extends VisTable implements JointInput {
	VisSelectBox<String> selectBox;
	VisLabel nameLabel;
	Array<String> bodies;

	public BodyInput(String nm, LibgdxEditor editor){
		super();
		nameLabel = new VisLabel(nm);
		selectBox = new VisSelectBox<>();
		bodies = new Array<>();
		for(String s : editor.getBodiesList()) bodies.add(s);
		selectBox.setItems(bodies);
		
		add(nameLabel).padRight(10);
		add(selectBox).growX().row();
	}
	
	@Override
	public String getValue() {
		try {
			return selectBox.getSelected() + ".getBody()";
		} catch(Exception ex){}
		return null;
	}

	@Override
	public void setValue(Object object) {
		try {
			String ob = object.toString().replace(".getBody()","");
			selectBox.setSelected(ob);
		} catch(Exception e){
			// e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return nameLabel.getText().toString();
	}
	
	@Override
	public String getCode() {
		return getValue() + ".getBody()";
	}
}