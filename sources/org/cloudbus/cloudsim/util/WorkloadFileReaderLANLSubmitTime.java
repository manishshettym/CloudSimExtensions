package org.cloudbus.cloudsim.util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSubmitTime;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;



public class WorkloadFileReaderLANLSubmitTime extends WorkloadFileReader
{
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
		
		public WorkloadFileReaderLANLSubmitTime(final String fileName, final int rating) throws FileNotFoundException
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