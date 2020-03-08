
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
 
 package anywheresoftware.b4a;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import android.graphics.Color;

public class ConnectorUtils {
    public static final byte INT = 1;
    public static final byte STRING = 2;
    public static final byte MAP = 3;
    public static final byte ENDOFMAP = 4;
    public static final byte BOOL = 5;
    public static final byte COLOR = 6;
    public static final byte FLOAT = 7;
    public static final byte SCALED_INT = 8;
    public static final byte CACHED_STRING = 9;
    public static final byte RECT32 = 11;
    public static final byte NULL = 12;
    
	private static ThreadLocal<ByteBuffer> myBb = new ThreadLocal<ByteBuffer>() {
		@Override
		protected ByteBuffer initialValue() {
	        ByteBuffer bbW = ByteBuffer.allocate(50 * 1024);
			bbW.order(ByteOrder.LITTLE_ENDIAN);
			return bbW;
	    }
	};

	private static Charset charset = Charset.forName("UTF8");
	public static ByteBuffer startMessage(byte message) {
		ByteBuffer bbW = myBb.get();
		bbW.clear();
		bbW.put(message);
		return bbW;
	}
	public static void sendMessage(ConnectorConsumer consumer) {
		if (consumer == null)
			return;
		ByteBuffer bbW = myBb.get();
		bbW.flip();
		boolean addPrefix = consumer.shouldAddPrefix();
		byte[] b;
		if (addPrefix) {
			
			b = new byte[bbW.limit() + 4];
			int size = b.length - 4;
			bbW.get(b, 4, size);
			for (int i = 0; i <= 3; i++) {
	            b[i] = (byte) (size & 0xFF);
	            size = size >> 8;
	        }
		}
		else {
			b = new byte[bbW.limit()];
			bbW.get(b);
		}
		consumer.putTask(b);
		
	}
	public static void writeInt(int i) {
		myBb.get().putInt(i);
	}
	public static void writeFloat(float f) {
		myBb.get().putFloat(f);
	}
	public static void mark() {
		myBb.get().mark();
	}
	public static void resetToMark() {
		myBb.get().reset();
	}
	/**
	 * Returns true if there was enough room for the string.
	 */
	public static boolean writeString(String str) {
		if (str == null)
			str = "";
		if (str.length() > 700)
			str = str.substring(0, 699) + "......";
		ByteBuffer bbW = myBb.get();
		int pos = bbW.position();
		ByteBuffer bb = charset.encode(str);
		if (bbW.remaining() - bb.remaining() < 1000) {
			return false;
		}
		else {
			bbW.putInt(0); //stub
			bbW.put(bb);
		}
		bbW.putInt(pos, bbW.position() - pos - 4);
		return true;
	}

	public static int readInt(DataInputStream in) throws IOException {
		return Integer.reverseBytes(in.readInt());
	}
	public static short readShort(DataInputStream in) throws IOException {
		return Short.reverseBytes(in.readShort());
	}
	public static String readString(DataInputStream in) throws IOException {
		int size = readInt(in);
		ByteBuffer bb = ByteBuffer.allocate(size);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		in.readFully(bb.array());
		bb.limit(size);
		String s = charset.decode(bb).toString();
		return s;
	}
	private static String readCacheString(DataInputStream din, String[] cache) throws IOException {
		if (cache == null)
			return readString(din);
		return cache[ConnectorUtils.readInt(din)];
	}
	
	@SuppressWarnings("null")
	public static HashMap<String, Object> readMap(DataInputStream in, String[] cache) throws IOException {
		HashMap<String, Object> props = new HashMap<String, Object>();
		while (true) {
			String key = readCacheString(in, cache);
			byte b = in.readByte();
			Object value = null;
			if (b == INT)
				value = readInt(in);
			else if (b == CACHED_STRING) {
				value = readCacheString(in, cache);
			}
			else if (b == STRING) {
				value = readString(in);
			}
			
			else if (b == FLOAT) {
				value = Float.intBitsToFloat(readInt(in));
			}
			else if (b == MAP)
				value = readMap(in, cache);
			else if (b == BOOL) {
				value = (in.readByte() == 1);
			}
			else if (b == COLOR) {
				value = Color.argb(in.readUnsignedByte(), in.readUnsignedByte(),
						in.readUnsignedByte(), in.readUnsignedByte());
			} else if (b == NULL)
				value = null;
			else if (b == RECT32) {
				value = new int[] {readShort(in), readShort(in), readShort(in), readShort(in)};
			}
			else if (b == ENDOFMAP)
				break;
			else
				throw new RuntimeException("unknown type");
			props.put(key, value);
		}
		return props;
	}
	
	
}
