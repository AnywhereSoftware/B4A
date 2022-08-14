package anywheresoftware.b4a.objects.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map.Entry;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.B4aDebuggable;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.ShortName;

/**
 * A collection that holds pairs of keys and values. The keys are unique. Which means that if you add a key/value pair (entry) and
 *the collection already holds an entry with the same key, the previous entry will be removed from the map.
 *Fetching an item is done by looking for its key. This is usually a very fast operation (O(1) compared to O(n) in a list).
 *The key should be a string or a number. The value can be any type of object.
 *Note that this map implementation does return items in the same order as they were added.
 *Usually you will use Put to add items and Get or GetDefault to get the values based on the key.
 *If you need to iterate over all the items you can use a For loop like:<code>
 *For Each Key As String In Map.Keys
 * Dim Value As Object = Map.Get(Key)
 *Next</code>
 *Similar to a list, a map can hold any object, however if it is a process global variable then it cannot hold activity objects (like views).
 *Maps are very useful for storing applications settings. 
 *You can save and load maps with "simple values" with File.WriteMap and File.ReadMap. More complex maps can be saved with B4XSerializator.
 */
@SuppressWarnings("rawtypes")
@ShortName("Map")
public class Map extends AbsObjectWrapper<java.util.Map> implements B4aDebuggable{

	/**
	 *Initializes the object.
	 *Example:<code>
	 *Dim Map1 As Map
	 *Map1.Initialize</code>
	 */
	public void Initialize() {
		setObject(new MyMap());
	}
	/**
	 *Puts a key/value pair in the map, overwriting the previous item with this key (if such exists).
	 *Returns the previous item with this key or null if there was no such item.
	 *Note that if you are using strings as the keys then the keys are case sensitive.
	 *Example:<code>
	 *Map1.Put("Key", "Value")</code>
	 */
	public Object Put(Object Key, Object Value) {
		return getObject().put(Key, Value);
	}
	/**
	 * Removes the item with the given key, if such exists.
	 *Returns the item removed or null if no matching item was found.
	 */
	public Object Remove(Object Key) {
		return getObject().remove(Key);
	}
	/**
	 *Returns the value of the item with the given key.
	 *Returns Null if the value doesn't exist.
	 */
	public Object Get(Object Key) {
		return getObject().get(Key);
	}
	/**
	 * Returns the value of the item with the given key. If no such item exists the specified default value is returned.
	 */
	public Object GetDefault(Object Key, Object Default) {
		Object res = getObject().get(Key);
		if (res == null)
			return Default;
		return res;
	}
	/**
	 * Clears all items from the map.
	 */
	public void Clear() {
		getObject().clear();
	}
	/**
	 *<b>This method is deprecated. Use For Each to iterate over the keys or use B4XOrderedMap.</b>
	 * Returns the key of the item at the given index.
	 *GetKeyAt and GetValueAt should be used to iterate over all the items.
	 *These methods are optimized for iterating over the items in ascending order.
	 *Example:<code>
	 *For i = 0 to Map.Size - 1
	 *	Log("Key: " & Map.GetKeyAt(i))
	 *	Log("Value: " & Map.GetValueAt(i))
	 *Next</code>
	 */
	public Object GetKeyAt(int Index) {
		java.util.Map<Object, Object> m = getObject();
		if (m instanceof MyMap)
			return ((MyMap)m).getKey(Index);
		else
			throw new RuntimeException("method not supported. Use For Each instead.");
	}
	/**
	 *<b>This method is deprecated. Use For Each to iterate over the values or use B4XOrderedMap.</b>
	 * Returns the value of the item at the given index.
	 *GetKeyAt and GetValueAt should be used to iterate over all the items.
	 *These methods are optimized for iterating over the items in ascending order.
	 *Example:<code>
	 *For i = 0 to Map.Size - 1
	 *	Log("Key: " & Map.GetKeyAt(i))
	 *	Log("Value: " & Map.GetValueAt(i))
	 *Next</code>
	 */
	public Object GetValueAt(int Index) {
		java.util.Map<Object, Object> m = getObject();
		if (m instanceof MyMap)
			return ((MyMap)m).getValue(Index);
		else
			throw new RuntimeException("method not supported. Use For Each instead.");
	}
	/**
	 * Returns the number of items stored in the map.
	 */
	public int getSize() {
		return getObject().size();
	}
	/**
	 * Tests whether there is an item with the given key.
	 *Example:<code>
	 *If Map.ContainsKey("some key") Then ... </code>
	 */
	public boolean ContainsKey(Object Key) {
		return getObject().containsKey(Key);
	}
	/**
	 * Returns an object which can be used to iterate over all the keys with a For Each block.
	 *Example:<code>
	 *For Each k As String In map1.Keys
	 *	Log(k)
	 *Next</code>
	 */
	public IterableList Keys()
	{
		return new IterableList() {

			@Override
			public Object Get(int index) {
				return Map.this.GetKeyAt(index);
			}

			@Override
			public int getSize() {
				return Map.this.getSize();
			}
			
		};
	}
	/**
	 * Returns an object which can be used to iterate over all the values with a For Each block.
	 *Example:<code>
	 *For Each v As Int In map1.Values
	 *	Log(v)
	 *Next</code>
	 */
	public IterableList Values()
	{
		return new IterableList() {

			@Override
			public Object Get(int index) {
				return Map.this.GetValueAt(index);
			}

			@Override
			public int getSize() {
				return Map.this.getSize();
			}
			
		};
	}
	
	@Hide
	@Override
	public Object[] debug(int limit, boolean[] outShouldAddReflectionFields) {
		Object[] res = new Object[2 * (1 + Math.min(getSize(), limit))];
		res[0] = "Size";
		res[1] = getSize();
		int i = 2;
		for (Entry<Object, Object> e : ((java.util.Map<Object, Object>)getObject()).entrySet()) {
			if (i >= res.length - 1)
				break;
			res[i] = String.valueOf(e.getKey());
			if (res[i].toString().length() == 0)
				res[i] = "(empty string)";
			res[i + 1] = e.getValue();
			i += 2;
		}
		outShouldAddReflectionFields[0] = false;
		return res;
	}
	@Hide
	public static class MyMap implements java.util.Map<Object, Object> {
		private LinkedHashMap<Object, Object> innerMap = new LinkedHashMap<Object, Object>();
		private int iteratorPosition;
		private Iterator<Entry<Object, Object>> iterator;
		private Entry<Object, Object> currentEntry;
		public Object getKey(int index) {
			return getEntry(index).getKey();
		}
		public Object getValue(int index) {
			return getEntry(index).getValue();
		}
		private Entry<Object, Object> getEntry(int index) {
			if (iterator != null) {
				if (iteratorPosition == index) {
					//don't do anything
				}
				else if (iteratorPosition == index - 1) { //one before current
					currentEntry = iterator.next();
					iteratorPosition++;
				}
				else
					iterator = null; //enter next if
			}
			if (iterator == null) {
				iterator = innerMap.entrySet().iterator();
				for (int i = 0;i <= index;i++) {
					currentEntry = iterator.next();
				}
				iteratorPosition = index;
			}
			return currentEntry;
		}
		@Override
		public void clear() {
			iterator = null;
			innerMap.clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return innerMap.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return innerMap.containsValue(value);
		}

		@Override
		public Set<java.util.Map.Entry<Object, Object>> entrySet() {
			return innerMap.entrySet();
		}

		@Override
		public Object get(Object key) {
			return innerMap.get(key);
		}

		@Override
		public boolean isEmpty() {
			return innerMap.isEmpty();
		}

		@Override
		public Set<Object> keySet() {
			return innerMap.keySet();
		}

		@Override
		public Object put(Object key, Object value) {
			iterator = null;
			return innerMap.put(key, value);
		}

		@Override
		public void putAll(java.util.Map<? extends Object, ? extends Object> m) {
			iterator = null;
			innerMap.putAll(m);
		}

		@Override
		public Object remove(Object key) {
			iterator = null;
			return innerMap.remove(key);
		}

		@Override
		public int size() {
			return innerMap.size();
		}

		@Override
		public Collection<Object> values() {
			return innerMap.values();
		}
		@Override
		public String toString() {
			return innerMap.toString();
		}
		
	}
	

	
}

