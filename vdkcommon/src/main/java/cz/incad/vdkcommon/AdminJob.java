/*
 * Copyright (C) 2013-2015 Alberto Hernandez
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
package cz.incad.vdkcommon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;

/**
 *
 * @author alberto
 */
public class AdminJob {

    Logger LOGGER = Logger.getLogger(AdminJob.class.getName());

    private final Options opts;
    private final VDKJobData jobData;
    String configFile;

    public AdminJob(VDKJobData jobData) throws Exception {
        this.jobData = jobData;
        this.configFile = jobData.getConfigFile();
        opts = Options.getInstance();
        init();
    }

    private void init() {

    }

    public void run() {
        checkOffers();
    }

    //Check expiration date offer.
    // When expires archive them and send emails
    private void checkOffers() {
        try {
            Calendar now = Calendar.getInstance();
            Calendar o = Calendar.getInstance();
            Connection conn = DbUtils.getConnection();
            String sql = "select o.nazev, o.offer_id, o.datum, kn.email from offer o, knihovna kn"
                    + " where o.knihovna=kn.knihovna_id and archived=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setBoolean(1, false);
            
            String sql2 = "select * from zaznamoffer zo, knihovna kn where "
                    + "zo.pr_knihovna=kn.knihovna_id and zo.offer=?";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            
            String sql3 = "update offer set archived=? where "
                    + "offer_id=?";
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                o.setTime(rs.getDate("datum"));
                o.add(Calendar.DATE, opts.getInt("expirationDays", 7) * 3);
                if(o.before(now)){
                    //expired. Should archive and send emails
                    sendMail(rs.getString("email"), rs.getString("nazev"), rs.getString("offer_id"));
                    
                    ps2.setInt(1, rs.getInt("offer_id"));
                    ResultSet rs2 = ps2.executeQuery();
                    while(rs2.next()){
                        sendMail(rs2.getString("email"), rs2.getString("nazev"), rs.getString("offer_id"));
                    }
                    rs2.close();
                    
                    ps3.setBoolean(1, true);
                    ps3.setInt(2, rs.getInt("offer_id"));
                    ps3.executeUpdate();
                }
            }
            rs.close();
        } catch (NamingException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking offers");
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void sendMail(String to, String offerName, String offerId) {
        String from = opts.getString("admin.email");
        try {
            Properties properties = System.getProperties();
            Session session = Session.getDefaultInstance(properties);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));
            
            message.setSubject(opts.getString("admin.email.offer.subject"));
            
            String link = opts.getString("app.url") + "/reports/protocol.vm?id=" + offerId;
            String body = opts.getString("admin.email.offer.body")
                    .replace("${offer.nazev}", offerName)
                    .replace("${offer.report}", link);
            message.setText(body);

            Transport.send(message);
            LOGGER.fine("Sent message successfully....");
        } catch (MessagingException ex) {
            LOGGER.log(Level.SEVERE, "Error sending email to: {0}, from {1} ", new Object[]{to, from});
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
