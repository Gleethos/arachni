package comp;

public interface IPlugin {

	float BASELINE = 0.000000001f;

	/**
	 * Returns a score between 0 and 1 to indicate that the plugin is willing to
	 * handle the request. The plugin with the highest score will execute the
	 * request.
	 * 
	 * @param req
	 * @return A score between 0 and 1
	 */
	float canHandle(IRequest req);

	/**
	 * Called by the server when the plugin should handle the request.
	 * 
	 * @param req
	 * @return A new response object.
	 */
	IResponse handle(IRequest req);
}
