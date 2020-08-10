package org.activiti.designer.util;

import java.io.InputStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.core.resources.IFile;

public class XmlUtil {    
   
    public static String updateProceessAttributes(IFile bpmnFile, String processName, String processId) throws Exception{
    	XMLInputFactory xif = XMLInputFactory.newInstance();
    	InputStreamReader in = new InputStreamReader(bpmnFile.getContents(), "UTF-8");
    	XMLStreamReader xtr = xif.createXMLStreamReader(in);

    	while (xtr.hasNext()) {
    		xtr.next();

    		if (isProcessStartElement(xtr)) {
    			if (xtr.isCharacters()) {
    				String data = new String(xtr.getTextCharacters());
    				String txt = xtr.getText();
    			}    			
    			String prName = xtr.getAttributeValue("process", "name");
    			String prId = xtr.getAttributeValue("process", "id");
    			//currentProcess = new Process();
    	          //currentProcess.setId(xtr.getAttributeValue(null, "id"));
    		} else if (isProcessEndElement(xtr)) {
    			//result.add(currentProcess);
    			//currentProcess = null;
    		} else {
    			continue;
    		}
    	}

    	return "";    	  
    }    
    
	private static final boolean isProcessStartElement(final XMLStreamReader xtr) {
		return xtr.isStartElement() && "process".equalsIgnoreCase(xtr.getLocalName());
	}

	private static final boolean isProcessEndElement(final XMLStreamReader xtr) {
		return xtr.isEndElement() && "process".equalsIgnoreCase(xtr.getLocalName());
	}
}
 