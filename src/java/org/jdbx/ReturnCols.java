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


/**
 * ReturnCols represents the columns which should be returned
 * when a sql INSERT or UPDATE command is executed.
 */
public class ReturnCols
{
	/**
	 * A ReturnCols constant representing the columns with auto-generated values.
	 */
	public static final ReturnCols AUTOGEN = new ReturnCols();
	
	
	/**
	 * A Builder interface to define which columns should be reported when a INSERT is executed.
	 * @param <I> the concrete builder implementation
	 */
	public static interface Builder<I extends Builder<I>>
	{
		/**
		 * The columns defined by the given ReturnCols object are returned.
		 * @param cols the ReturnCols object or null if no columns should be returned
		 * @return this
		 */
		public I returnCols(ReturnCols cols);

		
		/**
		 * The columns which contain auto generated keys are returned.
		 * Ultimately the JDBC driver or database determine which columns are returned.   
		 * @return this
		 */
		public default I returnAutoKeyCols()
		{
			return returnCols(AUTOGEN);
		}


		/**
		 * Specifies the 1-based number of the column which is returned.
		 * @param number the column number
		 * @return this
		 */
		public default I returnCol(int number)
		{
			return returnCols(number);
		}


		/**
		 * Specifies the 1-based numbers of the columns which are returned.
		 * @param numbers the column numbers
		 * @return this
		 */
		public default I returnCols(int... numbers)
		{
			return returnCols(new ReturnCols(numbers));
		}


		/**
		 * Specifies the name of the columns which should be returned.
		 * @param colName the name
		 * @return this
		 */
		public default I returnCol(String colName)
		{
			return returnCols(colName);
		}


		/**
		 * Specifies the names of the columns which should be returned.
		 * @param colNames the names
		 * @return this
		 */
		public default I returnCols(String... colNames)
		{
			return returnCols(new ReturnCols(colNames));
		}
	}


	/**
	 * Creates a new ReturnCols object representing the auto generated key columns.
	 */
	private ReturnCols()
	{
		this(null, null);
	}


	/**
	 * Creates a new ReturnCols object.
	 * @param numbers the numbers of the columns which should be returned
	 */
	public ReturnCols(int[] numbers)
	{
		this(Check.notNull(numbers, "numbers"), null);
	}


	/**
	 * Creates a new ReturnCols object.
	 * @param names the names of the columns which should be returned
	 */
	public ReturnCols(String[] names)
	{
		this(null, Check.notNull(names, "names"));
	}


	private ReturnCols(int[] numbers, String[] names)
	{
		numbers_ = numbers;
		names_ 	 = names;
	}

	
	/**
	 * Returns the numbers of the columns which should be returned.
	 * @return the numbers or null if this ReturnCols object is not number based
	 */
	public int[] getNumbers()
	{
		return numbers_;
	}


	/**
	 * Returns the names of the columns which should be returned.
	 * @return the names or null if this ReturnCols object is not name based
	 */
	public String[] getNames()
	{
		return names_;
	}


	private final int[] numbers_;
	private final String[] names_;
}
