/*
 * Copyright (C) 2016 JDBX
 *
 * https://github.com/jdlib/JDBX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbx;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


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

	        List<Integer> numbers = map_.computeIfAbsent(name, n -> new ArrayList<Integer>());
	        numbers.add(Integer.valueOf(++count_));
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
	 * @return the original command string passed to the constructor.
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
	 * Returns the column numbers of a parameter in the converted command.
	 * @param paramName the parameter name
	 * @return the numbers
	 */
	public int[] getColNumbers(String paramName)
	{
		int[] numbers = paramMap_.get(paramName);
		return numbers != null ? numbers.clone() : null;
	}


	/**
	 * @return the parameter names as Set.
	 */
	public Set<String> getParamNames()
	{
		return Collections.unmodifiableSet(paramMap_.keySet());
	}


	/**
	 * Returns the original command.
	 */
	@Override public String toString()
	{
		return cmd_;
	}


	private final String cmd_;
	private final String converted_;
	private final Map<String,int[]> paramMap_ = new LinkedHashMap<>();
}
