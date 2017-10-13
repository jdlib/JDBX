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


/**
 * Options allows to set or get options of JDBX statements.
 * @see Stmt#options()
 */
public class Options
{
	Options(Stmt stmt)
	{
		stmt_ = stmt;
		resetResultOptions();
	}


	/**
	 * Sets that a Statement is pooled or not pooled.
	 * @param poolable the flag
	 * @return this
	 * @see Statement#setPoolable(boolean)
	 */
	public Options setPoolable(boolean value) throws JdbxException
	{
		set(Option.POOLABLE, Boolean.valueOf(value));
		return this;
	}


	/**
	 * Sets that a Statement is poolable.
	 * @return the flag
	 * @see Statement#isPoolable
	 */
	public boolean isPoolable() throws JdbxException
	{
		return get(Option.POOLABLE).booleanValue();
	}


	/**
	 * Sets the name of the SQL cursor which will be used by subsequent statement execute operations.
	 * @param cursorName the name of the cursor
	 * @return this
	 * @see Statement#setCursorName
	 */
	public Options setCursorName(String value) throws JdbxException
	{
		set(Option.CURSORNAME, value);
		return this;
	}


	/**
	 * Calls {@link Statement#setMaxFieldSize(int)}.
	 * @param size the maximum number of bytes
	 * @return this
	 */
	public Options setMaxFieldSize(int bytes) throws JdbxException
	{
		set(Option.MAXFIELDSIZE, Integer.valueOf(bytes));
		return this;
	}


	/**
	 * Returns {@link Statement#getMaxFieldSize()}.
	 * @return the maximum size
	 */
	public int getMaxFieldSize() throws JdbxException
	{
		return get(Option.MAXFIELDSIZE).intValue();
	}


	/**
	 * Calls {@link Statement#setLargeMaxRows(long)}.
	 * @param size the maximum number of bytes
	 * @return this
	 */
	public Options setLargeMaxRows(long bytes) throws JdbxException
	{
		set(Option.LARGEMAXROWS, Long.valueOf(bytes));
		return this;
	}


	/**
	 * Returns {@link Statement#getLargeMaxRows()}.
	 * @return the maximum number of rows
	 */
	public long getLargeMaxRows() throws JdbxException
	{
		return get(Option.LARGEMAXROWS).longValue();
	}


	/**
	 * Calls {@link Statement#setMaxRows(int)}.
	 * @param max the maximum number
	 * @return this
	 */
	public Options setMaxRows(int value) throws JdbxException
	{
		set(Option.MAXROWS, Integer.valueOf(value));
		return this;
	}


	/**
	 * Returns {@link Statement#getMaxRows()}.
	 * @return the maximum number of rows
	 */
	public int getMaxRows() throws JdbxException
	{
		return get(Option.MAXROWS).intValue();
	}


	/**
	 * Calls {@link Statement#setQueryTimeout(int)}.
	 * @param seconds the timeout in seconds
	 * @return this
	 */
	public Options setQueryTimeout(int seconds) throws JdbxException
	{
		set(Option.QUERYTIMEOUT, Integer.valueOf(seconds));
		return this;
	}


	/**
	 * Returns {@link Statement#getQueryTimeout()}.
	 * @return the timeout in seconds
	 */
	public int getQueryTimeout() throws JdbxException
	{
		return get(Option.QUERYTIMEOUT).intValue();
	}


	/**
	 * Calls {@link Statement#setEscapeProcessing(boolean)}.
	 * @param enable the flag
	 * @return this
	 */
	public Options setEscapeProcessing(boolean enable) throws JdbxException
	{
		set(Option.ESCAPEPROCESSING, Boolean.valueOf(enable));
		return this;
	}


	/**
	 * Calls {@link Statement#setFetchDirection(int)}.
	 * @param direction the fetch direction enum
	 * @return this
	 */
	public Options setFetchDirection(FetchDirection direction) throws JdbxException
	{
		Check.valid(direction);
		set(Option.FETCHDIRECTION, Integer.valueOf(direction.getCode()));
		return this;
	}


	/**
	 * Returns {@link Statement#getFetchDirection()} as enum value.
	 * @return the fetch direction
	 */
	public FetchDirection getFetchDirection() throws JdbxException
	{
		return FetchDirection.MAP.forCode(get(Option.FETCHDIRECTION));
	}


	/**
	 * Calls {@link Statement#setFetchSize(int)}.
	 * @param rows the fetch size
	 * @return this
	 */
	public Options setFetchSize(int rows) throws JdbxException
	{
		set(Option.FETCHSIZE, Integer.valueOf(rows));
		return this;
	}


	/**
	 * Returns {@link Statement#getFetchSize()}.
	 * @return the fetch size
	 */
	public int getFetchSize() throws JdbxException
	{
		return get(Option.FETCHSIZE).intValue();
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
	public Options setResultHoldability(QResultHoldability value)
	{
		if (changed(holdability_, value))
			holdability_ = value;
		return this;
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
	public Options setResultConcurrency(QResultConcurrency value)
	{
		if (changed(concurrency_, value))
			concurrency_ = value;
		return this;
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
	public Options setResultType(QResultType value)
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
	
	
	private <T> void set(Option<T> option, T value)
	{
		headValue_ = OptionValue.set(headValue_, option, value);
		if (stmt_.hasJdbcStmt())
		{
			try
			{
				option.setter.accept(stmt_.getJdbcStmt(), value);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}
	}
	
	
	private <T> T get(Option<T> option)
	{
		T value = OptionValue.get(headValue_, option);
		if (value == null)
		{
			try
			{
				value = option.getter.apply(stmt_.getJdbcStmt());
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}
		return value;
	}
	
	
	private void resetResultOptions()
	{
		resultSetType_ 	= QResultType.FORWARD_ONLY;
		concurrency_ 	= QResultConcurrency.READ_ONLY;
		holdability_ 	= QResultHoldability.CLOSE_AT_COMMIT;
	}
	
	
	/**
	 * Resets the options to its defaults.
	 */
	public Options reset()
	{
		resetResultOptions();
		headValue_ = null;
		stmt_.closeJdbcStmt();
		return this;
	}
	
	
	void apply(Statement statement) throws Exception
	{
		if (headValue_ != null)
			headValue_.apply(statement);
	}

	
	private final Stmt stmt_;
	private QResultType resultSetType_;
	private QResultConcurrency concurrency_;
	private QResultHoldability holdability_;
	private OptionValue<?> headValue_;
}