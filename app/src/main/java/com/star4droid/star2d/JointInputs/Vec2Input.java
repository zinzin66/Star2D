package com.star4droid.star2d.JointInputs;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.badlogic.gdx.math.Vector2;
import java.lang.reflect.Field;

public class Vec2Input extends VisTable implements JointInput {
	public VisLabel nameLabel;
	public VisTextField xField, yField;
	
	public Vec2Input(String field, Object joint){
		super();
		init(field);
		try {
			Field fld = joint.getClass().getField(field);
			fld.setAccessible(true);
			setValue(fld.get(joint));
		} catch(Throwable ex){}
	}
	
	public Vec2Input(String field){
		super();
		init(field);
		xField.setText("0");
		yField.setText("0");
	}
	
	private void init(String field){
		nameLabel = new VisLabel(field);
		xField = new VisTextField();
		yField = new VisTextField();
		
		xField.setStyle(new VisTextField.VisTextFieldStyle(xField.getStyle()));
		xField.getStyle().fontColor = com.badlogic.gdx.graphics.Color.CYAN;
		yField.setStyle(new VisTextField.VisTextFieldStyle(yField.getStyle()));
		yField.getStyle().fontColor = com.badlogic.gdx.graphics.Color.CYAN;
		
		VisTextButton pickBtn = new VisTextButton("Pick");
		
		pickBtn.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				pick(xField, yField);
			}
		});
		
		add(nameLabel).left().row();
		VisTable content = new VisTable();
		content.add(new VisLabel("X:")).padRight(5);
		content.add(xField).width(60).padRight(10);
		content.add(new VisLabel("Y:")).padRight(5);
		content.add(yField).width(60).padRight(10);
		content.add(pickBtn);
		add(content).left().row();
	}
	
	public void pick(VisTextField xt, VisTextField yt){
		// This should be overridden or implemented if picking is needed
	}
	
	@Override
	public void setValue(Object v){
		if(v instanceof Vector2){
			Vector2 vec2= (Vector2)v;
			xField.setText(vec2.x+"");
			yField.setText(vec2.y+"");
		} else {
		    if(v.toString().contains("getWorldCenter()")){
		        xField.setText(v.toString().replace(".getBody().getWorldCenter()",""));
		        return;
		    }
			String str=v.toString().replace("f","").replace("new Vector2(","").replace(")","");
			try {
				if(str.contains("&&")){
					xField.setText(str.split("&&")[0]);
					yField.setText(str.split("&&")[1]);
				} else if(str.contains(",")){
					xField.setText(str.split(",")[0]);
					yField.setText(str.split(",")[1]);
				}
			} catch(Exception e){}
		}
	}
	
	@Override
	public String getValue() {
		try {
			Float.parseFloat(xField.getText());
			Float.parseFloat(yField.getText());
			return "new Vector2("+xField.getText()+"f&&"+yField.getText()+"f)";
		} catch(Exception ex){
		    String str = xField.getText();
		    if(str.contains("(")) return null;
		    return str+".getBody().getWorldCenter()";
		}
	}
	
	@Override
	public String getName() {
		return nameLabel.getText().toString();
	}
	
	@Override
	public String getCode() {
		String v=getValue().replace("&&",",");
		return "			%1$s."+getName()+".set("+v+");\n";
	}
}