/*
 * Copyright (C) 2026 JDBX
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
package org.jdbx.sql;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class SqlWhereTest
{
	@Test public void testWhere()
	{
		SqlWhere where = new SqlWhere();
		assertTrue(where.isEmpty());
		assertEquals("", where.toString());
		where
			.add("a > 5")
			.and()
			.openParen()
				.openParen()
					.add("b < 10")
				.closeParen()
				.or()
				.add("b IS NULL")
			.closeParen();
		assertFalse(where.isEmpty());
		assertEquals("a > 5 AND ((b < 10) OR b IS NULL)", where.toString());
	}
}
