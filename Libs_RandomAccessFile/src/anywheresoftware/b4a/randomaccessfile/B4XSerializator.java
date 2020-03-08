
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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.collections.Map.MyMap;
//identical to B4J
@ShortName("B4XSerializator")
@Events(values={"ObjectToBytes (Success As Boolean, Bytes() As Byte)",
		"BytesToObject (Success As Boolean, NewObject As Object)"})

public class B4XSerializator {
	private OutputStream out;
	private DataInputStream in;
	private ByteBuffer bb;
	private Object tag;
	private final static byte T_NULL = 0, T_STRING = 1, T_SHORT = 2, T_INT = 3, T_LONG = 4, T_FLOAT = 5,
	T_DOUBLE = 6, T_BOOLEAN = 7, T_BYTE = 10, T_CHAR = 14, T_MAP = 20, T_LIST = 21,
	T_NSARRAY = 22, T_NSDATA = 23, T_TYPE = 24;
	//	private HashMap<String, Short> stringsCacheMap;
	//	private ArrayList<String> stringsCache;
	//	private final static int maxLengthForStringsCache = 200;
	public B4XSerializator() {
		bb = ByteBuffer.wrap(new byte[8]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
	}
	public void setTag(Object tag) {
		this.tag = tag;
	}
	/**
	 * Gets or sets the Tag value. This is a place holder that can used to store additional data.
	 */
	public Object getTag() {
		return tag;
	}
	/**
	 * In-memory version of RandomAccessFile.WriteB4XObject.
	 *The following types are supported: Lists, Arrays of bytes and Arrays of objects, Maps, Strings, primitive types and user defined types.
	 *Note that user defined types should be declared in the Main module.
	 */
	public byte[] ConvertObjectToBytes(Object Object) throws IOException {
		return WriteObject(Object);
	}
	/**
	 * Asynchronously converts the object to bytes. The ObjectToBytes event will be raised with the serialized bytes.
	  *Do not reuse the same B4XSerializator instance when calling asynchronous methods. 
	 */
	public void ConvertObjectToBytesAsync(BA ba, final Object Object, String EventName) {
		BA.runAsync(ba, this, EventName.toLowerCase(BA.cul) + "_objecttobytes", new Object[] {false, new byte[0]}, new Callable<Object[]> (){

			@Override
			public java.lang.Object[] call() throws Exception {
				return new Object[] {true, ConvertObjectToBytes(Object)};
			}
			
		});
	}
	/**
	 * In-memory version of RandomAccessFile.ReadB4XObject.
	 */
	public Object ConvertBytesToObject(byte[] Bytes) throws IOException {
		return ReadObject(Bytes);
	}
	/**
	 * Asynchronously converts the bytes to object. The BytesToObject event will be raised when the object is ready.
	 *Do not reuse the same B4XSerializator instance when calling asynchronous methods.   
	 */
	public void ConvertBytesToObjectAsync(BA ba, final byte[] Bytes, String EventName) {
		BA.runAsync(ba, this, EventName.toLowerCase(BA.cul) + "_bytestoobject", new Object[] {false, null}, new Callable<Object[]> (){

			@Override
			public java.lang.Object[] call() throws Exception {
				return new Object[] {true, ConvertBytesToObject(Bytes)};
			}
			
		});
	}
	
	@Hide
	public byte[] WriteObject(Object Object) throws IOException {
		//		stringsCacheMap = new HashMap<String, Short>();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		out = new DeflaterOutputStream(bout);
		writeObject(Object);
		out.close();
		return bout.toByteArray();
	}
	@Hide
	public Object ReadObject(byte[] arr) throws IOException {
		//		stringsCache = new ArrayList<String>();
		in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(arr)));
		Object ret = readObject();
		in.close();
		return ret;
	}
	private void writeInt(int i) throws IOException {
		bb.putInt(0, i);
		out.write(bb.array(), 0, 4);
	}
	private void writeShort(short s) throws IOException {
		bb.putShort(0, s);
		out.write(bb.array(), 0, 2);
	}
	private int readInt() throws IOException {
		in.readFully(bb.array(), 0, 4);
		return bb.getInt(0);
	}
	private short readShort() throws IOException {
		in.readFully(bb.array(), 0, 2);
		return bb.getShort(0);
	}
	private byte readByte() throws IOException {
		return in.readByte();
	}
	private void writeByte(byte b) throws IOException {
		out.write(b);
	}

	@SuppressWarnings("unchecked")
	private void writeObject(Object o) throws IOException {
		if (o instanceof ObjectWrapper) //this will only happen inside types that have list of map fields
			o = ((ObjectWrapper<Object>)o).getObject();

		if (o == null)
			writeByte(T_NULL);
		else if (o instanceof Number) {
			if (o instanceof Integer) {
				writeByte(T_INT);
				writeInt((Integer)o);
			} else if (o instanceof Double) {
				writeByte(T_DOUBLE);
				bb.putDouble(0, (Double)o);
				out.write(bb.array(), 0, 8);
			} else if (o instanceof Float) {
				writeByte(T_FLOAT);
				bb.putFloat(0, (Float)o);
				out.write(bb.array(), 0, 4);
			} else if (o instanceof Long) {
				writeByte(T_LONG);
				bb.putLong(0, (Long)o);
				out.write(bb.array(), 0, 8);
			} else if (o instanceof Byte) {
				writeByte(T_BYTE);
				writeByte((Byte)o);
			} else if (o instanceof Short) {
				writeByte(T_SHORT);
				writeShort((Short)o);
			}
		} else if (o instanceof Character) {
			writeByte(T_CHAR);
			bb.putChar(0, (Character)o);
			out.write(bb.array(), 0, 2);
		} else if (o instanceof Boolean) {
			writeByte(T_BOOLEAN);
			boolean b = (Boolean)o;
			writeByte((byte)(b ? 1 : 0));
		} else if (o instanceof String) {
			//String st = (String)o;
			//			Short s = null;
			//			if (st.length() < maxLengthForStringsCache) {
			//				s = stringsCacheMap.get(st);
			//			}
			//			if (s != null) {
			//				writeByte(T_CACHED_STRING);
			//				writeShort(s);
			//			}
			//			else {
			byte[] temp = ((String)o).getBytes("UTF8");
			//				if (st.length() < maxLengthForStringsCache && stringsCacheMap.size() < Short.MAX_VALUE) {
			//					short pos = (short) stringsCacheMap.size();
			//					stringsCacheMap.put((String)o, pos);
			//					writeByte(T_CACHED_STRING_ADD);
			//					writeShort((short) temp.length);
			//				}
			//				else {
			writeByte(T_STRING);
			writeInt(temp.length);
			//				}
			out.write(temp);
			//			}

		} else if (o instanceof List) {
			writeByte(T_LIST);
			writeList((List<?>)o);
		} else if (o instanceof Map) {
			writeByte(T_MAP);
			writeMap((Map<?,?>)o);
		} else if (o.getClass().isArray()) {
			if (o.getClass().getComponentType() == byte.class) {
				writeByte(T_NSDATA);
				byte[] b = (byte[])o;
				writeInt(b.length);
				out.write(b);
			}
			else if (o.getClass().getComponentType().isPrimitive()) {
				throw new RuntimeException("This method does not support arrays of primitives.");
			} else {
				writeByte(T_NSARRAY);
				Object[] oo = (Object[])o;
				writeList(Arrays.asList(oo));
			}
		} else {
			writeByte(T_TYPE);
			writeType(o);
		}
	}
	private Map<?,?> readMap() throws IOException {
		int len = readInt();
		MyMap mm = new MyMap();
		for (int i = 0;i < len;i++) {
			mm.put(readObject(), readObject());
		}
		return mm;
	}
	private void writeMap(Map<?,?> m) throws IOException {
		writeInt(m.size());
		for (Entry<?,?> e: m.entrySet()) {
			writeObject(e.getKey());
			writeObject(e.getValue());
		}
	}
	private ArrayList<?> readList() throws IOException {
		int len = readInt();
		ArrayList<Object> arr = new ArrayList<Object>(len);
		for (int i = 0;i < len;i++)
			arr.add(readObject());
		return arr;
	}
	private void writeList(List<?> list) throws IOException {
		writeInt(list.size());
		for (Object o : list) {
			writeObject(o);
		}
	}
	private void writeType(Object target) throws IOException {
		Field[] fields = RandomAccessFile.isB4XType(target);
		if (fields == null)
			throw new RuntimeException("Cannot serialize object: " + String.valueOf(target));
		try {
			writeObject(target.getClass().getName());
			MyMap map = new MyMap();
			for (Field f : fields) {
				f.setAccessible(true);
				map.put(f.getName(), f.get(target));
			}
			writeMap(map);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private Object readType()  {
		try {
			String cls = (String)readObject();
			Class<?> c = RandomAccessFile.readTypeClass(cls);
			MyMap map = (MyMap) readMap();
			Object o = c.newInstance();
			for (Field f : c.getDeclaredFields()) {
				Object val = map.get(f.getName());
				if (val == null)
					val = map.get("_" + f.getName());
				if (val != null) {
					f.setAccessible(true);
					if (f.getType() == anywheresoftware.b4a.objects.collections.List.class) {
						val = AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.collections.List(), val);
					}
					else if (f.getType() == anywheresoftware.b4a.objects.collections.Map.class)
						val = AbsObjectWrapper.ConvertToWrapper(new anywheresoftware.b4a.objects.collections.Map(), val);
					else if (f.getType() == boolean.class) {
						if (val instanceof Boolean == false)
							val = ((Number)val).intValue() == 1;
					}
					f.set(o, val);
				}
			}
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private Object readObject() throws IOException {
		byte t = readByte();
		int len;
		byte[] b;
		switch (t) {
		case T_NULL:
			return null;
		case T_INT:
			return readInt();
		case T_SHORT:
			return readShort();
		case T_LONG:
			in.readFully(bb.array(), 0, 8);
			return bb.getLong(0);
		case T_FLOAT:
			in.readFully(bb.array(), 0, 4);
			return bb.getFloat(0);
		case T_DOUBLE:
			in.readFully(bb.array(), 0, 8);
			return bb.getDouble(0);
		case T_BOOLEAN:
			return readByte() == 1;
		case T_BYTE:
			return readByte();
		case T_STRING:
			len = readInt();
			b = new byte[len];
			in.readFully(b);
			return new String(b, "UTF8");
//		case T_CACHED_STRING: {
//			short s = readShort();
//			return stringsCache.get(s);
//		}
//		case T_CACHED_STRING_ADD: {
//			len = readShort();
//			byte[] buffer = new byte[len];
//			in.readFully(buffer);
//			String s = new String(buffer, "UTF8");
//			stringsCache.add(s);
//			return s;
//		}
		case T_CHAR:
			in.readFully(bb.array(), 0, 2);
			return bb.getChar(0);
		case T_LIST:
			return readList();
		case T_MAP:
			return readMap();
		case T_NSDATA:
			len = readInt();
			b = new byte[len];
			in.readFully(b);
			return b;
		case T_NSARRAY:
			List<?> list = readList();
			return list.toArray();
		case T_TYPE:
			return readType();
		}
		throw new RuntimeException("Unsupported type: " + t);

	}

}
