/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdk.client;

import static cz.incad.vdk.client.DbOperations.LOGGER;
import cz.incad.vdkcommon.VDKScheduler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 *
 * @author alberto
 */
public class InitServlet extends HttpServlet {

    Scheduler sched;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }
    
    @Override
    public void destroy(){
        try {
            sched.shutdown(false);
        } catch (SchedulerException ex) {
            Logger.getLogger(InitServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void init() throws ServletException {
        try {
            sched = VDKScheduler.getInstance().getScheduler();
            getJobs();
            sched.start();
        } catch (SQLException ex) {
            Logger.getLogger(InitServlet.class.getName()).log(Level.SEVERE, null, ex);
        }catch (SchedulerException ex) {
            Logger.getLogger(InitServlet.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private void getJobs() throws SQLException, SchedulerException{
        
            
            try {
                
                File dir = new File(System.getProperty("user.home") + File.separator + 
                            ".vdkcr" + File.separator + "jobs" + File.separator);
                File[] children = dir.listFiles();
                for (File child : children) {// check interrupted thread
                
                    
                    if (!child.isDirectory()) {
                        VDKScheduler.addJob(child);
                    }
                }
                
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } 
            
            
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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
