/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcommon;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class VDKJobData {
    
    private static final Logger logger = Logger.getLogger(VDKJobData.class.getName());

    
    private JSONObject opts;
    
    private String configFile;
    private String configDir;
    private String configSimpleName;
    private String statusFile;
    
    private boolean interrupted = false;
    private JSONObject runtimeOptions;
    private String name;
    
    public VDKJobData(String conf, JSONObject runtime) throws Exception{
        this.configFile = conf;
        this.runtimeOptions = runtime;
    }
    
   public void load() throws Exception {
        File fdef = FileUtils.toFile(Options.class.getResource("/cz/incad/vdkcommon/job.json"));
        String json = FileUtils.readFileToString(fdef, "UTF-8");
        opts = new JSONObject(json);
        
        
        File f = new File(this.configFile);
        this.configDir = f.getParent();
        this.configSimpleName = f.getName().split("\\.")[0];
        this.statusFile = this.configDir + File.separator + "status" + File.separator + this.configSimpleName + ".status";
        if (f.exists() && f.canRead()) {
            json = FileUtils.readFileToString(f, "UTF-8");
            JSONObject confCustom = new JSONObject(json);
            Iterator keys = confCustom.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                opts.put(key, confCustom.get(key));
            }
        }

        Iterator keys = runtimeOptions.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            opts.put(key, runtimeOptions.get(key));
        }
        logger.info("VDKJobData loaded");
    }
   
   public String getString(String key){
       return opts.getString(key);
   }
   
   public String getString(String key, String def){
       return opts.optString(key, def);
   }
   
   public boolean getBoolean(String key){
       return opts.getBoolean(key);
   }
   
   public boolean getBoolean(String key, boolean def){
       return opts.optBoolean(key, def);
   }
   
   public int getInt(String key){
       return opts.getInt(key);
   }
   
   public int getInt(String key, int def){
       return opts.optInt(key, def);
   }

    /**
     * @return the opts
     */
    public JSONObject getOpts() {
        return opts;
    }

    /**
     * @param opts the opts to set
     */
    public void setOpts(JSONObject opts) {
        this.opts = opts;
    }


    /**
     * @param runtimeOptions the runtimeOptions to set
     */
    public void setRuntimeOptions(JSONObject runtimeOptions) {
        this.runtimeOptions = runtimeOptions;
    }
    
    /**
     * @return the configFile
     */
    public String getStatusFile() {
        return statusFile;
    }

    /**
     * @return the configSimpleName
     */
    public String getConfigSimpleName() {
        return this.configSimpleName;
    }

    /**
     * @return the configDir
     */
    public String getConfigDir() {
        return configDir;
    }

    /**
     * @return the configFile
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * @param configFile the configFile to set
     */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * @return the interrupted
     */
    public boolean isInterrupted() {
        return interrupted;
    }

    /**
     * @param interrupted the interrupted to set
     */
    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType(){
        return opts.getString("type");
    }


    boolean isHarvest() {
        return opts.optBoolean("harvest", false);
    }

    boolean isIndex() {
        return opts.optBoolean("index", false);
    }

}
