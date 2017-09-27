package org.jdbx;


import java.io.InputStream;
import java.sql.ResultSet;


abstract class GetResult extends GetValue
{
	/**
	 * Returns the value as a stream of ASCII characters.
	 * @return the stream
     * @see ResultSet#getAsciiStream(int)
	 */
	public InputStream getAsciiStream() throws JdbxException
	{
		return get(GetAccessors.ASCIISTREAM);
	}


	/**
	 * Returns the value as a stream of as a  stream of
     * uninterpreted bytes.
	 * @return the stream
     * @see ResultSet#getBinaryStream(int)
	 */
	public InputStream getBinaryStream() throws JdbxException
	{
		return get(GetAccessors.BINARYTREAM);
	}
}
