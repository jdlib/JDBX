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


/**
 * A functional interface like java.util.BiFunction which
 * can throw any exception.
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of results supplied
 */
@FunctionalInterface
public interface CheckedBiFunction<T,U,R>
{
    /**
     * Applies this function to the arguments.
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @return the function result
 	 * @throws Exception if an error occurs
    */
	public R apply(T arg1, U arg2) throws Exception;
}