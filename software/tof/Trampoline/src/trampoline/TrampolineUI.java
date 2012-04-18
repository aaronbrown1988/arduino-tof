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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

        pnlGraph.setLayout(new java.awt.BorderLayout());
        this.pnlGraph.add(chartObject, BorderLayout.CENTER);
         
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
        pnlGraph.removeAll();
        pnlGraph.validate();
        pnlGraph.repaint();
        this.pnlGraph.add(chartObject, BorderLayout.CENTER);
        pnlGraph.validate();
        pnlGraph.repaint();
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
        grpFiletype = new javax.swing.ButtonGroup();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        PanelToF = new javax.swing.JPanel();
        pnlStart = new javax.swing.JPanel();
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
        pnlData = new javax.swing.JPanel();
        btnClear = new javax.swing.JButton();
        jPanelDataTable = new javax.swing.JPanel();
        pnlStats = new javax.swing.JPanel();
        pnlGraph = new javax.swing.JPanel();
        PanelStats = new javax.swing.JPanel();
        PanelAdmin = new javax.swing.JPanel();
        pnlExport = new javax.swing.JPanel();
        lblUser = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        lblDoB = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        selCategory = new javax.swing.JComboBox();
        selDate = new javax.swing.JComboBox();
        selMonth = new javax.swing.JComboBox();
        selYear = new javax.swing.JComboBox();
        btnAddModifyUser = new javax.swing.JButton();
        btnDeleteUser = new javax.swing.JButton();
        btnExportUser = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        txtUserName = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        pnlJumpers = new javax.swing.JPanel();
        pnlImport = new javax.swing.JPanel();
        pnlReset = new javax.swing.JPanel();
        lblNewPassword = new javax.swing.JLabel();
        lblNewPassword2 = new javax.swing.JLabel();
        txtPassword2 = new javax.swing.JTextField();
        txtPassword1 = new javax.swing.JTextField();
        btnResetAll = new javax.swing.JButton();
        btnNewPassword = new javax.swing.JButton();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlStart.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Start Bouncing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

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

        javax.swing.GroupLayout pnlStartLayout = new javax.swing.GroupLayout(pnlStart);
        pnlStart.setLayout(pnlStartLayout);
        pnlStartLayout.setHorizontalGroup(
            pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStartLayout.createSequentialGroup()
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkBeamStatusBox1)
                    .addComponent(chkBeamStatusBox3)
                    .addGroup(pnlStartLayout.createSequentialGroup()
                        .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkBeamStatusBox2)
                            .addGroup(pnlStartLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3))))
                        .addGap(18, 18, 18)
                        .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selDeviceName, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPassName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlStartLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnGo)))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        pnlStartLayout.setVerticalGroup(
            pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlStartLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(selDeviceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkBeamStatusBox1)
                .addGap(1, 1, 1)
                .addComponent(chkBeamStatusBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkBeamStatusBox3)
                .addGap(5, 5, 5)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(selComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPassName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnGo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

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

        javax.swing.GroupLayout pnlDataLayout = new javax.swing.GroupLayout(pnlData);
        pnlData.setLayout(pnlDataLayout);
        pnlDataLayout.setHorizontalGroup(
            pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataLayout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelDataTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(136, Short.MAX_VALUE))
        );
        pnlDataLayout.setVerticalGroup(
            pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jPanelDataTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addComponent(btnClear)
                .addContainerGap(67, Short.MAX_VALUE))
        );

        pnlStats.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Event Stats", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        javax.swing.GroupLayout pnlStatsLayout = new javax.swing.GroupLayout(pnlStats);
        pnlStats.setLayout(pnlStatsLayout);
        pnlStatsLayout.setHorizontalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 151, Short.MAX_VALUE)
        );
        pnlStatsLayout.setVerticalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        pnlGraph.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Graph", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        javax.swing.GroupLayout pnlGraphLayout = new javax.swing.GroupLayout(pnlGraph);
        pnlGraph.setLayout(pnlGraphLayout);
        pnlGraphLayout.setHorizontalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlGraphLayout.setVerticalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 189, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout PanelToFLayout = new javax.swing.GroupLayout(PanelToF);
        PanelToF.setLayout(PanelToFLayout);
        PanelToFLayout.setHorizontalGroup(
            PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelToFLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlStats, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        PanelToFLayout.setVerticalGroup(
            PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelToFLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelToFLayout.createSequentialGroup()
                        .addComponent(pnlStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(pnlData, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlStart, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Time of Flight", PanelToF);

        javax.swing.GroupLayout PanelStatsLayout = new javax.swing.GroupLayout(PanelStats);
        PanelStats.setLayout(PanelStatsLayout);
        PanelStatsLayout.setHorizontalGroup(
            PanelStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 996, Short.MAX_VALUE)
        );
        PanelStatsLayout.setVerticalGroup(
            PanelStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 567, Short.MAX_VALUE)
        );

        jTabbedPane2.addTab("Stats", PanelStats);

        pnlExport.setBorder(javax.swing.BorderFactory.createTitledBorder("Export"));

        lblUser.setText("User:");

        lblName.setText("Name:");

        lblDoB.setText("DoB:");

        lblCategory.setText("Category:");

        btnAddModifyUser.setText("Add / Modify User");

        btnDeleteUser.setText("Delete User");

        btnExportUser.setText("Export User");

        grpFiletype.add(jRadioButton1);
        jRadioButton1.setText("CSV");

        grpFiletype.add(jRadioButton2);
        jRadioButton2.setText("Text");

        grpFiletype.add(jRadioButton3);
        jRadioButton3.setText("Excel");

        jTextField1.setText("jTextField1");

        javax.swing.GroupLayout pnlExportLayout = new javax.swing.GroupLayout(pnlExport);
        pnlExport.setLayout(pnlExportLayout);
        pnlExportLayout.setHorizontalGroup(
            pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExportLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCategory)
                    .addComponent(lblDoB)
                    .addComponent(lblUser)
                    .addComponent(lblName))
                .addGap(18, 18, 18)
                .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlExportLayout.createSequentialGroup()
                        .addComponent(selDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(selCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddModifyUser)
                    .addComponent(btnDeleteUser)
                    .addComponent(btnExportUser)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlExportLayout.setVerticalGroup(
            pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExportLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDoB)
                    .addComponent(selDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategory)
                    .addComponent(selCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnAddModifyUser)
                .addGap(18, 18, 18)
                .addComponent(btnDeleteUser)
                .addGap(18, 18, 18)
                .addComponent(btnExportUser)
                .addGap(18, 18, 18)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlJumpers.setBorder(javax.swing.BorderFactory.createTitledBorder("Jumpers"));

        javax.swing.GroupLayout pnlJumpersLayout = new javax.swing.GroupLayout(pnlJumpers);
        pnlJumpers.setLayout(pnlJumpersLayout);
        pnlJumpersLayout.setHorizontalGroup(
            pnlJumpersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlJumpersLayout.setVerticalGroup(
            pnlJumpersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 393, Short.MAX_VALUE)
        );

        pnlImport.setBorder(javax.swing.BorderFactory.createTitledBorder("Import"));

        javax.swing.GroupLayout pnlImportLayout = new javax.swing.GroupLayout(pnlImport);
        pnlImport.setLayout(pnlImportLayout);
        pnlImportLayout.setHorizontalGroup(
            pnlImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 346, Short.MAX_VALUE)
        );
        pnlImportLayout.setVerticalGroup(
            pnlImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        pnlReset.setBorder(javax.swing.BorderFactory.createTitledBorder("Reset"));

        lblNewPassword.setText("New Password:");

        lblNewPassword2.setText("Retype Password:");

        txtPassword2.setText("jTextField2");

        txtPassword1.setText("jTextField2");

        btnResetAll.setText("Reset All");

        btnNewPassword.setText("New Password");

        javax.swing.GroupLayout pnlResetLayout = new javax.swing.GroupLayout(pnlReset);
        pnlReset.setLayout(pnlResetLayout);
        pnlResetLayout.setHorizontalGroup(
            pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResetLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNewPassword2)
                    .addComponent(lblNewPassword))
                .addGap(18, 18, 18)
                .addGroup(pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNewPassword)
                    .addComponent(btnResetAll))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        pnlResetLayout.setVerticalGroup(
            pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResetLayout.createSequentialGroup()
                .addGroup(pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNewPassword)
                    .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnResetAll))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNewPassword2)
                    .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewPassword))
                .addGap(0, 35, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout PanelAdminLayout = new javax.swing.GroupLayout(PanelAdmin);
        PanelAdmin.setLayout(PanelAdminLayout);
        PanelAdminLayout.setHorizontalGroup(
            PanelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelAdminLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PanelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlJumpers, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PanelAdminLayout.createSequentialGroup()
                        .addComponent(pnlImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        PanelAdminLayout.setVerticalGroup(
            PanelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanelAdminLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanelAdminLayout.createSequentialGroup()
                        .addComponent(pnlJumpers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PanelAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlImport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(pnlExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1001, Short.MAX_VALUE)
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
        //Make the labels that we require for the centre panel. 
        labelArray = new JLabel[4][10];
        jPanelDataTable.setLayout(new GridLayout(11,4));
        jPanelDataTable.add(new JLabel("Number"));
        jPanelDataTable.add(new JLabel("ToF"));
        jPanelDataTable.add(new JLabel("ToN"));
        jPanelDataTable.add(new JLabel("Total"));
        for (int i = 0; i < 10; i++) {
            labelArray[0][i] = new JLabel("labNumber"+i);
            labelArray[1][i] = new JLabel("labTOF"+i);
            labelArray[2][i] = new JLabel("labTON"+i);
            labelArray[3][i] = new JLabel("labTotal"+i);
            for (int j = 0; j < 4; j++) {
                jPanelDataTable.add(labelArray[j][i]);
                labelArray[j][i].setText("");
            }
            labelArray[0][i].setText("Jump "+(i+1));
        }
        jPanelDataTable.repaint();
        
        //Set the numbers for the date of birth entries.
        for (int k = 1; k <= 31; k++) {
            selDate.addItem(k+"");
        }
        
        String[] monthName = {"January", "February","March", "April", "May", "June", "July","August", "September", "October", "November","December"};
         for (String s:monthName) {
            selMonth.addItem(s);
        }
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date date = new Date();
        int yearStart = Integer.parseInt(dateFormat.format(date));
        for (int l = yearStart; l > 1900; l--) {
            selYear.addItem(l+"");
        }
        
        String[] categoryName = {"A","B","C","D","E","F","G","H","I"};
         for (String s:categoryName) {
            selCategory.addItem(s);
        }
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
    private javax.swing.JButton btnAddModifyUser;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDeleteUser;
    private javax.swing.JButton btnExportUser;
    private javax.swing.JButton btnGo;
    private javax.swing.JButton btnNewPassword;
    private javax.swing.JButton btnResetAll;
    private javax.swing.JCheckBox chkBeamStatusBox1;
    private javax.swing.JCheckBox chkBeamStatusBox2;
    private javax.swing.JCheckBox chkBeamStatusBox3;
    private javax.swing.ButtonGroup grpFiletype;
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
    private javax.swing.JPanel jPanelDataTable;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblDoB;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNewPassword;
    private javax.swing.JLabel lblNewPassword2;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlData;
    private javax.swing.JPanel pnlExport;
    private javax.swing.JPanel pnlGraph;
    private javax.swing.JPanel pnlImport;
    private javax.swing.JPanel pnlJumpers;
    private javax.swing.JPanel pnlReset;
    private javax.swing.JPanel pnlStart;
    private javax.swing.JPanel pnlStats;
    private javax.swing.JComboBox selCategory;
    private javax.swing.JComboBox selComboBox1;
    private javax.swing.JComboBox selDate;
    private javax.swing.JComboBox selDeviceName;
    private javax.swing.JComboBox selMonth;
    private javax.swing.JComboBox selYear;
    private javax.swing.JTextField txtNumberOfBounces;
    private javax.swing.JTextField txtPassName;
    private javax.swing.JTextField txtPassword1;
    private javax.swing.JTextField txtPassword2;
    private javax.swing.JTextField txtUserName;
    // End of variables declaration//GEN-END:variables
}
