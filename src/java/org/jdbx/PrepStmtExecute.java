package org.jdbx;


import java.sql.PreparedStatement;
import org.jdbx.function.CheckedSupplier;


class PrepStmtExecute extends Execute
{
	public PrepStmtExecute(CheckedSupplier<PreparedStatement> supplier)
	{
		supplier_ = supplier;
	}


	@Override public ExecuteResult run() throws JdbxException
	{
		try
		{
			PreparedStatement pstmt = supplier_.get();
			boolean hasResultSet    = pstmt.execute();
			return new ExecuteResult(pstmt, hasResultSet);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	@Override protected String toDescription()
	{
		return supplier_.toString();
	}


	private CheckedSupplier<PreparedStatement> supplier_;
}
