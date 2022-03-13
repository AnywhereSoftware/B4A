
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
 
 package anywheresoftware.b4a.keywords;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import anywheresoftware.b4a.objects.streams.File;

/**
 * Bit is a predefined object containing bitwise related methods.
 *Example:<code>
 *Dim flags As Int
 *flags = Bit.Or(100, 200)</code>
 */
public class Bit {
	/**
	 * Returns the bitwise AND of the two values.
	 */
	public static int And(int N1, int N2) {
		return N1 & N2;
	}
	
	/**
	 * Returns the bitwise AND of the two values.
	 */
	public static long AndLong(long N1, long N2) {
		return N1 & N2;
	}
	
	/**
	 * Returns the bitwise OR of the two values.
	 */
	public static int Or(int N1, int N2) {
		return N1 | N2;
	}
	
	/**
	 * Returns the bitwise OR of the two values.
	 */
	public static long OrLong(long N1, long N2) {
		return N1 | N2;
	}
	/**
	 * Returns the bitwise XOR of the two values.
	 */
	public static int Xor(int N1, int N2) {
		return N1 ^ N2;
	}
	
	/**
	 * Returns the bitwise XOR of the two values.
	 */
	public static long XorLong(long N1, long N2) {
		return N1 ^ N2;
	}
	/**
	 * Returns the bitwise complement of the given value.
	 */
	public static int Not(int N) {
		return ~N;
	}
	
	/**
	 * Returns the bitwise complement of the given value.
	 */
	public static long NotLong(long N) {
		return ~N;
	}
	/**
	 * Shifts N left.
	 *Shift - Number of positions to shift.
	 */
	public static int ShiftLeft (int N, int Shift) {
		return N << Shift;
	}
	/**
	 * Shifts N left.
	 *Shift - Number of positions to shift.
	 */
	public static long ShiftLeftLong (long N, int Shift) {
		return N << Shift;
	}
	
	/**
	 * Shifts N right.
	 *Keeps the original value sign
	 *Shift - Number of positions to shift.
	 */
	public static int ShiftRight (int N, int Shift) {
		return N >> Shift;
	}
	
	/**
	 * Shifts N right.
	 *Keeps the original value sign
	 *Shift - Number of positions to shift.
	 */
	public static long ShiftRightLong (long N, int Shift) {
		return N >> Shift;
	}
	
	/**
	 * Shifts N right.
	 *Shifts zeroes in the leftmost positions.
	 *Shift - Number of positions to shift.
	 */
	public static int UnsignedShiftRight (int N, int Shift) {
		return N >>> Shift;
	}
	/**
	 * Shifts N right.
	 *Shifts zeroes in the leftmost positions.
	 *Shift - Number of positions to shift.
	 */
	public static long UnsignedShiftRightLong (long N, int Shift) {
		return N >>> Shift;
	}
	
	/**
	 * Returns a string representation of N in base 2.
	 */
	public static String ToBinaryString(int N) {
		return Integer.toBinaryString(N);
	}
	/**
	 * Returns a string representation of N in base 8.
	 */
	public static String ToOctalString(int N) {
		return Integer.toOctalString(N);
	}
	/**
	 * Returns a string representation of N in base 16.
	 */
	public static String ToHexString(int N) {
		return Integer.toHexString(N);
	}
	/**
	 * Returns a string representation of N in base 16.
	 */
	public static String ToHexStringLong(long N) {
		return Long.toHexString(N);
	}
	/**
	 * Parses Value as an integer using the specified radix.
	 *Radix - Should be between 2 to 36.
	 */
	public static int ParseInt(String Value, int Radix) {
		return Integer.parseInt(Value, Radix);
	}
	/**
	 * Parses Value as a long using the specified radix.
	 *Radix - Should be between 2 to 36.
	 */
	public static long ParseLong(String Value, int Radix) {
		return Long.parseLong(Value, Radix);
	}
	/**
	 * Reads the data from the input stream and writes it into an array of bytes.
	 *The input stream is automatically closed at the end.
	 */
	public static byte[] InputStreamToBytes(InputStream In) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		File.Copy2(In, out);
		return out.toByteArray();
	}
	/**
	 * Copies elements from SrcArray to DestArray.
	 *SrcArray - Source array.
	 *SrcOffset - Index of first element in the source array.
	 *DestArray - Destination array.
	 *DestOffset - Index of the first element in the destination array.
	 *Count - Number of elements to copy.
	 */
	public static void ArrayCopy(Object SrcArray, int SrcOffset, Object DestArray, int DestOffset, int Count){
		System.arraycopy(SrcArray, SrcOffset, DestArray, DestOffset, Count);
	}
}
