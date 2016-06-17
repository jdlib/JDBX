package org.jdbx;


import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedFunction;


/**
 * Execute is a builder class to configure and run a SQL or DDL command
 * which can return multiple results.
 */
public abstract class Execute extends StmtRunnable
{
	@Override protected final String getRunnableType()
	{
		return "Execute";
	}


	public void run(CheckedConsumer<ExecuteResult> consumer) throws JdbException
	{
		Check.notNull(consumer, "consumer");
		run(r -> {
			consumer.accept(r);
			return null;
		});
	}


	public <R> R run(CheckedFunction<ExecuteResult,R> reader) throws JdbException
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
			throw JdbException.of(e);
		}
	}


	public abstract ExecuteResult run() throws JdbException;
}
