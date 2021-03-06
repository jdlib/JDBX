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
import java.util.ArrayList;
import javax.sql.DataSource;
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.Unchecked;


/**
 * MultiStmt allows to create multiple Stmt instances.
 * It keeps track of these statements and when closed
 * automatically also closes the statements.
 */
public class MultiStmt implements AutoCloseable
{
	/**
	 * Creates a new MultiStmt.
	 * @param con a connection
	 */
	public MultiStmt(Connection con)
	{
		this(con, false);
	}


	/**
	 * Creates a new MultiStmt. It uses the given connection.
	 * @param con a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public MultiStmt(Connection con, boolean closeCon)
	{
		con_ 		= Check.notNull(con, "connection");
		closeCon_	= closeCon;
	}


	/**
	 * Creates a new MultiStmt. It uses a connection obtained from the datasource
	 * and closes the connection when itself is closed.
	 * @param dataSource a DataSource
	 */
	public MultiStmt(DataSource dataSource)
	{
		this(Check.notNull(dataSource, "dataSource")::getConnection, true);
	}


	/**
	 * Creates a new StaticStmt. It uses a connection obtained from the connection supplier
	 * @param supplier provides a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public MultiStmt(CheckedSupplier<Connection> supplier, boolean closeCon)
	{
		conSupplier_ = Check.notNull(supplier, "supplier");
		closeCon_ 	 = closeCon;
	}


	/**
	 * Returns the connection used by the MultiStmt.
	 * @return the connection
	 */
	public Connection getConnection() throws JdbxException
	{
		checkOpen();
		if (con_ == null)
			con_ = Unchecked.get(conSupplier_);
		return con_;
	}


	//------------------------------
	// factory
	//------------------------------


	/**
	 * Returns a new StaticStmt.
	 * @return the statement
	 */
	public StaticStmt newStaticStmt() throws JdbxException
	{
		return add(new StaticStmt(getConnection(), false));
	}


	/**
	 * Returns a new PrepStmt.
	 * @return the statement
	 */
	public PrepStmt newPrepStmt() throws JdbxException
	{
		return add(new PrepStmt(getConnection(), false));
	}


	/**
	 * Returns a new initialized PrepStmt.
	 * @param sql a SQL command
	 * @return the statement
	 */
	public PrepStmt newPrepStmt(String sql) throws JdbxException
	{
		return newPrepStmt().init(sql);
	}

	
	/**
	 * Returns a new CallStmt.
	 * @return the statement
	 */
	public CallStmt newCallStmt() throws JdbxException
	{
		return add(new CallStmt(getConnection(), false));
	}


	/**
	 * Returns a new initialized CallStmt.
	 * @param sql a SQL command
	 * @return the statement
	 */
	public CallStmt newCallStmt(String sql) throws JdbxException
	{
		return newCallStmt().init(sql);
	}

	
	/**
	 * Returns the number of statements stored by this MultiStmt.
	 * @return the number
	 */
	public int size()
	{
		return statements_.size();
	}


	private <S extends Stmt> S add(S stmt)
	{
		checkOpen();
		statements_.add(stmt);
		return stmt;
	}


	//------------------------------
	// open/closed state
	//------------------------------


	/**
	 * Returns if the MultiStmt is closed.
	 * @return the closed state
	 */
	public final boolean isClosed()
	{
		return isClosed_;
	}


	protected void checkOpen() throws JdbxException
	{
		if (isClosed())
			throw JdbxException.closed();
	}


	/**
	 * Closes all statement held by this MultiStmt.
	 */
	public void closeStmts() throws JdbxException
	{
		if (!isClosed())
		{
			JdbxException first = null;

			for (Stmt stmt : statements_)
			{
				try
				{
					stmt.close();
				}
				catch (JdbxException e)
				{
					if (first == null)
						first = e;
					else
						first.addSuppressed(e);
				}
			}

			statements_.clear();

			if (first != null)
				throw first;
		}
	}


	/**
	 * Closes this MultiStmt.
	 */
	@Override public void close() throws JdbxException
	{
		if (!isClosed())
		{
			try
			{
				if (!closeCon_)
					closeStmts();
				else if (con_ != null)
				{
					for (Stmt stmt : statements_)
						stmt.clearCon();
					con_.close();
				}
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
			finally
			{
				isClosed_		= true;
				con_  			= null;
				conSupplier_ 	= null;
				statements_.clear();
			}
		}
	}


	private Connection con_;
	private CheckedSupplier<Connection> conSupplier_;
	private boolean closeCon_;
	private boolean isClosed_;
	private final ArrayList<Stmt> statements_ = new ArrayList<>();
}
