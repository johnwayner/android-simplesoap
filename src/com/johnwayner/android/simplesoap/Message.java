package com.johnwayner.android.simplesoap;

public class Message {
	public String name;
	public String inputType;
	public String outputType;
	public Message(String name, String inputType, String outputType) {
		super();
		this.name = name;
		this.inputType = inputType;
		this.outputType = outputType;
	}
	public Message() {
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInputType() {
		return inputType;
	}
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}
	public String getOutputType() {
		return outputType;
	}
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}
	
	
}
