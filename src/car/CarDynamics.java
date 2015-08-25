package car;

import car.ActuatorInstruction.Instructions;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * Describes the characteristics of a car with simple vehicle physics. Car is
 * assumed to move along a straight 1D line. Hills and wind gust disturbances
 * are also included.
 *
 * @author Sam Leichter and Aydin Arik
 */
public class CarDynamics extends TimerTask implements Runnable {

    /**
     * Used to describe whether the engine is on or not.
     */
    public enum engine {

        ON,
        OFF;
    }
    // BlockingQueues used for message passing.
    private BlockingQueue<ActuatorInstruction> cruiseToDyn; // From CruiseControl to CarDynamics.
    private BlockingQueue<DynamicsReadout> dynToCruise;  // From CarDynamics to CruiseControl.
    private BlockingQueue<DynamicsReadout> dynToGUI; // From CarDynamics to GUI.
    // Generates disturbances to test cruise controller.
    private Disturbances disturbances = new Disturbances();
    // Constants which describe the car and the environment it is in.
    private static final double MAX_CURRENT = 400; // Max. current (A) that can be supplied to the motor.
    private static final double MAX_BRAKE_TORQUE = 1000; // Max. torque (Nm) that can be supplied by the brakes.
    private static final double MOTOR_SPROKET = 0.065; // radius of the motor sprocket
    private static final double WHEEL_SPROKET = 0.11; // radius of the wheel sprocket
    private static final double WHEEL_RADIUS = 0.25; // radius of the rear wheel
    private static final double GRAVITY = 9.81; // gravitational force (ms^-2)
    private static final double CAR_MASS = 1406; // mass of the car (kg)
    private static final double K = 0.8; // DC motor constant.
    private static final double FLUID_DENSITY = 1.2041; //Desity (kgm^-3) of air @ 20 degrees.
    private static final double DRAG_AREA = 0.550; //m^2 - DRAG_COEFF * AREA. This is for a 1994 Porsche 911 Speedster.
    private static final double COEFF_ROLLING_FRICTION = 0.015; //dry concrete with car tires (a typical value for automotive vehicles).
    // Constantly changing variables relating to the car. 
    private double I = 0; // Current (A) provided to the motor
    private double propulsionForce; // Force (N) that drives the car forward.
    private double forceDrag = 0; // Air drag force (N).
    private double motorTorque = 0; // torque (Nm) provided by the motor
    private double wheelTorque = 0; // torque (Nm) upon the wheel
    private double brakeTorque = 0; // break Torque (Nm)
    private double distance = 0; // Distance (m) car has travelled.
    private double speed = 0; // Current speed of car (ms^-1)
    private double acceleration = 0; // Current acceleration of car (ms^-2)  
    // Driver related inputs.
    private engine engineStatus = engine.OFF;
    private double percentageThrottle = 0;
    private double percentageBrake = 0;

    /**
     * CarDynamics constructor. Message passing queues must be specified.
     *
     * @param cruiseToDyn A BlockingQueues.
     * @param dynToCruise A BlockingQueues.
     * @param dynToGUI A BlockingQueues.
     */
    public CarDynamics(BlockingQueue<ActuatorInstruction> cruiseToDyn, BlockingQueue<DynamicsReadout> dynToCruise, BlockingQueue<DynamicsReadout> dynToGUI) {
        this.cruiseToDyn = cruiseToDyn;
        this.dynToCruise = dynToCruise;
        this.dynToGUI = dynToGUI;
    }

    /**
     * Putting messages in queues used for message passing between threads.
     */
    private void send() {

        // Create new readout of the current situation of the car/ environment.
        DynamicsReadout readout = new DynamicsReadout(
                distance,
                speed,
                engineStatus,
                percentageThrottle,
                percentageBrake,
                disturbances.getHillDisturbance(),
                disturbances.getWindDisturbance());

        // Send to CruiseControl.
        try {
            dynToCruise.put(readout);
        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }

        // Send to GUI.
        try {
            dynToGUI.put(readout);
        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }
    }

    /**
     * Taking messages from queues used for message passing between threads.
     */
    private void recieve() {

        //Recieve an instruction from CruiseControl.
        if (cruiseToDyn.size() > 0) {
            try {
                ActuatorInstruction instruction = cruiseToDyn.take();
                executeInstruction(instruction);
            } catch (InterruptedException intEx) {
                // Do nothing. This exception will not lead to anything disastrous.
            }
        }

    }

    /**
     * Do calculations necessary to determine the current situation of the car.
     * Calculations include determining the speed, distance traveled,
     * acceleration, forces and internal torques in the car. These calculations
     * must be done regardless of what state the car is in (ie whether engine is
     * on or off).
     */
    private void simulate() {
        // Generate new disturbances.
        disturbances.runDisturbances(speed);
        double windDisturbance = disturbances.getWindDisturbance();
        double angleDisturbance = disturbances.getHillDisturbance();

        double accelerationNew;

        // Relative air speed to car. Using previous speed as an approximation for this.
        double relativeAirSpeed = -speed + windDisturbance;
        forceDrag = (FLUID_DENSITY * Math.pow(relativeAirSpeed, 2) * DRAG_AREA) / 2;

        //
        // Calculating torques and forces in and on the car.
        //
        // Torque provided by the motor
        motorTorque = I * K;

        // Torque acting upon the wheel; forces are along chain.
        wheelTorque = motorTorque * (WHEEL_SPROKET / MOTOR_SPROKET);

        // Force driving the car forward.
        propulsionForce = (wheelTorque - brakeTorque) / WHEEL_RADIUS;

        //Subtracting force generated due to a slope.
        propulsionForce = propulsionForce - slopeGeneratedForce(angleDisturbance);

        // Taking into account the affects of rolling resistance from wheels.
        // This is dependant on hills/ slopes.
        propulsionForce = propulsionForce - rollingResistance(angleDisturbance);

        // The new acceleration. If relativeAirSpeed is negative, then this means
        // the air is moving against the car. If zero of postive the the air is
        // helping drive the car forward.
        accelerationNew = relativeAirSpeed <= 0 ? ((propulsionForce - forceDrag) / CAR_MASS) : ((propulsionForce + forceDrag) / CAR_MASS);

        // Calculating new car speed.
        speed += ((acceleration + accelerationNew) * Main.SIM_TICK_S) / 2;
        speed = (speed < 0) ? 0 : speed; //special case where breaking causes the car to stop and not go backwards.            

        // Calculating distance covered.
        distance += speed * Main.SIM_TICK_S;

        // If the speed is found to be zero or negative , then we are to assume 
        // the car is doing nothing. This condition stops the car from reversing 
        // (hand brake activated).
//        if (speed <= 0) {
//            speed = 0;
//            propulsionForce = 0;
//            accelerationNew = 0;
//            wheelTorque = 0;
//        }
        
        acceleration = accelerationNew;
    }

    /**
     * Run method for the thread.
     */
    @Override
    public void run() {
        send();
        recieve();
        simulate();
    }

    /**
     * Processes instructions.
     *
     * @param instruction recieved.
     */
    private void executeInstruction(ActuatorInstruction instruction) {
        if (instruction.getInstruction() == Instructions.MOTOR) {

            //Throttle can only be applied if engine is on.
            if (engineStatus == engine.ON) {
                setMotorPercentage(instruction.getFractionalPercentage());
                percentageThrottle = instruction.getPercentage();
            }

        } else if (instruction.getInstruction() == Instructions.BRAKE) {
            setBrakePercentage(instruction.getFractionalPercentage());
            percentageBrake = instruction.getPercentage();

        } else if (instruction.getInstruction() == Instructions.TURN_OFF_IGNITION) {
            setMotorPercentage(0);
            percentageThrottle = 0;
            engineStatus = engine.OFF;

        } else if (instruction.getInstruction() == Instructions.TURN_ON_IGNITION) {
            //Assume when turning engine on, zero throttle results.
            setMotorPercentage(0);
            percentageThrottle = 0;

            engineStatus = engine.ON;
        }
    }

    /**
     * Calculates the roll resistance acting on the car.
     *
     * http://www.sciencelearn.org.nz/Science-Stories/Cycling-Aerodynamics/Rolling-resistance
     * http://www.engineeringtoolbox.com/rolling-friction-resistance-d_1303.html
     *
     * @return The roll resistance due to the tires of the car.
     */
    private double rollingResistance(double angle) {
        return (COEFF_ROLLING_FRICTION * normalForce(angle));
    }

    /**
     * Applying throttle to increase motor current.
     *
     * @param percentage Fractional percentage.
     */
    private void setMotorPercentage(double percentage) {
        I = MAX_CURRENT * percentage;
    }

    /**
     * Applying brake to slow down the car.
     *
     * @param percentage Fractional percentage.
     */
    private void setBrakePercentage(double percentage) {
        brakeTorque = MAX_BRAKE_TORQUE * percentage;
    }

    /**
     * Normal force of the car.
     *
     * @return Force is perpendicular to car.
     */
    private double normalForce(double angle) {
        return (CAR_MASS * GRAVITY * Math.cos(angle));
    }

    /**
     * Force due to a slope.
     *
     * @return Force is parallel to road.
     */
    private double slopeGeneratedForce(double angle) {
        return (CAR_MASS * GRAVITY * Math.sin(Math.toRadians(angle)));
    }
}
