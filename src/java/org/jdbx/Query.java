package org.jdbx;


import java.sql.*;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;


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


	boolean applySkip(QResultCursor cursor) throws SQLException
	{
		return (skip_ <= 0) || cursor.skip(skip_);
	}


	/**
	 * Executes the query and returns the QResultCursor.
	 * @return the cursor
	 */
	public QResultCursor cursor() throws JdbxException
	{
		try
		{
			ResultSet resultSet = runQuery();
			return new QResultCursor(resultSet);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
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
	 * Returns a builder to access all rows of the result set.
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


	/**
	 * Executes the query and passes the result cursor to the consumer.
	 * @param consumer a result consumer
	 */
	public void read(CheckedConsumer<QResultCursor> consumer) throws JdbxException
	{
		read(result -> {
			consumer.accept(result);
			return null;
		});
	}


	/**
	 * Executes the query and passes the result cursor to the reader.
	 * @param reader a reader which can return a value from a result cursor
	 * @param <T> the type of the value returned by the reader
	 * @return the value returned by the reader.
	 */
	public <T> T read(CheckedFunction<QResultCursor,T> reader) throws JdbxException
	{
		return read(skip_ > 0, reader);
	}



	/**
	 * Implementation method to read the result using a reader function.
	 * We allow callers of this method to decide if they want to apply skipping themselves:
	 * If skipping is done here, the reader may invoke QResultCursor.next()
	 * after an unsuccessful prior call to this method - unfortunately in this case
	 * a JDBC driver is allowed to throw an exception instead of returning false.
	 */
	<R> R read(boolean applySkip, CheckedFunction<QResultCursor,R> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");

		Exception e1 = null, e2 = null;
		R returnValue = null;

		try (QResultCursor cursor = new QResultCursor(runQuery()))
		{
			if (applySkip)
				applySkip(cursor);
			returnValue = reader.apply(cursor);
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
			throw JdbxException.combine(e1, e2);
		return returnValue;
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

