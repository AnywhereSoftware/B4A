
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4a.objects;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.widget.TextView;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.streams.File;

/**
 * Collection of strings related functions.
 *The <link>Table example|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6946-creating-table-view-based-scrollview.html</link> uses LoadCSV to show the data in a table.
 */
@ShortName("StringUtils")
@Version(1.12f)
public class StringUtils {
	
	
	/**
	 * Returns the required height in order to show the given text in a label.
	 *This can be used to show dynamic text in a label.
	 *Note that the label must first be added to its parent and only then its height can be set.
	 *Example:<code>
	 *Dim Label1 As Label
	 *Label1.Initialize("")
	 *Label1.Text = "this is a long sentence, and we need to " _ 
	 *	& "know the height required in order To show it completely."
	 *Label1.TextSize = 20
	 *Activity.AddView(Label1, 10dip, 10dip, 200dip, 30dip)
	 *Dim su As StringUtils
	 *Label1.Height = su.MeasureMultilineTextHeight(Label1, Label1.Text)</code>
	 */
	public int MeasureMultilineTextHeight(TextView TextView, CharSequence Text) {
		StaticLayout sl = new StaticLayout(Text, TextView.getPaint(), 
				TextView.getLayoutParams().width - TextView.getPaddingLeft() - TextView.getPaddingRight(),
				Alignment.ALIGN_NORMAL, 1, 0 , true);
		return sl.getLineTop(sl.getLineCount());
	}
	@Hide
	public int MeasureMultilineTextHeight(TextView TextView, String Text) {
		return MeasureMultilineTextHeight(TextView, (CharSequence)Text);
	}
	/**
	 * Encodes the given bytes array into Base64 notation.
	 *Example:<code>
	 *Dim su As StringUtils
	 *Dim encoded As String
	 *encoded = su.EncodeBase64(data) 'data is a bytes array</code>
	 */
	public String EncodeBase64(byte[] Data) {
		return Base64.encodeBytes(Data);
	}
	/**
	 * Decodes data from Base64 notation.
	 */
	public byte[] DecodeBase64(String Data) throws IOException {
		return Base64.decode(Data);
	}
	/**
	 * Encodes a string into application/x-www-form-urlencoded format.
	 *Url - String to encode.
	 *CharSet - The character encoding name.
	 *Example:<code>
	 *Dim su As StringUtils
	 *Dim url, encodedUrl As String
	 *encodedUrl = su.EncodeUrl(url, "UTF8")</code>
	 */
	public String EncodeUrl(String Url, String CharSet) throws UnsupportedEncodingException {
		return URLEncoder.encode(Url, CharSet);
	}
	/**
	 * Decodes an application/x-www-form-urlencoded string. 
	 */
	public String DecodeUrl(String Url, String CharSet) throws UnsupportedEncodingException {
		return URLDecoder.decode(Url, CharSet);
	}
	/**
	 * Saves the table as a CSV file.
	 *Dir - Output file folder.
	 *FileName - Output file name.
	 *SeparatorChar - Separator character. The character that separates fields.
	 *Table - A List with arrays of strings as items. Each array represents a row. All arrays should be of the same length.
	 *Example:<code>
	 *Dim su As StringUtils
	 *su.SaveCSV(File.DirRootExternal, "1.csv", ",", table)</code>
	 */
	public static void SaveCSV (String Dir, String FileName, char SeparatorChar, List Table) throws IOException {
		SaveCSV2(Dir, FileName, SeparatorChar, Table, null);
	}
	/**
	 * Similar to SaveCSV. Will save the headers list as the first row. This should be a list (or array) of strings.
	 */
	public static void SaveCSV2 (String Dir, String FileName, char SeparatorChar, List Table, List Headers) throws IOException
	{
		int colCount = ((String[])Table.Get(0)).length;
		StringBuilder data = new StringBuilder();
		Pattern problemChars = Pattern.compile("[\"\\r\\n" + SeparatorChar + "]");
		if (Headers != null) {
			for (Object s : Headers.getObject()) {
				data.append(Word((String)s,problemChars,SeparatorChar));
			}
			data.setCharAt(data.length() - 1, '\n');
		}
		for (int rowI = 0;rowI < Table.getSize();rowI++)
		{
			String[] row = (String[])Table.Get(rowI);
			for (int i = 0;i<colCount;i++)
			{
				data.append(Word(row[i],problemChars,SeparatorChar));
			}
			data.setCharAt(data.length() - 1, '\n');
		}
		File.WriteString(Dir, FileName, data.toString());
	}

	private static String Word(String word, Pattern problemChars, char sep)
	{
		if (problemChars.matcher(word).find())
		{
			word = "\"" + word + "\"";
			int i = word.indexOf('\"',1);
			while (i > -1 && i < word.length() - 1)
			{
				word = word.substring(0, i) + "\"" + word.substring(i);
				i = word.indexOf("\"",i+2);
			}
		}
		return word+sep;
	}
	/**
	 * Loads a CSV file and stores it in a list of string arrays.
	 *Dir - CSV file folder.
	 *FileName - CSV file name.
	 *SeparatorChar - The character used to separate fields.
	 *Example:<code>
	 *Dim su As StringUtils
	 *Dim Table As List
	 *Table = su.LoadCSV(File.DirAssets, "1.csv", ",")</code>
	 */
	public static List LoadCSV (String Dir, String FileName, char SeparatorChar) throws IOException {
		return LoadCSV2(Dir, FileName, SeparatorChar, null);
	}
	/**
	 * Similar to LoadCSV. Will load the first row to the headers list.
	 */
	public static List LoadCSV2 (String Dir, String FileName, char SeparatorChar, List Headers) throws IOException {
		int i = 0, i2,i3;
		String data = File.ReadString(Dir, FileName);			
		List Table = new List();
		Table.Initialize();
		ArrayList<String> alFirstLine = new ArrayList<String>();
		boolean unixEnd = true;
		while (i< data.length()) //read first line
		{
			if (data.charAt(i) == '\"')
			{
				i2 = data.indexOf("\"",i+1);
				boolean shouldReplaceQuotes = false;
				while (i2 < data.length() && i2 >-1)
				{
					if (i2 == data.length() -1 || data.charAt(i2+1) != '\"')
						break;
					shouldReplaceQuotes = true;
					i2 = data.indexOf("\"",i2+2);
				}
				String ret = data.substring(i+1,i2);
				if (shouldReplaceQuotes)
					ret = ret.replace("\"\"", "\"");
				alFirstLine.add(ret);
				i = i2+2;
				if (data.charAt(i2+1) == '\r' || data.charAt(i2 + 1) == '\n')
				{
					if (data.charAt(i2+1) == '\r') {
						unixEnd = false;
						i++;
					}
					break;
				}
			}
			else
			{
				i2 = data.indexOf(SeparatorChar,i);
				i3 = data.indexOf('\n',i);
				if (i3 == -1) { //single line with no newline
					data = data + "\n";
					i3 = data.length() - 1;
				}
				if (i3 < i2 || i2 == -1) //last word in line
				{
					if (data.charAt(i3 - 1) == '\r') {
						unixEnd = false;
						i3--;
					}
					alFirstLine.add(data.substring(i,i3));
					i = i3 + (unixEnd ? 1 : 2);
					break;
				}
				else
				{
					alFirstLine.add(data.substring(i,i2));
					i = i2+1;
				}
			}
		}
		if (data.charAt(data.length() - 1) != '\n')
			data = data + (unixEnd ? "\n" : "\r\n");
		int colCount = alFirstLine.size();
		if (Headers != null) {
			if (!Headers.IsInitialized())
				Headers.Initialize();
			for (String s : alFirstLine) {
				Headers.Add(s);
			}
		}
		else {
			String[] list = new String[colCount];
			for (int a = 0;a < list.length;a++) {
				list[a] = alFirstLine.get(a);
			}
			Table.Add(list);
		}
		int[] ii = new int[1];
		ii[0] = i;
		while (ii[0] < data.length() - 1) //adds the data
		{
			String[] list = new String[colCount];
			for (i2 = 0; i2 < colCount-1; i2++) {
				list[i2] = ReadWord(data, ii,SeparatorChar);
			}
			if (!unixEnd) {
				list[i2] = ReadWord(data,ii, '\r');
				ii[0]++;
			}
			else
				list[i2] = ReadWord(data, ii, '\n');
			Table.Add(list);
		}
		return Table;
	}

	private static String ReadWord(String data,int[] ii,char sep) 
	{
		int i2;
		String ret;
		if (data.charAt(ii[0]) == '\"')
		{
			i2 = data.indexOf("\"",ii[0]+1);
			boolean shouldReplaceQuotes = false;
			while (i2 < data.length() && i2 >-1)
			{
				if (i2 == data.length()-1 || data.charAt(i2+1) != '\"')
					break;
				shouldReplaceQuotes = true;
				i2 = data.indexOf("\"",i2+2);
			}
			ret = data.substring(ii[0]+1,i2);
			if (shouldReplaceQuotes)
				ret = ret.replace("\"\"", "\"");
			ii[0] = i2+2;
		}
		else
		{
			i2 = data.indexOf(sep,ii[0]);
			ret = data.substring(ii[0],i2);
			ii[0] = i2+1;
		}
		return ret;
	}
}
