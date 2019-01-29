
package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Aisle;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Rack;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Summary;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

//Power aware datacenter that implements granularity in cloudsim

public class EnhancedPowerDatacenter extends PowerDatacenter
{
	
	
	protected List<Sector> sectorList;
	
	public int sectorleft;
	public  int aisleleft;
	public  int rackleft;
	
	
	public EnhancedPowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception
	{
		super(  name,
				characteristics,
				 vmAllocationPolicy,
				 storageList,
				 schedulingInterval);
		
		
		
		for (Rack rack : getCharacteristics().getRackList()) {
			rack.setDatacenter(this);
		}
		
		
		
		for (Aisle aisle : getCharacteristics().getAisleList()) {
			aisle.setDatacenter(this);
		}
		
		for (Sector sector : getCharacteristics().getSectorList()) {
			sector.setDatacenter(this);
		}
	}
	

	
	@Override
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;
		
		
		if(currentTime==300.1)Summary.printLine("STARTING SIMULATION ");
		
		
		Log.printLine("\n\n--------------------------------------------------------------\n\n");
		Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);

		for (PowerHost host : this.<PowerHost> getHostList()) {
			Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			
			if (time < minTime) 
			{
				minTime = time;
			}

			Log.formatLine(
					"%.2f: [Host #%d] utilization is %.2f%%",
					currentTime,
					host.getId(),
					host.getUtilizationOfCpu() * 100);
		}

		if (timeDiff > 0) {
			//Log.formatLine(
			//		"\nEnergy consumption for the last time frame from %.2f to %.2f:",
			//		getLastProcessTime(),
			//		currentTime);

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
						previousUtilizationOfCpu,
						utilizationOfCpu,
						timeDiff);
				
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				//Log.printLine();
				//Log.formatLine(
				//		"%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
				//		currentTime,
				//		host.getId(),
				//		getLastProcessTime(),
				//		previousUtilizationOfCpu * 100,
				//		utilizationOfCpu * 100);
				//Log.formatLine(
				//	"%.2f: [Host #%d] energy is %.2f W*sec",
				//		currentTime,
				//		host.getId(),
				//		timeFrameHostEnergy);
			}

			Log.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
			
			Summary.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
			// power used for cooling 
			//timeFrameDatacenterEnergy += getCoolingEnergy(timeDiff);
		}
		
		
		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}

		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}
	
	/*public double getCoolingEnergy(double timeDiff)
	{
		double coolingEnergy = 0.0;
		// iterate over sector list
		for(Sector sec : this.sectorList)
		{
			Log.printLine("Sector #"+sec.getSectorId() +" Cooling Status: "+sec.getCoolingStatus());
			double sectorCooling = 0.0;
			boolean coolingNeeded = false;
			for(EnhancedHost host : sec.getSectorHostList())
			{
				if(host.getUtilizationOfCpu() > 0.0)
				{
					coolingNeeded = true;
				}
				// not directly multiplying because hosts may be non homogeneous
				sectorCooling += host.getEnergyLinearInterpolation(
						1,
						1,
						timeDiff); // max utilization
			
			}
			// 33% percent of total power used is needed for cooling		
			if(coolingNeeded == true)
			{
				coolingEnergy += 0.33 * sectorCooling;
				sec.setCoolingStatus(1);
			}
			// if idling, hosts use upto 65% of peak power, may not be consistent with cloudsim assumptions
			else
			{
				sec.setCoolingStatus(0); // no longer fully cooled -- used in allocation policies
				coolingEnergy += 0.33 * 0.65 * sectorCooling;
			}
			// Assumptions: http://www.ic.unicamp.br/~bit/mo809/seminarios/Joaquim-Datacenters/suporte/The%20cost%20of%20a%20cloud-%20research%20problems%20in%20data%20center%20networks%20.pdf
			
		}
		
		Log.printLine("Total cooling cost is: "+coolingEnergy);
		return coolingEnergy;
	}*/
	
	public void setSectorLeft(int sec)
	{
		sectorleft=sec;
	}
	
	public  int getSectorLeft()
	{
		return sectorleft;
	}
	
	public  void setAisleLeft(int ai)
	{
		aisleleft=ai;
	}
	
	public  int getAisleLeft()
	{
		return aisleleft;
	}
	
	public  void setRackLeft(int rack)
	{
		rackleft=rack;
	}
	
	public int getRackLeft()
	{
		return rackleft;
	}
	
	
}