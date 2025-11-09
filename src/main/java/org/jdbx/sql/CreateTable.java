package org.jdbx.sql;


public class CreateTable
{
	private StringBuilder columns_ = new StringBuilder();
	private final String table_;
	private final String indent_;
	private boolean ifNotExists_;
	private String suffix_;


	public CreateTable(String table)
	{
		this(table, null);
	}


	public CreateTable(String table, boolean multiLine)
	{
		this(table, multiLine ? "  " : null);
	}


	public CreateTable(String table, String indent)
	{
		table_ = table;
		indent_ = indent;
	}


	public CreateTable ifNotExists()
	{
		ifNotExists_ = true;
		return this;
	}


	public CreateTable col(String decl)
	{
		if (columns_.length() > 0)
			columns_.append(", ");
		if (indent_ != null)
			columns_.append(System.lineSeparator()).append(indent_);
		columns_.append(decl);
		return this;
	}


	public CreateTable col(String... declParts)
	{
		if (declParts != null && declParts.length > 0)
		{
			col(declParts[0]);
			for (int i=1; i<declParts.length; i++)
				columns_.append(' ').append(declParts[i]);
		}
		return this;
	}


	public CreateTable suffix(String suffix)
	{
		suffix_ = suffix;
		return this;
	}


	private void ln(StringBuilder sb)
	{
		if (indent_ != null)
			sb.append(System.lineSeparator());
	}


	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		if (ifNotExists_)
			sb.append("IF NOT EXISTS ");
		sb.append(table_).append(" (").append(columns_);
		ln(sb);
		sb.append(')');
		if (suffix_ != null) {
			ln(sb);
			sb.append(' ').append(suffix_);
		}
		return sb.toString();
	}
}
