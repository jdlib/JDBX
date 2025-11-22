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


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.DoForNumber;
import org.jdbx.function.DoForName;
import org.jdbx.function.GetForNumber;
import org.jdbx.function.GetForName;
import org.jdbx.function.SetForNumber;
import org.jdbx.function.SetForName;
import org.jdbx.function.Unchecked;


/**
 * CallStmt allows to call SQL stored procedures.
 * It wraps {@link CallableStatement}.
 */
public class CallStmt extends Stmt
{
	/**
	 * Creates a new CallStmt.
	 * @param dataSource provides a connection
	 */
	public CallStmt(DataSource dataSource) throws JdbxException
	{
		super(dataSource);
	}


	/**
	 * Creates a new CallStmt.
	 * @param supplier provides a connection
	 * @param closeCon defines if the connection should be closed when the statement is closed.
	 */
	public CallStmt(CheckedSupplier<Connection> supplier, boolean closeCon) throws JdbxException
	{
		super(supplier, closeCon);
	}


	/**
	 * Creates a new CallStmt.
	 * @param con the connection used by the statement. The connection is not closed
	 * 		when the statement is closed.
	 */
	public CallStmt(Connection con)
	{
		this(con, false);
	}


	/**
	 * Creates a new CallStmt.
	 * @param con the connection used by the statement
	 * @param closeCon defines if the connection should be closed when the statement is closed.
	 */
	public CallStmt(Connection con, boolean closeCon)
	{
		super(con, closeCon);
	}


	/**
	 * Returns the JDBC CallableStatement used by the CallStmt.
	 * This method can only be called if {@link #init(String) initialized}.
	 */
	@Override public CallableStatement getJdbcStmt() throws JdbxException
	{
		checkInitialized();
		return (CallableStatement)jdbcStmt_;
	}


	/**
	 * Returns the ParameterMetaData.
	 * @return the meta data
	 */
	public ParameterMetaData getParamMetaData() throws JdbxException
	{
		return get(PreparedStatement::getParameterMetaData);
	}


	//------------------------------
	// init
	//------------------------------


	/**
	 * Initializes the SQL command which is executed by the CallStmt.
	 * @param sql the sql command
	 * @return this
	 */
	public CallStmt init(String sql) throws JdbxException
	{
		Check.notNull(sql, "sql");
		checkOpen();

		try
		{
			// close old statement if not null
			if (jdbcStmt_ != null)
			{
				Statement old = jdbcStmt_;
				jdbcStmt_ = null;
				old.close();
			}

			// create the new statement
			jdbcStmt_ 	= createJdbcStmt(sql);
			// assign the sql once the jdbc statement creation succeeded
			sql_ 	   	= sql;

			return this;
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	private CallableStatement createJdbcStmt(String sql) throws Exception
	{
		CallableStatement stmt = con_.prepareCall(sql,
			StmtOptions.getResultType(options_).getCode(),
			StmtOptions.getResultConcurrency(options_).getCode(),
			StmtOptions.getResultHoldability(options_).getCode());

		if (options_ != null)
			options_.applyOptionValues(stmt);

		return stmt;
	}


	/**
	 * Returns if the CallStmt is initialized.
	 * @see #init(String)
	 */
	@Override public boolean isInitialized()
	{
		return jdbcStmt_ != null;
	}


	//------------------------------
	// params
	//------------------------------


	/**
	 * Clears the parameters.
	 * @return this
	 */
	public CallStmt clearParams() throws JdbxException
	{
		checkInitialized();
		call(PreparedStatement::clearParameters);
		return this;
	}


	/**
	 * Sets the value of the parameter with the given number.
	 * @param number a parameter number, starting at 1.
	 * @param value a parameter value
	 * @return this
	 */
	public CallStmt param(int number, Object value) throws JdbxException
	{
		param(number).setObject(value);
		return this;
	}


	/**
	 * Returns a IndexedParam object to set or get the value of a parameter by number.
	 * @param number a parameter number, starting at 1.
	 * @return the IndexedParam
	 */
	public NumberedParam param(int number)
	{
		return new NumberedParam(number);
	}


	/**
	 * Allows to register an out parameter of CallStmt.
	 */
	public interface RegisterOut<P>
	{
		public P out(int sqlType) throws JdbxException;


		public P out(int sqlType, int scale) throws JdbxException;


		public P out(SQLType sqlType) throws JdbxException;


		public P out(SQLType sqlType, int scale) throws JdbxException;


		public P out(SQLType sqlType, String typeName) throws JdbxException;
	}


	/**
	 * A builder class to manage a parameter value specified by a parameter number.
	 */
	public class NumberedParam implements GetValue, RegisterOut<NumberedParam>, SetParam<CallableStatement>
	{
		private NumberedParam(int number)
		{
			number_ = Check.number(number);
		}


		@Override public NumberedParam out(int sqlType) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(number_, sqlType));
			return this;
		}


		@Override public NumberedParam out(int sqlType, int scale) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(number_, sqlType, scale));
			return this;
		}


		@Override public NumberedParam out(SQLType sqlType) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(number_, sqlType));
			return this;
		}


		@Override public NumberedParam out(SQLType sqlType, int scale) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(number_, sqlType, scale));
			return this;
		}


		@Override public NumberedParam out(SQLType sqlType, String typeName) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(number_, sqlType, typeName));
			return this;
		}


		@Override public <T> T getObject(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			try
			{
				return getJdbcStmt().getObject(number_, type);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public Object getObject(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			try
			{
				return getJdbcStmt().getObject(number_, map);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> T get(GetAccessors<T> accessors) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			return get(accessors.paramForNumber);
		}


		public <T> T get(GetForNumber<CallableStatement,T> getter) throws JdbxException
		{
			Check.notNull(getter, "getter");
			try
			{
				return getter.get(getJdbcStmt(), number_);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> void set(SetForNumber<PreparedStatement,T> setter, T value) throws JdbxException
		{
			Check.notNull(setter, "setter");
			try
			{
				setter.set(getJdbcStmt(), number_, value);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		public <T> void apply(DoForNumber<CallableStatement> runner) throws JdbxException
		{
			Check.notNull(runner, "runner");
			Unchecked.run(() -> runner.accept(getJdbcStmt(), number_));
		}


		private final int number_;
	}


	/**
	 * Returns a parameter object to set the value of a parameter by name.
	 * @param name the parameter name
	 * @return the parameter object
	 */
	public NamedParam param(String name)
	{
		return new NamedParam(name);
	}


	/**
	 * A builder class to manage a parameter value specified by a parameter name.
	 */
	public class NamedParam implements GetValue, RegisterOut<NamedParam>
	{
		private NamedParam(String name)
		{
			name_ = Check.name(name);
		}


		@Override public NamedParam out(int sqlType) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(name_, sqlType));
			return this;
		}


		@Override public NamedParam out(int sqlType, int scale) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(name_, sqlType, scale));
			return this;
		}


		@Override public NamedParam out(SQLType sqlType) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(name_, sqlType));
			return this;
		}


		@Override public NamedParam out(SQLType sqlType, int scale) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(name_, sqlType, scale));
			return this;
		}


		@Override public NamedParam out(SQLType sqlType, String typeName) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(name_, sqlType, typeName));
			return this;
		}


		public void set(Object value) throws JdbxException
		{
			set(value, CallableStatement::setObject);
		}


		public void set(Object value, SQLType type) throws JdbxException
		{
			Check.notNull(type, "type");
			Unchecked.run(() -> getJdbcStmt().setObject(name_, value, type));
		}


		public <T> void set(T value, SetForName<CallableStatement,T> setter) throws JdbxException
		{
			Check.notNull(setter, "setter");
			Unchecked.run(() -> setter.set(getJdbcStmt(), name_, value));
		}


		public <T> void apply(DoForName<CallableStatement> runner) throws JdbxException
		{
			Check.notNull(runner, "runner");
			Unchecked.run(() -> runner.accept(getJdbcStmt(), name_));
		}


		@Override public <T> T getObject(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			try
			{
				return getJdbcStmt().getObject(name_, type);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public Object getObject(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			try
			{
				return getJdbcStmt().getObject(name_, map);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		public <T> T get(GetForName<CallableStatement,T> getter) throws JdbxException
		{
			Check.notNull(getter, "getter");
			try
			{
				return getter.get(getJdbcStmt(), name_);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> T get(GetAccessors<T> accessors) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			return get(accessors.paramForName);
		}


		private final String name_;
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * @return a Query object to run the SQL query command.
	 * Note that actual query on JDBC level may not be run until a terminal operation of the
	 * method chain is executed.
	 */
	public Query query() throws JdbxException
	{
		checkInitialized();
		return new PrepStmtQuery(this::getJdbcStmt);
	}


	//------------------------------
	// execute
	//------------------------------


	/**
	 * Returns a Execute object which can be used to execute commands which
	 * return multiple results
	 * @return the Execute object
	 */
	public Execute createExecute()
	{
		checkInitialized();
		return new PrepStmtExecute(this::getJdbcStmt);
	}


	/**
	 * Executes the CallStmt.
	 * @return the ExecuteResult
	 */
	public ExecuteResult execute()
	{
		return createExecute().run();
	}


	//------------------------------
	// batch
	//------------------------------


	/**
	 * The batch class uses by CallStmt.
	 */
	public class CallBatch extends BatchGetCols
	{
		public Batch add() throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().addBatch());
			return this;
		}


		@Override protected CallStmt stmt()
		{
			return CallStmt.this;
		}
	}


	/**
	 * Returns a batch object.
	 * @return the batch
	 */
	public CallBatch batch()
	{
		return new CallBatch();
	}


	/**
	 * Returns a descriptive string.
	 */
	@Override public String toString()
	{
		String s = super.toString();
		if ((sql_ != null) && !isClosed())
			s += '[' + sql_ + ']';
		return s;
	}


	private String sql_;
}
