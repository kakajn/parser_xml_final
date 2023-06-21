package com.whitemaple.parser_xml_final;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Created with IntelliJ IDEA.
 * User: kakajn
 * Date: 2023/06/21 17:22
 * Package_name: com.whitemaple.parser_xml_final
 * Description:  Version: V1.0
 * Comment Before See:
 */
public class XMLParserImpl {

    //缓冲区的默认大小  为4MB
    //现在现做的简单一点, 把所有的数据都一次性读取进来
    public static final int DEFAULT_READ_BUFFER_SIZE = 4 * 1024 * 1024;

    //缓冲区的大小
    public int buffer_size;

    //读取指针的位置:
    public int read_pointer_pos;

    //读取的缓冲区
    public byte[] readBuffer;

    //构造函数
    public XMLParserImpl(String xmlFilePath) {
        this(xmlFilePath, DEFAULT_READ_BUFFER_SIZE);
        this.buffer_size = DEFAULT_READ_BUFFER_SIZE;
    }

    //先设置一个状态, 如果这个状态不为初始状态, 再进行调用parse方法的时候会报错
    private int parse_state = 0;

    //构造函数
    public XMLParserImpl(String xmlFilePath, int buffer_size) {
        //获取文件路径
        File file = new File(xmlFilePath);

        //创建输入流
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            //初始化readBuffer
            readBuffer = new byte[buffer_size];
            //把数据读取到缓冲区
            int read = fileInputStream.read(readBuffer);


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取下一个有效的字符, 不包括空格, table, 回车 等符号
     * 这个函数只读取内容, 并且改变 位置指针 的位置
     *
     * @return
     */
    public char getNextToken() {
        char current_read_byte = 0;

        //先读一次
        current_read_byte = (char)readBuffer[read_pointer_pos];

        //如果是无效的字符, 那么一直循环
        while (current_read_byte == InvalidateChars.SPACE.getASCII_NUMBER() ||
                current_read_byte == InvalidateChars.VERTICAL_TAB.getASCII_NUMBER() ||
                current_read_byte == InvalidateChars.HORIZONTAL_TAB.getASCII_NUMBER() ||
                current_read_byte == InvalidateChars.CARRIAGE_RETURN.getASCII_NUMBER() ||
                current_read_byte == InvalidateChars.NEW_LINE.getASCII_NUMBER()
        ) {
            read_pointer_pos++;
            current_read_byte = (char) readBuffer[read_pointer_pos];
        }
        return (char) readBuffer[read_pointer_pos];
    }
    //parse方法
    public XMLNode parse(){
        XMLNode rootNode = new XMLNode();
        XMLNode childrenNode = this.parse(rootNode);
        rootNode.addChildNode(childrenNode);
        return rootNode;
    }

    //解析的主要方法
    public XMLNode parse(XMLNode fatherXMLNode) {

        while (true) {
            char t = (char) getNextToken();

            //开始解析文档, 如果第一个解析的不是 < , 那么说明文档出现了语法错误
            if (t != '<') {
                throw new RuntimeException("invalidate format=====>"+locateExceptionDetail());
            }


            //解析版本号
            if (read_pointer_pos + 4 < buffer_size && "<?xml".equals(concatByteArrayToString(read_pointer_pos, read_pointer_pos + 5))) {
                //read_pointer_pos +5 跳过 <?xml
                read_pointer_pos += 5;

                char nextToken = (char) getNextToken();

                XMLNode xmlVersionNode = new XMLNode();

                while (isAlpha(getNextToken())) {
                    //解析<?xml> 属性名称
                    String s = parseVersionPropertiesName();

                    //更新nextToken的值
                    nextToken = getNextToken();

                    if (nextToken != '=' && nextToken != ':') {
                        throw new RuntimeException("parse version tag error occur =====>"+locateExceptionDetail());
                    }

                    //跳过=
                    read_pointer_pos++;

                    nextToken = getNextToken();

                    if (nextToken != '"') {
                        throw new RuntimeException("parse version tag error occur =====>"+locateExceptionDetail());
                    }

                    //跳过"
                    read_pointer_pos++;

                    //解析属性
                    String s1 = parseXMLTagPropertiesValue();

                    //解析完之后再跳过结尾的"
                    read_pointer_pos++;

                    //添加名字
                    xmlVersionNode.setTagText("xml");

                    //添加属性
                    xmlVersionNode.addTagProperties(s,s1);
                }
                //跳到下一个token
                nextToken = getNextToken();

                if ((char)readBuffer[read_pointer_pos] != '?' && (char)readBuffer[read_pointer_pos+1] !='>'){
                    throw new RuntimeException("parse version tag error occur =====>"+locateExceptionDetail());
                }
                read_pointer_pos += 2;
                //添加进父节点
                fatherXMLNode.addChildNode(xmlVersionNode);
            }

            //调用nextToken跳过空白字符
            getNextToken();
            //解析注释
            if (read_pointer_pos +3 < buffer_size && "<!--".equals(concatByteArrayToString(read_pointer_pos, read_pointer_pos+4))) {
                //跳过<!--
                read_pointer_pos += 4;

                String s = parseComment();
                //跳过两个--
                read_pointer_pos +=2;

                if (getNextToken() != '>'){
                    throw new RuntimeException("parse comment error occur =====>"+locateExceptionDetail());
                }
                //跳过 xml注释的 >
                read_pointer_pos ++;

                XMLNode commentXmlNode = new XMLNode();
                commentXmlNode.setTagName("commentNode");
                commentXmlNode.setTagText(s);

                //循环解析防止还有comment节点
                continue;
            }

            //解析element
            if (read_pointer_pos+1 < buffer_size && (isAlpha((char) readBuffer[read_pointer_pos+1]) || readBuffer[read_pointer_pos+1]=='_')){
                return parseElement(fatherXMLNode);
            }

            //出现未定义情况直接抛出异常
            throw new RuntimeException("error format =====>"+locateExceptionDetail());
        }
    }


    //解析版本号里面的属性名称
    public String parseXMLTagPropertiesValue() {
        StringBuilder stringBuilder = new StringBuilder();
        while (isAlpha((char)readBuffer[read_pointer_pos])) {
            stringBuilder.append((char)readBuffer[read_pointer_pos]);
            read_pointer_pos++;
        }
        return stringBuilder.toString();
    }

    //停止解析value
    public boolean stopParsePropertyValue(){
        if (!((char) readBuffer[read_pointer_pos] == '"')){
            return false;
        }
        return true;
    }

    //解析<?xml>里面的值
    public String parseVersionPropertiesName() {
        StringBuilder stringBuilder = new StringBuilder();
        while (isAlpha((char) readBuffer[read_pointer_pos])) {
            stringBuilder.append((char) readBuffer[read_pointer_pos]);
            read_pointer_pos++;
        }
        return stringBuilder.toString();
    }


    //解析注释 <!---->
    public String parseComment() {
        StringBuilder stringBuilder = new StringBuilder();
        while (notStopParseComment()) {
            stringBuilder.append((char) readBuffer[read_pointer_pos]);
            read_pointer_pos++;
        }
        return stringBuilder.toString();
    }

    //判断应不应该停止解析注释
    public boolean notStopParseComment() {
        if ('-' == readBuffer[read_pointer_pos] && '-' == readBuffer[read_pointer_pos + 1]) {
            return false;
        }
        return true;
    }

    //解析元素节点
    public XMLNode parseElement(XMLNode fatherNode) {
        //创建元素节点
        XMLNode xmlNode = new XMLNode();

        //过滤掉 <  使用getNextToken方法是达不到这种效果的
        read_pointer_pos++;

        //判断name首字符合法性
        if (!(read_pointer_pos < buffer_size
                && (isAlpha((char)readBuffer[read_pointer_pos]) || readBuffer[read_pointer_pos] == '_'))) {
            throw new RuntimeException("name first alpha is illegal =====>"+locateExceptionDetail());
        }

        //解析name属性
        String elementName = parseElementName();
        if (elementName != null && !"".equals(elementName)) {
            xmlNode.setTagName(elementName);
        }

        //正式解析内部
        while (read_pointer_pos < buffer_size) {
            char token = getNextToken();
            if (token == '/') { //1.单元素，直接解析后结束
                if (readBuffer[read_pointer_pos + 1] == '>') {
                    read_pointer_pos += 2;
                    return xmlNode;
                } else {
                    throw new RuntimeException("parse single_element failed =====>"+locateExceptionDetail());
                }
            }

            //2.对应三种情况：结束符、注释、下个子节点
            if (token == '<') {
                if ((char) readBuffer[read_pointer_pos+1] == '/') {
                    //过滤掉 </
                    read_pointer_pos += 2;
                    //如果开始标签的名字和结束标签的名字不相等, 那么抛出异常
                    if (!parseElementName().equals(xmlNode.getTagName())) {
                        throw new RuntimeException("parse end tag error =====>"+locateExceptionDetail());
                    }
                    char x = getNextToken();
                    if (x != '>') {
                        throw new RuntimeException("parse end tag error =====>"+locateExceptionDetail());
                    }
                    //千万注意把 '>' 过掉，防止下次解析被识别为初始的tag结束，实际上这个element已经解析完成
                    read_pointer_pos++;
                    return xmlNode;
                }
                //是注释的情况
                if (read_pointer_pos + 3 < buffer_size && "<!--".equals(concatByteArrayToString(read_pointer_pos, read_pointer_pos+4))) {
                    String s = parseComment();
                    //调用一下getNextToken 让read_pointer_pos位置增加
                    getNextToken();
                    if (readBuffer[read_pointer_pos] != '-' && readBuffer[read_pointer_pos + 1] != '-') {
                        throw new RuntimeException("comment parse error occur =====>"+locateExceptionDetail());
                    }
                    XMLNode commentXmlNode = new XMLNode();
                    commentXmlNode.setTagText(s);
                    fatherNode.addChildNode(commentXmlNode);
                    continue;
                }
                //其余情况可能是注释或子元素, 直接调用parse进行解析得到即可
                xmlNode.addChildNode(parse(xmlNode));
                continue;
            }

            //3.对应两种情况：该标签的text内容，下个标签的开始或者注释（直接continue跳到到下次循环即可
            if (token == '>') {
                read_pointer_pos++;                 //跳过>
                //判断下一个token是否为text, 如果不是则cotinue
                char nextToken = getNextToken();
                if (nextToken == '<') { //说明是子节点
                    continue;
                }
                //解析text 再解析child
                String s = concatByteArrayToString(read_pointer_pos, findNextCharPosition((char) '<'));
                xmlNode.setTagText(s);
                //解析完成之后read_pointer_pos 应该加上 s.length
                read_pointer_pos += s.length();
                //注意：有可能直接碰上结束符，所以需要continue，让element里的逻辑来进行判断
                continue;
            }

            //4.其余情况都为属性的解析
            String propertyName = parsePropertiesName();
            char nextToken1 = getNextToken();
            if (nextToken1 != '=') {
                throw new RuntimeException("parse attrs error =====>"+locateExceptionDetail());
            }
            //跳过等于 =
            read_pointer_pos++;
            //跳过 = 后面的空白字符
            nextToken1 = getNextToken();
            if (nextToken1 != '"') {
                throw new RuntimeException("parse attrs error =====>"+locateExceptionDetail());
            }
            //跳过开始"
            read_pointer_pos++;
            String propertyValue = parsePropertiesValue();
            //跳过结束"
            read_pointer_pos++;
            xmlNode.addTagProperties(propertyName, propertyValue);

        }

        throw new RuntimeException("parse element error =====>"+locateExceptionDetail());
    }

    //找出下一个符号在readBuffer中的位置
    public int findNextCharPosition(char findByte) {
        int temp_pos = read_pointer_pos;
        while (temp_pos <= buffer_size && readBuffer[temp_pos] != findByte) {
            temp_pos++;
        }
        return temp_pos;
    }

    //解析标签的名字
    public String parseElementName() {
        StringBuilder stringBuilder = new StringBuilder();
        while (isAlpha((char)readBuffer[read_pointer_pos])) {
            stringBuilder.append((char)readBuffer[read_pointer_pos]);
            read_pointer_pos++;
        }
        return stringBuilder.toString();
    }

    //解析属性名字
    public String parsePropertiesName() {
        StringBuilder stringBuilder = new StringBuilder();
        while (isAlpha((char)readBuffer[read_pointer_pos])) {
            stringBuilder.append((char)readBuffer[read_pointer_pos]);
            read_pointer_pos++;
        }
        return stringBuilder.toString();
    }

    //解析属性值
    public String parsePropertiesValue() {
        StringBuilder stringBuilder = new StringBuilder();
        while (!stopParsePropertyValue()) {
            stringBuilder.append((char)readBuffer[read_pointer_pos]);
            read_pointer_pos++;
        }
        return stringBuilder.toString();
    }


    //数组截取字符串
    public String concatByteArrayToString(int startArrayIndex, int endArrayIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int concatBeginIndexInArray = startArrayIndex; concatBeginIndexInArray < endArrayIndex; concatBeginIndexInArray++) {
            stringBuilder.append((char) readBuffer[concatBeginIndexInArray]);
        }
        return stringBuilder.toString();
    }

    //判断当前的token是不是字母
    public boolean isAlpha(char token) {
        if (token > 127) {
            return true;
        } else {
            if ( (token >= 65 && token <= 90) || (token >= 97 && token <= 122)){
                return true;
            }else if (token >= 48 && token <= 57){
                return true;
            }else if (token == ':' || token == '-' || token == '_' || token == '|' || token=='.') {
                return true;
            }
        }
        return false;
    }


    //locateExceptionDetail
    public String locateExceptionDetail(){
        return concatByteArrayToString(read_pointer_pos <=30 ? 0 : read_pointer_pos-30,read_pointer_pos);
    }
}
