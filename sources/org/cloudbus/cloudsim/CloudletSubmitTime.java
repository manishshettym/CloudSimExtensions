package org.cloudbus.cloudsim;
import java.util.LinkedList;

public class CloudletSubmitTime extends Cloudlet
{
	/** The time at which this Cloudlet is received at the Broker **/
	private double submitTime = 0.1; 
	
	public double getSubmitTime()
	{
		return this.submitTime;
	}
	
	
	public CloudletSubmitTime(
			final int cloudletId,
			final long cloudletLength,
			final int pesNumber,
			final long cloudletFileSize,
			final long cloudletOutputSize,
			final UtilizationModel utilizationModelCpu,
			final UtilizationModel utilizationModelRam,
			final UtilizationModel utilizationModelBw, double submitTime)
	{
		super(
				cloudletId,
				cloudletLength,
				pesNumber,
				cloudletFileSize,
				cloudletOutputSize,
				utilizationModelCpu,
				utilizationModelRam,
				utilizationModelBw,
				false);
		
		vmId = -1;
		accumulatedBwCost = 0.0;
		costPerBw = 0.0;
		this.submitTime = submitTime;
		requiredFiles = new LinkedList<String>();
	}
}