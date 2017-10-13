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
import org.jdbx.function.CheckedRunnable;
import org.jdbx.function.CheckedSupplier;


/**
 * Common base class of StaticStmt, PrepStmt and CallStmt.
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
		Check.notNull(supplier, "supplier");
		con_ 		 = CheckedSupplier.unchecked(supplier);
		closeAction_ = closeCon ? CloseAction.CONNECTION : CloseAction.STATEMENT;
	}


	/**
	 * Returns the connection used by the Stmt.
	 * @return the connection
	 * @throws JdbxException if the Stmt is already closed.
	 */
	public Connection getConnection() throws JdbxException
	{
		checkOpen();
		return con_;
	}


	/**
	 * Returns if the statement is initialized.
	 * When initialized a statement can be executed.
	 * @return is the statement initialized
	 */
	public abstract boolean isInitialized();


	/**
	 * Returns the internal JDBC statement used by the JDBC statement.
	 * @return the JDBC statement
	 * @throws JdbxException if the statement is not open.
	 */
	public abstract Statement getJdbcStmt() throws JdbxException;


	//------------------------------
	// open/closed state
	//------------------------------


	/**
	 * Returns if the statement is closed.
	 * @return is the statement closed
	 */
	public final boolean isClosed()
	{
		return con_ == null;
	}


	void setClosed()
	{
		con_ = null;
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


	/**
	 * Tests if the statement is prepared, i.e. {@link #getJdbcStmt()} may be called.
	 * @throws JdbxException thrown when the statement is not prepared
	 */
	protected void checkInitialized() throws JdbxException
	{
		checkOpen();
		if (stmt_ == null)
			throw JdbxException.illegalState("no statement prepared");
	}


	protected void closeJdbcStmt() throws JdbxException
	{
		try
		{
			if (stmt_ != null)
				call(Statement::close);
		}
		finally
		{
			stmt_ = null;
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
				else if ((stmt_ != null) && (closeAction_ == CloseAction.STATEMENT))
					stmt_.close();
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
			finally
			{
				con_  = null;
				stmt_ = null;
			}
		}
	}


	/**
	 * Returns {@link Statement#isCloseOnCompletion()}
	 * @return the flag
	 */
	public boolean isCloseOnCompletion() throws JdbxException
	{
		return get(Statement::isCloseOnCompletion).booleanValue();
	}


	/**
	 * Calls {@link Statement#closeOnCompletion()}
	 */
	public void closeOnCompletion() throws JdbxException
	{
		call(Statement::closeOnCompletion);
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
	 */
	public void cancel() throws JdbxException
	{
		call(Statement::cancel);
	}


	//------------------------------
	// warnings
	//------------------------------


	/**
	 * Returns the warnings collected by the JDBC statement.
	 * @return the warnings
	 * @see Statement#getWarnings()
	 */
	public SQLWarning getWarnings() throws JdbxException
	{
		checkOpen();
		return stmt_ != null ? get(Statement::getWarnings) : null;
	}


	/**
	 * Clears the warnings.
	 * @see Statement#clearWarnings()
	 */
	public void clearWarnings() throws JdbxException
	{
		if (stmt_ != null)
			call(Statement::clearWarnings);
	}


	//------------------------------
	// helper
	//------------------------------


	static interface BooleanSetter<STMT extends Statement>
	{
		public void set(Statement stmt, boolean value) throws Exception;
	}


	static interface IntSetter<STMT extends Statement>
	{
		public void set(STMT stmt, int value) throws Exception;
	}


	@SuppressWarnings("unchecked")
	protected <STMT extends Statement, T> void call(CheckedConsumer<STMT> fn) throws JdbxException
	{
		CheckedConsumer.unchecked(fn, (STMT)getJdbcStmt());
	}


	@SuppressWarnings("unchecked")
	protected <STMT extends Statement, T> T get(CheckedFunction<STMT,T> fn) throws JdbxException
	{
		return CheckedFunction.unchecked(fn, (STMT)getJdbcStmt());
	}


	@SuppressWarnings("unchecked")
	protected <STMT extends Statement> void setInt(IntSetter<STMT> fn, int arg) throws JdbxException
	{
		CheckedRunnable.unchecked(() -> fn.set((STMT)getJdbcStmt(), arg));
	}


	protected <STMT extends Statement> void setBoolean(BooleanSetter<STMT> fn, boolean arg) throws JdbxException
	{
		CheckedRunnable.unchecked(() -> fn.set(getJdbcStmt(), arg));
	}


	/**
	 * Returns a descriptive string.
	 */
	@Override public String toString()
	{
		return stmt_ != null ? stmt_.toString() : "<stmt closed>";
	}


	protected Connection con_;
	protected Statement stmt_;
	protected CloseAction closeAction_;
	protected StmtOptions options_;
}
