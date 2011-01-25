package com.johnwayner.android.simplesoap;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimpleSoap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WSDLFactory wsdlFactory = WSDLFactory.newInstance();
		      WSDLReader  wsdlReader  = wsdlFactory.newWSDLReader();
		      WSDLWriter  wsdlWriter  = wsdlFactory.newWSDLWriter();

		      Definition def = wsdlReader.readWSDL(null, "/media/Data/dev/UnitedRoadsShipperAndroid/soap_stuff/service.wsdl");		      
		      
		      Types types = def.getTypes();
		      List<Schema> elems = types.getExtensibilityElements();
		      
		      List<ComplexType> outputTypes = new ArrayList<ComplexType>();
		      
		      for(Schema elem : elems) {
		    	  NodeList complexTypes =  elem.getElement().getChildNodes();
		    	  for(int i=0; i<complexTypes.getLength(); i++) {
		    		  Node type = complexTypes.item(i);
		    		  if("s:element".equals(type.getNodeName())) {
		    			  ComplexType ctype = new ComplexType();
		    			  outputTypes.add(ctype);
		    			  ctype.name = type.getAttributes().getNamedItem("name").getNodeValue();
		    			  Node seq = type.getChildNodes().item(1).getChildNodes().item(1);
		    			  if("s:sequence".equals(seq.getNodeName())) {
		    				  NodeList fields = seq.getChildNodes();
		    				  for(int f=0; f<fields.getLength(); f++) {
		    					  if("s:element".equals(fields.item(f).getNodeName())) {
		    						  NamedNodeMap attrs = fields.item(f).getAttributes();
		    						  ctype.fields.add(
		    								 new Field(
		    										attrs.getNamedItem("name").getNodeValue(),
		    										wsdlType2JavaType(attrs.getNamedItem("type").getNodeValue()),
		    										!attrs.getNamedItem("maxOccurs").getNodeValue().equals("1")));
		    					  }
		    				  }
		    			  }
		    		  }
		    		  if("s:complexType".equals(type.getNodeName())) {
		    			  ComplexType ctype = new ComplexType();
		    			  outputTypes.add(ctype);
		    			  ctype.name = type.getAttributes().getNamedItem("name").getNodeValue();
		    			  Node seq = type.getChildNodes().item(1);
		    			  if("s:sequence".equals(seq.getNodeName())) {
		    				  NodeList fields = seq.getChildNodes();
		    				  for(int f=0; f<fields.getLength(); f++) {
		    					  if("s:element".equals(fields.item(f).getNodeName())) {
		    						  NamedNodeMap attrs = fields.item(f).getAttributes();
		    						  ctype.fields.add(
		    								 new Field(
		    										attrs.getNamedItem("name").getNodeValue(),
		    										wsdlType2JavaType(attrs.getNamedItem("type").getNodeValue()),
		    										!attrs.getNamedItem("maxOccurs").getNodeValue().equals("1")));
		    					  }
		    				  }
		    			  }
		    		  }
		    	  }
		      }
		      Properties prop = new Properties();
		      prop.load(SimpleSoap.class.getResourceAsStream("/com/johnwayner/android/simplesoap/velocity.config"));
		      Velocity.init(prop);
		      
		      VelocityContext context = new VelocityContext();
		      Template complexTypeTemplate = Velocity.getTemplate("com/johnwayner/android/simplesoap/templates/ComplexType.vm");
		      for(ComplexType type : outputTypes) {
		    	  context.put("type", type);
		    	  context.put("package", "com.unitedroads.android.shipper.soap.simple.types");
		    	  File output = new File("/media/Data/dev/UnitedRoadsShipperAndroid/UnitedRoadsShipperAndroid/src/com/unitedroads/android/shipper/soap/simple/types/" + type.name + ".java");
		    	  System.out.println("output: " + output.getAbsolutePath());
		    	  FileWriter fw = new FileWriter(output);
		    	  complexTypeTemplate.merge(context, fw);
		    	  fw.close();
		      }
		      
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	public static final Map<String, String> wsdlTypes2JavaTypes = new HashMap<String, String>();
	static {
		wsdlTypes2JavaTypes.put("s:string", "String");
		wsdlTypes2JavaTypes.put("s:boolean", "Boolean");
		wsdlTypes2JavaTypes.put("s:int", "Integer");
		wsdlTypes2JavaTypes.put("s:double", "Double");
		wsdlTypes2JavaTypes.put("s:date", "Date");
	}
	public static String wsdlType2JavaType(String wsdlType) {
		if(wsdlTypes2JavaTypes.containsKey(wsdlType)) {
			return wsdlTypes2JavaTypes.get(wsdlType);
		} else {
			if(wsdlType.startsWith("tns:")) {
				return wsdlType.substring(4);
			} 
		}
		
		throw new RuntimeException("Unmapped type: " + wsdlType);
	}
}
