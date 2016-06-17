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
 * A functional interface like java.util.Function which
 * can throw any exception.
 * @param <T> the type of the input to the function
 * @param <R> the type of results supplied
 */
@FunctionalInterface
public interface CheckedFunction<T,R>
{
	/**
	 * Calls the function and converts any exception into
	 * a JdbException
	 * @param function a function
	 * @param arg an argument
	 * @param <T> the type of the input to the function
	 * @param <R> the type of results supplied
	 * @return the function result
	 */
	public static <T,R> R unchecked(CheckedFunction<T,R> function, T arg) throws JdbException
	{
		try
		{
			return function.apply(arg);
		}
		catch (Exception e)
		{
			throw JdbException.of(e);
		}
	}


    /**
     * Applies this function to the argument.
     * @param arg the argument
     * @return the function result
 	 * @throws Exception if an error occurs
    */
	public R apply(T arg) throws Exception;
}