package org.jdbx.function;


import org.jdbx.JdbxException;


/**
 * Contains helper methods to call unchecked functional interfaces
 * and turn exceptions into JdbxExceptions.
 */
public class Unchecked
{
	/**
	 * Runs the runnable and converts any exception into a JdbxException.
	 * @param runnable the runnable
	 */
	public static void run(CheckedRunnable runnable) throws JdbxException
	{
		try
		{
			runnable.run();
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Passes the argument to the consumer and converts any exception into a JdbxException
	 * @param consumer a consumer
	 * @param arg an argument
	 * @param <T> the type of the input
	 */
	public static <T> void accept(CheckedConsumer<T> consumer, T arg) throws JdbxException
	{
		try
		{
			consumer.accept(arg);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Passes the argument to the consumer and converts any exception into a JdbxException
	 * @param consumer a consumer
	 * @param t an argument
	 * @param u an argument
	 * @param <T> the type of input 1
	 * @param <U> the type of input 2
	 */
	public static <T,U> void accept(CheckedBiConsumer<T,U> consumer, T t, U u) throws JdbxException
	{
		try
		{
			consumer.accept(t, u);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Calls the function and converts any exception into a JdbxException
	 * @param function a function
	 * @param arg an argument
	 * @param <T> the type of the input to the function
	 * @param <R> the type of results supplied
	 * @return the function result
	 */
	public static <T,R> R apply(CheckedFunction<T,R> function, T arg) throws JdbxException
	{
		try
		{
			return function.apply(arg);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Calls the bifunction and converts any exception into a JdbxException
	 * @param function a bifunction
	 * @param arg1 the first argument
	 * @param arg2 the second argument
	 * @param <T> the type of the first argument to the function
	 * @param <U> the type of the second argument to the function
	 * @param <R> the type of results supplied
	 * @return the function result
	 */
	public static <T,U,R> R apply(CheckedBiFunction<T,U,R> function, T arg1, U arg2) throws JdbxException
	{
		try
		{
			return function.apply(arg1, arg2);
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}


	/**
	 * Returns the value provided by the supplier and converts
	 * any exception into a JdbxException.
	 * @param supplier a supplier
	 * @param <T> the type of results supplied by this supplier
	 * @return the value provided by the supplier
	 */
	public static <T> T get(CheckedSupplier<T> supplier) throws JdbxException
	{
		try
		{
			return supplier.get();
		}
		catch (Exception e)
		{
			throw JdbxException.of(e);
		}
	}
}
