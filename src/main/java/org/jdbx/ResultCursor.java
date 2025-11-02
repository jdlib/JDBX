/*
 * Copyright (C) 2016 JDBX
 *
 * https://github.com/jdlib/JDBX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbx;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Map;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.GetForNumber;
import org.jdbx.function.SetForNumber;
import org.jdbx.function.SetForName;
import org.jdbx.function.Unchecked;


/**
 * ResultCursor represents the result of a SQL query.
 * It is a wrapper class for java.sql.ResultSet.
 */
public class ResultCursor implements AutoCloseable
{
	/**
	 * Returns a ResultCursor for the JDBC ResultSet
	 * which does not close the ResultSet when it is closed.
	 * Use this factory method to avoid compiler warnings
	 * for unclosed resources when you don't want to close the ResultSet.
	 * @param resultSet the wrapped result set
	 * @return the new ResultCursor
	 */
	@SuppressWarnings("resource")
	public static ResultCursor of(ResultSet resultSet)
	{
		return new ResultCursor(resultSet).setCloseResult(false);
	}


	/**
	 * Creates a new ResultCursor which wraps a ResultSet.
	 * @param resultSet the result set
	 */
	public ResultCursor(ResultSet resultSet)
	{
		resultSet_ = Check.notNull(resultSet, "resultSet");
	}


	/**
	 * Returns the internal JDBC ResultSet.
	 * @return the ResultSet
	 */
	public ResultSet getJdbcResult()
	{
		return resultSet_;
	}


	//----------------------------------
	// column operations
	//----------------------------------


	/**
	 * Returns the column values of the current row as array.
	 * @return the value array
	 */
	public Object[] cols() throws JdbxException
	{
		try
		{
			return ResultUtil.readValues(resultSet_);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Returns column values of the current row as array.
	 * @param numbers the column numbers, starting at 1.
	 * @return the value array
	 */
	public Object[] cols(int... numbers) throws JdbxException
	{
		Check.notNull(numbers, "numbers");
		try
		{
			return ResultUtil.readValues(resultSet_, numbers);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Returns column values of the current row as array.
	 * @param names the column names
	 * @return the value array
	 */
	public Object[] cols(String... names) throws JdbxException
	{
		Check.notNull(names, "names");
		try
		{
			return ResultUtil.readValues(resultSet_, names);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Returns the column values of the current row as map.
	 * @return a map whose keys are the column names
	 */
	public Map<String, Object> map() throws JdbxException
	{
		try
		{
			return ResultUtil.readMap(resultSet_);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Returns the column values of the current row as map.
	 * @param names the column names
	 * @return a map whose keys are the column names
	 */
	public Map<String, Object> map(String... names) throws JdbxException
	{
		Check.notNull(names, "names");
		try
		{
			return ResultUtil.readMap(resultSet_, names);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Calls {@link #col(int)} with column number 1.
	 * @return the column
	 */
	public NumberedColumn col()
	{
		return col(1);
	}


	/**
	 * Returns the result column for the given number.
	 * The column should only be used to immediately access the value, but
	 * not stored for later use.
	 * @param number the column number, starting at 1.
	 * @return the column
	 */
	public NumberedColumn col(int number)
	{
		numberedColumn_.number_ = Check.number(number);
		return numberedColumn_;
	}


	/**
	 * Returns the result column for the given name.
	 * The column should only be used to immediately access the value, but
	 * not stored for later use.
	 * @param name the column name
	 * @return the column
	 */
	public NamedColumn col(String name)
	{
		namedColumn_.name_ = Check.name(name);
		return namedColumn_;
	}


	/**
	 * Returns the result column for the next column number.
	 * The ResultCursor keeps internally keeps track of the next column number.
	 * Whenever the cursor is positioned on a new row, the next column number is reset to 1.
	 * The returned column object should only be used to immediately access the value, but
	 * not stored for later use.
	 * @return the column
	 */
	public NumberedColumn nextCol()
	{
		return col(nextColNumber_++);
	}


	/**
	 * Returns the next column number.
	 * @see #nextCol()
	 * @return the number, starting at 1
	 */
	public int getNextColNumber()
	{
		return nextColNumber_;
	}


	/**
	 * Sets the current column number.
	 * @param number the number, starting at 1
	 * @return this
	 */
	public ResultCursor setNextColNumber(int number)
	{
		nextColNumber_ = Check.number(number);
		return this;
	}


	/**
	 * Reset the next column number to 1.
	 * @return this
	 */
	public ResultCursor resetNextColNumber()
	{
		return setNextColNumber(1);
	}


	/**
	 * Increases the next column number by 1.
	 * @return this
	 * @see #nextCol()
	 */
	public ResultCursor skipNextCol()
	{
		return skipNextCols(1);
	}


	/**
	 * Increases the column number by the given count.
	 * @param count the count
	 * @return this
	 */
	public ResultCursor skipNextCols(int count)
	{
		return setNextColNumber(nextColNumber_ + count);
	}


	/**
	 * Allows to access the value of a column which was specified by column number.
	 */
	public class NumberedColumn implements GetResult, SetValue
	{
		/**
		 * Returns the column value as object with the given type.
		 * @param type the type of the column value
		 * @see ResultSet#getObject(int, Class)
		 * @return the value
		 */
		@Override public <T> T get(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			try
			{
				return resultSet_.getObject(number_, type);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		/**
		 * Returns the column value as object.
		 * @param map a <code>java.util.Map</code> object that contains the mapping
		 * 		from SQL type names to classes in the Java programming language
		 * @return the value
		 * @see ResultSet#getObject(int, Map)
		 */
		@Override public Object get(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			try
			{
				return resultSet_.getObject(number_, map);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> T get(GetAccessors<T> accessors) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			return get(accessors.resultForNumber);
		}


		public <T> T get(GetForNumber<ResultSet,T> getter) throws JdbxException
		{
			Check.notNull(getter, "getter");
			try
			{
				return getter.get(resultSet_, number_);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> void set(SetAccessors<T> accessors, T value) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			set(accessors.resultForIndex, value);
		}


		public <T> void set(SetForNumber<ResultSet,T> setter, T value) throws JdbxException
		{
			Check.notNull(setter, "setter");
			try
			{
				setter.set(resultSet_, number_, value);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		private int number_;
	}


	/**
	 * Allows to access the value of a column which was specified by a name.
	 */
	public class NamedColumn implements GetResult, SetValue
	{
		/**
		 * Returns the column value as object with the given type.
		 * @param type the type of the column value
		 * @see ResultSet#getObject(String, Class)
		 * @return the value
		 */
		@Override public <T> T get(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			try
			{
				return resultSet_.getObject(name_, type);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		/**
		 * Returns the column value as object.
		 * @param map a <code>java.util.Map</code> object that contains the mapping
		 * 		from SQL type names to classes in the Java programming language
		 * @return the value
		 * @see ResultSet#getObject(String, Map)
		 */
		@Override public Object get(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			try
			{
				return resultSet_.getObject(name_, map);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> T get(GetAccessors<T> accessors) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			try
			{
				return accessors.resultForName.get(resultSet_, name_);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> void set(SetAccessors<T> accessors, T value) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			set(accessors.resultForName, value);
		}


		public <T> void set(SetForName<ResultSet,T> setter, T value) throws JdbxException
		{
			Check.notNull(setter, "setter");
			try
			{
				setter.set(resultSet_, name_, value);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		private String name_;
	}


	//----------------------------------
	// row operations
	//----------------------------------


	/**
	 * Skips n rows.
	 * @param count the number of rows to skip. If &lt;= 0 this call has no effect.
	 * @return true, if the amount of rows have been skipped or count was &lt;= 0, false
	 * 		if the result had less remaining rows than then given row count
	 */
	public boolean skip(int count)
	{
		int n = 0;
		while (n < count)
		{
			if (!next())
				return false;
			n++;
		}
		return true;
	}


	//----------------------------------
	// accessors
	//----------------------------------


	/**
	 * Returns the fetch size of the ResultSet.
	 * @return the fetch size
	 */
	public int getFetchSize() throws JdbxException
	{
		return toInt(ResultSet::getFetchSize);
	}


	/**
	 * Sets the fetch size of the cursor.
	 * @param size the fetch size
	 */
	public void setFetchSize(int size) throws JdbxException
	{
		Unchecked.run(() -> resultSet_.setFetchSize(size));
	}


	/**
	 * Returns the fetch direction of the cursor.
	 * @return the fetch direction as enum value
	 */
	public FetchDirection getFetchDirection() throws JdbxException
	{
		return JdbcEnumMap.FETCH_DIRECTION.forCode(toInt(ResultSet::getFetchDirection));
	}


	/**
	 * Sets the fetch direction of the cursor.
	 * @param dir the fetch direction as enum value
	 */
	public void setFetchDirection(FetchDirection dir) throws JdbxException
	{
		Check.valid(FetchDirection.class, dir);
		Unchecked.run(() -> resultSet_.setFetchDirection(dir.getCode()));
	}


	/**
	 * Returns the concurrency of the cursor.
	 * @return the concurrency as enum value
	 */
	public Concurrency getConcurrency() throws JdbxException
	{
		return JdbcEnumMap.CONCURRENCY.forCode(toInt(ResultSet::getConcurrency));
	}


	/**
	 * Returns the holdability of the ResultSet.
	 * @return the holdability as enum value
	 */
	public Holdability getHoldability() throws JdbxException
	{
		return JdbcEnumMap.HOLDABILITY.forCode(toInt(ResultSet::getHoldability));
	}


	/**
	 * Returns the result type of the ResultSet.
	 * @return the type as enum value
	 */
	public ResultType getType() throws JdbxException
	{
		return JdbcEnumMap.RESULT_TYPE.forCode(toInt(ResultSet::getType));
	}


	/**
	 * Returns the name of the SQL cursor used by the ResultSet.
	 * @return the cursor name
	 */
	public String getCursorName() throws JdbxException
	{
		return toValue(ResultSet::getCursorName);
	}


	/**
	 * Returns the meta data of the ResultSet.
	 * @return the meta data
	 */
	public ResultSetMetaData getMetaData() throws JdbxException
	{
		return toValue(ResultSet::getMetaData);
	}


	/**
	 * Returns the number of the column with the given label.
	 * @param columnLabel the label
	 * @return the number, &gt;= 1
	 */
	public int findColumn(String columnLabel) throws JdbxException
	{
		return Unchecked.get(() -> Integer.valueOf(resultSet_.findColumn(columnLabel))).intValue();
	}


	/**
	 * Returns the ResultSet warnings.
	 * @return the first warning or null
	 */
	public SQLWarning getWarnings() throws JdbxException
	{
		return toValue(ResultSet::getWarnings);
	}


	/**
	 * Clears the ResultSet warnings.
	 */
	public void clearWarnings() throws JdbxException
	{
		call(ResultSet::clearWarnings);
	}


	/**
	 * Returns if the last value read from the ResultSet was null.
	 * @return was the last value read from the ResultSet null?
	 */
	public boolean wasNull() throws JdbxException
	{
		return toBoolean(ResultSet::wasNull);
	}


	//----------------------------------
	// position
	//----------------------------------


	/**
	 * Returns a position object.
	 * @return the position
	 */
	public Position position()
	{
		return new Position();
	}


	/**
	 * Allows to retrieve the result set position.
	 */
	public class Position
	{
		/**
		 * @return true if the result set is positioned before the first row
		 */
		public boolean isBeforeFirst() throws JdbxException
		{
			return toBoolean(ResultSet::isBeforeFirst);
		}


		/**
		 * @return true if the result set is positioned after the last row
		 */
		public boolean isAfterLast() throws JdbxException
		{
			return toBoolean(ResultSet::isAfterLast);
		}


		/**
		 * @return true if the result set is positioned on the last row
		 */
		public boolean isLast() throws JdbxException
		{
			return toBoolean(ResultSet::isLast);
		}
	}


	//----------------------------------
	// moving
	//----------------------------------


	/**
	 * @return a Move object which allows to move the current row.
	 */
	public Move move()
	{
		if (move_ == null)
			move_ = new Move();
		return move_;
	}


	/**
	 * Allows to move the current row.
	 */
	public class Move
	{
		/**
		 * Moves the cursor to the given row number.
		 * @param row the row
		 * @return true if successful
		 * @see ResultSet#absolute(int)
		 */
		public boolean absolute(int row) throws JdbxException
		{
			return Unchecked.get(() -> Boolean.valueOf(resultSet_.absolute(row))).booleanValue();
		}


		/**
		 * Moves the cursor a relative number of rows.
		 * @param rows the rows
		 * @return true if successful
		 * @see ResultSet#relative(int)
		 */
		public boolean relative(int rows) throws JdbxException
		{
			return Unchecked.get(() -> Boolean.valueOf(resultSet_.relative(rows))).booleanValue();
		}


		/**
		 * Moves the cursor to the end of the result.
		 * @see ResultSet#afterLast()
		 */
		public void afterLast() throws JdbxException
		{
			call(ResultSet::afterLast);
		}


		/**
		 * Moves the cursor before the start of the result.
		 * @see ResultSet#beforeFirst()
		 */
		public void beforeFirst() throws JdbxException
		{
			call(ResultSet::beforeFirst);
		}


		/**
		 * Moves the cursor to the first result row.
		 * @see ResultSet#first()
		 * @return was the cursor moved?
		 */
		public boolean first() throws JdbxException
		{
			return toBoolean(ResultSet::first);
		}


		/**
		 * Moves the cursor to the last result row.
		 * @see ResultSet#last()
		 * @return was the cursor moved?
		 */
		public boolean last() throws JdbxException
		{
			return toBoolean(ResultSet::last);
		}


		/**
		 * Moves the cursor to the previous row.
		 * @see ResultSet#previous()
		 * @return was the cursor moved?
		 */
		public boolean previous() throws JdbxException
		{
			return toBoolean(ResultSet::previous);
		}


		/**
		 * Moves the cursor to the next row.
		 * @see ResultSet#next()
		 * @return was the cursor moved?
		 */
		public boolean next() throws JdbxException
		{
			return ResultCursor.this.next();
		}


		/**
		 * Moves the cursor to the remembered cursor position.
		 * @see ResultSet#moveToCurrentRow()
		 */
		public void toCurrentRow() throws JdbxException
		{
			call(ResultSet::moveToCurrentRow);
		}


		/**
		 * Moves the cursor to the insert row.
		 * @see ResultSet#moveToInsertRow()
		 */
		public void toInsertRow() throws JdbxException
		{
			call(ResultSet::moveToInsertRow);
		}
	}


	/**
	 * Moves the cursor to the next row.
	 * @return true if cursor was moved
	 * @see ResultSet#next()
	 */
	public boolean next() throws JdbxException
	{
		return toBoolean(ResultSet::next);
	}


	/**
	 * Moves the cursor to the next row.
	 * @exception JdbxException thrown if there is no next row
	 * @return this
	 */
	public ResultCursor nextRequired() throws JdbxException
	{
		if (!next())
			throw JdbxException.invalidResult("no next row");
		return this;
	}


	//----------------------------------
	// helper
	//----------------------------------


	/**
	 * @return a row object to perform row operations
	 */
	public Row row()
	{
		if (row_ == null)
			row_ = new Row();
		return row_;
	}


	/**
	 * Allows to perform row operations.
	 * @see #row()
	 */
	public class Row
	{
		private Row()
		{
		}


		/**
		 * Inserts the contents of the insert row into the result.
		 * @see ResultSet#insertRow()
		 */
		public void insert() throws JdbxException
		{
			call(ResultSet::insertRow);
		}


		/**
		 * Returns if the current row was inserted.
		 * @see ResultSet#rowInserted()
		 * @return the flag
		 */
		public boolean isInserted() throws JdbxException
		{
			return toBoolean(ResultSet::rowInserted);
		}


		/**
		 * Updates the contents of the current row.
		 * @see ResultSet#updateRow()
		 */
		public void update() throws JdbxException
		{
			call(ResultSet::updateRow);
		}


		/**
		 * Returns if the current row was updated.
		 * @see ResultSet#rowUpdated()
		 * @return the flag
		 */
		public boolean isUpdated() throws JdbxException
		{
			return toBoolean(ResultSet::rowUpdated);
		}


		/**
		 * Deletes the the current row.
		 * @see ResultSet#deleteRow()
		 */
		public void delete() throws JdbxException
		{
			call(ResultSet::deleteRow);
		}


		/**
		 * Returns if the row was deleted.
		 * @see ResultSet#rowDeleted()
		 * @return the flag
		 */
		public boolean isDeleted() throws JdbxException
		{
			return toBoolean(ResultSet::rowDeleted);
		}


		/**
		 * Refreshes the current row.
		 * @see ResultSet#refreshRow()
		 */
		public void refresh() throws JdbxException
		{
			call(ResultSet::refreshRow);
		}


		/**
		 * Cancels the updates made to the current row.
		 * @see ResultSet#cancelRowUpdates()
		 */
		public void cancelUpdates() throws JdbxException
		{
			call(ResultSet::cancelRowUpdates);
		}
	}


	//----------------------------------
	// helper
	//----------------------------------


	private boolean toBoolean(CheckedFunction<ResultSet,Boolean> fn) throws JdbxException
	{
		return Unchecked.apply(fn, resultSet_).booleanValue();
	}


	private int toInt(CheckedFunction<ResultSet,Integer> fn) throws JdbxException
	{
		return Unchecked.apply(fn, resultSet_).intValue();
	}


	private <T> T toValue(CheckedFunction<ResultSet,T> fn) throws JdbxException
	{
		return Unchecked.apply(fn, resultSet_);
	}


	private void call(CheckedConsumer<ResultSet> fn) throws JdbxException
	{
		Unchecked.accept(fn, resultSet_);
	}


	//----------------------------------
	// closing
	//----------------------------------


	/**
	 * Returns if the internal ResultSet should be closed when this ResultCursor is closed.
	 * @return the close result flag
	 */
	public boolean isCloseResult()
	{
		return closeResult_;
	}


	/**
	 * Instructs the ResultCursor if the internal ResultSet should be closed
	 * when this ResultCursor is closed.
	 * @param flag the close flag
	 * @return this
	 */
	public ResultCursor setCloseResult(boolean flag)
	{
		closeResult_ = flag;
		return this;
	}


	/**
	 * Closes the ResultSet if {@link #isCloseResult()} is true.
	 */
	@Override public void close() throws JdbxException
	{
		if (closeResult_)
			call(ResultSet::close);
	}


	private final ResultSet resultSet_;
	private final NumberedColumn numberedColumn_ = new NumberedColumn();
	private final NamedColumn namedColumn_ = new NamedColumn();
	private int nextColNumber_ = 1;
	private boolean closeResult_ = true;
	private Move move_;
	private Row row_;
}
