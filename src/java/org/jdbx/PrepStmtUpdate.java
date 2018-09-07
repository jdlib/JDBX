package org.jdbx;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdbx.function.CheckedSupplier;


class PrepStmtUpdate extends Update
{
	public PrepStmtUpdate(CheckedSupplier<PreparedStatement> stmtSupplier)
	{
		stmtSupplier_ = stmtSupplier;
	}


	@Override protected long runUpdateImpl(boolean large) throws Exception
	{
		PreparedStatement pstmt = stmtSupplier_.get();
		return large ?
			pstmt.executeLargeUpdate() :
			pstmt.executeUpdate();
	}


	@Override protected ResultSet getGeneratedKeys() throws Exception
	{
		return stmtSupplier_.get().getGeneratedKeys();
	}


	@Override protected void cleanup() throws Exception
	{
	}


	@Override protected String toDescription()
	{
		return stmtSupplier_.toString();
	}


	private final CheckedSupplier<PreparedStatement> stmtSupplier_;
}
