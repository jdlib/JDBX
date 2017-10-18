package org.jdbx;


import java.sql.Statement;
import org.jdbx.function.CheckedBiConsumer;
import org.jdbx.function.CheckedFunction;


class StmtOption<T>
{
	public static final StmtOption<Boolean> POOLABLE  			= new StmtOption<>(Statement::setPoolable, Statement::isPoolable);
	public static final StmtOption<String> 	CURSORNAME 			= new StmtOption<>(Statement::setCursorName, null);
	public static final StmtOption<Integer> MAXFIELDSIZE		= new StmtOption<>(Statement::setMaxFieldSize, Statement::getMaxFieldSize);
	public static final StmtOption<Long> LARGEMAXROWS			= new StmtOption<>(Statement::setLargeMaxRows, Statement::getLargeMaxRows);
	public static final StmtOption<Integer> MAXROWS				= new StmtOption<>(Statement::setMaxRows, Statement::getMaxRows);
	public static final StmtOption<Integer> QUERYTIMEOUT		= new StmtOption<>(Statement::setQueryTimeout, Statement::getQueryTimeout);
	public static final StmtOption<Boolean> ESCAPEPROCESSING	= new StmtOption<>(Statement::setEscapeProcessing, null);
	public static final StmtOption<Integer> FETCHDIRECTION		= new StmtOption<>(Statement::setFetchDirection, Statement::getFetchDirection);
	public static final StmtOption<Integer> FETCHSIZE			= new StmtOption<>(Statement::setFetchSize, Statement::getFetchSize);
	
	
	private StmtOption(CheckedBiConsumer<Statement,T> setter, CheckedFunction<Statement,T> getter)
	{
		this.setter = setter;
		this.getter = getter;
	}
	
	
	public final CheckedBiConsumer<Statement,T> setter;
	public final CheckedFunction<Statement,T> getter;
}
