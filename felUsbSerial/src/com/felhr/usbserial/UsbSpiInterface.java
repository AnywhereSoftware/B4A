
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


public interface UsbSpiInterface
{
    // Clock dividers;
    int DIVIDER_2 = 2;
    int DIVIDER_4 = 4;
    int DIVIDER_8 = 8;
    int DIVIDER_16 = 16;
    int DIVIDER_32 = 32;
    int DIVIDER_64 = 64;
    int DIVIDER_128 = 128;

    // Common SPI operations
    boolean connectSPI();
    void writeMOSI(byte[] buffer);
    void readMISO(int lengthBuffer);
    void writeRead(byte[] buffer, int lenghtRead);
    void setClock(int clockDivider);
    void selectSlave(int nSlave);
    void setMISOCallback(UsbMISOCallback misoCallback);
    void closeSPI();

    // Status information
    int getClockDivider();
    int getSelectedSlave();

    interface UsbMISOCallback
    {
        int onReceivedData(byte[] data);
    }
}
