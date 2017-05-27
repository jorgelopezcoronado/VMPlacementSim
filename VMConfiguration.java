public class VMConfiguration extends MultiDimensionalIntObjectCollection<VM>
{
	public VMConfiguration()
	{
		super(false);
	}


	//deep cloning
	public VMConfiguration clone()
	{
		VMConfiguration clone = new VMConfiguration();
		for(int i = 0; i < this.size(); i++)
		{
			Object current = this.get(i);
			if(current instanceof VM)
			{
				VM curr = (VM)current;
				clone.add(curr.clone());
			}
		}
	
		return clone;
	}

/*	public static void main(String args[])
	{
		VMConfiguration VC = new VMConfiguration();
		
		VC.add(new VM(3,4,1));
		VC.add(new VM(3,4,1));
		VC.add(new VM(2,4,1));
		VC.add(new VM(1,4,1));

		System.out.println(VC);
		System.out.println(VC.allowRepeated);
	}
*/
}
