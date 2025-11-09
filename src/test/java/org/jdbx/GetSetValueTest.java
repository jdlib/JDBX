package org.jdbx;


import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jdbx.sql.CreateTable;
import org.jdbx.sql.SqlInsert;
import org.jdbx.test.JdbxTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class GetSetValueTest extends JdbxTest
{
	@Test public void testNumberValues()
	{
		new Builder()
			.col("TINYINT", Byte.valueOf((byte)1), GetResultValue::getByte, SetValue::setByte)
			.col("SMALLINT", Short.valueOf((short)2), GetResultValue::getShort, SetValue::setShort)
			.col("INTEGER", Integer.valueOf(3), GetResultValue::getInteger, SetValue::setInteger)
			.col("BIGINT", Long.valueOf(4L), GetResultValue::getLong, SetValue::setLong)
			.col("REAL", Float.valueOf(5.5f), GetResultValue::getFloat, SetValue::setFloat)
			.col("DOUBLE PRECISION", Double.valueOf(6.6), GetResultValue::getDouble, SetValue::setDouble)
			.col("NUMERIC(20,3)", new BigDecimal("7.765"), GetResultValue::getBigDecimal, SetValue::setBigDecimal)
			.run();
	}


	@Test public void testDateTimeValues()
	{
		// truncate nanos for h2 comparison
		LocalDateTime dt = LocalDateTime.now().withNano(0);
		new Builder()
			.col("TIMESTAMP", Timestamp.valueOf(dt), GetResultValue::getSqlTimestamp, SetValue::setSqlTimestamp)
			.col("TIMESTAMP", dt, GetResultValue::getLocalDateTime, SetValue::setLocalDateTime)
			.col("DATE", java.sql.Date.valueOf(dt.toLocalDate()), GetResultValue::getSqlDate, SetValue::setSqlDate)
			.col("DATE", dt.toLocalDate(), GetResultValue::getLocalDate, SetValue::setLocalDate)
			.col("TIME", java.sql.Time.valueOf(dt.toLocalTime()), GetResultValue::getSqlTime, SetValue::setSqlTime)
			.col("TIME", dt.toLocalTime(), GetResultValue::getLocalTime, SetValue::setLocalTime)
			.run();
	}


	@Test public void testMiscValues() throws Exception
	{
		Array sqlArray = con().createArrayOf("VARCHAR", new String[] {"1", "2"});
		new Builder()
			.col("BOOLEAN", Boolean.TRUE, GetResultValue::getBoolean, SetValue::setBoolean)
			.col("VARCHAR(20)", "abc", GetResultValue::getString, SetValue::setString)
			.col("VARCHAR(10) ARRAY", sqlArray, GetResultValue::getArray, SetValue::setArray, (a1,a2) -> assertArrayEquals(toArray(a1),toArray(a2)))
			.col("VARBINARY(10)", "hello".getBytes(), GetResultValue::getBytes, SetValue::setBytes, (b1,b2) -> assertArrayEquals(b1,b2))
			.run();
	}


	private static Object[] toArray(Array a)
	{
		try
		{
			return (Object[])a.getArray();
		}
		catch (SQLException e)
		{
			throw new IllegalStateException(e);
		}
	}


	private static class Builder
	{
		private static final String TABLE = "grvtest";
		private final List<Column<?>> cols_ = new ArrayList<>();
		private final CreateTable ct = new CreateTable(TABLE);
		private final SqlInsert ins  = new SqlInsert(TABLE);


		public <T> Builder col(String type, T value, Function<GetResultValue, T> getter, BiConsumer<SetValue, ? super T> setter)
		{
			return col(type, value, getter, setter, Assertions::assertEquals);
		}


		public <T> Builder col(String type, T value, Function<GetResultValue, T> getter, BiConsumer<SetValue, ? super T> setter, BiConsumer<T,T> assertion)
		{
			String name = "c" + cols_.size();
			Column<T> col = new Column<>(type, value, getter, setter, assertion);
			cols_.add(col);
			ct.col(name, type);
			ins.colParam(name);
			return this;
		}

		public <T> void run()
		{
			try (StaticStmt stmt = new StaticStmt(con()))
			{
				stmt.update(ct.toString());
				try
				{
					try (PrepStmt pstmt = new PrepStmt(con()))
					{
						pstmt.init(ins.toString());
						for (int i=0; i<cols_.size(); i++)
						{
							@SuppressWarnings("unchecked")
							Column<T> col = (Column<T>)cols_.get(i);
							col.setter.accept(pstmt.param(i+1), col.value);
						}
						pstmt.update().requireCount(1);
					}

					stmt.query("SELECT * FROM " + TABLE).row().required().consume(qr -> {
						for (int i=0; i<cols_.size(); i++)
						{
							@SuppressWarnings("unchecked")
							Column<T> col = (Column<T>)cols_.get(i);
							T actual = col.getter.apply(qr.col(i+1));
							col.assertion.accept(col.value, actual);
						}
					});
				}
				finally
				{
					stmt.update("DROP TABLE " + TABLE);
				}
			}
		}
	}


	private static class Column<T>
	{
		public final String type;
		public final T value;
		public final Function<GetResultValue, T> getter;
		public final BiConsumer<SetValue, ? super T> setter;
		public final BiConsumer<T,T> assertion;


		public Column(String type, T value, Function<GetResultValue, T> getter, BiConsumer<SetValue, ? super T> setter, BiConsumer<T,T> assertion)
		{
			this.type = type;
			this.value = value;
			this.getter = getter;
			this.setter = setter;
			this.assertion = assertion;
		}
	}
}
