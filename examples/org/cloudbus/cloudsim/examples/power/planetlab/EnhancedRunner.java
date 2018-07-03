package org.cloudbus.cloudsim.examples.power.planetlab;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Aisle;
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
import org.cloudbus.cloudsim.Rack;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
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
	protected static DatacenterBroker broker;

	/** The cloudlet list for workload. */
	protected static List<CloudletSubmitTime> cloudletListworkload;
	
	protected static List<Cloudlet> cloudletList;
	
	protected static List<EnhancedPowerDatacenter> datacenterList;
	
	/** The vm list. */
	protected static List<Vm> vmList;

	/** The lists !! */
	protected static List<EnhancedHost> hostList;
	protected static List<Sector> sectorList;
	protected static List<Aisle>aisleList;
	protected static List<Rack>rackList;
	
	
	public EnhancedRunner(String workFile, String outputFolder,String confpath)throws IOException, FileNotFoundException 
	{
		
		//Read the configuration file
		File inputFolder = new File(confpath);
		File[] files = inputFolder.listFiles();
		
		String fl = files[0].getAbsolutePath();
		
		int[]  data = new int[12];
		
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
		int [] failurezones = new int[6];
		failurezones = init(workFile,data[0],data[1],data[2],data[3],data[4],data[5],data);
		
		//call start
		start("SimpleSectorAllocation",outputFolder,failurezones);
	}
	
	protected int [] init(String workFile,int dc,int sectorPerDC , int aislesPerSector , int racksPerAisle, int hostsPerRack ,int cloudlets , int [] data)
	{
		int [] failurezones = new int[6];
		
		try {
			
			//Step1:initialize the library
			
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  
			CloudSim.init(num_user, calendar, trace_flag);
			
			int totalhosts = sectorPerDC * aislesPerSector * racksPerAisle * hostsPerRack ;
			
			hostList = EnhancedHelper.createEnhancedHostList(totalhosts);
			rackList = EnhancedHelper.createRackList(hostList,hostsPerRack);
			aisleList = EnhancedHelper.createAisleList(rackList,racksPerAisle);
			sectorList = EnhancedHelper.createSectorList(aisleList,aislesPerSector);
			

			
			int j=0;
			//parsing data to get only failure zones info:
			for(int i=0 ; i< data.length ; i++)
			{
				if(i>5)
				{
					failurezones[j++]=data[i];
				}
					
			}
			
			//initialize the alloc policy
			VmAllocationPolicy vmAllocationPolicy = new FzonesVmAllocationPolicy(hostList,rackList,aisleList,sectorList,failurezones);
			
			
			//Step2: Create datacenters
			datacenterList = new ArrayList <EnhancedPowerDatacenter>();
			for(int i=0; i<dc;i++)
			{
				
				datacenterList.add( EnhancedHelper.createEnhancedDatacenter("Datacenter_"+i , sectorList,aisleList,rackList,hostList,vmAllocationPolicy));
			}
			
			
			
			
			
			
			//Step3: Create the broker
			broker = createBroker();
			int brokerId = broker.getId();
			
			
			//Step4: Generate cloudlets from workload file OR using the no.
			cloudletList = createCloudlet(brokerId,cloudlets);
			
			/*WorkloadFileReaderLANLSubmitTime workloadReader = new WorkloadFileReaderLANLSubmitTime(workFile, Constants.HOST_MIPS[0]); //1860, rating in MIPS
			cloudletListworkload = workloadReader.generateWorkload(brokerId);
			Log.printLine("brokerId: "+brokerId);
			for(Cloudlet cl : cloudletListworkload)
			{
				cl.setUserId(brokerId);
			}*/
			
			
			
			//Step5: Create enough Vms for cloudlets
			vmList = Helper.createVmList(brokerId, cloudletList.size());

			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}
	
		return failurezones;
	}
	
	
	protected void start(String experimentName, String outputFolder,int [] failurezones) {
		System.out.println("Starting " + experimentName);
		
	
		try {
			
		
			broker.setfailurezones(failurezones);
			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);


			CloudSim.terminateSimulation(10*Constants.SIMULATION_LIMIT);

			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets"); 

			CloudSim.stopSimulation();

			for(EnhancedPowerDatacenter datacenter : datacenterList )
			{	
				Helper.printResults(
						datacenter,
						vmList,
						lastClock,
						experimentName,
						Constants.OUTPUT_CSV,
						outputFolder);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}

		Log.printLine("Finished " + experimentName);
	}
	
	
	private static DatacenterBroker createBroker(){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	
	private static List<Cloudlet> createCloudlet(int userId, int cloudlets){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		long length = 1000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

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