package org.jdbx;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

/**
 * Test class for Jdbx.
 */
public class JdbxClassTest extends JdbxTest
{
	@Test public void testMain()
	{
		PrintStream oldOut = System.out;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			System.setOut(new PrintStream(baos));
			Jdbx.main(new String[] {});
			Jdbx.main(new String[] {"-v"});
			String output = baos.toString();
			assertEquals(Jdbx.VERSION + System.lineSeparator() + "JDBX.version = " + Jdbx.VERSION + System.lineSeparator(), output);
		}
		finally
		{
			System.setOut(oldOut);
		}
	}


	@Test public void testFastQuery()
	{
		Query query = Jdbx.query(con(), "VALUES (1)");
		assertEquals("Query:VALUES (1)", query.toString());
		assertEquals(1, query.row().col().getInt());
	}


	@Test public void testFastUpdate()
	{
		Query query = Jdbx.query(con(), "VALUES (1)");
		assertEquals("Query:VALUES (1)", query.toString());
		assertEquals(1, query.row().col().getInt());
	}
}
