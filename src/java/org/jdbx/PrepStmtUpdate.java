package org.jdbx;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.jdbx.function.CheckedSupplier;


class PrepStmtUpdate extends Update
{
	public PrepStmtUpdate(CheckedSupplier<PreparedStatement> supplier)
	{
		supplier_ = supplier;
	}


	@Override protected long runUpdateImpl(boolean large) throws Exception
	{
		PreparedStatement pstmt = supplier_.get();
		return large ?
			pstmt.executeLargeUpdate() :
			pstmt.executeUpdate();
	}


	@Override protected ResultSet getGeneratedKeys() throws Exception
	{
		return supplier_.get().getGeneratedKeys();
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
