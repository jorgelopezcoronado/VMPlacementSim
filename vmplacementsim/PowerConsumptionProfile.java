package vmplacementsim;

public class PowerConsumptionProfile extends MultiDimensionalIntObjectCollection<PowerConsumption>
{
	public PowerConsumptionProfile()
	{
		super(true);
	}


	//deep cloning
	public PowerConsumptionProfile clone()
	{
		PowerConsumptionProfile clone = new PowerConsumptionProfile();
		for(int i = 0; i < this.size(); i++)
		{
			Object current = this.get(i);
			if(current instanceof PowerConsumption)
			{
				PowerConsumption curr = (PowerConsumption)current;
				clone.add(curr.clone());
			}
		}
	
		return clone;
	}
}
