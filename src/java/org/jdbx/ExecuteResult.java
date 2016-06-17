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
 *     if (result.hasResultSet()) {
 *     	   ResultSet rs = result.getResultSet(); ...
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
		HAS_RESULTSET,
		HAS_UPDATECOUNT,
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


	ExecuteResult(Statement stmt, boolean hasResultSet)
	{
		stmt_ 			= Check.notNull(stmt, "stmt");
		hasResultSet_	= hasResultSet;
		status_			= Status.BEFORE_FIRST;
	}


	private void initNext(boolean hasResultSet) throws JdbException
	{
		if (hasResultSet_ = hasResultSet)
		{
			status_ = Status.HAS_RESULTSET;
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
			status_ = updateCount_ != -1L ? Status.HAS_UPDATECOUNT : Status.AFTER_LAST;
		}
	}


	private void checkHasResult() throws JdbException
	{
		if (status_ == Status.BEFORE_FIRST)
			throw JdbException.illegalState("next() has not been called");
		else
			checkNotLast();
	}


	private void checkNotLast() throws JdbException
	{
		if (status_ == Status.AFTER_LAST)
			throw JdbException.illegalState("no more results");
	}


	/**
	 * Returns if the current result provides an update count.
	 * @return true if the current result provides an update count
	 * @throws JdbException if next() has not been called yet or if position after the last result
	 */
	public boolean isUpdateCount() throws JdbException
	{
		checkHasResult();
		return status_ == Status.HAS_UPDATECOUNT;
	}


	/**
	 * Returns if the current result provides a result set.
	 * @return true if the current result provides an update count
	 * @throws JdbException if next() has not been called yet or if position after the last result
	 */
	public boolean isResultSet() throws JdbException
	{
		checkHasResult();
		return status_ == Status.HAS_RESULTSET;
	}


	private void checkIsUpdateCount() throws JdbException
	{
		if (!isUpdateCount())
			throw JdbException.illegalState("current result is not an update count");
	}


	private void checkIsResultSet() throws JdbException
	{
		if (!isResultSet())
			throw JdbException.illegalState("current result is not a resultset");
	}


	/**
	 * Returns the update count of the current result.
	 * @return the update count
	 * @throws JdbException if the current result is not an {@link #isUpdateCount() update count}
	 */
	public int getUpdateCount() throws JdbException
	{
		checkIsUpdateCount();
		return (int)updateCount_;
	}


	/**
	 * Returns the (large) update count of the current result as long.
	 * @return the update count
	 * @throws JdbException if the current result is not an {@link #isUpdateCount() update count}
	 */
	public long getLargeUpdateCount() throws JdbException
	{
		checkIsUpdateCount();
		return updateCount_;
	}


	/**
	 * Returns the ResultSet of the current result.
	 * @return the result set
	 * @throws JdbException if the current result is not an {@link #isResultSet() result set}
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public ResultSet getResultSet() throws JdbException
	{
		checkIsResultSet();
		return  CheckedFunction.unchecked(Statement::getResultSet, stmt_);
	}


	/**
	 * Returns the ResultSet of the current result as Query object.
	 * @return the query
	 * @throws JdbException if the current result is not an {@link #isResultSet() result set}
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public Query queryResult() throws JdbException, SQLException
	{
		return Query.of(getResultSet());
	}


	/**
	 * Returns the generated keys of the current result.
	 * @return the result set containing the generated keys
	 * @throws JdbException if next() has not been called yet or if position after the last result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public ResultSet getGeneratedKeys() throws JdbException, SQLException
	{
		checkHasResult();
		return stmt_.getGeneratedKeys();
	}


	/**
	 * Returns the generated keys of the current result wrapped in a Query object.
	 * @return the query
	 * @throws JdbException if next() has not been called yet or if position after the last result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public Query queryGeneratedKeys() throws JdbException, SQLException
	{
		checkHasResult();
		return Query.of(getGeneratedKeys());
	}


	/**
	 * Moves to the next result. All open result sets are closed.
	 * @return true if there is a next result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean next() throws JdbException
	{
		return next(Current.CLOSE_ALL_RESULTS);
	}


	/**
	 * Moves to the next result.
	 * @param current determines what happens to previously obtained ResultSets
	 * @return true if there is a next result
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean next(Current current) throws JdbException
	{
		checkNotLast();
		Check.valid(current);
		if (status_ == Status.BEFORE_FIRST)
			initNext(hasResultSet_);
		else
		{
			try
			{
				initNext(stmt_.getMoreResults(current.getCode()));
			}
			catch (SQLException e)
			{
				throw JdbException.of(e);
			}
		}
		return (status_ == Status.HAS_RESULTSET) || (status_ == Status.HAS_UPDATECOUNT);
	}


	/**
	 * Moves to the next result until the result provides a ResultSet.
	 * @return true if moved to a result which is a ResultSet
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean nextResultSet() throws JdbException, SQLException
	{
		while (next())
		{
			if (isResultSet())
				return true;
		}
		return false;
	}


	/**
	 * Moves to the next result until the result provides an update count.
	 * @return true if moved to a result which provides an update count
	 * @throws SQLException if the JDBC operation throws a SQLException
	 */
	public boolean nextUpdateCount() throws JdbException, SQLException
	{
		while (next())
		{
			if (isUpdateCount())
				return true;
		}
		return false;
	}


	private Statement stmt_;
	private boolean hasResultSet_;
	private long updateCount_;
	private Status status_ = Status.BEFORE_FIRST;
}
