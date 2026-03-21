package com.star4droid.star2d.JointInputs;

import com.kotcrab.vis.ui.widget.VisCheckBox;
import java.lang.reflect.Field;

public class CheckboxInput extends VisCheckBox implements JointInput {
	
	public CheckboxInput(String field, Object joint){
		super(field);
		try {
			Field fld = joint.getClass().getField(field);
			fld.setAccessible(true);
			setValue(fld.get(joint).toString());
		} catch(Throwable ex){}
	}
	
	public CheckboxInput(String field){
		super(field);
		setChecked(false);
	}
	
	@Override
	public void setValue(Object v){
		setChecked(v.toString().equals("true"));
	}
	
	@Override
	public String getValue() {
		return String.valueOf(isChecked());
	}
	
	@Override
	public String getName() {
		return getText().toString();
	}
	
	@Override
	public String getCode(){
		return "\n			%1$s."+getName()+"="+getValue()+";\n";
	}
	
}