
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
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.RaisesSynchronousEvents;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.keywords.StringBuilderWrapper;
import anywheresoftware.b4a.objects.collections.List;

/**
 * A parser that sequentially reads a stream and raises events at the beginning and end of each element.
 *The StartElement includes the following parameters:
 *Uri - Namespace Uri, or empty string if there is no namespace.
 *Name - The element name.
 *Attributes - An Attributes object holding the element's attributes.
 *
 *The EndElement includes the following parameters:
 *Uri - Namespace Uri, or empty string if there is no namespace.
 *Name - The element name.
 *Text - The element text (if such exists).
 */
@ShortName("SaxParser")
@Events(values={"StartElement (Uri As String, Name As String, Attributes As Attributes)",
		"EndElement (Uri As String, Name As String, Text As StringBuilder)"})
@Version(1.11f)
public class SaxParser {
	@Hide
	public SAXParser sp;
	private String eventName;
	private BA ba;
	/**
	 * This library provides an XML Sax Parser.
	 *See this <link>tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6866-xml-parsing-xmlsax-library.html</link> for a working example.
	 */
	public static void LIBRARY_DOC() {
		
	}
	/**
	 * A list that holds the names of the parents elements.
	 *During parsing you can use this list to recognize the current element.
	 */
	public List Parents = new List();
	/**
	 * Initializes the object.
	 *Usually this object should be a process global object.
	 */
	public void Initialize(BA ba) throws ParserConfigurationException, SAXException {
		sp = SAXParserFactory.newInstance().newSAXParser();
		this.ba = ba;
	}
	/**
	 * Parses the given InputStream.
	 *EventName - The events subs name prefix.
	 */
	@RaisesSynchronousEvents
	public void Parse(InputStream InputStream, String EventName) throws ParserConfigurationException, SAXException, IOException {
		parse(new InputSource(InputStream), EventName);
	}
	private void parse(InputSource in, String EventName) throws SAXException, IOException {
		this.eventName = EventName.toLowerCase(BA.cul);
		MyHandler m = new MyHandler();
		XMLReader xr = sp.getXMLReader();
		xr.setContentHandler(m);
		xr.parse(in);
	}
	/**
	 * Parses the given TextReader.
	 *EventName - The events subs name prefix.
	 */
	@RaisesSynchronousEvents
	public void Parse2(Reader TextReader, String EventName) throws ParserConfigurationException, SAXException, IOException {
		parse(new InputSource(TextReader), EventName);
	}
	private class MyHandler extends DefaultHandler {
		private StringBuilder sb = new StringBuilder();
		private final String startEvent = eventName + "_" + "startelement";
		private final String endEvent = eventName + "_" + "endelement";
		private StringBuilderWrapper sbw = new StringBuilderWrapper();
		private AttributesWrapper aw = new AttributesWrapper();
		public MyHandler() {
			Parents.Initialize();
		}
		@Override
		public void startElement (String uri, String localName,
				String qName, Attributes attributes)
		throws SAXException
		{
			sb.setLength(0);
			aw.setObject(attributes);
			ba.raiseEvent2(null, true, startEvent,false, uri, localName, aw);
			Parents.Add(localName);
		}
		@Override
		public void characters (char ch[], int start, int length)
		throws SAXException
	    {
			sb.append(ch, start, length);
	    }
		@Override
		public void endElement (String uri, String localName, String qName)
		throws SAXException
	    {
			Parents.RemoveAt(Parents.getSize() - 1);
			sbw.setObject(sb);
			ba.raiseEvent2(null,true, endEvent,false, uri, localName, sbw);
	    }
	}
	/**
	 *This object is passed in StartElement event.
	 */
	@ShortName("Attributes")
	public static class AttributesWrapper extends AbsObjectWrapper<Attributes> {
		/**
		 * Returns the number of attributes in this element.
		 */
		public int getSize() {
			return getObject().getLength();
		}
		/**
		 * Returns the name of the attribute at the specified index.
		 *Note that the order of elements can change.
		 */
		public String GetName(int Index) {
			return getObject().getLocalName(Index);
		}
		/**
		 * Returns the value of the attribute at the specified index.
		 *Note that the order of elements can change.
		 */
		public String GetValue(int Index) {
			return getObject().getValue(Index);
		}
		/**
		 * Returns the value of the attribute with the following Uri and Name.
		 *Pass an empty string as the Uri if namespaces are not used.
		 *Returns an empty string if no such attribute was found.
		 */
		public String GetValue2(String Uri, String Name) {
			String r =  getObject().getValue(Uri, Name);
			return r == null ? "" : r;
		}
	}
}
