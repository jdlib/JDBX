/*
 * Copyright (C) 2025 JDBX
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
package org.jdbx.sql;


class ClauseBuilder
{
	private StringBuilder sb_;
	private final String sep_;
	private boolean skipSep_;


	public ClauseBuilder(String sep)
	{
		sep_ = sep;
	}


	public ClauseBuilder add(int value)
	{
		return add(Integer.toString(value));
	}


	public ClauseBuilder add(String item)
	{
		if (item != null && !item.isEmpty())
		{
			if (sb_ == null)
				sb_ = new StringBuilder();
			else if (skipSep_)
				skipSep_ = false;
			else
				sb_.append(sep_);
			sb_.append(item);
		}
		return this;
	}


	public ClauseBuilder addDirect(String item)
	{
		if (sb_ == null)
			sb_ = new StringBuilder();
		sb_.append(item);
		return this;
	}


	public void addTo(StringBuilder sb, String prefix)
	{
		addTo(sb, prefix, null);
	}


	public void addTo(StringBuilder sb, String prefix, String suffix)
	{
		if (!isEmpty())
		{
			if (prefix != null)
				sb.append(prefix);
			sb.append(sb_);
			if (suffix != null)
				sb.append(suffix);
		}
	}


	public void skipSep()
	{
		skipSep_ = true;
	}


	public boolean isEmpty()
	{
		return sb_ == null;
	}


	@Override public String toString()
	{
		return sb_ != null ? sb_.toString() : "";
	}
}
