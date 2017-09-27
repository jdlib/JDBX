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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;


/**
 * GetValue defines operations which return a value.
 * Result columns and statement parameters implement this interface.
 */
abstract class GetValue
{
	public Array getArray() throws JdbxException
	{
		return get(GetAccessors.ARRAY);
	}


	public BigDecimal getBigDecimal() throws JdbxException
	{
		return get(GetAccessors.BIGDECIMAL);
	}


	public Blob getBlob() throws JdbxException
	{
		return get(GetAccessors.BLOB);
	}


	public boolean getBoolean() throws JdbxException
	{
		return getBoolean(false);
	}


	public boolean getBoolean(boolean defaultValue) throws JdbxException
	{
		Boolean value = getBooleanObject();
		return value != null ? value.booleanValue() : defaultValue;
	}


	public Boolean getBooleanObject() throws JdbxException
	{
		return get(GetAccessors.BOOLEAN);
	}


	public byte getByte() throws JdbxException
	{
		Byte value = getByteObject();
		return value != null ? value.byteValue() : (byte)0;
	}


	public Byte getByteObject() throws JdbxException
	{
		return get(GetAccessors.BYTE);
	}


	public byte[] getBytes() throws JdbxException
	{
		return get(GetAccessors.BYTES);
	}


	public Reader getCharacterStream() throws JdbxException
	{
		return get(GetAccessors.CHARACTERSTREAM);
	}


	public Clob getClob() throws JdbxException
	{
		return get(GetAccessors.CLOB);
	}


	public double getDouble() throws JdbxException
	{
		return getDouble(0.0);
	}


	public double getDouble(double defaultValue) throws JdbxException
	{
		Double value = getDoubleObject();
		return value != null ? value.doubleValue() : defaultValue;
	}


	public Double getDoubleObject() throws JdbxException
	{
		return get(GetAccessors.DOUBLE);
	}


	public float getFloat() throws JdbxException
	{
		Float value = getFloatObject();
		return value != null ? value.floatValue() : (float)0;
	}


	public Float getFloatObject() throws JdbxException
	{
		return get(GetAccessors.FLOAT);
	}


	public int getInt() throws JdbxException
	{
		return getInt(0);
	}


	public int getInt(int defaultValue) throws JdbxException
	{
		Integer value = getInteger();
		return value != null ? value.intValue() : defaultValue;
	}


	public Integer getInteger() throws JdbxException
	{
		return get(GetAccessors.INTEGER);
	}


	public LocalDate getLocalDate() throws JdbxException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return get(LocalDate.class);
	}


	public LocalTime getLocalTime() throws JdbxException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return get(LocalTime.class);
	}


	public long getLong() throws JdbxException
	{
		return getLong(0);
	}


	public long getLong(long defaultValue) throws JdbxException
	{
		Long value = getLongObject();
		return value != null ? value.longValue() : defaultValue;
	}


	public Long getLongObject() throws JdbxException
	{
		return get(GetAccessors.LONG);
	}


	public Reader getNCharacterStream() throws JdbxException
	{
		return get(GetAccessors.NCHARACTERSTREAM);
	}


	public NClob getNClob() throws JdbxException
	{
		return get(GetAccessors.NCLOB);
	}


	public String getNString() throws JdbxException
	{
		return get(GetAccessors.NSTRING);
	}


	public Object getObject() throws JdbxException
	{
		return get(GetAccessors.OBJECT);
	}


	public Ref getRef() throws JdbxException
	{
		return get(GetAccessors.REF);
	}


	public RowId getRowId() throws JdbxException
	{
		return get(GetAccessors.ROWID);
	}


	public short getShort() throws JdbxException
	{
		Short value = getShortObject();
		return value != null ? value.shortValue() : (short)0;
	}


	public Short getShortObject() throws JdbxException
	{
		return get(GetAccessors.SHORT);
	}


	public java.sql.Date getSqlDate() throws JdbxException
	{
		return get(GetAccessors.SQLDATE);
	}


	public java.sql.Time getSqlTime() throws JdbxException
	{
		return get(GetAccessors.SQLTIME);
	}


	public java.sql.Timestamp getSqlTimestamp() throws JdbxException
	{
		return get(GetAccessors.SQLTIMESTAMP);
	}


	public SQLXML getSqlXml() throws JdbxException
	{
		return get(GetAccessors.SQLXML);
	}


	public String getString() throws JdbxException
	{
		return get(GetAccessors.STRING);
	}


	public abstract <T> T get(Class<T> type) throws JdbxException;


	public abstract Object get(Map<String,Class<?>> map) throws JdbxException;


	abstract <T> T get(GetAccessors<T> accessors) throws JdbxException;
}
