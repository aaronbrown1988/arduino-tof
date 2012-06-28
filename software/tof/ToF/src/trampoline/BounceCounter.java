/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author Kieran
 */
public class BounceCounter extends JFrame {
    /**
     * Creates new form BounceCounter
     */
    private Timer imageTimer_;
    private String[] imageString_;
    private int imageWidth_;
    private int imageHeight_;
    private Random randomGen_;
    private int timeCheck_;
    
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".JPG") || name.endsWith(".JPEG"));
        }
    };
    
    ActionListener imageAction = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            setImage(randomGen_.nextInt(imageString_.length));
            if (timeCheck_==3){
                updateTime();                
                timeCheck_ = 0;
            }else{
                timeCheck_++;
            }
        }
    };
    
    public BounceCounter() {
        
        this.setVisible(true);
        setBackground(new java.awt.Color(0, 0, 0));
        setForeground(new java.awt.Color(0, 0, 0));
        
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.setMaximizedBounds(env.getMaximumWindowBounds());
        this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH); 
                
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenResolution = toolkit.getScreenSize();
        this.setSize(screenResolution);
        this.setPreferredSize(screenResolution);
        
        lblTime = new javax.swing.JLabel();
        lblImage = new javax.swing.JLabel();
        lblTop = new javax.swing.JLabel();
        lbltop1 = new javax.swing.JLabel();
        lbltop2 = new javax.swing.JLabel();
        lbltop3 = new javax.swing.JLabel();
        lbltop4 = new javax.swing.JLabel();
        lblTop5 = new javax.swing.JLabel();
        lblHour5 = new javax.swing.JLabel();
        lblHour4 = new javax.swing.JLabel();
        lblHour3 = new javax.swing.JLabel();
        lblHour1 = new javax.swing.JLabel();
        lblHour2 = new javax.swing.JLabel();
        lblHour = new javax.swing.JLabel();

        lblTime.setText("");
        lblImage.setText("");
        lblTop.setText("Top Bouncers:");
        lbltop1.setText("first");
        lbltop2.setText("second");
        lbltop3.setText("third");
        lbltop4.setText("fourth");
        lblTop5.setText("fifth");
        lblHour5.setText("fifth");
        lblHour4.setText("fourth");
        lblHour3.setText("third");
        lblHour1.setText("first");
        lblHour2.setText("second");
        lblHour.setText("Top Bouncers this Hour:");
        lblTime.setFont(new java.awt.Font("Calibri", 1, 36)); // NOI18N
        lblTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTime.setForeground(new java.awt.Color(255, 255, 255));
        
        int screenHeight = this.getMaximizedBounds().height;
        int screenWidth = this.getMaximizedBounds().width;
        imageWidth_ = screenWidth - 20;
        imageHeight_ = screenHeight - 150;
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()   
                .addContainerGap(5,5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTime, screenWidth - 20,screenWidth - 20,screenWidth - 20)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblImage, imageWidth_, imageWidth_, imageWidth_)))
                .addContainerGap(5,5)
        );
 
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblImage, imageHeight_, imageHeight_, imageHeight_))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        
        /*javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()   
                .addContainerGap(5,5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTime, screenWidth - 20,screenWidth - 20,screenWidth - 20)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblImage, imageWidth_, imageWidth_, imageWidth_)
                        .addGap(5,5,5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTop,250,250,250)
                            .addComponent(lbltop1,250,250,250)
                            .addComponent(lbltop2,250,250,250)
                            .addComponent(lbltop3,250,250,250)
                            .addComponent(lbltop4,250,250,250)
                            .addComponent(lblTop5,250,250,250)
                            .addComponent(lblHour,250,250,250)
                            .addComponent(lblHour1,250,250,250)
                            .addComponent(lblHour2,250,250,250)
                            .addComponent(lblHour3,250,250,250)
                            .addComponent(lblHour4,250,250,250)
                            .addComponent(lblHour5,250,250,250))))
                .addContainerGap(5,5)
        );
 
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblImage, imageHeight_, imageHeight_, imageHeight_)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTop, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTop5)
                        .addGap(110, 110, 110)
                        .addComponent(lblHour, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour5)))
                .addContainerGap(32, Short.MAX_VALUE))
        );*/
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                callOnClose();
            }
        });
        
                this.imageTimer_ = new javax.swing.Timer(20000, imageAction);
        imageTimer_.start();
        
        loadImages();
        randomGen_ = new Random();
        setImage(randomGen_.nextInt(this.imageString_.length));
        updateTime();
    }
    
    public void loadImages(){
        File dir = new File("src/trampoline/24HourBounceImages");
        this.imageString_ = dir.list(filter);
    }
    
    public void setImage(int id){
        ImageIcon icon = new ImageIcon(getClass().getResource("/trampoline/24HourBounceImages/"+this.imageString_[id]));
        Image img = icon.getImage();
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(imageWidth_, imageHeight_, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        g.drawImage(img, 0,0, imageWidth_, imageHeight_, null);
        this.lblImage.setIcon(new ImageIcon(bi));
    }
    
    public void updateTime(){
        DateFormat timeFormat = new SimpleDateFormat("dd:HH:mm");
        Date time = new Date();
        String[] timeArray = (timeFormat.format(time)).split(":");

        String timeLabel = "Time Remaining: ";
        int hours=(29-Integer.parseInt(timeArray[0]))*24;
        hours += 11 - Integer.parseInt(timeArray[1]);
        int mins=60 - Integer.parseInt(timeArray[2]);

        if(hours<10){
            timeLabel += "0"+hours+":";
        }else{
            timeLabel += hours + ":";
        }

        if(mins<10){
            timeLabel +="0"+mins;
        }else{
            timeLabel += mins+"";
        }
        this.lblTime.setText(timeLabel);
    }

    public void callOnClose() {
        imageTimer_.stop();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTime = new javax.swing.JLabel();
        lblImage = new javax.swing.JLabel();
        lblTop = new javax.swing.JLabel();
        lbltop1 = new javax.swing.JLabel();
        lbltop2 = new javax.swing.JLabel();
        lbltop3 = new javax.swing.JLabel();
        lbltop4 = new javax.swing.JLabel();
        lblTop5 = new javax.swing.JLabel();
        lblHour5 = new javax.swing.JLabel();
        lblHour4 = new javax.swing.JLabel();
        lblHour3 = new javax.swing.JLabel();
        lblHour1 = new javax.swing.JLabel();
        lblHour2 = new javax.swing.JLabel();
        lblHour = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(0, 0, 0));
        setForeground(new java.awt.Color(0, 0, 0));

        lblTime.setFont(new java.awt.Font("Calibri", 1, 36)); // NOI18N
        lblTime.setForeground(new java.awt.Color(255, 255, 255));
        lblTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTime.setText("Time Remaining");

        lblImage.setText("image");

        lblTop.setText("Top Bouncers:");

        lbltop1.setText("first");

        lbltop2.setText("second");

        lbltop3.setText("third");

        lbltop4.setText("fourth");

        lblTop5.setText("fifth");

        lblHour5.setText("fifth");

        lblHour4.setText("fourth");

        lblHour3.setText("third");

        lblHour1.setText("first");

        lblHour2.setText("second");

        lblHour.setText("Top Bouncers this Hour:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(203, 203, 203))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTop, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbltop1)
                            .addComponent(lbltop2)
                            .addComponent(lbltop3)
                            .addComponent(lbltop4)
                            .addComponent(lblTop5)
                            .addComponent(lblHour, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblHour1)
                            .addComponent(lblHour2)
                            .addComponent(lblHour3)
                            .addComponent(lblHour4)
                            .addComponent(lblHour5))
                        .addContainerGap(29, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTop, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbltop4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTop5)
                        .addGap(110, 110, 110)
                        .addComponent(lblHour, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblHour5)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblHour;
    private javax.swing.JLabel lblHour1;
    private javax.swing.JLabel lblHour2;
    private javax.swing.JLabel lblHour3;
    private javax.swing.JLabel lblHour4;
    private javax.swing.JLabel lblHour5;
    private javax.swing.JLabel lblImage;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTop;
    private javax.swing.JLabel lblTop5;
    private javax.swing.JLabel lbltop1;
    private javax.swing.JLabel lbltop2;
    private javax.swing.JLabel lbltop3;
    private javax.swing.JLabel lbltop4;
    // End of variables declaration//GEN-END:variables
}
