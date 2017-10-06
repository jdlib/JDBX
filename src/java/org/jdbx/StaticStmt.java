/*
 * Copyright (C) 2016 JDBX
 * 
 * https://github.com/jdlib/JDBX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbx;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.jdbx.function.CheckedRunnable;
import org.jdbx.function.CheckedSupplier;


/**
 * StaticStmt allows to execute a static, parameterless SQL command.
 * It wraps java.sql.Statement.
 */
public class StaticStmt extends Stmt
{
	/**
	 * Creates a new StaticStmt. It uses a connection obtained from the datasource
	 * and closes the connection when itself is closed.
	 * @param dataSource a DataSource
	 */
	public StaticStmt(DataSource dataSource) throws JdbxException
	{
		super(dataSource);
	}


	/**
	 * Creates a new StaticStmt. It uses a connection obtained from the connection supplier
	 * @param supplier provides a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public StaticStmt(CheckedSupplier<Connection> supplier, boolean closeCon) throws JdbxException
	{
		super(supplier, closeCon);
	}


	/**
	 * Creates a new StaticStmt. Calls StaticStmt(con, false).
	 * @param con a connection
	 */
	public StaticStmt(Connection con)
	{
		this(con, false);
	}


	/**
	 * Creates a new StaticStmt. It uses the given connection.
	 * @param con a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public StaticStmt(Connection con, boolean closeCon)
	{
		super(con, closeCon);
	}


	@Override protected void checkInitialized() throws JdbxException
	{
		checkOpen();
	}


	/**
	 * Returns the internal java.sql.Statement.
	 */
	@Override public Statement getJdbcStmt() throws JdbxException
	{
		checkOpen();
		try
		{
			if (stmt_ == null)
			{
				if (options_ == null)
					stmt_ = con_.createStatement();
				else
				{
					stmt_ = con_.createStatement(
						options_.getResultType().getCode(),
						options_.getResultConcurrency().getCode(),
						options_.getResultHoldability().getCode()
					);
				}
			}
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
		return stmt_;
	}


	/**
	 * Returns true iif the statement is not closed.
	 * @return true if not closed
	 */
	@Override public boolean isInitialized()
	{
		return !isClosed();
	}


	//------------------------------
	// init
	//------------------------------


	/**
	 * Returns a builder to initialize the statement holdability, concurrency and resultset type.
	 * @return a init builder
	 * @throws JdbxException if closed
	 */
	public Init init() throws JdbxException
	{
		checkOpen();
		return new Init();
	}


	/**
	 * A Builder to initialize the StaticStmt.
	 */
	public class Init extends InitBase<Init>
	{
		private Init()
		{
			super(options_);
		}


		@Override protected void optionsChanged() throws JdbxException
		{
			checkOpen();
			updateOptions(options()); // will create options if not yet done
			closeStmt(); // will force recreate of jdbc statement with new options
		}
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * Returns a Query builder to execute the given SQL query command.
	 * @param sql a SQL command
	 * @return the Query
	 */
	public Query query(String sql)
	{
		Check.notNull(sql, "sql");
		return new StaticQuery(sql);
	}


	private class StaticQuery extends Query
	{
		public StaticQuery(String sql)
		{
			sql_ = sql;
		}


		@Override protected ResultSet runQueryImpl() throws Exception
		{
			return getJdbcStmt().executeQuery(sql_);
		}


		@Override protected void cleanup() throws JdbxException
		{
		}


		@Override protected String toDescription()
		{
			return sql_;
		}


		private String sql_;
	}


	//------------------------------
	// update
	//------------------------------


	/**
	 * Creates an Update object to perform an update operation.
	 * @param sql a SQL command
	 * @return an update object
	 */
	public StaticUpdate createUpdate(String sql) throws JdbxException
	{
		Check.notNull(sql, "sql");
		checkOpen();
		return new StaticUpdate(sql);
	}


	/**
	 * Executes an update operation.
	 * @param sql a SQL command
	 * @return the number of affected records.
	 */
	public UpdateResult<Void> update(String sql) throws JdbxException
	{
		return createUpdate(sql).run();
	}


	/**
	 * An Update implementation for StaticStmt.
	 */
	public class StaticUpdate extends Update implements ReturnCols.Builder<StaticUpdate>
	{
		private StaticUpdate(String sql)
		{
			sql_ = sql;
		}


		/**
		 * Defines which columns should be returned for INSERTs.
		 * @param cols the columns or null if no columns should be returned
		 * @return this
		 */
		@Override public StaticUpdate returnCols(ReturnCols cols)
		{
			returnCols_ = cols;
			return this;
		}


		@Override protected long runUpdateImpl(boolean large) throws Exception
		{
			return large ? runLargeUpdate() : runNormalUpdate();
		}


		private int runNormalUpdate() throws Exception
		{
			Statement jdbcStmt = getJdbcStmt();
			if (returnCols_ == null)
				return jdbcStmt.executeUpdate(sql_);
			else if (returnCols_.getNames() != null)
				return jdbcStmt.executeUpdate(sql_, returnCols_.getNames());
			else if (returnCols_.getIndexes() != null)
				return jdbcStmt.executeUpdate(sql_, returnCols_.getIndexes());
			else
				return jdbcStmt.executeUpdate(sql_, Statement.RETURN_GENERATED_KEYS);
		}


		private long runLargeUpdate() throws Exception
		{
			Statement jdbcStmt = getJdbcStmt();
			if (returnCols_ == null)
				return jdbcStmt.executeLargeUpdate(sql_);
			else if (returnCols_.getNames() != null)
				return jdbcStmt.executeLargeUpdate(sql_, returnCols_.getNames());
			else if (returnCols_.getIndexes() != null)
				return jdbcStmt.executeLargeUpdate(sql_, returnCols_.getIndexes());
			else
				return jdbcStmt.executeLargeUpdate(sql_, Statement.RETURN_GENERATED_KEYS);
		}


		@Override protected ResultSet getGeneratedKeys() throws Exception
		{
			return getJdbcStmt().getGeneratedKeys();
		}


		@Override protected void cleanup() throws Exception
		{
		}


		@Override protected String toDescription()
		{
			return sql_;
		}


		private String sql_;
		private ReturnCols returnCols_;
	}


	//------------------------------
	// execute
	//------------------------------


	/**
	 * An Execute implementation for StaticStmt.
	 */
	public class StaticExecute extends Execute implements ReturnCols.Builder<StaticExecute>
	{
		private StaticExecute(String sql)
		{
			sql_ = sql;
		}


		/**
		 * Defines which columns should be returned for INSERTs.
		 * @param cols the columns or null if no columns should be returned
		 * @return this
		 */
		@Override public StaticExecute returnCols(ReturnCols autoKeys)
		{
			autoKeys_ = autoKeys;
			return this;
		}


		@Override public ExecuteResult run() throws JdbxException
		{
			try
			{
				Statement stmt = getJdbcStmt();
				boolean hasResultSet;
				if (autoKeys_ == null)
					hasResultSet = stmt.execute(sql_);
				else if (autoKeys_.getNames() != null)
					hasResultSet = stmt.execute(sql_, autoKeys_.getNames());
				else if (autoKeys_.getIndexes() != null)
					hasResultSet = stmt.execute(sql_, autoKeys_.getIndexes());
				else
					hasResultSet = stmt.execute(sql_, Statement.RETURN_GENERATED_KEYS);
				return new ExecuteResult(stmt, hasResultSet);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override protected String toDescription()
		{
			return sql_;
		}


		private String sql_;
		private ReturnCols autoKeys_;
	}


	/**
	 * Returns a Execute object which can be used to execute commands which
	 * return multiple results
	 * @param sql a SQL command
	 * @return the Execute object
	 */
	public StaticExecute createExecute(String sql)
	{
		Check.notNull(sql, "sql");
		return new StaticExecute(sql);
	}


	//------------------------------
	// batch
	//------------------------------


	/**
	 * A Batch implementation for StaticStmt.
	 */
	public class StaticBatch extends Batch
	{
		/**
		 * Adds a SQL command to the batch.
		 * @param sql a SQL command
		 * @return this
		 */
		public StaticBatch add(String sql) throws JdbxException
		{
			Check.notNull(sql, "sql");
			CheckedRunnable.unchecked(() -> getJdbcStmt().addBatch(sql));
			return this;
		}


		@Override protected Stmt stmt()
		{
			return StaticStmt.this;
		}
	}


	/**
	 * Returns a Batch object which can be used to add statements to the batch and
	 * execute the batch.
	 * @return the batch
	 */
	public StaticBatch batch()
	{
		return new StaticBatch();
	}
}
