package org.jdbx;


import java.sql.ResultSet;
import java.sql.Statement;
import org.jdbx.function.GetReturnCols;
import org.jdbx.function.Unchecked;


/**
 * ExecuteResult represents the result of executing a SQL command
 * which can produce multiple results.
 * Usage:
 * <pre><code>
 * ExecuteResult result = ...;
 * while (result.next()) {
 *     if (result.isQueryResult()) {
 *     	   QueryResult qr = result.getQueryResult(); 
 *         // process query result
 *     }
 *     else {
 *         UpdateResult<Void> ur = result.getUpdateResult();
 *         // process update result
 *     }
 * }
 * </code></pre>
 * @see Execute#run()
 * @see StaticStmt#execute(String)
 * @see PrepStmt#execute()
 * @see CallStmt#execute()
 */
public class ExecuteResult
{
	private enum Status
	{
		BEFORE_FIRST,
		HAS_QUERYRESULT,
		HAS_UPDATERESULT,
		AFTER_LAST
	}


	/**
	 * An Enum for the constants which can be passed to
	 * {@link Statement#getMoreResults(int)}.
	 */
	public enum Current implements JdbcEnum
	{
		CLOSE_CUURENT_RESULT(Statement.CLOSE_CURRENT_RESULT),
		KEEP_CURRENT_RESULT(Statement.KEEP_CURRENT_RESULT),
		CLOSE_ALL_RESULTS(Statement.CLOSE_ALL_RESULTS);


		Current(int code)
		{
			code_ = code;
		}


		@Override public int getCode()
		{
			return code_;
		}


		private final int code_;
	}


	ExecuteResult(Statement stmt, boolean hasQueryResult)
	{
		stmt_ 			= Check.notNull(stmt, "stmt");
		hasQueryResult_	= hasQueryResult;
	}

	
	//-------------------------------
	// navigation
	//-------------------------------
	

	private void initNext(boolean hasQueryResult) throws JdbxException
	{
		if (hasQueryResult_ = hasQueryResult)
		{
			status_ = Status.HAS_QUERYRESULT;
			updateCount_ = -1;
		}
		else
		{
			try
			{
				updateCount_ = stmt_.getLargeUpdateCount();
			}
			catch (Exception e)
			{
				Unchecked.run(() -> updateCount_ = stmt_.getUpdateCount());
			}
			status_ = updateCount_ != -1L ? Status.HAS_UPDATERESULT : Status.AFTER_LAST;
		}
	}


	/**
	 * Moves to the next result. All open result sets are closed.
	 * @return true if there is a next result
	 */
	public boolean next() throws JdbxException
	{
		return next(Current.CLOSE_ALL_RESULTS);
	}


	/**
	 * Moves to the next result.
	 * @param current determines what happens to previously obtained ResultSets
	 * @return true if there is a next result
	 */
	public boolean next(Current current) throws JdbxException
	{
		checkNotLast();
		Check.valid(current, "current");
		if (status_ == Status.BEFORE_FIRST)
			initNext(hasQueryResult_);
		else
		{
			try
			{
				initNext(stmt_.getMoreResults(current.getCode()));
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}
		return (status_ == Status.HAS_QUERYRESULT) || (status_ == Status.HAS_UPDATERESULT);
	}


	/**
	 * Moves to the next result until the result is a query result.
	 * @return true if moved to a result which is a query result
	 */
	public boolean nextQueryResult() throws JdbxException
	{
		while (next())
		{
			if (isQueryResult())
				return true;
		}
		return false;
	}


	/**
	 * Moves to the next result until the result is an update result.
	 * @return true if moved to a result which is an update result
	 */
	public boolean nextUpdateResult() throws JdbxException
	{
		while (next())
		{
			if (isUpdateResult())
				return true;
		}
		return false;
	}


	private void checkHasResult() throws JdbxException
	{
		if (status_ == Status.BEFORE_FIRST)
			throw JdbxException.illegalState("next() has not been called");
		checkNotLast();
	}


	private void checkNotLast() throws JdbxException
	{
		if (status_ == Status.AFTER_LAST)
			throw JdbxException.illegalState("no more results");
	}
	
	
	//-------------------------------
	// update results
	//-------------------------------


	/**
	 * Returns if the current result is an update result.
	 * @return true if the current result provides an update result
	 * @throws JdbxException if next() has not been called yet or if position after the last result
	 */
	public boolean isUpdateResult() throws JdbxException
	{
		checkHasResult();
		return status_ == Status.HAS_UPDATERESULT;
	}


	private void checkIsUpdateResult() throws JdbxException
	{
		if (!isUpdateResult())
			throw JdbxException.illegalState("current result is not an update result");
	}

	
	public UpdateResult<Void> getUpdateResult() throws JdbxException
	{
		checkIsUpdateResult();
		return new UpdateResult<>(updateCount_);
	}
	

	public <V> UpdateResult<V> getUpdateResult(Class<V> colType) throws JdbxException
	{
		Check.notNull(colType, "colType");
		return getUpdateResult((c,q) -> q.row().col().get(colType));
	}


	public <V> UpdateResult<V> getUpdateResult(GetReturnCols<V> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");
		checkIsUpdateResult();

		try
		{
			try (ResultSet rs = stmt_.getGeneratedKeys()) 
			{ 
				V value = reader.read(updateCount_, QueryResult.of(rs));
				return new UpdateResult<>(updateCount_, value);
			}
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}
	
	
	//-------------------------------
	// query results
	//-------------------------------


	/**
	 * Returns if the current result provides a query result.
	 * @return true if the current result provides a query result
	 * @throws JdbxException if next() has not been called yet or if position after the last result
	 */
	public boolean isQueryResult() throws JdbxException
	{
		checkHasResult();
		return status_ == Status.HAS_QUERYRESULT;
	}


	private void checkIsQueryResult() throws JdbxException
	{
		if (!isQueryResult())
			throw JdbxException.illegalState("current result is not a query result");
	}


	/**
	 * Returns the current result as query result.
	 * @return the query result
	 * @throws JdbxException if the current result is not an {@link #isQueryResult() query result}
	 */
	public QueryResult getQueryResult() throws JdbxException
	{
		checkIsQueryResult();
		ResultSet resultSet = Unchecked.apply(Statement::getResultSet, stmt_);
		return QueryResult.of(resultSet);
	}


	private Statement stmt_;
	private boolean hasQueryResult_;
	private long updateCount_;
	private Status status_ = Status.BEFORE_FIRST;
}
