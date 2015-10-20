/*
 * Copyright (C) 2014 Alberto Hernandez
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
package cz.incad.vdk.client;

import cz.incad.vdkcommon.DbUtils;
import cz.incad.vdkcommon.Knihovna;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

import javax.servlet.http.HttpServletRequest;

public class LoggedController {

    static final Logger LOGGER = Logger.getLogger(LoggedController.class.getName());

    //protected HttpServletRequest req;
    public static String LOG_CONTROL_KEY = "logControl";
    
    boolean logged;
    String loggedName;
    Knihovna knihovna;

    public LoggedController(HttpServletRequest req) {
        this.logged = req.getSession() != null
                && req.getRemoteUser() != null
                && !req.getRemoteUser().equals("");
        this.loggedName = req.getRemoteUser();
    }

    public static boolean isLogged(HttpServletRequest req) {
        LoggedController logControl = (LoggedController) req.getSession().getAttribute(LoggedController.LOG_CONTROL_KEY);

        return logControl.isLogged();
    }

    public static Knihovna knihovna(HttpServletRequest req) {
        LoggedController logControl = (LoggedController) req.getSession().getAttribute(LoggedController.LOG_CONTROL_KEY);
        if (logControl == null) {
            logControl = new LoggedController(req);
            req.getSession().setAttribute(LoggedController.LOG_CONTROL_KEY, logControl);
        }
        System.out.println(logControl);
        return logControl.getKnihovna();
    }

    public boolean isLogged() {
        return this.logged;
    }
    
    public boolean hasRole(String role){
        return this.logged &&
                this.getKnihovna().hasRole(DbUtils.Roles.valueOf(role));
    }
    
    public boolean isAdmin(){
        return this.logged &&
                this.getKnihovna().hasRole(DbUtils.Roles.ADMIN);
    }

    public String getLoggedName() {
        if (!this.isLogged()) {
            return "Login";
        } else {
            return this.loggedName;
        }
    }

    public String getPriorita() {
        if (!this.isLogged()) {
            return "";
        } else {
            return this.getKnihovna().getPriorita() + "";
        }
    }

    public String getCode() {
        if (!this.isLogged()) {
            return "";
        } else {
            return this.getKnihovna().getCode();
        }
    }

    public String getUserJSONRepresentation() {
        if (!this.isLogged()) {
            return "{}";
        } else {
            return "{}";
        }
    }

    public Knihovna getKnihovna() {
        try {
            if (!this.isLogged()) {
                return null;
            } else if (this.knihovna == null) {
                Knihovna kn = new Knihovna(this.loggedName);
                this.knihovna = kn;
                return kn;
            } else {
                return this.knihovna;
            }
        } catch (NamingException | SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
