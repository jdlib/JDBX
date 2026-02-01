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


import org.junit.jupiter.api.Test;


public class MultiStmtTest extends JdbxTest
{
	@Test public void testCon() throws JdbxException
	{
		StaticStmt stmt;
		PrepStmt pstmt;
		CallStmt cstmt;
		try (MultiStmt mstmt = new MultiStmt(con()))
		{
			assertSame(mstmt.getConnection(), con());

			assertEquals(0, mstmt.size());
			stmt = mstmt.newStaticStmt();
			pstmt = mstmt.newPrepStmt();
			cstmt = mstmt.newCallStmt();
			assertEquals(3, mstmt.size());
			mstmt.closeStmts();
			assertEquals(0, mstmt.size());
			mstmt.closeStmts(); // no harm
		}

		assertTrue(stmt.isClosed());
		assertTrue(pstmt.isClosed());
		assertTrue(cstmt.isClosed());
	}
}
