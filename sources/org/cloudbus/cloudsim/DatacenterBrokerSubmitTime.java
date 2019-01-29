package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;


import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

public class DatacenterBrokerSubmitTime extends DatacenterBroker
{
	public DatacenterBrokerSubmitTime(String name) throws Exception
	{
		super(name);
		setCloudletList(new ArrayList<CloudletSubmitTime>());
		setCloudletSubmittedList(new ArrayList<CloudletSubmitTime>());
		setCloudletReceivedList(new ArrayList<CloudletSubmitTime>());

	
	}
	
	@Override 
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList()
	{
		return (List<T>) cloudletList;
	}
	
	
	
	@Override
	protected void submitCloudlets() 
	{
		System.out.println("Time of submit: "+CloudSim.clock());
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bound VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			//sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			send(getVmsToDatacentersMap().get(vm.getId()),((CloudletSubmitTime) cloudlet).getSubmitTime()-CloudSim.clock(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			//send(getVmsToDatacentersMap().get(vm.getId()),0, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}
}