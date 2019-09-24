package comp;

import java.io.InputStream;
import java.util.Map;

public interface IRequest {
	/**
	 * @return Returns true if the request is valid. A request is valid, if
	 *         method and url could be parsed. A header is not necessary.
	 */
	boolean isValid();

	/**
	 * @return Returns the request method in UPPERCASE. get -> GET
	 */
	String getMethod();

	/**
	 * @return Returns a URL object of the request. Never returns null.
	 */
	IUrl getUrl();

	/**
	 * @return Returns the request header. Never returns null. All keys must be
	 *         lower case.
	 */
	Map<String, String> getHeaders();

	/**
	 * @return Returns the number of header or 0, if no header where found.
	 */
	int getHeaderCount();

	/**
	 * @return Returns the user agent from the request header
	 */
	String getUserAgent();

	/**
	 * @return Returns the parsed content length request header. Never returns
	 *         null.
	 */
	int getContentLength();

	/**
	 * @return Returns the parsed content type request header. Never returns
	 *         null.
	 */
	String getContentType();

	/**
	 * @return Returns the request content (body) stream or null if there is no
	 *         content stream.
	 */
	InputStream getContentStream();

	/**
	 * @return Returns the request content (body) as string or null if there is
	 *         no content.
	 */
	String getContentString();

	/**
	 * @return Returns the request content (body) as byte[] or null if there is
	 *         no content.
	 */
	byte[] getContentBytes();
}
