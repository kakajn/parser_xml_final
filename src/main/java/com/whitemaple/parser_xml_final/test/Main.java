package com.whitemaple.parser_xml_final.test;

import com.whitemaple.parser_xml_final.XMLNode;
import com.whitemaple.parser_xml_final.XMLParserImpl;

/**
 * Created with IntelliJ IDEA.
 * User: kakajn
 * Date: 2023/06/21 23:34
 * Package_name: com.whitemaple.parser_xml_final.test
 * Description:  Version: V1.0
 * Comment Before See:
 */
public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        XMLParserImpl xmlParser = new XMLParserImpl("G:\\自学内容\\java解析XML\\parser_xml_final\\parser_xml_final\\src\\main\\resources\\maven.xml");
        XMLNode treeDom = xmlParser.parse();
        long end = System.currentTimeMillis();
        System.out.println("解析时间为:"+(end-start));
        System.out.println(treeDom);
    }
}
