package car;

import car.CarDynamics.engine;

/**
 * Class used for message passing between threads. These 'messages' will contain
 * information on the current situation of the simulated car.
 *
 * @author Aydin Arik and Sam Leichter
 */
public class DynamicsReadout {

    private double throttlePercentage;
    private double brakePercentage;
    private engine engineStatus;
    private double distance; //travelled.
    private double speed; //current vehicle speed.
    private double gradient; //hill angle (degrees) - positive=uphill, negative=downhill.
    private double windSpeed; //Wind speed m/s. Positive=same direction as car's motion.

    /**
     * Constructor to create a null message (all instance variable are zero).
     */
    public DynamicsReadout() {
        this.distance = 0;
        this.speed = 0;
        this.engineStatus = engine.OFF;
        this.throttlePercentage = 0;
        this.brakePercentage = 0;
        this.gradient = 0;
        this.windSpeed = 0;
    }

    /**
     * Constructor to initialise message.
     *
     * @param distance in metres.
     * @param speed in m/s.
     * @param engineStatus
     * @param throttlePercentage range is 0-100.
     * @param brakePercentage range is 0-100.
     * @param gradient in degrees.
     * @param windSpeed in m/s.
     */
    public DynamicsReadout(
            double distance,
            double speed,
            engine engineStatus,
            double throttlePercentage,
            double brakePercentage,
            double gradient,
            double windSpeed) {
        this.distance = distance;
        this.speed = speed;
        this.engineStatus = engineStatus;
        this.throttlePercentage = validatePercentageBounds(throttlePercentage);
        this.brakePercentage = validatePercentageBounds(brakePercentage);
        this.gradient = gradient;
        this.windSpeed = windSpeed;
    }

    /**
     *
     * @return between 0-100%.
     */
    public double getThrottleSetting() {
        return throttlePercentage;
    }

    /**
     *
     * @return between 0-100%.
     */
    public double getBrakePercentage() {
        return brakePercentage;
    }

    public engine getEngineStatus() {
        return engineStatus;
    }

    /**
     * Distance travelled by car.
     *
     * @return in m.
     */
    public double getDistanceMeters() {
        return distance;
    }

    /**
     * Distance travelled by car.
     *
     * @return in km/h
     */
    public double getDistanceKMeters() {
        return distance / 1000;
    }

    /**
     * Current speed of car.
     *
     * @return in m/s.
     */
    public double getSpeedMPS() {
        return speed;
    }

    /**
     * Current speed of car.
     *
     * @return in km/h.
     */
    public double getSpeedKPH() {
        return (speed * 3.6);
    }

    /**
     * Hill steepness.
     * 
     * @return in degrees.
     */
    public double getGradient() {
        return gradient;
    }

    /**
     * Current speed of wind gust.
     *
     * @return in m/s.
     */
    public double getWindSpeedMPS() {
        return windSpeed;
    }

    /**
     * Current speed of wind gust.
     *
     * @return in km/h.
     */
    public double getWindSpeedKPH() {
        return (windSpeed * 3.6);
    }

    /**
     * Check percentage is in a valid range. If percentage is, it will passed
     * through this method unchanged.
     *
     * @param percentage (0-100%)
     * @return new percentage (0-100%) - only different to input value if out of
     * bounds.
     */
    private static double validatePercentageBounds(double percentage) {
        if (percentage > 100) {
            percentage = 100;
        } else if (percentage < 0) {
            percentage = 0;
        }

        return percentage;
    }
}
