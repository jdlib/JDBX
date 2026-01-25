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


public class ClauseBuilderTest
{
	@Test public void testAddTo()
	{
		ClauseBuilder cb = new ClauseBuilder(", ");
		assertTrue(cb.isEmpty());

		cb.add(null);
		assertTrue(cb.isEmpty());

		cb.add("");
		assertTrue(cb.isEmpty());

		cb.add("one");
		assertFalse(cb.isEmpty());
		assertEquals("one", cb.toString());
	}
}
