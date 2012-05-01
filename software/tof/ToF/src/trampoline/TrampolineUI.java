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
import java.awt.geom.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author Andreas
 */
public class TrampolineUI extends javax.swing.JFrame {
    
    public ArrayList<PortController> portsAvaliable_;
    public ArrayList<String> portStrings_;
    public ArrayList<Integer> noOfTof_;
    public TofInterface currentInterface_;
    public DBConnect db_;
    
    static SplashScreen mySplash_;
    static Graphics2D splashGraphics;               // graphics context for overlay of the splash image
    static Rectangle2D.Double splashTextArea;       // area where we draw the text
    static Rectangle2D.Double splashProgressArea;   // area where we draw the progress bar
    static Font font;                               // used to draw our text
	
    javax.swing.Timer beamStatusTimer;
    javax.swing.Timer pageRefreshTimer;
    private int refresh;
    private int nextJumpToFill;
    private static int REFRESH_TIME = 30; // Time to keep refreshing for after GO event
    private double[] chartValues;
    private String[] chartNames;
    private Chart chartObject_;
    private Chart chartObjectStats_;
    private JLabel[] labelArray_;
    private JLabel[] beamStatusGreenArray_;
    private JLabel[] beamStatusRedArray_;
    
    
   ActionListener beamstatus = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            if(currentInterface_ != null){
                boolean beamStatus[] = currentInterface_.getBeamStatus();

                for(int i=0;i<8;i++){
                    if(beamStatus[0]==true){
                        beamStatusRedArray_[2*i].setVisible(false);
                        beamStatusRedArray_[2*i+1].setVisible(false);
                        beamStatusGreenArray_[2*i].setVisible(true);
                        beamStatusGreenArray_[2*i+1].setVisible(true);
                    }else{
                        beamStatusRedArray_[2*i].setVisible(true);
                        beamStatusRedArray_[2*i+1].setVisible(true);
                        beamStatusGreenArray_[2*i].setVisible(false);
                        beamStatusGreenArray_[2*i+1].setVisible(false);                        
                    }
                }
            }
        }
    };
    
    ActionListener updatepage = new ActionListener(){
        public void actionPerformed(ActionEvent evt){        
            if(refresh>0){
                if(currentInterface_.getRoutine().getJumps().length >= nextJumpToFill){
                    Jump thisJump = currentInterface_.getRoutine().getJumps()[nextJumpToFill-1];
                    
                    labelArray_[(nextJumpToFill-1)*4].setVisible(true);
                    labelArray_[(nextJumpToFill-1)*4+1].setText(thisJump.getTof()+"");
                    labelArray_[(nextJumpToFill-1)*4+2].setText(thisJump.getTon()+"");
                    labelArray_[(nextJumpToFill-1)*4+3].setText(thisJump.getTotal()+"");
                    
                    // UPDATE BAR GRAPH
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
        this.splashText("Setting up Hardware.");
        this.splashProgress(1);
        initHardware();
        this.splashText("Setting up Database.");
        this.splashProgress(50);
        initDatabase();
        this.splashText("Setting up GUI.");
        this.splashProgress(75);
        initComponentsNonGenerated();
   
        if (mySplash_ != null)   // check if we really had a spash screen
            mySplash_.close();   // we're done with it
    }
    
    private void initHardware(){
        this.splashText("Finding ToF Devices on system.");
        this.splashProgress(0);
        PortController thisPort = new PortController();
        this.portsAvaliable_ = new ArrayList<PortController>();
        this.portStrings_ = thisPort.getPorts();
        this.noOfTof_ = thisPort.getNoTof();
        
        if(portStrings_.size()==0){
            drpDeviceName.addItem("<<No ToF Connected>>");
        }
        
        this.splashText("Connecting to available ToF Devices.");
        this.splashProgress(33);
        for (int i=0; i<portStrings_.size();i++) {
            String s = this.portStrings_.get(i);
            thisPort = new PortController(s);
            this.portsAvaliable_.add(thisPort);
            for(int j=1;j<=this.noOfTof_.get(i);j++){
                drpDeviceName.addItem(s+" Device "+j);
            }
        }
        
        String currentlySelected = drpDeviceName.getSelectedItem().toString();
        if (currentlySelected.equals("<<No ToF Connected>>")){
            this.currentInterface_ = null;
        }else{
            this.currentInterface_ = this.stringToTof(currentlySelected);
        }
        
        this.splashText("Getting beam status from ToF.");
        this.splashProgress(66);
        beamStatusTimer = new javax.swing.Timer(1, beamstatus);
        beamStatusTimer.start();
        
        pageRefreshTimer = new javax.swing.Timer(1000, updatepage);
    }
    
    private void initComponentsNonGenerated() {
        Dimension dim = this.getSize();
        int width = dim.width;
        int height = dim.height;
        
        tabPane.setSize(new Dimension(width,height));
        
        //Make the labels that we require for the centre panel. 
        labelArray_ = new JLabel[40];
        Font labelFont = new Font("Calibri", Font.PLAIN, 10);
        Font labelTitleFont = new Font("Calibri", Font.PLAIN, 16);
        Font labelJumpNoFont = new Font("Calibri", Font.PLAIN, 16);
        
        pnlDataTable.setLayout(new GridLayout(11,4));
        
        //Create the labels for the titles.
        JLabel lblNumber = new JLabel();
        lblNumber.setFont(labelTitleFont);
        lblNumber.setName("lblNumber");
        lblNumber.setText("");
        pnlDataTable.add(lblNumber);
        
        JLabel lblTof = new JLabel();
        lblTof.setFont(labelTitleFont);
        lblTof.setName("lblTof");
        lblTof.setText("ToF");
        pnlDataTable.add(lblTof);
        
        JLabel lblTon = new JLabel();
        lblTon.setFont(labelTitleFont);
        lblTon.setName("lblTon");
        lblTon.setText("ToN");
        pnlDataTable.add(lblTon);
        
        JLabel lblTotal = new JLabel();
        lblTotal.setFont(labelTitleFont);
        lblTotal.setName("lblTotal");
        lblTotal.setText("Total");
        pnlDataTable.add(lblTotal);
               
        //Create the labels for the Numbers themselves. 
        for (int i = 0; i < 10; i++) {
            labelArray_[i*4]   = new JLabel("labNumber"+i);
            labelArray_[i*4+1] = new JLabel("labTOF"+i);
            labelArray_[i*4+2] = new JLabel("labTON"+i);
            labelArray_[i*4+3] = new JLabel("labTotal"+i);
            //labelArray[0][i].setText("Jump "+(i+1));
        }
        
        //Then place the labels just created into the GridLayout. 
        for (int i = 0; i< 10; i++) {
            labelArray_[i*4].setText("Jump "+(i+1)+":");
            labelArray_[i*4].setFont(labelJumpNoFont);
            //labelArray_[i*4].setVisible(false);
            
            pnlDataTable.add(labelArray_[i*4+1]);
            labelArray_[i*4+1].setText("ToF "+i);
            labelArray_[i*4+1].setFont(labelFont);
            
            pnlDataTable.add(labelArray_[i*4+2]);
            labelArray_[i*4+2].setText("ToN "+i);
            labelArray_[i*4+2].setFont(labelFont);
            
            pnlDataTable.add(labelArray_[i*4+3]);
            labelArray_[i*4+3].setText("Total "+i);
            labelArray_[i*4+3].setFont(labelFont);
        }
        
        pnlDataTable.repaint();
        
        //Set the numbers for the date of birth entries on Club Management. 
        for (int k = 1; k <= 31; k++) {
            selDate.addItem(k+"");
        }
        
        //And month on Club Management.
        String[] monthName = {"January", "February","March", "April", "May", "June", "July","August", "September", "October", "November","December"};
        for (int l = 1; l <= 12; l++) {
            selMonth.addItem(new ComboItem(Integer.toString(l), monthName[l-1]));
        }
        
        //Finally the year on Club Management. 
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date date = new Date();
        int yearStart = Integer.parseInt(dateFormat.format(date));
        for (int l = yearStart; l > 1900; l--) {
            selYear.addItem(l+"");
        }
        
        //Category on Club Management. 
        String[] categoryName = {"A","B","C","D","E","F","G","H","I"};
        for (String s:categoryName) {
            selCategory.addItem(s);
        }
         
        updateGymnastDropDown();
         
        // Setup Beam Status images
        this.beamStatusRedArray_ = new JLabel[16];
        this.beamStatusGreenArray_ = new JLabel[16];
        
        for(int i = 0; i < 16; i++){
            beamStatusRedArray_[i] = new JLabel("");
            beamStatusGreenArray_[i] = new JLabel("");
            beamStatusRedArray_[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/trampoline/images/redBeam.png")));
            beamStatusGreenArray_[i].setIcon(new javax.swing.ImageIcon(getClass().getResource("/trampoline/images/greenBeam.png")));
            layBeamStatus.add(beamStatusRedArray_[i], javax.swing.JLayeredPane.POPUP_LAYER);
            layBeamStatus.add(beamStatusGreenArray_[i], javax.swing.JLayeredPane.POPUP_LAYER);
            beamStatusGreenArray_[i].setVisible(false);
        }
        
        beamStatusRedArray_[0].setBounds(37, 180, 20, 20);
        beamStatusGreenArray_[0].setBounds(37, 180, 20, 20);
        beamStatusRedArray_[1].setBounds(333, 180, 20, 20);
        beamStatusGreenArray_[1].setBounds(333, 180, 20, 20);
        beamStatusRedArray_[2].setBounds(37, 140, 20, 20);
        beamStatusGreenArray_[2].setBounds(37, 140, 20, 20);
        beamStatusRedArray_[3].setBounds(333, 140, 20, 20);
        beamStatusGreenArray_[3].setBounds(333, 140, 20, 20);
        beamStatusRedArray_[4].setBounds(37, 100, 20, 20);
        beamStatusGreenArray_[4].setBounds(37, 100, 20, 20);
        beamStatusRedArray_[5].setBounds(333, 100, 20, 20);
        beamStatusGreenArray_[5].setBounds(333, 100, 20, 20);
        beamStatusRedArray_[6].setBounds(70, 58, 20, 20);
        beamStatusGreenArray_[6].setBounds(70, 58, 20, 20);
        beamStatusRedArray_[7].setBounds(70, 217, 20, 20);
        beamStatusGreenArray_[7].setBounds(70, 217, 20, 20);
        beamStatusRedArray_[8].setBounds(125, 58, 20, 20);
        beamStatusGreenArray_[8].setBounds(125, 58, 20, 20);
        beamStatusRedArray_[9].setBounds(125, 217, 20, 20);
        beamStatusGreenArray_[9].setBounds(125, 217, 20, 20);
        beamStatusRedArray_[10].setBounds(188, 58, 20, 20);
        beamStatusGreenArray_[10].setBounds(188, 58, 20, 20);
        beamStatusRedArray_[11].setBounds(188, 217, 20, 20);
        beamStatusGreenArray_[11].setBounds(188, 217, 20, 20);
        beamStatusRedArray_[12].setBounds(250, 58, 20, 20);
        beamStatusGreenArray_[12].setBounds(250, 58, 20, 20);
        beamStatusRedArray_[13].setBounds(250, 217, 20, 20);
        beamStatusGreenArray_[13].setBounds(250, 217, 20, 20);
        beamStatusRedArray_[14].setBounds(305, 58, 20, 20);
        beamStatusGreenArray_[14].setBounds(305, 58, 20, 20);
        beamStatusRedArray_[15].setBounds(305, 217, 20, 20);
        beamStatusGreenArray_[15].setBounds(305, 217, 20, 20);
 
        //Set size of list of tags
        int noItems = lstTags.getModel().getSize()*35;
        System.out.println(noItems);
        lstTags.setPreferredSize(new Dimension(128,noItems));
        lstTags.setSize(new Dimension (128,noItems));
        
                
        //Create a dummy chart to add to essentially reserve the space on the relevant panels.        
        double[] values = new double[3];
        String[] names = new String[3];
        values[0] = 1;
        names[0] = "Item 1";

        values[1] = 2;
        names[1] = "Item 2";

        values[2] = 4;
        names[2] = "Item 3";
        
        //Create the chart objects with dummy data.
        chartObject_ = new Chart(values, names, "title");
        chartObjectStats_ = new Chart(values, names, "title ststs");

        //Set the two graph panels to have the appropriate layouts and add the charts. 
        pnlGraph.setLayout(new java.awt.BorderLayout());
        pnlGraph.add(chartObject_, BorderLayout.CENTER);
        pnlStatisticsSmall.setLayout(new java.awt.BorderLayout());
        pnlStatisticsSmall.add(chartObjectStats_, BorderLayout.CENTER);
         
        //Initially give values to avoid NullPointerExceptions
        chartValues = new double[10];
        chartNames  = new String[10];
        for (int i = 0; i < 10; i++) {
            chartValues[i] = 0;
            chartNames[i]  = "Bounce "+i;
        }
    }
    
    private void initDatabase(){
        db_ = new DBConnect();
    }
    
    public TofInterface stringToTof(String s) {
        String split[] = s.split(" ");
        
        if(split.length == 3){
            int id = this.portStrings_.indexOf(split[0]);
            return this.portsAvaliable_.get(id).getInterface(Integer.parseInt(split[2])-1);
        }else{
            int id = this.portStrings_.indexOf(split[0]);
            return this.portsAvaliable_.get(id).getInterface(0);
        }
    }
    
    public void callOnClose() {
        for (PortController thisPort : this.portsAvaliable_) {
            thisPort.close();
        }
        beamStatusTimer.stop();
    }
    
    //This function updates the mini chart on each bounce. 
    public void updateChart(double[] values, String[] names, String title, JPanel pan) {        
        chartObject_.updateInfo(values, names, title);

        //jPanel4.setLayout(new java.awt.BorderLayout());
        pan.removeAll();
        pan.validate();
        pan.repaint();
        pan.add(chartObject_, BorderLayout.CENTER);
        pan.validate();
        pan.repaint();
    }
    
    public void updateChart(double[] values, String[] names, String title) {
        updateChart(values, names, "Bounce Height", pnlGraph);
    }
    
    public void updateChart(double[] values, String[] names) {
        updateChart(values, names, "Bounce Height");
    }
    
    public void updateGymnastDropDown() {
        JComboBox[] boxesToUpdate = {selStatsGymnast, selDataGymnast, selUserName};
        Gymnast[] gymnastList = db_.getAllGymnasts();
        
        for (JComboBox jcb:boxesToUpdate) {
            jcb.removeAllItems();
            
            jcb.addItem(new ComboItem(0, "<< Please Select Gymnast >>"));

            //Gymnast List on Statistics
            for (Gymnast g:gymnastList) {
                jcb.addItem(new ComboItem(g.getID(), g.getName()));
            }
        }
    }
    
    public void updateJumpTime(String jumpNum, Jump j) {
        //This needs to be written to take account of the new JLabels for the times. 
    }
    
    public void updateJumpTime(int jumpNum, Jump j) {
        updateJumpTime(String.valueOf(jumpNum), j);
    }
    
    //Given a routine, this function will update the main Statistics panel with a graph as well
    //as textual information (the exact nature of which is to be decided).
    public void updateStatistics(Routine r) {
        double[] times  = r.getStatsTimes();
        double[] values = new double[r.getNumberOfJumps()];
        String[] names  = new String[r.getNumberOfJumps()];
        for (int i = 0; i < r.getNumberOfJumps(); i++) {
            values[i] = times[i]*1000;
            names[i]  = "Jump "+i;
            System.out.println(i+ " - "+values[i]+ "=== "+names[i]);
        }
        
        chartObjectStats_.updateInfo(values, names, "tsdfsndf");

        //jPanel4.setLayout(new java.awt.BorderLayout());
        pnlStatisticsSmall.removeAll();
        pnlStatisticsSmall.validate();
        pnlStatisticsSmall.repaint();
        pnlStatisticsSmall.add(chartObjectStats_, BorderLayout.CENTER);
        pnlStatisticsSmall.validate();
        pnlStatisticsSmall.repaint();
    }
    
    public void updateStatistics(Jump[] jumpList) {
        Routine r = new Routine(jumpList);
        updateStatistics(r);
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
        tabPane = new javax.swing.JTabbedPane();
        pnlToF = new javax.swing.JPanel();
        pnlStart = new javax.swing.JPanel();
        btnCollectData = new javax.swing.JButton();
        txtNumberOfBounces = new javax.swing.JTextField();
        labNumberOfBounces = new javax.swing.JLabel();
        drpDeviceName = new javax.swing.JComboBox();
        labSelectTof = new javax.swing.JLabel();
        labGymnast = new javax.swing.JLabel();
        selDataGymnast = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstTags = new javax.swing.JList();
        lblTags = new javax.swing.JLabel();
        btnClearData = new javax.swing.JButton();
        pnlData = new javax.swing.JPanel();
        btnSaveComments = new javax.swing.JButton();
        pnlDataTable = new javax.swing.JPanel();
        labNameForData = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtComment = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        pnlStats = new javax.swing.JPanel();
        pnlGraph = new javax.swing.JPanel();
        layBeamStatus = new javax.swing.JLayeredPane();
        lblTrampoline = new javax.swing.JLabel();
        pnlStatistics = new javax.swing.JPanel();
        pnlStatisticsSmall = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnReadFile = new javax.swing.JButton();
        selStatsGymnast = new javax.swing.JComboBox();
        lblStatsGymnast = new javax.swing.JLabel();
        btnStatisticsUpdate = new javax.swing.JButton();
        lblStatsRoutine = new javax.swing.JLabel();
        drpStatsRoutine = new javax.swing.JComboBox();
        pnlImportExport = new javax.swing.JPanel();
        pnlImport = new javax.swing.JPanel();
        pnlExport = new javax.swing.JPanel();
        rdoExportCsv = new javax.swing.JRadioButton();
        btnExportUser = new javax.swing.JButton();
        rdoExportText = new javax.swing.JRadioButton();
        rdoExportExcel = new javax.swing.JRadioButton();
        pnlAdmin = new javax.swing.JPanel();
        pnlGymnast = new javax.swing.JPanel();
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
        txtName = new javax.swing.JTextField();
        selUserName = new javax.swing.JComboBox();
        lblGymnastSuccess = new javax.swing.JLabel();
        pnlRoutines = new javax.swing.JPanel();
        pnlReset = new javax.swing.JPanel();
        lblNewPassword = new javax.swing.JLabel();
        lblNewPassword2 = new javax.swing.JLabel();
        txtPassword2 = new javax.swing.JTextField();
        txtPassword1 = new javax.swing.JTextField();
        btnResetAll = new javax.swing.JButton();
        btnNewPassword = new javax.swing.JButton();
        menBar = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chronos");
        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(1280, 1024));

        tabPane.setBackground(new java.awt.Color(255, 255, 255));
        tabPane.setPreferredSize(new java.awt.Dimension(1280, 1024));
        tabPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tabPaneFocusGained(evt);
            }
        });

        pnlToF.setBackground(new java.awt.Color(255, 255, 255));
        pnlToF.setPreferredSize(new java.awt.Dimension(1280, 1024));

        pnlStart.setBackground(new java.awt.Color(255, 255, 255));
        pnlStart.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Start Bouncing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        btnCollectData.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        btnCollectData.setText("Collect Data");
        btnCollectData.setName("");
        btnCollectData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCollectDataActionPerformed(evt);
            }
        });

        txtNumberOfBounces.setText("10");
        txtNumberOfBounces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNumberOfBouncesActionPerformed(evt);
            }
        });

        labNumberOfBounces.setLabelFor(txtNumberOfBounces);
        labNumberOfBounces.setText("Number of Jumps To Collect:");

        drpDeviceName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drpDeviceNameActionPerformed(evt);
            }
        });

        labSelectTof.setText("Select a ToF Device:");

        labGymnast.setText("Select a Gymnast:");

        lstTags.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        lstTags.setMaximumSize(new java.awt.Dimension(1280, 1024));
        lstTags.setMinimumSize(new java.awt.Dimension(35, 128));
        lstTags.setPreferredSize(new java.awt.Dimension(40, 128));
        jScrollPane2.setViewportView(lstTags);

        lblTags.setText("Select Tags for Pass:");

        btnClearData.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        btnClearData.setText("Clear Data");
        btnClearData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlStartLayout = new javax.swing.GroupLayout(pnlStart);
        pnlStart.setLayout(pnlStartLayout);
        pnlStartLayout.setHorizontalGroup(
            pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStartLayout.createSequentialGroup()
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlStartLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labSelectTof)
                            .addComponent(labGymnast)
                            .addComponent(lblTags)
                            .addComponent(labNumberOfBounces)))
                    .addGroup(pnlStartLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(btnCollectData)))
                .addGap(38, 38, 38)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(drpDeviceName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(selDataGymnast, 0, 157, Short.MAX_VALUE)
                    .addGroup(pnlStartLayout.createSequentialGroup()
                        .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnClearData, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlStartLayout.setVerticalGroup(
            pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStartLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labSelectTof)
                    .addComponent(drpDeviceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labGymnast)
                    .addComponent(selDataGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labNumberOfBounces)
                    .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTags)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClearData, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCollectData, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlData.setBackground(new java.awt.Color(255, 255, 255));
        pnlData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        btnSaveComments.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        btnSaveComments.setText("Save Comments");
        btnSaveComments.setName("");
        btnSaveComments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveCommentsActionPerformed(evt);
            }
        });

        pnlDataTable.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout pnlDataTableLayout = new javax.swing.GroupLayout(pnlDataTable);
        pnlDataTable.setLayout(pnlDataTableLayout);
        pnlDataTableLayout.setHorizontalGroup(
            pnlDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlDataTableLayout.setVerticalGroup(
            pnlDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 417, Short.MAX_VALUE)
        );

        labNameForData.setText("Comments:");

        txtComment.setColumns(20);
        txtComment.setRows(5);
        jScrollPane1.setViewportView(txtComment);

        jButton1.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        jButton1.setText("Clear Comments");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlDataLayout = new javax.swing.GroupLayout(pnlData);
        pnlData.setLayout(pnlDataLayout);
        pnlDataLayout.setHorizontalGroup(
            pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDataLayout.createSequentialGroup()
                .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlDataLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlDataTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlDataLayout.createSequentialGroup()
                        .addComponent(labNameForData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSaveComments, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlDataLayout.setVerticalGroup(
            pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlDataTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDataLayout.createSequentialGroup()
                        .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labNameForData)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSaveComments))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlStats.setBackground(new java.awt.Color(255, 255, 255));
        pnlStats.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Event Stats", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        javax.swing.GroupLayout pnlStatsLayout = new javax.swing.GroupLayout(pnlStats);
        pnlStats.setLayout(pnlStatsLayout);
        pnlStatsLayout.setHorizontalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnlStatsLayout.setVerticalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 286, Short.MAX_VALUE)
        );

        pnlGraph.setBackground(new java.awt.Color(255, 255, 255));
        pnlGraph.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Graph", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N
        pnlGraph.setPreferredSize(new java.awt.Dimension(500, 500));

        javax.swing.GroupLayout pnlGraphLayout = new javax.swing.GroupLayout(pnlGraph);
        pnlGraph.setLayout(pnlGraphLayout);
        pnlGraphLayout.setHorizontalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 352, Short.MAX_VALUE)
        );
        pnlGraphLayout.setVerticalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 305, Short.MAX_VALUE)
        );

        layBeamStatus.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Beam Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        lblTrampoline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/trampoline/images/eurotramp.jpg"))); // NOI18N
        lblTrampoline.setBounds(15, 40, 375, 217);
        layBeamStatus.add(lblTrampoline, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout pnlToFLayout = new javax.swing.GroupLayout(pnlToF);
        pnlToF.setLayout(pnlToFLayout);
        pnlToFLayout.setHorizontalGroup(
            pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToFLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(layBeamStatus)
                    .addComponent(pnlStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlStats, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlToFLayout.setVerticalGroup(
            pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToFLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlToFLayout.createSequentialGroup()
                        .addComponent(pnlStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE))
                    .addComponent(pnlData, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlToFLayout.createSequentialGroup()
                        .addComponent(pnlStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(layBeamStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabPane.addTab("Time of Flight", pnlToF);

        pnlStatisticsSmall.setBackground(new java.awt.Color(255, 102, 102));
        pnlStatisticsSmall.setPreferredSize(new java.awt.Dimension(495, 495));

        javax.swing.GroupLayout pnlStatisticsSmallLayout = new javax.swing.GroupLayout(pnlStatisticsSmall);
        pnlStatisticsSmall.setLayout(pnlStatisticsSmallLayout);
        pnlStatisticsSmallLayout.setHorizontalGroup(
            pnlStatisticsSmallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 495, Short.MAX_VALUE)
        );
        pnlStatisticsSmallLayout.setVerticalGroup(
            pnlStatisticsSmallLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        btnReadFile.setText("Read File");
        btnReadFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReadFileActionPerformed(evt);
            }
        });

        lblStatsGymnast.setText("Select Gymnast:");

        btnStatisticsUpdate.setText("Update Lists Below");
        btnStatisticsUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatisticsUpdateActionPerformed(evt);
            }
        });

        lblStatsRoutine.setText("Select Routine:");

        drpStatsRoutine.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<< Select Gymnast First >>" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnReadFile)
                        .addGap(117, 117, 117)
                        .addComponent(btnStatisticsUpdate))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblStatsGymnast)
                            .addComponent(lblStatsRoutine))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(drpStatsRoutine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selStatsGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(466, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnReadFile)
                        .addGap(53, 53, 53)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(selStatsGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblStatsGymnast)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(btnStatisticsUpdate)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblStatsRoutine)
                    .addComponent(drpStatsRoutine, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(516, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlStatisticsLayout = new javax.swing.GroupLayout(pnlStatistics);
        pnlStatistics.setLayout(pnlStatisticsLayout);
        pnlStatisticsLayout.setHorizontalGroup(
            pnlStatisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatisticsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlStatisticsSmall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlStatisticsLayout.setVerticalGroup(
            pnlStatisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlStatisticsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatisticsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlStatisticsSmall, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabPane.addTab("Statistics", pnlStatistics);

        pnlImport.setBorder(javax.swing.BorderFactory.createTitledBorder("Import"));

        javax.swing.GroupLayout pnlImportLayout = new javax.swing.GroupLayout(pnlImport);
        pnlImport.setLayout(pnlImportLayout);
        pnlImportLayout.setHorizontalGroup(
            pnlImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 492, Short.MAX_VALUE)
        );
        pnlImportLayout.setVerticalGroup(
            pnlImportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        pnlExport.setBorder(javax.swing.BorderFactory.createTitledBorder("Export"));

        grpFiletype.add(rdoExportCsv);
        rdoExportCsv.setText("CSV");

        btnExportUser.setText("Export User");

        grpFiletype.add(rdoExportText);
        rdoExportText.setText("Text");

        grpFiletype.add(rdoExportExcel);
        rdoExportExcel.setText("Excel");

        javax.swing.GroupLayout pnlExportLayout = new javax.swing.GroupLayout(pnlExport);
        pnlExport.setLayout(pnlExportLayout);
        pnlExportLayout.setHorizontalGroup(
            pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExportLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExportUser)
                    .addComponent(rdoExportCsv)
                    .addComponent(rdoExportText)
                    .addComponent(rdoExportExcel))
                .addContainerGap(664, Short.MAX_VALUE))
        );
        pnlExportLayout.setVerticalGroup(
            pnlExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlExportLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(btnExportUser)
                .addGap(18, 18, 18)
                .addComponent(rdoExportCsv)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoExportText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdoExportExcel)
                .addContainerGap(506, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlImportExportLayout = new javax.swing.GroupLayout(pnlImportExport);
        pnlImportExport.setLayout(pnlImportExportLayout);
        pnlImportExportLayout.setHorizontalGroup(
            pnlImportExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlImportExportLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlImportExportLayout.setVerticalGroup(
            pnlImportExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlImportExportLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlImportExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlImport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabPane.addTab("Import/Export Data", pnlImportExport);

        pnlGymnast.setBorder(javax.swing.BorderFactory.createTitledBorder("Gymnast"));

        lblUser.setText("User:");

        lblName.setText("Name:");

        lblDoB.setText("DoB:");

        lblCategory.setText("Category:");

        btnAddModifyUser.setText("Add User");
        btnAddModifyUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddModifyUserActionPerformed(evt);
            }
        });

        btnDeleteUser.setText("Delete User");
        btnDeleteUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteUserActionPerformed(evt);
            }
        });

        selUserName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selUserNameActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlGymnastLayout = new javax.swing.GroupLayout(pnlGymnast);
        pnlGymnast.setLayout(pnlGymnastLayout);
        pnlGymnastLayout.setHorizontalGroup(
            pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGymnastLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGymnastLayout.createSequentialGroup()
                        .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCategory)
                            .addComponent(lblDoB)
                            .addComponent(lblUser)
                            .addComponent(lblName))
                        .addGap(18, 18, 18)
                        .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlGymnastLayout.createSequentialGroup()
                                .addComponent(selDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(selCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAddModifyUser)
                            .addComponent(btnDeleteUser)
                            .addComponent(selUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(lblGymnastSuccess))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlGymnastLayout.setVerticalGroup(
            pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGymnastLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(selUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDoB)
                    .addComponent(selDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategory)
                    .addComponent(selCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnAddModifyUser)
                .addGap(18, 18, 18)
                .addComponent(btnDeleteUser)
                .addGap(18, 18, 18)
                .addComponent(lblGymnastSuccess)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlRoutines.setBorder(javax.swing.BorderFactory.createTitledBorder("Routines"));

        javax.swing.GroupLayout pnlRoutinesLayout = new javax.swing.GroupLayout(pnlRoutines);
        pnlRoutines.setLayout(pnlRoutinesLayout);
        pnlRoutinesLayout.setHorizontalGroup(
            pnlRoutinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1077, Short.MAX_VALUE)
        );
        pnlRoutinesLayout.setVerticalGroup(
            pnlRoutinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 393, Short.MAX_VALUE)
        );

        pnlReset.setBorder(javax.swing.BorderFactory.createTitledBorder("Reset"));

        lblNewPassword.setText("New Password:");

        lblNewPassword2.setText("Retype Password:");

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
                    .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(pnlResetLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNewPassword)
                    .addComponent(btnResetAll))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGap(0, 151, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlAdminLayout = new javax.swing.GroupLayout(pnlAdmin);
        pnlAdmin.setLayout(pnlAdminLayout);
        pnlAdminLayout.setHorizontalGroup(
            pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdminLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlRoutines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAdminLayout.setVerticalGroup(
            pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdminLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAdminLayout.createSequentialGroup()
                        .addComponent(pnlRoutines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(pnlGymnast, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabPane.addTab("Club Management", pnlAdmin);

        jMenu3.setText("File");

        jMenuItem1.setText("jMenuItem1");
        jMenu3.add(jMenuItem1);

        menBar.add(jMenu3);

        jMenu4.setText("Edit");
        menBar.add(jMenu4);

        setJMenuBar(menBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, 1319, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabPane, javax.swing.GroupLayout.PREFERRED_SIZE, 711, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(281, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCommentsActionPerformed
        // ADD CODE TO CLEAR SCREEN HERE
    }//GEN-LAST:event_btnSaveCommentsActionPerformed

    private void btnCollectDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollectDataActionPerformed
        this.btnClearDataActionPerformed(evt);
        if(this.currentInterface_!=null){
            this.currentInterface_.collectBounces(Integer.parseInt(txtNumberOfBounces.getText()), this.db_, 1);
            refresh = REFRESH_TIME;
            nextJumpToFill = 1;
            pageRefreshTimer.start();
        }
    }//GEN-LAST:event_btnCollectDataActionPerformed

    private void txtNumberOfBouncesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNumberOfBouncesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNumberOfBouncesActionPerformed

    private void drpDeviceNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drpDeviceNameActionPerformed
        if(!(drpDeviceName.getSelectedItem().toString().equals("<<No ToF Connected>>"))){
            this.currentInterface_ = this.stringToTof(drpDeviceName.getSelectedItem().toString());
        }       
    }//GEN-LAST:event_drpDeviceNameActionPerformed

    private void btnAddModifyUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddModifyUserActionPerformed
        if (selUserName.getSelectedIndex() == 0) {
            if (txtName.getText() == "") {
                lblGymnastSuccess.setText("You must include a gymnast name.");
            } else {
                //Then we need to add the gymnast. Start by entering the information into the databse. 
                ComboItem c = (ComboItem) selMonth.getSelectedItem();

                db_.addGymnast(txtName.getText(), Integer.parseInt(selDate.getSelectedItem().toString()), Integer.parseInt(c.getID()), Integer.parseInt(selYear.getSelectedItem().toString()), selCategory.getSelectedItem().toString());

                //Add a success message.
                lblGymnastSuccess.setText("The Gymnast '"+txtName.getText()+"' has been added.");

                //Then clear all the items. 
                txtName.setText("");
                selDate.setSelectedIndex(0);

                //Re-update the drop-down.
                updateGymnastDropDown();
            }
        } else {
            //Then we have to edit the user selected.
            
        }
    }//GEN-LAST:event_btnAddModifyUserActionPerformed

    private void btnReadFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReadFileActionPerformed
        //Basically testing code for this button.
        Read read = new Read();
        Jump[] jumpList = read.createJumpList("data/andreasTest.xml");
        updateStatistics(jumpList);
    }//GEN-LAST:event_btnReadFileActionPerformed

    private void tabPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tabPaneFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_tabPaneFocusGained

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        txtComment.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnClearDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearDataActionPerformed
        for(int i=0;i<10;i++){
            labelArray_[i*4].setVisible(false);
            labelArray_[i*4+1].setText("");
            labelArray_[i*4+2].setText("");
            labelArray_[i*4+3].setText("");
        }
    }//GEN-LAST:event_btnClearDataActionPerformed

    private void btnStatisticsUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatisticsUpdateActionPerformed
        db_.getJump(3);
        updateGymnastDropDown();
    }//GEN-LAST:event_btnStatisticsUpdateActionPerformed

    private void btnDeleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteUserActionPerformed
        ComboItem c = (ComboItem) selUserName.getSelectedItem();
        int confirmInt = (int) JOptionPane.showConfirmDialog(pnlStatistics, "Are you sure you want to Delete '"+c+"'?", "Delete User", 0, 0);
        
        if (confirmInt == 0) {
            lblGymnastSuccess.setText("The Gymnast '"+c+"' has been deleted.");
            db_.deleteGymnast(c.getNumericID());
        }
        
        updateGymnastDropDown();
    }//GEN-LAST:event_btnDeleteUserActionPerformed

    private void selUserNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selUserNameActionPerformed
        ComboItem c = (ComboItem) selUserName.getSelectedItem();
        
        if (c.getID() == "0") {
            btnAddModifyUser.setText("Add User");
        } else {
            Gymnast g = db_.getGymnast(c.getNumericID());
            txtName.setText(g.getName());
            selDate.setSelectedIndex(g.getDobDay());
            btnAddModifyUser.setText("Modify User");
        }
    }//GEN-LAST:event_selUserNameActionPerformed

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
        
        splashInit();           // initialize splash overlay drawing parameters
               
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TrampolineUI().setVisible(true);
            }
        });
    }
    
    private static void splashInit(){
        // the splash screen object is created by the JVM, if it is displaying a splash image
        
        mySplash_ = SplashScreen.getSplashScreen();
        // if there are any problems displaying the splash image
        // the call to getSplashScreen will returned null

        if (mySplash_ != null)
        {
            // get the size of the image now being displayed
            Dimension ssDim = mySplash_.getSize();
            int height = ssDim.height;
            int width = ssDim.width;
            
            // stake out some area for our status information
            splashTextArea = new Rectangle2D.Double(15., height*0.898, width * .45, 20);
            
            splashProgressArea = new Rectangle2D.Double(width * .55, height*.92, width*.4, 12);

            // create the Graphics environment for drawing status info
            splashGraphics = mySplash_.createGraphics();
            font = new Font("Dialog", Font.PLAIN, 14);
            splashGraphics.setFont(font);

            // initialize the status info
            splashText("Starting...");
            splashProgress(0);
        }
    }
    
    public static void splashText(String str)
    {
        if (mySplash_ != null && mySplash_.isVisible())
        {   // important to check here so no other methods need to know if there
            // really is a Splash being displayed

            // erase the last status text
            splashGraphics.setPaint(Color.WHITE);
            splashGraphics.fill(splashTextArea);

            // draw the text
            splashGraphics.setPaint(Color.BLUE);
            splashGraphics.drawString(str, (int)(splashTextArea.getX() + 10),(int)(splashTextArea.getY() + 15));

            // make sure it's displayed
            mySplash_.update();
        }
    }
    
     public static void splashProgress(int pct)
    {
        if (mySplash_ != null && mySplash_.isVisible())
        {

            // Note: 3 colors are used here to demonstrate steps
            // erase the old one
            splashGraphics.setPaint(Color.WHITE);
            splashGraphics.fill(splashProgressArea);

            // draw an outline
            splashGraphics.setPaint(Color.BLACK);
            splashGraphics.draw(splashProgressArea);

            // Calculate the width corresponding to the correct percentage
            int x = (int) splashProgressArea.getMinX();
            int y = (int) splashProgressArea.getMinY();
            int wid = (int) splashProgressArea.getWidth();
            int hgt = (int) splashProgressArea.getHeight();

            int doneWidth = Math.round(pct*wid/100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width

            // fill the done part one pixel smaller than the outline
            splashGraphics.setPaint(Color.RED);
            splashGraphics.fillRect(x, y+1, doneWidth, hgt-1);

            // make sure it's displayed
            mySplash_.update();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddModifyUser;
    private javax.swing.JButton btnClearData;
    private javax.swing.JButton btnCollectData;
    private javax.swing.JButton btnDeleteUser;
    private javax.swing.JButton btnExportUser;
    private javax.swing.JButton btnNewPassword;
    private javax.swing.JButton btnReadFile;
    private javax.swing.JButton btnResetAll;
    private javax.swing.JButton btnSaveComments;
    private javax.swing.JButton btnStatisticsUpdate;
    private javax.swing.JComboBox drpDeviceName;
    private javax.swing.JComboBox drpStatsRoutine;
    private javax.swing.ButtonGroup grpFiletype;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labGymnast;
    private javax.swing.JLabel labNameForData;
    private javax.swing.JLabel labNumberOfBounces;
    private javax.swing.JLabel labSelectTof;
    private javax.swing.JLayeredPane layBeamStatus;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblDoB;
    private javax.swing.JLabel lblGymnastSuccess;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNewPassword;
    private javax.swing.JLabel lblNewPassword2;
    private javax.swing.JLabel lblStatsGymnast;
    private javax.swing.JLabel lblStatsRoutine;
    private javax.swing.JLabel lblTags;
    private javax.swing.JLabel lblTrampoline;
    private javax.swing.JLabel lblUser;
    private javax.swing.JList lstTags;
    private javax.swing.JMenuBar menBar;
    private javax.swing.JPanel pnlAdmin;
    private javax.swing.JPanel pnlData;
    private javax.swing.JPanel pnlDataTable;
    private javax.swing.JPanel pnlExport;
    private javax.swing.JPanel pnlGraph;
    private javax.swing.JPanel pnlGymnast;
    private javax.swing.JPanel pnlImport;
    private javax.swing.JPanel pnlImportExport;
    private javax.swing.JPanel pnlReset;
    private javax.swing.JPanel pnlRoutines;
    private javax.swing.JPanel pnlStart;
    private javax.swing.JPanel pnlStatistics;
    private javax.swing.JPanel pnlStatisticsSmall;
    private javax.swing.JPanel pnlStats;
    private javax.swing.JPanel pnlToF;
    private javax.swing.JRadioButton rdoExportCsv;
    private javax.swing.JRadioButton rdoExportExcel;
    private javax.swing.JRadioButton rdoExportText;
    private javax.swing.JComboBox selCategory;
    private javax.swing.JComboBox selDataGymnast;
    private javax.swing.JComboBox selDate;
    private javax.swing.JComboBox selMonth;
    private javax.swing.JComboBox selStatsGymnast;
    private javax.swing.JComboBox selUserName;
    private javax.swing.JComboBox selYear;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTextArea txtComment;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNumberOfBounces;
    private javax.swing.JTextField txtPassword1;
    private javax.swing.JTextField txtPassword2;
    // End of variables declaration//GEN-END:variables
}
