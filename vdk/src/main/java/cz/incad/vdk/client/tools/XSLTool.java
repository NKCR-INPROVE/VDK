/*
 * Copyright (C) 2013 alberto
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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.config.DefaultKey;

/**
 *
 * @author alberto
 */
@DefaultKey("xslt")
public class XSLTool {

    Transformer transformer;
    String xslt;
    String xml;
    
    
    HashMap<String, String> params = new HashMap<String, String>();
    
    public String transform() {
            return transform(xml, xslt);
    }

    public String transform(String source, String xsl) {
        try {
            TransformerFactory tfactory = TransformerFactory.newInstance();
            StreamSource xsltSource = new StreamSource(new StringReader(xsl));
            transformer = tfactory.newTransformer(xsltSource);
            StreamResult destStream = new StreamResult(new StringWriter());
            
            for(Map.Entry<String, String> entry : params.entrySet()){
                transformer.setParameter(entry.getKey(), entry.getValue());
            }
            transformer.transform(new StreamSource(new StringReader(source)), destStream);
            StringWriter sw = (StringWriter) destStream.getWriter();
            return sw.toString();
        }catch (TransformerConfigurationException ex2) {
            Logger.getLogger(XSLTool.class.getName()).log(Level.SEVERE, null, ex2);
            return null;
        } catch (TransformerException ex) {
            Logger.getLogger(XSLTool.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public void addParam(String name, String value){
        if(value == null){
            value = "";
        }
        params.put(name, value);
    }
    
    public void setTemplatedXsl(String styleName){
        try{
            Template t = Velocity.getTemplate(styleName);
            System.out.println(t.toString());
        }catch(Exception ex){
            Logger.getLogger(XSLTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setXsl(String xslt){
        this.xslt = xslt;
    }
    
    public void setXml(String xml){
        this.xml = xml;
    }
    


}
