package org.jdbx;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdbx.function.CheckedSupplier;


class PrepStmtQueryResult extends QueryResult
{
	public PrepStmtQueryResult(CheckedSupplier<PreparedStatement> supplier)
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


	private CheckedSupplier<PreparedStatement> supplier_;
}
