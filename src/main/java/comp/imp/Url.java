package comp.imp;

import comp.IPlugin;
import comp.IUrl;

import java.util.HashMap;
import java.util.Map;

public class Url implements IUrl {

    private Map<String, String> _parameter = new HashMap<>();
    private String _raw_url = "";
    private String _path = "";
    private String _filename = "";
    private String _extension = "";
    private String _fragment = "";

    private void _paraParse(String exp){
        String[] pairs = exp.split("&");
        for(String pair : pairs){
            String[] split = pair.split("=");
            if(split.length>1){
                if(split[1].endsWith("HTTP/1.1")){
                    split[1] = split[1].substring(0, split[1].length()-8);
                    split[1] = split[1].trim();
                }
                _parameter.put(IPlugin.util.decodeValue(split[0]), IPlugin.util.decodeValue(split[1]));
            }
        }
    }

    public Url(String url){
        if(url==null) return;
        _raw_url = url.split(" ")[0];
        String[] parts = url.split("\\?");
        if(parts.length>1) _paraParse(parts[1]);
        else if(parts[0].contains("=")) _paraParse(parts[0]);

        parts = _raw_url.split("/");
        StringBuilder path = new StringBuilder();
        for(int i=0; i<parts.length-1; i++){
            path.append(parts[i]).append("/");
        }
        _path = path.toString();
        if(parts.length>0){
            _filename = (parts[parts.length-1].contains("."))?parts[parts.length-1].split("\\.")[0]:parts[parts.length-1].split("\\?")[0];
            _filename = IPlugin.util.decodeValue(_filename);
            _extension = "";
            if(parts[parts.length-1].contains(".")) { // Only split after first ".", append the rest back on... :
                String[] extensionParts = parts[parts.length-1].split("\\.");
                for(int i=1; i<extensionParts.length; i++) _extension += (extensionParts[i]+".");
                _extension = _extension.substring(0, _extension.length()-1);
            }
            _extension = _extension.split("\\?")[0];
            _extension = _extension.split("#")[0];
        }
        _fragment = (_raw_url.contains("#"))?_raw_url.split("#")[1]:"";
    }

    @Override
    public String getRawUrl() {
        return _raw_url;
    }

    @Override
    public String getPath() {
        return _path + ((!getFileName().equals(""))?(getFileName() + "." + getExtension()):"");
    }

    @Override
    public Map<String, String> getParameter() {
        return _parameter;
    }

    @Override
    public int getParameterCount() {
        return _parameter.size();
    }

    @Override
    public String[] getSegments() {
        return (getRawUrl().substring(0, 1).equals("/"))
                ?getRawUrl().substring(1, getRawUrl().length()).split("/")
                :getRawUrl().split("/");
    }

    @Override
    public String getFileName() {
        return _filename;
    }

    @Override
    public String getExtension() {
        return _extension;
    }

    @Override
    public String getFragment() {
        return _fragment;
    }
}
