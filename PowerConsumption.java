public class PowerConsumption extends MultiDimensionalIntObject
{

	public PowerConsumption()
	{
		super();
	}
	
	public PowerConsumption(int p)
	{
		super(p);
	}

	public PowerConsumption(int... values)
	{
		super(values);
	}

	public PowerConsumption clone()
	{
		PowerConsumption clone = new PowerConsumption(this.getValues());
		clone.setName(this.name);
		return clone;
	}
}

