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


import java.sql.ResultSet;


/**
 * Holdability is an Enum for the JDBC constants
 * ResultSet.HOLD_CURSORS_OVER_COMMIT and ResultSet.CLOSE_CURSORS_AT_COMMIT.
 * Given a JDBX statement object, you can configure the result holdability as follows:
 * <p><code>stmt.options().resultHoldability(Holdability.nnn)</code>
 */
public enum Holdability implements JdbcEnum
{
	HOLD_OVER_COMMIT(ResultSet.HOLD_CURSORS_OVER_COMMIT),
	CLOSE_AT_COMMIT(ResultSet.CLOSE_CURSORS_AT_COMMIT),
	INVALID(-1);


	/**
	 * Maps codes to enums.
	 */
	public static final Map<Holdability> MAP = new Map<>(Holdability.class, INVALID);


	Holdability(int code)
	{
		code_ = code;
	}


	/**
	 * Returns the JDBC constant associated with the enum.
	 */
	@Override public int getCode()
	{
		return code_;
	}


	private int code_;
}
