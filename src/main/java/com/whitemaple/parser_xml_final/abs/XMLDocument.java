package com.whitemaple.parser_xml_final.abs;

import com.whitemaple.parser_xml_final.XMLNode;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kakajn
 * Date: 2023/06/21 17:17
 * Package_name: com.whitemaple.parser_xml_final.impl
 * Description:  Version: V1.0
 * Comment Before See:
 */
public interface XMLDocument {

    //打印所有的节点
    public void printDocument();

    //获取属性为xxx的所有节点
    public List<XMLNode> getNodeByProperties(String propertiesValue);
}
