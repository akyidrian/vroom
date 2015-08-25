
package car;

/**
 * Class used for message passing between threads. These 'messages' will contain
 * driver input information for the car such as turning the car on and off as well
 * as braking and throttle.
 * 
 * @author Aydin Arik and Sam Leichter
 */
public class ActuatorInstruction {
    private Instructions instruction; //The instruction.
    private double percentageInput; //Percentage input for brake or throttle/ motor.
    
    /**
     * List of valid instructions.
     */
    public enum Instructions {
        BRAKE,
        MOTOR,
        TURN_ON_IGNITION,
        TURN_OFF_IGNITION;
    }
    
    /**
     * Constructor for instructions which will have percentageInput = 0.
     * 
     * @param instruction 
     */
    public ActuatorInstruction (Instructions instruction) {
        this.instruction = instruction;
        this.percentageInput = 0; 
    }
    
    /**
     * Constructor which allow you to specify percentageInput. Note, percentageInput
     * will only be used if the instruction is not a TURN_ON_IGNITION or TURN_OFF_IGNITION
     * instruction.
     * 
     * @param instruction
     * @param percentage Must be between 0-100%. Do not use fractional percentages such as 0.1 for 10% for example.
     */
    public ActuatorInstruction (Instructions instruction, double percentage) {
        this(instruction);
                
        if (instruction.equals(Instructions.MOTOR) || instruction.equals(Instructions.BRAKE)) {
            this.percentageInput = validatePercentageBounds(percentage);
        }
    }
    
    /**
     * Returns a fractional value for the percentageInput.
     * 
     * @return 
     */
    public double getFractionalPercentage () {
        return (percentageInput / 100.0);
    }
    
    
    /**
     * 
     * @return Instructions object.
     */
    public Instructions getInstruction() {
        return instruction;
    }
    
    /**
     * 
     * @return a percentage (0-100%)
     */
    public double getPercentage() {
        return percentageInput;
    }
    
    /**
     * Check percentage is in a valid range. If percentage is, it will passed 
     * through this method unchanged.
     * 
     * @param percentage (0-100%)
     * @return new percentage (0-100%) - only different to input value if out of bounds.
     */
    private static double validatePercentageBounds(double percentage) {
        if (percentage > 100) {
            percentage = 100;
        }
        else if (percentage < 0) {
            percentage = 0;
        }
        
        return percentage; 
    }
}
