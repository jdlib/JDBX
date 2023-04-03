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


	/**
	 * Runs the command and passes the result to the consumer.
	 */
	public void run(CheckedConsumer<ExecuteResult> consumer) throws JdbxException
	{
		Check.notNull(consumer, "consumer");
		run(r -> {
			consumer.accept(r);
			return null;
		});
	}


	/**
	 * Runs the command and passes the result to the reader function.
	 * @return the result returned by the reader
	 */
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


	/**
	 * Runs the command.
	 */
	public abstract ExecuteResult run() throws JdbxException;
}
