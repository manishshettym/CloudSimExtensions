package org.cloudbus.cloudsim.examples;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.util.WorkloadFileReader;


public class WorkloadHelper {

	/**
	 * Creates the cloudlet list for given workload file(swf).
	 */
	public static List<Cloudlet> createCloudletList(int brokerId, String workFile)
			throws FileNotFoundException 
	{
		List<Cloudlet> list = new ArrayList<Cloudlet>();
		
		WorkloadFileReader workloadFileReader=new WorkloadFileReader(workFile,1);
		
		//generate cloudlets from workload file
		list = workloadFileReader.generateWorkload();
		
		//set broker id and vm id
		for (int i = 0; i < list.size(); i++) 
		{
			list.get(i).setUserId(brokerId);
			list.get(i).setVmId(i);
		}
		

		return list;
	}

}
