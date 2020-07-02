package BIF.SWE1.unittests;

import static org.junit.Assert.assertTrue;

import comp.IResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.*;

public abstract class AbstractTestFixture<T> {

	protected StringBuilder getBody(IResponse resp) throws UnsupportedEncodingException, IOException {
		StringBuilder body = new StringBuilder();

		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		try {
			resp.send(ms);
			BufferedReader sr = new BufferedReader(new InputStreamReader(
					new ByteArrayInputStream(ms.toByteArray()), "UTF-8"));
			String line;
			while ((line = sr.readLine()) != null) {
				body.append(line + "\n");
			}
		} finally {
			ms.close();
		}
		return body;
	}


	@SuppressWarnings("unchecked")
	public AbstractTestFixture() {
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	protected abstract T createInstance();

	protected static void log(String format, Object... args) {
		final String message = String.format(format, args);
		System.out.println(message);
	}

	protected void assertEmptyOrNull(String str) {
		assertTrue("string is not empty or null", str == null || "".equals(str));
	}

	protected void assertNotEmptyOrNull(String str) {
		assertTrue("string is empty or null", str != null && !"".equals(str));
	}

	protected void assertEquals(Object expected, Object actual) {
		org.junit.Assert.assertEquals(expected, actual);
	}
	
	protected void assertEquals(String msg, Object expected, Object actual) {
		org.junit.Assert.assertEquals(msg, expected, actual);
	}

	protected void assertEquals(float expected, float actual, float delta) {
		org.junit.Assert.assertEquals(expected, actual, delta);
	}
	
	protected void assertEquals(String expected, String actual) {
		expected = (expected != null ? expected : "").toLowerCase().replace(" ", "");
		actual = (actual != null ? actual : "").toLowerCase().replace(" ", "");
		org.junit.Assert.assertEquals(expected, actual);
	}

	protected void assertNotEquals(String expected, String actual) {
		expected = (expected != null ? expected : "").toLowerCase().replace(" ", "");
		actual = (actual != null ? actual : "").toLowerCase().replace(" ", "");
		org.junit.Assert.assertNotEquals(expected, actual);
	}

	protected void assertEquals(String msg, String expected, String actual) {
		expected = (expected != null ? expected : "").toLowerCase().replace(" ", "");
		actual = (actual != null ? actual : "").toLowerCase().replace(" ", "");
		org.junit.Assert.assertEquals(msg, expected, actual);
	}
	protected void assertNotEquals(String msg, String expected, String actual) {
		expected = (expected != null ? expected : "").toLowerCase().replace(" ", "");
		actual = (actual != null ? actual : "").toLowerCase().replace(" ", "");
		org.junit.Assert.assertNotEquals(msg, expected, actual);
	}

	protected void assertThrows(Class<Exception> exceptionClass, Runnable delegate) {
		assertThrows("No exceptions was thrown", delegate);
	}

	protected void assertThrows(String msg, Runnable delegate) {
		try {
			delegate.run();
			org.junit.Assert.assertTrue(msg, false);
		} catch (Exception ex) {
			// Expected
		}
	}
}
