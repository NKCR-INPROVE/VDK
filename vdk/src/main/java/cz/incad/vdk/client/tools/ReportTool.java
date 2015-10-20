package cz.incad.vdk.client.tools;

import cz.incad.vdk.client.DbOperations;
import cz.incad.vdkcommon.Knihovna;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class ReportTool {

    private static final Logger logger = Logger.getLogger(ReportTool.class.getName());
    protected HttpServletRequest req;
    private int offerId;
    private String type;
    private JSONObject protokol;
    //private Knihovna knihovna;

    public void configure(Map props) {
        req = (HttpServletRequest) props.get("request");
        type = req.getParameter("type");
        offerId = Integer.parseInt(req.getParameter("id"));
    }

    public JSONObject getOffer(String id) {
        try {
            return DbOperations.getOffer(offerId).getJSONObject(id);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public JSONObject getProtocol() {
        try {
            if (protokol == null) {
                protokol = DbOperations.getProtocol(offerId);
                //knihovna = new Knihovna(protokol.getJSONObject("knihovna").getInt("id"));
            }
            return protokol;
        } catch (JSONException | SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        
        }
    }

//    public Knihovna getNabizejici() {
//        if (knihovna == null) {
//            getProtocol();
//        }
//        return knihovna;
//    }
    public Knihovna getPrejimajici(int id) {
        try {
            if (protokol == null) {
                getProtocol();
            }
            Knihovna kn = new Knihovna(id);
            return kn;
        } catch (NamingException | SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public List<JSONObject> getItems(int id) {
        List<JSONObject> rows = new ArrayList<>();
        try {
            JSONArray ja = protokol.getJSONObject("prejimajici")
                    .getJSONObject("pr_" + id)
                    .getJSONArray("rows");
            for (int i = 0; i < ja.length(); i++) {
                rows.add(ja.getJSONObject(i));
            }
            return rows;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public JSONObject getOfferContent(String id) {
        try {
            return DbOperations.getOfferRows(id);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
