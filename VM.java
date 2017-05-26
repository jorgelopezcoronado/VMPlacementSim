public class VM extends MultiDimensionalIntObject
{

	public VM()
	{
		super();
	}
	
	public VM(int p)
	{
		super(p);
	}

	public VM(int... values)
	{
		super(values);
	}
/*
	public static void main(String[] args)
	{
		VM vm1 = new VM(3);
		vm1.setValues(1,2);
		vm1.setValues(1,2666, 3);
		VM vm2 = new VM(3);
		vm2.setValues(1,2);
		vm2.setValues(1,666, 3);

		System.out.println(vm1+"="+vm2+"? "+vm1.equals(vm2));

		vm2.setValues(1,2666, 3);

		System.out.println(vm1+"="+vm2+"? "+vm1.equals(vm2));
	}
*/
}

