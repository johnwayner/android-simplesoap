package com.johnwayner.android.simplesoap;

import java.util.ArrayList;
import java.util.List;

public class ComplexType {
	public String name;
	public List<Field> fields = new ArrayList<Field>();
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public List<Field> getFields() {
		return fields;
	}


	public void setFields(List<Field> fields) {
		this.fields = fields;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name);
		sb.append("\n");
		for(Field f : fields) {
			sb.append("\t").append(f).append("\n");
		}
		return sb.toString();
	}
}
