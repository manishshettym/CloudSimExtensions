package org.cloudbus.cloudsim.examples.power;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Aisle;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterBrokerSubmitTime;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Rack;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.examples.power.planetlab.FzonesVmAllocationPolicy;
import org.cloudbus.cloudsim.power.EnhancedPowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;

import org.cloudbus.cloudsim.power.ReplicationEnhancedPowerDatacenter;

import org.cloudbus.cloudsim.power.PowerDatacenterBrokerSubmitTime;

import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class EnhancedHelper extends Helper
{
	
	
	public static PowerDatacenterBrokerSubmitTime createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new PowerDatacenterBrokerSubmitTime("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return (PowerDatacenterBrokerSubmitTime) broker;
	}
	
	
	
	public static List<Sector> createSectorList(List<Aisle>aisleList, int aislesPerSector)
	{
		
		List<Sector>sectorList = new ArrayList<Sector>();
		int i = 0;
		int sectorid = 0;
		List<Aisle> list;
		while(i<aisleList.size())
		{
			
			if(i+aislesPerSector > aisleList.size())
			{
				list = aisleList.subList(i, aisleList.size());
				
			}
			else
			{	
				list = aisleList.subList(i, i+aislesPerSector);
			}
			sectorList.add(new Sector(sectorid,list,0)); // 0 not currently cooled, define the constant somewhere
			setSectorForAisle(list,sectorid);
			sectorid++;
			
			Log.printLine("Sector #"+(sectorid)+": aisles "+i+" to "+(i+aislesPerSector-1));
			Log.printLine("Sector #"+(sectorid)+" size is: "+list.size());
			i = i+aislesPerSector;
			
		}
		return sectorList;
	}
	
	public static List<Aisle> createAisleList(List<Rack>rackList, int racksPerAisle)
	{
		
		List<Aisle>aisleList = new ArrayList<Aisle>();
		int i = 0;
		int aisleid = 0;
		List<Rack> list;
		while(i<rackList.size())
		{
			
			if(i+racksPerAisle > rackList.size())
			{
				list = rackList.subList(i, rackList.size());
				
			}
			else
			{	
				list = rackList.subList(i, i+racksPerAisle);
			}
			aisleList.add(new Aisle(aisleid,list,0)); // 0 not currently cooled, define the constant somewhere
			setAisleForRack(list,aisleid);
			aisleid++;
			
			Log.printLine("Aisle #"+(aisleid)+": racks "+i+" to "+(i+racksPerAisle-1));
			Log.printLine("Aisle #"+(aisleid)+" size is: "+list.size());
			i = i+racksPerAisle;
			
		}
		return aisleList;
	}
	
	public static List<Rack> createRackList(List<EnhancedHost>hostList, int hostsPerRack)
	{
		
		List<Rack>rackList = new ArrayList<Rack>();
		int i = 0;
		int rackid = 0;
		List<EnhancedHost> list;
		while(i<hostList.size())
		{
			
			if(i+hostsPerRack > hostList.size())
			{
				list = hostList.subList(i, hostList.size());
				
			}
			else
			{	
				list = hostList.subList(i, i+hostsPerRack);
			}
			
			rackList.add(new Rack(rackid,list,0)); // 0 not currently cooled, define the constant somewhere
			
			setRackForHost(list,rackid);
			
			
			rackid++;
			
			Log.printLine("Rack #"+(rackid)+": hosts "+i+" to "+(i+hostsPerRack-1));
			Log.printLine("Rack #"+(rackid)+" size is: "+list.size());
			i = i+hostsPerRack;
			
		}
		return rackList;
	}
	
	
	public static List<EnhancedHost> createEnhancedHostList(int hostsNumber) {
		List<EnhancedHost> hostList = new ArrayList<EnhancedHost>();
		
		for (int i = 0; i < hostsNumber; i++) {
			int hostType = i % Constants.HOST_TYPES;
			
			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
			}
			
			
			
			hostList.add(new EnhancedHost(
					i,
					new RamProvisionerSimple(Constants.HOST_RAM[hostType]),
					new BwProvisionerSimple(Constants.HOST_BW),
					Constants.HOST_STORAGE,
					peList,
					new VmSchedulerTimeSharedOverSubscription(peList),
					Constants.HOST_POWER[hostType]));
		}
		return hostList;
	}
	
	
	//////////////////////////// Create OBJECTS METHODS //////////////////////
	
	public static EnhancedPowerDatacenter createEnhancedDatacenter(
			String name,
			//Class<EnhancedPowerDatacenter> class1,
			List<Sector> sectorList,
			List<Aisle> aisleList,
			List<Rack> rackList,
			List<EnhancedHost> hostList,
			VmAllocationPolicy vmAllocationPolicy) throws Exception 
	{
		
		Log.printLine(name + " trying to be created");
		
		//Setup the characterstics
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource

		
		// I think all the other lists need to be passed as characteristics
		
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch,
				os,
				vmm,
				hostList,
				rackList,
				aisleList,
				sectorList,
				time_zone,
				cost,
				costPerMem,
				costPerStorage,
				costPerBw);
		
		
		EnhancedPowerDatacenter datacenter = null;
		
		try {
			datacenter = new EnhancedPowerDatacenter(
					name,
					characteristics,
					vmAllocationPolicy,
					new LinkedList<Storage>(),
					Constants.SCHEDULING_INTERVAL
					);
			
			datacenter.setSectorLeft(sectorList.size()-1);
			datacenter.setAisleLeft(aisleList.size()-1);
			datacenter.setRackLeft(rackList.size()-1);
			
			FzonesVmAllocationPolicy vmallocpol = (FzonesVmAllocationPolicy) datacenter.getVmAllocationPolicy();
			vmallocpol.setDcId(datacenter.getId());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.exit(0);
		}

		return datacenter;
	}
	
	
	public static void setRackForHost(List <EnhancedHost> list, int rackid)
	{
		for(EnhancedHost host : list)
		{
			host.setRack(rackid);
		}
	}
	
	public static void setAisleForRack(List <Rack> list, int aisleid)
	{
		for(Rack rack : list)
		{
			rack.setAisle(aisleid);
		}
	}
	
	public static void setSectorForAisle(List <Aisle> list, int sectorid)
	{
		for(Aisle aisle : list)
		{
			aisle.setSector(sectorid);
		}
	}

}