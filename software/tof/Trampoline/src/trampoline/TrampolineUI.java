/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;

/**
 *
 * @author Andreas
 */
public class TrampolineUI extends javax.swing.JFrame {
    
    public final ArrayList<HardwareInterface> interfaceList = new ArrayList<HardwareInterface>();
    public final ArrayList<String> stringList;
    public HardwareInterface selectedHardware;
    javax.swing.Timer beamStatusTimer;
    javax.swing.Timer pageRefreshTimer;
    private int refresh;
    private int nextJumpToFill;
    private static int REFRESH_TIME = 30; // Time to keep refreshing for after GO event
    private double[] chartValues;
    private String[] chartNames;
    private Chart chartObject;
    private JLabel[][] labelArray; //labelArray[column][row]
    
    ActionListener beamstatus = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            int beamStatus[] = selectedHardware.getBeamStatus();
            if(beamStatus[0]==1){
                chkBeamStatusBox1.setSelected(true);
            } else {
                chkBeamStatusBox1.setSelected(false);
            }
            
            if(beamStatus[1]==1){
                chkBeamStatusBox2.setSelected(true);
            } else {
                chkBeamStatusBox2.setSelected(false);
            }
            
            if(beamStatus[2]==1){
                chkBeamStatusBox3.setSelected(true);
            } else {
                chkBeamStatusBox3.setSelected(false);
            }
        }
    };
    
    ActionListener updatepage = new ActionListener(){
        public void actionPerformed(ActionEvent evt){
                        
            if(refresh>0){
                if(selectedHardware.getJumps().size() >= nextJumpToFill){
                    JLabel[] labels = createLabels("N" + Integer.toString(nextJumpToFill));
                    Jump thisJump = selectedHardware.getJumps().get(nextJumpToFill-1);
                    
                    labels[0].setText(thisJump.getTof()+"");
                    labels[1].setText(thisJump.getTon()+"");
                    labels[2].setText(thisJump.getTotal()+"");
                    
                    /** UPDATE BAR GRAPH**/
                    chartValues[nextJumpToFill-1] = thisJump.getTof();

                    updateChart(chartValues, chartNames);
                    nextJumpToFill++;
                }
                refresh--;
            } else {
                pageRefreshTimer.stop();
            }         
        }
    };
    
    /**
     * Creates new form TrampolineUI
     */
    public TrampolineUI() {
        initComponents();
        
        HardwareInterface hi = new HardwareInterface();
        stringList = hi.getPorts();
        
        for (String s:stringList) {
            hi = new HardwareInterface(s);
            interfaceList.add(hi);
            selDeviceName.addItem(s);
        }
        
        selectedHardware = idToHardware(stringList.get(0));
        /*
         * if (stringList.length == 1) deviceName.visible = false;
         */
                
        beamStatusTimer = new javax.swing.Timer(1, beamstatus);
        beamStatusTimer.start();
        
        pageRefreshTimer = new javax.swing.Timer(1000, updatepage);
        
        //FIRST ONE        
        double[] values = new double[3];
        String[] names = new String[3];
        values[0] = 1;
        names[0] = "Item 1";

        values[1] = 2;
        names[1] = "Item 2";

        values[2] = 4;
        names[2] = "Item 3";
        
        chartObject = new Chart(values, names, "title");

        jPanelGraph.setLayout(new java.awt.BorderLayout());
        this.jPanelGraph.add(chartObject, BorderLayout.CENTER);
         
        chartValues = new double[10];
        chartNames  = new String[10];
        //Initially give values to avoid NullPointerExceptions
        for (int i = 0; i < 10; i++) {
            chartValues[i] = 0;
            chartNames[i]  = "Bounce "+i;
        }
        
        initComponentsNonGenerated();
    }
    
    public HardwareInterface idToHardware(String s) {
        int id = stringList.indexOf(s);
        HardwareInterface hi = interfaceList.get(id);
        return hi;
    }
    
    public String passnameToFilename(String s) {
        s = s.replaceAll("[^[a-zA-Z]]", "");
        int i = 0;
        
        do {
            File file = new File("data/data-"+s+"-"+i+".xml");
            i++;
        } while (false);
        
        return s;
    }
    
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(.\\d+)?");
    }
    
    public enum JLabelName {
        N1, N2, N3, N4, N5, N6, N7, N8, N9, N10, TOF, TON, TOTAL, ALL;
    }
    
    public void callOnClose() {
        for (HardwareInterface hi:interfaceList) {
            hi.close();
        }
        beamStatusTimer.stop();
    }
    
    /**
     * This function provides us with an array of items as necessary. 
     */
    public JLabel[] createLabels(String something) {
        
        JLabelName somethingenum = JLabelName.valueOf(something);
        int numberOfLabels;
        
        if (isNumeric(something)) {
            numberOfLabels = 3;
        } else if (something.startsWith("Type")) {
            numberOfLabels = 10;
        } else {
            numberOfLabels = 30;
        }
        JLabel[] labels = new JLabel [numberOfLabels];
        return labels;
    }
    
    public void updateChart(double[] values, String[] names, String title) {        
        chartObject.updateInfo(values, names, title);

        //jPanel4.setLayout(new java.awt.BorderLayout());
        jPanelGraph.removeAll();
        jPanelGraph.validate();
        jPanelGraph.repaint();
        this.jPanelGraph.add(chartObject, BorderLayout.CENTER);
        jPanelGraph.validate();
        jPanelGraph.repaint();
    }
    
    public void updateChart(double[] values, String[] names) {
        updateChart(values, names, "Bounce Height");
    }
    
    public void updateJumpTime(String jumpNum, Jump j) {
        JLabel[] labelList;
        
        labelList = createLabels("N"+jumpNum);
        labelList[0].setText(String.valueOf(j.getTof()));
        labelList[1].setText(String.valueOf(j.getTon()));
        labelList[2].setText(String.valueOf(j.getTotal()));
    }
    
    public void updateJumpTime(int jumpNum, Jump j) {
        updateJumpTime(String.valueOf(jumpNum), j);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        PanelToF = new javax.swing.JPanel();
        jPanelStart = new javax.swing.JPanel();
        btnGo = new javax.swing.JButton();
        txtNumberOfBounces = new javax.swing.JTextField();
        txtPassName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        selDeviceName = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        selComboBox1 = new javax.swing.JComboBox();
        chkBeamStatusBox1 = new javax.swing.JCheckBox();
        chkBeamStatusBox2 = new javax.swing.JCheckBox();
        chkBeamStatusBox3 = new javax.swing.JCheckBox();
        jPanelData = new javax.swing.JPanel();
        btnClear = new javax.swing.JButton();
        jPanelDataTable = new javax.swing.JPanel();
        jPanelEventStats = new javax.swing.JPanel();
        jPanelGraph = new javax.swing.JPanel();
        PanelStats = new javax.swing.JPanel();
        PanelAdmin = new javax.swing.JPanel();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelStart.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Start Bouncing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        btnGo.setFont(new java.awt.Font("Calibri", 1, 36)); // NOI18N
        btnGo.setText("GO!");
        btnGo.setName("");
        btnGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoActionPerformed(evt);
            }
        });

        txtNumberOfBounces.setText("10");
        txtNumberOfBounces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNumberOfBouncesActionPerformed(evt);
            }
        });

        jLabel1.setText("Number of Bounces:");

        jLabel2.setText("Name for Data:");

        selDeviceName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selDeviceNameActionPerformed(evt);
            }
        });

        jLabel3.setText("Select a ToF Device:");

        jLabel4.setText("Select a Gymnast:");

        selComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Gymnast A", "Gymnast B", "Other people" }));

        chkBeamStatusBox1.setText("Beam 1 Engaged?");

        chkBeamStatusBox2.setText("Beam 2 Engaged?");
        chkBeamStatusBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkBeamStatusBox2ActionPerformed(evt);
            }
        });

        chkBeamStatusBox3.setText("Beam 3 Engaged?");

        javax.swing.GroupLayout jPanelStartLayout = new javax.swing.GroupLayout(jPanelStart);
        jPanelStart.setLayout(jPanelStartLayout);
        jPanelStartLayout.setHorizontalGroup(
            jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStartLayout.createSequentialGroup()
                .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkBeamStatusBox1)
                    .addComponent(chkBeamStatusBox3)
                    .addGroup(jPanelStartLayout.createSequentialGroup()
                        .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkBeamStatusBox2)
                            .addGroup(jPanelStartLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selDeviceName, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPassName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanelStartLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnGo)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        jPanelStartLayout.setVerticalGroup(
            jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelStartLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(selDeviceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkBeamStatusBox1)
                .addGap(1, 1, 1)
                .addComponent(chkBeamStatusBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkBeamStatusBox3)
                .addGap(5, 5, 5)
                .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(selComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanelStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPassName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnGo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        btnClear.setFont(new java.awt.Font("Calibri", 1, 36)); // NOI18N
        btnClear.setText("Clear Information");
        btnClear.setName("");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelDataTableLayout = new javax.swing.GroupLayout(jPanelDataTable);
        jPanelDataTable.setLayout(jPanelDataTableLayout);
        jPanelDataTableLayout.setHorizontalGroup(
            jPanelDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelDataTableLayout.setVerticalGroup(
            jPanelDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 302, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelDataLayout = new javax.swing.GroupLayout(jPanelData);
        jPanelData.setLayout(jPanelDataLayout);
        jPanelDataLayout.setHorizontalGroup(
            jPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDataLayout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(jPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelDataTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(136, Short.MAX_VALUE))
        );
        jPanelDataLayout.setVerticalGroup(
            jPanelDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDataLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jPanelDataTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(btnClear)
                .addContainerGap(67, Short.MAX_VALUE))
        );

        jPanelEventStats.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Event Stats", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        javax.swing.GroupLayout jPanelEventStatsLayout = new javax.swing.GroupLayout(jPanelEventStats);
        jPanelEventStats.setLayout(jPanelEventStatsLayout);
        jPanelEventStatsLayout.setHorizontalGroup(
            jPanelEventStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 151, Short.MAX_VALUE)
        );
        jPanelEventStatsLayout.setVerticalGroup(
            jPanelEventStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        jPanelGraph.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Graph", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        javax.swing.GroupLayout jPanelGraphLayout = new javax.swing.GroupLayout(jPanelGraph);
        jPanelGraph.setLayout(jPanelGraphLayout);
        jPanelGraphLayout.setHorizontalGroup(
            jPanelGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelGraphLayout.setVerticalGroup(
            jPanelGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 189, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout PanelToFLayout = new javax.swing.GroupLayout(PanelToF);
        PanelToF.setLayout(PanelToFLayout);
        PanelToFLayout.setHorizontalGroup(
            PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelToFLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelEventStats, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PanelToFLayout.setVerticalGroup(
            PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelToFLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelToFLayout.createSequentialGroup()
                        .addComponent(jPanelEventStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanelData, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelStart, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Time of Flight", PanelToF);

        javax.swing.GroupLayout PanelStatsLayout = new javax.swing.GroupLayout(PanelStats);
        PanelStats.setLayout(PanelStatsLayout);
        PanelStatsLayout.setHorizontalGroup(
            PanelStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        PanelStatsLayout.setVerticalGroup(
            PanelStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 567, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Stats", PanelStats);

        javax.swing.GroupLayout PanelAdminLayout = new javax.swing.GroupLayout(PanelAdmin);
        PanelAdmin.setLayout(PanelAdminLayout);
        PanelAdminLayout.setHorizontalGroup(
            PanelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 995, Short.MAX_VALUE)
        );
        PanelAdminLayout.setVerticalGroup(
            PanelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 567, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Administrate", PanelAdmin);

        jMenu3.setText("File");

        jMenuItem1.setText("jMenuItem1");
        jMenu3.add(jMenuItem1);

        jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");
        jMenuBar2.add(jMenu4);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initComponentsNonGenerated() {
        labelArray = new JLabel[4][10];
        //Make Labels
        jPanelDataTable.setLayout(new GridLayout(11,4));
        jPanelDataTable.add(new JLabel("Number"));
        jPanelDataTable.add(new JLabel("ToF"));
        jPanelDataTable.add(new JLabel("ToN"));
        jPanelDataTable.add(new JLabel("Total"));
        for (int i = 0; i < 10; i++) {
            labelArray[0][i] = new JLabel("jLabelNumber"+i);
            labelArray[1][i] = new JLabel("jLabelTOF"+i);
            labelArray[2][i] = new JLabel("jLabelTON"+i);
            labelArray[3][i] = new JLabel("jLabelTotal"+i);
            for (int j = 0; j < 4; j++) {
                jPanelDataTable.add(labelArray[j][i]);
                labelArray[j][i].setText("");
            }
            labelArray[0][i].setText("Jump "+(i+1));
        }
        jPanelDataTable.repaint();
    }
    
    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        /*JLabel[] labelList;
        
        labelList = createLabels("N4");
        
        for (int i = 0; i < labelList.length; i++) {  // i indexes each element successively.
            labelList[i].setText("fdsdf");
        }
        
        System.out.println(deviceName.getSelectedItem().toString());
        
        System.out.println(deviceName.getSelectedItem().toString());
        
        for(Jump thisJump : this.selectedHardware.getJumps()){
            System.out.println(thisJump);
        }*/
        
        JLabel[] labelList;
        
        labelList = createLabels("ALL");
        for (int i = 0; i < labelList.length; i++) {
            labelList[i].setText("0.00");
        }
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoActionPerformed
        /*String bouncenum = jTextField1.getText();
        String break1    = jTextField2.getText();
        String engage    = jTextField3.getText();
        String break2    = jTextField2.getText();
        
        JLabel[] labelList;
        labelList = createLabels("N"+bouncenum);
        
        Jump j;
        j = new Jump(break1, engage, break2);
        
        updateJumpTime(bouncenum, j);
        *
        */
       
        selectedHardware.collectBounces(Integer.parseInt(txtNumberOfBounces.getText()), "data/testfile.xml", txtPassName.getText());
        refresh = REFRESH_TIME;
        nextJumpToFill = 1;
        JLabel[] labels = createLabels("ALL");
        for(JLabel label : labels){
            label.setText("");
        }
        pageRefreshTimer.start();
    }//GEN-LAST:event_btnGoActionPerformed

    private void txtNumberOfBouncesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNumberOfBouncesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNumberOfBouncesActionPerformed

    private void selDeviceNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selDeviceNameActionPerformed
        selectedHardware = idToHardware(selDeviceName.getSelectedItem().toString());
    }//GEN-LAST:event_selDeviceNameActionPerformed

    private void chkBeamStatusBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkBeamStatusBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkBeamStatusBox2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TrampolineUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TrampolineUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanelAdmin;
    private javax.swing.JPanel PanelStats;
    private javax.swing.JPanel PanelToF;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnGo;
    private javax.swing.JCheckBox chkBeamStatusBox1;
    private javax.swing.JCheckBox chkBeamStatusBox2;
    private javax.swing.JCheckBox chkBeamStatusBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanelData;
    private javax.swing.JPanel jPanelDataTable;
    private javax.swing.JPanel jPanelEventStats;
    private javax.swing.JPanel jPanelGraph;
    private javax.swing.JPanel jPanelStart;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JComboBox selComboBox1;
    private javax.swing.JComboBox selDeviceName;
    private javax.swing.JTextField txtNumberOfBounces;
    private javax.swing.JTextField txtPassName;
    // End of variables declaration//GEN-END:variables
}
