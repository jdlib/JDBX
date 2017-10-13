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


import org.jdbx.JdbxException;


/**
 * A functional interface like java.util.BiConsumer which
 * can throw any exception.
 * @param <T> the type of the input
 */
@FunctionalInterface
public interface CheckedBiConsumer<T,U>
{
	/**
	 * Passes the argument to the consumer and converts any exception into a JdbxException
	 * @param consumer a consumer
	 * @param arg an argument
	 * @param <T> the type of the input
	 */
	public static <T,U> void unchecked(CheckedBiConsumer<T,U> consumer, T t, U u) throws JdbxException
	{
		try
		{
			consumer.accept(t, u);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


    /**
     * Performs this operation on the given argument.
     * @param t the first argument
     * @param u the second argument
 	 * @throws Exception if an error occurs
    */
    public void accept(T t, U u) throws Exception;
}