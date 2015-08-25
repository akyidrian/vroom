package car;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.UIManager;

/**
 * Main entry point into the program.
 * 
 * @author Aydin Arik and Sam Leichter
 */
public class Main {

    public static final int SIM_TICK_MS = 20;//milliseconds between each tick.
    public static final double SIM_TICK_S = SIM_TICK_MS / 1000.0; //seconds between each tick.

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Timer timer = new Timer();

        //BlockingQueues between CruiseControl and CarDynamics.
        final BlockingQueue<ActuatorInstruction> cruiseToDyn = new LinkedBlockingQueue<ActuatorInstruction>();
        final BlockingQueue<DynamicsReadout> dynToCruise = new LinkedBlockingQueue<DynamicsReadout>();

        //BlockingQueues between GUI and CruiseControl.
        final BlockingQueue<ActuatorInstruction> GUIToCruiseActInst = new LinkedBlockingQueue<ActuatorInstruction>();
        final BlockingQueue<CCInstruction> GUIToCruiseCCInst = new LinkedBlockingQueue<CCInstruction>();

        //BlockingQueues between GUI and CarDynamics.
        final BlockingQueue<DynamicsReadout> dynToGUI = new LinkedBlockingQueue<DynamicsReadout>();

        //Creating objects.
        TimerTask dynamics = new CarDynamics(cruiseToDyn, dynToCruise, dynToGUI);
        TimerTask cruise = new CruiseControl(dynToCruise, GUIToCruiseCCInst, GUIToCruiseActInst, cruiseToDyn);

        //Giving objects a timer. This is mainly so that CarDynamics is operating 
        //in a discrete-time manner.
        timer.scheduleAtFixedRate(dynamics, 0, Main.SIM_TICK_MS);

        timer.scheduleAtFixedRate(cruise, 0, Main.SIM_TICK_MS);

        /*
         * Gui related. This code configures the 'look and feel' of the gui.
         */
        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        //Create gui object. This is what the driver 'interfaces' with.
        GUI gui = new GUI(dynToGUI,GUIToCruiseActInst, GUIToCruiseCCInst);
        
        //Begin threads.
        Thread dynThread = new Thread(dynamics);
        Thread cruiseThread = new Thread(cruise);
        dynThread.start(); 
        cruiseThread.start();         
    }
}
