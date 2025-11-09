package org.jdbx.sql;


public class SqlSelect
{
	private final ClauseBuilder out_ = new ClauseBuilder(", ");
	private final SqlFrom from_ = new SqlFrom();
	private final SqlWhere where_ = new SqlWhere();
	private ClauseBuilder groupBy_;
	private ClauseBuilder having_;
	private ClauseBuilder orderBy_;


	public SqlSelect out(String item)
	{
		out_.add(item);
		return this;
	}


	public SqlSelect from(String item)
	{
		from_.add(item);
		return this;
	}


	public SqlFrom from()
	{
		return from_;
	}


	public SqlSelect where(String item)
	{
		where_.add(item);
		return this;
	}


	public SqlWhere where()
	{
		return where_;
	}


	public SqlSelect groupBy(String item)
	{
		if (groupBy_ == null)
			groupBy_ = new ClauseBuilder(", ");
		groupBy_.add(item);
		return this;
	}


	public SqlSelect having(String item)
	{
		if (having_ == null)
			having_ = new ClauseBuilder(", ");
		having_.add(item);
		return this;
	}


	public SqlSelect orderBy(String item)
	{
		if (orderBy_ == null)
			orderBy_ = new ClauseBuilder(", ");
		orderBy_.add(item);
		return this;
	}


	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		out_.addTo(sb, "SELECT ");
		from_.builder().addTo(sb, " FROM ");
		where_.builder().addTo(sb, " WHERE ");
		if (groupBy_ != null)
			groupBy_.addTo(sb, " GROUP BY ");
		if (having_ != null)
			having_.addTo(sb, " HAVING ");
		if (orderBy_ != null)
			orderBy_.addTo(sb, " ORDER BY ");
		return sb.toString();
	}

}
