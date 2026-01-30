package com.star4droid.star2d.JointInputs;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.Validators;
import java.lang.reflect.Field;

public class FloatInput extends VisTable implements JointInput {
	public VisLabel nameLabel;
	public VisTextField valueField;

	public FloatInput(String field, Object joint){
		super();
		init(field);
		try {
			Field fld = joint.getClass().getField(field);
			fld.setAccessible(true);
			setValue(fld.get(joint).toString());
		} catch(Throwable ex){}
	}
	
	public FloatInput(String field){
		super();
		init(field);
		valueField.setText("0");
	}
	
	private void init(String field){
		nameLabel = new VisLabel(field);
		valueField = new VisTextField();
		valueField.setStyle(new VisTextField.VisTextFieldStyle(valueField.getStyle()));
		valueField.getStyle().fontColor = com.badlogic.gdx.graphics.Color.CYAN;
		
		add(nameLabel).left().row();
		add(valueField).growX().row();
	}
	
	@Override
	public void setValue(Object v){
		valueField.setText(v.toString().replace("f",""));
	}
	
	@Override
	public String getValue() {
		try {
			Float.parseFloat(valueField.getText());
			return valueField.getText();
		} catch(Exception ex){
			return "0";
		}
	}
	
	@Override
	public String getName() {
		return nameLabel.getText().toString();
	}
	
	@Override
	public String getCode() {
		return "			%1$s."+getName()+"="+getValue()+"f;\n";
	}
}