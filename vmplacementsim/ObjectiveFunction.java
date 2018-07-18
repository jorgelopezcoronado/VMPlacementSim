package vmplacementsim;

@FunctionalInterface
public interface ObjectiveFunction
{
	int compute(PlacementConfiguration pc);
}


