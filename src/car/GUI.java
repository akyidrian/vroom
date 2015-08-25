package car;

import car.ActuatorInstruction.Instructions;
import car.CCInstruction.CCInstructions;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.Timer;

/**
 * An 'interface' with which the driver can turn the engine on and off, adjust
 * car throttle and brake, turn on and off cruise control and adjust the speed of
 * the cruise control. There is a dashboard to display current speed and
 * distance traveled. Cruise control set speed is also shown, as well as current
 * disturbances.
 *
 * @author Aydin Arik and Sam Leichter
 */
public class GUI extends javax.swing.JFrame {

    /**
     * States of the cruise controller. These are only used by this GUI.
     */
    private enum CCStates {
        ON,
        OFF,
        INACTIVE;
    }
    
    // BlockingQueues used for message passing between threads.
    private BlockingQueue<ActuatorInstruction> GUIToCruiseActInst;
    private BlockingQueue<CCInstruction> GUIToCruiseCCInst;
    private BlockingQueue<DynamicsReadout> dynToGUI;
    
    private double cruiseSpeed = 0; //currently set cruise speed.
    private double currSpeed = 0; //current vehicle speed.
    private static final int MAX_CRUISE_SPEED = 200; // Max. cruising speed is 200kph.
    private static final int CRUISE_SPEED_CHANGE = 5; //the amount the speed of cruise controller changes by with a +/- button push.
    private CCStates CCState = CCStates.OFF;
    private Timer GUIUpdateTimer;

    
    /**
     * GUI constructor. Requires BlockingQueues to be used between this thread
     * and others in the car.
     * 
     * @param dynToGUI
     * @param GUIToCruiseActInst
     * @param GUIToCruiseCCInst 
     */
    public GUI(
            BlockingQueue<DynamicsReadout> dynToGUI,
            BlockingQueue<ActuatorInstruction> GUIToCruiseActInst,
            BlockingQueue<CCInstruction> GUIToCruiseCCInst) {
        initComponents();

        // BlockingQueue initialisations.
        this.dynToGUI = dynToGUI;
        this.GUIToCruiseActInst = GUIToCruiseActInst;
        this.GUIToCruiseCCInst = GUIToCruiseCCInst;

        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
                GUIUpdateTimer = new Timer(Main.SIM_TICK_MS / 2, new ActionListener() { 
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateDynamicsReadout();
                        repaint();
                    }
                });
                GUIUpdateTimer.start();
            }
        });
    }

    /**
     * Updates all the fields on the GUI.
     */
    void updateDynamicsReadout() {
        if (dynToGUI.size() > 0) {
            try {
                DynamicsReadout readout = dynToGUI.take();
                
                // Display information.
                currSpeed = readout.getSpeedKPH();
                terrainGradient.setText(String.format("%.01f%s", readout.getGradient(), "\u00b0"));
                windSpeed.setText(String.format("%.01f %s", readout.getWindSpeedKPH(), "km/h"));
                accelPercentageField.setText(String.format("%.01f%s", readout.getThrottleSetting(), "%"));
                brakePercentageField.setText(String.format("%.01f%s", readout.getBrakePercentage(), "%"));
                distanceField.setText(String.format("%.01f %s", readout.getDistanceKMeters(), "km"));
                speedField.setText(String.format("%.01f %s", currSpeed, "km/h"));
                
            } catch (InterruptedException intEx) {
                // Do nothing. This exception will not lead to anything disastrous.
            }
        }
    }

    /**
     * Changes the state and fields associated with cruise control.
     *
     * @param State The state to change the cruise control to.
     */
    private void stateChange(CCStates State) {
        try {
            if (State == CCStates.ON) {
                // Reset sliders/ driver throttle and brake inputs. We assume 
                // the driver is doing nothing whilst the cruise control system
                //is enabled.
                brakeSlider.setValue(0);
                GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.BRAKE, 0));
                accelSlider.setValue(0);
                GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.MOTOR, 0));
                
                CCState = CCStates.ON;
                GUIToCruiseCCInst.put(new CCInstruction(CCInstructions.ACTIVATE, cruiseSpeed));
                
                // Configure gui for this situation.
                cruiseControlToggle.setSelected(true);
                cruiseSlower.setEnabled(true);
                cruiseFaster.setEnabled(true);
                cruiseStateField.setText("On");
                cruiseControlToggle.setText("Off");
                
            } else if (State == CCStates.OFF) {
                CCState = CCStates.OFF;
                GUIToCruiseCCInst.put(new CCInstruction(CCInstructions.DEACTIVATE, 0));
                
                // Configure gui for this situation.
                cruiseControlToggle.setSelected(false);
                cruiseSlower.setEnabled(false);
                cruiseFaster.setEnabled(false);
                cruiseStateField.setText("Off");
                cruiseControlToggle.setText("Set");
                
            } else if (State == CCStates.INACTIVE) {
                CCState = CCStates.INACTIVE;
                GUIToCruiseCCInst.put(new CCInstruction(CCInstructions.DEACTIVATE, 0));
                
                // Configure gui for this situation.
                cruiseControlToggle.setSelected(false);
                cruiseSlower.setEnabled(false);
                cruiseFaster.setEnabled(false);
                cruiseStateField.setText("Inactive");
                cruiseControlToggle.setText("Resume");
                                
            }
        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        driverControl = new javax.swing.JPanel();
        accelSlider = new javax.swing.JSlider();
        brakeSlider = new javax.swing.JSlider();
        accelPercentageField = new javax.swing.JTextField();
        brakePercentageField = new javax.swing.JTextField();
        accelLabel = new javax.swing.JLabel();
        brakeLabel = new javax.swing.JLabel();
        dcPanelLabel = new javax.swing.JLabel();
        ignition = new javax.swing.JToggleButton();
        disturbances = new javax.swing.JPanel();
        windSpeed = new javax.swing.JTextField();
        terrainGradient = new javax.swing.JTextField();
        weatherLabel = new javax.swing.JLabel();
        terrainLabel = new javax.swing.JLabel();
        disturbancesPanelLabel = new javax.swing.JLabel();
        cruiseControl = new javax.swing.JPanel();
        cruiseSetSpeedLabel = new javax.swing.JLabel();
        cruiseStateField = new javax.swing.JTextField();
        cruiseControlToggle = new javax.swing.JToggleButton();
        cruiseFaster = new javax.swing.JButton();
        cruiseSetSpeedField = new javax.swing.JTextField();
        cruiseSlower = new javax.swing.JButton();
        ccPanelLabel = new javax.swing.JLabel();
        dashboard = new javax.swing.JPanel();
        distanceField = new javax.swing.JTextField();
        distanceLabel = new javax.swing.JLabel();
        speedLabel = new javax.swing.JLabel();
        speedField = new javax.swing.JTextField();
        dashboardPanelLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Vroom Vroom");
        setMaximumSize(new java.awt.Dimension(365, 575));
        setMinimumSize(new java.awt.Dimension(365, 575));
        setName("Vroom Vroom"); // NOI18N
        setPreferredSize(new java.awt.Dimension(365, 575));
        setResizable(false);

        driverControl.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        driverControl.setMaximumSize(new java.awt.Dimension(130, 386));
        driverControl.setMinimumSize(new java.awt.Dimension(130, 386));

        accelSlider.setMajorTickSpacing(1);
        accelSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        accelSlider.setValue(0);
        accelSlider.setFocusable(false);
        accelSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                accelSliderMouseReleased(evt);
            }
        });

        brakeSlider.setMajorTickSpacing(1);
        brakeSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        brakeSlider.setValue(0);
        brakeSlider.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        brakeSlider.setFocusable(false);
        brakeSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                brakeSliderMouseReleased(evt);
            }
        });

        accelPercentageField.setEditable(false);
        accelPercentageField.setAutoscrolls(false);
        accelPercentageField.setFocusable(false);
        accelPercentageField.setMaximumSize(new java.awt.Dimension(40, 25));
        accelPercentageField.setMinimumSize(new java.awt.Dimension(40, 25));
        accelPercentageField.setPreferredSize(new java.awt.Dimension(40, 25));
        accelPercentageField.setRequestFocusEnabled(false);

        brakePercentageField.setEditable(false);
        brakePercentageField.setAutoscrolls(false);
        brakePercentageField.setFocusable(false);
        brakePercentageField.setMaximumSize(new java.awt.Dimension(40, 25));
        brakePercentageField.setMinimumSize(new java.awt.Dimension(40, 25));
        brakePercentageField.setPreferredSize(new java.awt.Dimension(40, 25));
        brakePercentageField.setRequestFocusEnabled(false);

        accelLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        accelLabel.setText("Throttle");

        brakeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        brakeLabel.setText("Brake");

        dcPanelLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        dcPanelLabel.setText("Driver Control");

        ignition.setText("Ignition");
        ignition.setFocusable(false);
        ignition.setMaximumSize(new java.awt.Dimension(63, 56));
        ignition.setMinimumSize(new java.awt.Dimension(63, 56));
        ignition.setPreferredSize(new java.awt.Dimension(63, 56));
        ignition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignitionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout driverControlLayout = new javax.swing.GroupLayout(driverControl);
        driverControl.setLayout(driverControlLayout);
        driverControlLayout.setHorizontalGroup(
            driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(driverControlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(driverControlLayout.createSequentialGroup()
                        .addGroup(driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(accelPercentageField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(accelSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                            .addComponent(accelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(brakeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(brakePercentageField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(brakeSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)))
                    .addComponent(dcPanelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ignition, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        driverControlLayout.setVerticalGroup(
            driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(driverControlLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(dcPanelLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(brakeLabel)
                    .addComponent(accelLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(brakeSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addComponent(accelSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(driverControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accelPercentageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brakePercentageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        disturbances.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        disturbances.setMinimumSize(new java.awt.Dimension(260, 87));

        windSpeed.setEditable(false);
        windSpeed.setAutoscrolls(false);
        windSpeed.setFocusable(false);
        windSpeed.setMaximumSize(new java.awt.Dimension(140, 25));
        windSpeed.setMinimumSize(new java.awt.Dimension(140, 25));
        windSpeed.setPreferredSize(new java.awt.Dimension(140, 25));
        windSpeed.setRequestFocusEnabled(false);

        terrainGradient.setEditable(false);
        terrainGradient.setAutoscrolls(false);
        terrainGradient.setFocusable(false);
        terrainGradient.setMaximumSize(new java.awt.Dimension(140, 25));
        terrainGradient.setMinimumSize(new java.awt.Dimension(140, 25));
        terrainGradient.setPreferredSize(new java.awt.Dimension(140, 25));
        terrainGradient.setRequestFocusEnabled(false);

        weatherLabel.setText("Wind Gusts");

        terrainLabel.setText("Terrain");

        disturbancesPanelLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        disturbancesPanelLabel.setText("Disturbances");

        javax.swing.GroupLayout disturbancesLayout = new javax.swing.GroupLayout(disturbances);
        disturbances.setLayout(disturbancesLayout);
        disturbancesLayout.setHorizontalGroup(
            disturbancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(disturbancesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(disturbancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(terrainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(disturbancesPanelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(disturbancesLayout.createSequentialGroup()
                        .addComponent(terrainGradient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(30, 30, 30)
                .addGroup(disturbancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(windSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(weatherLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        disturbancesLayout.setVerticalGroup(
            disturbancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(disturbancesLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(disturbancesPanelLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(disturbancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(terrainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(weatherLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(disturbancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(terrainGradient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(windSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        cruiseControl.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        cruiseControl.setMaximumSize(new java.awt.Dimension(173, 299));
        cruiseControl.setMinimumSize(new java.awt.Dimension(173, 299));
        cruiseControl.setPreferredSize(new java.awt.Dimension(170, 299));

        cruiseSetSpeedLabel.setText("Set Cruise Speed");

        cruiseStateField.setEditable(false);
        cruiseStateField.setText("Off");
        cruiseStateField.setAutoscrolls(false);
        cruiseStateField.setFocusable(false);
        cruiseStateField.setMaximumSize(new java.awt.Dimension(125, 25));
        cruiseStateField.setMinimumSize(new java.awt.Dimension(125, 25));
        cruiseStateField.setPreferredSize(new java.awt.Dimension(125, 25));
        cruiseStateField.setRequestFocusEnabled(false);

        cruiseControlToggle.setText("Set");
        cruiseControlToggle.setEnabled(false);
        cruiseControlToggle.setFocusable(false);
        cruiseControlToggle.setMaximumSize(new java.awt.Dimension(120, 25));
        cruiseControlToggle.setMinimumSize(new java.awt.Dimension(120, 25));
        cruiseControlToggle.setPreferredSize(new java.awt.Dimension(120, 25));
        cruiseControlToggle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                cruiseControlToggleMouseReleased(evt);
            }
        });

        cruiseFaster.setText("+");
        cruiseFaster.setEnabled(false);
        cruiseFaster.setFocusable(false);
        cruiseFaster.setMaximumSize(new java.awt.Dimension(50, 30));
        cruiseFaster.setMinimumSize(new java.awt.Dimension(50, 30));
        cruiseFaster.setPreferredSize(new java.awt.Dimension(50, 30));
        cruiseFaster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cruiseFasterActionPerformed(evt);
            }
        });

        cruiseSetSpeedField.setEditable(false);
        cruiseSetSpeedField.setText("0 km/h");
        cruiseSetSpeedField.setAutoscrolls(false);
        cruiseSetSpeedField.setFocusable(false);
        cruiseSetSpeedField.setMaximumSize(new java.awt.Dimension(125, 25));
        cruiseSetSpeedField.setMinimumSize(new java.awt.Dimension(125, 25));
        cruiseSetSpeedField.setPreferredSize(new java.awt.Dimension(125, 25));
        cruiseSetSpeedField.setRequestFocusEnabled(false);

        cruiseSlower.setText("-");
        cruiseSlower.setEnabled(false);
        cruiseSlower.setFocusable(false);
        cruiseSlower.setMaximumSize(new java.awt.Dimension(50, 30));
        cruiseSlower.setMinimumSize(new java.awt.Dimension(50, 30));
        cruiseSlower.setPreferredSize(new java.awt.Dimension(50, 30));
        cruiseSlower.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cruiseSlowerActionPerformed(evt);
            }
        });

        ccPanelLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        ccPanelLabel.setText("Cruise Control");

        javax.swing.GroupLayout cruiseControlLayout = new javax.swing.GroupLayout(cruiseControl);
        cruiseControl.setLayout(cruiseControlLayout);
        cruiseControlLayout.setHorizontalGroup(
            cruiseControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cruiseControlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cruiseControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cruiseControlLayout.createSequentialGroup()
                        .addComponent(ccPanelLabel)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cruiseControlLayout.createSequentialGroup()
                        .addGroup(cruiseControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(cruiseControlLayout.createSequentialGroup()
                                .addComponent(cruiseSlower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cruiseFaster, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cruiseSetSpeedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(cruiseControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cruiseSetSpeedLabel)
                                .addGroup(cruiseControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(cruiseStateField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cruiseControlToggle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(100, 100, 100))))
        );
        cruiseControlLayout.setVerticalGroup(
            cruiseControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cruiseControlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ccPanelLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cruiseStateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cruiseControlToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(cruiseSetSpeedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cruiseSetSpeedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cruiseControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cruiseFaster, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cruiseSlower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dashboard.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        distanceField.setEditable(false);
        distanceField.setAutoscrolls(false);
        distanceField.setFocusable(false);
        distanceField.setMaximumSize(new java.awt.Dimension(125, 25));
        distanceField.setMinimumSize(new java.awt.Dimension(125, 25));
        distanceField.setPreferredSize(new java.awt.Dimension(125, 25));
        distanceField.setRequestFocusEnabled(false);

        distanceLabel.setText("Distance Covered");

        speedLabel.setText("Current Speed");

        speedField.setEditable(false);
        speedField.setAutoscrolls(false);
        speedField.setFocusable(false);
        speedField.setMaximumSize(new java.awt.Dimension(125, 25));
        speedField.setMinimumSize(new java.awt.Dimension(125, 25));
        speedField.setPreferredSize(new java.awt.Dimension(125, 25));
        speedField.setRequestFocusEnabled(false);

        dashboardPanelLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        dashboardPanelLabel.setText("Dashboard");

        javax.swing.GroupLayout dashboardLayout = new javax.swing.GroupLayout(dashboard);
        dashboard.setLayout(dashboardLayout);
        dashboardLayout.setHorizontalGroup(
            dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(distanceField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speedField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dashboardPanelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(speedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(distanceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        dashboardLayout.setVerticalGroup(
            dashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashboardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dashboardPanelLabel)
                .addGap(13, 13, 13)
                .addComponent(distanceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(distanceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speedLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dashboard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cruiseControl, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(driverControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(disturbances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(disturbances, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cruiseControl, javax.swing.GroupLayout.PREFERRED_SIZE, 232, Short.MAX_VALUE))
                    .addComponent(driverControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * The event associated with the cruise speed decrease button. Used to decrease
     * the set speed of the cruise controller.
     *
     * @param evt action event.
     */
    private void cruiseSlowerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cruiseSlowerActionPerformed
        cruiseSpeed -= CRUISE_SPEED_CHANGE; 
        cruiseSpeed = cruiseSpeed < 0 ? 0 : cruiseSpeed;

        try {
            GUIToCruiseCCInst.put(new CCInstruction(CCInstructions.SET_SPEED, cruiseSpeed));
        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }
        cruiseSetSpeedField.setText(String.format("%.01f %s", cruiseSpeed, "km/h"));
    }//GEN-LAST:event_cruiseSlowerActionPerformed
    /**
     * The event associated with the cruise speed increase button. Used to increase
     * the set speed of the cruise controller.
     *
     * @param evt action event.
     */
    private void cruiseFasterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cruiseFasterActionPerformed
        cruiseSpeed += CRUISE_SPEED_CHANGE;
        cruiseSpeed = cruiseSpeed > MAX_CRUISE_SPEED ? MAX_CRUISE_SPEED : cruiseSpeed;
        try {
            GUIToCruiseCCInst.put(new CCInstruction(CCInstructions.SET_SPEED, cruiseSpeed));
        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }
        cruiseSetSpeedField.setText(String.format("%.01f %s", cruiseSpeed, "km/h"));
    }//GEN-LAST:event_cruiseFasterActionPerformed
    /**
     * The event associated with the ignition button changes. This (dis/en)ables 
     * the motor.
     *
     * @param evt action event.
     */
    private void ignitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignitionActionPerformed
        try {
            if (ignition.isSelected()) {
                stateChange(CCStates.OFF);//Ensuring cruise controller is off.
                GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.TURN_ON_IGNITION));
                GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.BRAKE, brakeSlider.getValue()));
                GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.MOTOR, accelSlider.getValue()));
                cruiseControlToggle.setEnabled(true);
            } else {
                stateChange(CCStates.OFF);//Ensuring cruise controller is off.
                GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.TURN_OFF_IGNITION));
                GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.BRAKE, brakeSlider.getValue()));
                cruiseControlToggle.setEnabled(false);
            }

        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }
    }//GEN-LAST:event_ignitionActionPerformed

    /**
     * The mouse event associated with the cruise control set/ resume/ off 
     * toggle button.
     *
     * @param evt mouse event.
     */
    private void cruiseControlToggleMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cruiseControlToggleMouseReleased
        if (cruiseControlToggle.isSelected()) {
            if (CCState == CCStates.OFF) { // Checking previous CCState was off before updating cruise speed.
                cruiseSpeed = currSpeed;
            }
            stateChange(CCStates.ON);
            cruiseSetSpeedField.setText(String.format("%.01f %s", cruiseSpeed, "km/h"));
        } else {
            stateChange(CCStates.OFF);
        }
    }//GEN-LAST:event_cruiseControlToggleMouseReleased

    /**
     * The mouse event associated with the throttle slider.
     * 
     * @param evt slider mouse released event.
     */
    private void accelSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_accelSliderMouseReleased
        if (CCState == CCStates.ON) {
            stateChange(CCStates.INACTIVE);
        }
        try {
            GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.MOTOR, accelSlider.getValue()));
        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }
    }//GEN-LAST:event_accelSliderMouseReleased

    /**
     * The mouse event associated with the brake slider.
     * 
     * @param evt slider mouse released event.
     */
    private void brakeSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_brakeSliderMouseReleased
        if (CCState == CCStates.ON) {
            stateChange(CCStates.INACTIVE);
        }
        try {
            GUIToCruiseActInst.put(new ActuatorInstruction(Instructions.BRAKE, brakeSlider.getValue()));
        } catch (InterruptedException intEx) {
            // Do nothing. This exception will not lead to anything disastrous.
        }
    }//GEN-LAST:event_brakeSliderMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel accelLabel;
    private javax.swing.JTextField accelPercentageField;
    private javax.swing.JSlider accelSlider;
    private javax.swing.JLabel brakeLabel;
    private javax.swing.JTextField brakePercentageField;
    private javax.swing.JSlider brakeSlider;
    private javax.swing.JLabel ccPanelLabel;
    private javax.swing.JPanel cruiseControl;
    private javax.swing.JToggleButton cruiseControlToggle;
    private javax.swing.JButton cruiseFaster;
    private javax.swing.JTextField cruiseSetSpeedField;
    private javax.swing.JLabel cruiseSetSpeedLabel;
    private javax.swing.JButton cruiseSlower;
    private javax.swing.JTextField cruiseStateField;
    private javax.swing.JPanel dashboard;
    private javax.swing.JLabel dashboardPanelLabel;
    private javax.swing.JLabel dcPanelLabel;
    private javax.swing.JTextField distanceField;
    private javax.swing.JLabel distanceLabel;
    private javax.swing.JPanel disturbances;
    private javax.swing.JLabel disturbancesPanelLabel;
    private javax.swing.JPanel driverControl;
    private javax.swing.JToggleButton ignition;
    private javax.swing.JTextField speedField;
    private javax.swing.JLabel speedLabel;
    private javax.swing.JTextField terrainGradient;
    private javax.swing.JLabel terrainLabel;
    private javax.swing.JLabel weatherLabel;
    private javax.swing.JTextField windSpeed;
    // End of variables declaration//GEN-END:variables
}
