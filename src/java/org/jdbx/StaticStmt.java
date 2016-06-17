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
	public StaticStmt(DataSource dataSource) throws JdbException
	{
		super(dataSource);
	}


	/**
	 * Creates a new StaticStmt. It uses a connection obtained from the connection supplier
	 * @param supplier provides a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public StaticStmt(CheckedSupplier<Connection> supplier, boolean closeCon) throws JdbException
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


	@Override protected void checkInitialized() throws JdbException
	{
		checkOpen();
	}


	/**
	 * Returns the internal java.sql.Statement.
	 */
	@Override public Statement getJdbcStmt() throws JdbException
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
			throw JdbException.of(e);
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
	 * @throws JdbException if closed
	 */
	public Init init() throws JdbException
	{
		checkOpen();
		return new Init();
	}


	/**
	 * A Builder to initialize the Stmt.
	 */
	public class Init extends InitBase<Init>
	{
		private Init()
		{
			super(options_);
		}


		@Override protected void optionsChanged() throws JdbException
		{
			checkOpen();
			updateOptions(options()); // will create options if not yet done
			closeStmt();
		}
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * Creates a query to execute the given SQL query command.
	 * @param sql a SQL command
	 * @return the query
	 */
	public Query createQuery(String sql)
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


		@Override protected void cleanup() throws JdbException
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
	public StaticUpdate createUpdate(String sql) throws JdbException
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
	public int update(String sql) throws JdbException
	{
		return createUpdate(sql).run();
	}


	/**
	 * An Update implementation for StaticStmt.
	 */
	public class StaticUpdate extends Update implements AutoKeys.Builder<StaticUpdate>
	{
		private StaticUpdate(String sql)
		{
			sql_ = sql;
		}


		/**
		 * Sets the AutoKeys object which should be used.
		 * @param autoKeys the autoKeys or null if no auto generated keys should be returned
		 * @return this
		 */
		@Override public StaticUpdate reportAutoKeys(AutoKeys autoKeys)
		{
			autoKeys_ = autoKeys;
			return this;
		}


		@Override protected long runUpdateImpl(boolean large) throws Exception
		{
			return large ? runLargeUpdate() : runNormalUpdate();
		}


		private int runNormalUpdate() throws Exception
		{
			Statement jdbcStmt = getJdbcStmt();
			if (autoKeys_ == null)
				return jdbcStmt.executeUpdate(sql_);
			else if (autoKeys_.getColNames() != null)
				return jdbcStmt.executeUpdate(sql_, autoKeys_.getColNames());
			else if (autoKeys_.getColIndexes() != null)
				return jdbcStmt.executeUpdate(sql_, autoKeys_.getColIndexes());
			else
				return jdbcStmt.executeUpdate(sql_, Statement.RETURN_GENERATED_KEYS);
		}


		private long runLargeUpdate() throws Exception
		{
			Statement jdbcStmt = getJdbcStmt();
			if (autoKeys_ == null)
				return jdbcStmt.executeLargeUpdate(sql_);
			else if (autoKeys_.getColNames() != null)
				return jdbcStmt.executeLargeUpdate(sql_, autoKeys_.getColNames());
			else if (autoKeys_.getColIndexes() != null)
				return jdbcStmt.executeLargeUpdate(sql_, autoKeys_.getColIndexes());
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
		private AutoKeys autoKeys_;
	}


	//------------------------------
	// execute
	//------------------------------


	/**
	 * An Execute implementation for StaticStmt.
	 */
	public class StaticExecute extends Execute implements AutoKeys.Builder<StaticExecute>
	{
		private StaticExecute(String sql)
		{
			sql_ = sql;
		}


		/**
		 * Sets the AutoKeys object which should be used.
		 * @param autoKeys the autoKeys or null if no auto generated keys should be returned
		 * @return this
		 */
		@Override public StaticExecute reportAutoKeys(AutoKeys autoKeys)
		{
			autoKeys_ = autoKeys;
			return this;
		}


		@Override public ExecuteResult run() throws JdbException
		{
			try
			{
				Statement stmt = getJdbcStmt();
				boolean hasResultSet;
				if (autoKeys_ == null)
					hasResultSet = stmt.execute(sql_);
				else if (autoKeys_.getColNames() != null)
					hasResultSet = stmt.execute(sql_, autoKeys_.getColNames());
				else if (autoKeys_.getColIndexes() != null)
					hasResultSet = stmt.execute(sql_, autoKeys_.getColIndexes());
				else
					hasResultSet = stmt.execute(sql_, Statement.RETURN_GENERATED_KEYS);
				return new ExecuteResult(stmt, hasResultSet);
			}
			catch (Exception e)
			{
				throw JdbException.of(e);
			}
		}


		@Override protected String toDescription()
		{
			return sql_;
		}


		private String sql_;
		private AutoKeys autoKeys_;
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
		public StaticBatch add(String sql) throws JdbException
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
