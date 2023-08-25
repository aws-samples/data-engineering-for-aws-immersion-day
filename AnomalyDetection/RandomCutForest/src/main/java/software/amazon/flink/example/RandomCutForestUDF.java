package software.amazon.flink.example;

import org.apache.flink.table.functions.ScalarFunction;
import com.amazon.randomcutforest.RandomCutForest;

public class RandomCutForestUDF extends ScalarFunction{
    
    public double eval(float value) throws Exception{
        
        double retVal = Double.parseDouble("0");
        retVal = calculateAnomalyScore(value); 
        if (retVal>0){
            return retVal;
        }else{
            return Double.parseDouble("0");
        }
    }
    public static void main (String[] args) throws java.lang.Exception
	{ 
        int[] values = {2000, 1000, 100, 10, 5, 1};
        for (int value : values){
            System.out.println(value + ":" + calculateAnomalyScore(value));
        }
 	}

 	private static double calculateAnomalyScore(float value){
        double score = Double.parseDouble("0");
        RandomCutForest forest = RandomCutForest.builder()
        .numberOfTrees(5)
        .sampleSize(10)
        .dimensions(1)
        .timeDecay(100000)
        .shingleSize(1)
        .build();    
        double divider = (Float.toString(value).length());
        // System.out.println(value + ":" + Float.toString(value).length());
        if (value > 1){
            for (int i=0; i<divider; i++){                  
                float[] point = new float[]{i};
                score = (forest.getAnomalyScore(point) * divider);
                forest.update(point);
            }
        }
        return score;
    }
}
