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


import org.jdbx.NamedParamCmd;
import org.junit.jupiter.api.Test;


public class NamedParamCmdTest extends JdbxTest
{
	@Test public void test()
	{
		NamedParamCmd cmd;

		cmd = assertParse("SELECT :x, :y, :x", "SELECT ?, ?, ?");
		assertIndexes(cmd, "x", 1, 3);
		assertIndexes(cmd, "y", 2);
		assertNull(cmd.getIndexes("z"));

		cmd = assertParse("SELECT '1'::json, ':x', \":x\", :x", "SELECT '1'::json, ':x', \":x\", ?");
		assertIndexes(cmd, "x", 1);

		cmd = assertParse("SELECT -1, --:x\n :x", "SELECT -1, --:x\n ?");
		assertIndexes(cmd, "x", 1);

		cmd = assertParse("SELECT 1/2, /*:x/*:x */ :x", "SELECT 1/2, /*:x/*:x */ ?");
		assertIndexes(cmd, "x", 1);
	}


	private NamedParamCmd assertParse(String in, String converted)
	{
		NamedParamCmd cmd = new NamedParamCmd(in);
		assertSame(in, cmd.toString());
		assertSame(in, cmd.getOriginal());
		assertEquals(converted, cmd.getConverted());
		return cmd;
	}


	private void assertIndexes(NamedParamCmd cmd, String name, int... expected)
	{
		int[] actual = cmd.getIndexes(name);
		if (actual == null)
			fail("no parameter: " + name);
		assertArrayEquals(expected, actual);
	}
}
