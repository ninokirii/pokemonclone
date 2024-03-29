package game;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Image;
import javax.swing.*;
import java.io.*;
import java.util.*;

import static game.Button.*;

/**
 *A grid of Tiles in the game, also known as a map.
 *
 *
 */
class Area extends Presenter
{

	private List<Tile> tiles = new ArrayList<Tile>(); 
	private String name;

	public Tile tilePlayerFacing(int n)
	{
		Direction d = player().direction();
		Tile now = player().tile();
		Tile next = tileAt(now.x+d.dx()*n,now.y+d.dy()*n);
		return next;
	}
	
	/**
	 *Find the tile with the given coordinates
	 */
	public Tile tileAt(int x, int y)
	{
		for(Tile t: tiles)
			if(t.x==x && t.y==y) return t;
		return null;
	}
	
	private void walk()
	{
		Tile now = player().tile();
		Tile next = tilePlayerFacing(1);
		
		if(next==null)return; //edge of map
		if(next.entity()!=null) return; //occupied
		if(next.isObstacle())
		{
			if(!next.target.isEmpty()) showMessage(next.target); //sign
			return;
		}
		if(next.isCliff() && cliffsOn)
		{
			if(player().direction()==next.cliffDirection())
				next = tilePlayerFacing(2);
			else 
				return;
		}
		if(next.isDoor())
		{
			System.out.println("Jump to "+next.target);
			Area a = Area.named(next.targetMap());
			a.tileAt(next.targetX(),next.targetY()).entity(player());
			enterPresenter(a);
			return;
		}
		if(next.isGrass() && grassOn)
		{
			Pokemon p = now.genPokemon();
			if(p!=null)
				enterPresenter(new Battle(p,this)); //open battle
		}

		player().startStride();
		next.entity(player());
		repaint();
	}
	
	/**
	 *Move player on arrow keys, enter pokedex on Q.
	 */
	public void buttonPressed(Button b){
		if(player().inStride())	return;
		if(b==LEFT){ player().direction(Direction.WEST); walk(); }
		if(b==DOWN){ player().direction(Direction.SOUTH); walk(); }
		if(b==RIGHT){ player().direction(Direction.EAST); walk(); }
		if(b==UP){ player().direction(Direction.NORTH); walk(); }
		if(b==START)
		{
			String choice = showMenu(new String[]{"Pokedex","Pokemon","Pack","Ash","Save","Option","Debug","Exit"});
			
			if(choice.equals("Pokedex")) enterPresenter(new PokedexPresenter(this));
			if(choice.equals("Debug")) doDebugMenu();
		}
		if(b==A)
		{
			Tile next = tilePlayerFacing(1);
			
			if (next.isObstacle() && !next.isWater() && !next.target.isEmpty()) showMessage(next.target);
		}
		if(b==B)
		{
			
		}
	}
	
	private static boolean cliffsOn = true;
	private static boolean grassOn = true;
	private static boolean zoom = true;

	private void doDebugMenu()
	{
		String x = showMenu(new String[]{"CliffsOn ("+cliffsOn+")","GrassOn ("+grassOn+")","Zoom ("+zoom+")","Teleport","Trigger Wild","Cancel"});
		
		if(x.startsWith("Cliffs")){
			cliffsOn = !cliffsOn;
			showMessage("Now "+cliffsOn);
		}
		else if(x.startsWith("Grass")){
			grassOn = !grassOn;
			showMessage("Now "+grassOn);
		}
		else if(x.startsWith("Zoom")){
			zoom = !zoom;
			showMessage("Now "+zoom);
		}
		else if(x.equals("Teleport")) {
		
			String agoto = showMenu("Teleport where?",new String[]{"pallet","PewterCity","SaffronCity","FuchsiaCity","NewBarkTown"});
			Area a = Area.named(agoto);
			if(agoto.equals("pallet")) a.tileAt(5,7).entity(player());
			if(agoto.equals("PewterCity")) a.tileAt(13,28).entity(player());
			if(agoto.equals("SaffronCity")) a.tileAt(10,31).entity(player());
			if(agoto.equals("FuchsiaCity")) a.tileAt(20,30).entity(player());
			if(agoto.equals("NewBarkTown")) a.tileAt(25,15).entity(player());
			enterPresenter(a);
		} else if(x.equals("Trigger Wild")) {
		
			List<String> names = new LinkedList<String>();
			for(Species s: Species.all()) names.add(s.name());
			
			String what = showMenu( names.toArray(new String[1]) );
			
			enterPresenter(new Battle( Species.named(what).makeWildAtLevel(20),this)); //open battle
		}
		
	}
	
	
	
	/**
	 *Draw black background, center the player, then draw each tile.
	 */
	public void drawOn(Graphics2D g)
	{
		if(zoom)g.scale(2.0,2.0);
		Player player = player();
		
		//fill background with black
		g.setColor(Color.BLACK);
		g.fillRect(0,0,16*20+2,16*18+2);
		
		
		//make the player the center
		int yf = ((player.direction()==Direction.NORTH || player.direction()==Direction.SOUTH && player.inStride()) ? -8 : 0);
		int xf = ((player.direction()==Direction.EAST || player.direction()==Direction.WEST && player.inStride()) ? 8 : 0);
		yf=xf=0;
		
		int cx = (zoom ? 4 : 10 );
		int cy = (zoom ? 4 : 10 );
		
		g.translate( (player.tile().x-cx)*-16-xf, (player.tile().y-cy)*-16-yf);

		//draw each tile
		for(Tile t:tiles) t.drawSelfOn(g);
		for(Tile t:tiles) t.drawEntityOn(g);
		
		g.translate( -((player.tile().x-cx)*-16-xf), -((player.tile().y-cy)*-16-yf));
		
		if(zoom)g.scale(0.5,0.5);
	}
	
	/**
	 *Step each tile
	 */
	public void step(int ms){
		for(Tile t:tiles) t.step(ms);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	
	public void gotFocus()
	{
		new Thread(){
			public void run()
			{
				for(Tile t:tiles)
				{
					if(t.isDoor()) Area.named(t.targetMap());
				}
			}
		}.start();
	}
	
	private Area(String x) throws Exception
	{
		XmlElement mapE = XmlElement.parseFrom(Game.jarStream(x));
		name = mapE.contentOf("name");
		
		for(XmlElement tileE: mapE.children("tile"))
		{
			tiles.add(Tile.fromXml(tileE, this));
		}
		if(tiles.size()==0) throw new Exception();

		System.out.println("Loaded Area: "+name);
	}
	
	/**
	 *Returns a loaded map with the given name, only maps in the areas directory will be loaded.
	 */
	public static synchronized Area named(String name){
		for(Area a: areas)
			if(a.name.equalsIgnoreCase(name))
				return a;
				
		try{
			Area a = new Area("areas/"+name+".xml");
			areas.add(a);
			return a;
		}catch(Exception exx){}
		try{
			Area a = new Area("areas/"+name+".nml");
			areas.add(a);
			return a;
		}catch(Exception exx){}
				
		System.out.println("There is no area named "+name);
		return null;
	}
	private static LinkedList<Area> areas = new LinkedList<Area>();

}