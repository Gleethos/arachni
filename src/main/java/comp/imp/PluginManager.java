package comp.imp;

import comp.IPlugin;
import comp.IPluginManager;
import java.util.*;

public class PluginManager implements IPluginManager {

    Map<String, IPlugin> _plugins = new HashMap<>();
    Map<String, IPlugin> _available = new HashMap<>();

    public PluginManager(){
        _loadClasses();
    }

    private void _loadClasses(){
        ServiceLoader<IPlugin> loader = ServiceLoader.load(IPlugin.class);
        try {
            Iterator<IPlugin> instances = loader.iterator();
            while (instances.hasNext()) {
                IPlugin plugin = instances.next();
                String[] split = plugin.getClass().getName().split("\\.");
                String key = split[split.length-1];
                if(!_available.containsKey(key)){
                    _available.put(key, plugin);
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            serviceError.printStackTrace();
        }

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
            _loadClasses();
            if(_available.containsKey(plugin)){
                _plugins.put(plugin, _available.get(plugin));
            }
        }
    }

    @Override
    public void clear() {
        _plugins = new HashMap<>();
        _available =  new HashMap<>();
        _loadClasses();
    }
}
