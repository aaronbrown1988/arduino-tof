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
import java.awt.Image.*;
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
	
    javax.swing.Timer jumpTimer;
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
    private int currentRoutineId_;      // ID of routine displayed on screen currently;
    private Map<String,ImageIcon> locationImagesSmall_; //Array of location images for labels
    private Map<String,ImageIcon> locationImagesLarge_; //Array of location images for labels
    
    private boolean adminAccessGranted_;
    private String adminPassword_;
    private Dimension screenResolution_; //Current screen resolution when program loaded
    private ErrorHandler errorHandler_;  // Instance of the project Error Handler.
    
    
   ActionListener pageRefresh = new ActionListener() {
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
            
            if(errorHandler_.isError()){
                lblError.setText(errorHandler_.getCurrentErrorShort() + " (Click for more info...)");
            }
        }
    };
    
    ActionListener jumpAction = new ActionListener(){
        public void actionPerformed(ActionEvent evt){        
            if(refresh>0){
                if(currentInterface_.getRoutine().getJumps().length >= nextJumpToFill){
                    Jump thisJump = currentInterface_.getRoutine().getJumps()[nextJumpToFill-1];
                    
                    labelArray_[(nextJumpToFill-1)*5].setVisible(true);
                    labelArray_[(nextJumpToFill-1)*5+1].setText(thisJump.getTof()+"");
                    labelArray_[(nextJumpToFill-1)*5+2].setText(thisJump.getTon()+"");
                    labelArray_[(nextJumpToFill-1)*5+3].setText(thisJump.getTotal()+"");
                    labelArray_[(nextJumpToFill-1)*5+4].setText(thisJump.getLocation());
                    
                    // UPDATE BAR GRAPH
                    chartValues[nextJumpToFill-1] = thisJump.getTof();

                    updateChart(chartValues, chartNames);
                    nextJumpToFill++;
                }
                refresh--;
            } else {
                btnSaveComments.setVisible(true);
                btnClearComments.setVisible(true);
                txtComments.setVisible(true);
                lblComments.setVisible(true);
                pageRefreshTimer.stop();
                currentRoutineId_ = currentInterface_.getRoutineId();
            } 
        }
    };
    
    /**
     * Creates new form TrampolineUI
     */
        
    public TrampolineUI() {
        
        initComponents();
        this.splashText("Connecting to Database.");
        this.splashProgress(10);
        initDatabase();
        this.splashText("Setting up GUI.");
        this.splashProgress(20);
        initGeneralUI();
        this.splashProgress(30);
        initToFUI();
        this.splashProgress(35);
        initStatisticsUI();
        this.splashProgress(45);
        initImportExportUI();
        this.splashProgress(50);
        initClubManagementUI();
        this.splashText("Setting up Hardware.");
        this.splashProgress(75);
        initHardware();
        if (mySplash_ != null)   // check if we really had a spash screen
            mySplash_.close();   // we're done with it
    }
    
    private void initHardware(){
        this.errorHandler_ = new ErrorHandler();
        this.splashText("Finding ToF Devices on system.");
        PortController thisPort = new PortController(this.errorHandler_);
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
            thisPort = new PortController(this.errorHandler_, s);
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
        pageRefreshTimer = new javax.swing.Timer(1, pageRefresh);
        pageRefreshTimer.start();
        
        jumpTimer = new javax.swing.Timer(1000, jumpAction);
    }
    
    private void initToFUI(){
        int screenHeight = this.getMaximizedBounds().height;
        int screenWidth = this.getMaximizedBounds().width;
        int heightTags;  
        
        //Resolution specific layout
        if(screenWidth<1200){
            //Code for screens less than 1200 width (once taken off taskbar) assuming size of 1024 x 768
            pnlStats.setVisible(false);
            pnlGraph.setVisible(false);

            GroupLayout pnlToFLayout = (GroupLayout)pnlToF.getLayout(); 
            pnlToFLayout.setHorizontalGroup(
                pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlToFLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(layBeamStatus, 380, 380, 380)
                        .addComponent(pnlStart, 380, 380, 380))
                    .addGap(15)
                    .addComponent(pnlData, screenWidth - 420, screenWidth - 420, screenWidth - 420)
                    .addContainerGap(5,5))
            );
            
            pnlToFLayout.setVerticalGroup(
                pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlToFLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addGroup(pnlToFLayout.createSequentialGroup()
                            .addComponent(pnlStart,screenHeight-365,screenHeight-365,screenHeight-365)
                            .addGap(15)
                            .addComponent(layBeamStatus, 240, 240, 240))
                        .addComponent(pnlData,screenHeight-110,screenHeight-110,screenHeight-110))
                     .addContainerGap(5,5)
            ));
            
            GroupLayout pnlDataLayout = (GroupLayout)pnlData.getLayout();
            pnlDataLayout.setHorizontalGroup(
                pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlDataLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addComponent(pnlDataTable,screenWidth - 470,screenWidth - 470,screenWidth - 470)
                    .addContainerGap(5,5))
                .addComponent(lblComments)
                .addComponent(sclComments, screenWidth - 470, screenWidth - 470,screenWidth - 470)
                .addGroup(pnlDataLayout.createSequentialGroup()
                    .addComponent(btnClearComments,(screenWidth - 470)/2,(screenWidth - 470)/2,(screenWidth - 470)/2)
                    .addComponent(btnSaveComments,(screenWidth - 470)/2,(screenWidth - 470)/2,(screenWidth - 470)/2)) 
            );

            pnlDataLayout.setVerticalGroup(
                pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlDataLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addComponent(pnlDataTable, screenHeight-320,screenHeight-320,screenHeight-320)
                    .addGap(5,5,5)
                    .addComponent(lblComments,25,25,25)
                    .addComponent(sclComments,100,100,100)
                    .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(btnClearComments, 25,25,25)
                        .addGap(5,5,5)
                        .addComponent(btnSaveComments,25,25,25))
                    .addContainerGap(5,5))
            );
            
            heightTags = 75;
            //Setup image array
            locationImagesSmall_ = new HashMap<String,ImageIcon>(35);
            locationImagesLarge_ = new HashMap<String,ImageIcon>(35);
            char[] letters = {'A','B','C','D','E','F','G'};
            char[] numbers = {'0','1','2','3','4'};
            int count = 0;
            for(char letter : letters){
                for(char number: numbers){
                    ImageIcon icon = new ImageIcon(getClass().getResource("/trampoline/images/"+letter+number+".png"));
                    Image img = icon.getImage();
                    java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(60, 35, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics g = bi.createGraphics();
                    g.drawImage(img, 0,0, 60, 35, null);
                    locationImagesSmall_.put(letter+number+"" ,new ImageIcon(bi));
                    locationImagesLarge_.put(letter+number+"", icon);
                    count++;
                }
            }
        }else{
            //Code for screens 1280 X 1024 or larger
            pnlStats.setVisible(true);
            pnlGraph.setVisible(true);
            
            GroupLayout pnlToFLayout = (GroupLayout)pnlToF.getLayout();  
            pnlToFLayout.setHorizontalGroup(
                pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlToFLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(layBeamStatus, 380, 380, 380)
                        .addComponent(pnlStart, 380, 380, 380))
                    .addGap(15)
                    .addComponent(pnlData, screenWidth - 800, screenWidth - 800, screenWidth - 800)
                    .addGap(15)
                    .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(pnlStats, 380, 380, 380)
                        .addComponent(pnlGraph, 380, 380, 380))
                    .addContainerGap(5,5))
            );
            
            pnlToFLayout.setVerticalGroup(
                pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlToFLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addGroup(pnlToFLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addGroup(pnlToFLayout.createSequentialGroup()
                            .addComponent(pnlStart,screenHeight-365,screenHeight-365,screenHeight-365)
                            .addGap(15)
                            .addComponent(layBeamStatus, 240, 240, 240))
                        .addComponent(pnlData,screenHeight-110,screenHeight-110,screenHeight-110)
                        .addGroup(pnlToFLayout.createSequentialGroup()
                                .addComponent(pnlStats,(screenHeight-125)/2,(screenHeight-125)/2,(screenHeight-125)/2)
                                .addGap(15)
                                .addComponent(pnlGraph,(screenHeight-125)/2,(screenHeight-125)/2,(screenHeight-125)/2)))
                    .addContainerGap(5,5)
            ));
            
           GroupLayout pnlDataLayout = (GroupLayout)pnlData.getLayout();
            pnlDataLayout.setHorizontalGroup(
                pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlDataLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addComponent(pnlDataTable,screenWidth - 830,screenWidth - 830,screenWidth - 830)
                    .addContainerGap(5,5))
                .addComponent(lblComments)
                .addComponent(sclComments, screenWidth - 830, screenWidth - 830,screenWidth - 830)
                .addGroup(pnlDataLayout.createSequentialGroup()
                    .addComponent(btnClearComments,(screenWidth - 830)/2,(screenWidth - 830)/2,(screenWidth - 830)/2)
                    .addComponent(btnSaveComments,(screenWidth - 830)/2,(screenWidth - 830)/2,(screenWidth - 830)/2)) 
            );

            pnlDataLayout.setVerticalGroup(
                pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlDataLayout.createSequentialGroup()
                    .addContainerGap(5,5)
                    .addComponent(pnlDataTable, screenHeight-320,screenHeight-320,screenHeight-320)
                    .addGap(5,5,5)
                    .addComponent(lblComments,25,25,25)
                    .addComponent(sclComments,100,100,100)
                    .addGroup(pnlDataLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(btnClearComments, 25,25,25)
                        .addGap(5,5,5)
                        .addComponent(btnSaveComments,25,25,25))
                    .addContainerGap(5,5))
            );
            
            //Setup image array
            locationImagesSmall_ = new HashMap<String,ImageIcon>(35);
            locationImagesLarge_ = new HashMap<String,ImageIcon>(35);
            char[] letters = {'A','B','C','D','E','F','G'};
            char[] numbers = {'0','1','2','3','4'};
            int count = 0;
            for(char letter : letters){
                for(char number: numbers){
                    ImageIcon icon = new ImageIcon(getClass().getResource("/trampoline/images/"+letter+number+".png"));
                    Image img = icon.getImage();
                    java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(60, 35, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics g = bi.createGraphics();
                    g.drawImage(img, 0,0, 60, 35, null);
                    locationImagesSmall_.put(""+letter+number, new ImageIcon(bi));
                    locationImagesLarge_.put(""+letter+number, icon);
                    count++;
                }
            }
           
        }
        
        heightTags = screenHeight - 665;
        GroupLayout pnlStartLayout = (GroupLayout)pnlStart.getLayout();  
        pnlStartLayout.setHorizontalGroup(
            pnlStartLayout.createSequentialGroup()
            .addGroup(pnlStartLayout.createSequentialGroup()
                .addContainerGap(5,5)
                .addGroup(pnlStartLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(labSelectTof,340, 340, 340)
                    .addComponent(drpDeviceName,340, 340, 340)
                    .addComponent(labGymnast,340, 340, 340)
                    .addComponent(drpDataGymnast,340, 340, 340)
                    .addComponent(labNumberOfBounces,340, 340, 350)
                    .addComponent(txtNumberOfBounces,50,50,50)
                    .addComponent(lblTags,340, 340, 340)
                    .addComponent(sclTags,340, 340, 340)
                    .addGroup(pnlStartLayout.createSequentialGroup()
                        .addComponent(btnCollectData,150,150,150)
                        .addComponent(btnClearData, 150,150,150))))
                .addContainerGap(5,5)
        );
        pnlStartLayout.setVerticalGroup(
            pnlStartLayout.createSequentialGroup()
            .addGroup(pnlStartLayout.createSequentialGroup()
                .addComponent(labSelectTof,25,25,25)
                .addGap(5,5,5)
                .addComponent(drpDeviceName,25,25,25)
                .addGap(5,5,5)
                .addComponent(labGymnast,25,25,25)
                .addGap(5,5,5)
                .addComponent(drpDataGymnast,25,25,25)
                .addGap(5,5,5)
                .addComponent(labNumberOfBounces,25,25,25)
                .addGap(5,5,5)
                .addComponent(txtNumberOfBounces,25,25,25)
                .addGap(5,5,5)
                .addComponent(lblTags,25,25,25)
                .addGap(5,5,5)
                .addComponent(sclTags, heightTags, heightTags, heightTags)
                .addGap(5,5,5)
                .addGroup(pnlStartLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnCollectData, 25, 25, 25)
                    .addComponent(btnClearData, 25, 25, 25))
                .addContainerGap(5,5))
        );
        
        //Make the labels that we require for the centre panel. 
        labelArray_ = new JLabel[50];
        Font labelTitleFont = new Font("Calibri", Font.PLAIN, 16);
        Font labelJumpNoFont = new Font("Calibri", Font.PLAIN, 16);
        
        pnlDataTable.setLayout(new GridLayout(11,5));
        
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
        
        JLabel lblLocation = new JLabel();
        lblLocation.setFont(labelTitleFont);
        lblLocation.setName("lblLocation");
        lblLocation.setText("Location:");
        pnlDataTable.add(lblLocation);
        
        //Create the labels for the Numbers themselves. 
        for (int i = 0; i < 10; i++) {
            labelArray_[i*5]   = new JLabel("labNumber"+i);
            labelArray_[i*5+1] = new JLabel("labTOF"+i);
            labelArray_[i*5+2] = new JLabel("labTON"+i);
            labelArray_[i*5+3] = new JLabel("labTotal"+i);
            labelArray_[i*5+4] = new JLabel("labLocation"+i);
        }
        
        //Then place the labels just created into the GridLayout. 
        for (int i = 0; i< 10; i++) {
            pnlDataTable.add(labelArray_[i*5]);
            labelArray_[i*5].setText("Jump "+(i+1)+":");
            labelArray_[i*5].setFont(labelJumpNoFont);
            //labelArray_[i*4].setVisible(false);
            
            pnlDataTable.add(labelArray_[i*5+1]);
            labelArray_[i*5+1].setText("ToF "+i);
            labelArray_[i*5+1].setFont(getFont("labelFont"));
            
            pnlDataTable.add(labelArray_[i*5+2]);
            labelArray_[i*5+2].setText("ToN "+i);
            labelArray_[i*5+2].setFont(getFont("labelFont"));
            
            pnlDataTable.add(labelArray_[i*5+3]);
            labelArray_[i*5+3].setText("Total "+i);
            labelArray_[i*5+3].setFont(getFont("labelFont"));
            
            pnlDataTable.add(labelArray_[i*5+4]);
            labelArray_[i*5+4].setText("");
            labelArray_[i*5+4].setFont(getFont("labelFont"));
        }
 
        labelArray_[4].setIcon(locationImagesSmall_.get("D2"));
        labelArray_[9].setIcon(locationImagesSmall_.get("E2"));
        labelArray_[14].setIcon(locationImagesSmall_.get("F1"));
        labelArray_[19].setIcon(locationImagesSmall_.get("G4"));
        labelArray_[24].setIcon(locationImagesSmall_.get("A3"));
        labelArray_[29].setIcon(locationImagesSmall_.get("B4"));
        labelArray_[34].setIcon(locationImagesSmall_.get("C1"));
        labelArray_[39].setIcon(locationImagesSmall_.get("E4"));
                
        pnlDataTable.repaint();
        
        // Setup Beam Status images
        lblTrampoline.setBounds(7, 20, 366, 217);
        
        this.beamStatusRedArray_ = new JLabel[16];
        this.beamStatusGreenArray_ = new JLabel[16];
        
        for(int i = 0; i < 16; i++){
            beamStatusRedArray_[i] = new JLabel("");
            beamStatusGreenArray_[i] = new JLabel("");
            beamStatusRedArray_[i].setIcon(new ImageIcon(getClass().getResource("/trampoline/images/redBeam.png")));
            beamStatusGreenArray_[i].setIcon(new ImageIcon(getClass().getResource("/trampoline/images/greenBeam.png")));
            layBeamStatus.add(beamStatusRedArray_[i], JLayeredPane.POPUP_LAYER);
            layBeamStatus.add(beamStatusGreenArray_[i], JLayeredPane.POPUP_LAYER);
            beamStatusGreenArray_[i].setVisible(false);
        }
        
        beamStatusRedArray_[0].setBounds(29, 160, 20, 20);
        beamStatusGreenArray_[0].setBounds(29, 160, 20, 20);
        beamStatusRedArray_[1].setBounds(325, 160, 20, 20);
        beamStatusGreenArray_[1].setBounds(325, 160, 20, 20);
        beamStatusRedArray_[2].setBounds(29, 120, 20, 20);
        beamStatusGreenArray_[2].setBounds(29, 120, 20, 20);
        beamStatusRedArray_[3].setBounds(325, 120, 20, 20);
        beamStatusGreenArray_[3].setBounds(325, 120, 20, 20);
        beamStatusRedArray_[4].setBounds(29, 80, 20, 20);
        beamStatusGreenArray_[4].setBounds(29, 80, 20, 20);
        beamStatusRedArray_[5].setBounds(325, 80, 20, 20);
        beamStatusGreenArray_[5].setBounds(325, 80, 20, 20);
        beamStatusRedArray_[6].setBounds(62, 40, 20, 20);
        beamStatusGreenArray_[6].setBounds(62, 40, 20, 20);
        beamStatusRedArray_[7].setBounds(62, 200, 20, 20);
        beamStatusGreenArray_[7].setBounds(62, 200, 20, 20);
        beamStatusRedArray_[8].setBounds(117, 40, 20, 20);
        beamStatusGreenArray_[8].setBounds(117, 40, 20, 20);
        beamStatusRedArray_[9].setBounds(117, 200, 20, 20);
        beamStatusGreenArray_[9].setBounds(117, 200, 20, 20);
        beamStatusRedArray_[10].setBounds(180, 40, 20, 20);
        beamStatusGreenArray_[10].setBounds(180, 40, 20, 20);
        beamStatusRedArray_[11].setBounds(180, 200, 20, 20);
        beamStatusGreenArray_[11].setBounds(180, 200, 20, 20);
        beamStatusRedArray_[12].setBounds(242, 40, 20, 20);
        beamStatusGreenArray_[12].setBounds(242, 40, 20, 20);
        beamStatusRedArray_[13].setBounds(242, 200, 20, 20);
        beamStatusGreenArray_[13].setBounds(242, 200, 20, 20);
        beamStatusRedArray_[14].setBounds(297, 40, 20, 20);
        beamStatusGreenArray_[14].setBounds(297, 40, 20, 20);
        beamStatusRedArray_[15].setBounds(297, 200, 20, 20);
        beamStatusGreenArray_[15].setBounds(297, 200, 20, 20);
        
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
        pnlGraph.setLayout(new java.awt.BorderLayout());
        pnlGraph.add(chartObject_, BorderLayout.CENTER);
        
        //Initially give values to avoid NullPointerExceptions
        chartValues = new double[10];
        chartNames  = new String[10];
        for (int i = 0; i < 10; i++) {
            chartValues[i] = 0;
            chartNames[i]  = "Bounce "+i;
        }
    }
    
    private void initStatisticsUI(){

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
        chartObjectStats_ = new Chart(values, names, "title ststs");
        //Set the two panels to have the appropriate layouts and add the charts. 
        pnlStatisticsSmall.setLayout(new java.awt.BorderLayout());
        pnlStatisticsSmall.add(chartObjectStats_, BorderLayout.CENTER);
    }
    
    private void initImportExportUI(){
        
    }
    
    private void initClubManagementUI(){
        int screenHeight = this.getMaximizedBounds().height;
        int screenWidth = this.getMaximizedBounds().width;
        
        
        GroupLayout pnlClubManagementLayout = (GroupLayout)pnlClubManagement.getLayout();  
        pnlClubManagementLayout.setHorizontalGroup(
            pnlClubManagementLayout.createSequentialGroup()
            .addContainerGap(5,5)    
            .addGroup(pnlClubManagementLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                .addGroup(pnlClubManagementLayout.createSequentialGroup()
                    .addComponent(pnlGymnast,550,550,550)
                    .addGap(15,15,15)
                    .addComponent(lblClubManagementInfo,400,400,400))
                .addComponent(pnlGymnastDetails,screenWidth-20,screenWidth-20,screenWidth-20)
                .addComponent(pnlRoutines,screenWidth-20,screenWidth-20,screenWidth-20)
                .addComponent(pnlAdmin,screenWidth-20,screenWidth-20,screenWidth-20))
            .addContainerGap(5,5)  
        );

        pnlClubManagementLayout.setVerticalGroup(
            pnlClubManagementLayout.createSequentialGroup()
            .addContainerGap(5,5)
            .addGroup(pnlClubManagementLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                .addComponent(pnlGymnast, 85, 85, 85)
                .addComponent(lblClubManagementInfo,85,85,85))
            .addComponent(pnlGymnastDetails,200,200,200)
            .addComponent(pnlRoutines, screenHeight - 470, screenHeight - 470, screenHeight - 470)
            .addComponent(pnlAdmin,85,85,85)
            .addContainerGap(5,5)
        );
        
        GroupLayout pnlGymnastLayout = (GroupLayout)pnlGymnast.getLayout();
        pnlGymnastLayout.setHorizontalGroup(
            pnlGymnastLayout.createSequentialGroup()
            .addContainerGap(5,5)
            .addComponent(lblGymnast,60,60,60)
            .addGap(5,5,5)
            .addComponent(drpGymnastName,300,300,300)
            .addGap(20,20,20)    
            .addComponent(btnAddGymnast,115,115,115)   
            .addContainerGap(5,5)
        );
        
        pnlGymnastLayout.setVerticalGroup(
            pnlGymnastLayout.createSequentialGroup()
            .addContainerGap(5,5)
            .addGroup(pnlGymnastLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                .addComponent(lblGymnast,25,25,25)
                .addComponent(drpGymnastName,25,25,25)
                .addComponent(btnAddGymnast,25,25,25))
            .addContainerGap(5,5)
        );
        
        GroupLayout pnlAdminLayout = (GroupLayout)pnlAdmin.getLayout();
        pnlAdminLayout.setHorizontalGroup(
            pnlAdminLayout.createSequentialGroup()
             .addContainerGap(5,5)
             .addComponent(lblNewPassword,90,90,90)
             .addGap(5,5,5)
             .addComponent(txtPassword1,150,150,150)
             .addGap(15,15,15)
             .addComponent(lblNewPassword2,100,100,100)
             .addGap(5,5,5)
             .addComponent(txtPassword2,150,150,150)
             .addGap(5,5,5)
             .addComponent(btnNewPassword,120,120,120)
             .addGap(screenWidth - 900,screenWidth - 900,screenWidth - 900)
             .addComponent(btnResetAll,120,120,120)
             .addGap(5,5,5)
             .addComponent(btnLogout,80,80,80)
             .addContainerGap(5,5)   
        );
        
        pnlAdminLayout.setVerticalGroup(
            pnlAdminLayout.createSequentialGroup()
            .addContainerGap(5,5)
            .addGroup(pnlAdminLayout.createParallelGroup(GroupLayout.Alignment.LEADING,false)
                .addComponent(lblNewPassword,25,25,25)
                .addComponent(txtPassword1,25,25,25)
                .addComponent(lblNewPassword2,25,25,25)
                .addComponent(txtPassword2,25,25,25)
                .addComponent(btnNewPassword,25,25,25)
                .addComponent(btnResetAll,25,25,25)
                .addComponent(btnLogout,25,25,25))
            .addContainerGap(5,5)
        );
        
         //Set the numbers for the date of birth entries on Club Management. 
        for (int k = 1; k <= 31; k++) {
            drpDate.addItem(k);
        }
        
        //And month on Club Management.
        String[] monthName = {"January", "February","March", "April", "May", "June", "July","August", "September", "October", "November","December"};
        for (int l = 1; l <= 12; l++) {
            drpMonth.addItem(l);
        }
        
        //Finally the year on Club Management. 
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date date = new Date();
        int yearStart = Integer.parseInt(dateFormat.format(date));
        for (int l = 1900; l <= yearStart; l++) {
            drpYear.addItem(l);
        }
        
        //Category on Club Management. 
        String[] categoryName = {"F.I.G. A","F.I.G. B","National C","Regional D","Regional E","Regional F","Regional G","Club H","Club I"};
        for (int l = 1; l <= categoryName.length; l++) {
            drpCategory.addItem(new ComboItem(Integer.toString(l), categoryName[l-1]));
        }
         
        updateGymnastDropDown();
        
        //Make club management tab invisible
        pnlGymnast.setVisible(false);
        pnlGymnastDetails.setVisible(false);
        pnlRoutines.setVisible(false);
        pnlAdmin.setVisible(false);
        lblClubManagementInfo.setVisible(false);
    }
            
    private void initGeneralUI() { 
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        this.screenResolution_ = toolkit.getScreenSize();
        this.setSize(screenResolution_);
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chronos");
        tabPane.setBackground(new Color(255, 255, 255));
        tabPane.setBackground(new Color(255, 255, 255));
        
        layMainLayer.setSize(screenResolution_);
        tabPane.setSize(screenResolution_);
        pnlToF.setSize(screenResolution_);
        pnlStatistics.setSize(screenResolution_);
        pnlImportExport.setSize(screenResolution_);
        pnlClubManagement.setSize(screenResolution_);
        
        
        GraphicsEnvironment env =
        GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.setMaximizedBounds(env.getMaximumWindowBounds());
        this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH); 
        
        //Set size of list of tags
        int noItems = lstTags.getModel().getSize()*35;
        System.out.println(noItems);
        lstTags.setPreferredSize(new Dimension(128,noItems));
        lstTags.setSize(new Dimension (128,noItems));
        
        lblError.setText("");
        lblError.setCursor(new Cursor(java.awt.Cursor.HAND_CURSOR));
        lblError.setFont(getFont("errorFont"));
    }

    private void initDatabase(){
        db_ = new DBConnect(errorHandler_);
        adminAccessGranted_ = false;
        currentRoutineId_ = 0;
        
        // Change to retrieve from database
        adminPassword_ = "21232F297A57A5A743894A0E4A801FC3";
        
    }
    
    public Font getFont(String s){
        Font returnFont = new Font("Verdana", Font.PLAIN, 1);
        
        if(s.equals("labelFont")){
            returnFont = new Font("Verdana", Font.PLAIN, 12);
        }
        
        if(s.equals("dropdownFont")){
            returnFont = new Font("Verdana", Font.PLAIN, 12);
        }
        
        if(s.equals("scrollboxFont")){
            returnFont = new Font("Verdana", Font.PLAIN, 12);
        }
        
        if(s.equals("buttonFont")){
            returnFont = new Font("Verdana", Font.PLAIN, 12);
        }
        
        if(s.equals("borderFont")){
                returnFont = new Font("Verdana", Font.BOLD, 14);
        }
        
        if(s.equals("errorFont")){
                returnFont = new Font("Verdana", Font.BOLD, 16);
        }
        
        return returnFont;
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
        pageRefreshTimer.stop();
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
        JComboBox[] boxesToUpdate = {selStatsGymnast, drpDataGymnast, drpGymnastName};
        System.out.println("update gropdown");
        Gymnast[] gymnastList = db_.getAllGymnasts();
        System.out.println("finished");
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
        //Routine r = new Routine(jumpList);
        //updateStatistics(r);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        layMainLayer = new javax.swing.JLayeredPane();
        tabPane = new javax.swing.JTabbedPane();
        pnlToF = new javax.swing.JPanel();
        pnlStart = new javax.swing.JPanel();
        btnCollectData = new javax.swing.JButton();
        txtNumberOfBounces = new javax.swing.JTextField();
        labNumberOfBounces = new javax.swing.JLabel();
        drpDeviceName = new javax.swing.JComboBox();
        labSelectTof = new javax.swing.JLabel();
        labGymnast = new javax.swing.JLabel();
        drpDataGymnast = new javax.swing.JComboBox();
        sclTags = new javax.swing.JScrollPane();
        lstTags = new javax.swing.JList();
        lblTags = new javax.swing.JLabel();
        btnClearData = new javax.swing.JButton();
        pnlData = new javax.swing.JPanel();
        btnSaveComments = new javax.swing.JButton();
        pnlDataTable = new javax.swing.JPanel();
        lblComments = new javax.swing.JLabel();
        btnClearComments = new javax.swing.JButton();
        sclComments = new javax.swing.JScrollPane();
        txtComments = new javax.swing.JTextArea();
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
        pnlClubManagement = new javax.swing.JPanel();
        pnlGymnast = new javax.swing.JPanel();
        lblGymnast = new javax.swing.JLabel();
        drpGymnastName = new javax.swing.JComboBox();
        btnAddGymnast = new javax.swing.JButton();
        pnlAdmin = new javax.swing.JPanel();
        lblNewPassword = new javax.swing.JLabel();
        lblNewPassword2 = new javax.swing.JLabel();
        txtPassword2 = new javax.swing.JTextField();
        txtPassword1 = new javax.swing.JTextField();
        btnResetAll = new javax.swing.JButton();
        btnNewPassword = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlGymnastDetails = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblDoB = new javax.swing.JLabel();
        drpDate = new javax.swing.JComboBox();
        drpMonth = new javax.swing.JComboBox();
        drpYear = new javax.swing.JComboBox();
        lblCategory = new javax.swing.JLabel();
        drpCategory = new javax.swing.JComboBox();
        btnAddModifyUser = new javax.swing.JButton();
        btnDeleteUser = new javax.swing.JButton();
        pnlRoutines = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        lblClubManagementInfo = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        menBar = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chronos");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                TrampolineUIWindowOpened(evt);
            }
        });

        layMainLayer.setPreferredSize(new java.awt.Dimension(1280, 1024));

        tabPane.setBackground(new java.awt.Color(255, 255, 255));
        tabPane.setPreferredSize(new java.awt.Dimension(1280, 1024));
        tabPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tabPaneFocusGained(evt);
            }
        });

        pnlToF.setPreferredSize(new java.awt.Dimension(1280, 1024));

        pnlStart.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Start Bouncing", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 14))); // NOI18N

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

        drpDeviceName.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        drpDeviceName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drpDeviceNameActionPerformed(evt);
            }
        });

        labSelectTof.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
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
        sclTags.setViewportView(lstTags);

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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlStartLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(drpDataGymnast, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sclTags, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(drpDeviceName, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlStartLayout.createSequentialGroup()
                        .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlStartLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(lblTags))
                            .addGroup(pnlStartLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(labNumberOfBounces))
                            .addGroup(pnlStartLayout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(btnCollectData)
                                .addGap(38, 38, 38)
                                .addComponent(btnClearData, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlStartLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlStartLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labGymnast))
                            .addGroup(pnlStartLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(labSelectTof, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 31, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlStartLayout.setVerticalGroup(
            pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStartLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(labSelectTof)
                .addGap(7, 7, 7)
                .addComponent(drpDeviceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labGymnast)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(drpDataGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labNumberOfBounces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNumberOfBounces, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblTags)
                .addGap(11, 11, 11)
                .addComponent(sclTags, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlStartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCollectData, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearData, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        pnlData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        btnSaveComments.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        btnSaveComments.setText("Save Comments");
        btnSaveComments.setName("");
        btnSaveComments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveCommentsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlDataTableLayout = new javax.swing.GroupLayout(pnlDataTable);
        pnlDataTable.setLayout(pnlDataTableLayout);
        pnlDataTableLayout.setHorizontalGroup(
            pnlDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 252, Short.MAX_VALUE)
        );
        pnlDataTableLayout.setVerticalGroup(
            pnlDataTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 191, Short.MAX_VALUE)
        );

        lblComments.setText("Comments:");

        btnClearComments.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        btnClearComments.setText("Clear Comments");
        btnClearComments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearCommentsActionPerformed(evt);
            }
        });

        sclComments.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        txtComments.setColumns(20);
        txtComments.setLineWrap(true);
        txtComments.setRows(5);
        sclComments.setViewportView(txtComments);

        javax.swing.GroupLayout pnlDataLayout = new javax.swing.GroupLayout(pnlData);
        pnlData.setLayout(pnlDataLayout);
        pnlDataLayout.setHorizontalGroup(
            pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataLayout.createSequentialGroup()
                .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDataLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlDataTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDataLayout.createSequentialGroup()
                        .addGap(85, 85, 85)
                        .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSaveComments)
                            .addComponent(btnClearComments))))
                .addContainerGap(201, Short.MAX_VALUE))
            .addGroup(pnlDataLayout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addGroup(pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sclComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblComments))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlDataLayout.setVerticalGroup(
            pnlDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDataLayout.createSequentialGroup()
                .addComponent(pnlDataTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblComments)
                .addGap(69, 69, 69)
                .addComponent(sclComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(btnClearComments)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSaveComments)
                .addGap(176, 176, 176))
        );

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

        pnlGraph.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Graph", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N
        pnlGraph.setPreferredSize(new java.awt.Dimension(500, 500));

        javax.swing.GroupLayout pnlGraphLayout = new javax.swing.GroupLayout(pnlGraph);
        pnlGraph.setLayout(pnlGraphLayout);
        pnlGraphLayout.setHorizontalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 363, Short.MAX_VALUE)
        );
        pnlGraphLayout.setVerticalGroup(
            pnlGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        layBeamStatus.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Beam Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Calibri", 1, 18))); // NOI18N

        lblTrampoline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/trampoline/images/eurotramp.png"))); // NOI18N
        lblTrampoline.setBounds(10, 40, 360, 217);
        layBeamStatus.add(lblTrampoline, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout pnlToFLayout = new javax.swing.GroupLayout(pnlToF);
        pnlToF.setLayout(pnlToFLayout);
        pnlToFLayout.setHorizontalGroup(
            pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToFLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(layBeamStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                    .addComponent(pnlStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlStats, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlToFLayout.setVerticalGroup(
            pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlToFLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlToFLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlToFLayout.createSequentialGroup()
                        .addComponent(pnlStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlGraph, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE))
                    .addGroup(pnlToFLayout.createSequentialGroup()
                        .addComponent(pnlStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(layBeamStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlData, javax.swing.GroupLayout.PREFERRED_SIZE, 632, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(977, Short.MAX_VALUE))
        );

        tabPane.addTab("Time of Flight", pnlToF);

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

        selStatsGymnast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selStatsGymnastActionPerformed(evt);
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
                .addContainerGap(427, Short.MAX_VALUE))
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
                .addContainerGap(1455, Short.MAX_VALUE))
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
                    .addComponent(pnlStatisticsSmall, javax.swing.GroupLayout.DEFAULT_SIZE, 1600, Short.MAX_VALUE))
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

        rdoExportCsv.setText("CSV");

        btnExportUser.setText("Export User");

        rdoExportText.setText("Text");

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
                .addContainerGap(625, Short.MAX_VALUE))
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
                .addContainerGap(1445, Short.MAX_VALUE))
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

        pnlClubManagement.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                pnlClubManagementComponentShown(evt);
            }
        });
        pnlClubManagement.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pnlClubManagementFocusGained(evt);
            }
        });

        pnlGymnast.setBorder(javax.swing.BorderFactory.createTitledBorder("Gymnast"));

        lblGymnast.setText("Gymnast:");

        drpGymnastName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drpGymnastNameActionPerformed(evt);
            }
        });

        btnAddGymnast.setText("Add New User");

        javax.swing.GroupLayout pnlGymnastLayout = new javax.swing.GroupLayout(pnlGymnast);
        pnlGymnast.setLayout(pnlGymnastLayout);
        pnlGymnastLayout.setHorizontalGroup(
            pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGymnastLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblGymnast)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(drpGymnastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(btnAddGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(480, Short.MAX_VALUE))
        );
        pnlGymnastLayout.setVerticalGroup(
            pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGymnastLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGymnastLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGymnast)
                    .addComponent(drpGymnastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddGymnast))
                .addContainerGap(65, Short.MAX_VALUE))
        );

        pnlAdmin.setBorder(javax.swing.BorderFactory.createTitledBorder("Administration"));

        lblNewPassword.setText("New Password:");

        lblNewPassword2.setText("Retype Password:");

        btnResetAll.setText("Reset All Data");
        btnResetAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetAllActionPerformed(evt);
            }
        });

        btnNewPassword.setText("New Password");

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAdminLayout = new javax.swing.GroupLayout(pnlAdmin);
        pnlAdmin.setLayout(pnlAdminLayout);
        pnlAdminLayout.setHorizontalGroup(
            pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdminLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblNewPassword)
                .addGap(32, 32, 32)
                .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(69, 69, 69)
                .addComponent(lblNewPassword2)
                .addGap(37, 37, 37)
                .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNewPassword)
                .addGap(99, 99, 99)
                .addComponent(btnResetAll)
                .addGap(52, 52, 52)
                .addComponent(btnLogout)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAdminLayout.setVerticalGroup(
            pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdminLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblNewPassword)
                .addComponent(txtPassword1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnResetAll)
                .addComponent(btnNewPassword)
                .addComponent(txtPassword2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblNewPassword2)
                .addComponent(btnLogout))
        );

        pnlGymnastDetails.setBorder(javax.swing.BorderFactory.createTitledBorder("Gymnast Details"));

        lblName.setText("Name:");

        lblDoB.setText("DoB:");

        lblCategory.setText("Category:");

        btnAddModifyUser.setText("Add / Modify User");
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

        javax.swing.GroupLayout pnlGymnastDetailsLayout = new javax.swing.GroupLayout(pnlGymnastDetails);
        pnlGymnastDetails.setLayout(pnlGymnastDetailsLayout);
        pnlGymnastDetailsLayout.setHorizontalGroup(
            pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblName)
                            .addComponent(lblDoB))
                        .addGap(36, 36, 36)
                        .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                                .addComponent(drpDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(drpMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(drpYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(444, 444, 444)
                                .addComponent(btnAddModifyUser))
                            .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(101, 101, 101)
                                .addComponent(lblCategory)
                                .addGap(18, 18, 18)
                                .addComponent(drpCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(btnDeleteUser, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlGymnastDetailsLayout.setVerticalGroup(
            pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGymnastDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCategory)
                    .addComponent(drpCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlGymnastDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDoB)
                    .addComponent(drpDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(drpMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(drpYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddModifyUser))
                .addGap(18, 18, 18)
                .addComponent(btnDeleteUser)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlRoutines.setBorder(javax.swing.BorderFactory.createTitledBorder("Routines"));

        javax.swing.GroupLayout pnlRoutinesLayout = new javax.swing.GroupLayout(pnlRoutines);
        pnlRoutines.setLayout(pnlRoutinesLayout);
        pnlRoutinesLayout.setHorizontalGroup(
            pnlRoutinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRoutinesLayout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlRoutinesLayout.setVerticalGroup(
            pnlRoutinesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRoutinesLayout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblClubManagementInfo.setText("TEST INFORMATION IN MESSAGE BOX");

        javax.swing.GroupLayout pnlClubManagementLayout = new javax.swing.GroupLayout(pnlClubManagement);
        pnlClubManagement.setLayout(pnlClubManagementLayout);
        pnlClubManagementLayout.setHorizontalGroup(
            pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClubManagementLayout.createSequentialGroup()
                .addGroup(pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlGymnastDetails, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlClubManagementLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlRoutines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlAdmin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnlClubManagementLayout.createSequentialGroup()
                        .addComponent(pnlGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addComponent(lblClubManagementInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 232, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlClubManagementLayout.setVerticalGroup(
            pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClubManagementLayout.createSequentialGroup()
                .addGroup(pnlClubManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlClubManagementLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlGymnast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlClubManagementLayout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(lblClubManagementInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(210, 210, 210)
                .addComponent(pnlGymnastDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlRoutines, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlAdmin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(739, Short.MAX_VALUE))
        );

        tabPane.addTab("Club Management", pnlClubManagement);

        tabPane.setBounds(0, 10, 1280, 1650);
        layMainLayer.add(tabPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblError.setFont(new java.awt.Font("Calibri", 1, 18)); // NOI18N
        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblErrorMouseClicked(evt);
            }
        });
        lblError.setBounds(460, 10, 420, 20);
        layMainLayer.add(lblError, javax.swing.JLayeredPane.POPUP_LAYER);

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
            .addComponent(layMainLayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(layMainLayer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCommentsActionPerformed
        // CURRENTLY THE ROUTINE ID OF DISPLAYED ROUTINE IS IN THE VARIABLE currentRoutineId_
    }//GEN-LAST:event_btnSaveCommentsActionPerformed

    private void btnCollectDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollectDataActionPerformed
        this.btnClearDataActionPerformed(evt);
        if(this.currentInterface_!=null){
            this.currentInterface_.collectBounces(Integer.parseInt(txtNumberOfBounces.getText()), this.db_, 1);
            refresh = REFRESH_TIME;
            nextJumpToFill = 1;
            btnSaveComments.setVisible(false);
            btnClearComments.setVisible(false);
            txtComments.setVisible(false);
            lblComments.setVisible(false);
            jumpTimer.start();
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
        if (drpGymnastName.getSelectedIndex() == 0) {
            if (txtName.getText() == "") {
                lblClubManagementInfo.setText("Please give a name for the gymnast.");
            } else {
                //Then we need to add the gymnast. Start by entering the information into the databse. 
                db_.addGymnast(txtName.getText(), Integer.parseInt(drpDate.getSelectedItem().toString()), Integer.parseInt(drpMonth.getSelectedItem().toString()), Integer.parseInt(drpYear.getSelectedItem().toString()), drpCategory.getSelectedItem().toString());

                //Add a success message.
                lblClubManagementInfo.setText("The Gymnast '"+txtName.getText()+"' has been added.");

                //Then clear all the items. 
                txtName.setText("");
                drpDate.setSelectedIndex(0);
                drpMonth.setSelectedIndex(0);
                drpYear.setSelectedIndex(0);
                drpCategory.setSelectedIndex(0);

                //Re-update the drop-down.
                updateGymnastDropDown();
            }
        } else {
            //Then we need to edit the gymnast. 
            ComboItem gymnastItem = (ComboItem) drpGymnastName.getSelectedItem();

            //Note that we have to take 1 off the date and month because of offsets. 
            db_.editGymnast(gymnastItem.getNumericID(), txtName.getText(), Integer.parseInt(drpDate.getSelectedItem().toString())-1, Integer.parseInt(drpMonth.getSelectedItem().toString())-1, Integer.parseInt(drpYear.getSelectedItem().toString()), drpCategory.getSelectedItem().toString(), 1);
            lblClubManagementInfo.setText("The Gymnast '"+txtName.getText()+"' has been edited.");

            //Then clear all the items. 
            txtName.setText("");
            drpDate.setSelectedIndex(0);
            drpMonth.setSelectedIndex(0);
            drpYear.setSelectedIndex(0);
            drpCategory.setSelectedIndex(0);

            //Re-update the drop-down.
            updateGymnastDropDown();
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

    private void btnClearCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearCommentsActionPerformed
        txtComments.setText("");
    }//GEN-LAST:event_btnClearCommentsActionPerformed

    private void btnClearDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearDataActionPerformed
        for(int i=0;i<10;i++){
            labelArray_[i*5].setVisible(false);
            labelArray_[i*5+1].setText("");
            labelArray_[i*5+2].setText("");
            labelArray_[i*5+3].setText("");
            labelArray_[i*5+4].setText("");
        }
    }//GEN-LAST:event_btnClearDataActionPerformed

    private void btnStatisticsUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatisticsUpdateActionPerformed
        
        //System.out.println("before jump");
        //db_.getJump(3);
        //System.out.println("after jump");
        updateGymnastDropDown();
    }//GEN-LAST:event_btnStatisticsUpdateActionPerformed

    private void pnlClubManagementFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pnlClubManagementFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_pnlClubManagementFocusGained

    private void pnlClubManagementComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_pnlClubManagementComponentShown
        // TODO add your handling code here:
        if(this.adminAccessGranted_){
            pnlGymnast.setVisible(true);
            pnlGymnastDetails.setVisible(true);
            pnlRoutines.setVisible(true);
            pnlAdmin.setVisible(true);
            lblClubManagementInfo.setVisible(true);
        }else{
            PasswordPrompt passwordPopup = new PasswordPrompt(this, true, adminPassword_);
            passwordPopup.setVisible(true);
            //switch(passwordPopup.getReturnStatus()){
            switch(1){
                case 0:
                    //BAD PASSWORD
                    tabPane.setSelectedIndex(0);
                    pnlGymnast.setVisible(false);
                    pnlGymnastDetails.setVisible(false);
                    pnlRoutines.setVisible(false);
                    pnlAdmin.setVisible(false);
                    lblClubManagementInfo.setVisible(false);
                    this.errorHandler_.setError(1);
                    break;
                case 1:
                    this.adminAccessGranted_ = true;
                    pnlGymnast.setVisible(true);
                    pnlGymnastDetails.setVisible(true);
                    pnlRoutines.setVisible(true);
                    pnlAdmin.setVisible(true);
                    lblClubManagementInfo.setVisible(true);
                    this.errorHandler_.clearError();
                    break;
            }
        }
    }//GEN-LAST:event_pnlClubManagementComponentShown

    private void TrampolineUIWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_TrampolineUIWindowOpened
        // TODO add your handling code here:
    }//GEN-LAST:event_TrampolineUIWindowOpened

    private void lblErrorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblErrorMouseClicked
        if(errorHandler_.isError()){
            JOptionPane.showMessageDialog(this,this.errorHandler_.getCurrentErrorLong(),this.errorHandler_.getCurrentErrorShort(), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_lblErrorMouseClicked

    private void btnDeleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteUserActionPerformed
        ComboItem c = (ComboItem) drpGymnastName.getSelectedItem();
        int confirmInt = (int) JOptionPane.showConfirmDialog(pnlStatistics, "Are you sure you want to Delete '"+c+"'?", "Delete User", 0, 0);
        
        if (confirmInt == 0) {
            lblClubManagementInfo.setText("The Gymnast '"+c+"' has been deleted.");
            db_.deleteGymnast(c.getNumericID());
        }
        
        updateGymnastDropDown();
    }//GEN-LAST:event_btnDeleteUserActionPerformed

    private void drpGymnastNameActionPerformed(java.awt.event.ActionEvent evt) {                                               
        ComboItem c = (ComboItem) drpGymnastName.getSelectedItem();
        
        System.out.println(c+"herp");
        if (c != null) {
            if (c.getNumericID() == 0) {
                btnAddModifyUser.setText("Add User");
                txtName.setText("");
                drpDate.setSelectedIndex(0);
                drpMonth.setSelectedIndex(0);
                drpYear.setSelectedIndex(0);
                drpCategory.setSelectedIndex(0);
            } else {
                Gymnast g = db_.getGymnast(c.getNumericID());
                txtName.setText(g.getName());

                drpDate.setSelectedIndex(g.getDobDay());
                drpMonth.setSelectedIndex(g.getDobMonth());
                drpYear.setSelectedIndex(g.getDobYear()-1900);
                drpCategory.setSelectedIndex(g.getCategory());
                btnAddModifyUser.setText("Modify User");
            }
        }
    }                                           

    private void selStatsGymnastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selStatsGymnastActionPerformed
        //Get the selected gymnast. 
        ComboItem c = (ComboItem) selStatsGymnast.getSelectedItem();
        
        if (c != null && c.getNumericID() != 0) {
            Gymnast g = db_.getGymnast(c.getNumericID());

            //Update the routine drop-down. 
            Routine[] routineList = db_.getRoutinesForGymnast(g.getID());
            drpStatsRoutine.removeAllItems();
            for (Routine r:routineList) {
                drpStatsRoutine.addItem(new ComboItem(r.getID(), "ID: "+r.getID()));
            }
        }
    }//GEN-LAST:event_selStatsGymnastActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        tabPane.setSelectedIndex(0);
        pnlGymnast.setVisible(false);
        pnlGymnastDetails.setVisible(false);
        pnlRoutines.setVisible(false);
        pnlAdmin.setVisible(false);
        lblClubManagementInfo.setVisible(false);
        this.adminAccessGranted_ = false;
        this.errorHandler_.setError(4);
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnResetAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetAllActionPerformed
        
        //Password currently hardcoded to "MasterReset" -> to be retrived from database later
        ResetAllConfirm resetDialog = new ResetAllConfirm(this, true, "D642FBD7CACF27F7568A6ED701AA44D8");
        resetDialog.setVisible(true);
        switch(resetDialog.getReturnStatus()){
            case 0:
                // Bad password
                lblClubManagementInfo.setText("Error: Incorrect Password Supplied for Master Reset");
                break;
            case 1:
                //Correct password
                Object[] options = {"Yes","No"};
                int finalConfirm = JOptionPane.showOptionDialog(this,"Continuing will erase ALL DATA stored\n"
                    + "in the database. Do you wish to continue?\n","Final Confirm of Master Reset" ,
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

                if(finalConfirm == JOptionPane.YES_OPTION){
                    //Confirmed to delete all the database data
                    lblClubManagementInfo.setText("Database successfully purged of all data.");
                }else{
                    lblClubManagementInfo.setText("Master reset aborted.");
                }
                break;
                //Display popup for confirmation
        }   
    }//GEN-LAST:event_btnResetAllActionPerformed
   
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
        JFrame.setDefaultLookAndFeelDecorated(true);
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
    private javax.swing.JButton btnAddGymnast;
    private javax.swing.JButton btnAddModifyUser;
    private javax.swing.JButton btnClearComments;
    private javax.swing.JButton btnClearData;
    private javax.swing.JButton btnCollectData;
    private javax.swing.JButton btnDeleteUser;
    private javax.swing.JButton btnExportUser;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnNewPassword;
    private javax.swing.JButton btnReadFile;
    private javax.swing.JButton btnResetAll;
    private javax.swing.JButton btnSaveComments;
    private javax.swing.JButton btnStatisticsUpdate;
    private javax.swing.JComboBox drpCategory;
    private javax.swing.JComboBox drpDataGymnast;
    private javax.swing.JComboBox drpDate;
    private javax.swing.JComboBox drpDeviceName;
    private javax.swing.JComboBox drpGymnastName;
    private javax.swing.JComboBox drpMonth;
    private javax.swing.JComboBox drpStatsRoutine;
    private javax.swing.JComboBox drpYear;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel labGymnast;
    private javax.swing.JLabel labNumberOfBounces;
    private javax.swing.JLabel labSelectTof;
    private javax.swing.JLayeredPane layBeamStatus;
    private javax.swing.JLayeredPane layMainLayer;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblClubManagementInfo;
    private javax.swing.JLabel lblComments;
    private javax.swing.JLabel lblDoB;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblGymnast;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNewPassword;
    private javax.swing.JLabel lblNewPassword2;
    private javax.swing.JLabel lblStatsGymnast;
    private javax.swing.JLabel lblStatsRoutine;
    private javax.swing.JLabel lblTags;
    private javax.swing.JLabel lblTrampoline;
    private javax.swing.JList lstTags;
    private javax.swing.JMenuBar menBar;
    private javax.swing.JPanel pnlAdmin;
    private javax.swing.JPanel pnlClubManagement;
    private javax.swing.JPanel pnlData;
    private javax.swing.JPanel pnlDataTable;
    private javax.swing.JPanel pnlExport;
    private javax.swing.JPanel pnlGraph;
    private javax.swing.JPanel pnlGymnast;
    private javax.swing.JPanel pnlGymnastDetails;
    private javax.swing.JPanel pnlImport;
    private javax.swing.JPanel pnlImportExport;
    private javax.swing.JPanel pnlRoutines;
    private javax.swing.JPanel pnlStart;
    private javax.swing.JPanel pnlStatistics;
    private javax.swing.JPanel pnlStatisticsSmall;
    private javax.swing.JPanel pnlStats;
    private javax.swing.JPanel pnlToF;
    private javax.swing.JRadioButton rdoExportCsv;
    private javax.swing.JRadioButton rdoExportExcel;
    private javax.swing.JRadioButton rdoExportText;
    private javax.swing.JScrollPane sclComments;
    private javax.swing.JScrollPane sclTags;
    private javax.swing.JComboBox selStatsGymnast;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTextArea txtComments;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtNumberOfBounces;
    private javax.swing.JTextField txtPassword1;
    private javax.swing.JTextField txtPassword2;
    // End of variables declaration//GEN-END:variables
}
