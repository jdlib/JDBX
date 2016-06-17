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
package org.jdbx.function;


import org.jdbx.JdbException;


/**
 * A functional interface like java.lang.Runnable which
 * can throw any exception.
 */
@FunctionalInterface
public interface CheckedRunnable
{
	/**
	 * Runs the runnable and converts any exception into a JdbException.
	 * @param runnable the runnable
	 */
	public static void unchecked(CheckedRunnable runnable) throws JdbException
	{
		try
		{
			runnable.run();
		}
		catch (Exception e)
		{
			throw JdbException.of(e);
		}
	}


    /**
     * Performs the runnable action.
	 * @throws Exception if an error occurs
     */
	public void	run() throws Exception;
}
