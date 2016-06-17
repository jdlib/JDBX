package org.jdbx;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * NamedParamCmd represents a SQL command which contains
 * named parameters.
 */
public class NamedParamCmd
{
	/**
	 * Creates a NamedParamCmd.
	 * @param sql the sql command
	 */
	public NamedParamCmd(String sql)
	{
		cmd_ = Check.notNull(sql, "sql");

		Parser parser = new Parser();
		parser.parse();

		converted_ = parser.builder_.toString();
		parser.map_.entrySet().stream().forEach(entry -> {
			paramMap_.put(entry.getKey(), entry.getValue().stream().mapToInt(n -> n.intValue()).toArray());
		});
	}


	private class Parser
	{
		public void parse()
		{
			char last = 0;
			while (hasMore())
			{
				char c = cmd_.charAt(index_++);
				builder_.append(c);

				if ((c == ':') && (last != ':') && Character.isJavaIdentifierStart(peek()))
					consumeParam();
				if ((c == '\'') || (c == '"'))
					consumeUpto(c);
				else if ((c == '-') && (peek() == '-'))
					consumeUpto('\n');
				else if ((c == '/') && (peek() == '*'))
					consumeMultiLineComment();

				last = c;
			}
		}


		private int peek()
		{
			return hasMore() ? cmd_.charAt(index_) : -1;
		}


		private void consumeParam()
		{
			builder_.setLength(builder_.length() - 1);
			builder_.append('?');

			int start = index_;
			do
			{
				index_++;
			}
	        while (hasMore() && Character.isJavaIdentifierPart(cmd_.charAt(index_)));

	        String name = cmd_.substring(start, index_);

	        List<Integer> indexes = map_.computeIfAbsent(name, n -> new ArrayList<Integer>());
	        indexes.add(Integer.valueOf(++count_));
 		}


		private void consumeMultiLineComment()
		{
			builder_.append('*');
			index_++;
			while (hasMore())
			{
				consumeUpto('*');
				if (peek() == '/')
				{
					index_++;
					builder_.append('/');
					break;
				}
			}
		}


		private void consumeUpto(char end)
		{
			while (hasMore())
			{
				char c = cmd_.charAt(index_++);
				builder_.append(c);
				if (c == end)
					break;
			}
		}


		private boolean hasMore()
		{
			return index_ < cmd_.length();
		}


		private int count_;
		private int index_;
		private StringBuilder builder_ = new StringBuilder();
		private Map<String,List<Integer>> map_ = new LinkedHashMap<>();
	}


	/**
	 * Returns the command string with all named parameters
	 * replaced by a '?' character.
	 * @return the converted command
	 */
	public String getConverted()
	{
		return converted_;
	}


	/**
	 * Returns the original command string passed to the constructor.
	 * @return the original command
	 */
	public String getOriginal()
	{
		return cmd_;
	}


	Map<String,int[]> getParamMap()
	{
		return paramMap_;
	}


	/**
	 * Returns the index numbers of a parameter in the
	 * converted command.
	 * @param name the parameter name
	 * @return the indexes
	 */
	public int[] getIndexes(String name)
	{
		int[] indexes = paramMap_.get(name);
		return indexes != null ? indexes.clone() : null;
	}


	/**
	 * Returns an Iterator for the parameter names
	 * @return an Iterator for the parameter names
	 */
	public Iterator<String> getNames()
	{
		return paramMap_.keySet().iterator();
	}


	/**
	 * Returns the command.
	 */
	@Override public String toString()
	{
		return cmd_;
	}


	private String cmd_;
	private String converted_;
	private Map<String,int[]> paramMap_ = new LinkedHashMap<>();
}
