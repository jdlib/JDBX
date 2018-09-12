package org.jdbx;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdbx.function.CheckedSupplier;


/**
 * A QueryResult implementation which is based on a PreparedStatement.
 */
class PrepStmtQResult extends QueryResult
{
	public PrepStmtQResult(CheckedSupplier<PreparedStatement> supplier)
	{
		supplier_ = supplier;
	}


	@Override protected ResultSet runQueryImpl() throws Exception
	{
		return supplier_.get().executeQuery();
	}


	@Override protected void cleanup() throws Exception
	{
	}


	@Override protected String toDescription()
	{
		return supplier_.toString();
	}


	private final CheckedSupplier<PreparedStatement> supplier_;
}
