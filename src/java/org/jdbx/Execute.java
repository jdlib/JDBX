package org.jdbx;


import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;


/**
 * Execute is the JDBC and JDBX term for executing a SQL or DDL command
 * whose result type (query or update) is not known or which can return
 * multiple (query or update) results. 
 * @see StaticStmt#createExecute(String)
 * @see PrepStmt#createExecute()
 * @see CallStmt#createExecute()
 */
public abstract class Execute extends StmtRunnable
{
	@Override protected final String getRunnableType()
	{
		return "Execute";
	}


	public void run(CheckedConsumer<ExecuteResult> consumer) throws JdbxException
	{
		Check.notNull(consumer, "consumer");
		run(r -> {
			consumer.accept(r);
			return null;
		});
	}


	public <R> R run(CheckedFunction<ExecuteResult,R> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");
		registerRun();

		try
		{
			ExecuteResult result = run();
			return reader.apply(result);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	public abstract ExecuteResult run() throws JdbxException;
}
