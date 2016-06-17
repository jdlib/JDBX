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
import java.sql.SQLType;
import org.jdbx.function.DoForIndex;
import org.jdbx.function.SetForIndex;


interface SetIndexedParam<STMT extends PreparedStatement>
{
	/**
	 * Sets a byte value.
	 * @param value the value
	 */
	public default void setByte(byte value) throws JdbException
	{
		apply((stmt,i) -> stmt.setByte(i,value));
	}


	/**
	 * Sets a float value.
	 * @param value the value
	 */
	public default void setFloat(float value) throws JdbException
	{
		apply((stmt,i) -> stmt.setFloat(i,value));
	}


	/**
	 * Sets a double value.
	 * @param value the value
	 */
	public default void setDouble(double value) throws JdbException
	{
		apply((stmt,i) -> stmt.setDouble(i,value));
	}


	/**
	 * Sets a int value.
	 * @param value the value
	 */
	public default void setInt(int value) throws JdbException
	{
		apply((stmt,i) -> stmt.setInt(i,value));
	}


	/**
	 * Sets a long value.
	 * @param value the value
	 */
	public default void setLong(long value) throws JdbException
	{
		apply((stmt,i) -> stmt.setLong(i,value));
	}


	/**
	 * Sets a short value.
	 * @param value the value
	 */
	public default void setShort(short value) throws JdbException
	{
		apply((stmt,i) -> stmt.setShort(i,value));
	}


	/**
	 * Sets a Object value.
	 * @param value the value
	 */
	public void set(Object value) throws JdbException;


	/**
	 * Sets a Object value.
	 * @param value the value
	 * @param type the target SQLType
	 */
	public void set(Object value, SQLType type) throws JdbException;


	/**
	 * Sets a Object value.
	 * @param value the value
	 * @param setter used to actually the value.
	 * @param <T> the type of the value
	 */
	public <T> void set(T value, SetForIndex<STMT,T> setter) throws JdbException;


	/**
	 * Calls the runner to perform its action.
	 * @param runner a function to perform some action
	 */
	public void apply(DoForIndex<STMT> runner) throws JdbException;
}
