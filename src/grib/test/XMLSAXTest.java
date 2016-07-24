package grib.test;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 <student>
 <person age="25"><!--如果没有age属性，默认的为20-->
 <name>崔卫兵</name>
 <college>PC学院</college>
 <telephone>62354666</telephone>
 <notes>男,1982年生,硕士，现就读于北京邮电大学</notes>
 </person>
 <person>
 <name>cwb</name>
 <college leader="leader1">PC学院</college><!--如果没有leader属性，默认的为leader-->
 <telephone>62358888</telephone>
 <notes>男,1987年生,硕士，现就读于中国农业大学</notes>
 </person>
 <person age="45">
 <name>xxxxx</name>
 <college leader="学院领导">xxx学院</college>
 <telephone>66666666</telephone>
 <notes>注视中，注释中</notes>
 </person>
 </student>

 */
public class XMLSAXTest extends DefaultHandler {
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

	public void characters(char[] ch, int start, int length) throws SAXException {
		// 取出目前节点对应的值
		currentValue = new String(ch, start, length);
	}

	public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
		if (qName.equalsIgnoreCase("student")) {
			// currentElement= "";
		} else if (qName.equalsIgnoreCase("person")) {
			i++;
			// currentElement= "";
			String age = attr.getValue("age");
			if (age != null) {
				hashMap.put(qName + "-age" + i, age);
			} else {
				hashMap.put(qName + "-age" + i, "20");
			}
		} else if (qName.equalsIgnoreCase("college")) {
			currentElement = qName;
			String leader = attr.getValue("leader");
			if (leader != null) {
				hashMap.put(qName + "-leader" + i, leader);
			} else {
				hashMap.put(qName + "-leader" + i, "leader");
			}
		} else {
			currentElement = qName;
		}

	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("student")) {
			// hashMap.put(currentElement, currentValue);
		} else if (qName.equalsIgnoreCase("person")) {

		} else {
			currentElement += i;
			hashMap.put(currentElement, currentValue);
		}
	}

	public static void main(String[] args) {
		try {
			// 初始化与解析
			XMLSAXTest handler = new XMLSAXTest();
			SAXParserFactory saxparserfactory = SAXParserFactory.newInstance();
			SAXParser saxparser = saxparserfactory.newSAXParser();
			saxparser.parse(new File("studentInfo.xml"), handler);

			// 解析完后获取解析信息
			HashMap<?, ?> hashMap = handler.getHashMap();
			System.out.println("姓名\t年龄\t学院\t学院领导\t电话\t\t备注");
			for (int i = 0; i < hashMap.size(); i += 6) {
				int j = i / 6;
				System.out.print(hashMap.get("name" + j) + "\t");
				System.out.print(hashMap.get("person-age" + j) + "\t");
				System.out.print(hashMap.get("college" + j) + "\t");
				System.out.print(hashMap.get("college-leader" + j) + "\t");
				System.out.print(hashMap.get("telephone" + j) + "\t");
				System.out.println(hashMap.get("notes" + j) + "\t");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
