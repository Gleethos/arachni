package comp.imp;

import comp.IResponse;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Response implements IResponse {

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    Map<String, String> _headers = new HashMap<>();
    String _content_type = "";
    int _status_code = 0;
    byte[] _content;

    @Override
    public Map<String, String> getHeaders() {
        return _headers;
    }

    @Override
    public int getContentLength() {
        return (_content!=null)?_content.length:0;
    }

    @Override
    public String getContentType() {
        return _content_type;
    }

    @Override
    public void setContentType(String contentType) {
        _content_type = contentType;
    }

    @Override
    public int getStatusCode() {
        return _status_code;
    }

    @Override
    public void setStatusCode(int status) {
        _status_code = status;
    }

    @Override
    public String getStatus() {
        return null;
    }

    @Override
    public void addHeader(String header, String value) {
        _headers.put(header, value);
    }

    @Override
    public String getServerHeader() {
        return _headers.get("Server");
    }

    @Override
    public void setServerHeader(String server) {
        _headers.put("Server", server);
    }

    @Override
    public void setContent(String content) {

    }

    @Override
    public void setContent(byte[] content) {
        _content = content;
    }

    @Override
    public void setContent(InputStream stream) {//_socket.getStream()...
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        //stream.readAllBytes();
        // get first line of the request from the client
        String input = null;
        try {
            input = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // we parse the request with a string tokenizer
        StringTokenizer parse = new StringTokenizer(input);
        String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
        _headers.put("GET", method);
        while(parse.hasMoreTokens()){
            _headers.put(parse.toString(), parse.nextToken());
        }
        // we get file requested
        String fileRequested = parse.nextToken().toLowerCase();
        System.out.println("File: "+fileRequested);
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
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
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    @Override
    public void send(OutputStream network)
    {
        PrintWriter out = new PrintWriter(network);
        BufferedOutputStream dataOut = new BufferedOutputStream(network);
        byte[] fileData = _content;// send HTTP Headers
        out.println("HTTP/1.1 200 OK");
        out.println("Server: Java HTTP Server from SSaurel : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + "text/html");
        out.println("Content-length: " + getContentLength());
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer
        if(_content!=null) {
            try {
                dataOut.write(fileData, 0, getContentLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            dataOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
