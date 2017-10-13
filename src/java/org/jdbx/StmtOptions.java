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


import java.sql.Statement;
import org.jdbx.function.CheckedRunnable;


/**
 * StmtOptions holds the options which can be set on a JDBX statement.
 * All operations return or set values on the wrapped java.sql.Statement object.
 * @see Stmt#options()
 */
public class StmtOptions
{
	StmtOptions(Stmt stmt)
	{
		stmt_ = stmt;
	}


	/**
	 * Sets that a Statement is pooled or not pooled.
	 * @param poolable the flag
	 * @return this
	 * @see Statement#setPoolable(boolean)
	 */
	public StmtOptions setPoolable(boolean poolable) throws JdbxException
	{
		stmt_.setBoolean(Statement::setPoolable, poolable);
		return this;
	}


	/**
	 * Sets that a Statement is poolable.
	 * @return the flag
	 * @see Statement#isPoolable
	 */
	public boolean isPoolable() throws JdbxException
	{
		return stmt_.get(Statement::isPoolable).booleanValue();
	}


	/**
	 * Sets the name of the SQL cursor which will be used by subsequent statement execute operations.
	 * @param cursorName the name of the cursor
	 * @return this
	 * @see Statement#setCursorName
	 */
	public StmtOptions setCursorName(String cursorName) throws JdbxException
	{
		CheckedRunnable.unchecked(() -> stmt_.getJdbcStmt().setCursorName(cursorName));
		return this;
	}


	/**
	 * Calls {@link Statement#setMaxFieldSize(int)}.
	 * @param size the maximum number of bytes
	 * @return this
	 */
	public StmtOptions setMaxFieldSize(int size) throws JdbxException
	{
		stmt_.setInt(Statement::setMaxFieldSize, size);
		return this;
	}


	/**
	 * Returns {@link Statement#getMaxFieldSize()}.
	 * @return the maximum size
	 */
	public int getMaxFieldSize() throws JdbxException
	{
		return stmt_.get(Statement::getMaxFieldSize).intValue();
	}


	/**
	 * Calls {@link Statement#setLargeMaxRows(long)}.
	 * @param max the maximum number
	 * @return this
	 */
	public StmtOptions setLargeMaxRows(long max) throws JdbxException
	{
		CheckedRunnable.unchecked(() -> stmt_.getJdbcStmt().setLargeMaxRows(max));
		return this;
	}


	/**
	 * Returns {@link Statement#getLargeMaxRows()}.
	 * @return the maximum number of rows
	 */
	public long getLargeMaxRows() throws JdbxException
	{
		return stmt_.get(Statement::getLargeMaxRows).longValue();
	}


	/**
	 * Calls {@link Statement#setMaxRows(int)}.
	 * @param max the maximum number
	 * @return this
	 */
	public StmtOptions setMaxRows(int max) throws JdbxException
	{
		stmt_.setInt(Statement::setMaxRows, max);
		return this;
	}


	/**
	 * Returns {@link Statement#getMaxRows()}.
	 * @return the maximum number of rows
	 */
	public int getMaxRows() throws JdbxException
	{
		return stmt_.get(Statement::getMaxRows).intValue();
	}


	/**
	 * Calls {@link Statement#setQueryTimeout(int)}.
	 * @param seconds the timeout in seconds
	 * @return this
	 */
	public StmtOptions setQueryTimeout(int seconds) throws JdbxException
	{
		stmt_.setInt(Statement::setQueryTimeout, seconds);
		return this;
	}


	/**
	 * Returns {@link Statement#getQueryTimeout()}.
	 * @return the timeout in seconds
	 */
	public int getQueryTimeout() throws JdbxException
	{
		return stmt_.get(Statement::getQueryTimeout).intValue();
	}


	/**
	 * Calls {@link Statement#setEscapeProcessing(boolean)}.
	 * @param enable the flag
	 * @return this
	 */
	public StmtOptions setEscapeProcessing(boolean enable) throws JdbxException
	{
		stmt_.setBoolean(Statement::setEscapeProcessing, enable);
		return this;
	}


	/**
	 * Calls {@link Statement#setFetchDirection(int)}.
	 * @param direction the fetch direction enum
	 * @return this
	 */
	public StmtOptions setFetchDirection(FetchDirection direction) throws JdbxException
	{
		Check.valid(direction);
		stmt_.setInt(Statement::setFetchDirection, direction.getCode());
		return this;
	}


	/**
	 * Returns {@link Statement#getFetchDirection()} as enum value.
	 * @return the fetch direction
	 */
	public FetchDirection getFetchDirection() throws JdbxException
	{
		return FetchDirection.MAP.forCode(stmt_.get(Statement::getFetchDirection));
	}


	/**
	 * Calls {@link Statement#setFetchSize(int)}.
	 * @param rows the fetch size
	 * @return this
	 */
	public StmtOptions setFetchSize(int rows) throws JdbxException
	{
		stmt_.setInt(Statement::setFetchSize, rows);
		return this;
	}


	/**
	 * Returns {@link Statement#getFetchSize()}.
	 * @return the fetch size
	 */
	public int getFetchSize() throws JdbxException
	{
		return stmt_.get(Statement::getFetchSize).intValue();
	}


	/**
	 * Returns {@link Statement#getResultSetHoldability()} as enum value.
	 * @return the holdability
	 */
	public QResultHoldability getResultHoldability()
	{
		return holdability_;
	}


	/**
	 * Sets the result holdability.
	 * This will reinitialize the internal JDBC statement. 
	 * @return this
	 */
	public StmtOptions setResultHoldability(QResultHoldability value)
	{
		if (changed(holdability_, value))
			holdability_ = value;
		return this;
	}
	

	protected boolean initResultHoldability(QResultHoldability value)
	{
		boolean changed = changed(holdability_, value);
		holdability_ = value;
		return changed;
	}


	/**
	 * Returns {@link Statement#getResultSetConcurrency()} as enum value.
	 * @return the concurrency
	 */
	public QResultConcurrency getResultConcurrency()
	{
		return concurrency_;
	}


	/**
	 * Sets the result concurrency.
	 * This will reinitialize the internal JDBC statement. 
	 * @return this
	 */
	public StmtOptions setResultConcurrency(QResultConcurrency value)
	{
		if (changed(concurrency_, value))
			concurrency_ = value;
		return this;
	}
	

	protected boolean initResultConcurrency(QResultConcurrency value)
	{
		boolean changed = changed(concurrency_, value);
		concurrency_ = value;
		return changed;
	}


	/**
	 * Returns {@link Statement#getResultSetType()} as enum value.
	 * @return the result type
	 */
	public QResultType getResultType()
	{
		return resultSetType_;
	}

	
	/**
	 * Set the {@link Statement#setResultSetType()}.
	 * This will reinitialize the JDBC statement. 
	 * @return this
	 */
	public StmtOptions setResultType(QResultType value)
	{
		if (changed(resultSetType_, value))
			resultSetType_ = value;
		return this;
	}
	

	private boolean changed(JdbcEnum oldValue, JdbcEnum newValue)
	{
		Check.valid(newValue);
		if (oldValue != newValue)
		{
			stmt_.closeJdbcStmt();
			return true;
		}
		else
			return false;
	}


	protected Stmt stmt_;
	private QResultType resultSetType_ = QResultType.FORWARD_ONLY;
	private QResultConcurrency concurrency_ = QResultConcurrency.READ_ONLY;
	private QResultHoldability holdability_ = QResultHoldability.CLOSE_AT_COMMIT;
}
