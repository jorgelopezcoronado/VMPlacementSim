public class PlacementConfiguration
{
	private int[][] pc;
	private final  VMConfiguration VC;
	private final CloudInfrastructure CI;
	private final PowerConsumptionProfile CP;
	
	public PlacementConfiguration(CloudInfrastructure CI, VMConfiguration VC) throws IllegalArgumentException
	{
		if(CI.size() <= 0 || VC.size() <= 0)
		{
			System.out.println("Error, wrong arguments for placement configuration size, 0 not allowed!");
			throw new IllegalArgumentException();
		}
		
		if(((Host)CI.getFirst()).vector.length != ((VM)VC.getFirst()).vector.length)
		{
			System.out.println("Error, CI and VC have different dimension size not allowed!");
			throw new IllegalArgumentException();
		}

		this.CI = CI;
		this.VC = VC;
	
		/*create default power consumption profile.*/
		PowerConsumptionProfile pcp = new PowerConsumptionProfile();
		int dimensions = ((Host)CI.element()).dimensions + 1;
		for(int i = 0; i < CI.size(); i++)
			pcp.add(new PowerConsumption(dimensions));
		this.CP = pcp;

		this.pc = new int[CI.size()][VC.size()];
	}

	public PlacementConfiguration(CloudInfrastructure CI, VMConfiguration VC, PowerConsumptionProfile CP) throws IllegalArgumentException
	{
		if(CI.size() <= 0 || VC.size() <= 0 || CP.size() <= 0)
		{
			System.out.println("Error, wrong arguments for placement configuration size, 0 not allowed!");
			throw new IllegalArgumentException();
		}
		
		if(((Host)CI.getFirst()).vector.length != ((VM)VC.getFirst()).vector.length)
		{
			System.out.println("Error, CI and VC have different dimension size not allowed!");
			throw new IllegalArgumentException();
		}
		
		if(((Host)CI.getFirst()).vector.length + 1 != ((PowerConsumption)CP.getFirst()).vector.length || CI.size() != CP.size())
		{
			System.out.println("Error, CI and CP have different dimension size or size, not allowed!");
			throw new IllegalArgumentException();
		}

		this.CI = CI;
		this.VC = VC;
	
		this.pc = new int[CI.size()][VC.size()];

		this.CP = CP;
	}
	
	public VMConfiguration getVMConfiguration()
	{
		return this.VC;
	}
		
	public CloudInfrastructure getCloudInfrastructure()
	{
		return this.CI;
	}

	public PowerConsumptionProfile getPowerConsumptionProfile()
	{
		return this.CP;
	}

	public int[][] getMatrix()
	{
		return this.pc;
	}

	public void setValAtIndex(int value, int i, int j) throws IllegalArgumentException
	{
		if(i < 0 || j < 0 || value < 0)//yeah, only positive integers U {0}
		{
			System.out.println("Error, wrong arguments for placement configuration size, 0 not allowed!");
			throw new IllegalArgumentException();
		}	

		if(value > 0 )
		{
			((Host)CI.get(i)).setBooted(true);
		}

		this.pc[i][j] = value;

	}

	public void incrementAtIndex(int i, int j)
	{
		if(i < 0 || j < 0)
		{
			System.out.println("Error, wrong arguments for placement configuration size, 0 not allowed!");
			throw new IllegalArgumentException();
		}
		
		this.pc[i][j]++;
	}

	public boolean pcEquals(Object o)
	{
		if(!(o instanceof PlacementConfiguration))
			return false;
		PlacementConfiguration temp = (PlacementConfiguration)o;
	
		if(this.pc.length != temp.pc.length || this.pc[0].length != temp.pc[0].length)
			return false;

		for(int i = 0; i < this.pc.length; i++)
			for (int j = 0; j < this.pc[0].length; j++)	
				if(this.pc[i][j] != temp.pc[i][j])
					return false;
		return true;
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof PlacementConfiguration))
			return false;
		PlacementConfiguration temp = (PlacementConfiguration)o;

		return this.pcEquals(temp) &&  temp.VC.equals(this.VC) && temp.CI.equals(this.CI); //compare also environment
	}


	public String toString()
	{
		String retVal = "";
		for(int i = 0; i < this.pc.length; i++)
		{
			for(int j = 0; j < this.pc[0].length; j++)
				retVal += pc[i][j]+"\t";
			retVal += "\n";
		}
		return retVal;
	}

	//deep deep closning
	public PlacementConfiguration clone()
	{
		PlacementConfiguration clone = new PlacementConfiguration(this.CI.clone(), this.VC.clone(), this.CP.clone());
		for(int i = 0; i < this.pc.length; i++)
			for (int j = 0; j < this.pc[0].length; j++)	
				clone.pc[i][j] = this.pc[i][j];	
	
		return clone;
	}

	/*2add: matrix norm computations for new metrics / distance functions*/
	/*2add: */

/*
	public static void main(String args[])
	{
	
		CloudInfrastructure CI = new CloudInfrastructure();
		CI.add(new Host(3,2));	
		CI.add(new Host(3,3));	
		CI.add(new Host(3,2));	

		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));
		
		PlacementConfiguration pc1 = new PlacementConfiguration(CI, VC);
		PlacementConfiguration pc2 = new PlacementConfiguration(CI, VC);

		pc1.setValAtIndex(1, 0, 0);
		pc1.incrementAtIndex(1,1);

		pc2.setValAtIndex(1, 0, 0);
		System.out.println("pc1=\n"+pc1);
		System.out.println("pc2=\n"+pc2);

		System.out.println("pc1 = pc2? "+pc1.equals(pc2));

		System.out.println("addding 1 to pc2_11...");
		pc2.incrementAtIndex(1,1);
		System.out.println("pc2=\n"+pc2);

		System.out.println("pc1 = pc2? "+pc2.equals(pc1));

	}
/**/
}
