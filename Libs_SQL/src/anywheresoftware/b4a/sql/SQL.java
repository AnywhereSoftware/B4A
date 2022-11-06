
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
 
 package anywheresoftware.b4a.sql;

import java.io.File;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
/**
 * The main object that accesses the database.
 */
@ShortName("SQL")
@Events(values={"QueryComplete (Success As Boolean, Result As ResultSet)",
		"NonQueryComplete (Success As Boolean)"})

@Version(1.50f)
public class SQL implements CheckForReinitialize{
	private SQLiteDatabase db;
	private volatile ArrayList<Object[]> nonQueryStatementsList = new ArrayList<Object[]>();
	
	private static SQL cloneMe(SQL sql) {
		SQL ret = new SQL();
		ret.db = sql.db;
		ret.nonQueryStatementsList = sql.nonQueryStatementsList;
		return ret;
	}
	/**
	 * Opens the database file. A new database will be created if it does not exist and CreateIfNecessary is true.
	 *IMPORTANT: this object should be declared in Sub Process_Globals.
	 *Example:<code>
	 *Dim SQL1 As SQL
	 *SQL1.Initialize(File.DirInternal, "MyDb.db", True)</code>
	 */
	public void Initialize(String Dir, String FileName, boolean CreateIfNecessary) {
		db = SQLiteDatabase.openDatabase(new File(Dir, FileName).toString(), null, 
				(CreateIfNecessary ? SQLiteDatabase.CREATE_IF_NECESSARY : 0) | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}
	/**
	 * The SQL library allows you to create and manage SQL databases.
	 *See the <link>SQL tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/6736-sql-tutorial.html</link> for more information.
	 */
	public static void LIBRARY_DOC() {
		
	}
	private void checkNull() {
		if (db == null)
			throw new RuntimeException("Object should first be initialized.");
	}
	/**
	 * Tests whether the database is initialized and opened.
	 */
	public boolean IsInitialized() {
		if (db == null)
			return false;
		return db.isOpen();
	}
	/**
	 * Executes a single non query SQL statement.
	 *Example:<code>
	 *SQL1.ExecNonQuery("CREATE TABLE table1 (col1 TEXT , col2 INTEGER, col3 INTEGER)")</code>
	 *If you plan to do many "writing" queries one after another, then you should consider using BeginTransaction / EndTransaction.
	 *It will execute significantly faster.
	 */
	public void ExecNonQuery(String Statement) {
		checkNull();
		db.execSQL(Statement);
	}
	/**
	 * Executes a single non query SQL statement.
	 *The statement can include question marks which will be replaced by the items in the given list.
	 *Note that Basic4android converts arrays to lists implicitly.
	 *The values in the list should be strings, numbers or bytes arrays.
	 *Example:<code>
	 *SQL1.ExecNonQuery2("INSERT INTO table1 VALUES (?, ?, 0)", Array As Object("some text", 2))</code>
	 */
	public void ExecNonQuery2(String Statement, List Args) {
		SQLiteStatement s = db.compileStatement(Statement);
		try {
			int numArgs = Args.IsInitialized() == false ? 0 : Args.getSize();
			for (int i = 0; i < numArgs; i++) {
				DatabaseUtils.bindObjectToProgram(s, i + 1, Args.Get(i));
			}
			s.execute();
		} finally {
			s.close();
		}
	}
	/**
	 * Adds a non-query statement to the batch of statements.
	 *The statements are (asynchronously) executed when you call ExecNonQueryBatch.
	 *Args parameter can be Null if it is not needed.
	 *Example:<code>
	 *For i = 1 To 1000
	 *	sql.AddNonQueryToBatch("INSERT INTO table1 VALUES (?)", Array(Rnd(0, 100000)))
	 *Next
	 *Dim SenderFilter As Object = sql.ExecNonQueryBatch("SQL")
	 *Wait For (SenderFilter) SQL_NonQueryComplete (Success As Boolean)
	 *Log("NonQuery: " & Success)</code>
	 */
	public void AddNonQueryToBatch(String Statement, List Args) {
		nonQueryStatementsList.add(new Object[] {Statement, Args});
	}
	/**
	 * Asynchronously executes a batch of non-query statements (such as INSERT).
	 *The NonQueryComplete event is raised after the statements are completed.
	 *You should call AddNonQueryToBatch one or more times before calling this method to add statements to the batch.
	 *Note that this method internally begins and ends a transaction.
	 *Returns an object that can be used as the sender filter for Wait For calls.
	 *Example:<code>
	 *For i = 1 To 1000
	 *	sql.AddNonQueryToBatch("INSERT INTO table1 VALUES (?)", Array(Rnd(0, 100000)))
	 *Next
	 *Dim SenderFilter As Object = sql.ExecNonQueryBatch("SQL")
	 *Wait For (SenderFilter) SQL_NonQueryComplete (Success As Boolean)
	 *Log("NonQuery: " & Success)</code>
	 */
	public Object ExecNonQueryBatch(final BA ba, final String EventName) {
		final ArrayList<Object[]> myList = nonQueryStatementsList;
		nonQueryStatementsList = new ArrayList<Object[]>();
		final SQL ret = SQL.cloneMe(this);
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				synchronized (db) {
					try {
						BeginTransaction();
						for (Object[] o: myList) {
							ExecNonQuery2((String)o[0], (List)o[1]);
						}
						TransactionSuccessful();
						EndTransaction();
						ba.raiseEventFromDifferentThread(ret, SQL.this, 0, EventName.toLowerCase(BA.cul) + "_nonquerycomplete",
								true, new Object[] {true});
					} catch (Exception e) {
						EndTransaction();
						e.printStackTrace();
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(ret, SQL.this, 0, EventName.toLowerCase(BA.cul) + "_nonquerycomplete",
								true, new Object[] {false});
					}
				}
			}
			
		}, this, 1);
		return ret;
	}
	
	/**
	 * Asynchronously executes the given query. The QueryComplete event will be raised when the results are ready.
	 *Note that ResultSet extends Cursor. You can use Cursor if preferred.
	 *Returns an object that can be used as the sender filter for Wait For calls.
	 *Example:<code>
	 *Dim SenderFilter As Object = sql.ExecQueryAsync("SQL", "SELECT * FROM table1", Null)
	 *Wait For (SenderFilter) SQL_QueryComplete (Success As Boolean, rs As ResultSet)
	 *If Success Then
	 *	Do While rs.NextRow
	 *		Log(rs.GetInt2(0))
	 *	Loop
	 *	rs.Close
	 *Else
	 *	Log(LastException)
	 *End If</code>
	 */
	public Object ExecQueryAsync(final BA ba, final String EventName, final String Query, final List Args) {
		final SQL ret = SQL.cloneMe(this);
		ba.submitRunnable(new Runnable() {

			@Override
			public void run() {
				synchronized (db) {
					try {
						String[] s = null;
						if (Args != null && Args.IsInitialized()) {
							s = new String[Args.getSize()];
							for (int i = 0;i < s.length;i++) {
								Object o = Args.Get(i);
								s[i] = o == null ? null : String.valueOf(o);
							}
						}
						Cursor c = ExecQuery2(Query, s);
						ba.raiseEventFromDifferentThread(ret, SQL.this, 0, EventName.toLowerCase(BA.cul) + "_querycomplete",
								true, new Object[] {true, AbsObjectWrapper.ConvertToWrapper(new ResultSetWrapper(), c)});
					} catch (Exception e) {
						e.printStackTrace();
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(ret, SQL.this, 0, EventName.toLowerCase(BA.cul) + "_querycomplete",
								true, new Object[] {false, AbsObjectWrapper.ConvertToWrapper(new ResultSetWrapper(), null)});
					}
				}
			}
			
		}, this, 0);
		return ret;
	}
	
	/**
	 * Executes the query and returns a cursor which is used to go over the results.
	 *Example:<code>
	 *Dim Cursor As Cursor
	 *Cursor = SQL1.ExecQuery("SELECT col1, col2 FROM table1")
	 *For i = 0 To Cursor.RowCount - 1
	 *	Cursor.Position = i
	 *	Log(Cursor.GetString("col1"))
	 *	Log(Cursor.GetInt("col2"))
	 *Next</code>
	 */
	public Cursor ExecQuery(String Query) {
		checkNull();
		return ExecQuery2(Query, null);
	}
	/**
	 * Executes the query and returns a cursor which is used to go over the results.
	 *The query can include question marks which will be replaced with the values in the array.
	 *Example:<code>
	 *Dim Cursor As Cursor
	 *Cursor = sql1.ExecQuery2("SELECT col1 FROM table1 WHERE col3 = ?", Array As String(22))</code>
	 *SQLite will try to convert the string values based on the columns types.
	 */
	public Cursor ExecQuery2(String Query, String[] StringArgs) {
		checkNull();
		return db.rawQuery(Query, StringArgs);
	}
	
	/**
	 * Executes the query and returns the value in the first column and the first row (in the result set).
	 *Returns Null if no results were found.
	 *Example:<code>
	 *Dim NumberOfMatches As Int
	 *NumberOfMatches = SQL1.ExecQuerySingleResult("SELECT count(*) FROM table1 WHERE col2 > 300")</code>
	 */
	public String ExecQuerySingleResult(String Query) {
		return ExecQuerySingleResult2(Query, null);
	}
	/**
	 * Executes the query and returns the value in the first column and the first row (in the result set).
	 *Returns Null if no results were found.
	 *Example:<code>
	 *Dim NumberOfMatches As Int
	 *NumberOfMatches = SQL1.ExecQuerySingleResult2("SELECT count(*) FROM table1 WHERE col2 > ?", Array As String(300))</code>
	 */
	public String ExecQuerySingleResult2(String Query, String[] StringArgs) {
		checkNull();
		Cursor cursor = db.rawQuery(Query, StringArgs);
		try {
			if (!cursor.moveToFirst())
				return null;
			if (cursor.getColumnCount() == 0)
				return null;
			return cursor.getString(0);
		} finally {
			cursor.close();
		}
	}
	/**
	 * Begins a transaction. A transaction is a set of multiple "writing" statements that are atomically committed,
	 *hence all changes will be made or no changes will be made.
	 *As a side effect those statements will be executed significantly faster (in the default case a transaction is implicitly created for
	 *each statement).
	 *It is very important to handle transaction carefully and close them.
	 *The transaction is considered successful only if TransactionSuccessful is called. Otherwise no changes will be made.
	 *Typical usage:<code>
	 *SQL1.BeginTransaction
	 *Try
	 *	'block of statements like:
	 *	For i = 1 to 1000
	 *		SQL1.ExecNonQuery("INSERT INTO table1 VALUES(...)
	 *	Next
	 *	SQL1.TransactionSuccessful
	 *Catch
	 *	Log(LastException.Message) 'no changes will be made
	 *End Try
	 *SQL1.EndTransaction</code>
	 */
	public void BeginTransaction() {
		checkNull();
		db.beginTransaction();
	}
	/**
	 * Marks the transaction as a successful transaction. No further statements should be executed till calling EndTransaction.
	 */
	public void TransactionSuccessful() {
		db.setTransactionSuccessful();
	}
	/**
	 * Ends the transaction.
	 */
	public void EndTransaction() {
		db.endTransaction();
	}
	/**
	 * Closes the database.
	 *Does not do anything if the database is not opened or was closed before.
	 */
	public void Close() {
		if (db != null && db.isOpen())
			db.close();
	}


	@ShortName("Cursor")
	public static class CursorWrapper extends AbsObjectWrapper<Cursor> {
		/**
		 *Gets or sets the current position (row).
		 *Note that the starting position of a cursor returned from a query is -1.
		 *The first valid position is 0.
		 *Example:<code>
		 *Dim Cursor As Cursor
		 *Cursor = SQL1.ExecQuery("SELECT col1, col2 FROM table1")
		 *For i = 0 To Cursor.RowCount - 1
		 *	Cursor.Position = i
		 *	Log(Cursor.GetString("col1"))
		 *	Log(Cursor.GetInt("col2"))
		 *Next
		 *Cursor.Close</code>
		 */
		public int getPosition() {
			return getObject().getPosition();
		}
		public void setPosition(int Value) {
			getObject().moveToPosition(Value);
		}
		/**
		 * Returns the name of the column at the specified index.
		 *The first column index is 0.
		 */
		public String GetColumnName(int Index) {
			return getObject().getColumnName(Index);
		}
		/**
		 * Gets the numbers or rows available in the result set.
		 */
		public int getRowCount() {
			return getObject().getCount();
		}
		/**
		 * Gets the number of columns available in the result set.
		 */
		public int getColumnCount() {
			return getObject().getColumnCount();
		}
		/**
		 * Returns the Int value stored in the column at the given ordinal.
		 *The value will be converted to Int if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetInt2(0))</code>
		 */
		public int GetInt2(int Index) {
			return getObject().getInt(Index);
		}
		/**
		 * Returns the Int value stored in the given column.
		 *The value will be converted to Int if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetInt("col2"))</code>
		 */
		public int GetInt(String ColumnName) {
			return getObject().getInt(getObject().getColumnIndexOrThrow(ColumnName));
		}
		/**
		 * Returns the String value stored in the column at the given ordinal.
		 *The value will be converted to String if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetString2(0))</code>
		 */
		public String GetString2(int Index) {
			return getObject().getString(Index);
		}
		/**
		 * Returns the String value stored in the given column.
		 *The value will be converted to String if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetString("col2"))</code>
		 */
		public String GetString(String ColumnName) {
			return getObject().getString(getObject().getColumnIndexOrThrow(ColumnName));
		}
		/**
		 * Returns the Long value stored in the column at the given ordinal.
		 *The value will be converted to Long if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetLong2(0))</code>
		 */
		public Long GetLong2(int Index) {
			return getObject().getLong(Index);
		}
		/**
		 * Returns the Long value stored in the given column.
		 *The value will be converted to Long if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetLong("col2"))</code>
		 */
		public Long GetLong(String ColumnName) {
			return getObject().getLong(getObject().getColumnIndexOrThrow(ColumnName));
		}
		/**
		 * Returns the Double value stored in the column at the given ordinal.
		 *The value will be converted to Double if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetDouble2(0))</code>
		 */
		public Double GetDouble2(int Index) {
			return getObject().getDouble(Index);
		}
		/**
		 * Returns the Double value stored in the given column.
		 *The value will be converted to Double if it is of different type.
		 *Example:<code>
		 *Log(Cursor.GetDouble("col2"))</code>
		 */
		public Double GetDouble(String ColumnName) {
			return getObject().getDouble(getObject().getColumnIndexOrThrow(ColumnName));
		}
		/**
		 * Returns the blob stored in the given column.
		 *Example:<code>
		 *Dim Buffer() As Byte
		 *Buffer = Cursor.GetBlob("col1")</code>
		 */
		public byte[] GetBlob(String ColumnName) {
			return getObject().getBlob(getObject().getColumnIndexOrThrow(ColumnName));
		}
		/**
		 * Returns the blob stored in the column at the given ordinal.
		 *Example:<code>
		 *Dim Buffer() As Byte
		 *Buffer = Cursor.GetBlob2(0)</code>
		 */
		public byte[] GetBlob2(int Index) {
			return getObject().getBlob(Index);
		}
		/**
		 * Closes the cursor and frees resources.
		 */
		public void Close() {
			getObject().close();
		}

	}
	/**
	 * This type is an extension to the Cursor type.
	 *It adds a single method (NextRow).
	 *Its interface is the same as B4J and B4i ResultSet type.
	 *Note that ResultSet should always be closed when it is no longer needed. 
	 */
	@ShortName("ResultSet")
	public static class ResultSetWrapper extends CursorWrapper {
		/**
		 * Moves the cursor to the next result. Returns false when the cursor reaches the end.
		 *Example:<code>
		 *Do While ResultSet1.NextRow
		 * 'Work with Row
		 *Loop</code>
		 */
		public boolean NextRow() {
			int position = getPosition() + 1;
			if (getRowCount() > position) {
				setPosition(position);
				return true;
			}
			else
				return false;
		}
	}
}


