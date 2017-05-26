public class Host extends MultiDimensionalIntObject
{

	private int avail[]; 
	
	public Host()
	{
		super();
		avail = this.getValues();
	}
	
	public Host(int p)
	{
		super(p);
		avail = this.getValues();
	}

	public Host(int... values)
	{
		super(values);
		avail = this.getValues();
	}
	
	public void setValues(int... values)
	{
		super.setValues(values);
		avail = this.getValues();
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
			System.out.println("Unable to allocate VM "+vm.name+vm+" into host"+this.name+this+", not enough available space "+this.avalable()+".");
			return;
		}

		for(int i = 0; i < vm.dimensions; i++)
			this.avail[i] -= vm.vector[i];

	}
	
}

