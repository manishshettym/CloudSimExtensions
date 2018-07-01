package org.cloudbus.cloudsim.util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.Integer;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSubmitTime;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Log;


public class WorkloadFileReaderSDSC extends WorkloadFileReader
{
	static int count = 0;
	protected final int QUEUE = 15 - 1;
	@SuppressWarnings("deprecation")
	@Override
	protected void extractField(final String[] array, final int line) {
		try {
			Integer obj = null;

			// get the job number
			int id = 0;
			if (JOB_NUM == IRRELEVANT) {
				id = jobs.size() + 1;
			} else {
				obj = new Integer(array[JOB_NUM].trim());
				id = obj.intValue();
			}

			// get the submit time
			final Long l = new Long(array[SUBMIT_TIME].trim());
			final long submitTime = l.intValue();

			// get the user estimated run time
			obj = new Integer(array[REQ_RUN_TIME].trim());
			final int reqRunTime = obj.intValue();

			// if the required run time field is ignored, then use
			// the actual run time
			obj = new Integer(array[RUN_TIME].trim());
			int runTime = obj.intValue();

			final int userID = new Integer(array[USER_ID].trim()).intValue();
			final int groupID = new Integer(array[GROUP_ID].trim()).intValue();

			// according to the SWF manual, runtime of 0 is possible due
			// to rounding down. E.g. runtime is 0.4 seconds -> runtime = 0
			if (runTime <= 0) {
				runTime = 1; // change to 1 second
			}

			// get the number of allocated processors
			obj = new Integer(array[REQ_NUM_PROC].trim());
			int numProc = obj.intValue();

			// if the required num of allocated processors field is ignored
			// or zero, then use the actual field
			if (numProc == IRRELEVANT || numProc == 0) {
				obj = new Integer(array[NUM_PROC].trim());
				numProc = obj.intValue();
			}

			// finally, check if the num of PEs required is valid or not
			if (numProc <= 0) {
				numProc = 1;
			}
			if(Integer.valueOf(array[QUEUE]) == 0) 	// in the SDSC workload queue 0 is for interactive jobs
			{
				Log.printLine("Line no is: "+ count++);
				Log.printLine(id);
				if(count>1000) {
				return;
				}
				createJob(id, submitTime, runTime, numProc, reqRunTime, userID, groupID);
				
			}
			return;
		} catch (final Exception e) {
				e.printStackTrace();
		}
	}


	// override super jobs
			ArrayList<CloudletSubmitTime> jobs;
			
			@Override
			protected void createJob(
					final int id,
					final long submitTime,
					final int runTime,
					final int numProc,
					final int reqRunTime,
					final int userID,
					final int groupID) {
				// create the cloudlet
				final int len = runTime * rating;
				UtilizationModel utilizationModel = new UtilizationModelFull();
				final CloudletSubmitTime wgl = new CloudletSubmitTime(
						id,
						len,
						numProc,
						0,
						0,
						utilizationModel,
						utilizationModel,
						utilizationModel, submitTime);
				jobs.add(wgl);
			}
			

	
	
	public WorkloadFileReaderSDSC(final String fileName, final int rating) throws FileNotFoundException
	{
		super( fileName,  rating);
	}
	
	public ArrayList<CloudletSubmitTime> generateWorkload(int userId)
	{
		if (jobs == null) {
			jobs = new ArrayList<CloudletSubmitTime>();

			// create a temp array
			fieldArray = new String[MAX_FIELD];

			try {
				if (file.getName().endsWith(".gz")) {
					readGZIPFile(file);
				} else if (file.getName().endsWith(".zip")) {
					readZipFile(file);
				} else {
					readFile(file);
				}
			} catch (final FileNotFoundException e) {
			} catch (final IOException e) {
			}
		}
		for(Cloudlet cl : jobs )
			cl.setUserId(userId);
		return jobs;
	}

}
