package vmplacementsim;

import java.util.LinkedList;

public abstract class MultiDimensionalIntObjectCollection <T extends MultiDimensionalIntObject> extends LinkedList 
{
	protected boolean allowRepeated;
	protected static int counter = 0;
	
	public MultiDimensionalIntObjectCollection()
	{
		super();
		//default init of boolean false, that is why allow repeated ommited
	}

	public MultiDimensionalIntObjectCollection(boolean allowRepeated)
	{
		super();
		this.allowRepeated = allowRepeated;
	}
	
	public void add(T element)
	{
		if(!(element instanceof MultiDimensionalIntObject))
		{
			System.out.println("Error, adding incorrect object type to MultiDimensionalIntObjectCollection!");
			return;
		}

		MultiDimensionalIntObject mo = (MultiDimensionalIntObject)element;

		if(this.size() > 0)
		{
			Object first = this.getFirst();
			if(!(first instanceof MultiDimensionalIntObject))
				return;
			MultiDimensionalIntObject f = (MultiDimensionalIntObject)first;
			if(this.size() > 0 && mo.vector.length != f.vector.length)
			{
				System.out.println("Error, adding an element to a multi-dimensional object collection with different sizes!");
			}
		}

		if(!this.allowRepeated && super.indexOf(mo) != -1) //we can't repeat and there is an existing equal element
		{
			System.out.println("Error, adding an existing element to a multi-dimensional object collection, which does not allow!");
			return;
		}
		
		mo.setName("type"+(++this.counter));
		super.add(mo);	
	}

	public String toString()
	{
		String stringRep = "(";
		for(int i = 0; i < this.size() - 1; i++)
			stringRep += this.get(i)+", ";
	
		return stringRep + this.get(this.size() - 1)+")";
	}

}

