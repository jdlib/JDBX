package org.jdbx;


import java.sql.Connection;
import java.sql.ResultSet;


/**
 * Jdbx allows you to perform simple queries and updates
 * without the need to explicitly create a statement.
 */
public class Jdbx
{
	/**
	 * Runs a query.
	 * @param con a connection
	 * @param sql a SQL command
	 * @param params zero or more parameters
	 * @return the Query
	 */
	public static QueryResult query(Connection con, String sql, Object... params)
	{
		Check.notNull(con, "con");
		Check.notNull(sql, "sql");
		return new FastQuery(new StmtProvider(con, sql, params));
	}


	/**
	 * Creates a Update.
	 * @param con a connection
	 * @param sql a SQL command
	 * @param params zero or more parameters
	 * @return the Update
	 */
	public static Update createUpdate(Connection con, String sql, Object... params)
	{
		Check.notNull(con, "con");
		Check.notNull(sql, "sql");
		return new FastUpdate(new StmtProvider(con, sql, params));
	}


	/**
	 * Executes a Update.
	 * @param con a connection
	 * @param sql a SQL command
	 * @param params zero or more parameters
	 * @return the update count
	 */
	public static UpdateResult<Void> update(Connection con, String sql, Object... params) throws JdbxException
	{
		return createUpdate(con, sql, params).run();
	}


	private static class StmtProvider
	{
		public StmtProvider(Connection con, String sql, Object[] params)
		{
			con_ 	= con;
			sql_ 	= sql;
			params_	= (params != null) && (params.length > 0) ? params : null;
		}


		private void checkNotUsed()
		{
			if ((con_ == null) || (stmt_ != null))
				throw new IllegalStateException("already used");
		}


		public Update createUpdate() throws JdbxException
		{
			checkNotUsed();
			if (params_ == null)
			{
				stmt_ = new StaticStmt(con_);
				return ((StaticStmt)stmt_).createUpdate(sql_);
			}
			else
			{
				stmt_ = new PrepStmt(con_);
				return ((PrepStmt)stmt_).init(sql_).params(params_).createUpdate();
			}
		}


		public QueryResult query() throws JdbxException
		{
			checkNotUsed();
			if (params_ == null)
			{
				stmt_ = new StaticStmt(con_);
				return ((StaticStmt)stmt_).query(sql_);
			}
			else
			{
				stmt_ = new PrepStmt(con_);
				return ((PrepStmt)stmt_).init(sql_).params(params_).query();
			}
		}


		public void cleanup() throws Exception
		{
			try
			{
				if (stmt_ != null)
					stmt_.close();
			}
			finally
			{
				con_ = null;
				stmt_ = null;
			}
		}


		@Override public String toString()
		{
			return sql_;
		}


		private Connection con_;
		private String sql_;
		private Object[] params_;
		private Stmt stmt_;
	}


	private static class FastQuery extends QueryResult
	{
		public FastQuery(StmtProvider provider)
		{
			provider_ = provider;
		}


		@Override protected ResultSet runQueryImpl() throws Exception
		{
			return provider_.query().runQuery();
		}


		@Override protected void cleanup() throws Exception
		{
			provider_.cleanup();
		}


		@Override protected String toDescription()
		{
			return provider_.toString();
		}


		private final StmtProvider provider_;
	}


	private static class FastUpdate extends Update
	{
		public FastUpdate(StmtProvider provider)
		{
			provider_ = provider;
		}


		@Override protected long runUpdateImpl(boolean large) throws Exception
		{
			update_ = provider_.createUpdate();
			return update_.runUpdate(large);
		}


		@Override protected ResultSet getGeneratedKeys() throws Exception
		{
			return update_.getGeneratedKeys();
		}


		@Override protected void cleanup() throws Exception
		{
			update_ = null;
			provider_.cleanup();
		}


		@Override protected String toDescription()
		{
			return provider_.toString();
		}


		private final StmtProvider provider_;
		private Update update_;
	}
}
