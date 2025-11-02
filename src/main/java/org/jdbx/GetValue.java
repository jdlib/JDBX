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
	 * @return the value as an BigDecimal.
	 */
	public default BigDecimal getBigDecimal() throws JdbxException
	{
		return get(GetAccessors.BIGDECIMAL);
	}


	/**
	 * @return the value as an Blob.
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


	public default byte getByte() throws JdbxException
	{
		Byte value = getByteObject();
		return value != null ? value.byteValue() : (byte)0;
	}


	public default Byte getByteObject() throws JdbxException
	{
		return get(GetAccessors.BYTE);
	}


	public default byte[] getBytes() throws JdbxException
	{
		return get(GetAccessors.BYTES);
	}


	public default Reader getCharacterStream() throws JdbxException
	{
		return get(GetAccessors.CHARACTERSTREAM);
	}


	public default Clob getClob() throws JdbxException
	{
		return get(GetAccessors.CLOB);
	}


	public default double getDouble() throws JdbxException
	{
		return getDouble(0.0);
	}


	public default double getDouble(double defaultValue) throws JdbxException
	{
		Double value = getDoubleObject();
		return value != null ? value.doubleValue() : defaultValue;
	}


	public default Double getDoubleObject() throws JdbxException
	{
		return get(GetAccessors.DOUBLE);
	}


	public default float getFloat() throws JdbxException
	{
		Float value = getFloatObject();
		return value != null ? value.floatValue() : (float)0;
	}


	public default Float getFloatObject() throws JdbxException
	{
		return get(GetAccessors.FLOAT);
	}


	public default int getInt() throws JdbxException
	{
		return getInt(0);
	}


	public default int getInt(int defaultValue) throws JdbxException
	{
		Integer value = getInteger();
		return value != null ? value.intValue() : defaultValue;
	}


	public default Integer getInteger() throws JdbxException
	{
		return get(GetAccessors.INTEGER);
	}


	public default LocalDate getLocalDate() throws JdbxException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return get(LocalDate.class);
	}


	public default LocalTime getLocalTime() throws JdbxException
	{
		// http://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/jdbc_42.html
		return get(LocalTime.class);
	}


	public default long getLong() throws JdbxException
	{
		return getLong(0);
	}


	public default long getLong(long defaultValue) throws JdbxException
	{
		Long value = getLongObject();
		return value != null ? value.longValue() : defaultValue;
	}


	public default Long getLongObject() throws JdbxException
	{
		return get(GetAccessors.LONG);
	}


	public default Reader getNCharacterStream() throws JdbxException
	{
		return get(GetAccessors.NCHARACTERSTREAM);
	}


	public default NClob getNClob() throws JdbxException
	{
		return get(GetAccessors.NCLOB);
	}


	public default String getNString() throws JdbxException
	{
		return get(GetAccessors.NSTRING);
	}


	public default Object getObject() throws JdbxException
	{
		return get(GetAccessors.OBJECT);
	}


	public default Ref getRef() throws JdbxException
	{
		return get(GetAccessors.REF);
	}


	public default RowId getRowId() throws JdbxException
	{
		return get(GetAccessors.ROWID);
	}


	public default short getShort() throws JdbxException
	{
		Short value = getShortObject();
		return value != null ? value.shortValue() : (short)0;
	}


	public default Short getShortObject() throws JdbxException
	{
		return get(GetAccessors.SHORT);
	}


	public default java.sql.Date getSqlDate() throws JdbxException
	{
		return get(GetAccessors.SQLDATE);
	}


	public default java.sql.Time getSqlTime() throws JdbxException
	{
		return get(GetAccessors.SQLTIME);
	}


	public default java.sql.Timestamp getSqlTimestamp() throws JdbxException
	{
		return get(GetAccessors.SQLTIMESTAMP);
	}


	public default SQLXML getSqlXml() throws JdbxException
	{
		return get(GetAccessors.SQLXML);
	}


	public default String getString() throws JdbxException
	{
		return get(GetAccessors.STRING);
	}


	public abstract <T> T get(Class<T> type) throws JdbxException;


	public abstract Object get(Map<String,Class<?>> map) throws JdbxException;


	public abstract <T> T get(GetAccessors<T> accessors) throws JdbxException;
}
