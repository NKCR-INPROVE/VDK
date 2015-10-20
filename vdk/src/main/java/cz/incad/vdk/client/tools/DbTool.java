package cz.incad.vdk.client.tools;

import cz.incad.vdk.client.DbOperations;
import cz.incad.vdkcommon.Knihovna;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author alberto
 */
public class DbTool {

    private static final Logger logger = Logger.getLogger(DbTool.class.getName());
    protected HttpServletRequest req;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
    }

    public ArrayList<Knihovna> getUsers() {
        try {
            return DbOperations.getUsers();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public ArrayList<String> getRoles() {
        try {
            return DbOperations.getRoles();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
