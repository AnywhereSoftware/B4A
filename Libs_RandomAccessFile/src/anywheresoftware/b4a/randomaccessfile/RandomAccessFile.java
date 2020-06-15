
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.collections.Map.MyMap;
/**
 * This object allows you to non-sequentially access files and bytes arrays.
 *You can also use it to encode numbers to bytes (and vice versa).
 *Note that assets files (files added with the file manager) cannot be opened with this object as those files are actually packed inside the APK file.
 *A short tutorial about the encryption methods is available <link>here|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/11565-encryptying-information-randomaccessfile-library.html</link>.
 */
@ShortName("RandomAccessFile")
@Version(2.33f)
public class RandomAccessFile {
	private FileChannel channel;
	private ByteBuffer bb4;
	private ByteBuffer bb8;
    private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] SALT = new byte[] {12,54,23,45,23,52,12};
    private static final byte[] IV =  { 116, 13, 72, -50, 77, 45, -3, -72, -117, 32, 23, 19, 72, 21, 111, 22 };
    private static final byte COMPRESS = 1;
    private static final byte ENCRYPT = 2;
	private static final ConcurrentHashMap<String, String> correctedClasses = new ConcurrentHashMap<String, String>();
	private static final ConcurrentHashMap<Class<?>, Boolean> knownTypes = new ConcurrentHashMap<Class<?>, Boolean>();
	/**
	 * Holds the current file position.
	 *This value is updated automatically after each read or write operation.
	 */
	public long CurrentPosition;
	/**
	 * Opens the specified file.
	 *Note that it is not possible to open a file saved in the assets folder with this object.
	 *If needed you can copy the file to another location and then open it.
	 *ReadOnly - Whether to open the file in read only mode (otherwise it will be readable and writable).
	 *Example:<code>
	 *Dim raf As RandomAccessFile
	 *raf.Initialize(File.DirInternal, "1.dat", false)</code>
	 */
	public void Initialize(String Dir, String File, boolean ReadOnly) throws FileNotFoundException {
		Initialize2(Dir, File, ReadOnly, false);
	}
	/**
	 * Same as Initialize with the option to set the byte order to little endian instead of the
	 *default big endian. This can be useful when sharing files with Windows computers.
	 */
	public void Initialize2(String Dir, String File, boolean ReadOnly, boolean LittleEndian) throws FileNotFoundException {
		java.io.RandomAccessFile raf = new java.io.RandomAccessFile(new File(Dir, File), ReadOnly ? "r" : "rw");
		channel = raf.getChannel();
		bb4 = ByteBuffer.allocateDirect(4);
		bb8 = ByteBuffer.allocateDirect(8);
		if (LittleEndian) {
			bb4.order(ByteOrder.LITTLE_ENDIAN);
			bb8.order(ByteOrder.LITTLE_ENDIAN);
		}
		CurrentPosition = 0;
	}
	/**
	 * Treats the given buffer as a random access file with a constant size.
	 *This allows you to read and write values to an array of bytes.
	 */
	public void Initialize3(byte[] Buffer, boolean LittleEndian) {
		channel = new ByteArrayChannel(Buffer);
		bb4 = ByteBuffer.allocateDirect(4);
		bb8 = ByteBuffer.allocateDirect(8);
		if (LittleEndian) {
			bb4.order(ByteOrder.LITTLE_ENDIAN);
			bb8.order(ByteOrder.LITTLE_ENDIAN);
		}
		CurrentPosition = 0;
	}
	/**
	 * Returns the file size.
	 */
	public long getSize() throws IOException {
		return channel.size();
	}
	/**
	 * Closes the stream.
	 */
	public void Close() throws IOException {
		channel.close();
	}
	/**
	 * Flushes any cached data.
	 */
	public void Flush() throws IOException {
		channel.force(true);
	}
	/**
	 * Reads an Int value stored in the specified position.
	 *Reads 4 bytes.
	 */
	public int ReadInt(long Position) throws IOException {
		bb4.clear();
		channel.read(bb4, Position);
		bb4.flip();
		CurrentPosition = Position + 4;
		return bb4.getInt();
	}
	/**
	 * Reads a Float value stored in the specified position.
	 *Reads 4 bytes.
	 */
	public float ReadFloat(long Position) throws IOException {
		bb4.clear();
		channel.read(bb4, Position);
		bb4.flip();
		CurrentPosition = Position + 4;
		return bb4.getFloat();
	}
	/**
	 * Reads a Short value stored in the specified position.
	 *Reads 2 bytes.
	 */
	public short ReadShort(long Position) throws IOException {
		bb4.clear();
		bb4.limit(2);
		channel.read(bb4, Position);
		bb4.flip();
		CurrentPosition = Position + 2;
		return bb4.getShort();
	}
	/**
	 * Reads a Long value stored in the specified position.
	 *Reads 8 bytes.
	 */
	public long ReadLong(long Position) throws IOException {
		bb8.clear();
		channel.read(bb8, Position);
		bb8.flip();
		CurrentPosition = Position + 8;
		return bb8.getLong();
	}
	/**
	 * Reads a Double value stored in the specified position.
	 *Reads 8 bytes.
	 */
	public double ReadDouble(long Position) throws IOException {
		bb8.clear();
		channel.read(bb8, Position);
		bb8.flip();
		CurrentPosition = Position + 8;
		return bb8.getDouble();
	}
	/**
	 * Reads an unsigned bytes (0 - 255) stored in the specified position.
	 *The value returned is of type Int as Byte can only store values between -128 to 127.
	 */
	public int ReadUnsignedByte(long Position) throws IOException {
		CurrentPosition = Position + 1;
		return ReadSignedByte(Position) & 0xFF;
	}
	/**
	 * Reads a signed byte (-128 - 127) stored in the specified position.
	 */
	public byte ReadSignedByte(long Position) throws IOException {
		CurrentPosition = Position + 1;
		bb4.clear();
		bb4.limit(1);
		channel.read(bb4, Position);
		bb4.flip();
		return bb4.get();
	}
	/**
	 * Reads bytes from the stream and into to the given array.
	 *Buffer - Array of bytes where the data will be written to.
	 *StartOffset - The first byte read will be written to Buffer(StartOffset).
	 *Length - Number of bytes to read.
	 *Position - The position of the first byte to read.
	 *Returns the number of bytes read which is equal to Length (unless the file is smaller than the requested length).
	 */
	public int ReadBytes(byte[] Buffer, int StartOffset, int Length, long Position) throws IOException {
		int read = 0;
		while (read < Length) {
			int c = channel.read(ByteBuffer.wrap(Buffer, StartOffset + read, Length - read), Position + read);
			if (c == -1)
				break;
			read += c;
		}
		CurrentPosition = Position + read;
		return read;
	}
	/**
	 * Writes an Int value to the specified position.
	 *Writes 4 bytes.
	 */
	public void WriteInt(int Value, long Position) throws IOException {
		bb4.clear();
		bb4.putInt(Value);
		bb4.flip();
		CurrentPosition = Position + 4;
		channel.write(bb4, Position);
	}
	/**
	 * Writes a Float value to the specified position.
	 *Writes 4 bytes.
	 */
	public void WriteFloat(float Value, long Position) throws IOException {
		bb4.clear();
		bb4.putFloat(Value);
		bb4.flip();
		CurrentPosition = Position + 4;
		channel.write(bb4, Position);
	}
	/**
	 * Writes a Short value to the specified position.
	 *Writes 2 bytes.
	 */
	public void WriteShort(short Value, long Position) throws IOException {
		bb4.clear();
		bb4.putShort(Value);
		bb4.flip();
		CurrentPosition = Position + 2;
		channel.write(bb4, Position);
	}
	/**
	 * Writes a Long value to the specified position.
	 *Writes 8 bytes.
	 */
	public void WriteLong(long Value, long Position) throws IOException {
		bb8.clear();
		bb8.putLong(Value);
		bb8.flip();
		CurrentPosition = Position + 8;
		channel.write(bb8, Position);
	}
	/**
	 * Writes a Double value to the specified position.
	 *Writes 8 bytes.
	 */
	public void WriteDouble(double Value, long Position) throws IOException {
		bb8.clear();
		bb8.putDouble(Value);
		bb8.flip();
		CurrentPosition = Position + 8;
		channel.write(bb8, Position);
	}
	/**
	 * Writes a Byte value to the specified position.
	 *Writes 1 byte.
	 */
	public void WriteByte(byte Byte, long Position) throws IOException {
		bb4.clear();
		bb4.put(Byte);
		bb4.flip();
		CurrentPosition = Position + 1;
		channel.write(bb4, Position);
	}
	/**
	 * Writes the given buffer to the stream. The first byte written is Buffer(StartOffset)
	 *and the last is Buffer(StartOffset + Length - 1).
	 *Returns the numbers of bytes written which is equal to Length.
	 */
	public int WriteBytes(byte[] Buffer, int StartOffset, int Length, long Position) throws IOException {
		int written = 0;
		while (written < Length) {
			int w = channel.write(ByteBuffer.wrap(Buffer, StartOffset + written, Length - written), Position + written);
			written += w;
		}
		CurrentPosition = Position + Length;
		return Length;
	}
	/**
	 * Similar to WriteObject. This method writes the object in a format supported by B4i, B4A and B4J.
	 * The following types are supported: Lists, Arrays of bytes and Arrays of objects, Maps, Strings, primitive types and user defined types.
	 * Note that user defined types should be declared in the Main module.
	 */
	public void WriteB4XObject(Object Object, long Position) throws Exception {
		B4XSerializator bs = new B4XSerializator();
		byte[] arr = bs.WriteObject(Object);
		WriteInt(arr.length, Position);
		WriteByte((byte)1, CurrentPosition); //compressed field
		WriteBytes(arr, 0, arr.length, CurrentPosition);
	}
	/**
	 * Reads an object previously written with WriteB4XObject.
	 */
	public Object ReadB4XObject(long Position) throws Exception {
		B4XSerializator bs = new B4XSerializator();
		int len = ReadInt(Position);
		CurrentPosition += 1; //compressed field
		byte[] arr = new byte[len];
		ReadBytes(arr, 0, arr.length, CurrentPosition);
		return bs.ReadObject(arr);

	}
	/**
	 * Writes the given object to the stream.
	 *This method is capable of writing the following types of objects: Lists, Arrays, Maps, Strings, primitive types and user defined types.
	 *Combinations of these types are also supported. For example, a map with several lists of arrays can be written.
	 *The element type inside a collection must be a String or primitive type.
	 *Note that changing your package name may make older objects files unusable (requiring you to write them again).
	 *Object - The object that will be written.
	 *Compress - Whether to compress the data before writing it. Should be true in most cases.
	 *Position - The position in the file that this object will be written to.
	 */
	public void WriteObject(Object Object, boolean Compress, long Position) throws IllegalArgumentException, IllegalAccessException, IOException {
		writeHelper(Compress ? COMPRESS : 0, Object, null, Position);
	}
	/**
	 * Similar to WriteObject. The object is encrypted with AES-256 and then written to the stream.
	 *Note that it is faster to write a single large object compared to many smaller objects.
	 *Object - The object that will be written.
	 *Password - The password that protects the object.
	 *Position - The position in the file that this object will be written to.
	 */
	public void WriteEncryptedObject(Object Object, String Password, long Position) throws IllegalArgumentException, IOException, IllegalAccessException {
		writeHelper(ENCRYPT, Object, Password, Position);
	}
	private void writeHelper(byte mode, Object Object,String password, long Position) throws IOException, IllegalArgumentException, IllegalAccessException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream out;
		if (mode == COMPRESS || mode == ENCRYPT) {
			GZIPOutputStream gz = new GZIPOutputStream(bo);
			out = new ObjectOutputStream(gz);
		}
		else {
			out = new ObjectOutputStream(bo);
		}
		writeObject(out, Object);
		out.close();
		byte[] b = bo.toByteArray();
		if (mode == ENCRYPT)
			b = encrypt(b, password);
		int size = b.length + 1; //the mode byte is included in the size for historical reasons.
		WriteInt(size, Position);
		WriteByte(mode, Position + 4);
		ByteBuffer bb = ByteBuffer.wrap(b);
		int c = 0, offset = 0;
		do {
			c = channel.write(bb, Position + 5 + offset);
			offset += c;
		} while (c > 0);
		CurrentPosition = Position + size - 1 + 5;
	}
	private byte[] encrypt(byte[] data, String password) {
		try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
            KeySpec keySpec =
                new PBEKeySpec(password.toCharArray(),SALT, 1024, 256);
            SecretKey tmp = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher e = Cipher.getInstance(CIPHER_ALGORITHM);
            e.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(IV));
            return e.doFinal(data);
        } catch (GeneralSecurityException e) {
            // This can't happen on a compatible Android device.
            throw new RuntimeException("Invalid environment", e);
        }
	}
	/**
	 * Reads an object from the stream.
	 *See WriteObject for supported types.
	 */
	public Object ReadObject(long Position) throws IOException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		return readHelper(null, Position);
	}
	/**
	 * Reads an encrypted object from the stream.
	 *Password - The password used while writing the object.
	 *Position - Stream position.
	 */
	public Object ReadEncryptedObject(String Password, long Position) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		return readHelper(Password, Position);
	}
	private Object readHelper(String password, long Position) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		int size = ReadInt(Position);
		byte mode = ReadSignedByte(Position + 4);
		byte[] b = new byte[size - 1]; //the mode byte is included in the size
		ByteBuffer bb = ByteBuffer.wrap(b);
		int i = channel.read(bb, Position + 5);
		while (i < size) {
			int c = channel.read(bb, Position + 5 + i);
			if (c == 0)
				break;
			i += c;
		}
		CurrentPosition = Position + 5 + size - 1;

		if (mode == ENCRYPT) {
			if (password == null)
				throw new RuntimeException("Data was encrypted. You should provide password.");
			b = decrypt(b, password);
		}
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		ObjectInputStream oin;
		
		if (mode == COMPRESS || mode == ENCRYPT) {
			GZIPInputStream gin = new GZIPInputStream(in);
			oin = new ObjectInputStream(gin);
		}
		else {
			oin = new ObjectInputStream(in);
		}
		Object o = readObject(oin);
		oin.close();
		return o;
	}
	private byte[] decrypt(byte[] b, String password) {
		try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
            KeySpec keySpec =
                new PBEKeySpec(password.toCharArray(),SALT, 1024, 256);
            SecretKey tmp = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            Cipher d = Cipher.getInstance(CIPHER_ALGORITHM);
            d.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(IV));
            return d.doFinal(b);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error decrypting object.", e);
        }
	}
	private static final byte LIST_TYPE = 1;
	private static final byte WRAPPED = 2;
	private static final byte NOT_WRAPPED = 3;
	private static final byte MAP_TYPE = 4;
	private static final byte ARRAY_TYPE = 5;
	private static final byte SERIALIZED_TYPE = 6;
	private static final byte B4ATYPE_TYPE = 7;
	private static final byte NULL_TYPE = 8;

	@SuppressWarnings("unchecked")
	private void writeObject(ObjectOutputStream out, Object o) throws IOException, IllegalArgumentException, IllegalAccessException {
		Object oo;
		if (o instanceof ObjectWrapper)
			oo = ((ObjectWrapper<Object>)o).getObject();
		else
			oo = o;
		
		if (oo instanceof java.util.Map) {
			out.write(MAP_TYPE);
			writeMap(out, o);
		}
		else if (oo instanceof java.util.List) {
			out.write(LIST_TYPE);
			writeList(out, o);
		}
		else if (o != null && (o.getClass().isArray())) {
			out.write(ARRAY_TYPE);
			writeArray(out, o);
		}
		else if (o instanceof Serializable) {
			out.write(SERIALIZED_TYPE);
			out.writeObject(o);
		}
		else if (o != null) {
			writeType(out, o);
		}
		else {
			out.write(NULL_TYPE);
		}
	}
	private Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Object o = null;
		switch (in.read()) {
		case MAP_TYPE:
			o = readMap(in);
			break;
		case LIST_TYPE:
			o = readList(in);
			break;
		case ARRAY_TYPE:
			o = readArray(in);
			break;
		case SERIALIZED_TYPE:
			o = in.readObject();
			break;
		case B4ATYPE_TYPE:
			o = readType(in);
			break;
		case NULL_TYPE:
			o = null;
			break;
		}
		return o;
	}
	@Hide
	public static Field[] isB4XType(Object type) {
		Field[] fields = type.getClass().getDeclaredFields();
		Boolean IsInitializedField = knownTypes.get(type.getClass());
		if (IsInitializedField == null) {
			IsInitializedField = false;
			for (Field f : fields) {
				if (f.getName().equals("IsInitialized")) {
					IsInitializedField = true;
					break;
				}
			}
			knownTypes.put(type.getClass(), IsInitializedField);
		}
		return IsInitializedField ? fields : null;
	}
	private void writeType(ObjectOutputStream out, Object type) throws IOException, IllegalArgumentException, IllegalAccessException {
		Field[] fields = isB4XType(type);
		if (fields == null) {
			out.write(NULL_TYPE);
			return;
		}
		out.write(B4ATYPE_TYPE);
		out.writeObject(type.getClass().getName());
		Map.MyMap map = new Map.MyMap();
		for (Field f : fields) {
			f.setAccessible(true);
			map.put(f.getName(), f.get(type));
		}
		writeMap(out, map);
	}
	@Hide
	public static Class<?> readTypeClass(String className) throws ClassNotFoundException {
		Class<?> c;
		try {
			if (correctedClasses.containsKey(className))
				className = correctedClasses.get(className);
			c = Class.forName(className);
		} catch (ClassNotFoundException cnfe) {
			int dollar = className.lastIndexOf("."); //b4j.example.main$_point
			String corrected;
			if (dollar > -1)
				corrected = BA.packageName + className.substring(dollar);
			else 
				corrected = BA.packageName + ".main$" + className; //from b4i
			//BA.Log("Class not found: " + className + ", trying: " + corrected);
			try {
				c = Class.forName(corrected);
			} catch (ClassNotFoundException cnfe2) {
				corrected = corrected.replace(".main$", ".b4xmainpage$");
				c = Class.forName(corrected);
			}
			correctedClasses.put(className, corrected);
		}
		return c;
	}

	private Object readType(ObjectInputStream in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		String className = (String)in.readObject();
		Class<?> c = readTypeClass(className);
		MyMap map = (MyMap) readMap(in);
		Object o = c.newInstance();
		for (Field f : c.getDeclaredFields()) {
			Object val = map.get(f.getName());
			if (val != null || map.containsKey(f.getName()) == true) {
				f.setAccessible(true);
				f.set(o, val);
			}
		}
		return o;
	}
	@SuppressWarnings("unchecked")
	private void writeMap(ObjectOutputStream out, Object m) throws IOException, IllegalArgumentException, IllegalAccessException {
		java.util.Map<Object, Object> map;
		if (m instanceof ObjectWrapper) {
			map = (java.util.Map)((ObjectWrapper)m).getObject();
			out.write(WRAPPED);			
		}
		else {
			map = (java.util.Map) m;
			out.write(NOT_WRAPPED);
		}
		out.writeInt(map.size());
		for (Entry e : map.entrySet()) {
			writeObject(out, e.getKey());
			writeObject(out, e.getValue());
		}
	}
	private Object readMap(ObjectInputStream in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		boolean shouldWrap;
		shouldWrap = in.readByte() == WRAPPED;
		Map.MyMap Map = new Map.MyMap();
		int size = in.readInt();
		for (int i = 0;i < size;i++) {
			Object key = readObject(in);
			Object value = readObject(in);
			Map.put(key, value);
		}
		if (shouldWrap) {
			Map m = new Map();
			m.setObject(Map);
			return m;
		}
		else
			return Map;
	}
	private void writeArray(ObjectOutputStream out, Object array) throws IOException, IllegalArgumentException, IllegalAccessException {
		out.writeObject(array.getClass().getComponentType());
		int size = Array.getLength(array);
		out.writeInt(size);
		for (int i = 0;i < size;i++) {
			writeObject(out, Array.get(array, i));
		}
	}
	private Object readArray(ObjectInputStream in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> c = (Class<?>) in.readObject();
		int size = in.readInt();
		Object arr = Array.newInstance(c, size);
		for (int i = 0;i < size;i++) {
			Array.set(arr, i,  readObject(in));
		}
		return arr;
	}
	@SuppressWarnings("unchecked")
	private void writeList(ObjectOutputStream out, Object l) throws IOException, IllegalArgumentException, IllegalAccessException {
		java.util.List list;
		if (l instanceof ObjectWrapper) {
			list = (java.util.List)((ObjectWrapper)l).getObject();
			out.write(WRAPPED);			
		}
		else {
			list = (java.util.List) l;
			out.write(NOT_WRAPPED);
		}
		out.writeInt(list.size());
		for (int i = 0;i < list.size();i++) {
			writeObject(out, list.get(i));
		}
	}
	@SuppressWarnings("unchecked")
	private Object readList(ObjectInputStream in) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		boolean shouldWrap;
		shouldWrap = in.readByte() == WRAPPED;
		java.util.List list = new ArrayList();
		int size = in.readInt();
		for (int i = 0;i < size;i++) {
			list.add(readObject(in));
		}
		if (shouldWrap) {
			List l = new List();
			l.setObject(list);
			return l;
		}
		else
			return list;
	}
	private static class ByteArrayChannel extends FileChannel {
		private byte[] buffer;
		public ByteArrayChannel(byte[] buffer) {
			this.buffer = buffer;
		}
		@Override
		public void force(boolean metaData) throws IOException {
			//
		}
		@Override
		public FileLock lock(long position, long size, boolean shared)
				throws IOException {
			return null;
		}
		@Override
		public MappedByteBuffer map(MapMode mode, long position, long size)
				throws IOException {
			return null;
		}
		@Override
		public long position() throws IOException {
			return 0;
		}
		@Override
		public FileChannel position(long newPosition) throws IOException {
			return null;
		}
		@Override
		public int read(ByteBuffer dst) throws IOException {
			throw new UnsupportedOperationException();
		}
		@Override
		public int read(ByteBuffer dst, long position) throws IOException {
			int pos = (int)position;
			while (dst.hasRemaining()) {
				dst.put(buffer[pos++]);
			}
			return pos - (int)position;
		}
		@Override
		public long read(ByteBuffer[] dsts, int offset, int length)
				throws IOException {
			return 0;
		}
		@Override
		public long size() throws IOException {
			return buffer.length;
		}
		@Override
		public long transferFrom(ReadableByteChannel src, long position,
				long count) throws IOException {
			return 0;
		}
		@Override
		public long transferTo(long position, long count,
				WritableByteChannel target) throws IOException {
			return 0;
		}
		@Override
		public FileChannel truncate(long size) throws IOException {
			return null;
		}
		@Override
		public FileLock tryLock(long position, long size, boolean shared)
				throws IOException {
			return null;
		}
		@Override
		public int write(ByteBuffer src) throws IOException {
			return 0;
		}
		@Override
		public int write(ByteBuffer src, long position) throws IOException {
			int pos = (int)position;
			while (src.hasRemaining()) {
				buffer[pos++] = src.get();
			}
			return pos - (int)position;
		}
		@Override
		public long write(ByteBuffer[] srcs, int offset, int length)
				throws IOException {
			return 0;
		}
		@Override
		protected void implCloseChannel() throws IOException {
			//
		}
	}
}
