package org.cloudbus.cloudsim.examples.power;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.cloudbus.cloudsim.Aisle;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Rack;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Summary;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.planetlab.FzonesVmAllocationPolicy;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.EnhancedPowerDatacenter;
import java.io.*;

public class EnhancedRunner 
{
	
	/** The enable output. */
	private static boolean enableOutput;


	/** The broker. */
	protected static DatacenterBroker broker;

	
	protected static List<Cloudlet> cloudletList;
	
	protected static List<EnhancedPowerDatacenter> datacenterList;
	
	/** The vm list. */
	protected static List<Vm> vmList;

	/** The lists may be needed !! */
	protected static List<EnhancedHost> hostList;
	protected static List<Sector> sectorList;
	protected static List<Aisle>aisleList;
	protected static List<Rack>rackList;
	
	
	public EnhancedRunner(String workFile, String outputFolder,String confpath)throws IOException, FileNotFoundException 
	{
		boolean enableOutput = true;
		boolean outputToFile = true;
		
		//logging all outputs
		try {
			initLogOutput(
					enableOutput,
					outputToFile,
					outputFolder,
					"SimpleSectorAllocation");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		//logging the summarized output
		try {
			initSummaryOutput(enableOutput,
					outputToFile,
					outputFolder,
					"SimpleSectorAllocation");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
		
		//Read the configuration file
		File inputFolder = new File(confpath);
		File[] files = inputFolder.listFiles();
		
		String fl = files[0].getAbsolutePath();
		
		int[]  data = new int[13];
		
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fl)));

			// read one line at the time0
			
			Summary.printLine("CONFIGURATION USED");
			
			int i=0;
			int j=0;
			
			while (reader.ready()) 
			{
				
				if(i%2==1)
				{
						data[j]=Integer.parseInt(reader.readLine());
						Summary.printLine(data[j]);
						i++;
						j++;
				}
				
				else
				{
					Summary.print(reader.readLine()+ ": ");
					i++;
				}
			}

			reader.close();
			Summary.printLine();
			
			
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
		int [] failurezones = new int[7];
		
		try {
			
			//Step1:initialize the library
			
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  
			CloudSim.init(num_user, calendar, trace_flag);
			
			int totalhosts = sectorPerDC * aislesPerSector * racksPerAisle * hostsPerRack ;
			int j=0;
			
			for(int i=0 ; i< data.length-1 ; i++)
			{
				if(i>5)
				{
					failurezones[j]=data[i];
					j++;
				}
				
			}
			
			failurezones[6]=failurezones[0];
			
			
			//Step2: Create datacenters and other entities
			datacenterList = new ArrayList <EnhancedPowerDatacenter>();
			for(int i=0; i<dc;i++)
			{
				
				List<EnhancedHost> hosts = new ArrayList<EnhancedHost>();
				List<Rack>racks = new ArrayList<Rack>();
				List<Aisle>aisles = new ArrayList<Aisle>();
				List<Sector> sectors = new ArrayList<Sector>();
				
				
				hosts = EnhancedHelper.createEnhancedHostList(totalhosts);
				racks = EnhancedHelper.createRackList(hosts,hostsPerRack);
				aisles = EnhancedHelper.createAisleList(racks,racksPerAisle);
				sectors = EnhancedHelper.createSectorList(aisles,aislesPerSector);
				
				

				
				VmAllocationPolicy vmAllocationPolicy = new FzonesVmAllocationPolicy(hosts,racks,aisles,sectors,failurezones);
				
				datacenterList.add( EnhancedHelper.createEnhancedDatacenter("Datacenter_"+i , sectors,aisles,racks,hosts,vmAllocationPolicy));
				
				
			}
			
			
			
			//Step3: Create the broker
			broker = createBroker();
			int brokerId = broker.getId();
			
			
			DatacenterBroker.setDcLeft(datacenterList.size()-1);
			
			
			
			
			//Step4: Generate cloudlets from workload file OR using the no.
			if(cloudlets!=0)
			{
				cloudletList = createCloudlet(brokerId,cloudlets);
			}
			
			
			else
			{	
				Log.printLine("ENTERED WORKLOAD CASE for file : ");
				cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, workFile);
				Log.printLine(cloudletList.size() + " cloudlets have been created");
			}
			
			
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
		
		Summary.printLine(" INITIALIZING the configuration ");
		
	
		try {
			
		
			broker.setfailurezones(failurezones);
			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);


			CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT); //has to be 1 day => each cloudlet has only 288 5min time slices => 288*5 = 1 day in min
			
			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets"); 

			
			CloudSim.stopSimulation();

			for(EnhancedPowerDatacenter datacenter : datacenterList )
			{	
				Helper.printResults(
						datacenter,
						DatacenterBroker.vmList,
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
	
	public static EnhancedPowerDatacenter getDcById( int id)
	{
		for(EnhancedPowerDatacenter datacenter : datacenterList)
		{
			if(datacenter.getId()==id)
				return datacenter;
		}
		
		return null;
	}

	
// LOGGING FUNCTIONS
	protected void initLogOutput(
			boolean enableOutput,
			boolean outputToFile,
			String outputFolder,
			String parameter) throws IOException, FileNotFoundException {
		
		setEnableOutput(enableOutput);
		Log.setDisabled(!isEnableOutput());
		
		if (isEnableOutput() && outputToFile) {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			File folder2 = new File(outputFolder + "/log");
			if (!folder2.exists()) {
				folder2.mkdir();
			}

			File file = new File(outputFolder + "/log/"
					+ getExperimentName(parameter) + ".txt");
			file.createNewFile();
			Log.setOutput(new FileOutputStream(file));
		}
	}
	
	public void setEnableOutput(boolean enableOutput) {
		EnhancedRunner.enableOutput = enableOutput;
	}

	/**
	 * Checks if is enable output.
	 * 
	 * @return true, if is enable output
	 */
	public boolean isEnableOutput() {
		return enableOutput;
	}
	
	
	//Summarized output logging
	protected void initSummaryOutput(
			boolean enableOutput,
			boolean outputToFile,
			String outputFolder,
			String parameter
			) throws IOException, FileNotFoundException 
	{
		
		setEnableOutput(enableOutput);
		Summary.setDisabled(!isEnableOutput());
		
		if (isEnableOutput() && outputToFile) 
		{
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			File folder2 = new File(outputFolder + "/summary");
			if (!folder2.exists()) {
				folder2.mkdir();
			}

			File file = new File(outputFolder + "/summary/"
					+ getExperimentName(parameter) + ".txt");
			file.createNewFile();
			Summary.setOutput(new FileOutputStream(file));
		}
	}
	
	
}