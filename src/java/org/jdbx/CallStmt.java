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
import org.jdbx.function.DoForName;
import org.jdbx.function.GetForIndex;
import org.jdbx.function.GetForName;
import org.jdbx.function.SetForIndex;
import org.jdbx.function.SetForName;
import org.jdbx.function.Unchecked;


/**
 * CallStmt allows to call SQL stored procedures.
 * It wraps java.sql.CallableStatement.
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
	 * This method can only be called if {@link #init() initialized}.
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
	 * Returns a builder to initialize the CallStmt.
	 * @return the builder
	 */
	public Init init() throws JdbxException
	{
		checkOpen();
		return new Init();
	}


	/**
	 * Initializes the SQL command which is executed by the CallStmt.
	 * @param sql the sql command
	 * @return this
	 */
	public CallStmt init(String sql) throws JdbxException
	{
		return init().sql(sql);
	}


	/**
	 * Init is a builder to define the SQL command and auto keys behaviour.
	 */
	public class Init extends InitBase<Init>
	{
		private Init()
		{
		}


		/**
		 * Initializes the SQL command which is executed by the CallStmt.
		 * @param sql the sql command
		 * @return the CallStmt
		 */
		public CallStmt sql(String sql) throws JdbxException
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
				jdbcStmt_ 	= con_.prepareCall(sql, resultType_.getCode(), concurrency_.getCode(), holdability_.getCode());
				sql_ 	   	= sql;
				updateOptions(CallStmt.this);

				return CallStmt.this;
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}
	}


	/**
	 * Returns if the CallStmt is initialized.
	 * @see #init()
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
	 * Sets the value of the parameter with the given index.
	 * @param index a parameter index, starting at 1.
	 * @param value a parameter value
	 * @return this
	 */
	public CallStmt param(int index, Object value) throws JdbxException
	{
		param(index).setObject(value);
		return this;
	}

	
	/**
	 * Returns a IndexedParam object to set or get the value of a parameter by index.
	 * @return the IndexedParam
	 */
	public IndexedParam param(int index)
	{
		return new IndexedParam(index);
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
	 * A builder class to manage a parameter value specified by a parameter index.
	 */
	public class IndexedParam implements GetValue, RegisterOut<IndexedParam>, SetParam<CallableStatement>
	{
		private IndexedParam(int index)
		{
			index_ = Check.index(index);
		}


		@Override public IndexedParam out(int sqlType) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(index_, sqlType));
			return this;
		}


		@Override public IndexedParam out(int sqlType, int scale) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(index_, sqlType, scale));
			return this;
		}


		@Override public IndexedParam out(SQLType sqlType) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(index_, sqlType));
			return this;
		}


		@Override public IndexedParam out(SQLType sqlType, int scale) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(index_, sqlType, scale));
			return this;
		}


		@Override public IndexedParam out(SQLType sqlType, String typeName) throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().registerOutParameter(index_, sqlType, typeName));
			return this;
		}
		

		@Override public <T> T get(Class<T> type) throws JdbxException
		{
			Check.notNull(type, "type");
			try
			{
				return getJdbcStmt().getObject(index_, type);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public Object get(Map<String,Class<?>> map) throws JdbxException
		{
			Check.notNull(map, "map");
			try
			{
				return getJdbcStmt().getObject(index_, map);
			}
			catch (SQLException e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> T get(GetAccessors<T> accessors) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			return get(accessors.paramForIndex);
		}


		public <T> T get(GetForIndex<CallableStatement,T> getter) throws JdbxException
		{
			Check.notNull(getter, "getter");
			try
			{
				return getter.get(getJdbcStmt(), index_);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		@Override public <T> void set(SetForIndex<PreparedStatement,T> setter, T value) throws JdbxException
		{
			Check.notNull(setter, "setter");
			try
			{
				setter.set(getJdbcStmt(), index_, value);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}

		
		private int index_;
	}


	/**
	 * Returns a parameter object to set the value of a parameter by name.
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


		@Override public <T> T get(Class<T> type) throws JdbxException
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


		@Override public Object get(Map<String,Class<?>> map) throws JdbxException
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


		private String name_;
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * Executes the current SQL query command and returns a QueryResult. 
	 * @return the query result
	 */
	public QueryResult query() throws JdbxException
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
	public class CallBatch extends Batch
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
