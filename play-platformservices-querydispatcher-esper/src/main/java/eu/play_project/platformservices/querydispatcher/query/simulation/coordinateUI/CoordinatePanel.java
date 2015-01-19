/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.coordinateUI;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author ningyuan 
 * 
 * Oct 9, 2014
 *
 */
public class CoordinatePanel extends JPanel{
	 
	 private String[] points = new String[0];
	 
	 private DecimalFormat df = new DecimalFormat("0.000");
	 
	 public void setPoints(String[] p){
		 points = p;
	 }
	 
	 public void paintComponent(Graphics g) {
		 super.paintComponent(g);       
	  
	     drawPoints(g);
	 } 
	 
	 private void drawPoints(Graphics g){
		 
		 for(int i = 0; i < points.length; i++){
			 //XXX parameters must be changed for different signal
			 
			 Double p = Double.valueOf(points[i]);
			 //int y = Double.valueOf(p).intValue();
			 int y = Double.valueOf(p*1000).intValue();
			 	//System.out.print("!!!!  y "+y+" ");
			 int x = 10+i;
			 if(y > 450){
				 g.setColor(Color.RED);
				 g.drawLine(x, 10, x, 10);
			 }
			 else{
				 g.setColor(Color.RED);
				 g.drawLine(x, 450-y, x, 450-y);
			 }
			 	
		 }
		 
	 }
}
