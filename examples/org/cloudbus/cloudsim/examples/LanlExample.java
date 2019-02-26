package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.examples.power.EnhancedRunner;

public class LanlExample
{
	public static void main(String[] args)
	{
		try {
		
		new EnhancedRunner("/home/manish/CloudSimExtension/examples/workload/LANL/LANL-CM5-short.swf","output","/home/manish/CloudSimExtension/examples/workload/planetlab/test/");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}