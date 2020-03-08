
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

import java.io.OutputStream;
import java.util.Arrays;

public class SerialOutputStream extends OutputStream
{
    protected final UsbSerialInterface device;

    public SerialOutputStream(UsbSerialInterface device)
    {
        this.device = device;
    }

    @Override
    public void write(int b)
    {
        device.write(new byte[] { (byte)b });
    }

    @Override
    public void write(byte[] b)
    {
        device.write(b);
    }
}
