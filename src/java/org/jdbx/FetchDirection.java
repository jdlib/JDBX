package org.jdbx;


import java.sql.ResultSet;


/**
 * An enum for the ResultSet.FETCH_* constants.
 */
public enum FetchDirection implements JdbcEnum
{
	FORWARD(ResultSet.FETCH_FORWARD),
	REVERSE(ResultSet.FETCH_REVERSE),
	UNKNOWN(ResultSet.FETCH_UNKNOWN),
	INVALID(-1);


	public static final Map<FetchDirection> MAP = new Map<>(FetchDirection.class, INVALID);

	
	FetchDirection(int code)
	{
		code_ = code;
	}


	/**
	 * Returns the JDBC constant.
	 * @return the constant
	 */
	@Override public int getCode()
	{
		return code_;
	}


	private final int code_;
}
