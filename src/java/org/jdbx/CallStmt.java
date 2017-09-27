package org.jdbx;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbx.function.CheckedRunnable;
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.DoForIndex;
import org.jdbx.function.DoForName;
import org.jdbx.function.GetForIndex;
import org.jdbx.function.GetForName;
import org.jdbx.function.SetForIndex;
import org.jdbx.function.SetForName;


/**
 * CallStmt allows to execute SQL stored procedures.
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
	 * @param con the connection used by the statement
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
	 * This method can only be called if {@link #isInitialized()} returns true.
	 */
	@Override public CallableStatement getJdbcStmt() throws JdbxException
	{
		checkInitialized();
		return (CallableStatement)stmt_;
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
		return init().cmd(sql);
	}


	/**
	 * Init is a builder to define the SQL command and auto keys behaviour.
	 */
	public class Init extends InitBase<Init>
	{
		private Init()
		{
			super(options_);
		}


		/**
		 * Initializes the SQL command which is executed by the CallStmt.
		 * @param sql the sql command
		 * @return the CallStmt
		 */
		public CallStmt cmd(String sql) throws JdbxException
		{
			Check.notNull(sql, "sql");
			checkOpen();

			try
			{
				// close old statement
				if (stmt_ != null)
				{
					CallableStatement p = (CallableStatement)stmt_;
					stmt_ = null;
					p.close();
				}

				// store changed options
				if (optionsChanged_)
					updateOptions(options()); // will create options if not yet done

				// create the new statement
				stmt_ = createJdbcStmt(sql);

				return CallStmt.this;
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		private CallableStatement createJdbcStmt(String sql) throws SQLException
		{
			if (options_ == null)
				return con_.prepareCall(sql);
			else
				return con_.prepareCall(sql,
					options_.getResultType().getCode(),
					options_.getResultConcurrency().getCode(),
					options_.getResultHoldability().getCode());
		}
	}


	/**
	 * Returns if the CallStmt is initialized.
	 */
	@Override public boolean isInitialized()
	{
		return stmt_ != null;
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
	 * Returns a parameter object to set the value of a parameter by index.
	 * @return the parameter object
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
	 * A builder class to set parameter value.
	 */
	public class IndexedParam extends GetValue
		implements RegisterOut<IndexedParam>, SetIndexedParam<CallableStatement>
	{
		private IndexedParam(int index)
		{
			index_ = Check.index(index);
		}


		@Override public IndexedParam out(int sqlType) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(index_, sqlType));
			return this;
		}


		@Override public IndexedParam out(int sqlType, int scale) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(index_, sqlType, scale));
			return this;
		}


		@Override public IndexedParam out(SQLType sqlType) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(index_, sqlType));
			return this;
		}


		@Override public IndexedParam out(SQLType sqlType, int scale) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(index_, sqlType, scale));
			return this;
		}


		@Override public IndexedParam out(SQLType sqlType, String typeName) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(index_, sqlType, typeName));
			return this;
		}

		@Override public void set(Object value) throws JdbxException
		{
			set(value, PreparedStatement::setObject);
		}


		@Override public void set(Object value, SQLType type) throws JdbxException
		{
			Check.notNull(type, "type");
			CheckedRunnable.unchecked(() -> getJdbcStmt().setObject(index_, value, type));
		}


		@Override public <T> void set(T value, SetForIndex<CallableStatement,T> setter) throws JdbxException
		{
			Check.notNull(setter, "setter");
			CheckedRunnable.unchecked(() -> setter.set(getJdbcStmt(), index_, value));
		}


		@Override public void apply(DoForIndex<CallableStatement> runner) throws JdbxException
		{
			Check.notNull(runner, "runner");
			CheckedRunnable.unchecked(() -> runner.accept(getJdbcStmt(), index_));
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


		@Override <T> T get(GetAccessors<T> accessors) throws JdbxException
		{
			Check.notNull(accessors, "accessors");
			return get(accessors.paramForIndex);
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
	 * A builder class to set parameter value.
	 */
	public class NamedParam extends GetValue implements RegisterOut<NamedParam>
	{
		private NamedParam(String name)
		{
			name_ = Check.name(name);
		}


		@Override public NamedParam out(int sqlType) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(name_, sqlType));
			return this;
		}


		@Override public NamedParam out(int sqlType, int scale) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(name_, sqlType, scale));
			return this;
		}


		@Override public NamedParam out(SQLType sqlType) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(name_, sqlType));
			return this;
		}


		@Override public NamedParam out(SQLType sqlType, int scale) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(name_, sqlType, scale));
			return this;
		}


		@Override public NamedParam out(SQLType sqlType, String typeName) throws JdbxException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().registerOutParameter(name_, sqlType, typeName));
			return this;
		}


		public void set(Object value) throws JdbxException
		{
			set(value, CallableStatement::setObject);
		}


		public void set(Object value, SQLType type) throws JdbxException
		{
			Check.notNull(type, "type");
			CheckedRunnable.unchecked(() -> getJdbcStmt().setObject(name_, value, type));
		}


		public <T> void set(T value, SetForName<CallableStatement,T> setter) throws JdbxException
		{
			Check.notNull(setter, "setter");
			CheckedRunnable.unchecked(() -> setter.set(getJdbcStmt(), name_, value));
		}


		public <T> void apply(DoForName<CallableStatement> runner) throws JdbxException
		{
			Check.notNull(runner, "runner");
			CheckedRunnable.unchecked(() -> runner.accept(getJdbcStmt(), name_));
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


		@Override <T> T get(GetAccessors<T> accessors) throws JdbxException
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
	 * Creates a query to execute the current SQL command.
	 * @return the query object
	 */
	public Query createQuery() throws JdbxException
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
			CheckedRunnable.unchecked(() -> getJdbcStmt().addBatch());
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
}
