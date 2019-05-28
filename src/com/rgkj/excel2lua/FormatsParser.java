package com.rgkj.excel2Lua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatsParser
{
	public boolean isSuccess = false;
	public String referSheetName = "";
	public int referRowCount = 0;
	public Set<Integer> referRows = new HashSet();
	public HashMap<String, String> referContent = new HashMap();
	public HashMap<String, String> map = new HashMap();
  
	public void referParse(String referDesc)
	{
		String[] strs = referDesc.split(":");
		this.referSheetName = strs[0];
		if (this.referSheetName.isEmpty()) {
			return;
		}
		if (strs.length > 1)
		{
			ArrayList<String> list = new ArrayList();
			Pattern pattern = Pattern.compile("\\[(.*?)\\]");
			Matcher m = pattern.matcher(strs[1]);
			while (m.find())
			{
			    int i = 1;
			    list.add(m.group(i));
			    i++;
			}
			this.referRows.clear();
  
			String cxt = ((String)list.get(0)).trim();
			String[] rows = cxt.split(",");
			for (String row : rows) {
				if (Uitls.isNumeric(row))
				{
					int index = Integer.valueOf(row.trim()).intValue();
					this.referRows.add(Integer.valueOf(index));
				}
				else
				{
					this.isSuccess = false;
					return;
				}
			}
		}
		this.referRowCount = this.referRows.size();
		this.isSuccess = true;
	}
  
	public void mapParse(String referDesc)
	{
		String[] items = referDesc.split(",");
		for (String str : items)
		{
			String[] _items = str.split("=");
			if ((_items.length == 2) && (_items[0].trim().length() > 0))
			{
				_items[0] = _items[0].replace(" ", "");
				_items[1] = _items[1].replace(" ", "");
				 this.map.put(_items[0], _items[1]);
			}
			else
			{
				this.isSuccess = false;
				return;
			}
		}
		this.isSuccess = true;
	}
}
