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


import java.sql.Connection;
import org.jdbx.BatchResult;
import org.jdbx.JdbxException;
import org.jdbx.PrepStmt;
import org.jdbx.StaticStmt;


/**
 * BatchDemo shows how to use SQL batch processing with JDBX.
 */
@SuppressWarnings("unused")
public class BatchDemo
{
	/**
	 * Use {@link StaticStmt#batch()} to add batch cmds and run the batch.
	 */
	public void staticCmds(Connection con) throws JdbxException
	{
		try (StaticStmt stmt = new StaticStmt(con))
		{
			BatchResult<Void> result = stmt.batch()
				.add("UPDATE StatusA SET flag = 0")
				.add("UPDATE StatusB SET flag = 1")
				.add("UPDATE StatusC SET flag = 2")
				.run();
		}
	}


	/**
	 * Use {@link PrepStmt#batch()} to add batch cmds and run the batch.
	 */
	public void paramCmds(Connection con, int... ids) throws JdbxException
	{
		try (PrepStmt stmt = new PrepStmt(con))
		{
			stmt.init("UPDATE Status SET flag = 1 WHERE id = ?");
			for (int id : ids)
			{
				stmt.param(1).setInt(id);
				stmt.batch().add();
			}
			BatchResult<Void> result = stmt.batch().run();
		}
	}
}
