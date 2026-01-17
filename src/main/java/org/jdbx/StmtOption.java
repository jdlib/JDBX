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
	public static final StmtOption<Boolean> CLOSEONCOMPLETION	= new StmtOption<>(Boolean.FALSE, null, Statement::isCloseOnCompletion);
	public static final StmtOption<String> 	CURSORNAME 			= new StmtOption<>(null, Statement::setCursorName, null);
	public static final StmtOption<Boolean> ESCAPEPROCESSING	= new StmtOption<>(Boolean.TRUE, Statement::setEscapeProcessing, null);
	public static final StmtOption<Integer> FETCHDIRECTION		= new StmtOption<>(Integer.valueOf(FetchDirection.UNKNOWN.getCode()), Statement::setFetchDirection, Statement::getFetchDirection);
	public static final StmtOption<Integer> FETCHSIZE			= new StmtOption<>(null, Statement::setFetchSize, Statement::getFetchSize);
	public static final StmtOption<Long>    LARGEMAXROWS		= new StmtOption<>(null, Statement::setLargeMaxRows, Statement::getLargeMaxRows);
	public static final StmtOption<Integer> MAXFIELDSIZE		= new StmtOption<>(null, Statement::setMaxFieldSize, Statement::getMaxFieldSize);
	public static final StmtOption<Integer> MAXROWS				= new StmtOption<>(null, Statement::setMaxRows, Statement::getMaxRows);
	public static final StmtOption<Boolean> POOLABLE  			= new StmtOption<>(null, Statement::setPoolable, Statement::isPoolable);
	public static final StmtOption<Integer> QUERYTIMEOUT		= new StmtOption<>(null, Statement::setQueryTimeout, Statement::getQueryTimeout);


	private StmtOption(T defaultValue, CheckedBiConsumer<Statement,T> setter, CheckedFunction<Statement,T> getter)
	{
		this.defaultValue 	= defaultValue;
		this.setter 			= setter;
		this.getter 			= getter;
	}


	public T getDefaultValue()
	{
		if (defaultValue != null)
			return defaultValue;
		throw JdbxException.illegalState(getter.toString() + ": default value is implementation dependent. To access it, first initialize the statement");
	}


	private final T defaultValue; // null if the value is driver dependent
	public final CheckedBiConsumer<Statement,T> setter;
	public final CheckedFunction<Statement,T> getter;
}
