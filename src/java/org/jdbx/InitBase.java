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
			resultSetType_ 	= QResultType.FORWARD_ONLY;
			concurrency_ 	= QResultConcurrency.READ_ONLY;
			holdability_ 	= QResultHoldability.CLOSE_AT_COMMIT;
		}
	}


	protected void updateOptions(StmtOptions options)
	{
		options.initResultConcurrency(concurrency_);
		options.initResultType(resultSetType_);
		options.initResultHoldability(holdability_);
	}


	@SuppressWarnings("unchecked")
	public P resultType(QResultType value)
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
	public P resultConcurrency(QResultConcurrency value)
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
	public P resultHoldability(QResultHoldability value)
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


	protected QResultType resultSetType_;
	protected QResultConcurrency concurrency_;
	protected QResultHoldability holdability_;
	protected boolean optionsChanged_;
}


