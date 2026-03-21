package com.star4droid.star2d.JointInputs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.star4droid.star2d.Helpers.JointsHelper;
import com.star4droid.star2d.editor.LibgdxEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class JointDialog extends VisWindow implements JointInput {
	VisTable linear;
	VisTextField nameField;
	VisTextButton addButton, cancelButton;
	Object object;
	Object toSet;
	public static String allowedChars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_";
	boolean loadDone=false;
	LibgdxEditor editor;
	
	public JointDialog(String joint,String nm, LibgdxEditor editor){
		super(nm.isEmpty() ? "Add Joint" : "Edit Joint");
		this.editor = editor;
		setModal(true);
		setCenterOnAdd(true);
		addCloseButton();
		
		linear = new VisTable();
		linear.columnDefaults(0).left();
		
		nameField = new VisTextField(nm);
		if(!nm.equals("")){
			nameField.setDisabled(true);
		}
		
		VisTable nameTable = new VisTable();
		nameTable.add(new VisLabel("Name: ")).padRight(5);
		nameTable.add(nameField).growX();
		linear.add(nameTable).growX().padBottom(10).row();
		
		setup(joint, nm, editor);
		
		ScrollPane scrollPane = new ScrollPane(linear);
		scrollPane.setFadeScrollBars(false);
		add(scrollPane).grow().width(400).height(500).row();
		
		VisTable buttonTable = new VisTable();
		addButton = new VisTextButton(nm.equals("") ? "Add" : "Edit");
		cancelButton = new VisTextButton("Cancel");
		
		buttonTable.add(addButton).padRight(10);
		buttonTable.add(cancelButton);
		add(buttonTable).padTop(10).row();
		
		cancelButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				close();
			}
		});
		
		addButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				String value = getValue();
				if(value!=null) {
					if(nameField.getText().isEmpty()) return;
					for(char c:nameField.getText().toCharArray()){
						if(!allowedChars.contains(String.valueOf(c))){
							// Show error? 
							return;
						}
					}
					onDone(value,nameField.getText());
					close();
				}
			}
		});
		
		pack();
		centerWindow();
		if(getStage()==null && editor.getUiStage()!=null){
		    editor.getUiStage().addActor(this);
		}
	}
	
	public void setup(final String joint,final String nm,final LibgdxEditor editor){
		try {
			//constructor params...(from json file)
			int i=0;
			ArrayList<String> list= new ArrayList<>();
			String[] names=JointsHelper.get(joint,"params").split(",");
			for(String str:JointsHelper.get(joint,"types").split(",")){
				list.add(names[i].toLowerCase());
				switch(str.toLowerCase()){
					case "vector2":
					linear.add(new Vec2Input(names[i]){
						@Override
						public void pick(final VisTextField tx,final VisTextField ty){
						    JointDialog.this.setVisible(false);
							// Utils.showMessage(ctx,"Select point from the screen");
							editor.setOnPick((x,y)->{
							    Gdx.app.postRunnable(()->{
    								tx.setText(x+"");
    								ty.setText(y+"");
    								JointDialog.this.setVisible(true);
							    });
							});
						}
					}).growX().row();
					break;
					case "float":
					linear.add(new FloatInput(names[i])).growX().row();
					break;
					case "body":
					linear.add(new BodyInput(names[i],editor)).growX().row();
					break;
				}
				i++;
			}
			//fields
			String className = "com.badlogic.gdx.physics.box2d.joints."+joint+(joint.endsWith("Def") ? "" : "Def");
			Class<?> clazz = Class.forName(className);
			Constructor<?> cc = clazz.getConstructor();
			object = cc.newInstance();
			for(Field field:clazz.getFields()){
				if(java.lang.reflect.Modifier.isFinal(field.getModifiers()) && !com.badlogic.gdx.math.Vector2.class.isAssignableFrom(field.getType())) continue;
				if(list.contains(field.getName().toLowerCase())) continue;
				if(field.getType() == float.class || field.getType() == Float.class){
					linear.add(new FloatInput(field.getName(),object)).growX().row();
				} else if(com.badlogic.gdx.math.Vector2.class.isAssignableFrom(field.getType())){
					linear.add(new Vec2Input(field.getName(),object){
						@Override
						public void pick(final VisTextField tx,final VisTextField ty){
							JointDialog.this.setVisible(false);
							// Utils.showMessage(ctx,"Select point from the screen");
							editor.setOnPick((x,y)->{
							    Gdx.app.postRunnable(()->{
    								tx.setText(x+"");
    								ty.setText(y+"");
    								JointDialog.this.setVisible(true);
							    });
							});
						}
					}).growX().row();
				} else if(field.getType() == boolean.class || field.getType() == Boolean.class){
					linear.add(new CheckboxInput(field.getName(),object)).left().row();
				}
			}
			
			loadDone = true;
			if(toSet!=null) setValue(toSet);
			
		} catch(final Throwable ex){
			ex.printStackTrace();
		}
	}
	
	@Override
	public void setValue(Object v){
		if((!loadDone)||linear==null){
			toSet = v;
			return;
		}
		try {
		if(v instanceof String){
			String str=v.toString();
			if(str.equals("")) return;
			HashMap<String,JointInput> jmap= new HashMap<>();
			ArrayList<HashMap<String, Object>> fields = new Gson().fromJson(str,new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
			for(Actor view : linear.getChildren()){
				if(view instanceof JointInput){
					JointInput jn = ((JointInput)view);
					jmap.put(jn.getName(),jn);
				}
			}
			for(HashMap<String,Object> hash:fields){
			    if(jmap.containsKey(hash.get("name").toString()))
				    jmap.get(hash.get("name").toString()).setValue(hash.get("value"));
			}
		}
		} catch(Exception ex){
			toSet = v;
		}
	}
	
	public void onDone(String string,String name){
		
	}
	
	@Override
	public String getValue() {
		try {
			ArrayList<HashMap<String,Object>> arrayList= new ArrayList<>();
				
				for(Actor view : linear.getChildren()){
					if(!(view instanceof JointInput)) continue;
					JointInput jn=(JointInput)view;
					HashMap<String,Object> hash=new HashMap<>();
					if(jn.getValue()==null) {
						// Utils.showMessage(getContext(),"Error..!");
						return null;
					}
					hash.put("value",jn.getValue());
					hash.put("name",jn.getName());
					hash.put("code",jn.getCode());
					arrayList.add(hash);
				}
			return new Gson().toJson(arrayList);
		} catch(Exception ex){}
		return null;
	}
	
	@Override
	public String getName() {
		return nameField.getText();
	}
	
	@Override
	public String getCode() {
		return null;
	}
	
	public static void showJointListDialog(final LibgdxEditor editor, final Runnable runnable){
		final VisDialog dialog = new VisDialog("Select Joint");
		dialog.setModal(true);
		dialog.addCloseButton();
		
		VisTable content = new VisTable();
		
		for(HashMap<String,Object> hash:JointsHelper.getJointsListMap()){
			if(hash.get("joint").toString().contains("GearJoint")) continue;
			 final String jointName = hash.get("joint").toString();
			final VisTextButton button = new VisTextButton(jointName.replace("Def",""));
			
			button.addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y){
					dialog.fadeOut();
					JointDialog jointDialog = new JointDialog(jointName, "", editor){
						public void onDone(String string,String name){
							Gdx.files.absolute(editor.getProject().getJoints(editor.getScene())+name+"-"+button.getText().toString()).writeString(string,false);
							runnable.run();
						}
					};
					editor.getUiStage().addActor(jointDialog);
				}
			});
			content.add(button).growX().pad(5).row();
		}
		
		ScrollPane pane = new ScrollPane(content);
		dialog.add(pane).width(300).height(400);
		
		dialog.show(editor.getUiStage());
	}
}