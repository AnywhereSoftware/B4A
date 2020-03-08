
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
 
 package anywheresoftware.b4a;
public interface B4AClass
{
	BA getBA();
	BA getActivityBA();
	boolean IsInitialized();

	public static abstract class ImplB4AClass implements B4AClass{
		public BA ba;
		protected ImplB4AClass mostCurrent;

		public BA getBA() {
			return ba;
		}
		/**
		 * Tries to return an ActivityBA if such is available
		 */
		public BA getActivityBA() {
			BA aba = null;
			if (ba.sharedProcessBA.activityBA != null) //will be null for services
				aba = ba.sharedProcessBA.activityBA.get();
			if (aba == null)
				aba = ba;
			return aba;

		}
		@Override
		public String toString() {
			return BA.TypeToString(this, true);
		}
		public boolean IsInitialized() {
			return ba != null;    
		}
		
	}
}
