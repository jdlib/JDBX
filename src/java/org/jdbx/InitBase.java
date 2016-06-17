package org.jdbx;


class InitBase<P extends InitBase<P>>
{
	protected InitBase(StmtOptions options)
	{
		if (options != null)
		{
			resultSetType_ 	= options.getResultType();
			concurrency_ 	= options.getResultConcurrency();
			holdability_ 	= options.getResultHoldability();
		}
		else
		{
			resultSetType_ 	= ResultType.FORWARD_ONLY;
			concurrency_ 	= ResultConcurrency.READ_ONLY;
			holdability_ 	= ResultHoldability.CLOSE_AT_COMMIT;
		}
	}


	protected void updateOptions(StmtOptions options)
	{
		options.initResultConcurrency(concurrency_);
		options.initResultType(resultSetType_);
		options.initResultHoldability(holdability_);
	}


	@SuppressWarnings("unchecked")
	public P resultType(ResultType value)
	{
		Check.valid(value);
		if (resultSetType_ != value);
		{
			resultSetType_ = value;
			optionsChanged();
		}
		return (P)this;
	}


	@SuppressWarnings("unchecked")
	public P resultConcurrency(ResultConcurrency value)
	{
		Check.valid(value);
		if (concurrency_ != value)
		{
			concurrency_ = value;
			optionsChanged();
		}
		return (P)this;
	}


	@SuppressWarnings("unchecked")
	public P resultHoldability(ResultHoldability value)
	{
		Check.valid(value);
		if (holdability_ != value)
		{
			holdability_ = value;
			optionsChanged();
		}
		return (P)this;
	}


	protected void optionsChanged()
	{
		optionsChanged_ = true;
	}


	protected ResultType resultSetType_;
	protected ResultConcurrency concurrency_;
	protected ResultHoldability holdability_;
	protected boolean optionsChanged_;
}


