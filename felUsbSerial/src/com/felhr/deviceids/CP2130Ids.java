
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
 
 package com.felhr.deviceids;

public class CP2130Ids
{
    private static final ConcreteDevice[] cp2130Devices = new ConcreteDevice[]{
            new ConcreteDevice(0x10C4, 0x87a0),
    };

    public static boolean isDeviceSupported(int vendorId, int productId)
    {
        for(int i=0;i<=cp2130Devices.length-1;i++)
        {
            if(cp2130Devices[i].vendorId == vendorId && cp2130Devices[i].productId == productId )
            {
                return true;
            }
        }
        return false;
    }

    private static class ConcreteDevice
    {
        public int vendorId;
        public int productId;

        public ConcreteDevice(int vendorId, int productId)
        {
            this.vendorId = vendorId;
            this.productId = productId;
        }
    }
}
