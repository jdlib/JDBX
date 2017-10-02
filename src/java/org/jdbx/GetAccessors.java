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


import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import org.jdbx.function.GetForIndex;
import org.jdbx.function.GetForName;
import java.net.URL;


/**
 * GetAccessors stores method handles for getter methods in ResultSet and CallableStatement.
 */
class GetAccessors<T>
{
	public static final GetAccessors<Array> ARRAY = new GetAccessors<>(
		ResultSet::getArray,
		ResultSet::getArray,
		CallableStatement::getArray,
		CallableStatement::getArray);

	public static final GetAccessors<InputStream> ASCIISTREAM = new GetAccessors<>(
		ResultSet::getAsciiStream,
		ResultSet::getAsciiStream,
		null,
		null);

	public static final GetAccessors<BigDecimal> BIGDECIMAL = new GetAccessors<>(
		ResultSet::getBigDecimal,
		ResultSet::getBigDecimal,
		CallableStatement::getBigDecimal,
		CallableStatement::getBigDecimal);

	public static final GetAccessors<InputStream> BINARYTREAM = new GetAccessors<>(
		ResultSet::getBinaryStream,
		ResultSet::getBinaryStream,
		null,
		null);

	public static final GetAccessors<Blob> BLOB = new GetAccessors<>(
		ResultSet::getBlob,
		ResultSet::getBlob,
		CallableStatement::getBlob,
		CallableStatement::getBlob);

	public static final GetAccessors<Boolean> BOOLEAN = new GetAccessors<>(
		normResultForIndex(ResultSet::getBoolean),
		normResultForName(ResultSet::getBoolean),
		normCallForIndex(CallableStatement::getBoolean),
		normCallForName(CallableStatement::getBoolean));

	public static final GetAccessors<Byte> BYTE = new GetAccessors<>(
		normResultForIndex(ResultSet::getByte),
		normResultForName(ResultSet::getByte),
		normCallForIndex(CallableStatement::getByte),
		normCallForName(CallableStatement::getByte));

	public static final GetAccessors<byte[]> BYTES = new GetAccessors<>(
		ResultSet::getBytes,
		ResultSet::getBytes,
		CallableStatement::getBytes,
		CallableStatement::getBytes);

	public static final GetAccessors<Reader> CHARACTERSTREAM = new GetAccessors<>(
		ResultSet::getCharacterStream,
		ResultSet::getCharacterStream,
		CallableStatement::getCharacterStream,
		CallableStatement::getCharacterStream);

	public static final GetAccessors<Clob> CLOB = new GetAccessors<>(
		ResultSet::getClob,
		ResultSet::getClob,
		CallableStatement::getClob,
		CallableStatement::getClob);

	public static final GetAccessors<Double> DOUBLE = new GetAccessors<>(
		normResultForIndex(ResultSet::getDouble),
		normResultForName(ResultSet::getDouble),
		normCallForIndex(CallableStatement::getDouble),
		normCallForName(CallableStatement::getDouble));

	public static final GetAccessors<Float> FLOAT = new GetAccessors<>(
		normResultForIndex(ResultSet::getFloat),
		normResultForName(ResultSet::getFloat),
		normCallForIndex(CallableStatement::getFloat),
		normCallForName(CallableStatement::getFloat));

	public static final GetAccessors<Integer> INTEGER = new GetAccessors<>(
		normResultForIndex(ResultSet::getInt),
		normResultForName(ResultSet::getInt),
		normCallForIndex(CallableStatement::getInt),
		normCallForName(CallableStatement::getInt));

	public static final GetAccessors<Long> LONG = new GetAccessors<>(
		normResultForIndex(ResultSet::getLong),
		normResultForName(ResultSet::getLong),
		normCallForIndex(CallableStatement::getLong),
		normCallForName(CallableStatement::getLong));

	public static final GetAccessors<Reader> NCHARACTERSTREAM = new GetAccessors<>(
		ResultSet::getNCharacterStream,
		ResultSet::getNCharacterStream,
		CallableStatement::getNCharacterStream,
		CallableStatement::getNCharacterStream);

	public static final GetAccessors<NClob> NCLOB = new GetAccessors<>(
		ResultSet::getNClob,
		ResultSet::getNClob,
		CallableStatement::getNClob,
		CallableStatement::getNClob);

	public static final GetAccessors<String> NSTRING = new GetAccessors<>(
		ResultSet::getNString,
		ResultSet::getNString,
		CallableStatement::getNString,
		CallableStatement::getNString);

	public static final GetAccessors<Object> OBJECT = new GetAccessors<>(
		ResultSet::getObject,
		ResultSet::getObject,
		CallableStatement::getObject,
		CallableStatement::getObject);

	public static final GetAccessors<Ref> REF = new GetAccessors<>(
		ResultSet::getRef,
		ResultSet::getRef,
		CallableStatement::getRef,
		CallableStatement::getRef);

	public static final GetAccessors<RowId> ROWID = new GetAccessors<>(
		ResultSet::getRowId,
		ResultSet::getRowId,
		CallableStatement::getRowId,
		CallableStatement::getRowId);

	public static final GetAccessors<Short> SHORT = new GetAccessors<>(
		normResultForIndex(ResultSet::getShort),
		normResultForName(ResultSet::getShort),
		normCallForIndex(CallableStatement::getShort),
		normCallForName(CallableStatement::getShort));

	public static final GetAccessors<Date> SQLDATE = new GetAccessors<>(
		ResultSet::getDate,
		ResultSet::getDate,
		CallableStatement::getDate,
		CallableStatement::getDate);

	public static final GetAccessors<SQLXML> SQLXML = new GetAccessors<>(
		ResultSet::getSQLXML,
		ResultSet::getSQLXML,
		CallableStatement::getSQLXML,
		CallableStatement::getSQLXML);

	public static final GetAccessors<Time> SQLTIME = new GetAccessors<>(
		ResultSet::getTime,
		ResultSet::getTime,
		CallableStatement::getTime,
		CallableStatement::getTime);

	public static final GetAccessors<Timestamp> SQLTIMESTAMP = new GetAccessors<>(
		ResultSet::getTimestamp,
		ResultSet::getTimestamp,
		CallableStatement::getTimestamp,
		CallableStatement::getTimestamp);

	public static final GetAccessors<String> STRING = new GetAccessors<>(
		ResultSet::getString,
		ResultSet::getString,
		CallableStatement::getString,
		CallableStatement::getString);

	public static final GetAccessors<URL> URL = new GetAccessors<>(
		ResultSet::getURL,
		ResultSet::getURL,
		CallableStatement::getURL,
		CallableStatement::getURL);

	public GetAccessors
	(
		GetForIndex<ResultSet,T> resultForIndex,
		GetForName<ResultSet,T> resultForName,
		GetForIndex<CallableStatement,T> paramForIndex,
		GetForName<CallableStatement,T>	paramForName
	)
	{
		this.resultForIndex = resultForIndex;
		this.resultForName 	= resultForName;
		this.paramForIndex	= paramForIndex;
		this.paramForName	= paramForName;
	}


	public final GetForIndex<ResultSet,T> resultForIndex;
	public final GetForName<ResultSet,T> resultForName;
	public final GetForIndex<CallableStatement,T> paramForIndex;
	public final GetForName<CallableStatement,T> paramForName;


	private static <T> GetForIndex<ResultSet,T> normResultForIndex(GetForIndex<ResultSet,T> fn)
	{
		return (rs,i) -> {
			T value = fn.get(rs, i);
			return !rs.wasNull() ? value : null;
		};
	}


	private static <T> GetForName<ResultSet,T> normResultForName(GetForName<ResultSet,T> fn)
	{
		return (rs,n) -> {
			T value = fn.get(rs, n);
			return !rs.wasNull() ? value : null;
		};
	}


	private static <T> GetForIndex<CallableStatement,T> normCallForIndex(GetForIndex<CallableStatement,T> fn)
	{
		return (rs,i) -> {
			T value = fn.get(rs, i);
			return !rs.wasNull() ? value : null;
		};
	}


	private static <T> GetForName<CallableStatement,T> normCallForName(GetForName<CallableStatement,T> fn)
	{
		return (rs,n) -> {
			T value = fn.get(rs, n);
			return !rs.wasNull() ? value : null;
		};
	}
}
