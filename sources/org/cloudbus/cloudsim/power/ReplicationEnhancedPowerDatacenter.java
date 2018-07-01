package org.cloudbus.cloudsim.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.lists.SectorList;

public class ReplicationEnhancedPowerDatacenter extends EnhancedPowerDatacenter
{
	
	protected List<Integer>failureZoneIds;
	protected Map<Integer, SectorAllocationPolicyHoneyBeeV2> failureZonePolicyMap;

	protected List<Vm> vmList;
	protected int replicationFactor = 3;
	public ReplicationEnhancedPowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval,
			List<Sector> sectorList, 
			List<Integer>failureZoneIds,
			List<Vm> vmList) throws Exception
	{
		super(name,
		characteristics,
		 vmAllocationPolicy,
		 storageList,
		 schedulingInterval,sectorList);
		
		this.vmList = vmList;
		this.failureZoneIds = failureZoneIds;
		this.failureZonePolicyMap = new HashMap<Integer,SectorAllocationPolicyHoneyBeeV2>();
		// organize sectors by their failure zone ids
		for(Integer id : failureZoneIds)
		{
			List<Sector> failureZoneSectors = new ArrayList<Sector>();
			List<EnhancedHost> failureZoneHosts = new ArrayList<EnhancedHost>();
			for(Sector sector : sectorList)
			{
				if(sector.getFailureZoneId() == id)
				{
					failureZoneSectors.add(sector);
					failureZoneHosts.addAll(sector.getSectorHostList());
				}
			}
			// add to the map
			SectorAllocationPolicyHoneyBeeV2 policy = new SectorAllocationPolicyHoneyBeeV2(failureZoneHosts,failureZoneSectors,vmList);
			failureZonePolicyMap.put(id, policy);
		}
		
	
	}
	
	
	
	@Override
	protected void processVmCreate(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();

		boolean result = true;
		for(Integer failureZoneId : this.failureZoneIds) {
			result = result && this.failureZonePolicyMap.get(failureZoneId).allocateHostForVm(vm); // allocation is succesful if vm is allocated in all
																									// failure zones
		}
		Log.printLine("created a vm");	
		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, data);
		}

		if (result) {
			getVmList().add(vm);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}
			
			for(Integer failureZoneId : this.failureZoneIds) {
					
					vm.updateVmProcessing(CloudSim.clock(),this.failureZonePolicyMap.get(failureZoneId).getHost(vm).getVmScheduler()
					.getAllocatedMipsForVm(vm));
					
					
			}
		}

	}
	

	
	
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) 
	{
		Log.printLine("Process cloudlet Submit");
		updateCloudletProcessing();
		Log.printLine("Done cloudlet processing update");
		try {
			// gets the Cloudlet object
			Cloudlet cl = (Cloudlet) ev.getData();

			// checks whether this Cloudlet has finished or not
			if (cl.isFinished()) {
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printLine(getName() + ": Warning - Cloudlet #" + cl.getCloudletId() + " owned by " + name
						+ " is already completed/finished.");
				Log.printLine("Therefore, it is not being executed again");
				Log.printLine();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					// unique tag = operation tag
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					sendNow(cl.getUserId(), tag, data);
				}

				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics()
					.getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getVmId();

			// time to transfer the files
			double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());
			int temp = this.replicationFactor;
			for(Integer failureZoneId : this.failureZoneIds) // pick failure zones with least power consumption
			{
				if(temp-- == 0) {break;}
				Host host = this.failureZonePolicyMap.get(failureZoneId).getHost(vmId, userId);
				
				Vm vm = host.getVm(vmId, userId);
				CloudletScheduler scheduler = vm.getCloudletScheduler();
				double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);
	
				// if this cloudlet is in the exec queue
				if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
					estimatedFinishTime += fileTransferTime;
					send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
				}
	
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.TRUE;
	
					// unique tag = operation tag
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					sendNow(cl.getUserId(), tag, data);
				}
			}
		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}
		setCloudletSubmitted(CloudSim.clock());

		checkCloudletCompletion();
		
	}
	
	@Override
	protected void checkCloudletCompletion() {
		//Log.printLine("Checking cloudlet completion");
		for(Integer failureZoneId : this.failureZoneIds) 
		{	
			List<? extends Host> list = this.failureZonePolicyMap.get(failureZoneId).getHostList();
			for (int i = 0; i < list.size(); i++) {
				Host host = list.get(i);
				for (Vm vm : host.getVmList()) {
					//Log.printLine("Checking compleiting of : "+vm.getId());
					while (vm.getCloudletScheduler().isFinishedCloudlets()) {
						Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
						if (cl != null) {
							//Log.printLine("A cloudlet finsihed");
							sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
						}
					}
				}
			}
		}
	}

	@Override
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		Log.printLine("\n\n--------------------------------------------------------------\n\n");
		Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);

		for (PowerHost host : this.<PowerHost> getHostList()) {
			//Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
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

			//Log.formatLine(
			//		"\n%.2f: Data center's energy is %.2f W*sec\n",
			//		currentTime,
			//		timeFrameDatacenterEnergy);
		}
		timeFrameDatacenterEnergy += getCoolingEnergy(timeDiff);
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
	@Override
	protected void updateCloudletProcessing() {
		//Log.printLine();
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			Log.printLine("here");
			return;
		}
		double currentTime = CloudSim.clock();
		Log.printLine("Update Cloudlet Processing");
		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			System.out.print(currentTime + " ");

			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
						getVmList());

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null) {
							Log.formatLine(
									"%.2f: Migration of VM #%d to Host #%d is started",
									currentTime,
									vm.getId(),
									targetHost.getId());
						} else {
							Log.formatLine(
									"%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									currentTime,
									vm.getId(),
									oldHost.getId(),
									targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration purposes, the other
						// half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(
								getId(),
								vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
								CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}

			setLastProcessTime(currentTime);
		}
	}

	


}