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
package org.jdbx.demo;


import java.sql.ResultSet;
import org.jdbx.JdbxException;
import org.jdbx.QueryCursor;
import org.jdbx.ResultIterator;


/**
 * Sample bean used in the demos.
 */
@SuppressWarnings("unused")
public class City
{
	/**
	 * Reads fields from current result row (City.*) and returns a new City object.
	 */
	public static City read(ResultSet r) throws JdbxException
	{
		ResultIterator it 	= ResultIterator.of(r);
		City city 			= new City();
		city.id 			= it.getInteger();
		city.name			= it.getString();
		city.country		= it.getString();
		city.size			= it.getInt();
		return city;
	}

	
	/**
	 * Reads fields from current result row (City.*) and returns a new City object.
	 */
	public static City read(QueryCursor cursor) throws JdbxException
	{
		return read(cursor.getJdbcResult());
	}


	private Integer id;
	private String name;
	private String country;
	private int size;
}