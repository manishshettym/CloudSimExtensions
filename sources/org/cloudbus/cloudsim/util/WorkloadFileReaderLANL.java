package org.cloudbus.cloudsim.util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.cloudbus.cloudsim.Cloudlet;





public class WorkloadFileReaderLANL extends WorkloadFileReader
{
		// override super jobs
		ArrayList<Cloudlet> jobs;
		
	
		public WorkloadFileReaderLANL(final String fileName, final int rating) throws FileNotFoundException
		{
			super( fileName,  rating);
		}
		
		public ArrayList<Cloudlet> generateWorkload(int userId)
		{
			if (jobs == null) {
				jobs = new ArrayList<Cloudlet>();

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
			System.out.println("Lenghts of jobs"+jobs.size());
			return jobs;
		}

}