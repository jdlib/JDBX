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
import org.jdbx.function.CheckedBiConsumer;
import org.jdbx.function.CheckedFunction;


class StmtOption<T>
{
	public static final StmtOption<Boolean> CLOSEONCOMPLETION	= new StmtOption<>("CloseOnCompletion", Boolean.FALSE, null, Statement::isCloseOnCompletion);
	public static final StmtOption<String> 	CURSORNAME 			= new StmtOption<>("CursorName", null, Statement::setCursorName, null);
	public static final StmtOption<Boolean> ESCAPEPROCESSING	= new StmtOption<>("EscapeProcessing", Boolean.TRUE, Statement::setEscapeProcessing, null);
	public static final StmtOption<Integer> FETCHDIRECTION		= new StmtOption<>("FetchDirection", Integer.valueOf(FetchDirection.UNKNOWN.getCode()), Statement::setFetchDirection, Statement::getFetchDirection);
	public static final StmtOption<Integer> FETCHSIZE			= new StmtOption<>("FetchSize", null, Statement::setFetchSize, Statement::getFetchSize);
	public static final StmtOption<Long>    LARGEMAXROWS		= new StmtOption<>("LargeMaxRows", null, Statement::setLargeMaxRows, Statement::getLargeMaxRows);
	public static final StmtOption<Integer> MAXFIELDSIZE		= new StmtOption<>("MaxFieldSize", null, Statement::setMaxFieldSize, Statement::getMaxFieldSize);
	public static final StmtOption<Integer> MAXROWS				= new StmtOption<>("MaxRows", null, Statement::setMaxRows, Statement::getMaxRows);
	public static final StmtOption<Boolean> POOLABLE  			= new StmtOption<>("Poolable", null, Statement::setPoolable, Statement::isPoolable);
	public static final StmtOption<Integer> QUERYTIMEOUT		= new StmtOption<>("QueryTimeout", null, Statement::setQueryTimeout, Statement::getQueryTimeout);


	private StmtOption(String name, T defaultValue, CheckedBiConsumer<Statement,T> setter, CheckedFunction<Statement,T> getter)
	{
		this.name			= name;
		this.defaultValue 	= defaultValue;
		this.setter 		= setter;
		this.getter 		= getter;
	}


	public T getDefaultValue()
	{
		if (defaultValue != null)
			return defaultValue;
		throw JdbxException.illegalState(name + ": default value is implementation dependent. To access it, first initialize the statement");
	}


	@Override public String toString()
	{
		return name;
	}


	public final String name;
	private final T defaultValue; // null if the value is driver dependent
	public final CheckedBiConsumer<Statement,T> setter;
	public final CheckedFunction<Statement,T> getter;
}
