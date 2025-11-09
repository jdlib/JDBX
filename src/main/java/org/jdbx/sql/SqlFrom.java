package org.jdbx.sql;


public class SqlFrom
{
	private ClauseBuilder cb_ = new ClauseBuilder(" ");


	ClauseBuilder builder()
	{
		return cb_;
	}


	public SqlFrom add(String item)
	{
		cb_.add(item);
		return this;
	}


	public SqlFrom add(String[] items)
	{
		for (String item : items)
			cb_.add(item);
		return this;
	}


	public SqlFrom comma()
	{
		if (!cb_.isEmpty())
			cb_.addDirect(",");
		return this;
	}


	@Override public String toString()
	{
		return cb_.toString();
	}
}
