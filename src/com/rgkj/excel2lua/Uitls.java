package com.rgkj.excel2lua;

public class Uitls {
	public static boolean isNumeric(String str){
		if ((str != null) && (!"".equals(str.trim()))) {
			return str.matches("\\d+(\\.\\d+)?");
	    }
	    return false;
	}
}
