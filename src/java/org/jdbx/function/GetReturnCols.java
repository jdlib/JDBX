package org.jdbx.function;


import java.sql.Statement;
import org.jdbx.QueryResult;


/**
 * A functional interface for a service which reads the returned column values
 * from an update result.
 */
@FunctionalInterface
public interface GetReturnCols<V>
{
	/**
	 * Reads the returned column values as a value of type V 
	 * @param updateCount the update count
	 * @param result a QueryResult containing the returned column values
	 * @return the value representing the returned column values 
	 * @see Statement#getGeneratedKeys()
	 * @throws Exception if an error occurs
	 */
	public V read(long updateCount, QueryResult result) throws Exception;
}
