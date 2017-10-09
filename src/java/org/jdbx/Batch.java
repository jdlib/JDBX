package org.jdbx;


import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;


/**
 * Batch represents a list of batched SQL commands.
 */
public abstract class Batch
{
	/**
	 * A functional interface for a service which reads auto generated keys
	 * when performing a batch.
	 */
	@FunctionalInterface
	public static interface AutoKeysReader<R>
	{
		/**
		 * Reads the auto generated keys and stores the result
		 * into the value of the BatchResult.
		 * @param generatedKeys a result-set containing the auto generated keys
		 * @param result the BatchResult storing the update count and the value describing the auto generated keys.
		 * @see Statement#getGeneratedKeys()
		 * @throws Exception if an error occurs
		 */
		public void read(ResultSet generatedKeys, BatchResult<R> result) throws Exception;
	}


	/**
	 * Clears the batch.
	 * @see Statement#clearBatch()
	 */
	public void clear() throws JdbxException
	{
		stmt().call(Statement::clearBatch);
	}


	/**
	 * Executes the batched SQL commands.
	 * @return an array of update counts
	 * @see Statement#executeBatch()
	 */
	public int[] run() throws JdbxException
	{
		int[] result = stmt().get(Statement::executeBatch);
		return result != null ? result : new int[0];
	}


	/**
	 * Executes the batched SQL commands.
	 * @return an array of update counts
	 * @see Statement#executeLargeBatch()
	 */
	public long[] runLarge() throws JdbxException
	{
		long[] result = stmt().get(Statement::executeLargeBatch);
		return result != null ? result : new long[0];
	}


	/**
	 * Executes the batched SQL commands.
	 * @param reader a reader to extract the auto generated keys. The keys are reported
	 * 		in {@link BatchResult#value}.
	 * @param <V> the type of the value returned in {@link BatchResult#value}
	 * @return the result
	 */
	public <V> BatchResult<V> runGetAutoKeys(AutoKeysReader<V> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");

		BatchResult<V> result = new BatchResult<>(run());
		try
		{
			reader.read(stmt().getJdbcStmt().getGeneratedKeys(), result);
		}
		catch(Exception e)
		{
			throw JdbxException.of(e);
		}
		return result;
	}


	/**
	 * Executes the batched SQL commands.
	 * A list of auto-generated keys of the given type will also be returned.
	 * @param colType the type of the generated keys
	 * @param <V> the type of the auto generated keys
	 * @return the result
	 */
	public <V> BatchResult<List<V>> runGetAutoKeys(Class<V> colType) throws JdbxException
	{
		Check.notNull(colType, "colType");
		return runGetAutoKeys((rs,r) -> {
			r.value = QueryResult.of(rs).rows().col().get(colType);
		});
	}


	/**
	 * Executes the batched SQL commands.
	 * An auto-generated key of the given type will also be returned.
	 * @param keyType the type of the generated key
	 * @param <V> the type of the auto generated key
	 * @return the result
	 */
	public <V> BatchResult<V> runGetAutoKey(Class<V> keyType) throws JdbxException
	{
		Check.notNull(keyType, "keyType");
		return runGetAutoKeys((rs,r) -> {
			if (rs.next())
				r.value = rs.getObject(1, keyType);
		});
	}


	protected abstract Stmt stmt();
}
