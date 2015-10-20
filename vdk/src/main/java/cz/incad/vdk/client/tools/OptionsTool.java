/*
 * Copyright (C) 2015 alberto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.vdk.client.tools;

import cz.incad.vdkcommon.Options;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author alberto
 */
public class OptionsTool {
    public String getString(String key){
        try {
            return Options.getInstance().getString(key);
        } catch (IOException | JSONException ex) {
            Logger.getLogger(OptionsTool.class.getName()).log(Level.SEVERE, null, ex);
            return key;
        }
    }
    
    public String[] getLevels(){
        try {
            JSONArray arr = Options.getInstance().getJSONArray("admin.users.levels");
            String[] ret = new String[arr.length()];
            for(int i = 0; i<arr.length(); i++){
                ret[i] = Integer.toString(arr.getInt(i));
            }      
            return ret;
        } catch (IOException | JSONException ex) {
            Logger.getLogger(OptionsTool.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
