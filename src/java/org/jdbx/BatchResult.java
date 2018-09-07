package org.jdbx;


import java.sql.Statement;
import org.jdbx.function.GetReturnCols;


/**
 * BatchResult is returned by various {@link Batch} methods.
 * It stores the update count of the batch and information
 * about automatically generated keys.
 * @see Batch#runGetCols(org.jdbx.function.GetReturnCols)
 * @see Batch#runGetCols(Class)
 */
public class BatchResult<V>
{
	/**
	 * CountType categorizes update counts.
	 */
	public enum CountType
	{
		/**
		 * All update counts have been >= 0.
		 */
		SUCCESS,

		/**
		 * Corresponds to {@link Statement#SUCCESS_NO_INFO}.
		 */
		SUCCESS_NO_INFO,

		/**
		 * Corresponds to {@link Statement#EXECUTE_FAILED}.
		 */
		EXECUTE_FAILED,

		/**
		 * The JDBC driver returned an invalid update count.
		 */
		INVALID;
		
		
		public static CountType forCount(int count)
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
	 * Checks that the {@link #size()} has the given value.
	 * @throws JdbxException thrown if the actual size does not match the expected size
	 * @return this
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
	public CountType getCountType(int index)
	{
		return CountType.forCount(getCount(index));
	}


	/**
	 * Checks that the update count with the given index equals the given count.
	 * @return this
	 * @throws JdbxException thrown if the actual value does not match the expected value
	 */
	public BatchResult<V> requireCount(int index, int expectedCount) throws JdbxException
	{
		int actualCount = getCount(index); 
		if (actualCount != expectedCount)
			throw JdbxException.invalidResult("#" + index + ": expected update count " + expectedCount + ", but was " + actualCount);
		return this;
	}


	/**
	 * Checks that the update count type with the given index equals the given type.
	 * @return this
	 * @throws JdbxException thrown if the actual type does not match the expected type
	 */
	public BatchResult<V> requireCountType(int index, CountType expectedType) throws JdbxException
	{
		CountType actualType = getCountType(index); 
		if (actualType != expectedType)
			throw JdbxException.invalidResult("#" + index + ": expected update count type " + expectedType + ", but was " + actualType);
		return this;
	}


	/**
	 * Checks that the value of this BatchResult is not null.
	 * @throws JdbxException thrown if the actual value is null
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
	 * Stores the value returned by the {@link GetReturnCols} reader passed to {@link Batch#runGetCols(org.jdbx.function.GetReturnCols)}.
	 */
	private final V value_;
}


