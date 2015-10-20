/*
 * Copyright (C) 2013 Pavel Stastny
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.vdk.client.tools;

import cz.incad.vdk.client.LoggedController;
import cz.incad.vdkcommon.DbUtils;
import cz.incad.vdkcommon.Knihovna;
import java.sql.SQLException;
import java.util.Map;
import javax.naming.NamingException;

import javax.servlet.http.HttpServletRequest;


/**
 * Controls whether current session is authenticated
 * 
 * @author pavels
 */
public class LoggedControllerTool {

    protected HttpServletRequest req;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
    }

    /**
     * Returns true if the current session is authenticated
     * @return
     */
    public boolean isLogged() {
        return req.getSession() != null
                && req.getRemoteUser() != null 
                && !req.getRemoteUser().equals("");
    }
    
    public String getLoggedName() {
        if (!this.isLogged()) return "Login"; 
        else { 
            return req.getRemoteUser();
        }
    }

    public String getUserJSONRepresentation() {
        if (!this.isLogged()) return "{}"; 
        else {
            return LoggedController.knihovna(req).getJson().toString();
        }
    }
    
    
    
    public boolean isAdmin(){
        return this.isLogged() &&
                LoggedController.knihovna(req).hasRole(DbUtils.Roles.ADMIN);
    }
    
    public void setKnihovna() throws NamingException, SQLException{
        Knihovna kn = new Knihovna(req.getRemoteUser());
        req.getSession().setAttribute("knihovna", kn);
    }
    
}
