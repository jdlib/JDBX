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


public class SqlSelectTest
{
	@Test public void testIncomplete()
	{
		SqlSelect select = new SqlSelect();
		assertEquals("SELECT", select.toString());

		select.out("t1.a");
		assertEquals("SELECT t1.a", select.toString());
	}


	@Test public void testFull()
	{
		SqlSelect select = new SqlSelect()
			.out("t1.a")
			.out("t2.b")
			.from("t1")
			.from("inner join", "t2", "on", "t1.id = t2.t_id")
			.where("t2.b > 10")
			.orderBy("t1.a desc");
		assertEquals("SELECT t1.a, t2.b FROM t1 inner join t2 on t1.id = t2.t_id WHERE t2.b > 10 ORDER BY t1.a desc", select.toString());
	}


	@Test public void testFrom()
	{
		SqlSelect select = new SqlSelect()
			.from("t1")
			.from(f -> f.comma().add("t2"));
		assertEquals("SELECT FROM t1, t2", select.toString());
	}


	@Test public void testWhere()
	{
		SqlSelect select = new SqlSelect()
			.where("a > 5")
			.where(w -> w
				.and()
				.add("a < 10"));
		assertEquals("SELECT WHERE a > 5 AND a < 10", select.toString());
	}


	@Test public void testGroupBy()
	{
		SqlSelect select = new SqlSelect()
			.out("a")
			.out("b")
			.out("count(c)")
			.out("sum(d)")
			.from("t")
			.groupBy("a")
			.groupBy("b")
			.having("count(b) > 5")
			.having(h -> h.and().add("sum(d) < 5"));
		assertEquals("SELECT a, b, count(c), sum(d) FROM t GROUP BY a, b HAVING count(b) > 5 AND sum(d) < 5", select.toString());
	}
}
