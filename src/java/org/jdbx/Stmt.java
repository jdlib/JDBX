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
import java.sql.SQLWarning;
import java.sql.Statement;
import javax.sql.DataSource;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.Unchecked;


/**
 * Common base class of {@link StaticStmt}, {@link PrepStmt} and {@link CallStmt}.
 */
public abstract class Stmt implements AutoCloseable
{
	private enum CloseAction
	{
		NOTHING,
		STATEMENT,
		CONNECTION,
	}


	protected Stmt(Connection con, boolean closeCon)
	{
		con_ 			= Check.notNull(con, "connection");
		closeAction_ 	= closeCon ? CloseAction.CONNECTION : CloseAction.STATEMENT;
	}


	protected Stmt(DataSource dataSource) throws JdbxException
	{
		this(Check.notNull(dataSource, "dataSource")::getConnection, true);
	}


	protected Stmt(CheckedSupplier<Connection> supplier, boolean closeCon) throws JdbxException
	{
		this(Unchecked.get(Check.notNull(supplier, "supplier")), closeCon);
	}


	//------------------------------
	// connection
	//------------------------------


	/**
	 * Returns the connection used by the statement.
	 * @return the connection
	 * @throws JdbxException if the statement is already closed.
	 */
	public Connection getConnection() throws JdbxException
	{
		checkOpen();
		return con_;
	}


	void clearCon()
	{
		con_ = null;
	}


	//------------------------------
	// accessors
	//------------------------------


	/**
	 * Returns the internal JDBC statement used by the JDBC statement.
	 * @return the JDBC statement
	 * @throws JdbxException if this statement was already closed or not yet initialized.
	 * @see #isClosed()
	 * @see #isInitialized()
	 */
	public abstract Statement getJdbcStmt() throws JdbxException;
	
	
	//------------------------------
	// state
	//------------------------------


	/**
	 * Returns if the statement is initialized.
	 * When initialized a statement can be used to execute sql commands.
	 * In implementation terms initialized means that the JDBC statement on which this statement operates was created.   
	 * @return is the statement initialized
	 */
	public abstract boolean isInitialized();


	/**
	 * Tests if the statement is initialized, i.e. {@link #getJdbcStmt()} may be called.
	 * @throws JdbxException thrown when the statement is not initialized
	 */
	protected void checkInitialized() throws JdbxException
	{
		checkOpen();
		if (jdbcStmt_ == null)
			throw JdbxException.illegalState("statement not initialized");
	}

	
	/**
	 * Returns if the statement is closed.
	 * @return is the statement closed
	 */
	public final boolean isClosed()
	{
		return con_ == null;
	}


	/**
	 * Tests if the statement is open.
	 * @throws JdbxException thrown when the statement is already closed
	 */
	protected void checkOpen() throws JdbxException
	{
		if (isClosed())
			throw JdbxException.closed();
	}


	protected void closeJdbcStmt() throws JdbxException
	{
		if (jdbcStmt_ != null)
		{
			try
			{
				call(Statement::close);
			}
			finally
			{
				jdbcStmt_ = null;
			}
		}
	}


	/**
	 * Closes the statement. This operation has no effect if already closed.
	 */
	@Override public void close() throws JdbxException
	{
		if (!isClosed())
		{
			try
			{
				if (closeAction_ == CloseAction.CONNECTION)
					con_.close();
				else if ((jdbcStmt_ != null) && (closeAction_ == CloseAction.STATEMENT))
					jdbcStmt_.close();
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
			finally
			{
				con_  	  = null;
				jdbcStmt_ = null;
			}
		}
	}


	//------------------------------
	// options
	//------------------------------


	/**
	 * Returns the statement options. 
	 * @return the options
	 */
	public final StmtOptions options() throws JdbxException
	{
		if (options_ == null)
			options_ = new StmtOptions(this);
		return options_;
	}


	//------------------------------
	// cancel
	//------------------------------


	/**
	 * Cancels execution of the current command.
     * This method can be used by one thread to cancel a statement that
     * is being executed by another thread.
	 */
	public void cancel() throws JdbxException
	{
		call(Statement::cancel);
	}


	//------------------------------
	// warnings
	//------------------------------


	/**
	 * Returns the warnings collected by the statement.
     * @return the first <code>SQLWarning</code> object or <code>null</code>
     * 		if there are no warnings
	 * @see Statement#getWarnings()
	 */
	public SQLWarning getWarnings() throws JdbxException
	{
		checkOpen();
		return jdbcStmt_ != null ? get(Statement::getWarnings) : null;
	}


	/**
	 * Clears the warnings.
	 * @see Statement#clearWarnings()
	 */
	public void clearWarnings() throws JdbxException
	{
		if (jdbcStmt_ != null)
			call(Statement::clearWarnings);
	}


	//------------------------------
	// helpers
	//------------------------------


	@SuppressWarnings("unchecked")
	protected <STMT extends Statement, T> void call(CheckedConsumer<STMT> fn) throws JdbxException
	{
		Unchecked.accept(fn, (STMT)getJdbcStmt());
	}


	@SuppressWarnings("unchecked")
	protected <STMT extends Statement, T> T get(CheckedFunction<STMT,T> fn) throws JdbxException
	{
		return Unchecked.apply(fn, (STMT)getJdbcStmt());
	}


	protected Connection con_;
	protected Statement jdbcStmt_;
	protected StmtOptions options_;
	protected final CloseAction closeAction_;
}
