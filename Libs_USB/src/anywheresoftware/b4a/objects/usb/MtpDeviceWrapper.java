
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
 
 package anywheresoftware.b4a.objects.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.mtp.MtpStorageInfo;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.usb.UsbManagerWrapper.UsbDeviceWrapper;
@ShortName("MtpDevice")
public class MtpDeviceWrapper extends AbsObjectWrapper<MtpDevice>{
	public void Initialize(String EventName, UsbDevice UsbDevice) {
		MtpDevice m = new MtpDevice(UsbDevice);
		setObject(m);
	}
	public void Open(UsbDeviceConnectionWrapper Connection) {
		getObject().open(Connection.connection);
	}
//	public void test() {
//		int[] ids = getObject().getStorageIds();
//		for(int id : ids) {
//			MtpStorageInfo m = getObject().getStorageInfo(id);
//			Common.Log(m.getDescription() + " " + m.getVolumeIdentifier() + " " + m.getFreeSpace());
//			int[] handles = getObject().getObjectHandles(id, 0, 0);
//			for (int h : handles) {
//				MtpObjectInfo mi = getObject().getObjectInfo(h);
//				Common.Log(mi.getName() + ": " + mi.getCompressedSize() + ", " + mi.getFormat());
//			}
//		}
//	}
	public void Close() {
		if (IsInitialized())
			getObject().close();
	}
}
