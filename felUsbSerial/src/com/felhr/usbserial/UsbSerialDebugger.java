
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
 
 package com.felhr.usbserial;

import com.felhr.utils.HexData;

import android.util.Log;

public class UsbSerialDebugger
{
    private static final String CLASS_ID = UsbSerialDebugger.class.getSimpleName();
    public static final String ENCODING = "UTF-8";

    private UsbSerialDebugger()
    {

    }

    public static void printLogGet(byte[] src, boolean verbose)
    {
        if(!verbose)
        {
            Log.i(CLASS_ID, "Data obtained from write buffer: " + new String(src));
        }else
        {
            Log.i(CLASS_ID, "Data obtained from write buffer: " + new String(src));
            Log.i(CLASS_ID, "Raw data from write buffer: " + HexData.hexToString(src));
            Log.i(CLASS_ID, "Number of bytes obtained from write buffer: " + src.length);
        }
    }

    public static void printLogPut(byte[] src, boolean verbose)
    {
        if(!verbose)
        {
            Log.i(CLASS_ID, "Data obtained pushed to write buffer: " + new String(src));
        }else
        {
            Log.i(CLASS_ID, "Data obtained pushed to write buffer: " + new String(src));
            Log.i(CLASS_ID, "Raw data pushed to write buffer: " + HexData.hexToString(src));
            Log.i(CLASS_ID, "Number of bytes pushed from write buffer: " + src.length);
        }
    }

    public static void printReadLogGet(byte[] src, boolean verbose)
    {
        if(!verbose)
        {
            Log.i(CLASS_ID, "Data obtained from Read buffer: " + new String(src));
        }else
        {
            Log.i(CLASS_ID, "Data obtained from Read buffer: " + new String(src));
            Log.i(CLASS_ID, "Raw data from Read buffer: " + HexData.hexToString(src));
            Log.i(CLASS_ID, "Number of bytes obtained from Read buffer: " + src.length);
        }
    }

    public static void printReadLogPut(byte[] src, boolean verbose)
    {
        if(!verbose)
        {
            Log.i(CLASS_ID, "Data obtained pushed to read buffer: " + new String(src));
        }else
        {
            Log.i(CLASS_ID, "Data obtained pushed to read buffer: " + new String(src));
            Log.i(CLASS_ID, "Raw data pushed to read buffer: " + HexData.hexToString(src));
            Log.i(CLASS_ID, "Number of bytes pushed from read buffer: " + src.length);
        }
    }



}
