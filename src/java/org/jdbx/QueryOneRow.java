package org.jdbx;


import java.sql.ResultSet;
import java.util.Map;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.GetForIndex;
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
	 * Instructs the query to throw an Exception
	 * if the result is empty.
	 * @return this
	 */
	public QueryOneRow required()
	{
		required_ = true;
		return this;
	}


	/**
	 * Instructs the query to throw an Exception
	 * if the result contains more than one row.
	 * @return this
	 */
	public QueryOneRow unique()
	{
		unique_ = true;
		return this;
	}


	/**
	 * Calls the reader function for the first result row.
	 * @param reader receives a result and returns a value
	 * @param <T> the type of the value returned by the reader
	 * @return the value returned by the reader. If the result is empty, null is returned
	 */
	public <T> T read(CheckedFunction<QueryResult,T> reader) throws JdbxException
	{
		return read(reader, null);
	}


	/**
	 * Calls the ResultSet reader for the first result row.
	 * @param reader receives a result and returns a value
	 * @param emptyValue the return value if the result is empty
	 * @param <T> the type of the value returned by the reader
	 * @return the value returned by the reader, or emptyValue if the result is empty
	 */
	public <T> T read(CheckedFunction<QueryResult,T> reader, T emptyValue) throws JdbxException
	{
		Check.notNull(reader, "reader");
		return query_.read(false, result -> {
			if (query_.applySkip(result) && result.next())
			{
				T value = reader.apply(result);
				if (unique_ && result.next())
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
	 * Reads the first row and returns a map of it values. The column names
	 * are the keys in the map.
	 * @return the map or null if the result is empty.
	 */
	public Map<String,Object> map() throws JdbxException
	{
		return read(ResultUtil::readMap);
	}


	/**
	 * Reads the first row and returns a map of it values. The column names
	 * are the keys in the map.
	 * @param colNames the names of the columns which should be returned
	 * @return the map or null if the result is empty.
	 */
	public Map<String,Object> map(String... colNames) throws JdbxException
	{
		Check.notNull(colNames, "column names");
		return read(r -> ResultUtil.readMap(r.getJdbcResult(), colNames));
	}


	/**
	 * Reads the first row and returns its values as array.
	 * @return the array or null if the result is empty.
	 */
	public Object[] cols() throws JdbxException
	{
		return read(ResultUtil::readValues);
	}


	/**
	 * Reads the first row and returns its values as array.
	 * @param indexes the indexes of the columns to read
	 * @return the array or null if the result is empty.
	 */
	public Object[] cols(int... indexes) throws JdbxException
	{
		Check.notNull(indexes, "indexes");
		return read(r -> ResultUtil.readValues(r.getJdbcResult(), indexes));
	}


	/**
	 * Reads the first row and returns its values as array.
	 * @param names the names of the columns to read
	 * @return the array or null if the result is empty.
	 */
	public Object[] cols(String... names) throws JdbxException
	{
		Check.notNull(names, "names");
		return read(r -> ResultUtil.readValues(r.getJdbcResult(), names));
	}


	/**
	 * Returns a column object for the first column.
	 * @return the column
	 */
	public IndexedCol col()
	{
		return col(1);
	}


	/**
	 * Returns a column object for the n-th column.
	 * @param index the column index, starting at 1.
	 * @return the column
	 */
	public IndexedCol col(int index)
	{
		return new IndexedCol(index);
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
	 * A Column implementation for columns accessed by index.
	 */
	public class IndexedCol implements GetResult
	{
		private IndexedCol(int index)
		{
			index_ = Check.index(index);
		}


		/**
		 * Returns an object.
		 * @param type the object type
		 * @return the object
		 */
		@Override public <T> T get(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			return read(r -> r.getJdbcResult().getObject(index_, type));
		}


		/**
		 * Returns an object.
		 * @param map contains mapping from SQL type names to Java classes
		 * @return the object
		 */
		@Override public Object get(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			return read(r -> r.getJdbcResult().getObject(index_, map));
		}


		/**
		 * Returns an object.
		 * @param fn a function which can extract the object from a result set
		 * @param <T> the type of the value returned by the function
		 * @return the object
		 */
		public <T> T get(GetForIndex<ResultSet,T> fn) throws JdbxException
		{
			Check.notNull(fn, "fn");
			return read(r -> fn.get(r.getJdbcResult(), index_));
		}


		/**
		 * Returns an object for an accessor.
		 * @return the object
		 */
		@Override public <T> T get(GetAccessors<T> accessor) throws JdbxException
		{
			Check.notNull(accessor, "accessor");
			return get(accessor.resultForIndex);
		}


		private int index_;
	}


	/**
	 * A Column implementation for columns accessed by name.
	 */
	public class NamedCol implements GetResult
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
		@Override public <T> T get(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			return read(r -> r.getJdbcResult().getObject(name_, type));
		}


		/**
		 * Returns an object.
		 * @param map contains mapping from SQL type names to Java classes
		 * @return the object
		 */
		@Override public Object get(Map<String,Class<?>> map) throws JdbxException
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


	private Query query_;
	private boolean required_;
	private boolean unique_;
}
