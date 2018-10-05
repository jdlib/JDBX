package org.jdbx;


import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbx.function.CheckedConsumer;
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.SetForIndex;
import org.jdbx.function.Unchecked;


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
		return (PreparedStatement)jdbcStmt_;
	}


	//------------------------------
	// init
	//------------------------------


	/**
	 * Returns if this PrepStmt is initialized.
	 * @see #init()
	 * @see #init(String)
	 */
	@Override public boolean isInitialized()
	{
		return jdbcStmt_ != null;
	}


	/**
	 * Initializes the statement to use the given SQL command.
	 * @param cmd a SQL command
	 * @return this
	 */
	public PrepStmt init(String cmd) throws JdbxException
	{
		return init().sql(cmd);
	}


	/**
	 * Returns a builder to initialize this PrepStmt.
	 * In order to take effect, in the end a sql command must be specified on the builder
	 * by calling its {@link Init#sql(String)} method.
	 * @return the init builder
	 */
	public Init init() throws JdbxException
	{
		checkOpen();
		return new Init();
	}

	
	/**
	 * A Builder to initialize the PrepStmt.
	 */
	public class Init implements ReturnCols.Builder<Init>
	{
		private Init()
		{
		}
		
		
		/**
		 * Defines which columns should be returned for insert or update commands.
		 * @param cols the columns or null if no columns should be returned
		 * @return this
		 */
		@Override public Init returnCols(ReturnCols cols)
		{
			returnCols_ = cols;
			return this;
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
		public PrepStmt sql(NamedParamCmd cmd) throws JdbxException
		{
			Check.notNull(cmd, "cmd");
			namedParams_ = false;
			sql(cmd.getConverted());
			paramMap_ = cmd.getParamMap();
			return PrepStmt.this;
		}


		/**
		 * Instructs the statement to use the SQL command.
		 * @param sql a SQL command
		 * @return the PrepStmt
		 */
		public PrepStmt sql(String sql) throws JdbxException
		{
			Check.notNull(sql, "sql");
			checkOpen();

			try
			{
				if (jdbcStmt_ != null)
				{
					Statement old = jdbcStmt_;
					jdbcStmt_ = null;
					paramMap_ = null;	// TODO closeJdbcStmt should set it to null also
					old.close();
				}
				
				if (namedParams_)
				{
					NamedParamCmd npc = new NamedParamCmd(sql);
					paramMap_ = npc.getParamMap();
					sql = npc.getConverted();
				}
				
				jdbcStmt_ 	= createJdbcStmt(sql);
				sql_ 		= sql;

				return PrepStmt.this;
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
		}


		private PreparedStatement createJdbcStmt(String sql) throws Exception
		{
			PreparedStatement stmt;
			if (returnCols_ == null)
			{
				stmt = con_.prepareStatement(sql,
					StmtOptions.getResultType(options_).getCode(),
					StmtOptions.getResultConcurrency(options_).getCode(),
					StmtOptions.getResultHoldability(options_).getCode());
			}
			else if (returnCols_.getNames() != null)
				stmt = con_.prepareStatement(sql, returnCols_.getNames());
			else if (returnCols_.getIndexes() != null)
				stmt = con_.prepareStatement(sql, returnCols_.getIndexes());
			else
				stmt = con_.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			if (options_ != null)
				options_.applyOptionValues(stmt);
			
			return stmt;
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
			throw new IllegalArgumentException("statement is not named: use init().named().cmd(sql) to create a named statement");
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
		Unchecked.run(getJdbcStmt()::clearParameters);
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


		private final int index_;
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
		
		
		private final int[] indexes_;
	}


	//------------------------------
	// query
	//------------------------------


	/**
	 * Returns a QueryResult builder to execute the current SQL command.
	 * @return the QueryResult
	 */
	public QueryResult query() throws JdbxException
	{
		checkInitialized();
		return new PrepStmtQResult(this::getJdbcStmt);
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
		return new PrepUpdate();
	}


	/**
	 * Executes an update operation for the current SQL command.
	 * @return the update result
	 */
	public UpdateResult<Void> update() throws JdbxException
	{
		return createUpdate().run();
	}
	
	
	private class PrepUpdate extends Update
	{
		@Override protected long run(boolean large) throws Exception
		{
			PreparedStatement pstmt = getJdbcStmt();
			return large ?
				pstmt.executeLargeUpdate() :
				pstmt.executeUpdate();
		}


		@Override protected ResultSet getGeneratedKeys() throws Exception
		{
			return getJdbcStmt().getGeneratedKeys();
		}


		@Override protected void cleanup() throws Exception
		{
		}


		@Override protected String toDescription()
		{
			return sql_;
		}
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
	public class PrepBatch extends Batch
	{
		/**
		 * Sets the parameters. Calls {@link PrepStmt#params(Object...)}
		 * @param values the parameter values
		 * @return this
		 */
		public PrepBatch params(Object... values) throws JdbxException
		{
			stmt().params(values);
			return this;
		}
		
		
		/**
		 * Sets the value of the parameter with the given index.  Calls {@link PrepStmt#param(int, Object)}
		 * @param index a parameter index, starting at 1.
		 * @param value a parameter value
		 * @return this
		 */
		public PrepBatch param(int index, Object value) throws JdbxException
		{
			stmt().param(index, value);
			return this;
		}


		/**
		 * Adds a statement to the batch using the current parameters.
		 * @return this
		 */
		public PrepBatch add() throws JdbxException
		{
			Unchecked.run(() -> getJdbcStmt().addBatch());
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
	public PrepBatch batch()
	{
		return new PrepBatch();
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
	

	private Map<String,int[]> paramMap_;
	private String sql_;
}
