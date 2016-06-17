package org.jdbx;


interface JdbcEnum
{
	/**
	 * Returns the value of the corresponding JDBC constant.
	 * @return the constant value
	 */
	public int getCode();


	/**
	 * Returns if this enum is valid.
	 * @return the invalid flag
	 */
	default boolean isInvalid()
	{
		return getCode() < 0;
	}


	/**
	 * Maps between JDBC constants and enum values.
	 */
	public static class Map<E extends Enum<E> & JdbcEnum>
	{
		public Map(Class<E> type, E unknown)
		{
			values_ 	= type.getEnumConstants();
			unknown_ 	= unknown;
		}


		public E forCode(int code)
		{
			for (E e : values_)
			{
				if (e.getCode() == code)
					return e;
			}
			return unknown_;
		}


		public E forCode(Integer code)
		{
			return code != null ? forCode(code.intValue()) : unknown_;
		}


		private E[] values_;
		private E unknown_;
	}
}
