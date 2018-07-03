package org.cloudbus.cloudsim.examples.power.planetlab;

import org.cloudbus.cloudsim.examples.power.planetlab.EnhancedRunner;

public class RandomBiasedSamplingExample
{
	public static void main(String[] args)
	{
		try {
		String input = new String("C:\\Users\\Admin\\Documents\\cloudsim-3.0.3\\examples\\workload\\LANL\\LANL-CM5-short.swf");
		new EnhancedRunner(input,10,"output",1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}