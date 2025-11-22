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
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.Unchecked;


/**
 * StaticStmt allows to execute a static, parameterless SQL command.
 * It wraps java.sql.Statement.
 */
public class StaticStmt extends Stmt
{
	/**
	 * Creates a new StaticStmt.
	 * Calls StaticStmt(con, false).
	 * @param con a connection
	 */
	public StaticStmt(Connection con)
	{
		this(con, false);
	}


	/**
	 * Creates a new StaticStmt using the given connection.
	 * @param con a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public StaticStmt(Connection con, boolean closeCon)
	{
		super(con, closeCon);
	}


	/**
	 * Creates a new StaticStmt. It uses a connection obtained from the datasource
	 * and closes that connection when itself is closed.
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


	@Override protected void checkInitialized() throws JdbxException
	{
		checkOpen();
	}


	/**
	 * Returns the internal java.sql.Statement.
	 */
	@Override public Statement getJdbcStmt() throws JdbxException
	{
		if (jdbcStmt_ == null)
		{
			checkOpen();
			try
			{
				jdbcStmt_ = createJdbcStmt();
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}
		return jdbcStmt_;
	}


	private Statement createJdbcStmt() throws Exception
	{
		if (options_ == null)
			return con_.createStatement();
		else
		{
			Statement statement = con_.createStatement(
				options_.getResultType().getCode(),
				options_.getResultConcurrency().getCode(),
				options_.getResultHoldability().getCode()
			);
			options_.applyOptionValues(statement);
			return statement;
		}
	}


	//------------------------------
	// init
	//------------------------------


	/**
	 * Returns true if the statement is not closed.
	 * A StaticStmt, once created is always initialized.
	 * @return true if not closed
	 */
	@Override public boolean isInitialized()
	{
		return !isClosed();
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * Returns a Query builder to execute the given SQL query command and process the result.
	 * Note that actual query on JDBC level may not be run until a terminal operation of the
	 * method chain is executed.
	 * @param sql a SQL command
	 * @return the Query object
	 */
	public Query query(String sql)
	{
		Check.notNull(sql, "sql");
		return new StaticQResult(sql);
	}


	private class StaticQResult extends Query
	{
		public StaticQResult(String sql)
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


		@Override protected String describe()
		{
			return sql_;
		}


		private final String sql_;
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
	 * Executes an update operation. It is a shortcut for createUpdate(sql).run().
	 * @param sql a SQL command
	 * @return a UpdateResult containing the number of affected records.
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


		@Override protected long run(boolean large) throws Exception
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
			else if (returnCols_.getNumbers() != null)
				return jdbcStmt.executeUpdate(sql_, returnCols_.getNumbers());
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
			else if (returnCols_.getNumbers() != null)
				return jdbcStmt.executeLargeUpdate(sql_, returnCols_.getNumbers());
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


		@Override protected String describe()
		{
			return sql_;
		}


		private final String sql_;
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
		 * @param returnCols the columns or null if no columns should be returned
		 * @return this
		 */
		@Override public StaticExecute returnCols(ReturnCols returnCols)
		{
			returnCols_ = returnCols;
			return this;
		}


		@Override public ExecuteResult run() throws JdbxException
		{
			try
			{
				Statement stmt = getJdbcStmt();
				boolean hasResultSet;
				if (returnCols_ == null)
					hasResultSet = stmt.execute(sql_);
				else if (returnCols_.getNames() != null)
					hasResultSet = stmt.execute(sql_, returnCols_.getNames());
				else if (returnCols_.getNumbers() != null)
					hasResultSet = stmt.execute(sql_, returnCols_.getNumbers());
				else
					hasResultSet = stmt.execute(sql_, Statement.RETURN_GENERATED_KEYS);
				return new ExecuteResult(stmt, hasResultSet);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override protected String describe()
		{
			return sql_;
		}


		private final String sql_;
		private ReturnCols returnCols_;
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


	/**
	 * Executes a SQL command and returns the result.
	 * It is a shortcut for createExecute(sql).run().
	 * @param sql a SQL command
	 * @return the ExecuteResult
	 */
	public ExecuteResult execute(String sql)
	{
		return createExecute(sql).run();
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
			Unchecked.run(() -> getJdbcStmt().addBatch(sql));
			return this;
		}


		@Override protected Stmt stmt()
		{
			return StaticStmt.this;
		}
	}


	/**
	 * Returns a Batch object which can be used to add statements to the batch and
	 * execute the batch.<p>
	 * Each call will return a new Batch object but still they all operate on the same underlying
	 * JDBC Statement, so you may store the Batch object in a local variable or just call this
	 * method each time you need it.<p>
	 *
	 * @see StaticBatch#add(String)
	 * @return the batch
	 */
	public StaticBatch batch()
	{
		return new StaticBatch();
	}
}
