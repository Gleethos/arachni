package comp.imp;

import comp.IPlugin;
import comp.IPluginManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class PluginManager implements IPluginManager {

    private final Map<String, IPlugin> _plugins = new HashMap<>();

    public PluginManager(){
        this.add("TestPlugin");
        this.add("FileReader");
        this.add("Navigator");
        this.add("TemperatureReader");
        this.add("ToLower");
        this.add("Oracle");
        this.add("CRUD");
    }

    public IPlugin get(String name){
        return _plugins.get(name);
    }

    @Override
    public Iterable<IPlugin> getPlugins() {
        return _plugins.values();
    }

    @Override
    public void add(IPlugin plugin) {
        if(!_plugins.containsKey(plugin.getClass().getName())){
            _plugins.put(plugin.getClass().getName(), plugin);
        }
    }

    @Override
    public void add(String plugin){
        if(!_plugins.containsKey(plugin)){
            try {
                if(!loadPlugin(plugin,"build/classes/java/main/comp/imp/plugins")){
                    if(!loadPlugin(plugin,"build/classes/java/test/testsuite/unittests/mocks")){
                        if(!loadPlugin(plugin,"plugins")) {
                            throw new IllegalStateException("Plugin '"+plugin+"' not found!");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clear() {
        _plugins.clear();
    }

    public boolean loadPlugin(String pluginName, String packagePath) throws MalformedURLException, IOException, ClassNotFoundException
    {
        File location = new File(packagePath);
        String packagePraefix = _resolvePackagePrefix(location.getPath());
        File pluginLocations[] = location.listFiles((File file)->file.getName().endsWith(".jar")||file.getName().endsWith(".class"));
        URL url = null;
        try {
            url = location.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("Searching for plugins at: "+url);
        URL[] urls = new URL[]{ url };
        URLClassLoader classLoader = new URLClassLoader(urls);
        try {
            for (File pluginLocation : pluginLocations) {
                String[] fragments = pluginLocation.toPath().getFileName().toString().split("\\.");
                String foundFileName = fragments[0];
                fragments = pluginName.split("\\.");
                pluginName = fragments[fragments.length - 1];
                if (foundFileName.equals(pluginName)) {
                    // Instantiate plugin:
                    //==============================================================================================\\
                    IPlugin target = (IPlugin) classLoader.loadClass(packagePraefix + foundFileName).newInstance();
                    //==============================================================================================\\
                    _plugins.put(foundFileName, target);
                    try {
                        classLoader.close();
                    } catch (IOException e) {

                    }
                    return true;
                }
            }
        } catch (Exception e) {

        }
        try {
            classLoader.close();
        } catch (IOException e) {

        }
        return false;
    }

    private String _resolvePackagePrefix(String path){
        StringBuilder result = new StringBuilder();
        String[] parts = path.replace("\\", "/").split("/");
        boolean javaFound = false;
        for(String part : parts){
            result.append((javaFound && !part.equals("main") && !part.equals("test")) ? part + "." : "");
            javaFound = part.equals("java") || javaFound;
        }
        return result.toString();
    }




}
