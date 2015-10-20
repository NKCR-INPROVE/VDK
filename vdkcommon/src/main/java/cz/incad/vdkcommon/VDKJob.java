
package cz.incad.vdkcommon;

import cz.incad.vdkcommon.oai.*;
import cz.incad.vdkcommon.solr.Indexer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;

/**
 *
 * @author alberto
 */
public class VDKJob implements InterruptableJob {

    private static final Logger LOGGER = Logger.getLogger(VDKJob.class.getName());
    VDKJobData jobdata;

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            String jobKey = jec.getJobDetail().getKey().toString();
            int i = 0;
            for (JobExecutionContext j : jec.getScheduler().getCurrentlyExecutingJobs()) {
                if (jobKey.equals(j.getJobDetail().getKey().toString())) {
                    i++;
                }
            }
            if (i > 1) {
                LOGGER.log(Level.INFO, "jobKey {0} is still running. Nothing to do.", jobKey);
                return;
            }

            JobDataMap data = jec.getMergedJobDataMap();
            if(data.containsKey("runtime_data")){
                jobdata = (VDKJobData) data.get("jobdata");
                jobdata.setRuntimeOptions((JSONObject) data.get("runtime_data"));
            }else{
                jobdata = (VDKJobData) data.get("jobdata");
            }
            
            jobdata.load();
            jobdata.setInterrupted(false);

            if(jobdata.getType().equalsIgnoreCase("admin")){
                AdminJob aj = new AdminJob(jobdata);
                aj.run();
            }else if(jobdata.getType().equalsIgnoreCase("harvest")){
                OAIHarvester oh = new OAIHarvester(new HarvesterJobData(jobdata));
                oh.harvest();
            }else if(jobdata.getType().equalsIgnoreCase("index")){
                Indexer indexer =  new Indexer(jobdata);
                indexer.run();
            }

            LOGGER.log(Level.INFO, "jobKey: {0}", jobKey);

        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(VDKJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        //Thread.currentThread().interrupt();
        jobdata.setInterrupted(true);
    }

}
