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


public class NamedParamCmdTest extends JdbxTest
{
	@Test public void test()
	{
		NamedParamCmd cmd;

		cmd = assertParse("SELECT :x, :y, :x", "SELECT ?, ?, ?");
		assertColNumbers(cmd, "x", 1, 3);
		assertColNumbers(cmd, "y", 2);
		assertNull(cmd.getColNumbers("z"));

		cmd = assertParse("SELECT '1'::json, ':x', \":x\", :x", "SELECT '1'::json, ':x', \":x\", ?");
		assertColNumbers(cmd, "x", 1);

		cmd = assertParse("SELECT -1, --:x\n :x", "SELECT -1, --:x\n ?");
		assertColNumbers(cmd, "x", 1);

		cmd = assertParse("SELECT 1/2, /*:x/*:x */ :x", "SELECT 1/2, /*:x/*:x */ ?");
		assertColNumbers(cmd, "x", 1);
	}


	private NamedParamCmd assertParse(String original, String converted)
	{
		NamedParamCmd cmd = new NamedParamCmd(original);
		assertSame(original, cmd.toString());
		assertSame(original, cmd.getOriginal());
		assertEquals(converted, cmd.getConverted());
		return cmd;
	}


	private void assertColNumbers(NamedParamCmd cmd, String paramName, int... expected)
	{
		int[] actual = cmd.getColNumbers(paramName);
		if (actual == null)
			fail("no parameter: " + paramName);
		assertArrayEquals(expected, actual);
	}
}
