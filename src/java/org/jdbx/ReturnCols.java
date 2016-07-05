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
 * when a INSERT command is executed.
 */
public class ReturnCols
{
	/**
	 * A Builder interface to define which columns should be reported.
	 * @param <I> the concrete builder implementation
	 */
	public static interface Builder<I extends Builder<I>>
	{
		/**
		 * The columns defined by the ReturnCols object should be returned.
		 * @param cols the ReturnCols object or null if no columns should be returned
		 * @return this
		 */
		public I returnCols(ReturnCols cols);

		
		/**
		 * The columns which contain auto generated keys should be returned.
		 * @return this
		 */
		public default I returnGenCols()
		{
			return returnCols(new ReturnCols());
		}


		/**
		 * Defines the indexes of the columns which should be returned.
		 * @param colIndexes the indexes
		 * @return this
		 */
		public default I returnCols(int... colIndexes)
		{
			return returnCols(new ReturnCols(colIndexes));
		}


		/**
		 * Defines the names of the columns which should be returned.
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
	public ReturnCols()
	{
	}


	/**
	 * Creates a new ReturnCols object.
	 * @param indexes the indexes of the columns which should be returned
	 */
	public ReturnCols(int[] indexes)
	{
		Check.notNull(indexes, "indexes");
		indexes_ = indexes;
	}


	/**
	 * Creates a new ReturnCols object.
	 * @param names the names of the columns which should be returned
	 */
	public ReturnCols(String[] names)
	{
		Check.notNull(names, "names");
		names_ = names;
	}


	/**
	 * Returns the indexes of the columns which should be returned.
	 * @return the indexes or null if this ReturnCols object is not index based
	 */
	public int[] getIndexes()
	{
		return indexes_;
	}


	/**
	 * Returns the names of the columns which should be returned.
	 * @return the named or null if this ReturnCols object is not name based
	 */
	public String[] getNames()
	{
		return names_;
	}


	private int[] indexes_;
	private String[] names_;
}
