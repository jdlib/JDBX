package org.jdbx;

import java.sql.Statement;

class OptionValue<T>
{
	@SuppressWarnings("unchecked")
	public static <T> OptionValue<?> set(OptionValue<?> head, Option<T> option, T value)
	{
		Check.notNull(value, "value");
		if (head == null)
			return new OptionValue<>(option, value);
		
		@SuppressWarnings("rawtypes")
		OptionValue v = head;
		while (v != null)
		{
			if (v.option_ == option)
			{
				v.value_ = value;
				break;
			}
			if (v.next_ == null)
			{
				v.next_ = new OptionValue<>(option, value);
				break;
			}
			v = v.next_;
		}
		return head;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T get(OptionValue<?> head, Option<T> option)
	{
		@SuppressWarnings("rawtypes")
		OptionValue v = head;
		while (v != null)
		{
			if (v.option_ == option)
				return (T)v.value_;
			v = v.next_;
		}
		return null;
	}
	
	
	private OptionValue(Option<T> option, T value)
	{
		option_ = Check.notNull(option, "option");
		value_  = value;
	}
	
	
	@SuppressWarnings("unchecked")
	public void add(OptionValue<?> next)
	{
		if (option_ == next.option_)
			value_ = (T)next.value_;
		else if (next_ == null)
			next_ = next;
		else
			next_.add(next);
	}

	
	public OptionValue<?> next()
	{
		return next_;
	}

	
	public void apply(Statement statement) throws Exception
	{
		option_.setter.accept(statement, value_); 
		if (next_ != null)
			next_.apply(statement);
	}
	
	
	private OptionValue<?> next_;
	private final Option<T> option_;
	private T value_;
}
