package vmplacementsim;

public class CloudInfrastructure extends MultiDimensionalIntObjectCollection<Host>
{
	public CloudInfrastructure()
	{
		super(true);
	}
	
	//deep cloning
	public CloudInfrastructure clone()
	{
		CloudInfrastructure clone = new CloudInfrastructure();
		for(int i = 0; i < this.size(); i++)
		{
			Object current = this.get(i);
			if(current instanceof Host)
			{
				Host curr = (Host)current;
				clone.add(curr.clone());
			}
		}
	
		return clone;
	}
}
