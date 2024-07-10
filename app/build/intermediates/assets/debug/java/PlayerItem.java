package com.star4droid.template.Utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.star4droid.star2d.ElementDefs.ElementEvent;

public interface PlayerItem {
	public void update();
	public Body getBody();
	public String getName();
	public String getParentName();
	public PlayerItem getChild(String child);
	public void setScript(ItemScript script);
	public <T extends ItemScript> T getScript();
	public ChildsHolder getChildsHolder();
	public void setParent(PlayerItem item);
	public void addChild(PlayerItem item);
	public PropertySet<String,Object> getProperties();
	public Actor getClone(String newName);
	public void setItemText(String text);
    public void setProgress(int progress);
	public int getProgress();
    public void setMax(int max);
	public float getJoyStickX();
	public float getJoyStickY();
	public double getAngle();
	public double getPower();
	public double getAngleDegrees();
	public void destroy();
	public float getBodyX();
	public float getBodyY();
	public float distToPoint(float x,float y);
	public float getActorX();
	public float getActorY();
	public float distTo(PlayerItem item);
	public boolean isExsits(String file);
	public Actor getActor();
	public void setImage(FileHandle fileHandle);
	public void setImage(Texture texture);
	public void setAnimation(String name);
	public void setAnimation(Animation animation);
	public void removeAnimation();
	public ElementEvent getElementEvents();
}