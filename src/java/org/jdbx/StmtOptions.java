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
import java.util.ArrayList;
import java.util.List;
import org.jdbx.function.Unchecked;


/**
 * StmtOptions allows to retrieve or set options of a JDBX statement.
 * @see Stmt#options()
 */
public class StmtOptions
{
	static final ResultType DEFAULT_RESULT_TYPE 	= ResultType .FORWARD_ONLY;
	static final Concurrency DEFAULT_CONCURRENCY 	= Concurrency.READ_ONLY;
	static final Holdability DEFAULT_HOLDABILITY 	= Holdability.CLOSE_AT_COMMIT;
	
	
	StmtOptions(Stmt stmt)
	{
		stmt_ = stmt;
	}


	/**
	 * Sets that the statement is pooled or not pooled.
	 * @param poolable the poolable flag
	 * @return this
	 * @see Statement#setPoolable(boolean)
	 */
	public StmtOptions setPoolable(boolean poolable) throws JdbxException
	{
		set(StmtOption.POOLABLE, Boolean.valueOf(poolable));
		return this;
	}


	/**
	 * Returns if a statement is poolable.
	 * @return the poolable flag
	 * @see Statement#isPoolable
	 */
	public boolean isPoolable() throws JdbxException
	{
		return get(StmtOption.POOLABLE).booleanValue();
	}


	/**
	 * Sets the name of the SQL cursor which will be used by subsequent statement operations.
	 * @param cursorName the name of the cursor
	 * @return this
	 * @see Statement#setCursorName
	 */
	public StmtOptions setCursorName(String cursorName) throws JdbxException
	{
		set(StmtOption.CURSORNAME, cursorName);
		return this;
	}


	/**
	 * Sets the limit for the maximum number of bytes that can be returned for
     * character and binary column values in a result produced by this statement.
	 * @param bytes the maximum number of bytes
	 * @see Statement#setMaxFieldSize(int)
	 * @return this
	 */
	public StmtOptions setMaxFieldBytes(int bytes) throws JdbxException
	{
		set(StmtOption.MAXFIELDSIZE, Integer.valueOf(bytes));
		return this;
	}


    /**
     * Returns the maximum number of bytes that can be
     * returned for character and binary column values in a result.
	 * @return the maximum size in bytes
	 * @see Statement#getMaxFieldSize()
	 */
	public int getMaxFieldBytes() throws JdbxException
	{
		return get(StmtOption.MAXFIELDSIZE).intValue();
	}


	/**
	 * Sets the limit for the maximum number of rows that any result generated by this statement 
	 * can contain to the given number. If the limit is exceeded, the excess rows are silently dropped. 
	 * @param count the maximum number
	 * @return this
	 * @see Statement#setLargeMaxRows(long)
	 */
	public StmtOptions setLargeMaxRows(long count) throws JdbxException
	{
		set(StmtOption.LARGEMAXROWS, Long.valueOf(count));
		return this;
	}


	/**
	 * Returns the limit for the maximum number of rows that any result generated by this statement 
	 * can contain. 
	 * @return the maximum number of rows. Zero means there is no limit.
	 * @see Statement#getLargeMaxRows()
	 */
	public long getLargeMaxRows() throws JdbxException
	{
		return get(StmtOption.LARGEMAXROWS).longValue();
	}


	/**
	 * Calls {@link Statement#setMaxRows(int)}.
	 * @param count the maximum number
	 * @return this
	 */
	public StmtOptions setMaxRows(int count) throws JdbxException
	{
		set(StmtOption.MAXROWS, Integer.valueOf(count));
		return this;
	}


	/**
	 * Returns {@link Statement#getMaxRows()}.
	 * @return the maximum number of rows
	 */
	public int getMaxRows() throws JdbxException
	{
		return get(StmtOption.MAXROWS).intValue();
	}


	/**
	 * Calls {@link Statement#setQueryTimeout(int)}.
	 * @param seconds the timeout in seconds
	 * @return this
	 */
	public StmtOptions setQueryTimeoutSeconds(int seconds) throws JdbxException
	{
		set(StmtOption.QUERYTIMEOUT, Integer.valueOf(seconds));
		return this;
	}


	/**
	 * Returns {@link Statement#getQueryTimeout()}.
	 * @return the timeout in seconds
	 */
	public int getQueryTimeoutSeconds() throws JdbxException
	{
		return get(StmtOption.QUERYTIMEOUT).intValue();
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
		Check.valid(direction, "direction");
		set(StmtOption.FETCHDIRECTION, Integer.valueOf(direction.getCode()));
		return this;
	}


	/**
	 * Returns {@link Statement#getFetchDirection()} as enum value.
	 * @return the fetch direction
	 */
	public FetchDirection getFetchDirection() throws JdbxException
	{
		return FetchDirection.MAP.forCode(get(StmtOption.FETCHDIRECTION));
	}


	/**
	 * Calls {@link Statement#setFetchSize(int)}.
	 * @param rows the fetch size
	 * @return this
	 */
	public StmtOptions setFetchRows(int rows) throws JdbxException
	{
		set(StmtOption.FETCHSIZE, Integer.valueOf(rows));
		return this;
	}


	/**
	 * Returns {@link Statement#getFetchSize()}.
	 * @return the fetch size
	 */
	public int getFetchRows() throws JdbxException
	{
		return stmt_.get(Statement::getFetchSize).intValue();
	}


	/**
	 * Returns {@link Statement#isCloseOnCompletion()}
	 * @return the flag
	 */
	public boolean isCloseOnCompletion() throws JdbxException
	{
		return get(StmtOption.CLOSEONCOMPLETION).booleanValue();
	}


	/**
	 * Calls {@link Statement#closeOnCompletion()}
	 * @return this
	 */
	public StmtOptions setCloseOnCompletion() throws JdbxException
	{
		stmt_.call(Statement::closeOnCompletion);
		return this;
	}


	static Holdability getResultHoldability(StmtOptions options)
	{
		return options != null ? options.getResultHoldability() : DEFAULT_HOLDABILITY;
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


	/**
	 * Sets the result set holdability.
	 * This will close the current JDBC statement associated with the JDBX statement.
	 * @param value the holdability
	 * @return this
	 */
	public StmtOptions setResultHoldability(Holdability value)
	{
		if (holdability_ != value)
		{
			holdability_ = Check.valid(value, "holdability");
			stmt_.closeJdbcStmt();
		}
		return this;
	}


	static Concurrency getResultConcurrency(StmtOptions options)
	{
		return options != null ? options.getResultConcurrency() : DEFAULT_CONCURRENCY;
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
	
	
	/**
	 * Sets the result set concurrency.
	 * This will close the current JDBC statement associated with the JDBX statement.
	 * @param value the concurrency
	 * @return this
	 */
	public StmtOptions setResultConcurrency(Concurrency value)
	{
		if (concurrency_ != value)
		{
			concurrency_ = Check.valid(value, "concurrency");
			stmt_.closeJdbcStmt();
		}
		return this;
	}


	static ResultType getResultType(StmtOptions options)
	{
		return options != null ? options.getResultType() : DEFAULT_RESULT_TYPE;
	}


	/**
	 * Returns {@link Statement#getResultSetType()} as enum value.
	 * You can set the result type when you initialize the JDBX statement.
	 * @return the result type
	 */
	public ResultType getResultType()
	{
		return resultType_;
	}


	/**
	 * Sets the result type.
	 * This will close the current JDBC statement associated with the JDBX statement.
	 * @param value the result type 
	 * @return this
	 */
	public StmtOptions setResultType(ResultType value)
	{
		if (resultType_ != value)
		{
			resultType_ = Check.valid(value, "resultType");
			stmt_.closeJdbcStmt();
		}
		return this;
	}
	
	
	private <T> void set(StmtOption<T> option, T value)
	{
		stmt_.checkOpen();
		getOptionValue(option, true).set(value);
		if (stmt_.jdbcStmt_ != null)
			Unchecked.accept(option.setter, stmt_.getJdbcStmt(), value);
	}


	private <T> T get(StmtOption<T> option)
	{
		stmt_.checkOpen();
		
		// if we have a statement use that to retrieve the option value
		if (stmt_.jdbcStmt_ != null) 
			return Unchecked.apply(option.getter, stmt_.jdbcStmt_);
		
		// if the option was explicitly set return that value;
		OptionValue<T> v = getOptionValue(option, false);
		if (v != null)
			return v.value;
		
		// fallback to the default value. Unfortunately some options
		// are implementation dependent, and this can throw an exception
		return option.getDefaultValue();
	}

	
	/**
	 * Apply all explicitly set options to the statement.
	 * @param statement
	 */
	<T> void applyOptionValues(Statement statement)
	{
		if (optionValues_ != null)
		{
			for (OptionValue<?> v : optionValues_)
			{
				@SuppressWarnings("unchecked")
				OptionValue<T> tv = (OptionValue<T>)v; 
				Unchecked.accept(tv.option.setter, statement, tv.value);
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private <T> OptionValue<T> getOptionValue(StmtOption<T> option, boolean create)
	{
		if (optionValues_ != null)
		{
			for (OptionValue<?> v : optionValues_)
			{
				if (v.option == option)
					return (OptionValue<T>)v;
			}
		}
		return create ? createOptionValue(option) : null;
	}
	
	
	private <T> OptionValue<T> createOptionValue(StmtOption<T> option)
	{
		if (optionValues_ == null)
			optionValues_ = new ArrayList<>(3);
		OptionValue<T> v = new OptionValue<>(option);
		optionValues_.add(v);
		return v;
	}
	
	
	private static class OptionValue<T>
	{
		public OptionValue(StmtOption<T> option)
		{
			this.option = option;
		}
		
		
		public OptionValue<T> set(T value)
		{
			this.value = value;
			return this;
		}
		
		
		public final StmtOption<T> option;
		public T value;
	}

	
	private final Stmt stmt_;
	private ResultType resultType_ 		= DEFAULT_RESULT_TYPE;
	private Concurrency concurrency_ 	= DEFAULT_CONCURRENCY;
	private Holdability holdability_ 	= DEFAULT_HOLDABILITY;
	private List<OptionValue<?>> optionValues_;
}
