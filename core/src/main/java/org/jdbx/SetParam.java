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


import java.sql.PreparedStatement;
import org.jdbx.function.SetForNumber;


interface SetParam<STMT extends PreparedStatement> extends SetValue
{
//	/**
//	 * Sets a byte value.
//	 * @param value the value
//	 */
//	public default void setByte(byte value) throws JdbxException
//	{
//		apply((stmt,i) -> stmt.setByte(i,value));
//	}
//
//
//	/**
//	 * Sets a float value.
//	 * @param value the value
//	 */
//	public default void setFloat(float value) throws JdbxException
//	{
//		apply((stmt,i) -> stmt.setFloat(i,value));
//	}
//
//
//	/**
//	 * Sets a double value.
//	 * @param value the value
//	 */
//	public default void setDouble(double value) throws JdbxException
//	{
//		apply((stmt,i) -> stmt.setDouble(i,value));
//	}
//
//
//	/**
//	 * Sets a int value.
//	 * @param value the value
//	 */
//	public default void setInt(int value) throws JdbxException
//	{
//		apply((stmt,i) -> stmt.setInt(i,value));
//	}
//
//
//	/**
//	 * Sets a long value.
//	 * @param value the value
//	 */
//	public default void setLong(long value) throws JdbxException
//	{
//		apply((stmt,i) -> stmt.setLong(i,value));
//	}
//
//
//	/**
//	 * Sets a short value.
//	 * @param value the value
//	 */
//	public default void setShort(short value) throws JdbxException
//	{
//		apply((stmt,i) -> stmt.setShort(i,value));
//	}
//
//
//	/**
//	 * Sets a Object value.
//	 * @param value the value
//	 */
//	public void set(Object value) throws JdbxException;
//
//
//	/**
//	 * Sets a Object value.
//	 * @param value the value
//	 * @param type the target SQLType
//	 */
//	public void set(Object value, SQLType type) throws JdbxException;
//
//
//	/**
//	 * Sets a Object value.
//	 * @param value the value
//	 * @param setter used to actually the value.
//	 * @param <T> the type of the value
//	 */
//	public <T> void set(T value, SetForNumber<STMT,T> setter) throws JdbxException;
//
//
//	/**
//	 * Calls the runner to perform its action.
//	 * @param runner a function to perform some action
//	 */
//	public void apply(DoForNumber<STMT> runner) throws JdbxException;


	@Override public default <T> void set(SetAccessors<T> accessors, T value) throws JdbxException
	{
		Check.notNull(accessors, "accessors");
		set(accessors.paramForIndex, value);
	}
	
	
	public abstract <T> void set(SetForNumber<PreparedStatement,T> setter, T value) throws JdbxException;
}
