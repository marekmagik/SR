package smarttag;

import static org.junit.Assert.assertEquals;

import org.jscience.mathematics.vector.Float64Vector;
import org.junit.Before;
import org.junit.Test;

import pl.edu.agh.smarttag.position.DistanceCalculator;

public class DistanceCalculatorTest {

	DistanceCalculator distanceCalculator;
	
	@Before
	public void setup(){
		distanceCalculator = new DistanceCalculator();
	}
	
	@Test
	public void testBasicOperations() {
		Float64Vector source = Float64Vector.valueOf(1,0,-1);
		Float64Vector dest = Float64Vector.valueOf(1,2,1);
		Float64Vector sourceDirection = Float64Vector.valueOf(1,1,2);
		double result = distanceCalculator.computeDistance(source, dest, sourceDirection);
		assertEquals(Math.sqrt(2),result,0.000001);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidVector(){
		Float64Vector source = Float64Vector.valueOf(1,0,-1);
		Float64Vector dest = Float64Vector.valueOf(1,2,1);
		Float64Vector sourceDirection = Float64Vector.valueOf(0,0,0);
		distanceCalculator.computeDistance(source, dest, sourceDirection);
	}
	
	@Test
	public void testBasicOperations2(){
		Float64Vector source = Float64Vector.valueOf(0,0,0);
		Float64Vector dest = Float64Vector.valueOf(0,0,1);
		Float64Vector sourceDirection = Float64Vector.valueOf(0,0,1);
		double result = distanceCalculator.computeDistance(source, dest, sourceDirection);
		assertEquals(0.0,result,0.0);
	}
	
	@Test
	public void testBasicOperations3(){
		Float64Vector source = Float64Vector.valueOf(0,0,0);
		Float64Vector dest = Float64Vector.valueOf(0,1,1);
		Float64Vector sourceDirection = Float64Vector.valueOf(0,0,1);
		double result = distanceCalculator.computeDistance(source, dest, sourceDirection);
		assertEquals(1,result,0.0);
	}
	
}
