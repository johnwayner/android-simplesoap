package com.johnwayner.android.simplesoap;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.wsdl.MessageImpl;

public class SimpleSoap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			WSDLFactory wsdlFactory = WSDLFactory.newInstance();
		      WSDLReader  wsdlReader  = wsdlFactory.newWSDLReader();
		      WSDLWriter  wsdlWriter  = wsdlFactory.newWSDLWriter();
		      
		      if(args.length < 3) {
		    	  System.out.println("Usage: XXXX <wsdl> <base package name> <output dir>");
		    	  System.exit(1);
		      }
		      
		      String wsdlFileName = args[0];
		      String packageName = args[1];
		      String outputDirName = args[2];

		      Definition def = wsdlReader.readWSDL(null, wsdlFileName);		      
		      
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
		      
		      Map<QName, MessageImpl> messages = def.getMessages();
		      
		      //OMG this is some serious hacking.  The wsdl I care about consistently
		      //    uses the message name (minus SoapIn/SoapOut) as the input arg and
		      //    the name plus "Response" as the output argument.  So I'm cheating.
		      List<Message> messageList = new ArrayList<Message>();
		      for(QName name : messages.keySet()) {
		    	  if(name.getLocalPart().endsWith("SoapIn")) {
		    		  String baseName = name.getLocalPart().replace("SoapIn", "");
		    		  messageList.add(new Message(baseName, baseName, baseName + "Response"));
		    	  }
		      }
		      
		      Properties prop = new Properties();
		      prop.load(SimpleSoap.class.getResourceAsStream("/com/johnwayner/android/simplesoap/velocity.config"));
		      Velocity.init(prop);
		      
		      VelocityContext context = new VelocityContext();
		      Template complexTypeTemplate = Velocity.getTemplate("com/johnwayner/android/simplesoap/templates/ComplexType.vm");
		      File outputTypeDir = new File(outputDirName + "/type/");
		      outputTypeDir.mkdir();
		      for(ComplexType type : outputTypes) {
		    	  context.put("type", type);
		    	  context.put("package", packageName + ".type");
		    	  File output = new File(outputTypeDir, type.name + ".java");
		    	  System.out.println("output: " + output.getAbsolutePath());
		    	  FileWriter fw = new FileWriter(output);
		    	  complexTypeTemplate.merge(context, fw);
		    	  fw.close();
		      }
		      
		      Template messageRequestTemplate = Velocity.getTemplate("com/johnwayner/android/simplesoap/templates/MessageRequest.vm");
		      Template messageResponseTemplate = Velocity.getTemplate("com/johnwayner/android/simplesoap/templates/MessageResponse.vm");
		      outputTypeDir = new File(outputDirName + "/message/");
		      outputTypeDir.mkdir();
		      context = new VelocityContext();
		      for(Message message: messageList) {
		    	  context.put("message", message);
		    	  context.put("package", packageName + ".message");
		    	  context.put("typePackage", packageName + ".type");
		    	  File reqOutput = new File(outputTypeDir, message.name + "Envelope.java");
		    	  File resOutput = new File(outputTypeDir, message.outputType + "Envelope.java");
		    	  System.out.println("output: " + resOutput.getAbsolutePath());
		    	  System.out.println("output: " + reqOutput.getAbsolutePath());
		    	  FileWriter fw = new FileWriter(reqOutput);
		    	  messageRequestTemplate.merge(context, fw);
		    	  fw.close();
		    	  fw = new FileWriter(resOutput);
		    	  messageResponseTemplate.merge(context, fw);
		    	  fw.close();
		      }
		      
		      context = new VelocityContext();
		      Template serviceTemplate = Velocity.getTemplate("com/johnwayner/android/simplesoap/templates/Service.vm");
		      File systemDir = new File(outputDirName);
		      systemDir.mkdir();
		      Service service = (Service)def.getServices().values().iterator().next();
		      context.put("serviceName", service.getQName().getLocalPart());
		      context.put("messages", messageList);
		      context.put("package", packageName);
		      context.put("typePackage", packageName + ".type");
		      context.put("messagePackage", packageName + ".message");
		      context.put("endPoint", ((SOAPAddress)((Port)service.getPorts().values().iterator().next()).getExtensibilityElements().get(0)).getLocationURI());
		      File systemOutput = new File(systemDir, context.get("serviceName") + ".java");
	    	  System.out.println("output: " + systemOutput.getAbsolutePath());
	    	  FileWriter fw = new FileWriter(systemOutput);
	    	  serviceTemplate.merge(context, fw);
	    	  fw.close();
	    	  
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
