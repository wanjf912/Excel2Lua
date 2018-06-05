package com.rgkj.excel2lua;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

public class Excel2Lua {

	// lua�ļ�����·��
	private static String exportPath;
	// excel�ļ�·��
	private static String importPath;
	// ��ǰ������
	private static int indentCount = 0;

	private static boolean hasException = false;
	
	private static StringBuffer allOutStringBuffer = new StringBuffer();
	private static String curXlsFileName;
	
	public static void main(String[] args) {      
		// ��ʼ������
		try {
			initConfig();
			listExcel2Lua();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//addOutString(e.toString());
			//showOutputDialog();
			e.printStackTrace();
		}
	}

	private static void indent(StringBuffer str) {
		for (int i = 0; i < indentCount; i++) {
			str.append("\t");
		}
		return;
	}
	
	// �Ƿ���excel�ļ�
	public static boolean isExcelFile(String name){
		if (null != name) {
			String fileType = name
					.substring(name.lastIndexOf("."), name.length())
					.trim().toLowerCase();
			return ".xls".equals(fileType) || ".xlsx".equals(fileType); 
		}
		return false;
	}
	

	/**
	 * �����ļ�����ȡexcel����
	 */
	public static Workbook getWorkbook(String filename){
		Workbook workbook = null;
		if (null != filename) {
			String fileType = filename.substring(filename.lastIndexOf("."), filename.length()).trim().toLowerCase();
			try {
				FileInputStream fileStream = new FileInputStream(new File(filename));
				if (".xls".equals(fileType)) {
					workbook = new HSSFWorkbook(fileStream);
				} else if (".xlsx".equals(fileType)) {
					workbook = new XSSFWorkbook(fileStream);
				}
			} catch (FileNotFoundException e) {
				interruptWithError(filename + "���ļ��Ҳ�����");
			} catch (IOException e) {
				interruptWithError(filename + "���ļ���ȡʧ�ܡ�");
			}
		}
		return workbook;
	}
	
	// ��ȡ��Ԫ���ֵ
	public static String getCellValue(Cell cell, Cell typeCell, Cell descCell){
		String typeString = typeCell.toString();
	    int cellType = cell.getCellType();
	    int rowIndex = cell.getRowIndex() + 1;
	    int colIndex = cell.getColumnIndex() + 1;
	    String rowColFmt = cell.getSheet().getSheetName() + "���ڵĵ�" + rowIndex + "��," + "��" + colIndex + "�е�";
	    if (typeString.equals("string"))
	    {
	      if (cellType == 0) {
	        return "\"" + (int)cell.getNumericCellValue() + "\"";
	      }
	      if (cellType == 4)
	      {
	        boolean b = cell.getBooleanCellValue();
	        if (b) {
	          return "true";
	        }
	        return "false";
	      }
	      if (cellType == 2) {
	        return "\"" + cell.getCellFormula() + "\"";
	      }
	      return "\"" + cell.getStringCellValue() + "\"";
	    }
	    if (typeString.equals("table")) {
	      return "{" + cell.getStringCellValue() + "}";
	    }
	    if (typeString.equals("double"))
	    {
	      if ((cellType == 1) || (cellType == 2)) {
	        interruptWithError(rowColFmt + "[" + cell + "]�������Ͳ���ȷ��Ҫ��Ϊdouble���ͣ�");
	      }
	      double f = cell.getNumericCellValue();
	      return f + "";
	    }
	    if (typeString.equals("int"))
	    {
	      if ((cellType == 1) || 
	        (cellType == 4)) {
	        interruptWithError(rowColFmt + "[" + cell + "]�������Ͳ���ȷ��Ҫ��Ϊint���ͣ�");
	      }
	      int f = (int)cell.getNumericCellValue();
	      return f + "";
	    }
	    if (typeString.equals("refer.sheet"))
	    {
	      if ((cellType == 4) || 
	        (cellType == 0) || 
	        (cellType == 2)) {
	        interruptWithError(rowColFmt + "[" + cell + "]�������Ͳ���ȷ��Ҫ��Ϊrefer.sheet���ͣ�");
	      }
	      Workbook workbook = cell.getSheet().getWorkbook();
	      String referString = cell.getStringCellValue();
	      
	      FormatsParser referParser = new FormatsParser();
	      referParser.referParse(referString);
	      if (referParser.isSuccess)
	      {
	        if (referParser.referRowCount > 0)
	        {
	          Sheet sheet = workbook.getSheet(referParser.referSheetName);
	          if (sheet == null) {
	            interruptWithError(referParser.referSheetName + "ҳ�����ڣ�");
	          }
	          return sheetRows2Lua(sheet, referParser.referRows).toString();
	        }
	        Sheet sheet = workbook.getSheet(cell.getStringCellValue());
	        if (sheet == null) {
	          interruptWithError(cell.getStringCellValue() + "ҳ�����ڣ�");
	        }
	        return sheet2Lua(sheet).toString();
	      }
	      interruptWithError(cell.getSheet().getSheetName() + "ҳ��[" + cell.getStringCellValue() + "]���ý���ʧ�ܣ���");
	    }
	    else
	    {
	      if (typeString.equals("bool"))
	      {
	        if ((cellType == 1) || 
	          (cellType == 5) || 
	          (cellType == 2))
	        {
	          interruptWithError(rowColFmt + "[" + cell + "]�������Ͳ���ȷ��Ҫ��Ϊbool���ͣ�");
	        }
	        else if (cellType == 4)
	        {
	          boolean b = cell.getBooleanCellValue();
	          return b ? "true" : "false";
	        }
	        int f = (int)cell.getNumericCellValue();
	        if (f != 0) {
	          return "true";
	        }
	        return "false";
	      }
	      if ((typeString.equals("table.string")) || (typeString.equals("table.string.col"))) {
	        return tableString2Lua(cell, true);
	      }
	      if (typeString.equals("table.string.row")) {
	        return tableString2Lua(cell, false);
	      }
	      if (typeString.equals("table.string.layout")) {
	        return "{}";
	      }
	      if ((typeString.equals("table.number")) || (typeString.equals("table.number.col"))) {
	        return tableNumber2Lua(cell, true);
	      }
	      if (typeString.equals("table.number.row")) {
	        return tableNumber2Lua(cell, false);
	      }
	      if (typeString.equals("table.number.layout")) {
	        return tableNumberLayout2Lua(cell);
	      }
	      if (typeString.equals("table.number.string")) {
	        return "{}";
	      }
	      if ((typeString.equals("table.map")) || (typeString.equals("table.map<string,number>"))) {
	        return tableMap2Lua(cell);
	      }
	      if (typeString.equals("table.map<string,string>")) {
	        return "{}";
	      }
	      if (typeString.equals("color3B")) {
	        return color3B2Lua(cell);
	      }
	      if (typeString.equals("color4B")) {
	        return color4B2Lua(cell);
	      }
	    }
	    return cell.toString();
	}
	
	public static String color4B2Lua(Cell cell)
	{
	    String string = cell.getStringCellValue();
	    string = string.replace(" ", "");
	    string = string.replace("\n", "");
	    String[] items = string.split(",");
	    if (items.length < 4) {
	      interruptWithError("[" + string + "]�������Ͳ���ȷ��Ҫ��Ϊtable.color4B���ͣ�");
	    }
	    for (int i = 0; i < items.length; i++) {
	      if (!Uitls.isNumeric(items[i])) {
	        interruptWithError("[" + string + "]�������Ͳ���ȷ��Ҫ��Ϊtable.color4B���ͣ�");
	      }
	    }
	    StringBuffer stringBuffer = new StringBuffer();
	    stringBuffer.append("cc.c4b(" + items[0] + "," + items[1] + "," + items[2] + "," + items[3] + ")");
	    
	    return stringBuffer.toString();
	}
	  
	public static String color3B2Lua(Cell cell)
	{
	    String string = cell.getStringCellValue();
	    string = string.replace(" ", "");
	    string = string.replace("\n", "");
	    String[] items = string.split(",");
	    if (items.length < 3) {
	    	interruptWithError("[" + string + "]�������Ͳ���ȷ��Ҫ��Ϊtable.color3B���ͣ�");
	    }
	    for (int i = 0; i < items.length; i++) {
		      if (!Uitls.isNumeric(items[i])) {
		        interruptWithError("[" + string + "]�������Ͳ���ȷ��Ҫ��Ϊtable.color3B���ͣ�");
		      }
	    }
	    StringBuffer stringBuffer = new StringBuffer();
	    stringBuffer.append("cc.c3b(" + items[0] + "," + items[1] + "," + items[2] + ")");
	    
	    return stringBuffer.toString();
	}

	public static String tableNumberLayout2Lua(Cell cell)
	{
	    String string = cell.getStringCellValue();
	    

	    String[] items = string.split("\n");
	    
	    StringBuffer stringBuffer = new StringBuffer();
	    stringBuffer.append("{");
	    stringBuffer.append("\n");
	    indentCount += 1;
	    indent(stringBuffer);
	    
	    String indentsString = "";
	    for (int i = 0; i < indentCount; i++) {
	    	indentsString = indentsString + "\t";
	    }
	    
	    for (int i = 0; i < items.length; i++)
	    {
	    	if (i + 1 < items.length)
	    	{
	    		items[i] = items[i] + "\n"; 
	    		items[i] = items[i] + indentsString;
	    	}
	    	stringBuffer.append(items[i]);
	    }
	    
	    stringBuffer.append("\n");
	    indentCount -= 1;
	    indent(stringBuffer);
	    stringBuffer.append("}");
	    
	    return stringBuffer.toString();
	}
	
	public static String tableMap2Lua(Cell cell)
	{
	    String string = cell.getStringCellValue();
	    FormatsParser parser = new FormatsParser();
	    parser.mapParse(string);
	    if (parser.isSuccess)
	    {
	    	StringBuffer stringBuffer = new StringBuffer();
	    	stringBuffer.append(" {");
	    	stringBuffer.append("\n");
	    	indentCount += 1;
	    	indent(stringBuffer);
	      
	    	HashMap<String, String> map = parser.map;
	    	Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
	    	while (entries.hasNext())
	    	{
		        Map.Entry<String, String> entry = (Map.Entry)entries.next();
		        stringBuffer.append((String)entry.getKey() + " = " + (String)entry.getValue());
		        stringBuffer.append(",");
		        if (entries.hasNext())
		        {
		        	stringBuffer.append("\n");
		        	indent(stringBuffer);
		        }
	    	}
	    	stringBuffer.append("\n");
	    	indentCount -= 1;
	    	indent(stringBuffer);
	    	stringBuffer.append("}");
  
	    	return stringBuffer.toString();
		}
		    
	    interruptWithError("[" + string + "]�������Ͳ���ȷ��Ҫ��Ϊtable.map���ͣ�");
	    return "";
	}
	
	public static String tableString2Lua(Cell cell, boolean bAsColumn)
	{
	    String string = cell.getStringCellValue();
	    String[] itmes = string.split(",");
	    ArrayList<String> arrayList = new ArrayList();

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{");
		if (bAsColumn)
		{
			stringBuffer.append("\n");
			indentCount += 1;
			indent(stringBuffer);
		}
		for (int i = 0; i < itmes.length; i++) {
			if (!itmes[i].trim().equals(""))
			{
				stringBuffer.append("\"" + itmes[i].trim() + "\", ");
				if ((bAsColumn) && (i + 1 < itmes.length))
				{
					stringBuffer.append("\n");
					indent(stringBuffer);
				}
			}
		}
		if (bAsColumn)
		{
			stringBuffer.append("\n");
			indentCount -= 1;
			indent(stringBuffer);
		}
		stringBuffer.append("}");
    
    	return stringBuffer.toString();
	}
	
	public static String tableNumber2Lua(Cell cell, boolean bAsColumn)
	{
	    String string = cell.getStringCellValue();
	    String[] items = string.split(",");
	 
	    StringBuffer stringBuffer = new StringBuffer();
	    stringBuffer.append(" {");
	    if (bAsColumn)
	    {
	    	stringBuffer.append("\n");
	    	indentCount += 1;
	    	indent(stringBuffer);
	    }
	    for (int i = 0; i < items.length; i++) {
			if (Uitls.isNumeric(items[i].trim()))
			{
				stringBuffer.append(items[i].trim() + ", ");
				if ((bAsColumn) && (i + 1 < items.length))
				{
					stringBuffer.append("\n");
				   indent(stringBuffer);
				}
			}
			else
			{
				interruptWithError("[" + string + "]�������Ͳ���ȷ��Ҫ��Ϊtable.number���ͣ�");
			}
	    }
	    if (bAsColumn)
	    {
	    	stringBuffer.append("\n");
	    	indentCount -= 1;
	    	indent(stringBuffer);
	    }
	    stringBuffer.append("}");
	    
	    return stringBuffer.toString();
	}
	/**
	 * ����sheetҳ��ָ����ֹ���ڵ������ݣ�����ֹ���е�����,��[from,to].
	 * @param sheet
	 * @param fromRow
	 * @param toRow
	 * @return
	 */
	private static StringBuffer sheetMultiRows2Lua(Sheet sheet, int fromRow ,int toRow){
		if (toRow < fromRow) {
			interruptWithError(sheet.getSheetName() + "ҳ��������ֹ�������ô���toRow ������ڵ���fromRow");
		}
		int key = 1;
		StringBuffer str = new StringBuffer();
		for (int n = fromRow; n <= toRow; n++ ) {
			str.append(sheetIndexRow2Lua(sheet, n, key++));
			str.append("\n");		
			if ( n + 1 <= toRow) {
				indent(str);
			}
		}
		return str;	
	}
	
	private static StringBuffer sheetIndexRow2Lua(Sheet sheet, int index, int key){
		if (index - 1 > sheet.getLastRowNum()) {
			interruptWithError(sheet.getSheetName() + "ҳ�ڲ����ڵ�" + index + "�У���");
		}
		// ��һ�У��ֶ�����
		Row typeRow = sheet.getRow(0);
		// �ڶ��У��ֶ�����
		Row keyRow = sheet.getRow(1);
		// �������У�����
		Row descRow = sheet.getRow(2);

		StringBuffer str = new StringBuffer();
	
		Row row = sheet.getRow(index - 1);
		
		Cell keyCell  = row.getCell(0);
		Cell typeCell = typeRow.getCell(0);
		Cell desCell  = descRow.getCell(0);
				
		str.append("[" + key + "] = {");
		str.append("\n");
		indentCount ++;
		indent(str);
		
		// ����ÿһ��
		for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
			keyCell  = row.getCell(j);
			typeCell = typeRow.getCell(j);
			desCell  = descRow.getCell(j);
			// ���Կյ�Ԫ��
			if ("null".equals(keyCell + "")) {
				println("Warning:" + "[" + keyRow.getCell(j) + "]"+ "�д��ڿյ�Ԫ��!!\t\t");
			} else {
				//System.out.print(keyCell + "\t\t");
				str.append(keyRow.getCell(j).toString());
				str.append(" = ");
				String ss = getCellValue(keyCell, typeCell, desCell);
				str.append(ss);
				str.append(",");
				str.append("\n");
				if (j + 1 < row.getLastCellNum()) {
					indent(str);
				}
			}
		}
		
		indentCount --;
		indent(str);
		
		str.append("},");
		str.append("\n");
		
		return str;
	}
	/**
	 * ����sheetҳ��ָ��������
	 * @param sheet
	 * @param rows
	 * @return
	 */
	private static StringBuffer sheetRows2Lua(Sheet sheet, Set<Integer> rows) {
			// ��ȡ������
			int totalRow = rows.size();

			StringBuffer str = new StringBuffer();
			str.append("{");
			str.append("\n");
			
			indentCount ++;
			indent(str);
			
			int n = 0;
			for (Integer rowIndex: rows) {
				str.append(sheetIndexRow2Lua(sheet, rowIndex, ++n));
				str.append("\n");
				if ( n + 1 <= totalRow) {
					indent(str);
				}
			}
			
			indentCount--;
			indent(str);
			str.append("}");
			
			return str;
	}
	
	/**
	 * ��������sheetҳȫ��
	 * @param sheet
	 * @return
	 */
	private static StringBuffer sheet2Lua(Sheet sheet){
		
		// ��ȡ������(��0��ʼ�����)
		int totalRow = sheet.getLastRowNum();
		if (totalRow + 1 < 3) {
			interruptWithError(sheet.getSheetName() + "ҳ�ڱ��ȱ�ٱ�ͷ����һ��Ϊ�����У��ڶ���Ϊ�����У�������Ϊ�����У�");
		}else if(totalRow + 1 == 3){
			interruptWithError(sheet.getSheetName() + "ҳ�ڱ��û�����ݣ��޷�������");
		}
		// ��һ�У��ֶ�����
		Row typeRow = sheet.getRow(0);
		// �ڶ��У��ֶ�����
		Row keyRow = sheet.getRow(1);
		// �������У�����
		Row descRow = sheet.getRow(2);

		StringBuffer str = new StringBuffer();
		str.append(curXlsFileName + "." + sheet.getSheetName() + " = ");
		str.append("{");
		str.append("\n");
		
		indentCount ++;
		indent(str);
		str.append(sheetMultiRows2Lua(sheet, 4, totalRow + 1));
		indentCount--;
		indent(str);
		str.append("}");
		str.append("\n\n");
		
		return str;
	}
	
	private static void excel2Lua(String excelPath) {
		
		// ����ļ��Ƿ����
		File excelFile = new File(excelPath);
		if (!excelFile.exists()) {
			throw new RuntimeException(excelPath + " ���ļ������ڡ�");
		}

		// ��ʼ������Ŀ¼
		File exportDir = new File(exportPath);
		
		// ����Ŀ¼
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}

		// excel�����
		Workbook workbook = getWorkbook(excelPath);

		// ��ȡ��1ҳ�ı��������0��ʼ
		Sheet sheet = workbook.getSheetAt(0);

		int totalRow = sheet.getLastRowNum();
		// ��һ�У��ֶ�����
		Row typeRow = sheet.getRow(0);
		// �ڶ��У��ֶ�����
		Row keyRow = sheet.getRow(1);
		// �������У�����
		Row descRow = sheet.getRow(2);

		String excelFileName = excelFile.getName().substring(0, excelFile.getName().lastIndexOf('.'));
		// lua�ļ�
		File luaFile = new File(new File(exportPath), excelFileName + ".lua");
		if (luaFile.exists()) {
			luaFile.delete();
		}

		curXlsFileName = excelFileName;
		
		try {
			//��ȡ������Ҫ�������ı�����
			int sheetCnt = workbook.getNumberOfSheets();
		    ArrayList<String> nameStrings = new ArrayList();
		    String flagString = ".unexport";
		    for (int i = 0; i < sheetCnt; i++)
		    {
		        String name = workbook.getSheetName(i);
		        if (name.lastIndexOf(flagString) <= 0) {
		          nameStrings.add(workbook.getSheetName(i));
		        }
		    }
		    println("����" + excelFile.getName() + ",��Ҫ����ҳ����" + nameStrings.size());
	
			println("����" + excelFile.getName() + "����Ҫ����ҳ����" + nameStrings.size());
			
			StringBuffer stringBuffer = new StringBuffer();
		    stringBuffer.append("local " + curXlsFileName + " = {}\n\n");
		      
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(luaFile), "UTF-8"));
			for (int i = 0; i < nameStrings.size(); i++)
			{
		        Sheet _sheet = workbook.getSheet((String)nameStrings.get(i));
		        println("��ʼ������" + _sheet.getSheetName() + ",������" + _sheet.getLastRowNum());
		        stringBuffer.append(sheet2Lua(_sheet));
		        String sheetNameString = _sheet.getSheetName();
		        if (sheetNameString.lastIndexOf(".enum") > 0) {
		          sheetNameString = sheetNameString.substring(0, sheetNameString.lastIndexOf(".enum"));
		        }
		        println("������ɣ�" + _sheet.getSheetName() + ",��" + excelFileName + "." + sheetNameString);
			}	
			if ((stringBuffer != null) || (stringBuffer.length() == 0))
			{
		        stringBuffer.append("\n");
		        stringBuffer.append("return " + curXlsFileName + ",");
		        for (int i = 0; i < nameStrings.size(); i++)
		        {
		        	String sheetName = (String)nameStrings.get(i);
		        	if (sheetName.lastIndexOf(".enum") > 0) {
		        		sheetName = sheetName.substring(0, sheetName.lastIndexOf(".enum"));
		        	}
		        	stringBuffer.append(curXlsFileName + "." + sheetName);
		        	if (i + 1 < nameStrings.size()) {
		        		stringBuffer.append(", ");
		        	}
		        }
		        stringBuffer.append("\n");
		        out.append(stringBuffer);
		        out.close();
			}
			
			println(excelFile.getName() + "��ȫ���������������lua�������£�");
			println("------------------------------------------------------------------------");
			println(stringBuffer.toString());
			
		 }catch (IOException e){
	    	interruptWithError(luaFile.getPath() + "�ļ�����ʧ�ܣ���");
	    }
	    println("������ɣ�" + luaFile.getName());
	}
	
	private static void showOutputDialog() {
		  JFrame jf = new JFrame("���");
		  JPanel jp = new JPanel();
		  JTextArea jta = new JTextArea(50,100);
		  JScrollPane jsp = new JScrollPane(jta);//�½�һ�����������棬���ı�����
		  jp.add(jsp);//ע�⣺��������������ӵ��齨�У�����������ı�����
		  jf.add(jp);
		  
		  jf.pack();
		  jf.setLocation(100,100);
		  jf.setResizable(false);
		  jf.setVisible(true);
		  jf.getRootPane().setWindowDecorationStyle(JRootPane.NONE);//����ָ���Ĵ���װ�η�� 
		  //jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		  jta.setText(allOutStringBuffer.toString());
	}
	
	private static void interruptWithError(String errString)  {
		allOutStringBuffer.append(errString + "\n");
		showOutputDialog();
		throw new RuntimeException(errString); 
	}
	
	private static void interruptWithError(Exception exception) {
		allOutStringBuffer.append(exception.toString() + "\n");
		showOutputDialog();
		throw new RuntimeException(exception); 
	}

	private static void print(String str){
		allOutStringBuffer.append(str);
		System.out.print(str);
	}
	
	private static void println(String str){
		allOutStringBuffer.append(str + "\n");
		System.out.println(str);
	}
	
	private static void addOutString(String str) {
		allOutStringBuffer.append(str);
	}
	
	private static void listExcel2Lua(){
		File excelDir = new File(importPath);
	    if (!excelDir.exists()) {
	    	interruptWithError("excel�ļ�Ŀ¼�����ڣ������á�");
	    }
	    for (File file : excelDir.listFiles()) {
	    	if ((!file.getName().substring(0, 2).equals("~$")) && (isExcelFile(file.getName()))) {
	    		excel2Lua(file.getPath());
	    	}
	    }
	    showOutputDialog();
	}
	
	private static void initConfig(){
		File file = new File("./config.cfg");
		if (!file.exists()) {
			interruptWithError("�����ļ�config.cfg�����ڣ���ʼ������ʧ�ܡ�");
		}
		
		HashMap<String, String> configMap = new HashMap<String, String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			println("��ʼ�����á�");
			String line;
			while( (line = in.readLine()) != null){
				if (line.contains("=")) {
					String kv[] = line.split("=");
					configMap.put(kv[0].trim(), kv[1].trim());
				}
			}
			in.close();
			
			importPath = configMap.get("importPath");
			exportPath = configMap.get("exportPath");
			
			for(Entry<String, String> entry: configMap.entrySet()){
				println(entry.getKey() + " = " + entry.getValue());
			}
			println("���ó�ʼ����ɡ�");
			
		} 
		catch (FileNotFoundException e) {
			interruptWithError("�����ļ�config.cfg�����ڣ���ʼ������ʧ�ܡ�");
		} catch (IOException e) {
			interruptWithError("��ȡ�����ļ�config.cfgʧ�ܡ�");
		}
	}
}
