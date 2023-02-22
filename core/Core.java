package unescan.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jssc.SerialPortException;
import unescan.Unescan;

public final class Core {
	
	private final HashMap<String, String> modelMap;
	private final String[] ownerList;
	private final ScanHandler scanHandler;
	private final ArrayList<Device> scanList;
	private boolean modified = false;
	private int verboseLevel = 2;
	private String confDate = "";
	

	public Core() throws ParserConfigurationException, SAXException, IOException
	{
		modelMap = makeModelMap();
		ownerList = makeOwnersList();
		scanList = new ArrayList<Device>();
		scanHandler = new ScanHandler(this);
	}
	
	/**
	 * 
	 * @param newDev
	 * @return The device scanned or the device already scanned if it is.
	 * @throws IOException
	 */
	public final Device writeDevice(final Device newDev)
	{
		//Test if device already scanned
		for(Device scannedDev : scanList) {
			if(scannedDev.getSerial().equals(newDev.getSerial())) return scannedDev;
		}

		scanList.add(newDev);
		modified = true;
		return newDev;
	}
	
	/**
	 * 
	 * @param serial
	 * @return Device if found, null otherwise
	 */
	public final Device existsSerial(final String serial)
	{
		for(Device d : scanList)
		{
			if(d.getSerial().equals(serial)) return d;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param clientId
	 * @return Device if found, null otherwise
	 */
	public final Device existsClientId(final String clientId)
	{
		for(Device d : scanList)
		{
			if(d.getClientId().equals(clientId)) return d;
		}
		
		return null;
	}
	public final Device writeClientId(final String clientId, final Device device)
	{
		//Test if clientId already scanned before
		for(Device d : scanList)
		{
			
			if(d.getClientId().equals(clientId)){
				if(verboseLevel > 0) System.out.println(this.getClass().getName() + " -> Duplicate client id");
				return d;
			}
		}
		for(Device d : scanList)
		{
			if(d == device)
			{
				d.setClientId(clientId);
				return d;
			}
		}
		
		return null;
	}
	
	public final void removeDevice(Device dev)
	{
		for(Device d : scanList)
		{
			if(d.getSerial().equals(dev.getSerial()))
			{
				scanList.remove(d);
				return;
			}
		}
	}
	
	public final void save(String filepath) throws IOException
	{
		File save = new File(filepath);
		BufferedWriter writer = Files.newBufferedWriter(save.toPath());
		
		//Header line
		String line = String.format("location,model,serial,owner,idClient,comment%n");
		writer.write(line);
		
		for(Device dev : scanList)
		{
			line = String.format(
					dev.getLocation() + ","
					+ dev.getModel()+ ","
					+ dev.getSerial() + ","
					+ dev.getOwner() + "," 
					+ dev.getClientId() + "," 
					+ dev.getComment() + "%n");
			writer.write(line);
		}
		modified = false;
		writer.close();
	}
	
	public final ArrayList<Device> getScanList()
	{
		return scanList;
	}
	
	public final Node getXmlRootNode(String node) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
		DocumentBuilder builder = factory.newDocumentBuilder();
		File file = new File(Unescan.START_PATH + "\\" + Unescan.CONFIG_FILE);
		Document xml = builder.parse(file);
		Element root = (Element)xml.getDocumentElement();
		confDate = root.getAttribute("generated");
		NodeList childs = root.getChildNodes();

		for(int i = 0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			String name = child.getNodeName();
			if(name.equals(node)) return child;
		}
		return xml.getDocumentElement();
		
	}
	
	public final HashMap<String, String> makeModelMap() throws ParserConfigurationException, SAXException, IOException
	{
		Element rootNode = (Element)getXmlRootNode("models");
		NodeList children = rootNode.getChildNodes();
		int nbChildren = children.getLength();
		HashMap<String, String> map = new HashMap<String, String>();
		for(int i = 0; i < nbChildren; i++)
		{
			Node child = children.item(i);
			if(child instanceof Element)
			{
				Element elem = (Element)child;
				String key = elem.getAttribute("value");
				String value = elem.getAttribute("pserial");
				map.put(key, value);
			}
		}
		
		
		return map;
	}
	
	public final String[] getSortedModels(final HashMap<String, String> map)
	{
		//Collections.sort(map.keySet().to);
		List<String> list = map.keySet().stream().collect(Collectors.toList());
		Collections.sort(list, Collator.getInstance());
		
		return list.toArray(new String[list.size()]);
		
	}
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException
	{
		Core core = new Core();
		System.out.println(core.getXmlRootNode(null));
	}
	
	public final String[] makeOwnersList() throws ParserConfigurationException, SAXException, IOException
	{
		Element rootNode = (Element)getXmlRootNode("enterprises");
		NodeList children = rootNode.getChildNodes();
		int nbChildren = children.getLength();
		ArrayList<String> ownerList = new ArrayList<String>();
		for(int i = 0; i < nbChildren; i++)
		{
			Node child = children.item(i);
			if(child instanceof Element)
			{
				Element elem = (Element)child;
				String owner = elem.getAttribute("value");
				ownerList.add(owner);
			}
		}
		
		return ownerList.toArray(new String[ownerList.size()]);
	}
	
	/**
	 * 
	 * @param serial
	 * @return Model string if found, empty String otherwise
	 */
	public final String getModel(final String serial)
	{
		for(String model : modelMap.keySet())
		{
			if(serial.matches(modelMap.get(model))) return model;
		}
		
		return "Inconnu";
	}
	
	public final ScanHandler getScanHandler()
	{
		return scanHandler;
	}
	public final HashMap<String, String> getModelMap()
	{
		return modelMap;
	}
	public void dispose() throws IOException, SerialPortException
	{
		if(scanHandler != null) scanHandler.close();
	}
	
	public final boolean isModified()
	{
		return modified;
	}
	
	public final String[] getOwners()
	{
		return ownerList;
	}
	
	public final String getConfDate()
	{
		return confDate;
	}

}
