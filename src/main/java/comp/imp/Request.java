package comp.imp;

import comp.IRequest;
import comp.IUrl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Request implements IRequest {

    private static final String[] METHODS = {"GET", "POST",};
    private String _method = "";
    private IUrl _url;
    byte[] _content;
    InputStream _stream;
    Map<String, String> _headers = new HashMap<>();


    public Request(InputStream stream){
        _stream = stream;
        String input = null;
        while(input==null || !input.equals("")){
            input = _extractLine(false, -1);
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
    }

    /**
     * Reads lines from the _stream variable up to a limit! (max)
     * @param readAll
     * @param max
     * @return
     */
    private String _extractLine(boolean readAll, int max){
        String line = "";
        int i=0;
        int index = 0;
        try{
            while((index<max||max<0) && i!=-1 && (index<=1 || (i!=10) || readAll)){//&&i!=13) {//
                i = _stream.read();
                if(i!=-1 && ((i!=10 && i!=13) || readAll)){
                    line += (char)i;
                }
                index++;
            }
        } catch (Exception e){
            System.out.println(e);
        }
        return line;
    }

    /**
     * Reads all content from stream!
     */
    private void _extractContent(){
        if(_content==null && _method!="GET"){
            int length = this.getContentLength();
            String line = _extractLine(false, length);//_in.readLine();
            _content = (line!=null)?line.getBytes():_content;
        }
    }

    @Override
    public boolean isValid() {
        if(_method.equals("")){
            return false;
        } else if(_headers.containsKey("get")&&_headers.get("get").equals("")){
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
        if(_content == null){
            if(_headers.containsKey("content-length")){
                return Integer.valueOf(_headers.get("content-length"));
            }
            return 0;
        }
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
        _extractContent();
        return new String(_content);
    }

    @Override
    public byte[] getContentBytes() {
        _extractContent();
        return _content;
    }

}
