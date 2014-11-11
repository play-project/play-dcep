/**
 * 
 */
package eu.play_project.platformservices.querydispatcher.query.simulation.coordinateUI;

import java.awt.Color;
import java.awt.Graphics;
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
	 
	 public void setPoints(String[] p){
		 points = p;
	 }
	 
	 public void paintComponent(Graphics g) {
		 super.paintComponent(g);       
	  
	     drawPoints(g);
	 } 
	 
	 private void drawPoints(Graphics g){
		 
		 for(int i = 0; i < points.length && i < 1000; i++){
			 int y = Integer.valueOf(points[i]);
			 int x = 10+i;
			 if(y > 600){
				 g.setColor(Color.RED);
				 g.drawLine(x, 10, x, 10);
			 }
			 else{
				 g.setColor(Color.BLACK);
				 g.drawLine(x, 810-y, x, 810-y);
			 }
			 	
		 }
		 
	 }
}
