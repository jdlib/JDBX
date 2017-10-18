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
import org.jdbx.function.Unchecked;


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
		set(StmtOption.POOLABLE, Boolean.valueOf(poolable));
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
		Unchecked.run(() -> stmt_.getJdbcStmt().setCursorName(cursorName));
		return this;
	}


	/**
	 * Calls {@link Statement#setMaxFieldSize(int)}.
	 * @param size the maximum number of bytes
	 * @return this
	 */
	public StmtOptions setMaxFieldSize(int size) throws JdbxException
	{
		set(StmtOption.MAXFIELDSIZE, Integer.valueOf(size));
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
		Unchecked.run(() -> stmt_.getJdbcStmt().setLargeMaxRows(max));
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
		set(StmtOption.MAXROWS, Integer.valueOf(max));
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
		set(StmtOption.QUERYTIMEOUT, Integer.valueOf(seconds));
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
		set(StmtOption.ESCAPEPROCESSING, Boolean.valueOf(enable));
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
		set(StmtOption.FETCHDIRECTION, Integer.valueOf(direction.getCode()));
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
		set(StmtOption.FETCHSIZE, Integer.valueOf(rows));
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
	 * You can set the holdability when you initialize the JDBX statement.
	 * @return the holdability
	 */
	public Holdability getResultHoldability()
	{
		return holdability_;
	}


	void setResultHoldability(Holdability value)
	{
		holdability_ = value;
	}


	/**
	 * Returns {@link Statement#getResultSetConcurrency()} as enum value.
	 * You can set the concurrency when you initialize the JDBX statement.
	 * @return the concurrency
	 */
	public Concurrency getResultConcurrency()
	{
		return concurrency_;
	}


	void setResultConcurrency(Concurrency value)
	{
		concurrency_ = value;
	}


	/**
	 * Returns {@link Statement#getResultSetType()} as enum value.
	 * You can set the result type when you initialize the JDBX statement.
	 * @return the result type
	 */
	public ResultType getResultType()
	{
		return resultSetType_;
	}


	void setResultType(ResultType value)
	{
		resultSetType_ = value;
	}
	
	
	private <T> void set(StmtOption<T> option, T value)
	{
		Unchecked.accept(option.setter, stmt_.getJdbcStmt(), value);
	}


	private final Stmt stmt_;
	private ResultType resultSetType_ = ResultType.FORWARD_ONLY;
	private Concurrency concurrency_ = Concurrency.READ_ONLY;
	private Holdability holdability_ = Holdability.CLOSE_AT_COMMIT;
}
