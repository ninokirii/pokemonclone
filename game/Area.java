package game;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;

//the area or map that is a grid of Tiles
class Area extends Presenter {

	java.util.List<Tile> tiles = new ArrayList<Tile>();
	java.util.List<Entity> entities = new ArrayList<Entity>();
	Player player = new Player();  
	WildPokemonGenerator gen; //for grassy areas
	
	Area()
	{
	//	for(int x=0;x<12;x++)
	//		for(int y=0;y<9;y++)
	//			tiles.add(new Tile(x,y));
try{		
		Node mapNode = Node.parseFrom(new FileInputStream("palletTown.nml"));
		for(Node tileNode: mapNode.subnodes("tile"))
		{
			int x = new Integer( tileNode.contentOf("x"));
			int y = new Integer( tileNode.contentOf("y"));
			Tile t = new Tile(x,y);
			t.imageFrom(tileNode.contentOf("image"));
			tiles.add(t);
		}
}catch(Exception ex){}				
				
				
		tile(5,5).entity(player);
	}
	
	Tile tile(int x, int y)
	{
		for(Tile t: tiles)
			if(t.x==x && t.y==y) return t;
		return null;
	}
	
	void move(int dx, int dy)
	{
		Tile t = player.tile();
		t.entity(null);
		
		Tile t2 = tile(t.x+dx,t.y+dy);
		t2.entity(player);
	}
	
	void drawOn(Graphics2D g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(0,0,320,240);
		
		for(Tile t:tiles) t.drawOn(g);
	}
	
	void keyPressed(char key){
		if(key=='A') move(-1,0);
		if(key=='S') move(0,1);
		if(key=='D') move(1,0);
		if(key=='W') move(0,-1);
	}
	void step(){}
}