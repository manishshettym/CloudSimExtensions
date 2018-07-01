package org.cloudbus.cloudsim.examples.power.planetlab;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSubmitTime;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterBrokerSubmitTime;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.EnhancedHelper;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.EnhancedPowerDatacenter;
import org.cloudbus.cloudsim.power.HillClimbingAlgorithm;
import org.cloudbus.cloudsim.power.RandomBiasedSampling;
import org.cloudbus.cloudsim.power.ReplicationEnhancedPowerDatacenter;
import org.cloudbus.cloudsim.power.SectorAllocationPolicyHoneyBeeV2;
import org.cloudbus.cloudsim.power.RandomBiasedSampling;
import org.cloudbus.cloudsim.power.SectorAllocationSimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.WorkloadFileReaderLANL;
import org.cloudbus.cloudsim.util.WorkloadFileReaderLANLSubmitTime;

import java.io.*;
import java.util.Scanner;

public class EnhancedRunner 
{
	


	/** The broker. */
	protected static DatacenterBrokerSubmitTime broker;

	/** The cloudlet list. */
	protected static List<CloudletSubmitTime> cloudletList;

	/** The vm list. */
	protected static List<Vm> vmList;

	/** The host list. */
	protected static List<EnhancedHost> hostList;
	
	protected static List<Sector> sectorList;
	
	public EnhancedRunner(String workFile, String outputFolder,String confpath)throws IOException, FileNotFoundException 
	{
		
		//Read the configuration file
		File inputFolder = new File(confpath);
		File[] files = inputFolder.listFiles();
		
		String fl = files[0].getAbsolutePath();
		
		int[]  data = new int[6];
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fl)));

			// read one line at the time
			
			int i=0;
			int j=0;
			
			while (reader.ready()) 
			{
				
				if(i%2==1)
				{
						data[j]=Integer.parseInt(reader.readLine());
						Log.printLine(data[j]);
						i++;
						j++;
				}
				
				else
				{
					reader.readLine();
					i++;
				}
			}

			reader.close();
			
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		
		
		

		//call init
		//init(workFile,data[0],data[1]);
		//call start
		//start("SimpleSectorAllocation",outputFolder,data[0],data[1]);
	}
	
	private static Datacenter createDatacenter(String name , int ho){

	List<Host> hostList = new ArrayList<Host>();
		
	for(int i=0 ; i<ho ;i++)	
	{	
		
		
		List<Pe> peList1 = new ArrayList<Pe>();
		int mips = 1000;
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(4, new PeProvisionerSimple(mips)));
		
		int hostId=i;
		int ram = 2048*2; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); 

	}
		



		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
	protected void init(String workFile,int dc, int ho ,int cl , int r ,int fz1 , int fz2 ) 
	{
		try {
			
			//Step1:initialize the library
			
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  
			CloudSim.init(num_user, calendar, trace_flag);
			
			
			//Step2: Create datacenters
			for(int i=0; i<dc;i++)
			{
				Datacenter datacenter = createDatacenter("Datacenter_"+i ,ho);
			}
			
			
			//Step2: Generate cloudlets from workload file
			
			WorkloadFileReaderLANLSubmitTime workloadReader = new WorkloadFileReaderLANLSubmitTime(workFile, Constants.HOST_MIPS[0]); //1860, rating in MIPS
			cloudletList = workloadReader.generateWorkload(brokerId);
			Log.printLine("brokerId: "+brokerId);
			for(Cloudlet cl : cloudletList)
			{
				cl.setUserId(brokerId);
			}
			
			
			
			vmList = Helper.createVmList(brokerId, cloudletList.size());
			//hostList = EnhancedHelper.createEnhancedHostList(40);
			hostList = EnhancedHelper.createEnhancedHostList(100);
			//sectorList = EnhancedHelper.MakeSectorList(hostList,hostsPerSector);
			sectorList = EnhancedHelper.MakeSectorList(hostList,hostsPerSector,numFailureZones);
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}
	}
	
	
	protected void start(String experimentName, String outputFolder, int HostsPerSector,int numOfFailureZones) {
		System.out.println("Starting " + experimentName);
		
		VmAllocationPolicy vmAllocationPolicy = new SectorAllocationPolicyHoneyBeeV2(hostList, sectorList,vmList);
	
		try {
		ReplicationEnhancedPowerDatacenter datacenter = (ReplicationEnhancedPowerDatacenter) EnhancedHelper.createReplicationEnhancedDatacenter(
					"Datacenter",
					ReplicationEnhancedPowerDatacenter.class,
					hostList,
					vmAllocationPolicy,
					HostsPerSector,
					numOfFailureZones,
					vmList);  
			datacenter.setDisableMigrations(false);
			
			/*EnhancedPowerDatacenter datacenter = (EnhancedPowerDatacenter) EnhancedHelper.createEnhancedDatacenter(
					"Datacenter",
					EnhancedPowerDatacenter.class,
					hostList,
					vmAllocationPolicy,
				HostsPerSector,
				numOfFailureZones,
				vmList);  
			datacenter.setDisableMigrations(false);*/

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);


			CloudSim.terminateSimulation(10*Constants.SIMULATION_LIMIT);

			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets"); 

			CloudSim.stopSimulation();

			Helper.printResults(
					datacenter,
					vmList,
					lastClock,
					experimentName,
					Constants.OUTPUT_CSV,
					outputFolder);

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}

		Log.printLine("Finished " + experimentName);
	}

	
	
	
	/**
	 * Gets the experiment name.
	 * 
	 * @param args the args
	 * @return the experiment name
	 */
	protected String getExperimentName(String... args) {
		StringBuilder experimentName = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (args[i].isEmpty()) {
				continue;
			}
			if (i != 0) {
				experimentName.append("_");
			}
			experimentName.append(args[i]);
		}
		return experimentName.toString();
	}

	

	
	
}