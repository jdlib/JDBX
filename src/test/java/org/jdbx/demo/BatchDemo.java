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
package org.jdbx.demo;


import java.util.List;
import org.jdbx.BatchResult;
import org.jdbx.BatchResult.CountType;
import org.jdbx.JdbxException;
import org.jdbx.PrepStmt;
import org.jdbx.StaticStmt;


/**
 * BatchDemo shows how to use SQL batch processing with JDBX.
 * For a basic unterstanding how to invoke batches for the various statements 
 * see {@link StaticStmtDemo#batchDemo(StaticStmt)} and {@link PrepStmtDemo#batchDemo(PrepStmt, int...)}.
 */
@SuppressWarnings("unused")
public class BatchDemo
{
	/**
	 * Evaluate the result counts of a BatchResult.
	 */
	public void evalResultCounts(StaticStmt stmt) throws JdbxException
	{
		// fill the batch, execute and obtain a BatchResult
		BatchResult<Void> result = stmt.batch()
			.add("UPDATE StatusA SET flag = 0")
			.add("UPDATE StatusB SET flag = 1")
			.add("UPDATE StatusC SET flag = 2")
			.run();
		
		// evaluate result counts
		int count0		= result.getCount(0); 		// get update count of first command
		int[] counts 	= result.getCounts(); 		// all counts as array
		CountType ct0	= result.getCountType(0);	// check if update succeeded, failed, etc.
		
		// assert result counts
		// if update count of command 0 wasn't 15 then this will throw an exception
		result.requireCount(0, 15);
		// if update CountType of command 0 wasn't SUCCESS then this will throw an exception
		result.requireCountType(0, CountType.SUCCESS);
	}


	/**
	 * Evaluate the result value of a BatchResult.
	 */
	public void evalResultValue(PrepStmt pstmt) throws JdbxException
	{
		pstmt.init("INSERT INTO Names (name) values (?)");
		pstmt.param(1, "Peter").batch().add();
		pstmt.param(1, "Paul").batch().add();
		
		// batch should return the generated Integer ids
		BatchResult<List<Integer>> result = pstmt.batch().runGetCols(Integer.class);
		
		// evaluate counts as before ...
		// and get the ids 
		List<Integer> ids = result.value();
	}
}
