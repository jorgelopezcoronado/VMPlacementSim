public class Host extends MultiDimensionalIntObject
{

	private int avail[]; 
	
	public Host()
	{
		super();
		avail = new int[this.dimensions];
		this.setAvail(this.getValues());
	}
	
	public Host(int p)
	{
		super(p);
		avail = new int[this.dimensions];
		this.setAvail(this.getValues());
	}

	public Host(int... values)
	{
		super(values);
		avail = new int[this.dimensions];
		this.setAvail(this.getValues());
	}
	
	public void setValues(int... values)
	{
		super.setValues(values);
		avail = new int[this.dimensions];
		this.setAvail(this.getValues());
	}

	public void setAvail(int... values)throws IllegalArgumentException
	{
		if(this.avail.length != values.length)
			throw new IllegalArgumentException("wrong number of dimensinons for available space");
		
		for(int i = 0 ; i < this.avail.length; i++)
			this.avail[i] = values[i];
	}

	public int[] getAvail()
	{
		return this.avail;
	}

	public boolean canAllocate(VM vm)throws IllegalArgumentException
	{
		if(vm.dimensions != this.dimensions)	
			throw new IllegalArgumentException("allocating a VM with different physical parameters than host!");
	
		for(int i = 0; i < vm.dimensions; i++)
			if(this.avail[i] < vm.vector[i])
				return false;
		return true;
	}

	private String available()
	{
		String stringRep = "(";
		for(int i = 0; i < this.avail.length - 1; i++)
			stringRep += this.avail[i]+", ";
	
		return stringRep + this.avail[this.avail.length - 1]+")";
	}

	public void allocate(VM vm)
	{
		if(!this.canAllocate(vm))
		{
			System.out.println("Unable to allocate VM "+vm.name+vm+" into host"+this.name+this+", not enough available space "+this.available()+".");
			return;
		}

		for(int i = 0; i < vm.dimensions; i++)
			this.avail[i] -= vm.vector[i];

	}
		
	public String toString()
	{
		String temp = super.toString();
		temp += ":(";
		for(int i = 0; i < this.avail.length - 1; i++)
			temp += this.avail[i]+", ";
	
		return temp + this.avail[this.avail.length - 1]+")";
	}

	public Host clone()
	{
		Host clone = new Host(this.getValues());
		clone.setName(this.name);
		clone.setAvail(this.getAvail());
		return clone;
	}
	
}

