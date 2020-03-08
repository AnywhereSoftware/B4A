
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

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
import anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper;

@Hide
public class CountingStreams {
	/**
	 * CountingInputStream and CountingOutputStream allow you to monitor the reading or writing progress.
	 *Counting streams wrap the actual stream and provide a Count property which allows you to get the number of bytes read or written.
	 *Counting streams are useful when the reading or writing operations are done in the background. You can then use a timer to monitor the progress.
	 *This example logs the downloading progress:
	 *<code>
	 *Sub Process_Globals
	 *	Dim hc As HttpClient
	 *	Dim cout As CountingOutputStream
	 *	Dim length As Int
	 *	Dim timer1 As Timer
	 *End Sub
	 *Sub Globals
	 *
	 *End Sub
	 *Sub Activity_Create(FirstTime As Boolean)
	 *	If FirstTime Then
	 *		hc.Initialize("hc")
	 *		timer1.Initialize("Timer1", 500)
	 *	End If
	 *	Dim req As HttpRequest
	 *	req.InitializeGet("http://www.basic4ppc.com/android/files/b4a-trial.zip")
	 *	hc.Execute(req, 1)
	 *End Sub
	 *
	 *Sub hc_ResponseSuccess (Response As HttpResponse, TaskId As Int)
	 *	cout.Initialize(File.OpenOutput(File.DirRootExternal, "1.zip", False))
	 *	Timer1.Enabled = True
	 *	length = Response.ContentLength
	 *	Response.GetAsynchronously("response", cOut, True, TaskId)
	 *End Sub
	 *
	 *Sub hc_ResponseError (Response As HttpResponse, Reason As String, StatusCode As Int, TaskId As Int)
	 *	Log("Error: " & Reason)
	 *	If Response <> Null Then
	 *		Log(Response.GetString("UTF8"))
	 *		Response.Release
	 *	End If
	 *End Sub
	 *
	 *Sub Response_StreamFinish (Success As Boolean, TaskId As Int)
	 *	timer1.Enabled = False
	 *	If Success Then
	 *		Timer1_Tick 'Show the current counter status
	 *		Log("Success!")
	 *	Else
	 *		Log("Error: " & LastException.Message)
	 *	End If
	 *End Sub
	 *
	 *Sub Timer1_Tick
	 *	Log(cout.Count & " out of " & length)
	 *End Sub</code>
	 */
	@ShortName("CountingInputStream")
	public static class CountingInput extends InputStreamWrapper {
		/**
		 * Initializes the counting stream by wrapping the given input stream.
		 */
		public void Initialize(InputStream InputStream) {
			setObject(new MyInputStream(InputStream));
		}
		@Hide
		@Override
		public void InitializeFromBytesArray (byte[] Buffer, int StartOffset, int MaxCount) {
			//
		}
		/**
		 * Gets or sets the number of bytes read.
		 */
		public long getCount() {
			return ((MyInputStream)getObject()).counter.get();
		}
		public void setCount(long v) {
			((MyInputStream)getObject()).counter.set(v);
		}
		
	}
	private static class MyInputStream extends FilterInputStream {
		AtomicLong counter = new AtomicLong(0);
		protected MyInputStream(InputStream in) {
			super(in);
		}
		@Override
		public int read() throws IOException {
			int r = in.read();
			if (r >= 0)
				counter.addAndGet(1);
			return r;
		}


		@Override
		public int read(byte[] buffer) throws IOException {
			return read(buffer, 0, buffer.length);

		}
		@Override
		public int read(byte[] buffer, int offset, int count) throws IOException {
			int r = in.read(buffer, offset, count);
			if (r > 0)
				counter.addAndGet(r);
			return r;
		}

	}
	/**
	 * See CountingInputStream for more information.
	 */
	@ShortName("CountingOutputStream")
	public static class CountingOutput extends OutputStreamWrapper {
		/**
		 * Initializes the counting stream by wrapping the given output stream.
		 */
		public void Initialize(OutputStream OutputStream) {
			setObject(new MyOutputStream(OutputStream));
		}
		@Override
		@Hide
		public void InitializeToBytesArray(int StartSize) {
			//
		}
		/**
		 * Gets or sets the number of bytes written.
		 */
		public long getCount() {
			return ((MyOutputStream)getObject()).counter.get();
		}
		public void setCount(long v) {
			((MyOutputStream)getObject()).counter.set(v);
		}
		
	}
	private static class MyOutputStream extends FilterOutputStream {
		AtomicLong counter = new AtomicLong(0);
	    public MyOutputStream(OutputStream out) {
			super(out);
		}

		@Override
	    public void write(byte[] buffer) throws IOException {
	    	write(buffer, 0, buffer.length);
	    }

	    @Override
	    public void write(byte[] buffer, int offset, int count) throws IOException {
	        out.write(buffer, offset, count);
	        counter.addAndGet(count);
	    }

	    @Override
	    public void write(int oneByte) throws IOException {
	        out.write(oneByte);
	        counter.addAndGet(1);
	    }
	}
}
