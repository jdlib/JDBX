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
	public Array getArray() throws JdbException
	{
		return get(GetAccessors.ARRAY);
	}


	public BigDecimal getBigDecimal() throws JdbException
	{
		return get(GetAccessors.BIGDECIMAL);
	}


	public Blob getBlob() throws JdbException
	{
		return get(GetAccessors.BLOB);
	}


	public boolean getBoolean() throws JdbException
	{
		return getBoolean(false);
	}


	public boolean getBoolean(boolean defaultValue) throws JdbException
	{
		Boolean value = getBooleanObject();
		return value != null ? value.booleanValue() : defaultValue;
	}


	public Boolean getBooleanObject() throws JdbException
	{
		return get(GetAccessors.BOOLEAN);
	}


	public byte getByte() throws JdbException
	{
		Byte value = getByteObject();
		return value != null ? value.byteValue() : (byte)0;
	}


	public Byte getByteObject() throws JdbException
	{
		return get(GetAccessors.BYTE);
	}


	public byte[] getBytes() throws JdbException
	{
		return get(GetAccessors.BYTES);
	}


	public Reader getCharacterStream() throws JdbException
	{
		return get(GetAccessors.CHARACTERSTREAM);
	}


	public Clob getClob() throws JdbException
	{
		return get(GetAccessors.CLOB);
	}


	public double getDouble() throws JdbException
	{
		return getDouble(0.0);
	}


	public double getDouble(double defaultValue) throws JdbException
	{
		Double value = getDoubleObject();
		return value != null ? value.doubleValue() : defaultValue;
	}


	public Double getDoubleObject() throws JdbException
	{
		return get(GetAccessors.DOUBLE);
	}


	public float getFloat() throws JdbException
	{
		Float value = getFloatObject();
		return value != null ? value.floatValue() : (float)0;
	}


	public Float getFloatObject() throws JdbException
	{
		return get(GetAccessors.FLOAT);
	}


	public int getInt() throws JdbException
	{
		return getInt(0);
	}


	public int getInt(int defaultValue) throws JdbException
	{
		Integer value = getInteger();
		return value != null ? value.intValue() : defaultValue;
	}


	public Integer getInteger() throws JdbException
	{
		return get(GetAccessors.INTEGER);
	}


	public LocalDate getLocalDate() throws JdbException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return get(LocalDate.class);
	}


	public LocalTime getLocalTime() throws JdbException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return get(LocalTime.class);
	}


	public long getLong() throws JdbException
	{
		return getLong(0);
	}


	public long getLong(long defaultValue) throws JdbException
	{
		Long value = getLongObject();
		return value != null ? value.longValue() : defaultValue;
	}


	public Long getLongObject() throws JdbException
	{
		return get(GetAccessors.LONG);
	}


	public Reader getNCharacterStream() throws JdbException
	{
		return get(GetAccessors.NCHARACTERSTREAM);
	}


	public NClob getNClob() throws JdbException
	{
		return get(GetAccessors.NCLOB);
	}


	public String getNString() throws JdbException
	{
		return get(GetAccessors.NSTRING);
	}


	public Object getObject() throws JdbException
	{
		return get(GetAccessors.OBJECT);
	}


	public Ref getRef() throws JdbException
	{
		return get(GetAccessors.REF);
	}


	public RowId getRowId() throws JdbException
	{
		return get(GetAccessors.ROWID);
	}


	public short getShort() throws JdbException
	{
		Short value = getShortObject();
		return value != null ? value.shortValue() : (short)0;
	}


	public Short getShortObject() throws JdbException
	{
		return get(GetAccessors.SHORT);
	}


	public java.sql.Date getSqlDate() throws JdbException
	{
		return get(GetAccessors.SQLDATE);
	}


	public java.sql.Time getSqlTime() throws JdbException
	{
		return get(GetAccessors.SQLTIME);
	}


	public java.sql.Timestamp getSqlTimestamp() throws JdbException
	{
		return get(GetAccessors.SQLTIMESTAMP);
	}


	public SQLXML getSqlXml() throws JdbException
	{
		return get(GetAccessors.SQLXML);
	}


	public String getString() throws JdbException
	{
		return get(GetAccessors.STRING);
	}


	public abstract <T> T get(Class<T> type) throws JdbException;


	public abstract Object get(Map<String,Class<?>> map) throws JdbException;


	abstract <T> T get(GetAccessors<T> accessors) throws JdbException;
}
