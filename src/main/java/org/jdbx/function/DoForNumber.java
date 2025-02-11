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
 * DoForNumber can perform an operation given
 * an object and a number.
 * @param <OBJ> the type of the object
 */
@FunctionalInterface
public interface DoForNumber<OBJ>
{
	/**
	 * Performs a number based operation on the object.
	 * @param obj an object
	 * @param number a number
	 * @throws Exception if an error occurs
	 */
	public void accept(OBJ obj, int number) throws Exception;
}
