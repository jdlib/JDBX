package org.jdbx.sql;


class StmtBuilder
{
	private final StringBuilder sb_ = new StringBuilder();
	private final String indent_;

	public StmtBuilder(String indent)
	{
		indent_ = indent;
	}


	public StmtBuilder append(String s)
	{
		sb_.append(s);
		return this;
	}


	public StmtBuilder append(char c)
	{
		sb_.append(c);
		return this;
	}


	public StmtBuilder sep()
	{
		if (indent_ == null)
			sb_.append(' ');
		return this;
	}


	public StmtBuilder lnOrSep()
	{
		if (indent_ != null)
			sb_.append(System.lineSeparator());
		else
			sb_.append(' ');
		return this;
	}


	public StmtBuilder ln()
	{
		if (indent_ != null)
			sb_.append(System.lineSeparator());
		return this;
	}


	public StmtBuilder indent()
	{
		if (indent_ != null)
			sb_.append(indent_);
		return this;
	}


	@Override
	public String toString()
	{
		return sb_.toString();
	}
}
