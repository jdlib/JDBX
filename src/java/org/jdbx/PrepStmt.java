package org.jdbx;


import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedRunnable;
import org.jdbx.function.CheckedSupplier;
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
	public PrepStmt(DataSource dataSource) throws JdbxException
	{
		super(dataSource);
	}


	/**
	 * Creates a new PrepStmt. It uses a connection obtained from the connection supplier
	 * @param supplier provides a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public PrepStmt(CheckedSupplier<Connection> supplier, boolean closeCon) throws JdbxException
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
	public ResultSetMetaData getMetaData() throws JdbxException
	{
		checkInitialized();
		return get(PreparedStatement::getMetaData);
	}


	/**
	 * Returns the meta data of the PreparedStatement.
	 * @return the parameter meta data
	 */
	public ParameterMetaData getParamMetaData() throws JdbxException
	{
		checkInitialized();
		return get(PreparedStatement::getParameterMetaData);
	}


	/**
	 * Returns the internal java.sql.PreparedStatement.
	 */
	@Override public PreparedStatement getJdbcStmt() throws JdbxException
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
	public Init init() throws JdbxException
	{
		checkOpen();
		return new Init();
	}


	/**
	 * Initializes the statement to use the following SQL command.
	 * This is the same as {@link #init() init()}{@link Init#cmd(String) .cmd(String)}.
	 * @param sql a SQL command
	 * @return this
	 */
	public PrepStmt init(String sql) throws JdbxException
	{
		return init().cmd(sql);
	}


	/**
	 * Allows to initialize the statement.
	 */
	public class Init extends InitBase<Init> implements ReturnCols.Builder<Init>
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
		public Init namedParams()
		{
			namedParams_ = true;
			return this;
		}


		/**
		 * Instructs the statement to use the given preparsed command.
		 * @param cmd a SQL command with named parameters
		 * @return the PrepStmt
		 */
		public PrepStmt cmd(NamedParamCmd cmd) throws JdbxException
		{
			Check.notNull(cmd, "cmd");
			namedParams_ = false;
			cmd(cmd.getConverted());
			paramMap_ = cmd.getParamMap();
			return PrepStmt.this;
		}


		/**
		 * Instructs the statement to use the SQL command.
		 * @param sql a SQL command
		 * @return the PrepStmt
		 */
		public PrepStmt cmd(String sql) throws JdbxException
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

				if (namedParams_)
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
				throw JdbxException.of(e);
			}
		}


		private PreparedStatement createJdbcStmt(String sql) throws SQLException
		{
			if (returnCols_ == null)
			{
				if (options_ == null)
					return con_.prepareStatement(sql);
				else
					return con_.prepareStatement(sql,
						options_.getResultType().getCode(),
						options_.getResultConcurrency().getCode(),
						options_.getResultHoldability().getCode());
			}
			else if (returnCols_.getNames() != null)
				return con_.prepareStatement(sql, returnCols_.getNames());
			else if (returnCols_.getIndexes() != null)
				return con_.prepareStatement(sql, returnCols_.getIndexes());
			else
				return con_.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		}


		/**
		 * Defines which columns should be returned for INSERTs.
		 * @param cols the columns or null if no columns should be returned
		 * @return this
		 */
		@Override public Init returnCols(ReturnCols cols)
		{
			returnCols_ = cols;
			return this;
		}


		private ReturnCols returnCols_;
		private boolean namedParams_;
	}


	//------------------------------
	// params
	//------------------------------


	/**
	 * Sets the parameters.
	 * @param values the parameter values
	 * @return this
	 */
	public PrepStmt params(Object... values) throws JdbxException
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
	public PrepStmt params(CheckedConsumer<PreparedStatement> builder) throws JdbxException
	{
		Check.notNull(builder, "builder");
		try
		{
			builder.accept(getJdbcStmt());
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
		return this;
	}


	/**
	 * Sets the value of the parameter with the given index.
	 * @param index a parameter index, starting at 1.
	 * @param value a parameter value
	 * @return this
	 */
	public PrepStmt param(int index, Object value) throws JdbxException
	{
		param(index).setObject(value);
		return this;
	}


	/**
	 * Returns a parameter object for given index.
	 * @param index a parameter index, starting at 1.
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
	public NamedParam param(String name) throws JdbxException
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
	public PrepStmt clearParams() throws JdbxException
	{
		CheckedRunnable.unchecked(getJdbcStmt()::clearParameters);
		return this;
	}


	/**
	 * A parameter object for a parameter index.
	 */
	public class IndexedParam implements SetParam<PreparedStatement>
	{
		private IndexedParam(int index)
		{
			index_ = Check.index(index);
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
	 * A parameter object for a parameter name.
	 */
	public class NamedParam implements SetParam<PreparedStatement>
	{
		private NamedParam(int[] indexes)
		{
			indexes_ = indexes;
		}


		@Override public <T> void set(SetForIndex<PreparedStatement,T> setter, T value) throws JdbxException
		{
			Check.notNull(setter, "setter");
			try
			{
				for (int index : indexes_)
					setter.set(getJdbcStmt(), index, value);
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}
		
		
		private int[] indexes_;
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * Returns a Query builder to execute the current SQL command.
	 * @return the Queryt
	 */
	public Query createQuery() throws JdbxException
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
	public Update createUpdate() throws JdbxException
	{
		checkInitialized();
		return new PrepStmtUpdate(this::getJdbcStmt);
	}


	/**
	 * Executes an update operation for the current SQL command.
	 * @return the number of affected records.
	 */
	public int update() throws JdbxException
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
		public ParamBatch add() throws JdbxException
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
