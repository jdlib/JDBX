package org.jdbx;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.CheckedRunnable;


/**
 * ExecuteResult represents the result of executing a SQL command
 * which can produce multiple results.
 * Usage:
 * <pre><code>
 * ExecuteResult result = ...;
 * while (result.next()) {
 *     if (result.isQuery()) {
 *     	   Query q = result.getQuery(); ...
 *     }
 *     else {
 *         int count = result.getUpdateCount(); ...
 *     }
 * }
 * </code></pre>
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


	ExecuteResult(Statement stmt, boolean hasQuery)
	{
		stmt_ 		= Check.notNull(stmt, "stmt");
		hasQuery_	= hasQuery;
		status_		= Status.BEFORE_FIRST;
	}


	private void initNext(boolean hasQuery) throws JdbxException
	{
		if (hasQuery_ = hasQuery)
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
				CheckedRunnable.unchecked(() -> updateCount_ = stmt_.getUpdateCount());
			}
			status_ = updateCount_ != -1L ? Status.HAS_UPDATERESULT : Status.AFTER_LAST;
		}
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


	/**
	 * Returns if the current result is an update result.
	 * @return true if the current result provides an update result
	 * @throws JdbxException if next() has not been called yet or if position after the last result
	 */
	public boolean isUpdate() throws JdbxException
	{
		checkHasResult();
		return status_ == Status.HAS_UPDATERESULT;
	}


	/**
	 * Returns if the current result provides query result.
	 * @return true if the current result provides a query result
	 * @throws JdbxException if next() has not been called yet or if position after the last result
	 */
	public boolean isQuery() throws JdbxException
	{
		checkHasResult();
		return status_ == Status.HAS_QUERYRESULT;
	}


	private void checkIsUpdate() throws JdbxException
	{
		if (!isUpdate())
			throw JdbxException.illegalState("current result is not an update result");
	}


	private void checkIsQuery() throws JdbxException
	{
		if (!isQuery())
			throw JdbxException.illegalState("current result is not a query result");
	}


	/**
	 * Returns the update count of the current update result.
	 * @return the update count
	 * @throws JdbxException if the current result is not an {@link #isUpdate() update result}
	 */
	public int getUpdateCount() throws JdbxException
	{
		checkIsUpdate();
		return (int)updateCount_;
	}


	/**
	 * Returns the (large) update count of the current update result as long.
	 * @return the update count
	 * @throws JdbxException if the current result is not an {@link #isUpdate() update result}
	 */
	public long getLargeUpdateCount() throws JdbxException
	{
		checkIsUpdate();
		return updateCount_;
	}


	/**
	 * Returns the current result as query.
	 * @return the query
	 * @throws JdbxException if the current result is not an {@link #isQuery() result set}
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public Query getQuery() throws JdbxException
	{
		checkIsQuery();
		ResultSet resultSet = CheckedFunction.unchecked(Statement::getResultSet, stmt_);
		return Query.of(resultSet);
	}


	/**
	 * Returns the generated keys of the current result.
	 * @return the result set containing the generated keys
	 * @throws JdbxException if next() has not been called yet or if position after the last result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public ResultSet getGeneratedKeys() throws JdbxException, SQLException
	{
		checkHasResult();
		return stmt_.getGeneratedKeys();
	}


	/**
	 * Returns the generated keys of the current result wrapped in a Query object.
	 * @return the query
	 * @throws JdbxException if next() has not been called yet or if position after the last result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public Query queryGeneratedKeys() throws JdbxException, SQLException
	{
		checkHasResult();
		return Query.of(getGeneratedKeys());
	}


	/**
	 * Moves to the next result. All open result sets are closed.
	 * @return true if there is a next result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean next() throws JdbxException
	{
		return next(Current.CLOSE_ALL_RESULTS);
	}


	/**
	 * Moves to the next result.
	 * @param current determines what happens to previously obtained ResultSets
	 * @return true if there is a next result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean next(Current current) throws JdbxException
	{
		checkNotLast();
		Check.valid(current);
		if (status_ == Status.BEFORE_FIRST)
			initNext(hasQuery_);
		else
		{
			try
			{
				initNext(stmt_.getMoreResults(current.getCode()));
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}
		return (status_ == Status.HAS_QUERYRESULT) || (status_ == Status.HAS_UPDATERESULT);
	}


	/**
	 * Moves to the next result until the result is a query result.
	 * @return true if moved to a result which is a ResultSet
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean nextQuery() throws JdbxException, SQLException
	{
		while (next())
		{
			if (isQuery())
				return true;
		}
		return false;
	}


	/**
	 * Moves to the next result until the result is an update result.
	 * @return true if moved to a result which is an update result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean nextUpdate() throws JdbxException, SQLException
	{
		while (next())
		{
			if (isUpdate())
				return true;
		}
		return false;
	}


	private Statement stmt_;
	private boolean hasQuery_;
	private long updateCount_;
	private Status status_ = Status.BEFORE_FIRST;
}
