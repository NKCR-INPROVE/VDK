/*
 * Copyright (C) 2014 alberto
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package cz.incad.vdk.client.tools;
import cz.incad.vdkcommon.Options;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;


import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.ResourceTool;

/**
 *
 * @author alberto
 */
@DefaultKey("i18n")
public class LanguageTool extends ResourceTool {
    
    public static final Logger LOGGER = Logger.getLogger(LanguageTool.class.getName());

    HttpServletRequest req;
    String language;
    String bundleUrl;
    private final String LANGUAGE_PARAM = "language";

    @Override
    public void configure(Map props) {
        try {
            req = (HttpServletRequest) props.get("request");
            language = req.getParameter(LANGUAGE_PARAM);
            setDefaultBundle((String)props.get("bundles"));
            if(language!=null && !"".equals(language)){
                req.getSession().setAttribute(LANGUAGE_PARAM, language);
                setLocale(ConversionUtils.toLocale(language));
            }else{
                if(req.getSession().getAttribute("language") != null){
                    language = (String) req.getSession().getAttribute("language");
                    setLocale(ConversionUtils.toLocale(language));
                }else{
                    language = req.getLocale().getLanguage();
                    setLocale(ConversionUtils.toLocale(language));
                }
            }
            String app = Options.getInstance().getString("app.host", "http://localhost:8080");
            bundleUrl =  app + "/i18n?action=bundle&format=xml&name=labels&language=" + language;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        
    }
    
    public String getBundleUrl(){
        return bundleUrl;
    }
    
    public String getLanguage(){
        return language;
    }
}
