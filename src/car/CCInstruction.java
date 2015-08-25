package car;

/**
 * Class used for message passing between threads. These 'messages' will contain
 * cruise control instruction provided by the driver/ gui.
 *
 * @author Aydin Arik and Sam Leichter
 */
public class CCInstruction {

    private CCInstructions instruction;
    private double speedSet;

    /**
     * Set of valid cruise control instructions.
     */
    public enum CCInstructions {

        ACTIVATE,
        SET_SPEED,
        DEACTIVATE;
    }

    public CCInstruction(CCInstructions instruction) {
        this.instruction = instruction;
        this.speedSet = 0;
    }

    CCInstruction(double speedSet) {
        this.instruction = CCInstructions.SET_SPEED;
        this.speedSet = speedSet;
    }

    CCInstruction(CCInstructions instruction, double speedSet) {
        this.instruction = instruction;

        this.speedSet = checkLowerBound(speedSet);
    }

    private double checkLowerBound(double value) {
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    /**
     * 
     * @return CCInstructions object.
     */
    public CCInstructions getInstruction() {
        return instruction;
    }

    /**
     * 
     * @return The cruise control speed set by driver.
     */
    public double getSpeedSetting() {
        return speedSet;
    }
}
