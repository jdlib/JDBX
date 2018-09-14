package org.jdbx;


import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.jdbx.function.GetReturnCols;


/**
 * Batch represents a list of batched SQL commands.
 */
public abstract class Batch
{
	/**
	 * Clears the batch.
	 * @return this
	 * @see Statement#clearBatch()
	 */
	public Batch clear() throws JdbxException
	{
		stmt().call(Statement::clearBatch);
		return this;
	}


	/**
	 * Executes the batched SQL commands.
	 * @return a BatchResult
	 * @see Statement#executeBatch()
	 */
	public BatchResult<Void> run() throws JdbxException
	{
		return new BatchResult<>(runImpl());
	}


	/**
	 * Runs the command and returns a list of auto generated keys.
	 * @param colType the type of the generated keys
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the key list
	 */
	public <V> BatchResult<List<V>> runGetCols(Class<V> colType) throws JdbxException
	{
		Check.notNull(colType, "colType");
		return runGetCols((c,q) ->  q.rows().col().get(colType));
	}


	/**
	 * Runs the command and passes the result-set of the generated keys to the reader.
	 * @param reader a reader functions. It read the keys from the update result-set and returns it as object of type V.
	 * @param <V> the type of the value returned by the AutoKeysReader
	 * @return an UpdateResult holding the update count and the column values
	 */
	public <V> BatchResult<V> runGetCols(GetReturnCols<V> reader) throws JdbxException
	{
		Check.notNull(reader, "reader");

		try
		{
			int[] counts = runImpl();
			try (ResultSet rs = stmt().getJdbcStmt().getGeneratedKeys()) 
			{ 
				V value = reader.read(counts != null ? counts.length : 0, QueryResult.of(rs));
				return new BatchResult<>(value, counts);
			}
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	private int[] runImpl()
	{
		return stmt().get(Statement::executeBatch);
	}


	protected abstract Stmt stmt();
}
