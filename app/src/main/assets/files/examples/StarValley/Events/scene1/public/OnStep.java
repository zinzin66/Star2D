setCameraXY(player);
if(Joystick1.getPower()!=0)
            player.getBody().setTransform(player.getBody().getPosition(),(float)Math.toRadians(Joystick1.getAngleDegrees() - 180));

player.getBody().setLinearVelocity(new Vector2(Joystick1.getJoyStickX()*25,Joystick1.getJoyStickY()*25));
if(getZooming()<4){
setZoom((float)(getZooming()+0.009));

} else {

}
