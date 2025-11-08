/*
 * Copyright (C) 2016 JDBX
 *
 * https://github.com/jdlib/JDBX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbx;


import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLXML;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;


/**
 * GetValue defines operations which return a value.
 * Result columns and statement parameters implement this interface.
 */
interface GetValue
{
	/**
	 * @return the value as an Array.
	 */
	public default Array getArray() throws JdbxException
	{
		return get(GetAccessors.ARRAY);
	}


	/**
	 * @return the value as a BigDecimal.
	 */
	public default BigDecimal getBigDecimal() throws JdbxException
	{
		return get(GetAccessors.BIGDECIMAL);
	}


	/**
	 * @return the value as a Blob.
	 */
	public default Blob getBlob() throws JdbxException
	{
		return get(GetAccessors.BLOB);
	}


	/**
	 * @return the value as boolean. If the value is null, false is returned.
	 */
	public default boolean getBoolean() throws JdbxException
	{
		return getBoolean(false);
	}


	/**
	 * @return the value as boolean. If the value is null, the default value is returned.
	 * @param defaultValue the default value
	 */
	public default boolean getBoolean(boolean defaultValue) throws JdbxException
	{
		Boolean value = getBooleanObject();
		return value != null ? value.booleanValue() : defaultValue;
	}


	/**
	 * @return the value as Boolean.
	 */
	public default Boolean getBooleanObject() throws JdbxException
	{
		return get(GetAccessors.BOOLEAN);
	}


	/**
	 * @return the value as byte. If the value is null, (byte)0 is returned.
	 */
	public default byte getByte() throws JdbxException
	{
		return getByte((byte)0);
	}


	/**
	 * @return the value as byte. If the value is null, the default value is returned.
	 * @param defaultValue the default value
	 */
	public default byte getByte(byte defaultValue) throws JdbxException
	{
		Byte value = getByteObject();
		return value != null ? value.byteValue() : defaultValue;
	}


	/**
	 * @return the value as Byte.
	 */
	public default Byte getByteObject() throws JdbxException
	{
		return get(GetAccessors.BYTE);
	}


	/**
	 * @return the value as byte array.
	 */
	public default byte[] getBytes() throws JdbxException
	{
		return get(GetAccessors.BYTES);
	}


	/**
	 * @return the value as Reader.
	 */
	public default Reader getCharacterStream() throws JdbxException
	{
		return get(GetAccessors.CHARACTERSTREAM);
	}


	/**
	 * @return the value as Clob.
	 */
	public default Clob getClob() throws JdbxException
	{
		return get(GetAccessors.CLOB);
	}


	/**
	 * @return the value as double. If the value is null, 0.0 returned.
	 */
	public default double getDouble() throws JdbxException
	{
		return getDouble(0.0);
	}


	/**
	 * @return the value as double. If the value is null, the default value is returned.
	 * @param defaultValue the default value
	 */
	public default double getDouble(double defaultValue) throws JdbxException
	{
		Double value = getDoubleObject();
		return value != null ? value.doubleValue() : defaultValue;
	}


	/**
	 * @return the value as Double.
	 */
	public default Double getDoubleObject() throws JdbxException
	{
		return get(GetAccessors.DOUBLE);
	}


	/**
	 * @return the value as float. If the value is null, 0.0f is returned.
	 */
	public default float getFloat() throws JdbxException
	{
		Float value = getFloatObject();
		return value != null ? value.floatValue() : (float)0;
	}


	/**
	 * @return the value as float. If the value is null, the default value is returned.
	 * @param defaultValue the default value
	 */
	public default float getFloat(float defaultValue) throws JdbxException
	{
		Float value = getFloatObject();
		return value != null ? value.floatValue() : defaultValue;
	}


	/**
	 * @return the value as Float.
	 */
	public default Float getFloatObject() throws JdbxException
	{
		return get(GetAccessors.FLOAT);
	}


	/**
	 * @return the value as int. If the value is null, 0 is returned.
	 */
	public default int getInt() throws JdbxException
	{
		return getInt(0);
	}


	/**
	 * @return the value as int. If the value is null, the default value is returned.
	 * @param defaultValue the default value
	 */
	public default int getInt(int defaultValue) throws JdbxException
	{
		Integer value = getInteger();
		return value != null ? value.intValue() : defaultValue;
	}


	/**
	 * @return the value as Integer.
	 */
	public default Integer getInteger() throws JdbxException
	{
		return get(GetAccessors.INTEGER);
	}


	/**
	 * @return the value as LocalDate.
	 */
	public default LocalDate getLocalDate() throws JdbxException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return getObject(LocalDate.class);
	}


	/**
	 * @return the value as LocalTime.
	 */
	public default LocalTime getLocalTime() throws JdbxException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return getObject(LocalTime.class);
	}


	/**
	 * @return the value as long. If the value is null, 0L is returned.
	 */
	public default long getLong() throws JdbxException
	{
		return getLong(0L);
	}


	/**
	 * @return the value as long. If the value is null, the default value is returned.
	 * @param defaultValue the default value
	 */
	public default long getLong(long defaultValue) throws JdbxException
	{
		Long value = getLongObject();
		return value != null ? value.longValue() : defaultValue;
	}


	/**
	 * @return the value as Long.
	 */
	public default Long getLongObject() throws JdbxException
	{
		return get(GetAccessors.LONG);
	}


	/**
	 * @return the value as Reader based on NChar character stream.
	 * @see ResultSet#getNCharacterStream
	 */
	public default Reader getNCharacterStream() throws JdbxException
	{
		return get(GetAccessors.NCHARACTERSTREAM);
	}


	/**
	 * @return the value as NClob.
	 */
	public default NClob getNClob() throws JdbxException
	{
		return get(GetAccessors.NCLOB);
	}


	/**
	 * @return the value as NString
	 * @see ResultSet#getNString
	 */
	public default String getNString() throws JdbxException
	{
		return get(GetAccessors.NSTRING);
	}


	/**
	 * @return the value as Object
	 * @see ResultSet#getNString
	 */
	public default Object getObject() throws JdbxException
	{
		return get(GetAccessors.OBJECT);
	}


	/**
	 * @return the value as an Object parsed as/casted to a certain type.
	 * @param type the class of the type
	 * @param<T> the type of the value
	 */
	public abstract <T> T getObject(Class<T> type) throws JdbxException;


	/**
	 * @return the value as Ref.
	 */
	public default Ref getRef() throws JdbxException
	{
		return get(GetAccessors.REF);
	}


	/**
	 * @return the value as RowId.
	 */
	public default RowId getRowId() throws JdbxException
	{
		return get(GetAccessors.ROWID);
	}


	/**
	 * @return the value as short. If the value is null, (short)0 is returned.
	 */
	public default short getShort() throws JdbxException
	{
		return getShort((short)0);
	}


	/**
	 * @return the value as short. If the value is null, the default value is returned.
	 * @param defaultValue the default value
	 */
	public default short getShort(short defaultValue) throws JdbxException
	{
		Short value = getShortObject();
		return value != null ? value.shortValue() : defaultValue;
	}


	/**
	 * @return the value as Short.
	 */
	public default Short getShortObject() throws JdbxException
	{
		return get(GetAccessors.SHORT);
	}


	/**
	 * @return the value as java.sql.Date.
	 */
	public default java.sql.Date getSqlDate() throws JdbxException
	{
		return get(GetAccessors.SQLDATE);
	}


	/**
	 * @return the value as java.sql.Time.
	 */
	public default java.sql.Time getSqlTime() throws JdbxException
	{
		return get(GetAccessors.SQLTIME);
	}


	/**
	 * @return the value as java.sql.Timestamp.
	 */
	public default java.sql.Timestamp getSqlTimestamp() throws JdbxException
	{
		return get(GetAccessors.SQLTIMESTAMP);
	}


	/**
	 * @return the value as SQLXML.
	 */
	public default SQLXML getSqlXml() throws JdbxException
	{
		return get(GetAccessors.SQLXML);
	}


	/**
	 * @return the value as String.
	 */
	public default String getString() throws JdbxException
	{
		return get(GetAccessors.STRING);
	}


	public abstract Object get(Map<String,Class<?>> map) throws JdbxException;


	/**
	 * @return the value as Object using the given accessors.
	 */
	public abstract <T> T get(GetAccessors<T> accessors) throws JdbxException;
}
