package comp;

import java.util.Map;

public interface IUrl {

	/**
	 * @return Returns the raw url.
	 */
	String getRawUrl();

	/**
	 * @return Returns the path of the url, without parameter.
	 */
	String getPath();

	/**
	 * @return Returns a dictionary with the parameter of the url. Never returns
	 *         null.
	 */
	Map<String, String> getParameter();
	
	
	/**
	 * @return Returns the number of parameter of the url. Returns 0 if there are no parameter.
	 */
	int getParameterCount();

	/**
	 * @return Returns the segments of the url path. A segment is divided by '/'
	 *         chars. Never returns null.
	 */
	String[] getSegments();

	/**
	 * @return Returns the filename (with extension) of the url path. If the url
	 *         contains no filename, a empty string is returned. Never returns
	 *         null. A filename is present in the url, if the last segment
	 *         contains a name with at least one dot.
	 */
	String getFileName();

	/**
	 * @return Returns the extension of the url filename, including the leading
	 *         dot. If the url contains no filename, a empty string is returned.
	 *         Never returns null.
	 */
	String getExtension();

	/**
	 * @return Returns the url fragment. A fragment is the part after a '#' char
	 *         at the end of the url. If the url contains no fragment, a empty
	 *         string is returned. Never returns null.
	 */
	String getFragment();
}
