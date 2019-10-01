package BIF.SWE1.unittests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class RequestHelper {
	public static InputStream getValidRequestStream(String url)
			throws Exception {
		return getValidRequestStream(url, "GET", "localhost", null, null);
	}

	public static InputStream getValidRequestStream(String url, String method)
			throws Exception {
		return getValidRequestStream(url, method, "localhost", null, null);
	}

	public static InputStream getValidRequestStream(String url, String method, String body)
			throws Exception {
		return getValidRequestStream(url, method, "localhost", null, body);
	}
	
	public static InputStream getValidRequestStream(String url, String[][] header) throws Exception {
		return getValidRequestStream(url, "GET", "localhost", header, null);
	}

	public static InputStream getValidRequestStream(String url, String method,
			String host, String[][] header, String body) throws Exception {
		byte[] bodyBytes = null;
        if (body != null)
        {
            bodyBytes = body.getBytes("UTF-8");
        }

		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		PrintWriter sw = new PrintWriter(new OutputStreamWriter(ms,
				StandardCharsets.US_ASCII));
		sw.printf("%s %s HTTP/1.1\n", method, url);
		sw.printf("Host: %s\n", host);
		sw.printf("Connection: keep-alive\n");
		sw.printf("Accept: text/html,application/xhtml+xml\n");
		sw.printf("User-Agent: Unit-Test-Agent/1.0 (The OS)\n");
		sw.printf("Accept-Encoding: gzip,deflate,sdch\n");
		sw.printf("Accept-Language: de-AT,de;q=0.8,en-US;q=0.6,en;q=0.4\n");
		if (bodyBytes != null)
        {
            sw.printf("Content-Length: %d\n", bodyBytes.length);
            sw.printf("Content-Type: application/x-www-form-urlencoded\n");
        }
		if (header != null) {
			for (String[] h : header) {
				sw.printf("%s: %s\n", h[0], h[1]);
			}
		}
		sw.println();
		
        if (bodyBytes != null)
        {
            sw.flush();
            ms.write(bodyBytes);
        }

		sw.flush();
		return new ByteArrayInputStream(ms.toByteArray());
	}

	public static InputStream getInvalidRequestStream() throws Exception {
		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		PrintWriter sw = new PrintWriter(new OutputStreamWriter(ms,
				StandardCharsets.US_ASCII));
		sw.printf("GET\n");
		sw.println();
		sw.flush();
		return new ByteArrayInputStream(ms.toByteArray());
	}

	public static InputStream getEmptyRequestStream() throws Exception {
		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		PrintWriter sw = new PrintWriter(new OutputStreamWriter(ms,
				StandardCharsets.US_ASCII));
		sw.println();
		sw.flush();
		return new ByteArrayInputStream(ms.toByteArray());
	}

}
