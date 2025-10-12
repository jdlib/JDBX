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
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import org.jdbx.function.SetForNumber;
import org.jdbx.function.SetForName;


/**
 * SetAccessors stores method handles for setter methods in ResultSet and PreparedStatement.
 */
class SetAccessors<T>
{
	public static final SetAccessors<Array> ARRAY = new SetAccessors<>(
		ResultSet::updateArray,
		ResultSet::updateArray,
		PreparedStatement::setArray);

	public static final SetAccessors<InputStream> ASCIISTREAM = new SetAccessors<>(
		ResultSet::updateAsciiStream,
		ResultSet::updateAsciiStream,
		PreparedStatement::setAsciiStream);

	public static final SetAccessors<BigDecimal> BIGDECIMAL = new SetAccessors<>(
		ResultSet::updateBigDecimal,
		ResultSet::updateBigDecimal,
		PreparedStatement::setBigDecimal);

	public static final SetAccessors<InputStream> BINARYTREAM = new SetAccessors<>(
		ResultSet::updateBinaryStream,
		ResultSet::updateBinaryStream,
		PreparedStatement::setBinaryStream);

	public static final SetAccessors<Blob> BLOB = new SetAccessors<>(
		ResultSet::updateBlob,
		ResultSet::updateBlob,
		PreparedStatement::setBlob);

	public static final SetAccessors<Boolean> BOOLEAN = SetAccessors.ofPrimitive(Types.BOOLEAN,
		ResultSet::updateBoolean,
		ResultSet::updateBoolean,
		PreparedStatement::setBoolean);

	public static final SetAccessors<Byte> BYTE = SetAccessors.ofPrimitive(Types.TINYINT,
		ResultSet::updateByte,
		ResultSet::updateByte,
		PreparedStatement::setByte);

	public static final SetAccessors<byte[]> BYTES = new SetAccessors<>(
		ResultSet::updateBytes,
		ResultSet::updateBytes,
		PreparedStatement::setBytes);

	public static final SetAccessors<Reader> CHARACTERSTREAM = new SetAccessors<>(
		ResultSet::updateCharacterStream,
		ResultSet::updateCharacterStream,
		PreparedStatement::setCharacterStream);

	public static final SetAccessors<Clob> CLOB = new SetAccessors<>(
		ResultSet::updateClob,
		ResultSet::updateClob,
		PreparedStatement::setClob);

	public static final SetAccessors<Double> DOUBLE = SetAccessors.ofPrimitive(Types.DOUBLE,
		ResultSet::updateDouble,
		ResultSet::updateDouble,
		PreparedStatement::setDouble);

	public static final SetAccessors<Float> FLOAT = SetAccessors.ofPrimitive(Types.FLOAT,
		ResultSet::updateFloat,
		ResultSet::updateFloat,
		PreparedStatement::setFloat);

	public static final SetAccessors<Integer> INTEGER = SetAccessors.ofPrimitive(Types.INTEGER,
		ResultSet::updateInt,
		ResultSet::updateInt,
		PreparedStatement::setInt);

	public static final SetAccessors<Long> LONG = SetAccessors.ofPrimitive(Types.BIGINT,
		ResultSet::updateLong,
		ResultSet::updateLong,
		PreparedStatement::setLong);

	public static final SetAccessors<Reader> NCHARACTERSTREAM = new SetAccessors<>(
		ResultSet::updateNCharacterStream,
		ResultSet::updateNCharacterStream,
		PreparedStatement::setNCharacterStream);

	public static final SetAccessors<NClob> NCLOB = new SetAccessors<>(
		ResultSet::updateNClob,
		ResultSet::updateNClob,
		PreparedStatement::setNClob);

	public static final SetAccessors<String> NSTRING = new SetAccessors<>(
		ResultSet::updateNString,
		ResultSet::updateNString,
		PreparedStatement::setNString);

	public static final SetAccessors<Object> OBJECT = new SetAccessors<>(
		ResultSet::updateObject,
		ResultSet::updateObject,
		PreparedStatement::setObject);

	public static final SetAccessors<Ref> REF = new SetAccessors<>(
		ResultSet::updateRef,
		ResultSet::updateRef,
		PreparedStatement::setRef);

	public static final SetAccessors<RowId> ROWID = new SetAccessors<>(
		ResultSet::updateRowId,
		ResultSet::updateRowId,
		PreparedStatement::setRowId);

	public static final SetAccessors<Short> SHORT = new SetAccessors<>(
		ResultSet::updateShort,
		ResultSet::updateShort,
		PreparedStatement::setShort);

	public static final SetAccessors<Date> SQLDATE = new SetAccessors<>(
		ResultSet::updateDate,
		ResultSet::updateDate,
		PreparedStatement::setDate);

	public static final SetAccessors<SQLXML> SQLXML = new SetAccessors<>(
		ResultSet::updateSQLXML,
		ResultSet::updateSQLXML,
		PreparedStatement::setSQLXML);

	public static final SetAccessors<Time> SQLTIME = new SetAccessors<>(
		ResultSet::updateTime,
		ResultSet::updateTime,
		PreparedStatement::setTime);

	public static final SetAccessors<Timestamp> SQLTIMESTAMP = new SetAccessors<>(
		ResultSet::updateTimestamp,
		ResultSet::updateTimestamp,
		PreparedStatement::setTimestamp);

	public static final SetAccessors<String> STRING = new SetAccessors<>(
		ResultSet::updateString,
		ResultSet::updateString,
		PreparedStatement::setString);

	public static final SetAccessors<URL> URL = new SetAccessors<>(
		null,
		null,
		PreparedStatement::setURL);

	
	public SetAccessors
	(
		SetForNumber<ResultSet,T> resultForIndex,
		SetForName<ResultSet,T> resultForName,
		SetForNumber<PreparedStatement,T> paramForIndex
	)
	{
		this.resultForIndex = resultForIndex;
		this.resultForName 	= resultForName;
		this.paramForIndex	= paramForIndex;
	}

	

	public static <T> SetAccessors<T> ofPrimitive(int sqlType,
		SetForNumber<ResultSet,T> resultForIndex,
		SetForName<ResultSet,T> resultForName,
		SetForNumber<PreparedStatement,T> paramForIndex)
	{
		return new SetAccessors<>(
			(ResultSet r, int i, T v) -> {
				if (v == null) { r.updateNull(i); } else { resultForIndex.set(r, i, v); }
			},
			(ResultSet r, String n, T v) -> {
				if (v == null) { r.updateNull(n); } else { resultForName.set(r, n, v); }
			},
			(PreparedStatement p,int i,T v) -> {
				if (v == null) { p.setNull(i, sqlType); } else { paramForIndex.set(p, i, v); 
			}
		});
	}
	
    
    public final SetForNumber<ResultSet,T> resultForIndex;
	public final SetForName<ResultSet,T> resultForName;
	public final SetForNumber<PreparedStatement,T> paramForIndex;
}
