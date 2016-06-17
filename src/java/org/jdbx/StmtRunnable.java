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


abstract class StmtRunnable
{
	protected void registerRun() throws JdbException
	{
		if (hasRun_)
			throw JdbException.illegalState(getRunnableType() + " can only be run once");
		hasRun_ = true;
	}


	protected abstract String getRunnableType();


	protected abstract String toDescription();


	/**
	 * Returns a string describing this object.
	 * @return a description
	 */
	@Override public final String toString()
	{
		return getRunnableType() + ':' + toDescription();
	}


	private boolean hasRun_;
}
