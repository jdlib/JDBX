package org.jdbx;


import java.sql.*;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.CheckedSupplier;


/**
 * Query is a builder class to configure and run a SQL command
 * which returns a result-set.
 */
public abstract class Query extends StmtRunnable
{
	/**
	 * Returns a Query object for an existing ResultSet.
	 * @param resultSet a ResultSet
	 * @return the Query
	 */
	public static Query of(ResultSet resultSet)
	{
		return new ResultSetQuery(resultSet);
	}


	/**
	 * Instructs the query to skip the first n rows.
	 * @param n a number of rows
	 * @return this
	 */
	public Query skip(int n)
	{
		skip_ = Math.max(0, n);
		return this;
	}


	int getSkip()
	{
		return skip_;
	}


	boolean applySkip(ResultSet result) throws SQLException
	{
		if (skip_ > 0)
		{
			int count = 0;
			while (count < skip_)
			{
				if (!result.next())
					return false;
				count++;
			}
		}
		return true;
	}


	/**
	 * Executes the query and returns a QueryResult.
	 * @return the result
	 */
	public QueryResult result() throws JdbException
	{
		return new QueryResult(resultSet());
	}


	/**
	 * Executes the query and returns a ResultSet.
	 * @return the result
	 */
	public ResultSet resultSet() throws JdbException
	{
		return CheckedSupplier.unchecked(this::runQuery);
	}


	/**
	 * Executes the query and passes the result-set to the consumer.
	 * @param consumer a result set consumer
	 */
	public void read(CheckedConsumer<ResultSet> consumer) throws JdbException
	{
		read(rs -> {
			consumer.accept(rs);
			return null;
		});
	}


	/**
	 * Executes the query and passes the result-set to the reader.
	 * @param reader a reader which can return a value from a result set
	 * @param <T> the type of the value returned by the reader
	 * @return the value returned by the reader.
	 */
	public <T> T read(CheckedFunction<ResultSet,T> reader) throws JdbException
	{
		return read0(skip_ > 0, reader);
	}



	/**
	 * Implementation method for #read(ResultReader<T> reader).
	 * We allow callers to decide if they want to apply skipping themselves:
	 * If skipping is done here, the ResultReader may invoke ResultSet.next()
	 * after an unsuccessful prior call to this method - unfortunately in this case
	 * a JDBC driver is allowed to throw an exceptions instead of returning false.
	 */
	<R> R read0(boolean applySkip, CheckedFunction<ResultSet,R> reader) throws JdbException
	{
		Check.notNull(reader, "reader");

		Exception e1 = null, e2 = null;
		R returnValue = null;

		try (ResultSet result = runQuery())
		{
			if (applySkip)
				applySkip(result);
			returnValue = reader.apply(result);
		}
		catch (Exception e)
		{
			e1 = e;
		}
		finally
		{
			try
			{
				cleanup();
			}
			catch (Exception e)
			{
				e2 = e;
			}
		}

		if ((e1 != null) || (e2 != null))
			throw JdbException.combine(e1, e2);
		return returnValue;
	}


	/**
	 * Returns a builder to access the first row of the result set.
	 * @return the builder
	 */
	public QueryOneRow row()
	{
		return new QueryOneRow(this);
	}


	/**
	 * Returns a builder to access the all rows of the result set.
	 * @return the builder
	 */
	public QueryRows rows()
	{
		return rows(Integer.MAX_VALUE);
	}


	/**
	 * Returns a builder to access upto max rows of the result set.
	 * @param max the maximum number of rows
	 * @return the builder
	 */
	public QueryRows rows(int max)
	{
		return new QueryRows(this, max);
	}


	protected final ResultSet runQuery() throws Exception
	{
		registerRun();
		return runQueryImpl();
	}


	/**
	 * Must only be called by {@link #run()}
	 */
	protected abstract ResultSet runQueryImpl() throws Exception;


	protected abstract void cleanup() throws Exception;


	@Override protected final String getRunnableType()
	{
		return "Query";
	}


	private int skip_;
}

