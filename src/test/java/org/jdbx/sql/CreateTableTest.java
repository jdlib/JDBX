/*
 * Copyright (C) 2025 JDBX
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


public class CreateTableTest
{
	@Test public void testBasics()
	{
		CreateTable ct = new CreateTable("t")
			.col("a INT")
			.col("b", "VARCHAR(100)", "NOT NULL");
		assertEquals("CREATE TABLE t (a INT, b VARCHAR(100) NOT NULL)", ct.toString());

		ct = new CreateTable("t")
			.multiLine()
			.col("a INT")
			.col("b INT");
		String actual = ct.toString().replace(System.lineSeparator(), "\n");
		assertEquals("CREATE TABLE t\n(\n  a INT,\n  b INT\n)", actual);
	}


	@Test public void testIfNoExists()
	{
		CreateTable ct = new CreateTable("t")
			.ifNotExists()
			.col("a INT");
		assertEquals("CREATE TABLE IF NOT EXISTS t (a INT)", ct.toString());
	}
}
