package sharif.ce.isl.rl.graph.environment;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;



public class Display extends JFrame{
	
	private final double[][] world;
	
	public Display(double[][] world) {
		this.world = world;
		this.setVisible( true ) ;
		this.setSize( 1024, 768) ;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		setBackground( Color.blue );
		
	}

	public void paint( Graphics g ) {
	   
		double min = 10000000;
		for(int i = 0; i < world.length; i++)
			for(int j = 0; j < world[0].length; j++)
				if(world[i][j] < min && world[i][j]>0)
					min = world[i][j];
	   	
		
		double max = world[0][0];
		for(int i = 0; i < world.length; i++)
			for(int j = 0; j < world[0].length; j++)
				if(world[i][j] > max)
					max = world[i][j];
		
		double cof = 200.0/(max - min);				
		int rgb;   
		for(int i = 0; i < world.length; i++)
			for(int j = 0; j < world[0].length; j++){
				if(world[i][j] == 0){
					g.setColor(Color.WHITE);
					g.fillRect(j*20 + 20 , i*20 + 20, 20, 20);
					continue;
				}
				
				rgb = (int)((world[i][j] - min)* cof) + 50;
				g.setColor(  new Color(rgb, rgb, rgb));
				g.fillRect(j*20 + 20 , i*20 + 20, 20, 20);
		   }		        
   	}

}
