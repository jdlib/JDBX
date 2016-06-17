package org.jdbx;


import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedRunnable;
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.DoForIndex;
import org.jdbx.function.SetForIndex;


/**
 * PrepStmt allows to execute a precompiled SQL command with parameters.
 * It wraps java.sql.PreparedStatement.
 */
public class PrepStmt extends Stmt
{
	/**
	 * Creates a new PrepStmt. It uses a connection obtained from the datasource
	 * and closes the connection when itself is closed.
	 * @param dataSource a DataSource
	 */
	public PrepStmt(DataSource dataSource) throws JdbException
	{
		super(dataSource);
	}


	/**
	 * Creates a new PrepStmt. It uses a connection obtained from the connection supplier
	 * @param supplier provides a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public PrepStmt(CheckedSupplier<Connection> supplier, boolean closeCon) throws JdbException
	{
		super(supplier, closeCon);
	}


	/**
	 * Creates a new PrepStmt. Calls {@link PrepStmt#PrepStmt(Connection, boolean) PrepStmt(con, false)}.
	 * @param con a connection
	 */
	public PrepStmt(Connection con)
	{
		this(con, false);
	}


	/**
	 * Creates a new PrepStmt. It uses the given connection.
	 * @param con a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public PrepStmt(Connection con, boolean closeCon)
	{
		super(con, closeCon);
	}


	/**
	 * Returns the meta data of the PreparedStatement.
	 * @return the meta data
	 */
	public ResultSetMetaData getMetaData() throws JdbException
	{
		checkInitialized();
		return get(PreparedStatement::getMetaData);
	}


	/**
	 * Returns the meta data of the PreparedStatement.
	 * @return the parameter meta data
	 */
	public ParameterMetaData getParamMetaData() throws JdbException
	{
		checkInitialized();
		return get(PreparedStatement::getParameterMetaData);
	}


	/**
	 * Returns the internal java.sql.PreparedStatement.
	 */
	@Override public PreparedStatement getJdbcStmt() throws JdbException
	{
		checkInitialized();
		return (PreparedStatement)stmt_;
	}


	//------------------------------
	// init
	//------------------------------


	/**
	 * Returns if the PrepStmt is initialized.
	 * @see #init()
	 */
	@Override public boolean isInitialized()
	{
		return stmt_ != null;
	}


	/**
	 * Returns a builder to initialize the statement.
	 * @return a init builder
	 */
	public Init init() throws JdbException
	{
		checkOpen();
		return new Init();
	}


	/**
	 * Initializes the statement to use the following SQL command.
	 * @param sql a SQL command
	 * @return this
	 */
	public PrepStmt init(String sql) throws JdbException
	{
		return init().cmd(sql);
	}


	/**
	 * Allows initialize the statement.
	 */
	public class Init extends InitBase<Init> implements AutoKeys.Builder<Init>
	{
		private Init()
		{
			super(options_);
		}


		/**
		 * Instructs the init builder that the SQL command has named parameters
		 * instead of positional parameters.
		 * @return this
		 */
		public Init named()
		{
			named_ = true;
			return this;
		}


		/**
		 * Instructs the statement to use the given preparsed command.
		 * @param cmd a SQL command with named parameters
		 * @return the PrepStmt
		 */
		public PrepStmt cmd(NamedParamCmd cmd) throws JdbException
		{
			Check.notNull(cmd, "cmd");
			named_ = false;
			cmd(cmd.getConverted());
			paramMap_ = cmd.getParamMap();
			return PrepStmt.this;
		}


		/**
		 * Instructs the statement to use the SQL command.
		 * @param sql a SQL command
		 * @return the PrepStmt
		 */
		public PrepStmt cmd(String sql) throws JdbException
		{
			Check.notNull(sql, "sql");
			checkOpen();

			try
			{
				if (stmt_ != null)
				{
					PreparedStatement p = (PreparedStatement)stmt_;
					stmt_ = null;
					paramMap_ = null;
					p.close();
				}

				if (optionsChanged_)
					updateOptions(options());

				if (named_)
				{
					NamedParamCmd npc = new NamedParamCmd(sql);
					paramMap_ = npc.getParamMap();
					sql = npc.getConverted();
				}
				stmt_ = createJdbcStmt(sql);

				return PrepStmt.this;
			}
			catch (Exception e)
			{
				throw JdbException.of(e);
			}
		}


		private PreparedStatement createJdbcStmt(String sql) throws SQLException
		{
			if (autoKeys_ == null)
			{
				if (options_ == null)
					return con_.prepareStatement(sql);
				else
					return con_.prepareStatement(sql,
						options_.getResultType().getCode(),
						options_.getResultConcurrency().getCode(),
						options_.getResultHoldability().getCode());
			}
			else if (autoKeys_.getColNames() != null)
				return con_.prepareStatement(sql, autoKeys_.getColNames());
			else if (autoKeys_.getColIndexes() != null)
				return con_.prepareStatement(sql, autoKeys_.getColIndexes());
			else
				return con_.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		}


		/**
		 * Sets the AutoKeys object which should be used.
		 * @param autoKeys the autoKeys or null if no auto generated keys should be returned
		 * @return this
		 */
		@Override public Init reportAutoKeys(AutoKeys autoKeys)
		{
			autoKeys_ = autoKeys;
			return this;
		}


		private AutoKeys autoKeys_;
		private boolean named_;
	}


	//------------------------------
	// params
	//------------------------------


	/**
	 * Sets the parameters.
	 * @param values the parameter values
	 * @return this
	 */
	public PrepStmt params(Object... values) throws JdbException
	{
		if ((values != null) && (values.length > 0))
		{
			params(pstmt -> {
				for (int i=0; i<values.length; i++)
					pstmt.setObject(i+1, values[i]);
			});
		}
		return this;
	}


	/**
	 * Passes the internal PreparedStatement to the parameter builder.
	 * @param builder a parameter builder
	 * @return this
	 */
	public PrepStmt params(CheckedConsumer<PreparedStatement> builder) throws JdbException
	{
		Check.notNull(builder, "builder");
		try
		{
			builder.accept(getJdbcStmt());
		}
		catch (Exception e)
		{
			throw JdbException.of(e);
		}
		return this;
	}


	/**
	 * Sets the value of the parameter with the given index..
	 * @param index a parameter index
	 * @param value a parameter value
	 * @return this
	 */
	public PrepStmt param(int index, Object value) throws JdbException
	{
		param(index).set(value);
		return this;
	}


	/**
	 * Returns a parameter object for given index.
	 * @param index a parameter index
	 * @return the parameter object. Use setters of the parameter object to set a parameter value
	 */
	public IndexedParam param(int index)
	{
		return new IndexedParam(index);
	}


	/**
	 * Returns a parameter object for given name.
	 * @param name a parameter name
	 * @return the parameter object. Use setters of the parameter object to set a parameter value
	 */
	public NamedParam param(String name) throws JdbException
	{
		Check.notNull(name, "name");
		checkInitialized();
		if (paramMap_ == null)
			throw new IllegalArgumentException("statement is not named: use prepare().named().cmd(sql) to create a named statement");
		int[] indexes = paramMap_.get(name);
		if (indexes == null)
			throw new IllegalArgumentException("sql command does not contain parameter '" + name + '\'');
		return new NamedParam(indexes);
	}


	/**
	 * Clears the parameters.
	 * @return this
	 */
	public PrepStmt clearParams() throws JdbException
	{
		CheckedRunnable.unchecked(getJdbcStmt()::clearParameters);
		return this;
	}


	/**
	 * A parameter object for a parameter index.
	 */
	public class IndexedParam implements SetIndexedParam<PreparedStatement>
	{
		private IndexedParam(int index)
		{
			index_ = Check.index(index);
		}


		@Override public void set(Object value) throws JdbException
		{
			set(value, PreparedStatement::setObject);
		}


		@Override public void set(Object value, SQLType type) throws JdbException
		{
			Check.notNull(type, "type");
			CheckedRunnable.unchecked(() -> getJdbcStmt().setObject(index_, value, type));
		}


		@Override public <T> void set(T value, SetForIndex<PreparedStatement,T> setter) throws JdbException
		{
			Check.notNull(setter, "setter");
			CheckedRunnable.unchecked(() -> setter.set(getJdbcStmt(), index_, value));
		}


		@Override public void apply(DoForIndex<PreparedStatement> runner) throws JdbException
		{
			Check.notNull(runner, "runner");
			CheckedRunnable.unchecked(() -> runner.accept(getJdbcStmt(), index_));
		}


		private int index_;
	}


	/**
	 * A parameter object for a parameter name.
	 */
	public class NamedParam implements SetIndexedParam<PreparedStatement>
	{
		private NamedParam(int[] indexes)
		{
			indexes_ = indexes;
		}


		@Override public void set(Object value) throws JdbException
		{
			set(value, PreparedStatement::setObject);
		}


		@Override public void set(Object value, SQLType type) throws JdbException
		{
			Check.notNull(type, "type");
			for (int index : indexes_)
				CheckedRunnable.unchecked(() -> getJdbcStmt().setObject(index, value, type));
		}


		@Override public <T> void set(T value, SetForIndex<PreparedStatement,T> setter) throws JdbException
		{
			Check.notNull(setter, "setter");
			for (int index : indexes_)
				CheckedRunnable.unchecked(() -> setter.set(getJdbcStmt(), index, value));
		}


		@Override public void apply(DoForIndex<PreparedStatement> runner) throws JdbException
		{
			Check.notNull(runner, "runner");
			for (int index : indexes_)
				CheckedRunnable.unchecked(() -> runner.accept(getJdbcStmt(), index));
		}


		private int[] indexes_;
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * Creates a query to execute the current SQL command.
	 * @return the query
	 */
	public Query createQuery() throws JdbException
	{
		checkInitialized();
		return new PrepStmtQuery(this::getJdbcStmt);
	}


	//------------------------------
	// update
	//------------------------------


	/**
	 * Creates an Update object to perform an update operation for the current
	 * SQL command.
	 * @return an update object
	 */
	public Update createUpdate() throws JdbException
	{
		checkInitialized();
		return new PrepStmtUpdate(this::getJdbcStmt);
	}


	/**
	 * Executes an update operation for the current SQL command.
	 * @return the number of affected records.
	 */
	public int update() throws JdbException
	{
		return createUpdate().run();
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
	 * Executes the PrepStmt.
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
	 * A Batch implementation for PrepStmt.
	 */
	public class ParamBatch extends Batch
	{
		public ParamBatch add() throws JdbException
		{
			CheckedRunnable.unchecked(() -> getJdbcStmt().addBatch());
			return this;
		}


		@Override protected PrepStmt stmt()
		{
			return PrepStmt.this;
		}
	}


	/**
	 * Returns a Batch object which can be used to add statements to the batch and
	 * execute the batch.
	 * @return the batch
	 */
	public ParamBatch batch()
	{
		return new ParamBatch();
	}


	private Map<String,int[]> paramMap_;
}
