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
    String _content_type = "";
    int _status_code = 0;

    BufferedReader _in;

    private String _extractLine(boolean readAll){
        String line = "";
        int i=0;
        try{
            while(i!=-1 && ((i!=10) || readAll)) {// && i!=13
                i = _stream.read();
                // converts integer to character and then to string
                if(i!=-1 && ((i!=10 && i!=13) || readAll)){
                    line += (char)i;
                }
            }
        } catch (Exception e){
            System.out.println(e);
        }
        //line = (readAll)?line.trim():line;
        return line;
    }

    public Request(InputStream stream){
        _stream = stream;
        //BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String input = null;
        while(input==null || !input.equals("")){
            //try {
                input = _extractLine(false);//in.readLine();
            //}// catch (IOException e) {
             //   e.printStackTrace();
            //}
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
        //_in = in;
        //if(_method!="GET"){
        //    try {
        //        //in.mark(100);
        //        String line = in.readLine();
        //        _content = (line!=null)?line.getBytes():_content;
        //        //in.reset();
        //    } catch (IOException e) {
        //        e.printStackTrace();
        //    }
        //}
        //_extractContent();

        //try {
        //    _stream.reset();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }

    private void _extractContent(){
        if(_content==null && _method!="GET"){//&& _in!=null
            //try {
                //in.mark(100);
                String line = _extractLine(true);//_in.readLine();

                _content = (line!=null)?line.getBytes():_content;
                //in.reset();
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}
        }
        //_in = null;
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
                Integer.valueOf(_headers.get("content-length"));
            }
        }
        //if(_content==null){
        //    return _content_length;
        //}
        _extractContent();
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
