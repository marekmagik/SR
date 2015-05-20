package pl.edu.agh.smarttag.position;

import org.jscience.mathematics.vector.Float64Vector;


public class DistanceCalculator {
	
	public double computeDistance(Float64Vector source, Float64Vector dest, Float64Vector sourceDirection){
		Float64Vector movedDest = dest.minus(source);
		double dividend = movedDest.cross(sourceDirection).normValue();
		double divisor = sourceDirection.normValue();
		
		if(divisor == 0)
			throw new IllegalArgumentException("Divisor has value 0");
		
		return dividend/divisor;
	}

}
