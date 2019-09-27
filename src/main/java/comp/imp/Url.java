package comp.imp;

import comp.IUrl;

import java.util.HashMap;
import java.util.Map;

public class Url implements IUrl {

    private Map<String, String> _parameter = new HashMap<>();
    private String _raw_url;
    private String _path;
    private String _filename;
    private String _extension;
    private String _fragment;

    public Url(String url){
        _raw_url = url.split(" ")[0];
        String[] parts = url.split("\\?");
        if(parts.length>1){
            String[] pairs = parts[1].split(";");
            for(String pair : pairs){
                String[] split = pair.split("=");
                _parameter.put(split[0], split[1]);
            }
        }
        parts = _raw_url.split("/");
        String path = "";
        for(int i=0; i<parts.length-1; i++){
            path += parts[i]+"/";
        }
        _path = path;
        if(parts.length>0){
            _filename = (parts[parts.length-1].contains("."))?parts[parts.length-1].split("\\.")[0]:parts[parts.length-1];
            _extension = (parts[parts.length-1].contains("."))?parts[parts.length-1].split("\\.")[1]:"";
        } else {
            _filename = "";
            _extension = "";
        }
        _fragment = (_raw_url.contains("#"))?_raw_url.split("#")[1]:"";
    }

    @Override
    public String getRawUrl() {
        return _raw_url;
    }

    @Override
    public String getPath() {
        return _path;
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
        return _path.split("/");
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
