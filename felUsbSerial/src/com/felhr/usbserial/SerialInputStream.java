
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

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

public class SerialInputStream extends InputStream implements UsbSerialInterface.UsbReadCallback
{
    protected final UsbSerialInterface device;
    protected ArrayBlockingQueue data = new ArrayBlockingQueue<Integer>(256);
    protected volatile boolean is_open;

    public SerialInputStream(UsbSerialInterface device)
    {
        this.device = device;
        is_open = true;
        device.read(this);
    }

    @Override
    public int read()
    {
        while (is_open)
        {
            try
            {
                return (Integer)data.take();
            } catch (InterruptedException e)
            {
                // ignore, will be retried by while loop
            }
        }
        return -1;
    }

    public void close()
    {
        is_open = false;
        try
        {
            data.put(-1);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void onReceivedData(byte[] new_data)
    {
        for (byte b : new_data)
        {
            try
            {
                data.put(((int)b) & 0xff);
            } catch (InterruptedException e)
            {
                // ignore, possibly losing bytes when buffer is full
            }
        }
    }
}
