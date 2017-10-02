package org.jdbx;


import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLXML;


/**
 * SetValue defines operations which set a value.
 * Result columns and statement parameters implement this interface.
 */
interface SetValue
{
	public default void setArray(Array value) throws JdbxException
	{
		set(SetAccessors.ARRAY, value);
	}


	public default void setBigDecimal(BigDecimal decimal) throws JdbxException
	{
		set(SetAccessors.BIGDECIMAL, decimal);
	}


	public default void setBlob(Blob blob) throws JdbxException
	{
		set(SetAccessors.BLOB, blob);
	}


	public default void setBoolean(boolean value) throws JdbxException
	{
		setBoolean(Boolean.valueOf(value));
	}


	public default void setBoolean(Boolean value) throws JdbxException
	{
		set(SetAccessors.BOOLEAN, value);
	}


	public default void setByte(byte value) throws JdbxException
	{
		setByte(Byte.valueOf(value));
	}


	public default void setByte(Byte value) throws JdbxException
	{
		set(SetAccessors.BYTE, value);
	}


	public default void setBytes(byte[] value) throws JdbxException
	{
		set(SetAccessors.BYTES, value);
	}


	public default void setCharacterStream(Reader value) throws JdbxException
	{
		set(SetAccessors.CHARACTERSTREAM, value);
	}


	public default void setClob(Clob value) throws JdbxException
	{
		set(SetAccessors.CLOB, value);
	}


	public default void setDouble(double value) throws JdbxException
	{
		setDouble(Double.valueOf(value));
	}


	public default void setDouble(Double value) throws JdbxException
	{
		set(SetAccessors.DOUBLE, value);
	}


	public default void setFloat(float value) throws JdbxException
	{
		setFloat(Float.valueOf(value));
	}


	public default void setFloat(Float value) throws JdbxException
	{
		set(SetAccessors.FLOAT, value);
	}


	public default void setInt(int value) throws JdbxException
	{
		setInteger(Integer.valueOf(value));
	}


	public default void setInteger(Integer value) throws JdbxException
	{
		set(SetAccessors.INTEGER, value);
	}


	public default void setLong(long value) throws JdbxException
	{
		setLong(Long.valueOf(value));
	}


	public default void setLong(Long value) throws JdbxException
	{
		set(SetAccessors.LONG, value);
	}


	public default void setNCharacterStream(Reader value) throws JdbxException
	{
		set(SetAccessors.NCHARACTERSTREAM, value);
	}


	public default void setNClob(NClob value) throws JdbxException
	{
		set(SetAccessors.NCLOB, value);
	}


	public default void setNString(String value) throws JdbxException
	{
		set(SetAccessors.NSTRING, value);
	}


	public default void setObject(Object value) throws JdbxException
	{
		set(SetAccessors.OBJECT, value);
	}


	public default void setRef(Ref value) throws JdbxException
	{
		set(SetAccessors.REF, value);
	}


	public default void setRowId(RowId value) throws JdbxException
	{
		set(SetAccessors.ROWID, value);
	}


	public default void setShort(short value) throws JdbxException
	{
		setShort(Short.valueOf(value));
	}


	public default void setShort(Short value) throws JdbxException
	{
		set(SetAccessors.SHORT, value);
	}


	public default void getSqlDate(java.sql.Date value) throws JdbxException
	{
		set(SetAccessors.SQLDATE, value);
	}


	public default void getSqlTime(java.sql.Time value) throws JdbxException
	{
		set(SetAccessors.SQLTIME, value);
	}


	public default void setSqlTimestamp(java.sql.Timestamp value) throws JdbxException
	{
		set(SetAccessors.SQLTIMESTAMP, value);
	}


	public default void setSqlXml(SQLXML value) throws JdbxException
	{
		set(SetAccessors.SQLXML, value);
	}


	public default void setString(String value) throws JdbxException
	{
		set(SetAccessors.STRING, value);
	}

	
	public abstract <T> void set(SetAccessors<T> accessors, T value) throws JdbxException;
}
