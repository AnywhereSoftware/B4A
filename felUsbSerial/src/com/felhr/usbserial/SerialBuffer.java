
/*
 * The MIT License (MIT)
 * Copyright (c) 2014 Felipe Herranz
 */
 
 package com.felhr.usbserial;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.objects.usb.felUsbSerial;

public class SerialBuffer
{
   
    private ByteBuffer readBuffer;
    private SynchronizedBuffer writeBuffer;
    private byte[] readBuffer_compatible; // Read buffer for android < 4.2
    private boolean debugging = false;

    public SerialBuffer(boolean version)
    {
    	BA.Log("Buffer size: " + felUsbSerial.BUFFER_READ_SIZE);
        writeBuffer = new SynchronizedBuffer();
        if(version)
        {
            readBuffer = ByteBuffer.allocate(felUsbSerial.BUFFER_READ_SIZE);

        }else
        {
            readBuffer_compatible = new byte[felUsbSerial.BUFFER_READ_SIZE];
        }
    }

    /*
     * Print debug messages
     */
    public void debug(boolean value)
    {
        debugging = value;
    }

    public void putReadBuffer(ByteBuffer data)
    {
        synchronized(this)
        {
            try
            {
                readBuffer.put(data);
            }catch(BufferOverflowException e)
            {
                // TO-DO
            }
        }
    }

    public ByteBuffer getReadBuffer()
    {
        synchronized(this)
        {
            return readBuffer;
        }
    }


    public byte[] getDataReceived()
    {
        synchronized(this)
        {
            byte[] dst = new byte[readBuffer.position()];
            readBuffer.position(0);
            readBuffer.get(dst, 0, dst.length);
            if(debugging)
                UsbSerialDebugger.printReadLogGet(dst, true);
            return dst;
        }
    }

    public void clearReadBuffer()
    {
        synchronized(this)
        {
            readBuffer.clear();
        }
    }

    public byte[] getWriteBuffer()
    {
        return writeBuffer.get();
    }

    public void putWriteBuffer(byte[]data)
    {
        writeBuffer.put(data);
    }


    public void resetWriteBuffer()
    {
        writeBuffer.reset();
    }

    public byte[] getBufferCompatible()
    {
        return readBuffer_compatible;
    }

    public byte[] getDataReceivedCompatible(int numberBytes)
    {
        byte[] tempBuff = Arrays.copyOfRange(readBuffer_compatible, 0, numberBytes);
        return tempBuff;
    }

    private class SynchronizedBuffer
    {
        private byte[] buffer;
        private int position;

        public SynchronizedBuffer()
        {
            this.buffer = new byte[felUsbSerial.BUFFER_WRITE_SIZE];
            position = -1;
        }

        public synchronized void put(byte[] src)
        {
            if(position == -1)
                position = 0;
            if(debugging)
                UsbSerialDebugger.printLogPut(src, true);
            if(position + src.length > felUsbSerial.BUFFER_WRITE_SIZE - 1) //Checking bounds. Source data does not fit in buffer
            {
                if(position < felUsbSerial.BUFFER_WRITE_SIZE)
                    System.arraycopy(src, 0, buffer, position, felUsbSerial.BUFFER_WRITE_SIZE - position);
                position = felUsbSerial.BUFFER_WRITE_SIZE;
                notify();
            }else // Source data fits in buffer
            {
                System.arraycopy(src, 0, buffer, position, src.length);
                position += src.length;
                notify();
            }
        }

        public synchronized byte[] get()
        {
            if(position == -1)
            {
                try
                {
                    wait();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            byte[] dst =  Arrays.copyOfRange(buffer, 0, position);
            if(debugging)
                UsbSerialDebugger.printLogGet(dst, true);
            position = -1;
            return dst;
        }

        public synchronized void reset()
        {
            position = -1;
        }
    }

}
