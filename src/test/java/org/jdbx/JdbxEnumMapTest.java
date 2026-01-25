package org.jdbx;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class JdbxEnumMapTest
{
	@Test public void test()
	{
		assertSame(Concurrency.READ_ONLY, Concurrency.map.forCode(Concurrency.READ_ONLY.getCode()));
		assertSame(Concurrency.INVALID, Concurrency.map.forCode(-1));
		assertSame(Concurrency.INVALID, Concurrency.map.forCode(null));
	}
}
