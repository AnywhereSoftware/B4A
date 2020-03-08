
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

public class XdcVcpIds
{
	/*
	 * Werner Wolfrum (w.wolfrum@wolfrum-elektronik.de)
	 */

    /* Different products and vendors of XdcVcp family
    */
    private static final ConcreteDevice[] xdcvcpDevices = new ConcreteDevice[]
            {
                    new ConcreteDevice(0x264D, 0x0232), // VCP (Virtual Com Port)
                    new ConcreteDevice(0x264D, 0x0120)  // USI (Universal Sensor Interface)
            };

    public static boolean isDeviceSupported(int vendorId, int productId)
    {
        for(int i=0;i<=xdcvcpDevices.length-1;i++)
        {
            if(xdcvcpDevices[i].vendorId == vendorId && xdcvcpDevices[i].productId == productId )
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
