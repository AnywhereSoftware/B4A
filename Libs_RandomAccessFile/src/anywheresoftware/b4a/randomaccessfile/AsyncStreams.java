
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.BreakIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.Adler32;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.streams.File;

/**
 * The AsyncStreams object allows you to read from an InputStream and write to an OutputStream in the background without blocking the main thread.
 *See the <link>AsyncStreams Tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/7669-asyncstreams-tutorial.html</link>.
 *NewData event is raised when new data is available.
 *Error event is raised when an error was encountered. You should check LastException to find the error.
 *Terminated event is raised when the other side has terminated the connection.
 *NewStream event is only raised in prefix mode when the other side sends a stream with WriteStream. This event is raised after the complete stream was received successfully.
 *The event includes the saved stream folder and name. Note that the file name is an arbitrary string.
 */
@ShortName("AsyncStreams")
@Events(values={"NewData (Buffer() As Byte)", "Error", "Terminated", "NewStream (Dir As String, FileName As String)"})
public class AsyncStreams {
	static final int STREAM_PREFIX = -2;
	static final byte[] CLOSE_PILL = new byte[0];
	private String eventName;
	private BA ba;
	private AIN ain;
	private Thread tin;
	private AOUT aout;
	private Thread tout;
	/**
	 * Received streams will be saved in this folder.
	 */
	public String StreamFolder;
	volatile long streamReceived;
	volatile long streamTotal;
	/**
	 * Initializes the object. Unlike in prefix mode, the NewData event will be raised with new data as soon as it is available.
	 *In - The InputStream that will be read. Pass Null if you only want to write with this object.
	 *Out - The OutputStream that is used for writing the data. Pass Null if you only want to read with this object.
	 *EventName - Determines the Subs that handle the NewData and Error events.
	 */
	public void Initialize(BA ba, InputStream In, OutputStream Out, String EventName) throws IOException {
		shared(ba, In, Out, EventName, false, false);
	}
	/**
	 * Initializes the object and sets it in "prefix" mode. In this mode incoming data should adhere to the following protocol:
	 *Every message begins with the message length as an Int value (4 bytes). This length should not include the additional 4 bytes.
	 *The NewData event will be raised only with full messages (not including the 4 bytes length value).
	 *The prefix Int value will be added to the output messages automatically.
	 *This makes it easier as you do not need to deal with broken messages.
	 *In - The InputStream that will be read. Pass Null if you only want to write with this object.
	 *BigEndian - Whether the length value is encoded in BigEndian or LittleEndian. 
	 *Out - The OutputStream that is used for writing the data. Pass Null if you only want to read with this object.
	 *EventName - Determines the Subs that handle the NewData and Error events.
	 */
	public void InitializePrefix(BA ba, InputStream In, boolean BigEndian, OutputStream Out, String EventName) throws IOException {
		if (File.getExternalWritable())
			StreamFolder = File.getDirDefaultExternal();
		else
			StreamFolder = File.getDirInternalCache();
		shared(ba, In, Out, EventName, BigEndian, true);
	}
	/**
	 * Returns the total number of bytes of the currently received file. Only valid in prefix mode.
	 */
	public long getStreamTotal() {
		return streamTotal;
	}
	/**
	 * Returns the number of bytes of the currently received file. Only valid in prefix mode.
	 */
	public long getStreamReceived() {
		return streamReceived;
	}
	private void shared(BA ba, InputStream In, OutputStream Out, String EventName, boolean BigEndian, boolean Prefix) throws IOException {
		if (IsInitialized())
			Close();
		this.ba = ba;
		this.eventName = EventName.toLowerCase(BA.cul);
		if (In != null) {
			ain = new AIN(In, BigEndian, Prefix);
			tin = new Thread(ain);
			tin.setDaemon(true);
			tin.start();
		}
		if (Out != null) {
			aout = new AOUT(Out, BigEndian, Prefix);
			tout = new Thread(aout);
			tout.setDaemon(true);
			tout.start();
		}
	}
	/**
	 * Tests whether this object has been initialized.
	 */
	public boolean IsInitialized() {
		return ain != null || aout != null;
	}
	/**
	 * Adds the given bytes array to the output stream queue.
	 *If the object was initialized with InitializePrefix then the array length will be added before the array.
	 *Returns False if the queue is full and it is not possible to queue the data.
	 */
	public boolean Write(byte[] Buffer) {
		return Write2(Buffer, 0, Buffer.length);
	}
	/**
	 * Adds the given bytes array to the output stream queue.
	 *If the object was initialized with InitializePrefix then the array length will be added before the array.
	 *Returns False if the queue is full and it is not possible to queue the data.
	 */
	public boolean Write2(byte[] Buffer, int Start, int Length) {
		AOUT a = aout;
		if (a == null)
			return false;
		return a.put(Buffer, Start, Length);
	}
	/**
	 * Sends a message to the internal queue. AsyncStreams will be closed when the message is processed.
	 *The Terminated event will be raised.
	 *Returns False if the queue is full or the connection is not open.
	 */
	public boolean SendAllAndClose() {
		return Write2(CLOSE_PILL, 0, 0);
	}
	/**
	 * Writes the given stream. This method is only supported in prefix mode.
	 *The checksum will be calculated and sent to the other size. The NewStream event will be raised, in the receiving side, after the stream was received successfully.
	 *This method is more efficient than sending the same data in chunks. It can handle streams of any size.
	 *In - InputStream that will be read. Note that the InputStream will be closed after the stream is sent.
	 *Size - Number of bytes to read from the stream.
	 */
	public boolean WriteStream(InputStream In, Long Size) {
		AOUT a = aout;
		if (a == null)
			return false;
		return a.put(In, Size);
	}
	/**
	 * Returns the number of messages waiting in the output queue.
	 */
	public int getOutputQueueSize() {
		if (aout == null)
			return 0;
		return aout.queue.size();
	}
	/**
	 * Closes the associated streams.
	 */
	public synchronized void Close() throws IOException {
		if (tin != null && ain != null) {
			ain.close();
			if (Thread.currentThread() != tin)
				tin.interrupt();
		}
		if (tout != null && aout != null) {
			aout.close();
			if (Thread.currentThread() != tout)
				tout.interrupt();
		}
		ain = null;
		aout = null;
	}

	private class AIN implements Runnable {
		private final InputStream in;
		private byte[] buffer = new byte[8192];
		private final byte[] prefixBuffer = new byte[4];
		private final boolean prefix;
		private volatile boolean working = true;
		private String ev = eventName + "_newdata";
		private ByteBuffer bb;
		public AIN (InputStream in, boolean bigEndian, boolean prefix) {
			this.in = in;
			this.prefix = prefix;
			if (prefix) {
				bb = ByteBuffer.wrap(new byte[8]);
				bb.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
			}
		}
		public void run() {
			try {
				byte[] data;

				while (working) {

					if (!prefix) {
						int count = in.read(buffer);
						if (count == 0)
							continue;
						if (count < 0) {
							closeUnexpected();
							break;
						}
						if (!working)
							break;
						data = new byte[count];
						System.arraycopy(buffer, 0, data, 0, count);
					}
					else { //prefix
						if (!readNumberOfBytes(in, prefixBuffer, 4))
							break;
						if (!working)
							break;
						bb.clear();
						bb.put(prefixBuffer, 0, 4);
						int msgLength = bb.getInt(0);
						if (msgLength > 100000000)
							throw new RuntimeException("Message size too large. Prefix mode can only work if both sides of the connection follow the 'prefix' protocol.");

						if (msgLength == STREAM_PREFIX) {
							if (!readNumberOfBytes(in, buffer, 8))
								break;
							bb.clear();
							bb.put(buffer, 0, 8);
							streamTotal = bb.getLong(0);
							streamReceived = 0;
							int i = 1;
							while (File.Exists(StreamFolder, String.valueOf(i))) {
								i++;
							}
							OutputStream out = File.OpenOutput(StreamFolder, String.valueOf(i), false).getObject();
							try {

								Adler32 adler = new Adler32();
								while (streamReceived < streamTotal) {
									long remain = streamTotal - streamReceived;
									int len = (int) (remain > buffer.length ? buffer.length : remain);
									if (!readNumberOfBytes(in, buffer, len))
										break;
									adler.update(buffer, 0, len);
									out.write(buffer, 0, len);
									streamReceived += len;
								}
								if (!readNumberOfBytes(in, buffer, 8))
									break;
								bb.clear();
								bb.put(buffer, 0, 8);
								long crc = bb.getLong(0);
								if (crc != adler.getValue())
									throw new Exception("CRC value does not match.");
							} finally {
								out.close();
							}
							ba.raiseEventFromDifferentThread(AsyncStreams.this, null, 0, eventName + "_newstream", true, new Object[] {StreamFolder,
									String.valueOf(i)});
							continue;//!!!

						}
						else {
							if (msgLength > buffer.length) { //length is larger than our array. Create a new array
								buffer = new byte[msgLength];
							}
							if (!readNumberOfBytes(in, buffer, msgLength))
								break;
							data = new byte[msgLength];
							System.arraycopy(buffer, 0, data, 0, data.length);
						}
					}
					ba.raiseEventFromDifferentThread(AsyncStreams.this, null, 0, ev, true, new Object[] {data});
				}

			} catch (Exception e) {
				if (working) {
					e.printStackTrace();
					ba.setLastException(e);
					ba.raiseEventFromDifferentThread(AsyncStreams.this, null, 0, eventName + "_error", false, null);
				}
			}

		}
		private boolean readNumberOfBytes(InputStream in, byte[] buffer, int len) throws IOException {
			int count = 0;
			while (count < len) {
				int c = in.read(buffer, count, len - count);
				if (c == -1) {
					closeUnexpected();
					return false;
				}
				count += c;
			}
			return true;
		}
		private void closeUnexpected() throws IOException {
			ba.raiseEventFromDifferentThread(AsyncStreams.this, null, 0, eventName + "_terminated", false, null);
			AsyncStreams.this.Close(); //close both threads
		}
		public void close() {
			working = false;
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	private class AOUT implements Runnable {
		private final OutputStream out;
		private volatile boolean working = true;
		private final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(100);
		private final boolean prefix;
		private final ByteBuffer bb;
		private byte[] streamBuffer;

		public AOUT (OutputStream out, boolean bigEndian, boolean prefix) {
			this.out = out;
			this.prefix = prefix;
			if (prefix) {
				bb = ByteBuffer.wrap(new byte[8]);
				bb.order(bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
			}
			else
				bb = null;
		}
		public void run() {
			while (working) {
				try {
					Object b = queue.take();
					if (b instanceof byte[]) {
						if (b == CLOSE_PILL) {
							ba.raiseEventFromDifferentThread(AsyncStreams.this, null, 0, eventName + "_terminated", false, null);
							AsyncStreams.this.Close(); //close both threads
							return;
						}
						out.write((byte[])b);
					}
					else {
						StreamAndSize st = (StreamAndSize)b;
						try {
							//stream
							Adler32 adler = new Adler32();
							synchronized (bb) {
								bb.putInt(0, STREAM_PREFIX);
								out.write(bb.array(), 0, 4);
								bb.putLong(0, st.size);
								out.write(bb.array(), 0, 8);
							}
							if (streamBuffer == null)
								streamBuffer = new byte[8192];
							int len;
							while ((len = st.in.read(streamBuffer)) > 0) {
								out.write(streamBuffer, 0, len);
								adler.update(streamBuffer, 0, len);
							}
							synchronized (bb) {

								bb.putLong(0, adler.getValue());
								out.write(bb.array(), 0, 8);
							}
						} finally {
							try {
								st.in.close();
							} catch (Exception ee) {
								ee.printStackTrace();
							}
						}
					}
				} catch (Exception e) {
					if (working) {
						e.printStackTrace();
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(AsyncStreams.this, null, 0, eventName + "_error", false, null);
					}
				}
			}
		}

		public boolean put(InputStream in, long size) {
			if (!prefix)
				throw new RuntimeException("WriteStream is only supported in prefix mode.");
			try {
				StreamAndSize st = new StreamAndSize();
				st.in = in;
				st.size = size;
				return queue.offer(st, 100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		public boolean put(byte[] buffer, int start, int len) {
			byte[] b;
			if (buffer == CLOSE_PILL)
				b = CLOSE_PILL;
			else {
				if (!prefix) {
					b = new byte[len];
					System.arraycopy(buffer, start, b, 0, len);
				}
				else {
					b = new byte[len + 4];
					synchronized (bb) {
						bb.putInt(0, len);
						System.arraycopy(bb.array(), 0, b, 0, 4);
					}
					System.arraycopy(buffer, start, b, 4, len);
				}
			}
			try {
				return queue.offer(b, 100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		public void close() {
			working = false;
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	static class StreamAndSize {
		long size;
		InputStream in;
	}
}
