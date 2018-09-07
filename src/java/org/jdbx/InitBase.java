package org.jdbx;


class InitBase<P extends InitBase<P>>
{
	void updateOptions(Stmt stmt)
	{
		if (optionsChanged_ || (stmt.options_ != null))
		{
			StmtOptions options = stmt.options(); // will create it if yet null
			options.setResultConcurrency(concurrency_);
			options.setResultType(resultType_);
			options.setResultHoldability(holdability_);
		}
	}


	@SuppressWarnings("unchecked")
	public P resultType(ResultType value)
	{
		if (changed(resultType_, value, "resultType"))
		{
			resultType_ = value;
			setOptionsChanged();
		}
		return (P)this;
	}


	@SuppressWarnings("unchecked")
	public P resultConcurrency(Concurrency value)
	{
		if (changed(concurrency_, value, "concurrency"))
		{
			concurrency_ = value;
			setOptionsChanged();
		}
		return (P)this;
	}


	@SuppressWarnings("unchecked")
	public P resultHoldability(Holdability value)
	{
		if (changed(holdability_, value, "holdability")) 
		{
			holdability_ = value;
			setOptionsChanged();
		}
		return (P)this;
	}


	protected <E extends JdbcEnum> boolean changed(E currentValue, E newValue, String what)
	{
		Check.valid(newValue, what);
		return currentValue != newValue;
	}


	void setOptionsChanged()
	{
		optionsChanged_ = true;
	}

	
	protected ResultType resultType_ 	= ResultType.FORWARD_ONLY;
	protected Concurrency concurrency_  = Concurrency.READ_ONLY;
	protected Holdability holdability_  = Holdability.CLOSE_AT_COMMIT;
	private boolean optionsChanged_;
}

