/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdk.client;

import cz.incad.vdkcommon.Options;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author alberto
 */
public class VelocityViewServlet extends org.apache.velocity.tools.view.VelocityViewServlet {
    @Override
    protected void setContentType(HttpServletRequest request,
            HttpServletResponse response) {
        if (request.getRequestURI().endsWith("logout.vm")) {
            HttpSession session = request.getSession(false);
            if(session!=null){
                session.invalidate();
            }
        }
        //System.out.println("SESSION: " + request.getSession().getId());
        Options.resetInstance();
        LoggedController logControl =  new LoggedController(request);
        request.getSession().setAttribute(LoggedController.LOG_CONTROL_KEY, logControl);
//        try {
//            logControl.setKnihovna();
//        } catch (NamingException | SQLException ex) {
//            Logger.getLogger(VelocityViewServlet.class.getName()).log(Level.SEVERE, null, ex);
//        }
        if (request.getRequestURI().endsWith(".css")) {
            response.setContentType("text/css");
        } else if(request.getRequestURI().contains("/csv/")) {
            String filename = "vdk_export.csv";
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/x-csv");
            response.setHeader("Content-Disposition", "attachment; filename="+filename);
        }else {
            response.setContentType(getVelocityView().getDefaultContentType());
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
