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


import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.GetForIndex;
import org.jdbx.function.GetForName;


/**
 * QueryRows is a builder class to
 * define the multi-row result of a query.
 */
public class QueryRows
{
	QueryRows(Query query, int max)
	{
		query_ = query;
		max_    = max;
	}


	/**
	 * Loops through all rows of the ResultSet and calls
	 * the ResultSet consumer for each row.
	 * @param consumer a ResultSet consumer
	 */
	public void read(CheckedConsumer<ResultSet> consumer) throws JdbException
	{
		Check.notNull(consumer, "consumer");
		query_.read(result -> {
			int index = -1;
			while ((++index < max_) && result.next())
				consumer.accept(result);
		});
	}


	/**
	 * Loops through all rows of the ResultSet and calls
	 * the ResultSet reader for each row. All values returned by the reader
	 * are collected in a List.
	 * @param reader receives a ResultSet and returns a value
	 * @param <T> the type of the result returned by the reader
	 * @return the list
	 */
	public <T> List<T> value(CheckedFunction<ResultSet,T> reader) throws JdbException
	{
		return value(reader, new ArrayList<>());
	}


	/**
	 * Loops through all rows of the ResultSet and calls
	 * the ResultSet reader for each row. All values returned by the reader
	 * are collected in the given List.
	 * @param reader receives a ResultSet and returns a value
	 * @param list a list
	 * @param <T> the type of the result returned by the reader
	 * @return the list
	 */
	public <T> List<T> value(CheckedFunction<ResultSet,T> reader, List<T> list) throws JdbException
	{
		Check.notNull(reader, "reader");
		Check.notNull(list, "list");
		return query_.read0(false, result -> {
			if (query_.applySkip(result))
			{
				int index = -1;
				while ((++index < max_) && result.next())
					list.add(reader.apply(result));
			}
			return list;
		});
	}


	/**
	 * Loops through all rows of the ResultSet, creates a Map for every row and
	 * returns a List of these maps.
	 * @return the list. Each Map entry in the list maps column name to column value.
	 */
	public List<Map<String,Object>> map() throws JdbException
	{
		return value(rs -> ResultUtil.readMap(rs));
	}


	/**
	 * Loops through all rows of the ResultSet, creates a Map for every row and
	 * returns a List of these maps.
	 * @param colNames a list of column names
	 * @return the list
	 */
	public List<Map<String,Object>> map(String... colNames) throws JdbException
	{
		Check.notNull(colNames, "column names");
		return value(rs -> ResultUtil.readMap(rs, colNames));
	}


	/**
	 * Loops through all rows of the ResultSet, creates a value array for every row and
	 * returns a List of these arrays.
	 * @return the list
	 */
	public List<Object[]> cols() throws JdbException
	{
		return value(rs -> ResultUtil.readValues(rs));
	}


	/**
	 * Loops through all rows of the ResultSet, creates a value array for every row and
	 * returns a List of these arrays.
	 * @param colIndexes the indexes of the columns which should be read from every row
	 * @return the list
	 */
	public List<Object[]> cols(int... colIndexes) throws JdbException
	{
		Check.notNull(colIndexes, "colIndexes");
		return value(rs -> ResultUtil.readValues(rs, colIndexes));
	}


	/**
	 * Loops through all rows of the ResultSet, creates a value array for every row and
	 * returns a List of these arrays.
	 * @param colNames the names of the columns which should be read from every row
	 * @return the list
	 */
	public List<Object[]> cols(String... colNames) throws JdbException
	{
		Check.notNull(colNames, "colNames");
		return value(rs -> ResultUtil.readValues(rs, colNames));
	}


	/**
	 * Returns a column object for the first result column. Use getters of the column object
	 * to return a list of values for the column,
	 * @return a column object.
	 */
	public IndexedCol col()
	{
		return col(1);
	}


	/**
	 * Returns a column object for the n-th result column. Use getters of the column object
	 * to return a list of values for the column,
	 * @param index a column index, starting at 1
	 * @return a column object.
	 */
	public IndexedCol col(int index)
	{
		return new IndexedCol(index);
	}


	/**
	 * Returns a column object for the result column with the given name. Use getters of the column object
	 * to return a list of values for the column,
	 * @param name a column name
	 * @return a column object.
	 */
	public NamedCol col(String name)
	{
		return new NamedCol(name);
	}


	/**
	 * Column is a builder class to
	 * define the single result column of a multi row result.
	 */
	public abstract class Column
	{
		/**
		 * Returns a list of BigDecimal objects.
		 * @return the list
		 */
		public List<BigDecimal> getBigDecimal() throws JdbException
		{
			return get(GetAccessors.BIGDECIMAL);
		}


		/**
		 * Returns a list of Blob objects.
		 * @return the list
		 */
		public List<Blob> getBlob() throws JdbException
		{
			return get(GetAccessors.BLOB);
		}


		/**
		 * Returns a list of Boolean objects.
		 * @return the list
		 */
		public List<Boolean> getBooleanObject() throws JdbException
		{
			return get(GetAccessors.BOOLEAN);
		}


		/**
		 * Returns a list of Byte objects.
		 * @return the list
		 */
		public List<Byte> getByteObject() throws JdbException
		{
			return get(GetAccessors.BYTE);
		}


		/**
		 * Returns a list of byte arrays.
		 * @return the list
		 */
		public List<byte[]> getBytes() throws JdbException
		{
			return get(GetAccessors.BYTES);
		}


		/**
		 * Returns a list of Reader objects.
		 * @return the list
		 */
		public List<Reader> getCharacterStream() throws JdbException
		{
			return get(GetAccessors.CHARACTERSTREAM);
		}


		/**
		 * Returns a list of Clob objects.
		 * @return the list
		 */
		public List<Clob> getClob() throws JdbException
		{
			return get(GetAccessors.CLOB);
		}


		/**
		 * Returns a list of Double objects.
		 * @return the list
		 */
		public List<Double> getDouble() throws JdbException
		{
			return get(GetAccessors.DOUBLE);
		}


		/**
		 * Returns a list of Float objects.
		 * @return the list
		 */
		public List<Float> getFloatObject() throws JdbException
		{
			return get(GetAccessors.FLOAT);
		}


		/**
		 * Returns a list of Integer objects.
		 * @return the list
		 */
		public List<Integer> getInteger() throws JdbException
		{
			return get(GetAccessors.INTEGER);
		}


		/**
		 * Returns a list of Long objects.
		 * @return the list
		 */
		public List<Long> getLong() throws JdbException
		{
			return get(GetAccessors.LONG);
		}


		/**
		 * Returns a list of Reader objects.
		 * @return the list
		 */
		public List<Reader> getNCharacterStream() throws JdbException
		{
			return get(GetAccessors.NCHARACTERSTREAM);
		}


		/**
		 * Returns a list of NClob objects.
		 * @return the list
		 */
		public List<NClob> getNClob() throws JdbException
		{
			return get(GetAccessors.NCLOB);
		}


		/**
		 * Returns a list of String objects.
		 * @return the list
		 */
		public List<String> getNString() throws JdbException
		{
			return get(GetAccessors.NSTRING);
		}


		/**
		 * Returns a list of RowId objects.
		 * @return the list
		 */
		public List<RowId> getRowId() throws JdbException
		{
			return get(GetAccessors.ROWID);
		}


		/**
		 * Returns a list of Short objects.
		 * @return the list
		 */
		public List<Short> getShort() throws JdbException
		{
			return get(GetAccessors.SHORT);
		}


		/**
		 * Returns a list of SQL array objects.
		 * @return the list
		 */
		public List<Array> getSqlArray() throws JdbException
		{
			return get(GetAccessors.ARRAY);
		}


		/**
		 * Returns a list of SQL Date objects.
		 * @return the list
		 */
		public List<java.sql.Date> getSqlDate() throws JdbException
		{
			return get(GetAccessors.SQLDATE);
		}


		/**
		 * Returns a list of SQL ref objects.
		 * @return the list
		 */
		public List<Ref> getSqlRef() throws JdbException
		{
			return get(GetAccessors.REF);
		}


		/**
		 * Returns a list of SQL array objects.
		 * @return the list
		 */
		public List<java.sql.Time> getSqlTime() throws JdbException
		{
			return get(GetAccessors.SQLTIME);
		}


		/**
		 * Returns a list of SQL timestamp objects.
		 * @return the list
		 */
		public List<java.sql.Timestamp> getSqlTimestamp() throws JdbException
		{
			return get(GetAccessors.SQLTIMESTAMP);
		}


		/**
		 * Returns a list of SQL array objects.
		 * @return the list
		 */
		public List<SQLXML> getSqlXml() throws JdbException
		{
			return get(GetAccessors.SQLXML);
		}


		/**
		 * Returns a list of String objects.
		 * @return the list
		 */
		public List<String> getString() throws JdbException
		{
			return get(GetAccessors.STRING);
		}


		/**
		 * Returns a list of objects.
		 * @return the list
		 */
		public List<Object> get() throws JdbException
		{
			return get(GetAccessors.OBJECT);
		}


		/**
		 * Returns a list of objects.
		 * @param type the object type
		 * @param <T> the object type
		 * @return the list
		 */
		public abstract <T> List<T> get(Class<T> type) throws JdbException;


		/**
		 * Returns a list of objects.
		 * @param accessor the accessor for the object
		 * @return the list
		 */
		protected abstract <T> List<T> get(GetAccessors<T> accessor) throws JdbException;
	}


	/**
	 * A Column implementation for columns accessed by index.
	 */
	public class IndexedCol extends Column
	{
		private IndexedCol(int index)
		{
			index_ = Check.index(index);
		}


		/**
		 * Returns a list of objects.
		 * @param type the object type
		 * @return the list
		 */
		@Override public <T> List<T> get(Class<T> type) throws JdbException
		{
			Check.notNull(type, "type");
			return value(result -> result.getObject(index_, type));
		}


		@Override protected <T> List<T> get(GetAccessors<T> accessor) throws JdbException
		{
			Check.notNull(accessor, "accessor");
			return get(accessor.resultForIndex);
		}


		/**
		 * Returns a list of objects.
		 * @param fn a function which can extract the object from a result set
		 * @param <T> the type of the function return value
		 * @return the list
		 */
		public <T> List<T> get(GetForIndex<ResultSet,T> fn) throws JdbException
		{
			Check.notNull(fn, "fn");
			return value(result -> fn.get(result, index_));
		}


		private int index_;
	}


	/**
	 * A Column implementation for columns accessed by name.
	 */
	public class NamedCol extends Column
	{
		private NamedCol(String name)
		{
			name_ = Check.name(name);
		}


		/**
		 * Returns a list of objects.
		 * @param type the object type
		 * @return the list
		 */
		@Override public <T> List<T> get(Class<T> type) throws JdbException
		{
			Check.notNull(type, "type");
			return value(result -> result.getObject(name_, type));
		}


		/**
		 * Returns a list of objects.
		 * @param fn a function which can extract the object from a result set
		 * @param <T> the type of the function return value
		 * @return the list
		 */
		public <T> List<T> get(GetForName<ResultSet,T> fn) throws JdbException
		{
			Check.notNull(fn, "fn");
			return value(result -> fn.get(result, name_));
		}


		@Override protected <T> List<T> get(GetAccessors<T> accessor) throws JdbException
		{
			Check.notNull(accessor, "accessor");
			return get(accessor.resultForName);
		}


		private String name_;
	}


	private Query query_;
	private int max_ = Integer.MAX_VALUE;
}
