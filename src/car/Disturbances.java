package car;

import java.util.Random;

/**
 * Generate hill and wind gust disturbances to test the cruise controller.
 * 
 * @author Aydin Arik and Sam Leicher
 */
public class Disturbances {


    private double windDisturbance = 0; // In m/s.
    private double angleDisturbance = 0; // In degrees.
    
    // 144km/h - Highest recorded wind speed in chch in the last 30 days (01/08/12 - 01/09/12).
    private static final double MAX_WIND_SPEED = 40;
    
    //Approximate (assumed) standard deviation of wind gusts. 
    private static final double STD_WIND_GUST = MAX_WIND_SPEED / 3; 
    
    //Used to generate new disturbances at a reasonable rate.
    private static final int MAX_WIND_GUST_TICK = 100;
    private int windGustTick = 0;
    private static final int MAX_HILL_DISTURBANCE_TICK = 10;
    private int hillDisturbanceTick = 0;
    
    //
    //Used to restrict the amount of change in a disturbance at any one time.
    //
    private static final double MAX_HILL_ANGLE_CHANGE = 0.5; //Maximum change in hill angle possible at any one time.
    
    // Steepest incline/ decline street is 19 degrees, so we will use 6 degrees (approx a third of max.) as a typical value.
    private static final double MAX_INCLINE = 6;

    /**
     * Generates new disturbance values.
     * 
     * @param speed Required to figure out whether a new hillDisturbance should be generated.
     */
    public void runDisturbances(double speed) {
        generateWindDisturbance();

        if (speed > 0) {
            generateHillDisturbance();
        }
    }
    
    /**
     * Randomly generating wind gusts. Wind gusts are assumed to follow a normal distribution.
     */
    private void generateWindDisturbance() {
        //Generating a new wind gust disturbance value ever so often.
        if (windGustTick == MAX_WIND_GUST_TICK) {
            Random rand = new Random();
            windDisturbance = STD_WIND_GUST * rand.nextGaussian();

            // Checking wind gust bounds. There will only be a very small chance (~0.3%) 
            // that the random number generator will generate a number outside the bounds.
            windDisturbance = windDisturbance > MAX_WIND_SPEED ? MAX_WIND_SPEED : windDisturbance;
            windDisturbance = windDisturbance < -MAX_WIND_SPEED ? -MAX_WIND_SPEED : windDisturbance;

            windGustTick = 0;
        } else {
            windGustTick++;
        }
    }

    /**
     * Randomly generating hill inclines to test CruiseControl system. Inclines
     * are incrementally changed (increased/ decreased), as would be expected on
     * a real road.
     */
    private void generateHillDisturbance() {
        //Generating a new hill disturbance value ever so often.
        if (hillDisturbanceTick == MAX_HILL_DISTURBANCE_TICK) { 
            Random rand = new Random();
            double hillAngleChange = MAX_HILL_ANGLE_CHANGE * rand.nextDouble();
            
            //deciding hill change direction. True = addition to current angle, false = subtraction.
            boolean angleChangeDir = rand.nextBoolean(); 

            /*
             * Positive angles mean incline. Negative angles (declines) are
             * ignored since the cruise controller doesn't have control over
             * brakes.
             */
            if (angleChangeDir) {
                angleDisturbance += hillAngleChange;
                angleDisturbance = angleDisturbance > MAX_INCLINE ? MAX_INCLINE : angleDisturbance;
            } else {
                angleDisturbance -= hillAngleChange;
                angleDisturbance = angleDisturbance < 0 ? 0 : angleDisturbance;
            }

            hillDisturbanceTick = 0;

        } else {
            hillDisturbanceTick++;
        }
    }
    
    /**
     * Returns the wind gust speed.
     * 
     * @return in m/s.
     */
    public double getWindDisturbance() {
        return windDisturbance;
    }
    
    /**
     * Return the hills incline.
     * 
     * @return in degrees.
     */
    public double getHillDisturbance() {
        return angleDisturbance;
    }
}
