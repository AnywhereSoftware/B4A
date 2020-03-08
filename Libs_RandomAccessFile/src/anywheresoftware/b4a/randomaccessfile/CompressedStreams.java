
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
 
 package anywheresoftware.b4a.randomaccessfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
import anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper;

/**
 * CompressedStreams object allows you to compress and decompress data using gzip or zlib compression methods.
 *There are two options for working with CompressedStreams:
 *Wrapping another stream by calling WrapInputStream or WrapOutputStream.
 *Compressing or decompressing the data in memory.
 *The following example demonstrates the usage of this object:<code>
 *Sub Globals
 *
 *End Sub
 *
 *Sub Activity_Create(FirstTime As Boolean)
 *	Dim sb As StringBuilder
 *	sb.Initialize
 *	'Concatenation operations are much faster with StringBuilder than with String.
 *	For i = 1 To 10000
 *		sb.Append("Playing with compressed streams.").Append(CRLF)
 *	Next
 *	Dim out As OutputStream
 *	Dim s As String
 *	Dim compress As CompressedStreams
 *	s = sb.ToString
 *	'Write the string without compressing it (we could have used File.WriteString instead).
 *	out = File.OpenOutput(File.DirRootExternal, "test.txt", False)
 *	WriteStringToStream(out, s)
 *	
 *	'Write the string with gzip compression.
 *	out = File.OpenOutput(File.DirRootExternal, "test.gz", False)
 *	out = compress.WrapOutputStream(out, "gzip")
 *	WriteStringToStream(out, s)
 *	
 *	'Write the string with zlib compression
 *	out = File.OpenOutput(File.DirRootExternal, "test.zlib", False)
 *	out = compress.WrapOutputStream(out, "zlib")
 *	WriteStringToStream(out, s)
 *	
 *	'Show the files sizes
 *	Msgbox("No compression: " & File.Size(File.DirRootExternal, "test.txt") & CRLF _
 *		& "Gzip: " & File.Size(File.DirRootExternal, "test.gz") & CRLF _
 *		& "zlib: " & File.Size(File.DirRootExternal, "test.zlib"), "Files sizes")
 *
 *	'Read data from a compressed file
 *	Dim in As InputStream
 *	in = File.OpenInput(File.DirRootExternal, "test.zlib")
 *	in = compress.WrapInputStream(in, "zlib")
 *	Dim reader As TextReader
 *	reader.Initialize(in)
 *	Dim line As String
 *	line = reader.ReadLine
 *	Msgbox(line, "First line")
 *	reader.Close
 *	
 *	'In memory compression / decompression
 *	Dim data() As Byte
 *	data = "Playing with in-memory compression.".GetBytes("UTF8")
 *	Dim compressed(), decompressed() As Byte
 *	compressed = compress.CompressBytes(data, "gzip")
 *	decompressed = compress.DecompressBytes(compressed, "gzip")
 *	'In this case the compressed data is longer than the decompressed data.
 *	'The data is too short for the compression to be useful.
 *	Log("Compressed: " & compressed.Length) 
 *	Log("Decompressed: " & decompressed.Length)
 *	Msgbox(BytesToString(decompressed,0, decompressed.Length, "UTF8"), "")
 *End Sub
 *Sub WriteStringToStream(Out As OutputStream, s As String)
 *	Dim t As TextWriter
 *	t.Initialize(Out)
 *	t.Write(s)
 *	t.Close 'Closes the internal stream as well
 *End Sub</code>
 */
@ShortName("CompressedStreams")
public class CompressedStreams {
	/**
	 * Wraps an input stream and returns an input stream that automatically decompresses the stream when it is read.
	 *In - The original input stream.
	 *CompressMethod - The name of the compression method (gzip or zlib).
	 */
	public InputStreamWrapper WrapInputStream(InputStream In, String CompressMethod) throws IOException {
		InputStream cin = getInputStream(CompressMethod, In);
		InputStreamWrapper i = new InputStreamWrapper();
		i.setObject(cin);
		return i;
	}
	/**
	 * Wraps an output streams and returns an output stream that automatically compresses the data when it is written to the stream.
	 *Out - The original output stream.
	 *CompressMethod - The name of the compression method (gzip or zlib).
	 */
	public OutputStreamWrapper WrapOutputStream(OutputStream Out, String CompressMethod) throws IOException {
		OutputStream cout = getOutputStream(CompressMethod, Out);
		OutputStreamWrapper i = new OutputStreamWrapper();
		i.setObject(cout);
		return i;
	}
	/**
	 * Returns a byte array with the compressed data.
	 *Data - Data to compress.
	 *CompressMethod - The name of the compression method (gzip or zlib).
	 */
	public byte[] CompressBytes(byte[] Data, String CompressMethod) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream(Data.length);
		OutputStream outputStream = getOutputStream(CompressMethod, b);
		outputStream.write(Data);
		outputStream.close();
		return b.toByteArray();
		
	}
	/**
	 * Returns a byte array with the decompressed data.
	 *CompressedData - The compressed data that should be decompressed.
	 *CompressMethod - The name of the compression method (gzip or zlib).
	 */
	public byte[] DecompressBytes(byte[] CompressedData, String CompressMethod) throws IOException {
		ByteArrayInputStream b = new ByteArrayInputStream(CompressedData);
		InputStream in = getInputStream(CompressMethod, b);
		ByteArrayOutputStream out = new ByteArrayOutputStream(CompressedData.length);
		File.Copy2(in, out);
		return out.toByteArray();
	}
	private InputStream getInputStream(String CompressMethod, InputStream In) throws IOException {
		String m = CompressMethod.toLowerCase(BA.cul);
		InputStream cin;
		if (m.equals("gzip")) {
			cin = new GZIPInputStream(In);
		}
		else if (m.equals("zlib")) {
			cin = new InflaterInputStream(In);
		}
		else {
			throw new RuntimeException("Unknown compression method: " + CompressMethod);
		}
		return cin;
	}
	private OutputStream getOutputStream(String CompressMethod, OutputStream out) throws IOException {
		String m = CompressMethod.toLowerCase(BA.cul);
		OutputStream cout;
		if (m.equals("gzip")) {
			cout = new GZIPOutputStream(out);
		}
		else if (m.equals("zlib")) {
			cout = new DeflaterOutputStream(out);
		}
		else {
			throw new RuntimeException("Unknown compression method: " + CompressMethod);
		}
		return cout;
	}
			
}
