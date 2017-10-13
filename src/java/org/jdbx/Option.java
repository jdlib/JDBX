package org.jdbx;


import java.sql.Statement;
import org.jdbx.function.CheckedBiConsumer;
import org.jdbx.function.CheckedFunction;


class Option<T>
{
	public static final Option<Boolean> POOLABLE  			= new Option<>(Statement::setPoolable, Statement::isPoolable);
	public static final Option<String> 	CURSORNAME 			= new Option<>(Statement::setCursorName, null);
	public static final Option<Integer> MAXFIELDSIZE		= new Option<>(Statement::setMaxFieldSize, Statement::getMaxFieldSize);
	public static final Option<Long> LARGEMAXROWS			= new Option<>(Statement::setLargeMaxRows, Statement::getLargeMaxRows);
	public static final Option<Integer> MAXROWS				= new Option<>(Statement::setMaxRows, Statement::getMaxRows);
	public static final Option<Integer> QUERYTIMEOUT		= new Option<>(Statement::setQueryTimeout, Statement::getQueryTimeout);
	public static final Option<Boolean> ESCAPEPROCESSING	= new Option<>(Statement::setEscapeProcessing, null);
	public static final Option<Integer> FETCHDIRECTION		= new Option<>(Statement::setFetchDirection, Statement::getFetchDirection);
	public static final Option<Integer> FETCHSIZE			= new Option<>(Statement::setFetchSize, Statement::getFetchSize);
	
	
	private Option(CheckedBiConsumer<Statement,T> setter, CheckedFunction<Statement,T> getter)
	{
		this.setter = setter;
		this.getter = getter;
	}
	
	
	public final CheckedBiConsumer<Statement,T> setter;
	public final CheckedFunction<Statement,T> getter;
}
