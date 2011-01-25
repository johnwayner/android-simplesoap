package com.johnwayner.android.simplesoap;

import java.io.PrintWriter;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import com.ibm.wsdl.xml.WSDLWriterImpl;

public class MyWriter extends WSDLWriterImpl {
	
	public void writeTypes(Definition def) {
		PrintWriter pw = new PrintWriter(System.out);
		
		try {
			printTypes(def.getTypes(), def, pw);
		} catch (WSDLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
