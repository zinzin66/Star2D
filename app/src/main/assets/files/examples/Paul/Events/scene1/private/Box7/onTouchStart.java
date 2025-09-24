for(int i = 0; i < 7; i++){
PlayerItem item = findItem("panel"+i);
if(item==null) continue;
if(checkCollision(player,item)){
player.getBody().applyLinearImpulse((float)(0),(float)(30000),player.getBody().getWorldCenter().x,player.getBody().getWorldCenter().y, true);
return;

} else {

}
}
if(checkCollision(player,barrel)){
player.getBody().applyLinearImpulse((float)(0),(float)(30000),player.getBody().getWorldCenter().x,player.getBody().getWorldCenter().y, true);
return;

} else {

}
