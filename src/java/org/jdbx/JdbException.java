package org.jdbx;


import java.sql.*;


/**
 * JdbException is a RuntimeException which is thrown
 * by Jdbx operations.
 * All SQLExceptions thrown by JDBC operations are wrapped
 * in a JdbException.
 */
public class JdbException extends RuntimeException
{
	/**
	 * Categorizes JdbExceptions.
	 * @see JdbException#getReason()
	 */
	public enum Reason
	{
		/**
		 * The JdbException wraps a SQLException thrown by a JDBC operation.
		 */
		JDBC,

		/**
		 * The JdbException wraps an Exception thrown when processing a result.
		 */
		PROCESS,

		/**
		 * The JdbException was thrown because of an invalid result.
		 */
		INVALID_RESULT,

		/**
		 * The JdbException was thrown because an operation was performed on a closed statement or resultset.
		 */
		CLOSED,

		/**
		 * The JdbException was thrown because an invalid operation was executed.
		 */
		ILLEGAL_STATE
	}


	/**
	 * Categorizes SQLExceptions.
	 */
	// sort from most specific to generic, see getSqlExType()
	public enum SqlExType
	{
		BATCH_UPDATE(BatchUpdateException.class),
		CLIENT_INFO(SQLClientInfoException.class),
		// non transient
			DATA(SQLDataException.class),
			FEATURE_NOT_SUPPORTED(SQLFeatureNotSupportedException.class),
			INTEGRITY_CONSTRAINT_VIOLATION(SQLIntegrityConstraintViolationException.class),
			INVALID_AUTHORIZATION_SPEC(SQLInvalidAuthorizationSpecException.class),
			NON_TRANSIENT_CONNECTION(SQLNonTransientConnectionException.class),
			SYNTAX_ERROR(SQLSyntaxErrorException.class),
			NON_TRANSIENT(SQLNonTransientException.class),
		RECOVERABLE(SQLRecoverableException.class),
		// transient
			TIMEOUT(SQLTimeoutException.class),
			TRANSIENT_ROLLBACK(SQLTransactionRollbackException.class),
			TRANSIENT_CONNECTION(SQLTransientConnectionException.class),
			TRANSIENT(BatchUpdateException.class),
		WARNING(SQLWarning.class),
		BASIC(SQLException.class);


		SqlExType(Class<? extends SQLException> exClass)
		{
			exClass_ = exClass;
		}


		public Class<? extends SQLException> getExClass()
		{
			return exClass_;
		}


		public boolean isNonTransient()
		{
			return SQLNonTransientException.class.isAssignableFrom(exClass_);
		}


		public boolean isTransient()
		{
			return SQLTransientException.class.isAssignableFrom(exClass_);
		}


		private Class<? extends SQLException> exClass_;
	}


	/**
	 * Creates a JdbException which wraps another Throwable.
	 * @param e Throwable
	 * @return the exception itself if it is already JdbException. Else a new JdbException
	 * 		with Reason JDBC or PROCESS is created
	 */
	public static JdbException of(Throwable e)
	{
		Check.notNull(e, "exception");
		if (e instanceof JdbException)
			return (JdbException)e;
		else
			return new JdbException(e, e instanceof SQLException ? Reason.JDBC : Reason.PROCESS);
	}


	/**
	 * Combines all exceptions into a single JdbxException.
	 * @param exceptions the exception which get combined
	 * @return the combined exception
	 */
	public static JdbException combine(Throwable... exceptions)
	{
		if (exceptions == null)
			return null;
		JdbException combined = null;
		for (Throwable e : exceptions)
		{
			if (e != null)
			{
				if (combined == null)
					combined = of(e);
				else
				{
					if ((e instanceof JdbException) && (e.getCause() != null))
						e = e.getCause();
					combined.addSuppressed(e);
				}
			}
		}
		return combined;
	}


	/**
	 * Creates a new JdbException with reason CLOSED.
	 * @return the exception
	 */
	public static JdbException closed()
	{
		return new JdbException("stmt already closed", Reason.CLOSED);
	}


	/**
	 * Creates a new JdbException with reason ILLEGAL_STATE.
	 * @param message an error message
	 * @return the exception
	 */
	public static JdbException illegalState(String message)
	{
		return new JdbException(message, Reason.ILLEGAL_STATE);
	}


	/**
	 * Creates a new JdbException with reason INVALID_RESULT.
	 * @param message an error message
	 * @return the exception
	 */
	public static JdbException invalidResult(String message)
	{
		return new JdbException(message, Reason.INVALID_RESULT);
	}


    protected JdbException(String message, Reason reason)
    {
        super(message);
        reason_ = Check.notNull(reason, "reason");
    }


    protected JdbException(Throwable cause, Reason reason)
    {
        super(cause);
		reason_ = Check.notNull(reason, "reason");
    }


    /**
     * Returns the reason.
     * @return the reason
     */
    public Reason getReason()
    {
    	return reason_;
    }


    /**
     * Returns if the cause of this exception is a SQLException
     * @return is the cause of this exception a SQLException
     */
    public boolean hasSqlExCause()
    {
    	return getCause() instanceof SQLException;
    }


    /**
     * Returns the cause of this exception as SQLException or null.
     * @return the cause of this exception as SQLException or null.
     */
    public SQLException getSqlExCause()
    {
    	return hasSqlExCause() ? (SQLException)getCause() : null;
    }


    /**
     * Returns the type of the cause of this exception, if it is a SQLException.
     * @return the type of the cause of this exception, if it is a SQLException, or null
     */
    public SqlExType getSqlExType()
    {
    	SQLException cause = getSqlExCause();
    	if (cause == null)
    		return null;

    	for (SqlExType type : SqlExType.values())
    	{
    		if (type.getExClass().isInstance(cause))
    			return type;
    	}
    	return SqlExType.BASIC;
    }


    private Reason reason_;
}
