package comp.imp;

import comp.IRequest;
import comp.IUrl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Request implements IRequest {

    private static final String[] METHODS = {
            "GET", "POST",
    };
    private String _method = null;
    private IUrl _url = null;
    byte[] _content;
    InputStream _stream;

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    Map<String, String> _headers = new HashMap<>();
    String _content_type = "";
    int _status_code = 0;


    public Request(InputStream stream){
        _stream = stream;
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String input = null;
        while(input==null || !input.equals("")){
            try {
                input = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(input!=null && !input.equals("")){
                StringTokenizer parse = new StringTokenizer(input);
                String key = parse.nextToken();
                key = (key.lastIndexOf(":")==key.length()-1)?key.substring(0, key.length()-1):key;
                String value = "";
                while(parse.hasMoreElements()){
                    value += (((value!="")?" ":"")+parse.nextToken());
                }
                _headers.put(key.toLowerCase(), value);
            }
        }

        for(String method : METHODS){
            if(_headers.containsKey(method.toLowerCase())){
                _method = method; break;
            }
        }
        _url = new Url(_headers.get(_method.toLowerCase()));
        if(_method!="GET"){
            try {
                String line = in.readLine();
                _content = (line!=null)?line.getBytes():_content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isValid() {
        if(_method==null){
            return false;
        }
        return true;
    }

    @Override
    public String getMethod() {
        return _method;
    }

    @Override
    public IUrl getUrl() {
        return _url;
    }

    @Override
    public Map<String, String> getHeaders() {
        return _headers;
    }

    @Override
    public int getHeaderCount() {
        return _headers.size();
    }

    @Override
    public String getUserAgent() {
        if(_headers.containsKey("user-agent")){
            return _headers.get("user-agent");
        }
        return "";
    }

    @Override
    public int getContentLength() {
        return _content.length;
    }

    @Override
    public String getContentType() {
        if(_headers.containsKey("Content-Type".toLowerCase())){
            return _headers.get("Content-Type".toLowerCase());
        }
        return "";
    }

    @Override
    public InputStream getContentStream() {
        return _stream;
    }

    @Override
    public String getContentString() {
        return new String(_content);
    }

    @Override
    public byte[] getContentBytes() {
        return _content;
    }
}
