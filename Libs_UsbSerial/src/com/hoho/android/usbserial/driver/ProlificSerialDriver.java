
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
 
 package com.hoho.android.usbserial.driver;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

public class ProlificSerialDriver extends CommonUsbSerialDriver
{
    private final String TAG = CdcAcmSerialDriver.class.getSimpleName();

    private UsbInterface mInterface;

    private UsbEndpoint mIntEndpoint;
    private UsbEndpoint mReadEndpoint;
    private UsbEndpoint mWriteEndpoint;

    private int mBaudRate;
    private int mDataBits;
    private int mStopBits;
    private int mParity;

    private boolean mRts = false;
    private boolean mDtr = false;

    private static final int USB_RECIP_INTERFACE = 0x01;
    private static final int USB_RT_ACM = UsbConstants.USB_TYPE_CLASS | USB_RECIP_INTERFACE;

    private static final int SET_LINE_CODING = 0x20;  // USB CDC 1.1 section 6.2
    private static final int GET_LINE_CODING = 0x21;
    private static final int SET_CONTROL_LINE_STATE = 0x22;
    private static final int SEND_BREAK = 0x23;
    
    private static final int TIME_OUT = 200;

    public ProlificSerialDriver(UsbDevice device, UsbDeviceConnection connection) {
        super(device, connection);
    }

    @Override
    public void open() throws IOException {
        Log.d(TAG, "claiming interfaces, count=" + mDevice.getInterfaceCount());

        Log.d(TAG, "Claiming interface.");
        mInterface = mDevice.getInterface(0);
        Log.d(TAG, "Control iface=" + mInterface);
        // class should be USB_CLASS_COMM   // class should be USB_CLASS_CDC_DATA

        if (!mConnection.claimInterface(mInterface, true)) {
            throw new IOException("Could not claim interface.");
        }
        mIntEndpoint = mInterface.getEndpoint(0);
        Log.d(TAG, "Read endpoint direction: " + mIntEndpoint.getDirection());
        mWriteEndpoint = mInterface.getEndpoint(1);
        Log.d(TAG, "Write endpoint direction: " + mWriteEndpoint.getDirection());
        mReadEndpoint = mInterface.getEndpoint(2);
        Log.d(TAG, "Read endpoint direction: " + mReadEndpoint.getDirection());

    	// some undocumented magic initialisation as found in Linux driver code
		byte[] rawdescs = mConnection.getRawDescriptors();
		int chiptype = 0;
		if (rawdescs[4] == 0x02)  // bDeviceClass
		{
			chiptype = 0;
		}
		else if (rawdescs[7] == 0x40) //bMaxPacketSize0
		{
			chiptype = 2; // type HX
			// bit of unknown magic Linux code says "reset upstream data pipes"
			vendorWrite(8, 0);
			vendorWrite(9, 0);
		}
		else if (rawdescs[4] == 0x00) // bDeviceClass
		{
			chiptype = 1;
		}			
		else if (rawdescs[4] == 0xff) // bDeviceClass
		{
			chiptype = 1;
		}
			
		byte[] buf = new byte[1];
		vendorRead(0x8484, 0, buf)	;
		vendorWrite(0x0404, 0);	
		vendorRead(0x8484, 0, buf);	
		vendorRead(0x8383, 0, buf);	
		vendorRead(0x8484, 0, buf);	
		vendorWrite(0x0404, 1);	
		vendorRead(0x8484, 0, buf);	
		vendorRead(0x8383, 0, buf);	
		vendorWrite(0, 1);	
		vendorWrite(1, 0);	
		if (chiptype == 2)
				vendorWrite(2, 0x44);	
		else	
				vendorWrite(2, 0x24);	
		// end of undocumented magic

        
        
        
        
        Log.d(TAG, "Setting line coding to 115200/8N1");
        mBaudRate = 115200;
        mDataBits = DATABITS_8;
        mParity = PARITY_NONE;
        mStopBits = STOPBITS_1;
        setParameters(mBaudRate, mDataBits, mStopBits, mParity);
    }

    private int sendProlificControlMessage(int request, int value, byte[] buf) {
        return mConnection.controlTransfer(
                USB_RT_ACM, request, value, 0, buf, buf != null ? buf.length : 0, 5000);
    }
    
    private void vendorRead(int value, int index, byte[] buf)
    {
    	mConnection.controlTransfer(0xc0, 0x01, value, index, buf, 1, TIME_OUT);
    }
    
    private void vendorWrite(int value, int index)
    {
    	mConnection.controlTransfer(0x40, 0x01, value, index, null, 0, TIME_OUT);
    }
    

    @Override
    public void close() throws IOException {
        mConnection.close();
    }

    @Override
    public int read(byte[] dest, int timeoutMillis) throws IOException {
        final int numBytesRead;
        synchronized (mReadBufferLock) {
            int readAmt = Math.min(dest.length, mReadBuffer.length);
            numBytesRead = mConnection.bulkTransfer(mReadEndpoint, mReadBuffer, readAmt,
                    timeoutMillis);
            if (numBytesRead < 0) {
                // This sucks: we get -1 on timeout, not 0 as preferred.
                // We *should* use UsbRequest, except it has a bug/api oversight
                // where there is no way to determine the number of bytes read
                // in response :\ -- http://b.android.com/28023
                return 0;
            }
            System.arraycopy(mReadBuffer, 0, dest, 0, numBytesRead);
        }
        return numBytesRead;
    }

    @Override
    public int write(byte[] src, int timeoutMillis) throws IOException {
        // TODO(mikey): Nearly identical to FtdiSerial write. Refactor.
        int offset = 0;

        while (offset < src.length) {
            final int writeLength;
            final int amtWritten;

            synchronized (mWriteBufferLock) {
                final byte[] writeBuffer;

                writeLength = Math.min(src.length - offset, mWriteBuffer.length);
                if (offset == 0) {
                    writeBuffer = src;
                } else {
                    // bulkTransfer does not support offsets, make a copy.
                    System.arraycopy(src, offset, mWriteBuffer, 0, writeLength);
                    writeBuffer = mWriteBuffer;
                }

                amtWritten = mConnection.bulkTransfer(mWriteEndpoint, writeBuffer, writeLength,
                        timeoutMillis);
            }
            if (amtWritten <= 0) {
               throw new IOException("Error writing " + writeLength
                       + " bytes at offset " + offset + " length=" + src.length);
            }
            Log.d(TAG, "Wrote amt=" + amtWritten + " attempted=" + writeLength);
            offset += amtWritten;
        }
        return offset;
    }

    @Deprecated
    @Override
    public int setBaudRate(int baudRate) throws IOException {
        mBaudRate = baudRate;
        setParameters(mBaudRate, mDataBits, mStopBits, mParity);
        return mBaudRate;
    }

    @Override
    public void setParameters(int baudRate, int dataBits, int stopBits, int parity) {
        byte stopBitsByte;
        switch (stopBits) {
            case STOPBITS_1: stopBitsByte = 0; break;
            case STOPBITS_1_5: stopBitsByte = 1; break;
            case STOPBITS_2: stopBitsByte = 2; break;
            default: throw new IllegalArgumentException("Bad value for stopBits: " + stopBits);
        }

        byte parityBitesByte;
        switch (parity) {
            case PARITY_NONE: parityBitesByte = 0; break;
            case PARITY_ODD: parityBitesByte = 1; break;
            case PARITY_EVEN: parityBitesByte = 2; break;
            case PARITY_MARK: parityBitesByte = 3; break;
            case PARITY_SPACE: parityBitesByte = 4; break;
            default: throw new IllegalArgumentException("Bad value for parity: " + parity);
        }

        byte[] msg = {
                (byte) ( baudRate & 0xff),
                (byte) ((baudRate >> 8 ) & 0xff),
                (byte) ((baudRate >> 16) & 0xff),
                (byte) ((baudRate >> 24) & 0xff),
                stopBitsByte,
                parityBitesByte,
                (byte) dataBits};
        sendProlificControlMessage(SET_LINE_CODING, 0, msg);
    }

    @Override
    public boolean getCD() throws IOException {
        return false;  // TODO
    }

    @Override
    public boolean getCTS() throws IOException {
        return false;  // TODO
    }

    @Override
    public boolean getDSR() throws IOException {
        return false;  // TODO
    }

    @Override
    public boolean getDTR() throws IOException {
        return mDtr;
    }

    @Override
    public void setDTR(boolean value) throws IOException {
        mDtr = value;
        setDtrRts();
    }

    @Override
    public boolean getRI() throws IOException {
        return false;  // TODO
    }

    @Override
    public boolean getRTS() throws IOException {
        return mRts;
    }

    @Override
    public void setRTS(boolean value) throws IOException {
        mRts = value;
        setDtrRts();
    }

    private void setDtrRts() {
        int value = (mRts ? 0x2 : 0) | (mDtr ? 0x1 : 0);
        sendProlificControlMessage(SET_CONTROL_LINE_STATE, value, null);
    }

    public static Map<Integer, int[]> getSupportedDevices() {
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<Integer, int[]>();
        supportedDevices.put(Integer.valueOf(UsbId.VENDOR_PROLIFIC),
                new int[] {
                        UsbId.PROLIFIC_PL2303
                });        
        if (UsbId.DRIVER_CUSTOM == UsbId.DRIVER_PROLIFIC) // *ADDED conditional
        {
        	supportedDevices.put(Integer.valueOf(UsbId.VENDOR_CUSTOM),
                new int[] {
                        UsbId.PRODUCT_CUSTOM
                });
        }
        return supportedDevices;
    }


}
