package org.jdbx;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class GetValueTest
{
	private static class GVImpl implements GetValue
	{
		@Override
		public <T> T getObject(Class<T> type) throws JdbxException
		{
			throw new UnsupportedOperationException("Not implemented yet");
		}

		@Override
		public Object getObject(Map<String, Class<?>> map) throws JdbxException
		{
			throw new UnsupportedOperationException("Not implemented yet");
		}

		@Override
		public <T> T get(GetAccessors<T> accessors) throws JdbxException
		{
			this.accessors = accessors;
			return null;
		}


		private GetAccessors<?> accessors;
	}


	private final GVImpl gv = new GVImpl();


	@Test public void testAccessors()
	{
		// adding coverage
		testAccessor(GetValue::getArray);
		testAccessor(GetValue::getBigDecimal);
		testAccessor(GetValue::getBlob);
		testAccessor(GetValue::getBoolean);
		testAccessor(GetValue::getCharacterStream);
		testAccessor(GetValue::getClob);
		testAccessor(GetValue::getNCharacterStream);
		testAccessor(GetValue::getNClob);
		testAccessor(GetValue::getNString);
		testAccessor(GetValue::getObject);
		testAccessor(GetValue::getRef);
		testAccessor(GetValue::getRowId);
		testAccessor(GetValue::getSqlXml);
		testAccessor(GetValue::getURL);
	}


	private <T> void testAccessor(Function<GetValue, T> fn)
	{
		gv.accessors = null;
		fn.apply(gv);
		assertNotNull(gv.accessors);
	}
}
