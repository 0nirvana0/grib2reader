package grib.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XMLdom4jTest2 {
	public static void main(String[] args) {
		try {
			// 创建saxReader对象
			SAXReader reader = new SAXReader();
			// 通过read方法读取一个文件 转换成Document对象
			Document document = reader.read(new File("src/dom4j/sida.xml"));

			// 获取根节点元素对象
			Element node = document.getRootElement();
			// 遍历所有的元素节点
			listNodes(node);
			// 获取四大名著元素节点中，子节点名称为红楼梦元素节点。
			Element element = node.element("红楼梦");
			
			// 获取element的id属性节点对象
			Attribute attr = element.attribute("id");
			// 删除属性
			element.remove(attr);
			// 添加新的属性
			element.addAttribute("name", "作者");
			// 在红楼梦元素节点中添加朝代元素的节点
			Element newElement = element.addElement("朝代");
			newElement.setText("清朝");
			// 获取element中的作者元素节点对象
			Element author = element.element("作者");
	
			// 删除元素节点
			boolean flag = element.remove(author);
			// 返回true代码删除成功，否则失败
			System.out.println(flag);
			// 添加CDATA区域
			element.addCDATA("红楼梦，是一部爱情小说.");
			// 写入到一个新的文件中
			writer(document);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** 
	 * 把document对象写入新的文件 
	 *  
	 * @param document 
	 * @throws Exception 
	 */
	public static void writer(Document document) throws Exception {
		// 紧凑的格式
		// OutputFormat format = OutputFormat.createCompactFormat();
		// 排版缩进的格式
		OutputFormat format = OutputFormat.createPrettyPrint();
		// 设置编码
		format.setEncoding("UTF-8");
		// 创建XMLWriter对象,指定了写出文件及编码格式
		// XMLWriter writer = new XMLWriter(new FileWriter(new
		// File("src//a.xml")),format);
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(new File("src//a.xml")), "UTF-8"), format);
		// 写入
		writer.write(document);
		// 立即写入
		writer.flush();
		// 关闭操作
		writer.close();
	}

	/** 
	 * 遍历当前节点元素下面的所有(元素的)子节点 
	 *  
	 * @param node 
	 */
	public static void listNodes(Element node) {
		System.out.println("当前节点的名称：：" + node.getName());
		// 获取当前节点的所有属性节点
		List<Attribute> list = node.attributes();
		// 遍历属性节点
		for (Attribute attr : list) {
			System.out.println(attr.getText() + "-----" + attr.getName() + "---" + attr.getValue());
		}

		if (!(node.getTextTrim().equals(""))) {
			System.out.println("文本内容：：：：" + node.getText());
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			listNodes(e);
		}
	}

	/** 
	 * 介绍Element中的element方法和elements方法的使用 
	 *  
	 * @param node 
	 */
	public void elementMethod(Element node) {
		// 获取node节点中，子节点的元素名称为西游记的元素节点。
		Element e = node.element("西游记");
		// 获取西游记元素节点中，子节点为作者的元素节点(可以看到只能获取第一个作者元素节点)
		Element author = e.element("作者");

		System.out.println(e.getName() + "----" + author.getText());

		// 获取西游记这个元素节点 中，所有子节点名称为作者元素的节点 。

		List<Element> authors = e.elements("作者");
		for (Element aut : authors) {
			System.out.println(aut.getText());
		}

		// 获取西游记这个元素节点 所有元素的子节点。
		List<Element> elements = e.elements();

		for (Element el : elements) {
			System.out.println(el.getText());
		}

	}
}
