package org.jdbx.function;


import java.sql.Statement;
import org.jdbx.Query;


/**
 * A functional interface for a service which reads the returned column values
 * (e.g. auto-generated keys) as a result of executing a SQL command.
 * @see Statement#getGeneratedKeys()
 * @param<V> the type of the value representing the returned column values
 */
@FunctionalInterface
public interface GetReturnCols<V>
{
	/**
	 * Reads the returned column values as a value of type V
	 * @param updateCount the update count
	 * @param query a Query containing the returned column values
	 * @return the value representing the returned column values
	 * @see Statement#getGeneratedKeys()
	 * @throws Exception if an error occurs
	 */
	public V read(long updateCount, Query query) throws Exception;
}
