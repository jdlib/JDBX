package org.jdbx;


/**
 * Check defines helper methods for argument checking.
 */
class Check
{
	/**
	 * Checks that an object is not null.
	 * @param object the object
	 * @param what describes the object.
	 * @return the object
	 * @exception IllegalArgumentException if the object is null.
	 */
	public static <T> T notNull(T object, String what)
	{
		if (object == null)
			throw new IllegalArgumentException(what + " is null");
		return object;
	}


	/**
	 * Checks that a JdbcEnum is not null and valid.
	 * @exception IllegalArgumentException if the object is null or invalid.
	 */
	public static <V extends JdbcEnum> V valid(V value)
	{
		Check.notNull(value, "value");
		if (value.isInvalid())
			throw new IllegalArgumentException("not valid: " + value);
		return value;
	}


	/**
	 * Checks that a column or parameter index is >= 1.
	 * @param index the index
	 * @return the index
	 * @exception IllegalArgumentException if the index is < 1.
	 */
	public static int index(int index)
	{
		if (index < 1)
			throw new IllegalArgumentException("index must be >= 1, is " + index);
		return index;
	}


	/**
	 * Checks that a name is not null.
	 * @param name the name
	 * @return the name
	 */
	public static String name(String name)
	{
		return notNull(name, "name");
	}
}
