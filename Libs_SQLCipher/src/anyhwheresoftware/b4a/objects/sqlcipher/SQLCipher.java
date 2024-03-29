
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
 
 package anyhwheresoftware.b4a.objects.sqlcipher;

//don't forget the native libs in the additional folder!!!
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;
import android.content.Context;
import android.database.Cursor;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4a.sql.SQL;

/**
 * SQLCipher type is an extension to SQL which supports encryption.
 *See the <link>tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/14965-android-database-encryption-sqlcipher-library.html</link> for more information.
 */
@ShortName("SQLCipher")
@Version(1.70f)
@DependsOn(values={"android-database-sqlcipher-4.5.4.aar", "sqlite-2.4.0.aar"})
public class SQLCipher extends SQL{
	private SQLiteDatabase db;
	private volatile ArrayList<Object[]> nonQueryStatementsList = new ArrayList<Object[]>();
	
	private static SQLCipher cloneMe(SQLCipher sql) {
		SQLCipher ret = new SQLCipher();
		ret.db = sql.db;
		ret.nonQueryStatementsList = sql.nonQueryStatementsList;
		return ret;
	}
	/**
	 * Opens the database file. A new database will be created if it does not exist and CreateIfNecessary is true.
	 */
	public void Initialize(String Dir, String FileName, boolean CreateIfNecessary, String Password, String Unused) throws IOException, InterruptedException {

		SQLiteDatabase.loadLibs(BA.applicationContext);
		db = SQLiteDatabase.openDatabase(new java.io.File(Dir, FileName).toString(), Password, null, 
				(CreateIfNecessary ? SQLiteDatabase.CREATE_IF_NECESSARY : 0) | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}


	private void checkNull() {
		if (db == null)
			throw new RuntimeException("Object should first be initialized.");
	}
	/**
	 * Tests whether the database is initialized and opened.
	 */
	@Override
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
	@Override
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
	@Override
	public void ExecNonQuery2(String Statement, List Args) {
		checkNull();
		SQLiteStatement s = db.compileStatement(Statement);
		try {
			int numArgs = Args.getSize();
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
	@Override
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
	@Override
	public Object ExecNonQueryBatch(final BA ba, final String EventName) {
		final ArrayList<Object[]> myList = nonQueryStatementsList;
		nonQueryStatementsList = new ArrayList<Object[]>();
		final SQLCipher ret = SQLCipher.cloneMe(this);
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
						ba.raiseEventFromDifferentThread(ret, SQLCipher.this, 0, EventName.toLowerCase(BA.cul) + "_nonquerycomplete",
								true, new Object[] {true});
					} catch (Exception e) {
						EndTransaction();
						e.printStackTrace();
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(ret, SQLCipher.this, 0, EventName.toLowerCase(BA.cul) + "_nonquerycomplete",
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
	@Override
	public Object ExecQueryAsync(final BA ba, final String EventName, final String Query, final List Args) {
		final SQLCipher ret = SQLCipher.cloneMe(this);
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
						ba.raiseEventFromDifferentThread(ret, SQLCipher.this, 0, EventName.toLowerCase(BA.cul) + "_querycomplete",
								true, new Object[] {true, AbsObjectWrapper.ConvertToWrapper(new ResultSetWrapper(), c)});
					} catch (Exception e) {
						e.printStackTrace();
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(ret, SQLCipher.this, 0, EventName.toLowerCase(BA.cul) + "_querycomplete",
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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public void BeginTransaction() {
		checkNull();
		db.beginTransaction();
	}
	/**
	 * Marks the transaction as a successful transaction. No further statements should be executed till calling EndTransaction.
	 */
	@Override
	public void TransactionSuccessful() {
		db.setTransactionSuccessful();
	}
	/**
	 * Ends the transaction.
	 */
	@Override
	public void EndTransaction() {
		db.endTransaction();
	}
	/**
	 * Closes the database.
	 *Does not do anything if the database is not opened or was closed before.
	 */
	@Override
	public void Close() {
		if (db != null && db.isOpen())
			db.close();
	}
}
