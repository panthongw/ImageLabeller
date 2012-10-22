package hci;

import hci.utils.*;
import java.util.ArrayList;

public class Label{
	
	private ArrayList<Point> polygon;
	private String labelName;

	public Label(ArrayList<Point> poly, String name){

		this.polygon = new ArrayList<Point>();
		for(int i = 0; i < poly.size(); i++){
			this.polygon.add(poly.get(i));
		}
		this.labelName = name;
	}

	public Label(){
		this.polygon = null;
		this.labelName = null;
	}

	public ArrayList<Point> getPolygon(){
		return this.polygon;
	}

	public String getLabel(){
		return this.labelName;
	}

	public void setPolygon(ArrayList<Point> newPoly){
		this.polygon = newPoly;
	}

	public void setLabel(String newName){
		this.labelName = newName;
	}
}