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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.GetForNumber;
import org.jdbx.function.GetForName;


/**
 * QueryOneRow is a builder class to
 * retrieve the single-row result of a query.
 */
public class QueryOneRow
{
	QueryOneRow(Query query)
	{
		query_ = query;
	}


	/**
	 * Instructs this builder to throw a {@link JdbxException} if the result is empty.
	 * @return this
	 */
	public QueryOneRow required()
	{
		required_ = true;
		return this;
	}


	/**
	 * Instructs this builder to throw a {@link JdbxException}
	 * if the result contains more than one row.
	 * @return this
	 */
	public QueryOneRow unique()
	{
		unique_ = true;
		return this;
	}


	/**
	 * Invokes the reader with query result positioned on the first row.
	 * @param reader receives a QueryResult
	 */
	public void consume(CheckedConsumer<QueryResult> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");
		read(qr -> { reader.accept(qr); return null; });
	}


	/**
	 * Calls the reader function for the first result row.
	 * @param reader receives a QueryResult positioned on the first row and returns a value constructed that row
	 * @param <T> the type of the value returned by the reader
	 * @return the value returned by the reader. If the result has no rows, null is returned
	 */
	public <T> T read(CheckedFunction<QueryResult,T> reader) throws JdbxException
	{
		return read(reader, null);
	}


	/**
	 * Calls the reader for the first result row.
	 * @param reader receives a QueryResult positioned on the first row and returns a value constructed from that row.
	 * @param emptyValue the value returned if the result has no rows
	 * @param <T> the type of the value returned by the reader
	 * @return the value returned by the reader, or emptyValue if the result is empty
	 */
	public <T> T read(CheckedFunction<QueryResult,T> reader, T emptyValue) throws JdbxException
	{
		Check.notNull(reader, "reader");
		return query_.read(false, result -> {
			if (query_.applySkip(result) && result.nextRow())
			{
				T value = reader.apply(result);
				if (unique_ && result.nextRow())
					throw JdbxException.invalidResult("query returned more than one row");
				return value;
			}
			else
			{
				if (required_)
					throw JdbxException.invalidResult("query did not return a result");
				return emptyValue;
			}
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


		public Map<String,Object> toMap()
		{
			return read(qr -> ResultUtil.toMap(qr.getJdbcResult(), names_, numbers_));
		}


		public List<Object> toList()
		{
			return Arrays.asList(toArray());
		}


		public Object[] toArray()
		{
			return read(qr -> ResultUtil.toArray(qr.getJdbcResult(), names_, numbers_));
		}


		private final String[] names_;
		private final int[] numbers_;
	}


	/**
	 * Returns a column object for the first column.
	 * @return the column
	 */
	public NumberedCol col()
	{
		return col(1);
	}


	/**
	 * Returns a column object for the n-th column.
	 * @param number the column number, starting at 1.
	 * @return the column
	 */
	public NumberedCol col(int number)
	{
		return new NumberedCol(number);
	}


	/**
	 * Returns a column object for the column with the given name.
	 * @param name the name
	 * @return the column
	 */
	public NamedCol col(String name)
	{
		return new NamedCol(name);
	}


	/**
	 * A Column implementation for columns accessed by column number.
	 */
	public class NumberedCol implements GetResultValue
	{
		private NumberedCol(int number)
		{
			number_ = Check.number(number);
		}


		/**
		 * Returns an object.
		 * @param type the object type
		 * @return the object
		 */
		@Override public <T> T getObject(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			return read(r -> r.getJdbcResult().getObject(number_, type));
		}


		/**
		 * Returns an object.
		 * @param map contains mapping from SQL type names to Java classes
		 * @return the object
		 */
		@Override public Object getObject(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			return read(r -> r.getJdbcResult().getObject(number_, map));
		}


		/**
		 * Returns an object.
		 * @param fn a function which can extract the object from a result set
		 * @param <T> the type of the value returned by the function
		 * @return the object
		 */
		public <T> T get(GetForNumber<ResultSet,T> fn) throws JdbxException
		{
			Check.notNull(fn, "fn");
			return read(r -> fn.get(r.getJdbcResult(), number_));
		}


		/**
		 * Returns an object for an accessor.
		 * @return the object
		 */
		@Override public <T> T get(GetAccessors<T> accessor) throws JdbxException
		{
			Check.notNull(accessor, "accessor");
			return get(accessor.resultForNumber);
		}


		private final int number_;
	}


	/**
	 * A Column implementation for columns accessed by name.
	 */
	public class NamedCol implements GetResultValue
	{
		private NamedCol(String name)
		{
			name_ = Check.name(name);
		}


		/**
		 * Returns an object.
		 * @param type the object type
		 * @return the object
		 */
		@Override public <T> T getObject(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			return read(r -> r.getJdbcResult().getObject(name_, type));
		}


		/**
		 * Returns an object.
		 * @param map contains mapping from SQL type names to Java classes
		 * @return the object
		 */
		@Override public Object getObject(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			return read(r -> r.getJdbcResult().getObject(name_, map));
		}


		/**
		 * Returns an object.
		 * @param fn a function which can extract the object from a result set
		 * @param <T> the type of the result returned by the function
		 * @return the object
		 */
		public <T> T get(GetForName<ResultSet,T> fn) throws JdbxException
		{
			Check.notNull(fn, "fn");
			return read(r -> fn.get(r.getJdbcResult(), name_));
		}


		@Override public <T> T get(GetAccessors<T> accessor) throws JdbxException
		{
			Check.notNull(accessor, "accessor");
			return get(accessor.resultForName);
		}


		private String name_;
	}


	private final Query query_;
	private boolean required_;
	private boolean unique_;
}
