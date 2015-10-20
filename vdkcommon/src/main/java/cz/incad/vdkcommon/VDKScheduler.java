
package cz.incad.vdkcommon;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.core.jmx.JobDataMapSupport;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author alberto
 */
public class VDKScheduler {
    
    static final Logger LOGGER = Logger.getLogger(VDKScheduler.class.getName());
    private static VDKScheduler _sharedInstance = null;
    private org.quartz.Scheduler scheduler;
    
    public synchronized static VDKScheduler getInstance() {
        if (_sharedInstance == null) {
            _sharedInstance = new VDKScheduler();
        }
        return _sharedInstance;
    }
    
    public VDKScheduler(){
        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            scheduler = sf.getScheduler();
        }catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } 
    }

    /**
     * @return the scheduler
     */
    public org.quartz.Scheduler getScheduler() {
        return scheduler;
    }
    
    
    public static void addJob(File f) throws Exception{
        JSONObject js = new JSONObject(FileUtils.readFileToString(f, "UTF-8"));
        org.quartz.Scheduler sched = VDKScheduler.getInstance().getScheduler();
        Map<String, Object> map = new HashMap<String, Object>();
        VDKJobData jobdata = new VDKJobData(f.getAbsolutePath(), new JSONObject());
        map.put("jobdata", jobdata);
        String jobName = f.getName().split("\\.")[0];
        JobDataMap data = JobDataMapSupport.newJobDataMap(map);
        JobDetail job = JobBuilder.newJob(VDKJob.class)
                .withIdentity(jobName)
                .setJobData(data)
                .build();
        if (sched.checkExists(job.getKey())) {
            sched.deleteJob(job.getKey());
        }
        String cronVal = js.optString("cron", "");
        if(cronVal.equals("")){
            sched.addJob(job, true, true);
            LOGGER.log(Level.INFO, "Job {0} added to scheduler", jobName);
            
        }else{
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_" + jobName)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronVal))
                    .build();
            sched.scheduleJob(job, trigger);
            LOGGER.log(Level.INFO, "Job {0} added to scheduler with {1}", new Object[]{jobName, cronVal});
        }
    }
    
    public static void addJob(String name, String cronVal, 
            String conf) throws SchedulerException, Exception {

        org.quartz.Scheduler sched = VDKScheduler.getInstance().getScheduler();
        Map<String, Object> map = new HashMap<String, Object>();
        VDKJobData jobdata = new VDKJobData(conf, new JSONObject());
        map.put("jobdata", jobdata);
        JobDataMap data = JobDataMapSupport.newJobDataMap(map);

        JobDetail job = JobBuilder.newJob(VDKJob.class)
                .withIdentity(conf)
                .setJobData(data)
                .build();
        if (sched.checkExists(job.getKey())) {
            sched.deleteJob(job.getKey());
        }
        if(cronVal.equals("")){
            LOGGER.log(Level.INFO, "Cron for {0} cleared ", conf);
        }else{
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger_" + conf)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronVal))
                    .build();
            sched.scheduleJob(job, trigger);
            LOGGER.log(Level.INFO, "Cron for {0} scheduled with {1}", new Object[]{name, cronVal});
        }
    }
    
    
}
