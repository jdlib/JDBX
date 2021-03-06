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
package org.jdbx.function;


/**
 * SetForName can set a named value on an object.
 * @param <OBJ> the type of the object
 * @param <V> the type of the value
 */
@FunctionalInterface
public interface SetForName<OBJ,V>
{
	/**
	 * Sets a value.
	 * @param object an object
	 * @param name a name
	 * @param value the value
	 * @throws Exception if an error occurs
	 */
	public void set(OBJ object, String name, V value) throws Exception;
}
