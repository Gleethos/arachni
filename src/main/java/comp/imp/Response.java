package comp.imp;

import comp.IResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Response implements IResponse {

    Map<String, String> _headers = new HashMap<>();
    int _status_code = -1;
    byte[] _content;

    public Response(){
        setServerHeader("Webio-Server");
        _headers.put("content-length", "0");
    }

    @Override
    public Map<String, String> getHeaders() {
        return _headers;
    }

    @Override
    public int getContentLength() {
        if(_content == null){
            if(_headers.containsKey("content-length")){
                Integer.valueOf(_headers.get("content-length"));
            }
        }
        return (_content!=null)?_content.length:0;
    }

    @Override
    public String getContentType() {
        String type = getHeaders().get("content-type");
        type = (type==null)?"":type;
        return type;
    }

    @Override
    public void setContentType(String contentType) {
        getHeaders().put("content-type", contentType);
    }

    @Override
    public int getStatusCode() {
        if(_status_code==-1){
            throw new RuntimeException("[RESPONSE]: ERROR. Status code not set!");
        }
        return _status_code;
    }

    @Override
    public void setStatusCode(int status) {
        _status_code = status;
    }

    @Override
    public String getStatus() {
        if(_status_code==-1){
            throw new RuntimeException("[RESPONSE]: ERROR. Status code not set!");
        }
        String code = String.valueOf(_status_code);
        return switch (_status_code) {
            case 200 -> code + "ok";
            case 404 -> code + "notfound";
            case 500 -> code + "internalservererror";
            default -> code;
        };
    }

    public String _getStatus(){
        if(_status_code==-1){
            throw new RuntimeException("[RESPONSE]: ERROR. Status code not set!");
        }
        String code = String.valueOf(_status_code);
        return switch (_status_code) {
            case 200 -> code + " OK";
            case 404 -> code + " Not Found";
            case 500 -> code + " Internal Server Error";
            default -> code;
        };
    }

    @Override
    public void addHeader(String header, String value) {
        _headers.put(header, value);
    }

    @Override
    public String getServerHeader() {
        return _headers.get("Webio-Server");
    }

    @Override
    public void setServerHeader(String server) {
        _headers.put("Webio-Server", server);
    }

    @Override
    public void setContent(String content) {
        try {
            _content = content.getBytes("UTF-8");
            if(_content!=null) _headers.put("content-length",String.valueOf(_content.length));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContent(byte[] content) {
        _content = content;
        if(_content!=null) _headers.put("content-length",String.valueOf(_content.length));
    }

    @Override
    public void setContent(InputStream stream) {//_socket.getStream()...
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        // get sm line of the request from the client
        String input = null;
        try {
            input = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            _content = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            _content = input.getBytes();
        }
        if(_content!=null) _headers.put("content-length",String.valueOf(_content.length));
    }

    private String _getHeaderString(){
        String[] header = {""};
        _headers.forEach((k, v)->{
            header[0] += k+": "+v+"\n";

        });
        header[0]+="\n"+((_headers.size()==0)?"\n":"");
        return header[0];
    }

    @Override
    public void send(OutputStream network)
    {
        if(getContentLength()==0 && getContentType().equals("text/html")){
           throw new RuntimeException("Sending failed! Content of type text/html is empty!");
        }
        byte[] content = _content;// send HTTP Headers

        String header = _getHeaderString();
        if(!header.contains("HTTP/1.") || !header.substring(0, 7).equals("HTTP/1.")){
            header = "HTTP/1. "+getServerHeader()+" "+_getStatus()+((header.substring(1).equals("\n"))?"":"\n")+header;
        }

        byte[] headerBytes = new byte[0];
        try {
            headerBytes = header.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try{
            network.write(headerBytes, 0, headerBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(_content!=null) {
            try {
                network.write(content, 0, getContentLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            network.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return  "[Response]:(S-"+this.getStatus()+", "+"C-"+this.getContentType()+", L-"+this.getContentLength()+"); ";
    }

}
