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
public class BatchResult<V>
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
		INVALID;
		
		
		public static UpdateType forCount(int count)
		{
			if (count >= 0)
				return SUCCESS;
			else if (count == Statement.SUCCESS_NO_INFO)
				return SUCCESS_NO_INFO;
			else if (count == Statement.EXECUTE_FAILED)
				return EXECUTE_FAILED;
			else
				return INVALID;
		}
	}


	/**
	 * Creates a new BatchResult.
	 * @param counts the update counts
	 */
	public BatchResult(int... counts)
	{
		this(null, counts);
	}
	
	
	/**
	 * Creates a new BatchResult.
	 * @param counts the update counts
	 */
	public BatchResult(V value, int... counts)
	{
		// silently correct misbehavior of null results returned by Statement.executeBatch
		counts_ = counts != null ? counts : new int[0];
		value_  = value;
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
	 * Returns the number of update counts in this BatchResult.
	 * @return the number of update counts
	 */
	public BatchResult<V> requireSize(int size)
	{
		if (size() != size)
			throw JdbxException.invalidResult("expected batch result size " + size + ", but was " + size());
		return this;
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
	 * Returns the update counts.
	 * @return the update counts
	 */
	public int[] getCounts()
	{
		return counts_;
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
		return UpdateType.forCount(getCount(index));
	}


	/**
	 * Checks that the update count with the given index equals the given count.
	 * @return this
	 * @throws JdbxException thrown if the actual value does not match the expected value
	 */
	public BatchResult<V> requireCount(int index, int count) throws JdbxException
	{
		if (getCount(index) != count)
			throw JdbxException.invalidResult("#" + index + ": expected update count " + count + ", but was " + getCount(index));
		return this;
	}


	/**
	 * Checks that the update count  type with the given index equals the given type.
	 * @return this
	 * @throws JdbxException thrown if the actual type does not match the expected type
	 */
	public BatchResult<V> requireCountType(int index, UpdateType type) throws JdbxException
	{
		if (getCountType(index) != type)
			throw JdbxException.invalidResult("#" + index + ": expected update count type " + type + ", but was " + getCountType(index));
		return this;
	}


	/**
	 * Checks that the value of this BatchResult is not null.
	 * @throws JdbxException thrown if the actual value does not match the expected value
	 */
	public V requireValue() throws JdbxException
	{
		if (value_ == null)
			throw JdbxException.invalidResult("expected non-null value");
		return value_;
	}
	
	
	public V value()
	{
		return value_;
	}


	/**
	 * Holds the update counts for the executed commands.
	 */
	private final int[] counts_;

	/**
	 * Stores the value returned by the AutoKeysReader.
	 */
	private final V value_;
}


