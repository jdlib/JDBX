package org.jdbx;


import java.sql.Connection;
import java.util.ArrayList;
import javax.sql.DataSource;
import org.jdbx.function.CheckedSupplier;
import org.jdbx.function.Unchecked;


/**
 * MultiStmt allows to create Stmt instances.
 * It keeps track of these statements and when closed
 * automatically also closes the statements.
 */
public class MultiStmt implements AutoCloseable
{
	/**
	 * Creates a new MultiStmt.
	 * @param con a connection
	 */
	public MultiStmt(Connection con)
	{
		this(con, false);
	}


	/**
	 * Creates a new MultiStmt. It uses the given connection.
	 * @param con a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public MultiStmt(Connection con, boolean closeCon)
	{
		con_ 		= Check.notNull(con, "connection");
		closeCon_	= closeCon;
	}


	/**
	 * Creates a new MultiStmt. It uses a connection obtained from the datasource
	 * and closes the connection when itself is closed.
	 * @param dataSource a DataSource
	 */
	public MultiStmt(DataSource dataSource)
	{
		this(Check.notNull(dataSource, "dataSource")::getConnection, true);
	}


	/**
	 * Creates a new StaticStmt. It uses a connection obtained from the connection supplier
	 * @param supplier provides a connection
	 * @param closeCon determines if the connection is closed when this statement is closed.
	 */
	public MultiStmt(CheckedSupplier<Connection> supplier, boolean closeCon)
	{
		conSupplier_ = Check.notNull(supplier, "supplier");
		closeCon_ 	 = closeCon;
	}


	/**
	 * Returns the connection used by the MultiStmt.
	 * @return the connection
	 */
	public Connection getConnection() throws JdbxException
	{
		checkOpen();
		if (con_ == null)
			con_ = Unchecked.get(conSupplier_);
		return con_;
	}


	//------------------------------
	// factory
	//------------------------------


	/**
	 * Returns a new StaticStmt.
	 * @return the statement
	 */
	public StaticStmt newStaticStmt() throws JdbxException
	{
		return add(new StaticStmt(getConnection(), false));
	}


	/**
	 * Returns a new PrepStmt.
	 * @return the statement
	 */
	public PrepStmt newPrepStmt() throws JdbxException
	{
		return add(new PrepStmt(getConnection(), false));
	}


	/**
	 * Returns a new CallStmt.
	 * @return the statement
	 */
	public CallStmt newCallStmt() throws JdbxException
	{
		return add(new CallStmt(getConnection(), false));
	}


	/**
	 * Returns the number of statements stored by this MultiStmt.
	 * @return the number
	 */
	public int size()
	{
		return statements_.size();
	}


	private <S extends Stmt> S add(S stmt)
	{
		statements_.add(stmt);
		return stmt;
	}


	//------------------------------
	// open/closed state
	//------------------------------


	/**
	 * Returns if the MultiStmt is closed.
	 * @return the closed state
	 */
	public final boolean isClosed()
	{
		return isClosed_;
	}


	protected void checkOpen() throws JdbxException
	{
		if (isClosed())
			throw JdbxException.closed();
	}


	/**
	 * Closes all statement held by this MultiStmt.
	 */
	public void closeStmts() throws JdbxException
	{
		if (!isClosed())
		{
			JdbxException first = null;

			for (Stmt stmt : statements_)
			{
				try
				{
					stmt.close();
				}
				catch (JdbxException e)
				{
					if (first == null)
						first = e;
					else
						first.addSuppressed(e);
				}
			}

			statements_.clear();

			if (first != null)
				throw first;
		}
	}


	/**
	 * Closes this MultiStmt.
	 */
	@Override public void close() throws JdbxException
	{
		if (!isClosed())
		{
			try
			{
				if (closeCon_ && (con_ != null))
					con_.close();
			}
			catch (Exception e)
			{
				throw JdbxException.of(e);
			}
			finally
			{
				for (Stmt stmt : statements_)
					stmt.setClosed();
				isClosed_		= true;
				con_  			= null;
				conSupplier_ 	= null;
				statements_		= null;
			}
		}
	}


	private Connection con_;
	private CheckedSupplier<Connection> conSupplier_;
	private boolean closeCon_;
	private boolean isClosed_;
	private ArrayList<Stmt> statements_ = new ArrayList<>();
}
