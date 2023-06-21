package com.whitemaple.parser_xml_final;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kakajn
 * Date: 2023/06/21 17:08
 * Package_name: com.whitemaple.parser_xml_final
 * Description:  Version: V1.0
 * Comment Before See:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XMLNode {

    //xml标签的名字
    private String tagName;

    //xml标签的内容
    private String tagText;

    //xml标签的子节点
    private List<XMLNode> childNodes;

    //xml标签的属性
    private Map<String ,String > tagProperties;

    public boolean addChildNode(XMLNode childXmlNode){
        if (childNodes == null){
            childNodes = new ArrayList<>();
        }
        return this.childNodes.add(childXmlNode);
    }

    public String addTagProperties(String tagPropertiesName, String tagPropertiesValue){
        if (tagProperties == null){
            tagProperties = new HashMap<>();
        }
        String put = this.tagProperties.put(tagPropertiesName, tagPropertiesValue);
        return put;
    }
}
