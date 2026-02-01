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
import java.util.Map;
import javax.sql.DataSource;
import org.jdbx.function.CheckedSupplier;
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
	 * @return a builder to register OUT or INOUT parameters
	 */
	public OutParams init(String sql) throws JdbxException
	{
		Check.notNull(sql, "sql");
		checkOpen();

		try
		{
			// close old statement if not null
			if (jdbcStmt_ != null)
				closeJdbcStmt();

			// create the new statement
			jdbcStmt_ = createJdbcStmt(sql);
			// assign the sql once the jdbc statement creation succeeded
			sql_ = sql;

			return new OutParamsImpl();
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Allows to register OUT or INOUT parameters of a CallStmt.
	 * @see #init(String)
	 */
	public interface OutParams
	{
		/**
		 * @return a OutParamType builder to register an out parameter by number.
		 * @param number a parameter number, starting at 1.
		 */
		public OutParamType registerOutParam(int number);


		/**
		 * @return a OutParamType builder to register an out parameter by name.
		 * @param name the parameter name
		 */
		public OutParamType registerOutParam(String name);
	}


	/**
	 * Allows to register OUT or INOUT parameters of a CallStmt.
	 * @see OutParams#registerOutParam(int)
	 * @see OutParams#registerOutParam(String)
	 */
	public interface OutParamAs
	{
		public OutParams as(int sqlType) throws JdbxException;


		public OutParams as(SQLType sqlType) throws JdbxException;
	}


	/**
	 * Allows to register OUT or INOUT parameters of a CallStmt.
	 * @see OutParams#registerOutParam(int)
	 * @see OutParams#registerOutParam(String)
	 */
	public interface OutParamType extends OutParamAs
	{
		public OutParamAs scale(int scale) throws JdbxException;


		public OutParamAs typeName(String typeName) throws JdbxException;
	}


	private class OutParamsImpl implements OutParams, OutParamType
	{
		private OutParamType init(int number, String name)
		{
			number_ 	= number;
			name_ 		= name;
			scale_ 		= null;
			typeName_ 	= null;
			return this;
		}

		@Override public OutParamType registerOutParam(int number)
		{
			return init(Check.number(number), null);
		}


		@Override public OutParamType registerOutParam(String name)
		{
			return init(0, Check.notNull(name, "name"));
		}


		/**
		 * Instructs this builder to use the given scale when registering the out parameter.
		 * It should be used when the parameter is of JDBC type <code>NUMERIC</code>
		 * or <code>DECIMAL</code>.
	     * @param scale the desired number of digits to the right of the
	     * 		decimal point. It must be greater than or equal to zero.
		 */
		@Override public OutParamAs scale(int scale) throws JdbxException
		{
			scale_ = Integer.valueOf(Check.scale(scale));
			return this;
		}


		/**
		 * Instructs this builder to use the given typeName when registering the out parameter.
		 * It should be used for a user-defined or <code>REF</code> output parameter.
		 * Examples of user-defined types include: <code>STRUCT</code>, <code>DISTINCT</code>,
	     * <code>JAVA_OBJECT</code>, and named array types.
		 * @param typeName the fully-qualified name of an SQL structured type
		 */
		@Override public OutParamAs typeName(String typeName) throws JdbxException
		{
			typeName_ = Check.notNull(typeName, "typeName");
			return this;
		}


		@Override public OutParams as(int sqlType) throws JdbxException
		{
			Unchecked.run(() -> {
				CallableStatement cs = getJdbcStmt();
				if (name_ != null)
				{
					if (typeName_ != null)
						cs.registerOutParameter(name_, sqlType, typeName_);
					else if (scale_ != null)
						cs.registerOutParameter(name_, sqlType, scale_.intValue());
					else
						cs.registerOutParameter(name_, sqlType);
				}
				else
				{
					if (typeName_ != null)
						cs.registerOutParameter(number_, sqlType, typeName_);
					else if (scale_ != null)
						cs.registerOutParameter(number_, sqlType, scale_.intValue());
					else
						cs.registerOutParameter(number_, sqlType);
				}
			});
			return this;
		}


		@Override public OutParams as(SQLType sqlType) throws JdbxException
		{
			Unchecked.run(() -> {
				CallableStatement cs = getJdbcStmt();
				if (name_ != null)
				{
					if (typeName_ != null)
						cs.registerOutParameter(name_, sqlType, typeName_);
					else if (scale_ != null)
						cs.registerOutParameter(name_, sqlType, scale_.intValue());
					else
						cs.registerOutParameter(name_, sqlType);
				}
				else
				{
					if (typeName_ != null)
						cs.registerOutParameter(number_, sqlType, typeName_);
					else if (scale_ != null)
						cs.registerOutParameter(number_, sqlType, scale_.intValue());
					else
						cs.registerOutParameter(number_, sqlType);
				}
			});
			return this;
		}


		private int number_;
		private String name_;
		private Integer scale_;
		private String typeName_;
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
	public class NumberedParam implements GetValue, SetParam<CallableStatement>
	{
		private NumberedParam(int number)
		{
			number_ = Check.number(number);
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
	public class NamedParam implements GetValue
	{
		private NamedParam(String name)
		{
			name_ = Check.name(name);
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
