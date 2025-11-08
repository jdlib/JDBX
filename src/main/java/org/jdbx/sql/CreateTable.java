package org.jdbx.sql;


public class CreateTable
{
	private final String table_;
	private final String indent_;
	private StringBuilder columns_ = new StringBuilder();


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


	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(table_).append(" (").append(columns_);
		if (indent_ != null)
			sb.append(System.lineSeparator());
		sb.append(')');
		return sb.toString();
	}
}
