package car;

import car.ActuatorInstruction.Instructions;
import car.CCInstruction.CCInstructions;
import car.CarDynamics.engine;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * All inputs provided by driver (from GUI) are passed into here and continue on 
 * to CarDynamics uninterrupted. If cruise control and engine are on, and there 
 * are no driver 'instructions' then PID controller will generate instructions 
 * to drive the car at some defined set speed.
 *
 * @author Aydin Arik & Sam Leichter
 */
public class CruiseControl extends TimerTask implements Runnable {

    // BlockingQueues used for message passing between threads.
    private BlockingQueue<DynamicsReadout> dynToCruise;
    private BlockingQueue<CCInstruction> GUIToCruiseCCInst;
    private BlockingQueue<ActuatorInstruction> GUIToCruiseActInst;
    private BlockingQueue<ActuatorInstruction> cruiseToDyn;
    
    
    private engine engineStatus = engine.OFF;
    private boolean takeControl = false;
    
    // Variable related to PID.
    private double error = 0;
    private double prevError = 0;
    private double totError = 0; //Related to integral control of PID.
    private int totErrorCounter = 0; //Reset integral error to prevent undisirable controller action.
    private final int TOT_ERROR_MAX_COUNTS = 200;
    private double setSpeed = 0; //km/h
    
    // PID Gains
    private double Kp = 4;
    private double Ki = 4;
    private double Kd = 2;
    
    ActuatorInstruction newInstruction; //new instruction from either PID output or driver. Driver instruction is alway of highest priority.
    ActuatorInstruction actInstruction; //actuator instruction from driver.
    DynamicsReadout readout = new DynamicsReadout(); //dynamics readout for the PID controller to use to calculate error.

    /**
     * CruiseControl constructor. Requires initialisation of the message passing
     * queues.
     * 
     * @param dynToCruise A BlockingQueues.
     * @param GUIToCruiseCCInst A BlockingQueues.
     * @param GUIToCruiseActInst A BlockingQueues.
     * @param cruiseToDyn A BlockingQueues.
     */
    public CruiseControl(
            BlockingQueue<DynamicsReadout> dynToCruise,
            BlockingQueue<CCInstruction> GUIToCruiseCCInst,
            BlockingQueue<ActuatorInstruction> GUIToCruiseActInst,
            BlockingQueue<ActuatorInstruction> cruiseToDyn) {
        this.dynToCruise = dynToCruise;
        this.GUIToCruiseCCInst = GUIToCruiseCCInst;
        this.GUIToCruiseActInst = GUIToCruiseActInst;
        this.cruiseToDyn = cruiseToDyn;
    }

    /**
     * Run method for the thread.
     */
    @Override
    public void run() {
        send();
        recieve();
        generateNextInstruction();
    }

    /*
     * Parses CCInstruction recieved.
     * 
     * @param CCInstruction the instruction to execute.
     */
    private void executeCCInstruction(CCInstruction CCInstruction) {
        CCInstructions aCCInstruction = CCInstruction.getInstruction();
        if (aCCInstruction == CCInstructions.ACTIVATE) {
            setTakeControl(true);
            setSpeed = CCInstruction.getSpeedSetting();

        } else if (aCCInstruction == CCInstructions.DEACTIVATE) {
            setTakeControl(false);
            //Don't change setSpeed.

        } else if (aCCInstruction == CCInstructions.SET_SPEED) {
            //Don't change takeControl.
            setSpeed = CCInstruction.getSpeedSetting();
        }
    }

    /**
     * Putting messages in queues used for message passing between threads.
     */
    private void send() {
        
        //Send an instruction to CarDynamics.
        if (newInstruction != null) { // Attempt to send an instruction only if one exists.
            try {
                cruiseToDyn.put(newInstruction); 
            } catch (InterruptedException intEx) {
                // Do nothing. This exception will not lead to anything disastrous.
            }
        }
    }

    /**
     * Taking messages from queues used for message passing between threads.
     */
    private void recieve() {
        
        //Recieve an instruction from GUI.
        if (GUIToCruiseActInst.size() > 0) {
            try {
                actInstruction = GUIToCruiseActInst.take();
            } catch (InterruptedException intEx) {
                // Do nothing. This exception will not lead to anything disastrous.
            }
        } else { // No new instructions. Required to ensure driver inputs are passed onto CarDynamics correctly when there are new ones.
            actInstruction = null;
        }

        //Recieve an instruction from GUI.
        if (GUIToCruiseCCInst.size() > 0) {
            try {
                CCInstruction CCInstruction = GUIToCruiseCCInst.take();
                executeCCInstruction(CCInstruction);

            } catch (InterruptedException intEx) {
                // Do nothing. This exception will not lead to anything disastrous.
            }
        }

        //Recieve an instruction from CarDynamics.
        if (dynToCruise.size() > 0) {
            try {
                readout = dynToCruise.take();

                engineStatus = readout.getEngineStatus();
            } catch (InterruptedException intEx) {
                // Do nothing. This exception will not lead to anything disastrous.
            }
        }
    }

    /**
     * Figuring out what the next ActuatorInstruction should be.
     */
    private void generateNextInstruction() {
        if (ccInControl() && (engineStatus == engine.ON) && (actInstruction == null)) {
            // newInstruction = some new instruction generated by cruise control.
            // Note, there is no braking control to slow the car down.
            newInstruction = new ActuatorInstruction(Instructions.MOTOR, doPID());
        } else {
            //pass the instruction provided by the driver.
            newInstruction = actInstruction;
        }
    }

    /**
     * Get an output from the PID. This decides how the car's throttle shall be
     * adjusted to achieve a given setSpeed. 
     *
     * @return What the throttle should be adjusted to to achieve a given
     * setSpeed.
     */
    private double doPID() {
        double PIDSetSpeed;
        error = setSpeed - readout.getSpeedKPH();
        totError += error;

        //zero intergral error to prevent integral control issues.
        if (totErrorCounter > TOT_ERROR_MAX_COUNTS) {
            totError = 0;
            totErrorCounter = 0;
        } else {
            totErrorCounter++;
        }

        // PID controller.
        PIDSetSpeed = Kp * error + (Ki * totError * Main.SIM_TICK_S) + (Kd * (error - prevError) / Main.SIM_TICK_S);

        // Mapping the PID output to the throttle.
        PIDSetSpeed = PIDSetSpeed > 100 ? 100 : PIDSetSpeed;
        PIDSetSpeed = PIDSetSpeed < 0 ? 0 : PIDSetSpeed;

        return PIDSetSpeed;
    }

    private void setTakeControl(boolean value) {
        takeControl = value;
    }

    private boolean ccInControl() {
        return takeControl;
    }
}
