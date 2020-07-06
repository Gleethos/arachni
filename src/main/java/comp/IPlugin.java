package comp;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public interface IPlugin {

	float BASELINE = 0.000000001f;
	File WEB_ROOT = new File("./webroot");
	String DEFAULT_FILE = "index.html";
	String FILE_NOT_FOUND = "404.html";

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

	/**
	 * Name of plugin
	 *
	 * @return
	 */
	String toString();

	class util
	{
		// Decodes a URL encoded string using `UTF-8`
		public static String decodeValue(String value) {
			try {
				return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex.getCause());
			}
		}

		public static byte[] readFileData(File file, int fileLength) throws IOException {
			FileInputStream fileIn = null;
			byte[] fileData = new byte[fileLength];
			try {
				fileIn = new FileInputStream(file);
				fileIn.read(fileData);
			} finally {
				if (fileIn != null)
					fileIn.close();
			}
			return fileData;
		}

		// return supported MIME Types
		public static String getContentType(String fileRequested) {
			if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html")) {
				return "text/html";
			} else if(fileRequested.endsWith(".jpg")) {
				return "image/jpeg";
			} else if(fileRequested.endsWith(".png")){
					return "image/png";
			} else if(fileRequested.endsWith(".js")){
				return "text/javascript";
			} else if(fileRequested.endsWith(".css")){
				return "text/css";
			} else if(fileRequested.endsWith(".txt")){
				return "text/plain";
			} else {
				return "text/html";
			}
		}


		public static void fileNotFound(IResponse response, String fileRequested) throws IOException
		{
			response.setStatusCode(404);
			File file = new File(WEB_ROOT, FILE_NOT_FOUND);
			int fileLength = (int) file.length();
			String content = getContentType(fileRequested);//"text/html";
			response.getHeaders().put("content-type", content);
			byte[] fileData = util.readFileData(file, fileLength);
			response.setContent(fileData);
			System.out.println("File " + fileRequested + " not found");

		}

		/**
		 * Helper method which reads the file with the given name and returns
		 * the contents of this file as a String. Will exit the application
		 * if the file can not be read.
		 *
		 * @param path
		 * @return The contents of the file
		 */
		public String readResource(String path){
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(stream));//new FileInputStream(fileName)));
				StringBuffer sb = new StringBuffer();
				String line = "";
				while (line!=null) {
					line = br.readLine();
					if (line != null) sb.append(line).append("\n");
				}
				return sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
				return null;
			}
		}





	}




}
