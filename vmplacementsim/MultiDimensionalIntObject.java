package vmplacementsim;

public abstract class MultiDimensionalIntObject
{
	public int dimensions = 1;
	protected int vector[];
	protected String name = "";
	
	public MultiDimensionalIntObject()
	{
		vector = new int[this.dimensions];
	}
	
	public MultiDimensionalIntObject(int dimensions)
	{
		this.dimensions = dimensions;
		vector = new int[this.dimensions];
	}

	public MultiDimensionalIntObject(int... values)
	{	
		this(values.length);	
		this.setValues(values);
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	} 
	
	public void setValues(int... values)
	{
		if (values.length != this.dimensions)
		{
			System.out.println("Error, wrong number of arguments to set to a multi-dimensaional object!");
			return;
		}
		
		int i = 0;
		for(int value : values)
			this.vector[i++] = value;
	}

	public int[] getValues()
	{
		int array[] = new int[this.dimensions];
		for(int i = 0; i < this.dimensions; i++)
			array[i] = this.vector[i]; 

		return array;
	}

	public String toString()
	{
		String stringRep = "(";
		for(int i = 0; i < this.vector.length - 1; i++)
			stringRep += this.vector[i]+", ";
	
		return stringRep + this.vector[this.vector.length - 1]+")";
	}

	public boolean equals(Object object)
	{
		if(!(object instanceof MultiDimensionalIntObject))
			return false;
		MultiDimensionalIntObject temp  = (MultiDimensionalIntObject)object;
		if(temp.vector.length != this.vector.length)
			return false;
		for(int i = 0; i < this.vector.length; i++)
			if(this.vector[i] != temp.vector[i])
				return false;
		
		return true;
	}

}
