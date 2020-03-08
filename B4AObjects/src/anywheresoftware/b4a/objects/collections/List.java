package anywheresoftware.b4a.objects.collections;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.IterableList;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.WarningEngine;
/**
 * Lists are similar to dynamic arrays. You can add and remove items from a list and it will change its size accordingly.
 *A list can hold any type of object. However if a list is declared as a process global object it cannot hold activity objects (like views).
 *Basic4android automatically converts regular arrays to lists. So when a List parameter is expected you can pass an array instead.
 *For example:<code>
 *Dim List1 As List
 *List1.Initialize
 *List1.AddAll(Array As Int(1, 2, 3, 4, 5))</code>
 *Use the Get method to get an item from the list.
 *Lists can be saved and loaded from files using File.WriteList and File.ReadList.
 *You can use a For loop to iterate over all the values:<code>
 *For i = 0 To List1.Size - 1
 * Dim number As Int
 * number = List1.Get(i)
 * ...
 *Next</code>
 */
@ShortName("List")
public class List extends AbsObjectWrapper<java.util.List<Object>> implements IterableList{
	/**
	 * Initializes an empty list.
	 */
	public void Initialize() {
		setObject(new java.util.ArrayList<Object>());
	}
	/**
	 * Initializes a list with the given values. This method should be used to convert arrays to lists.
	 *Note that if you pass a list to this method then both objects will share the same list,
	 *and if you pass an array the list will be of a fixed size. Meaning that you cannot later add or remove items.
	 *Example:<code>
	 *Dim List1 As List
	 *List1.Initialize2(Array As Int(1,2,3,4,5))</code>
	 *Example:<code>
	 *Dim List1 As List
	 *Dim SomeArray(10) As String
	 *'Fill array...
	 *List1.Initialize2(SomeArray)</code>
	 */
	public void Initialize2(List Array) {
		setObject(Array.getObject());
	}
	/**
	 * Removes all the items from the list.
	 */
	public void Clear() {
		getObject().clear();
	}
	/**
	 * Adds an item at the end of the list.
	 */
	public void Add(Object item) {
		if (BA.debugMode) {
			if (getObject().size() > 0) {
				Object prev = Get(this.getSize() - 1);
				if (prev != null && prev == item && !(prev instanceof String || prev instanceof Number || prev instanceof Boolean)) {
					BA.LogInfo("Warning: same object added to list multiple times.");
				}
			}
		}
		getObject().add(item);
	}
	/**
	 * Adds all elements in the specified collection to the end of the list.
	 *Note that you can add an array directly.
	 *Example:<code>
	 *List.AddAll(Array As String("value1", "value2"))</code>
	 */
	public void AddAll(List List) {
		getObject().addAll(List.getObject());
	}
	/**
	 * Adds all elements in the specified collection starting at the specified index.
	 */
	public void AddAllAt(int Index, List List) {
		getObject().addAll(Index, List.getObject());
	}
	/**
	 * Removes the item at the specified index.
	 */
	public void RemoveAt(int Index) {
		getObject().remove(Index);
	}
	/**
	 *Inserts the specified element in the specified index, before the current item at that index.
	 *As a result all items with index equal to or larger than the specified index are shifted.
	 */
	public void InsertAt(int Index, Object Item) {
		getObject().add(Index, Item);
	}
	/**
	 * Gets the item in the specified index. The item is not removed from the list.
	 */
	public Object Get(int Index) {
		return getObject().get(Index);
	}
	/**
	 * Replaces the current item in the specified index with the new item.
	 */
	public void Set(int Index, Object Item) {
		getObject().set(Index, Item);
	}
	/**
	 * Returns the number of items in the list.
	 */
	public int getSize() {
		return getObject().size();
	}
	/**
	 * Returns the index of the specified item, or -1 if it was not found.
	 */
	public int IndexOf(Object Item) {
		return getObject().indexOf(Item);
	}
	/**
	 * Sorts the list.
	 *The items must all be numbers or strings.
	 */
	@SuppressWarnings("unchecked")
	public void Sort(boolean Ascending) {
		if (Ascending)
			Collections.sort((java.util.List)getObject());
		else {
			Collections.sort((java.util.List)getObject(), new Comparator<Comparable>() {
				@Override
				public int compare(Comparable o1, Comparable o2) {
					return o2.compareTo(o1);
				}
			});
		}
	}
	/**
	 * Sorts a list with items of user defined type. The list is sorted based on the specified field.
	 *FieldName - The case-sensitive field name that will be used for sorting. Field must contain numbers or strings.
	 *Ascending - Whether to sort ascending or descending.
	 *Example:<code>
	 *Sub Process_Globals
	 *	Type Person(Name As String, Age As Int)
	 *End Sub
	 *
	 *Sub Activity_Create(FirstTime As Boolean)
	 *	Dim Persons As List
	 *	Persons.Initialize
	 *	For i = 1 To 50
	 *		Dim p As Person
	 *		p.Name = "Person" & i
	 *		p.Age = Rnd(0, 121)
	 *		Persons.Add(p)
	 *	Next
	 *	Persons.SortType("Age", True) 'Sort the list based on the Age field.
	 *	For i = 0 To Persons.Size - 1
	 *		Dim p As Person
	 *		p = Persons.Get(i)
	 *		Log(p)
	 *	Next
	 *End Sub</code>
	 */
	public void SortType(String FieldName, final boolean Ascending) throws SecurityException, NoSuchFieldException {
		sortList(FieldName, Ascending, false);
	}
	/**
	 * Similar to SortType. Lexicographically sorts the list, ignoring the characters case.
	 */
	public void SortTypeCaseInsensitive(String FieldName, final boolean Ascending) throws SecurityException, NoSuchFieldException {
		sortList(FieldName, Ascending, true);
	}
	private void sortList(String FieldName, final boolean Ascending,final boolean caseInsensitive) throws SecurityException, NoSuchFieldException {
		if (getSize() == 0)
			return;
		final Object o1 = Get(0);
		final Field f = o1.getClass().getDeclaredField(FieldName);
		f.setAccessible(true);
		Comparator<Object> c = new Comparator<Object>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(Object o1, Object o2) {
				try {
					int cmp;
					if (caseInsensitive)
						cmp = String.valueOf(f.get(o1)).compareToIgnoreCase(String.valueOf(f.get(o2)));
					else
						cmp = ((Comparable)f.get(o1)).compareTo(f.get(o2));
					return  cmp * (Ascending ? 1 : -1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		Collections.sort(getObject(), c);
	}

	/**
	 * Lexicographically sorts the list, ignoring the characters case.
	 *The items must all be numbers or strings.
	 */
	@SuppressWarnings("unchecked")
	public void SortCaseInsensitive(boolean Ascending) {
		if (Ascending) {
			Collections.sort((java.util.List)getObject(), new Comparator<Comparable>() {
				@Override
				public int compare(Comparable o1, Comparable o2) {
					return o1.toString().compareToIgnoreCase(o2.toString());
				}
			});
		}
		else {
			Collections.sort((java.util.List)getObject(), new Comparator<Comparable>() {
				@Override
				public int compare(Comparable o1, Comparable o2) {
					return o2.toString().compareToIgnoreCase(o1.toString());
				}
			});
		}
	}

}
