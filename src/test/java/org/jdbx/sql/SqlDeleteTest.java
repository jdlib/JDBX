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


public class SqlDeleteTest
{
	@Test public void test()
	{
		SqlDelete delete = new SqlDelete("t");
		assertEquals("DELETE FROM t", delete.toString());

		delete.where("id = 3").where(w -> w.and().add("locked = false"));
		assertEquals("DELETE FROM t WHERE id = 3 AND locked = false", delete.toString());
	}
}
