package com.johnwayner.android.simplesoap;

public class Field {
	public String name;
	public String type;
	public boolean isList;
	public Field(String name, String type, boolean isList) {
		super();
		this.name = name;
		this.type = type;
		this.isList = isList;
	}
	
	
	
	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getType() {
		if(isList) {
			return "List<" + type + ">";
		} else {
			return type;
		}
	}



	public void setType(String type) {
		this.type = type;
	}



	public boolean isList() {
		return isList;
	}



	public void setList(boolean isList) {
		this.isList = isList;
	}



	@Override
	public String toString() {
		if(isList) {
			return "List<" + type + "> " + name;
		} else {
			return type + " " + name;
		}
	}
}
