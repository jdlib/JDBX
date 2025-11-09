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
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


/**
 * SetValue defines operations which Sets a value.
 * Result columns and statement parameters implement this interface.
 */
interface SetValue
{
	/**
	 * Sets an Array value.
	 * @param value the value
	 */
	public default void setArray(Array value) throws JdbxException
	{
		set(SetAccessors.ARRAY, value);
	}


	/**
	 * Sets a BigDecimal value.
	 * @param value the value
	 */
	public default void setBigDecimal(BigDecimal value) throws JdbxException
	{
		set(SetAccessors.BIGDECIMAL, value);
	}


	/**
	 * Sets a Blob value.
	 * @param value the value
	 */
	public default void setBlob(Blob value) throws JdbxException
	{
		set(SetAccessors.BLOB, value);
	}


	/**
	 * Sets a boolean value.
	 * @param value the value
	 */
	public default void setBoolean(boolean value) throws JdbxException
	{
		setBoolean(Boolean.valueOf(value));
	}


	/**
	 * Sets a Boolean value.
	 * @param value the value
	 */
	public default void setBoolean(Boolean value) throws JdbxException
	{
		set(SetAccessors.BOOLEAN, value);
	}


	/**
	 * Sets a byte value.
	 * @param value the value
	 */
	public default void setByte(byte value) throws JdbxException
	{
		setByte(Byte.valueOf(value));
	}


	/**
	 * Sets a Byte value.
	 * @param value the value
	 */
	public default void setByte(Byte value) throws JdbxException
	{
		set(SetAccessors.BYTE, value);
	}


	/**
	 * Sets a byte array value.
	 * @param value the value
	 */
	public default void setBytes(byte[] value) throws JdbxException
	{
		set(SetAccessors.BYTES, value);
	}


	/**
	 * Sets a Reader value.
	 * @param value the value
	 */
	public default void setCharacterStream(Reader value) throws JdbxException
	{
		set(SetAccessors.CHARACTERSTREAM, value);
	}


	/**
	 * Sets a Clob value.
	 * @param value the value
	 */
	public default void setClob(Clob value) throws JdbxException
	{
		set(SetAccessors.CLOB, value);
	}


	/**
	 * Sets a double value.
	 * @param value the value
	 */
	public default void setDouble(double value) throws JdbxException
	{
		setDouble(Double.valueOf(value));
	}


	/**
	 * Sets a Double value.
	 * @param value the value
	 */
	public default void setDouble(Double value) throws JdbxException
	{
		set(SetAccessors.DOUBLE, value);
	}


	/**
	 * Sets a float value.
	 * @param value the value
	 */
	public default void setFloat(float value) throws JdbxException
	{
		setFloat(Float.valueOf(value));
	}


	/**
	 * Sets a Float value.
	 * @param value the value
	 */
	public default void setFloat(Float value) throws JdbxException
	{
		set(SetAccessors.FLOAT, value);
	}


	/**
	 * Sets a int value.
	 * @param value the value
	 */
	public default void setInt(int value) throws JdbxException
	{
		setInteger(Integer.valueOf(value));
	}


	/**
	 * Sets a Integer value.
	 * @param value the value
	 */
	public default void setInteger(Integer value) throws JdbxException
	{
		set(SetAccessors.INTEGER, value);
	}


	/**
	 * Sets a LocalDate value.
	 * @param value the value
	 */
	public default void setLocalDate(LocalDate value) throws JdbxException
	{
		Date dt = value != null ? Date.valueOf(value) : null;
		setSqlDate(dt);
	}


	/**
	 * Sets a LocalDateTime value.
	 * @param value the value
	 */
	public default void setLocalDateTime(LocalDateTime value) throws JdbxException
	{
		Timestamp ts = value != null ? Timestamp.valueOf(value) : null;
		setSqlTimestamp(ts);
	}


	/**
	 * Sets a LocalTime value.
	 * @param value the value
	 */
	public default void setLocalTime(LocalTime value) throws JdbxException
	{
		Time tm = value != null ? Time.valueOf(value) : null;
		setSqlTime(tm);
	}


	/**
	 * Sets a long value.
	 * @param value the value
	 */
	public default void setLong(long value) throws JdbxException
	{
		setLong(Long.valueOf(value));
	}


	/**
	 * Sets a Long value.
	 * @param value the value
	 */
	public default void setLong(Long value) throws JdbxException
	{
		set(SetAccessors.LONG, value);
	}


	/**
	 * Sets a NCharacterStream value.
	 * @param value the value
	 */
	public default void setNCharacterStream(Reader value) throws JdbxException
	{
		set(SetAccessors.NCHARACTERSTREAM, value);
	}


	/**
	 * Sets a NClob value.
	 * @param value the value
	 */
	public default void setNClob(NClob value) throws JdbxException
	{
		set(SetAccessors.NCLOB, value);
	}


	/**
	 * Sets a NString value.
	 * @param value the value
	 */
	public default void setNString(String value) throws JdbxException
	{
		set(SetAccessors.NSTRING, value);
	}


	/**
	 * Sets a Object value.
	 * @param value the value
	 */
	public default void setObject(Object value) throws JdbxException
	{
		set(SetAccessors.OBJECT, value);
	}


	/**
	 * Sets a Ref value.
	 * @param value the value
	 */
	public default void setRef(Ref value) throws JdbxException
	{
		set(SetAccessors.REF, value);
	}


	/**
	 * Sets a RowId value.
	 * @param value the value
	 */
	public default void setRowId(RowId value) throws JdbxException
	{
		set(SetAccessors.ROWID, value);
	}


	/**
	 * Sets a short value.
	 * @param value the value
	 */
	public default void setShort(short value) throws JdbxException
	{
		setShort(Short.valueOf(value));
	}


	/**
	 * Sets a Short value.
	 * @param value the value
	 */
	public default void setShort(Short value) throws JdbxException
	{
		set(SetAccessors.SHORT, value);
	}


	/**
	 * Sets a java.sql.Date value.
	 * @param value the value
	 */
	public default void setSqlDate(Date value) throws JdbxException
	{
		set(SetAccessors.SQLDATE, value);
	}


	/**
	 * Sets a java.sql.Time value.
	 * @param value the value
	 */
	public default void setSqlTime(Time value) throws JdbxException
	{
		set(SetAccessors.SQLTIME, value);
	}


	/**
	 * Sets a java.sql.Timestamp value.
	 * @param value the value
	 */
	public default void setSqlTimestamp(Timestamp value) throws JdbxException
	{
		set(SetAccessors.SQLTIMESTAMP, value);
	}


	/**
	 * Sets a SQLXML value.
	 * @param value the value
	 */
	public default void setSqlXml(SQLXML value) throws JdbxException
	{
		set(SetAccessors.SQLXML, value);
	}


	/**
	 * Sets a String value.
	 * @param value the value
	 */
	public default void setString(String value) throws JdbxException
	{
		set(SetAccessors.STRING, value);
	}


	/**
	 * Sets a value using the given accessors.
	 * @param accessors some accessors
	 * @param value the value
	 */
	public abstract <T> void set(SetAccessors<T> accessors, T value) throws JdbxException;
}
