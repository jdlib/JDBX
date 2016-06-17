package org.jdbx;


import java.sql.Statement;


/**
 * BatchResult is returned by various {@link Batch} methods.
 * It stores the update count of the batch and information
 * about automatically generated keys.
 * @see Batch#runGetAutoKeys(org.jdbx.Batch.AutoKeysReader)
 * @see Batch#runGetAutoKeys(Class)
 * @see Batch#runGetAutoKey(Class)
 */
public class BatchResult<R>
{
	/**
	 * UpdateType categorizes update counts
	 */
	public enum UpdateType
	{
		/**
		 * The type for all update counts >= 0.
		 */
		SUCCESS,

		/**
		 * The type for update count {@link Statement#SUCCESS_NO_INFO}.
		 */
		SUCCESS_NO_INFO,

		/**
		 * The type for update count {@link Statement#EXECUTE_FAILED}.
		 */
		EXECUTE_FAILED,

		/**
		 * The type for an invaid update count return by the JDBC driver.
		 */
		INVALID,
	}


	/**
	 * Creates a new BatchResult.
	 * @param the update counts
	 */
	public BatchResult(int[] counts)
	{
		// silently correct misbehavior of null results returned by Statement.executeBatch
		counts_ = counts != null ? counts : new int[0];
	}


	/**
	 * Returns the number of update counts in this BatchResult.
	 * @return the number of update counts
	 */
	public int size()
	{
		return counts_.length;
	}


	/**
	 * Returns the update count with the given index.
	 * @return the update count
	 */
	public int getCount(int index)
	{
		return counts_[index];
	}


	/**
	 * Returns the type of the update count with the given index.
	 * @return the type
	 * @see Statement#executeBatch()
	 * @see Statement#SUCCESS_NO_INFO
	 * @see Statement#EXECUTE_FAILED
	 */
	public UpdateType getCountType(int index)
	{
		int count = getCount(index);
		if (count >= 0)
			return UpdateType.SUCCESS;
		else if (count == Statement.SUCCESS_NO_INFO)
			return UpdateType.SUCCESS_NO_INFO;
		else if (count == Statement.EXECUTE_FAILED)
			return UpdateType.EXECUTE_FAILED;
		else
			return UpdateType.INVALID;
	}


	/**
	 * Checks that the update count with the given index equals the given count.
	 * @return this
	 * @throws JdbException thrown if the actual value does not match the expected value
	 */
	public BatchResult<R> checkCount(int index, int count) throws JdbException
	{
		if (getCount(index) != count)
			throw JdbException.invalidResult("#" + index + ": expected update count " + count + ", but was " + getCount(index));
		return this;
	}


	/**
	 * Checks that the update count  type with the given index equals the given type.
	 * @return this
	 * @throws JdbException thrown if the actual type does not match the expected type
	 */
	public BatchResult<R> checkCountType(int index, UpdateType type) throws JdbException
	{
		if (getCountType(index) != type)
			throw JdbException.invalidResult("#" + index + ": expected update count type " + type + ", but was " + getCountType(index));
		return this;
	}


	/**
	 * Checks that the value of this BatchResult is not null.
	 * @throws JdbException thrown if the actual value does not match the expected value
	 */
	public R checkHasValue() throws JdbException
	{
		if (this.value == null)
			throw JdbException.invalidResult("expected non-null value");
		return this.value;
	}


	/**
	 * Holds the update counts for the executed commands.
	 */
	private int[] counts_;

	/**
	 * Stores the value returned by the AutoKeysReader.
	 */
	public R value;
}


