
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
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.tidy.Tidy;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.streams.File;

/**
 * Tidy allows you to convert a HTML page to XHTML format and then parse it with XML parser.
 *Example<code>
 *Sub Process_Globals
 *    Dim sax As SaxParser
 *    Dim tid As Tidy
 *End Sub
 *
 *Sub Activity_Create(FirstTime As Boolean)
 *    tid.Initialize
 *    'parse the Html page and create a new xml document.
 *    tid.Parse(File.OpenInput(File.DirAssets, "index.html"), File.DirRootExternal, "1.xml")
 *    sax.Initialize
 *    sax.Parse(File.OpenInput(File.DirRootExternal, "1.xml"), "sax")
 *End Sub</code>
 */
@ShortName("Tidy")
@Version(1.10f)
public class TidyWrapper {
	@Hide
	public Tidy tidy;
	public void Initialize() {
		tidy = new Tidy();
		tidy.setXmlOut(true);
	}
	/**
	 * Parses the given HTML input stream and generates a XHTML file that can be parsed with a XML parser.
	 */
	public void Parse(InputStream In, String Dir, String FileName) throws IOException {
		OutputStream out = File.OpenOutput(Dir, FileName, false).getObject();
		tidy.parse(In, out);
		In.close();
		out.close();
	}
	
}
