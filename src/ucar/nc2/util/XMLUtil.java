package ucar.nc2.util;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLUtil extends DefaultHandler {
	// 存放所有的节点（这里的节点等于原来的节点+编号）以及它所对应的值
	private HashMap<String, String> hashMap = new HashMap<String, String>();
	// 目前的节点
	private String currentElement = null;
	// 目前节点所对应的值
	private String currentValue = null;
	// 用于节点编号（具体到person）
	private static int i = -1;

	public HashMap<String, String> getHashMap() {
		return hashMap;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// 取出目前节点对应的值
		currentValue = new String(ch, start, length);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("student")) {
			// currentElement= "";
		} else if (qName.equalsIgnoreCase("person")) {
			i++;
			// currentElement= "";
			String age = attributes.getValue("age");
			if (age != null) {
				hashMap.put(qName + "-age" + i, age);
			} else {
				hashMap.put(qName + "-age" + i, "20");
			}
		} else if (qName.equalsIgnoreCase("college")) {
			currentElement = qName;
			String leader = attributes.getValue("leader");
			if (leader != null) {
				hashMap.put(qName + "-leader" + i, leader);
			} else {
				hashMap.put(qName + "-leader" + i, "leader");
			}
		} else {
			currentElement = qName;
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("student")) {
			// hashMap.put(currentElement, currentValue);
		} else if (qName.equalsIgnoreCase("person")) {

		} else {
			currentElement += i;
			hashMap.put(currentElement, currentValue);
		}
	}

}
