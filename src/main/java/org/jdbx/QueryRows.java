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
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.GetForNumber;
import org.jdbx.function.GetForName;


/**
 * QueryRows is a builder class to extract a list of values from all remaining rows of a {@link QueryResult}.
 * @see Query#rows()
 */
public class QueryRows
{
	QueryRows(Query query)
	{
		query_ = query;
	}


	/**
	 * Limits the rows extracted from the result to max rows.
	 * @param max the maximum number of rows. A negative value is interpreted
	 * 		as unlimited rows.
	 */
	public QueryRows max(int max)
	{
		max_ = max;
		return this;
	}


	private boolean allowRow(int rowIndex)
	{
		return (max_ < 0) || (rowIndex < max_);
	}


	/**
	 * Loops through all rows of the ResultSet and calls
	 * the consumer for each row.
	 * @param consumer a ResultSet consumer
	 */
	public void forEach(CheckedConsumer<QueryResult> consumer) throws JdbxException
	{
		Check.notNull(consumer, "consumer");
		CheckedConsumer<QueryResult> c = result -> {
			int index = -1;
			while (allowRow(++index) && result.nextRow())
				consumer.accept(result);
		};
		query_.read(c);
	}


	/**
	 * Loops through all rows of the result and calls
	 * the reader function for each row. All values returned by the reader
	 * are collected in a List.
	 * @param reader receives a result and returns a value for the current row
	 * @param <T> the type of the result returned by the reader
	 * @return the list
	 */
	public <T> List<T> read(CheckedFunction<QueryResult,T> reader) throws JdbxException
	{
		return read(reader, new ArrayList<>());
	}


	/**
	 * Loops through all rows of the result and calls
	 * the reader function for each row. All values returned by the reader
	 * are collected in the given List.
	 * @param reader receives a result and returns a value for the current row
	 * @param list a list
	 * @param <T> the type of the result returned by the reader
	 * @return the list
	 */
	public <T> List<T> read(CheckedFunction<QueryResult,T> reader, List<T> list) throws JdbxException
	{
		Check.notNull(reader, "reader");
		Check.notNull(list, "list");
		return query_.read(false, result -> {
			if (query_.applySkip(result))
			{
				int index = -1;
				while (allowRow(++index) && result.nextRow())
					list.add(reader.apply(result));
			}
			return list;
		});
	}


	/**
	 * @return a builder to read the values of all columns.
	 */
	public Cols cols() throws JdbxException
	{
		return new Cols(null, null);
	}


	/**
	 * @return a builder to read the values of certain columns.
	 * @param numbers the numbers of the columns to read
	 */
	public Cols cols(int... numbers) throws JdbxException
	{
		Check.notNull(numbers, "numbers");
		return new Cols(null, numbers);
	}


	/**
	 * @return a builder to read the values of certain columns.
	 * @param names the names of the columns to read
	 */
	public Cols cols(String... names) throws JdbxException
	{
		Check.notNull(names, "names");
		return new Cols(names, null);
	}


	public class Cols
	{
		private Cols(String[] names, int[] numbers)
		{
			names_ = names;
			numbers_ = numbers;
		}


		public List<Map<String,Object>> toMap()
		{
			return read(qr -> ResultUtil.toMap(qr.getJdbcResult(), names_, numbers_));
		}


		public List<List<Object>> toList()
		{
			return read(qr -> Arrays.asList(ResultUtil.toArray(qr.getJdbcResult(), names_, numbers_)));
		}


		public List<Object[]> toArray()
		{
			return read(qr -> ResultUtil.toArray(qr.getJdbcResult(), names_, numbers_));
		}


		private final String[] names_;
		private final int[] numbers_;
	}



	/**
	 * Returns a NumberedCol object representing the first result. Use getters of NumberedCol
	 * to return a list of values of the column.
	 * to return a list of values for the column,
	 * @return the NumberedCol
	 */
	public NumberedCol col()
	{
		return col(1);
	}


	/**
	 * Returns a NumberedCol object representing the result column with the number. Use getters of NumberedCol
	 * to return a list of values of the column.
	 * @param number a column number, starting at 1
	 * @return the NumberedCol
	 */
	public NumberedCol col(int number)
	{
		return new NumberedCol(number);
	}


	/**
	 * Returns a NamedCol object representing the result column with the given name. Use getters of NamedCol
	 * to return a list of values of the column.
	 * @param name a column name
	 * @return the NamedCol
	 */
	public NamedCol col(String name)
	{
		return new NamedCol(name);
	}


	/**
	 * Column is a builder base class to represent the single result column of a multi row result.
	 * I allows to specify the type of the column and return a list of it's values from all rows.
	 */
	public abstract class Column
	{
		/**
		 * @return a list of BigDecimal objects.
		 */
		public List<BigDecimal> getBigDecimal() throws JdbxException
		{
			return get(GetAccessors.BIGDECIMAL);
		}


		/**
		 * @return a list of Blob objects.
		 */
		public List<Blob> getBlob() throws JdbxException
		{
			return get(GetAccessors.BLOB);
		}


		/**
		 * @return a list of Boolean objects.
		 */
		public List<Boolean> getBooleanObject() throws JdbxException
		{
			return get(GetAccessors.BOOLEAN);
		}


		/**
		 * @return a list of Byte objects.
		 */
		public List<Byte> getByteObject() throws JdbxException
		{
			return get(GetAccessors.BYTE);
		}


		/**
		 * @return a list of byte arrays.
		 */
		public List<byte[]> getBytes() throws JdbxException
		{
			return get(GetAccessors.BYTES);
		}


		/**
		 * @return a list of Reader objects.
		 */
		public List<Reader> getCharacterStream() throws JdbxException
		{
			return get(GetAccessors.CHARACTERSTREAM);
		}


		/**
		 * @return a list of Clob objects.
		 */
		public List<Clob> getClob() throws JdbxException
		{
			return get(GetAccessors.CLOB);
		}


		/**
		 * @return a list of Double objects.
		 */
		public List<Double> getDouble() throws JdbxException
		{
			return get(GetAccessors.DOUBLE);
		}


		/**
		 * @return a list of Float objects.
		 */
		public List<Float> getFloat() throws JdbxException
		{
			return get(GetAccessors.FLOAT);
		}


		/**
		 * @return a list of Integer objects.
		 */
		public List<Integer> getInteger() throws JdbxException
		{
			return get(GetAccessors.INTEGER);
		}


		/**
		 * @return a list of Long objects.
		 */
		public List<Long> getLong() throws JdbxException
		{
			return get(GetAccessors.LONG);
		}


		/**
		 * @return a list of Reader objects.
		 */
		public List<Reader> getNCharacterStream() throws JdbxException
		{
			return get(GetAccessors.NCHARACTERSTREAM);
		}


		/**
		 * @return a list of NClob objects.
		 */
		public List<NClob> getNClob() throws JdbxException
		{
			return get(GetAccessors.NCLOB);
		}


		/**
		 * @return a list of String objects.
		 */
		public List<String> getNString() throws JdbxException
		{
			return get(GetAccessors.NSTRING);
		}


		/**
		 * @return a list of RowId objects.
		 */
		public List<RowId> getRowId() throws JdbxException
		{
			return get(GetAccessors.ROWID);
		}


		/**
		 * @return a list of Short objects.
		 */
		public List<Short> getShort() throws JdbxException
		{
			return get(GetAccessors.SHORT);
		}


		/**
		 * @return a list of SQL array objects.
		 */
		public List<Array> getSqlArray() throws JdbxException
		{
			return get(GetAccessors.ARRAY);
		}


		/**
		 * @return a list of SQL Date objects.
		 */
		public List<java.sql.Date> getSqlDate() throws JdbxException
		{
			return get(GetAccessors.SQLDATE);
		}


		/**
		 * @return a list of SQL ref objects.
		 */
		public List<Ref> getSqlRef() throws JdbxException
		{
			return get(GetAccessors.REF);
		}


		/**
		 * @return a list of SQL array objects.
		 */
		public List<java.sql.Time> getSqlTime() throws JdbxException
		{
			return get(GetAccessors.SQLTIME);
		}


		/**
		 * @return a list of SQL timestamp objects.
		 */
		public List<java.sql.Timestamp> getSqlTimestamp() throws JdbxException
		{
			return get(GetAccessors.SQLTIMESTAMP);
		}


		/**
		 * @return a list of SQL array objects.
		 */
		public List<SQLXML> getSqlXml() throws JdbxException
		{
			return get(GetAccessors.SQLXML);
		}


		/**
		 * @return a list of String objects.
		 */
		public List<String> getString() throws JdbxException
		{
			return get(GetAccessors.STRING);
		}


		/**
		 * @return a list of URL objects.
		 */
		public List<URL> getURL() throws JdbxException
		{
			return get(GetAccessors.URL);
		}


		/**
		 * @return a list of objects.
		 */
		public List<Object> getObject() throws JdbxException
		{
			return get(GetAccessors.OBJECT);
		}


		/**
		 * @return a list of objects.
		 * @param type the object type
		 * @param <T> the object type
		 */
		public abstract <T> List<T> get(Class<T> type) throws JdbxException;


		/**
		 * @return a list of objects
		 * @param accessor the accessor for the object
		 */
		protected abstract <T> List<T> get(GetAccessors<T> accessor) throws JdbxException;
	}


	/**
	 * A Column implementation for columns accessed by column number.
	 */
	public class NumberedCol extends Column
	{
		private NumberedCol(int number)
		{
			number_ = Check.number(number);
		}


		/**
		 * @return a list of objects.
		 * @param type the object type
		 */
		@Override public <T> List<T> get(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			return read(r -> r.getJdbcResult().getObject(number_, type));
		}


		@Override protected <T> List<T> get(GetAccessors<T> accessor) throws JdbxException
		{
			Check.notNull(accessor, "accessor");
			return get(accessor.resultForNumber);
		}


		/**
		 * Returns a list of objects.
		 * @param fn a function which can extract the object from a result set
		 * @param <T> the type of the function return value
		 * @return the list
		 */
		public <T> List<T> get(GetForNumber<ResultSet,T> fn) throws JdbxException
		{
			Check.notNull(fn, "fn");
			return read(r -> fn.get(r.getJdbcResult(), number_));
		}


		private final int number_;
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
		@Override public <T> List<T> get(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			return read(r -> r.getJdbcResult().getObject(name_, type));
		}


		/**
		 * Returns a list of objects.
		 * @param fn a function which can extract the object from a result set
		 * @param <T> the type of the function return value
		 * @return the list
		 */
		public <T> List<T> get(GetForName<ResultSet,T> fn) throws JdbxException
		{
			Check.notNull(fn, "fn");
			return read(r -> fn.get(r.getJdbcResult(), name_));
		}


		@Override protected <T> List<T> get(GetAccessors<T> accessor) throws JdbxException
		{
			Check.notNull(accessor, "accessor");
			return get(accessor.resultForName);
		}


		private final String name_;
	}


	private final Query query_;
	private int max_ = -1; // unlimited
}
