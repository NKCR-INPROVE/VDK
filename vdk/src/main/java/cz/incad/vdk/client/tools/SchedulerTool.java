/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdk.client.tools;

import cz.incad.vdkcommon.VDKScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;

/**
 *
 * @author alberto
 */
public class SchedulerTool {

    private static final Logger logger = Logger.getLogger(SchedulerTool.class.getName());
    public ArrayList<String> getJobs() {
        ArrayList<String> ret = new ArrayList<String>();
        try {
            Scheduler scheduler = VDKScheduler.getInstance().getScheduler();
	 
	        for (String groupName : scheduler.getJobGroupNames()) {
	            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher
	                    .jobGroupEquals(groupName))) {
	                String jobName = jobKey.getName();
	                String jobGroup = jobKey.getGroup();
	                List<Trigger> triggers = (List<Trigger>) scheduler
	                        .getTriggersOfJob(jobKey);
                        
	                ret.add("[jobName] : " + jobName + " [groupName] : "
	                        + jobGroup + " - " + triggers.get(0).getNextFireTime());
	            }
	 
	        }


    
    
            return ret;
        } catch (SchedulerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
