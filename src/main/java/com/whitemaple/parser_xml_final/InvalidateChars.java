package com.whitemaple.parser_xml_final;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: kakajn
 * Date: 2023/06/21 17:52
 * Package_name: com.whitemaple.parser_xml_final
 * Description:  Version: V1.0
 * Comment Before See:
 */


public enum InvalidateChars {

    //空格
    SPACE(32),
    //水平制表符
    HORIZONTAL_TAB(9),
    //处置制表符
    VERTICAL_TAB(11),
    //回车键
    CARRIAGE_RETURN(13),
    //换行符
    NEW_LINE(10);



    private final int ASCII_NUMBER;

    private InvalidateChars(int ascii_number) {
        this.ASCII_NUMBER = ascii_number;
    }

    public int getASCII_NUMBER() {
        return ASCII_NUMBER;
    }
}
