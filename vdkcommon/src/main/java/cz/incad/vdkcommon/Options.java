package cz.incad.vdkcommon;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class Options {
    
    public static final Logger LOGGER = Logger.getLogger(Options.class.getName());

    private static Options _sharedInstance = null;
    private final JSONObject conf;

    public synchronized static Options getInstance() throws IOException, JSONException {
        if (_sharedInstance == null) {
            _sharedInstance = new Options();
        }
        return _sharedInstance;
    }
    
    public synchronized static void resetInstance(){
        _sharedInstance = null;
        LOGGER.log(Level.FINE, "Options reseted");
    }
    
    private String path(){
        return System.getProperty("user.home") + File.separator + ".vdkcr" + File.separator + "conf.json";
    }

    public Options() throws IOException, JSONException {
        File fdef = FileUtils.toFile(Options.class.getResource("/cz/incad/vdkcommon/conf.json"));

        String json = FileUtils.readFileToString(fdef, "UTF-8");
        conf = new JSONObject(json);

        File f = new File(path());
        if (f.exists() && f.canRead()) {
            json = FileUtils.readFileToString(f, "UTF-8");
            JSONObject confCustom = new JSONObject(json);
            Iterator keys = confCustom.keys();
            while (keys.hasNext() ) {
                String key = (String) keys.next();
                LOGGER.log(Level.FINE, "key {0} will be overrided", key);
                conf.put(key, confCustom.get(key));
            }
        }

    }

    public String getString(String key, String defVal) {
        return conf.optString(key, defVal);
    }

    public String getString(String key) {
        return conf.optString(key);
    }

    public boolean getBoolean(String key, boolean defVal) {
        return conf.optBoolean(key, defVal);
    }

    public boolean getBoolean(String key) {
        return conf.optBoolean(key);
    }

    public int getInt(String key, int defVal) {
        return conf.optInt(key, defVal);
    }
    
    public String[] getStrings(String key){
        JSONArray arr = conf.optJSONArray(key);
        String[] ret = new String[arr.length()];
        for(int i = 0; i<arr.length(); i++){
            ret[i] = arr.getString(i);
        }      
        return ret;
    }
    
    public JSONArray getJSONArray(String key){
        return conf.optJSONArray(key);
    }
    
    public JSONObject getJSONObject(String key){
        return conf.optJSONObject(key);
    }
    
    public JSONObject asJSON(){
        return conf;
    }
    
    public void save() throws IOException{
        File f = new File(path());
        FileUtils.writeStringToFile(f, conf.toString(), "UTF-8");
    }
    
    @Override
    public String toString(){
        return conf.toString();
    }
}
