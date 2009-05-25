package game;

import java.awt.*;
import javax.swing.*;
import java.util.*;

class Battle extends Presenter {

	private Battler ash;
	private Battler enemy;
	
	private ArrayList<Pokemon> yourParty;
	private Pokemon ashsPokemon;
	private Pokemon enemyPokemon;
	private int stage = 0;
	
	private int[][] menuPoints = {{140,155},{220,240}};//{X's},{Y's}
	private int menuIndexX,menuIndexY;
	
	//images
	private ImageIcon enemyBar = new ImageIcon("./resources/battle/enemybar.png");
	private ImageIcon ashImage = new ImageIcon("./resources/battle/ash.png");
	private ImageIcon bottomFrame = new ImageIcon("./resources/battle/bottomframe.png");
	private ImageIcon battleMenu = new ImageIcon("./resources/battle/battlemenu.png");
	private ImageIcon playerBar = new ImageIcon("./resources/battle/playerbar.png");
	private ImageIcon cursor = new ImageIcon("./resources/arrow.png");
	
	private Pokemon p; 
	private Presenter oldP;
	public Battle(Pokemon p, Presenter oldP)
	{	
		menuIndexX = 0;
		menuIndexY = 0;
		this.p=p; this.oldP=oldP;
		enemyPokemon = p;
		//remove this
		yourParty = new ArrayList<Pokemon>();
		yourParty.add(Species.named("Venusaur").makeWildAtLevel(100));
	}
	
	public void drawOn(Graphics2D g){
		g.setColor(Color.WHITE);
		g.fillRect(0,0,16*20,16*18);
		g.setColor(Color.BLACK);
		g.setFont(new Font("Courier New",Font.BOLD,20));
		final int TEXTX = 15;
		final int FULLHEALTH = 97;
		
		//health bars
		double enemyHealthRatio = enemyPokemon.getCurrentHP() /enemyPokemon.getBaseHP();
		double playerHealthRatio = 0;
		
		//images
		g.drawImage(enemyBar.getImage(),10,20,null);
		g.drawImage(playerBar.getImage(),155,145,null);
		g.drawImage(bottomFrame.getImage(),0,195,null);		
		g.drawImage(p.species().imageFront(),170,0,128,128,null);
		g.drawString(enemyPokemon.nickname(),10,15);
		if (ashsPokemon != null){
			g.drawImage(ashsPokemon.species().imageBack(),10,65,128,128,null);
			playerHealthRatio = ashsPokemon.getCurrentHP() / ashsPokemon.getBaseHP();
			g.drawString(ashsPokemon.nickname(),170,140);
		}
		
		
		
		if ( enemyHealthRatio > .5)g.setColor(Color.GREEN);
		else if (enemyHealthRatio > .25)g.setColor(Color.YELLOW);
		else g.setColor(Color.RED);
		g.fillRect(56,23,(int)(FULLHEALTH * enemyHealthRatio),7);
		
		if ( playerHealthRatio > .5)g.setColor(Color.GREEN);
		else if (playerHealthRatio > .25)g.setColor(Color.YELLOW);
		else g.setColor(Color.RED);
		g.fillRect(203,146,(int)(FULLHEALTH * playerHealthRatio),7);
		
		g.setColor(Color.BLACK);
		
		if (stage < 30) //3 seconds, intro to battle
		{			
			g.drawString("A WILD "+p.nickname(),TEXTX,225);
			g.drawString("HAS APPEARED!!",TEXTX,250);
			g.drawImage(ashImage.getImage(),10,100, null);			
		}
		else if (stage < 45){ //sending out players pokemon
			ashsPokemon = yourParty.get(0);
			g.drawString("GO..." + ashsPokemon.species().name(),TEXTX,225);
		}
		else{
			g.drawImage(battleMenu.getImage(),125,195,null);
			g.drawImage(cursor.getImage(),140 + menuIndexX * 100,220 + menuIndexY * 35,null);
		}
		
		
	}
	public void buttonPressed(Button b){
		if(stage > 3 /*&& stage < someothernum*/){
			if(b==Button.UP){if(menuIndexY ==1) menuIndexY = 0;}
			else if (b==Button.DOWN){if(menuIndexY == 0)menuIndexY = 1;}
			else if (b==Button.LEFT){if(menuIndexX == 1)menuIndexX = 0;}
			else if (b==Button.RIGHT){if(menuIndexX == 0)menuIndexX = 1;}
			
			else if(b==Button.START){
				
			}
			else enterPresenter(oldP);
		}
	}
	public void step(int ms){
		stage++;
	}

}