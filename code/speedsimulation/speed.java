//Written for www.HomoFaciens.de
//Program demonstrates correlations between time, distance and speed.
//Copyright (C) 2009 Norbert Heinz
//
//This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free //Software Foundation version 3 of the License.
//
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or //FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License along with this program; if not, see http://www.gnu.org/licenses/.

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.text.*;
import java.util.GregorianCalendar;
import java.util.Calendar;
import javax.sound.sampled.*;
import java.applet.*;



public class speed extends JApplet implements  ActionListener  {

    static String versionNumber="v1.2";

    ScenePainter painter;
    GraphPainter graph01;
    int sceneHeight=200, sceneWidth=900;
    Box dummyBoxCol1 = Box.createHorizontalBox(); //Empty boxes for creating fixed row width and column height
    Box dummyBoxCol3 = Box.createHorizontalBox();
    Box dummyBoxRow1 = Box.createHorizontalBox();
    Box dummyBoxRow2 = Box.createHorizontalBox();
    Box dummyBoxRow3 = Box.createHorizontalBox();
    JSpinner speedSpinner, timeSpinner, distanceSpinner;
    JRadioButton distanceButton, speedButton, timeButton;
    JButton calcButton;
    JLabel speedLabel, timeLabel, distanceLabel;
    JLabel speedUnitLabel, timeUnitLabel, distanceUnitLabel;
    JLabel sourceLabel, versionLabel;
    int[][] xKoords;          //keeps values for 5 graphs
    int graphActive=0;        //painted graphnumber for simulation (0-4)
    double xMax=1.0, yMax=1.0;  //maximum values for graph
    double coordsCount=350.0; //number of coordinates to calculate for graphpainting
    double coordStep=0.0;     //xMax/coordsCount
    int iPos[], iPosNow;        //actual X-position and x-endposition while simulation is running
    //Min, Max and standard values for spinner controls
    int speedMin=1, speedMax=350, timeMin=1, timeMax=50, distanceMin=1, distanceMax=1000;
    boolean isRunning=false; //is simulation still running?
    Timer timer;             //timer for simulation
    int timerPause=25;       //delay between two calculated frames in milliseconds
    String xValue="", yValue="";  //Labeling of x- and y-axis
    long simulationTime, startTime, simulationTimeOld;  //times passed while simulation is running
    String[] resultString, formularString;  //calculation results painted for each graph
    double[] speedValue;
    double[] timeValue;
    double[] distanceValue;
    double simulationSpeed=1.0;
    boolean initScene=true;   //clears scene and graphs if true
    static String language="initalValue";
    String givenParam="nothing yet";   //To applet passed arguments
    boolean firstPaint=false;         //clears and repaints bufferimage for graphs if true

    public void init() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        try{
          givenParam = getParameter("LanguageSettings");
        }
        catch(NullPointerException npe){
          System.out.println("could not get parameter 'LanguageSettings'");
        }

        if(givenParam.compareTo("nothing yet")!=0) {
          if (givenParam.compareTo("German")==0)language="German";
          else language="English";
        }
        //System.out.println("language at init()=>" + language + "<"); // Display the string.
    }

    public void start() {
      int i;

        initComponents();

        xKoords=new int[5][(int)coordsCount];
        iPos=new int[5];
        speedValue=new double[5];
        timeValue=new double[5];
        distanceValue=new double[5];
        timer = new Timer(timerPause, new timerEvent());
        resultString = new String[5];
        formularString = new String[5];
        for(i=0;i<5;i++){
          resultString[i]="";
          formularString[i]="";
          iPos[i]=0;
          speedValue[i]=0.0;
          timeValue[i]=0.0;
          distanceValue[i]=0.0;
        }
        graphActive=-1;
    }

    public static void main(String[] args) {

        if(args.length>0){
          if (args[0].compareTo("German")==0)language="German";
          else language="English";
        }
        else{
          language="English";
        }

        JFrame f = new JFrame("Speed");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JApplet ap = new speed();
        ap.init();
        ap.start();
        f.getContentPane().add("Center", ap);
        f.pack();
        f.setVisible(true);

    }


    private BufferedImage loadImage(String name) {
        String imgFileName = "images/"+name;
        URL url = speed.class.getResource(imgFileName);
        BufferedImage img = null;
        try {
            img =  ImageIO.read(url);
        }
        catch (Exception e){
          img=null;
          System.out.println("Error loading Image '" + imgFileName + "': " + e); // Display the string.
        }
        return img;
    }

    private AudioClip loadSound(String name) {
        String soundFileName = "sounds/"+name;
        AudioClip audioClp=null;
        try {
          URL url = this.getClass().getClassLoader().getResource(soundFileName);
          audioClp = newAudioClip(url);
        }
        catch (Exception e){
          audioClp=null;
          System.out.println("Error loading Sound '" + soundFileName + "': " + e); // Display the string.
        }
        return audioClp;
    }

    public void initComponents() {
        int i;
        JFormattedTextField ftf = null;
        GridBagConstraints c = new GridBagConstraints();
        Font font = new Font("Arial", Font.PLAIN, 12);

        getContentPane().setLayout(new BorderLayout());

        //create panels
        JPanel controlPanel = new JPanel();
        JPanel scenePanel = new JPanel();
        JPanel mainPane = new JPanel();
        JPanel graphPanel = new JPanel();

        //set panel properties
        controlPanel.setLayout(new GridBagLayout());
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        mainPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));


        //create buttons, labels etcetera
        if(language.compareTo("German")==0){
          speedButton = new JRadioButton("Geschwindigkeit");
          speedButton.setMnemonic(KeyEvent.VK_G);
          timeButton = new JRadioButton("Fahrzeit");
          timeButton.setMnemonic(KeyEvent.VK_F);
          distanceButton = new JRadioButton("Strecke");
          distanceButton.setMnemonic(KeyEvent.VK_R);

          calcButton = new JButton("Start");
          calcButton.setMnemonic(KeyEvent.VK_S);

          speedLabel = new JLabel("Geschwindigkeit:");
          timeLabel = new JLabel("Fahrzeit:");
          distanceLabel = new JLabel("Strecke:");
        }
        else if(language.compareTo("XXX")==0){
          speedLabel = new JLabel("XXX:");
        }
        else{
          speedButton = new JRadioButton("Speed");
          speedButton.setMnemonic(KeyEvent.VK_E);
          timeButton = new JRadioButton("Run time");
          timeButton.setMnemonic(KeyEvent.VK_T);
          distanceButton = new JRadioButton("Distance");
          distanceButton.setMnemonic(KeyEvent.VK_D);

          calcButton = new JButton("Start");
          calcButton.setMnemonic(KeyEvent.VK_S);

          speedLabel = new JLabel("Speed:");
          timeLabel = new JLabel("Run time:");
          distanceLabel = new JLabel("Distance:");
        }

        speedUnitLabel = new JLabel("m/s");
        timeUnitLabel = new JLabel("s");
        distanceUnitLabel = new JLabel("m");
        sourceLabel = new JLabel("Source & info:    www.HomoFaciens.de");
        versionLabel = new JLabel(versionNumber);

        SpinnerNumberModel speedModel = new SpinnerNumberModel(55, speedMin, speedMax, 1);
        speedSpinner = new JSpinner(speedModel);
        ftf = getTextField(speedSpinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(6); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
        speedLabel.setLabelFor(speedSpinner);

        SpinnerNumberModel timeModel = new SpinnerNumberModel((timeMax-timeMin) / 2, timeMin, timeMax, 1);
        timeSpinner = new JSpinner(timeModel);
        ftf = getTextField(timeSpinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(6); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }

        SpinnerNumberModel distanceModel = new SpinnerNumberModel(55, distanceMin, distanceMax, 1);
        distanceSpinner = new JSpinner(distanceModel);
        ftf = getTextField(distanceSpinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(6); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }

        painter = new ScenePainter();
        graph01 = new GraphPainter();

        //set properties of controls
        speedUnitLabel.setFont(font);
        timeUnitLabel.setFont(font);
        distanceUnitLabel.setFont(font);

        speedLabel.setFont(font);
        timeLabel.setFont(font);
        distanceLabel.setFont(font);

        sourceLabel.setFont(font);
        versionLabel.setFont(font);

        xValue="t [s]";
        yValue="s [m]";

        speedButton.setFont(font);
        speedButton.setActionCommand("speedButtonPressed");
        speedButton.setSelected(true);
        speedButton.addActionListener(this);

        timeButton.setFont(font);
        timeButton.setActionCommand("timeButtonPressed");
        timeButton.setSelected(false);
        timeButton.addActionListener(this);

        distanceButton.setFont(font);
        distanceButton.setActionCommand("distanceButtonPressed");
        distanceButton.setSelected(false);
        distanceButton.addActionListener(this);

        calcButton.setFont(font);
        calcButton.setActionCommand("calcButtonPressed");
        calcButton.addActionListener(this);

        ButtonGroup group = new ButtonGroup();
        group.add(speedButton);
        group.add(timeButton);
        group.add(distanceButton);

        speedSpinner.setVisible(false);
        speedLabel.setVisible(false);
        speedUnitLabel.setVisible(false);

        painter.car = loadImage("fiat126.png");
        painter.carTire = loadImage("fiat126-tire.png");
        painter.carTireDriving = loadImage("fiat126-tire-driving.png");
        painter.guidePost = loadImage("guidepost.png");
        painter.truck = loadImage("truck.png");
        painter.truckTire = loadImage("truck-tire.png");
        painter.truckTireDriving = loadImage("truck-tire-driving.png");
        painter.backGround = loadImage("background.png");
        painter.sun = loadImage("sun.png");
        painter.moon = loadImage("moon.png");
        painter.carSpoiler = loadImage("spoiler.png");
        painter.carRocket01 = loadImage("rocket01.png");
        painter.carRocket02 = loadImage("rocket02.png");
        painter.dove = loadImage("dove.png");
        painter.doveWing = loadImage("dovewing.png");
        painter.thrustSSC = loadImage("thrust-ssc.png");
        painter.thrustSSCFlame01 = loadImage("thrust-ssc-flame01.png");
        painter.thrustSSCFlame02 = loadImage("thrust-ssc-flame02.png");

        painter.truckSound=loadSound("truck.wav");
        painter.carSound=loadSound("car.wav");
        painter.doveSound=loadSound("dove.wav");
        painter.manSound=loadSound("man.wav");
        painter.thrustSSCSound=loadSound("thrust-ssc.wav");


        //add components
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;
        c.insets = new Insets(5,5,5,5);  //padding

        dummyBoxCol1.add(dummyBoxCol1.createRigidArea(new Dimension (120,1)));
        dummyBoxCol3.add(dummyBoxCol3.createRigidArea(new Dimension (25,1)));
        dummyBoxRow1.add(dummyBoxRow1.createRigidArea(new Dimension (1,20)));
        dummyBoxRow2.add(dummyBoxRow2.createRigidArea(new Dimension (1,20)));
        dummyBoxRow3.add(dummyBoxRow3.createRigidArea(new Dimension (1,20)));

        dummyBoxRow1.setVisible(false);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor=GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth=1;
        controlPanel.add(speedLabel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth=1;
        controlPanel.add(speedSpinner, c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth=1;
        controlPanel.add(speedUnitLabel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(timeLabel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(timeSpinner, c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(timeUnitLabel, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(distanceLabel, c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(distanceSpinner, c);

        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(distanceUnitLabel, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth=3;
        controlPanel.add(speedButton, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth=3;
        controlPanel.add(timeButton, c);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth=3;
        controlPanel.add(distanceButton, c);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth=3;
        controlPanel.add(calcButton, c);

        //set dummyBoxes for fixed layout of columns and rows
        c.gridwidth=1;
        c.gridx = 3;
        c.gridy = 0;
        controlPanel.add(dummyBoxRow1, c);
        c.gridx = 3;
        c.gridy = 1;
        controlPanel.add(dummyBoxRow2, c);
        c.gridx = 3;
        c.gridy = 2;
        controlPanel.add(dummyBoxRow3, c);
        c.gridx = 0;
        c.gridy = 7;
        controlPanel.add(dummyBoxCol1, c);
        c.gridx = 2;
        c.gridy = 7;
        controlPanel.add(dummyBoxCol3, c);

        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth=3;
        c.fill = GridBagConstraints.NONE;
        c.anchor=GridBagConstraints.LINE_END;
        controlPanel.add(sourceLabel, c);

        c.gridx = 0;
        c.gridy = 9;
        c.gridwidth=3;
        c.fill = GridBagConstraints.NONE;
        c.anchor=GridBagConstraints.LINE_END;
        controlPanel.add(versionLabel, c);

        //setup panels
        scenePanel.add("Center", painter);
        graphPanel.add("Center", graph01);
        graphPanel.add("Center", controlPanel);

        mainPane.add(scenePanel);
        mainPane.add(graphPanel);
        getContentPane().add(mainPane);
        getRootPane().setDefaultButton(calcButton);

    }

    public JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        } else {
            System.err.println("Unexpected editor type: "  + spinner.getEditor().getClass() + " isn't a descendant of DefaultEditor");
            return null;
        }
    }

    // Listens to the buttons.
    public void actionPerformed(ActionEvent e) {
        int i, i2;
        double coordMulti=0.5;

        if(e.getActionCommand()=="speedButtonPressed"){
          for(i=0;i<5;i++){
            resultString[i]="";
            formularString[i]="";
            iPos[i]=0;
            speedValue[i]=0.0;
            timeValue[i]=0.0;
            distanceValue[i]=0.0;
          }
          graphActive=-1;
          xMax=1.0;
          yMax=1.0;
          initScene=true;
          xValue="t [s]";
          yValue="s [m]";

          speedSpinner.setVisible(false);
          speedLabel.setVisible(false);
          speedUnitLabel.setVisible(false);
          dummyBoxRow1.setVisible(false);

          timeSpinner.setVisible(true);
          timeLabel.setVisible(true);
          timeUnitLabel.setVisible(true);
          dummyBoxRow2.setVisible(true);

          distanceSpinner.setVisible(true);
          distanceLabel.setVisible(true);
          distanceUnitLabel.setVisible(true);
          dummyBoxRow3.setVisible(true);
        }//if(e.getActionCommand()=="speedButtonPressed")

        if(e.getActionCommand()=="timeButtonPressed"){
          for(i=0;i<5;i++){
            resultString[i]="";
            formularString[i]="";
            iPos[i]=0;
            speedValue[i]=0.0;
            timeValue[i]=0.0;
            distanceValue[i]=0.0;
          }
          graphActive=-1;
          xMax=1.0;
          yMax=1.0;
          initScene=true;
          xValue="s [m]";
          yValue="v [m/s]";

          speedSpinner.setVisible(true);
          speedLabel.setVisible(true);
          speedUnitLabel.setVisible(true);
          dummyBoxRow1.setVisible(true);

          timeSpinner.setVisible(false);
          timeLabel.setVisible(false);
          timeUnitLabel.setVisible(false);
          dummyBoxRow2.setVisible(false);

          distanceSpinner.setVisible(true);
          distanceLabel.setVisible(true);
          distanceUnitLabel.setVisible(true);
          dummyBoxRow3.setVisible(true);
        }//if(e.getActionCommand()=="timeButtonPressed")

        if(e.getActionCommand()=="distanceButtonPressed"){
          for(i=0;i<5;i++){
            resultString[i]="";
            formularString[i]="";
            iPos[i]=0;
            speedValue[i]=0.0;
            timeValue[i]=0.0;
            distanceValue[i]=0.0;
          }
          graphActive=-1;
          xMax=1.0;
          yMax=1.0;
          initScene=true;
          xValue="t [s]";
          yValue="v [m/s]";

          //forceSpinner.setEnabled(false);
          speedSpinner.setVisible(true);
          speedLabel.setVisible(true);
          speedUnitLabel.setVisible(true);
          dummyBoxRow1.setVisible(true);

          timeSpinner.setVisible(true);
          timeLabel.setVisible(true);
          timeUnitLabel.setVisible(true);
          dummyBoxRow2.setVisible(true);

          distanceSpinner.setVisible(false);
          distanceLabel.setVisible(false);
          distanceUnitLabel.setVisible(false);
          dummyBoxRow3.setVisible(false);
        }//if(e.getActionCommand()=="distanceButtonPressed")

        if("calcButtonPressed".equals(e.getActionCommand())) {
          //System.out.println("Knopf gedrückt:"); // Display the string.
          if(painter.truckSound != null){
            painter.truckSound.stop();
            painter.truckSoundIsRunning=false;
          }
          if(painter.doveSound != null){
            painter.doveSound.stop();
            painter.doveSoundIsRunning=false;
          }
          if(painter.manSound != null){
            painter.manSound.stop();
            painter.manSoundIsRunning=false;
          }
          if(painter.thrustSSCSound != null){
            painter.thrustSSCSound.stop();
            painter.thrustSSCSoundIsRunning=false;
          }

          if(isRunning){
            if(painter.carSound != null){
              painter.carSound.stop();
            }
            if(language.compareTo("German")==0){
              calcButton.setText("Start");
            }
            else if(language.compareTo("XXX")==0){
              calcButton.setText("Start");
            }
            else{
              calcButton.setText("Start");
            }
            isRunning=false;
            painter.isRunning=false;
          }
          else{

            iPosNow=0;
            firstPaint=true;
            if(language.compareTo("German")==0){
              calcButton.setText("Stop");
            }
            else if(language.compareTo("XXX")==0){
              calcButton.setText("Stop");
            }
            else{
              calcButton.setText("Stop");
            }
            graphActive++;
            if(graphActive>4)graphActive=0;
            startTime=System.currentTimeMillis();
            simulationTime=0;
            simulationTimeOld=0;

            speedSpinner.setEnabled(false);
            timeSpinner.setEnabled(false);
            distanceSpinner.setEnabled(false);
            speedButton.setEnabled(false);
            timeButton.setEnabled(false);
            distanceButton.setEnabled(false);

            if(distanceButton.isSelected()){
              timeValue[graphActive]=((Integer)timeSpinner.getValue()).doubleValue();
              speedValue[graphActive]=((Integer)speedSpinner.getValue()).doubleValue();
              if(timeValue[graphActive]>xMax)xMax=timeValue[graphActive];
              if(speedValue[graphActive]>yMax)yMax=speedValue[graphActive];
              coordStep = xMax / coordsCount;
              for(i=0;i<5;i++){
                iPos[i]=(int) (timeValue[i] / coordStep);
              }
              coordMulti = (double)graph01.graphHeight / yMax;
              simulationSpeed=speedValue[graphActive];
              for(i2=0;i2<5;i2++){
                for(i=0;i<coordsCount;i++){
                  xKoords[i2][i]= (int) (coordMulti*speedValue[i2]);
                }
              }
              resultString[graphActive] = "v = " + new DecimalFormat("0.0").format(speedSpinner.getValue()) + "m/s, t = " + new DecimalFormat("0.0").format(timeSpinner.getValue()) + "s";
              formularString[graphActive] = new DecimalFormat("0.0").format(timeSpinner.getValue()) + "s * (" + new DecimalFormat("0.0").format(speedSpinner.getValue()) + "m/s) = " + new DecimalFormat("0.0").format( timeValue[graphActive] * speedValue[graphActive]) + "m";
            }
            if(speedButton.isSelected()){
              distanceValue[graphActive]=((Integer)distanceSpinner.getValue()).doubleValue();
              timeValue[graphActive]=((Integer)timeSpinner.getValue()).doubleValue();
              if(distanceValue[graphActive]>yMax)yMax=distanceValue[graphActive];
              if(timeValue[graphActive]>xMax)xMax=timeValue[graphActive];
              coordStep = xMax / coordsCount;
              for(i=0;i<5;i++){
                iPos[i]=(int) (timeValue[i] / coordStep);
              }
              coordMulti = (double)graph01.graphHeight / yMax;
              simulationSpeed=distanceValue[graphActive]/timeValue[graphActive];
              for(i2=0;i2<5;i2++){
                for(i=0;i<coordsCount;i++){
                  xKoords[i2][i]= (int) ((double) (i) * coordStep*coordMulti*distanceValue[i2]/timeValue[i2]);
                }
              }
              resultString[graphActive] = "s = " + new DecimalFormat("0.0").format(distanceSpinner.getValue()) + "m, t = " + new DecimalFormat("0.0").format(timeSpinner.getValue()) + "s";
              formularString[graphActive] = new DecimalFormat("0.0").format(distanceSpinner.getValue()) + "m / " + new DecimalFormat("0.0").format(timeSpinner.getValue()) + "s = " + new DecimalFormat("0.0").format( distanceValue[graphActive]/timeValue[graphActive]) + "m/s";
            }
            if(timeButton.isSelected()){
              distanceValue[graphActive]=((Integer)distanceSpinner.getValue()).doubleValue();
              speedValue[graphActive]=((Integer)speedSpinner.getValue()).doubleValue();
              if(distanceValue[graphActive]>xMax)xMax=distanceValue[graphActive];
              if(speedValue[graphActive]>yMax)yMax=speedValue[graphActive];
              coordStep = xMax / coordsCount;
              for(i=0;i<5;i++){
                iPos[i]=(int) (distanceValue[i] / coordStep);
              }
              coordMulti = (double)graph01.graphHeight / yMax;
              simulationSpeed=speedValue[graphActive];
              for(i2=0;i2<5;i2++){
                for(i=0;i<coordsCount;i++){
                  xKoords[i2][i]= (int) (coordMulti*speedValue[i2]);
                }
              }
              resultString[graphActive] = "v = " + new DecimalFormat("0.0").format(speedSpinner.getValue()) + "m/s, s = " + new DecimalFormat("0.0").format(distanceSpinner.getValue()) + "m";
              formularString[graphActive] = new DecimalFormat("0.0").format(distanceSpinner.getValue()) + "m / (" + new DecimalFormat("0.0").format(speedSpinner.getValue()) + "m/s) = " + new DecimalFormat("0.0").format( distanceValue[graphActive]/speedValue[graphActive]) + "s";
            }
            if(iPos[graphActive]>coordsCount)iPos[graphActive]=(int)coordsCount;
            if(timer.getInitialDelay()!=timerPause){
              timer.setInitialDelay(timerPause);
            }
            timer.start();
            isRunning=true;
            painter.isRunning=true;
            if(initScene){
              painter.doveY=(Math.random() * painter.sceneHeight * 0.7);
              if(simulationSpeed>15.0){
                painter.doveX=-200.0-(Math.random() *  1000.0);
              }
              else{
                painter.doveX=1200.0+(Math.random() *  1000.0);
              }
              if(simulationSpeed>1.3){
                painter.manX=-200.0;
              }
              else{
                painter.manX=painter.sceneWidth + 20.0;
              }
              if(simulationSpeed>341.0){
                painter.thrustSSCX=-1200.0;
              }
              else{
                painter.thrustSSCX=painter.sceneWidth + 20.0 + (Math.random() *  1000.0);
              }
              if(simulationSpeed > 22.0){
                painter.truckX=-850.0;
              }
              else{
                painter.truckX=painter.sceneWidth + 20.0 + (Math.random() *  1000.0);;
              }
            }
            if(painter.carSound!=null)painter.carSound.loop();
          }//if(!isRunning)
        }//if ("calcButtonPressed".equals(e.getActionCommand())) {
    }

    private class timerEvent implements ActionListener{
      public void actionPerformed(ActionEvent e){
        simulationTimeOld=simulationTime;
        simulationTime=System.currentTimeMillis()-startTime;

        if(speedButton.isSelected() || distanceButton.isSelected()){//Time at x-axis
          iPosNow=(int)(simulationTime/1000.0/coordStep);
        }
        if(timeButton.isSelected()){//Distance at x-axis
          iPosNow=(int)(speedValue[graphActive]*simulationTime/1000.0/coordStep);
        }

        if(iPos[graphActive]<iPosNow | !isRunning){
          iPosNow=iPos[graphActive];
          timer.stop();
          speedSpinner.setEnabled(true);
          timeSpinner.setEnabled(true);
          distanceSpinner.setEnabled(true);
          speedButton.setEnabled(true);
          timeButton.setEnabled(true);
          distanceButton.setEnabled(true);
          isRunning=false;
          painter.isRunning=false;
          if(painter.truckSound != null){
            painter.truckSound.stop();
          }
          if(painter.carSound != null){
            painter.carSound.stop();
          }
          if(painter.doveSound != null){
            painter.doveSound.stop();
          }
          if(painter.manSound != null){
            painter.manSound.stop();
          }
          if(painter.thrustSSCSound != null){
            painter.thrustSSCSound.stop();
          }
          if(language.compareTo("German")==0){
            calcButton.setText("Start");
          }
          else if(language.compareTo("XXX")==0){
            calcButton.setText("Start");
          }
          else{
            calcButton.setText("Start");
          }
          graph01.refreshScene(xKoords, iPos, xValue, yValue, resultString, formularString, xMax, yMax, 999);//refresh all lines
        }

        //car is 3meters long, picture of car is 195pixels wide: 195.0/3.0 is the number of pixels for 1meter.
        //truck speed is 22m/s
        painter.truckX +=(simulationTime-simulationTimeOld)*195.0/3.0*(simulationSpeed-22.0)/1000.0;
        painter.doveX+=(simulationTime-simulationTimeOld)*195.0/3.0*(simulationSpeed-15.0)/1000.0;
        painter.doveWingHeight=(int) (59.0 * Math.sin( simulationTime/250.0 ));
        painter.manCycle+=(simulationTime-simulationTimeOld)/150.0;
        painter.manX+=(simulationTime-simulationTimeOld)*195.0/3.0*(simulationSpeed-1.3)/1000.0;
        painter.thrustSSCX+=(simulationTime-simulationTimeOld)*195.0/3.0*(simulationSpeed-341.0)/1000.0;
        painter.guidePostX+=(simulationTime-simulationTimeOld)*195.0/3.0*(simulationSpeed)/1000.0;
        painter.backGroundX+=(simulationTime-simulationTimeOld)*195.0/3.0*(simulationSpeed)/1000.0/100.0;
        while(painter.backGroundX>painter.sceneWidth)painter.backGroundX-=sceneWidth;
        while(painter.manCycle>2.0*3.1415927)painter.manCycle-=2.0*3.1415927;
        if(firstPaint){
          graph01.refreshScene(xKoords, iPos, xValue, yValue, resultString, formularString, xMax, yMax, graphActive);
          graph01.xAlt=0;
          graph01.yAlt=xKoords[graphActive][0];
          firstPaint=false;
        }
        if(iPosNow>0 && iPosNow<coordsCount){
          graph01.addLine(iPosNow, xKoords[graphActive][iPosNow], graphActive);
        }
        painter.refreshScene(195.0 * simulationSpeed * simulationTime / 3.0 / 1000.0, (double) simulationSpeed);
        if(initScene){
          painter.backGroundX=0.0;
        }
        initScene=false;
      }
    }
}



class ScenePainter extends Component {

    BufferedImage car = null, guidePost = null, truck = null, carTire=null, carTireDriving = null, backGround = null;
    BufferedImage truckTire = null, truckTireDriving = null, carSpoiler = null, carRocket01 = null, carRocket02 = null;
    BufferedImage thrustSSC=null, thrustSSCFlame01=null, thrustSSCFlame02=null;
    BufferedImage sun = null, moon = null, dove = null, doveWing = null;
    int doveWingHeight=10;
    int sceneHeight = 200, sceneWidth = 900;
    int i;
    double guidePostX=0.0, truckX=1000.0, backGroundX=0.0, doveX=-1000.0, doveY=10.0;
    double manX=-100.0, manSize=100.0, manCycle=0.0, manCycle2=0.0, manY=100.0, thrustSSCX=2000.0;
    int[] tireHub={0,0};  //makes the tires of the car jump while driving
    int[] truckTireHub={0, 0, 0, 0, 0};
    int tireMaxHub=1, truckTireMaxHub=2;
    BufferedImage bufferImage=null;
    Graphics2D bufferImageSurface=null;
    AudioClip truckSound=null, carSound=null, doveSound=null, manSound=null, thrustSSCSound=null;
    boolean truckSoundIsRunning=false, carSoundIsRunning=false, manSoundIsRunning=false, doveSoundIsRunning=false;
    boolean isRunning=false, thrustSSCSoundIsRunning=false;
    boolean truckSoundStopped=true, carSoundStopped=true, doveSoundStopped=true, manSoundStopped=true, thrustSSCSoundStopped=true;
    double carSpeed=0.0;

    public Dimension getPreferredSize(){
        return new Dimension(sceneWidth, sceneHeight);
    }

    void refreshScene(double distance, double simulationCarSpeed) {

      //play sound, if truck, dove, man etcetera reach middle of the scene (=position of car)
      if(truckSound!=null){
        if(truckX<sceneWidth/2.0 + 20.0 && truckX>sceneWidth/2.0 - 20.0){
          if (!truckSoundIsRunning && isRunning){
            truckSound.play();
            truckSoundIsRunning=true;
            truckSoundStopped=false;
          }
        }
        else{
          truckSoundIsRunning=false;
        }
        if((truckX<0.0 || truckX>sceneWidth) && !truckSoundStopped){
          truckSound.stop();
          truckSoundIsRunning=false;
          truckSoundStopped=true;
        }
      }
      if(doveSound!=null){
        if(doveX<sceneWidth/2.0 + 20.0 && doveX>sceneWidth/2.0 - 20.0){
          if (!doveSoundIsRunning && isRunning){
            doveSound.play();
            doveSoundIsRunning=true;
            doveSoundStopped=false;
          }
        }
        else{
          doveSoundIsRunning=false;
        }
        if((doveX<0.0 || doveX>sceneWidth) && !doveSoundStopped){
          doveSound.stop();
          doveSoundIsRunning=false;
          doveSoundStopped=true;
        }
      }
      if(manSound!=null){
        if(manX<sceneWidth/2.0 + 20.0 && manX>sceneWidth/2.0 - 20.0){
          if (!manSoundIsRunning && isRunning){
            manSound.play();
            manSoundIsRunning=true;
            manSoundStopped=false;
          }
        }
        else{
          manSoundIsRunning=false;
        }
        if((manX<0.0 || manX>sceneWidth) && !manSoundStopped){
          manSound.stop();
          manSoundIsRunning=false;
          manSoundStopped=true;
        }
      }
      if(thrustSSCSound!=null){
        if(thrustSSCX<sceneWidth/2.0 + 20.0 && thrustSSCX>sceneWidth/2.0 - 20.0){
          if (!thrustSSCSoundIsRunning && isRunning){
            thrustSSCSound.play();
            thrustSSCSoundIsRunning=true;
            thrustSSCSoundStopped=false;
          }
        }
        else{
          thrustSSCSoundIsRunning=false;
        }
        if((manX<0.0 || manX>sceneWidth) && !manSoundStopped){
          thrustSSCSound.stop();
          thrustSSCSoundIsRunning=false;
          thrustSSCSoundStopped=true;
        }
      }
      while((int) guidePostX > (double) (sceneWidth) )guidePostX -= 195.0 * 50.0 / 3.0;

      //tires of car jump higher, the faster the car is driving
      tireMaxHub=2;
      if(simulationCarSpeed>10.0)tireMaxHub=3;
      if(simulationCarSpeed>30.0)tireMaxHub=4;
      if(simulationCarSpeed>50.0)tireMaxHub=5;
      carSpeed=simulationCarSpeed;
      repaint();
    }


    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Composite origComposite;
        int sunX, sunY;
        boolean sunShine=false;
        Calendar cal = new GregorianCalendar();
        int secondsSinceMidnight = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);

        //create buffer to avoid flickering while scene is calculated and painted
        if(bufferImage==null){
          bufferImage = (BufferedImage)createImage(sceneWidth, sceneHeight);
          bufferImageSurface = bufferImage.createGraphics();
        }

        origComposite = g2.getComposite();

        //calculates if sun (6:00 - 18:00) or moon (18:00 - 6:00) is shining
        //sunX: sun or moon are moving horizontal
        //suny: sun rising from 6:00 to 12:00 and declines from 12:00 - 18:00
        //      moon rising from 18:00 to 00:00 and declines from 00:00 - 6:00
        if(secondsSinceMidnight>6*3600 && secondsSinceMidnight<18*3600){//Sun shining
          secondsSinceMidnight-=6*3600;
          sunShine=true;
          sunX=(int)(sceneWidth * 0.1 + sceneWidth * 0.8 * secondsSinceMidnight / (3600.0 * 12.0));
          if(secondsSinceMidnight>6*3600){
            sunY=(int)( sceneHeight * 0.4 * ((secondsSinceMidnight - 3600.0 * 6.0) / (3600.0 * 6.0)));
          }
          else{
            sunY=(int)( sceneHeight * 0.4 * (1.0 - (secondsSinceMidnight / (3600.0 * 6.0))));
          }
        }
        else{//moon shining
          if(secondsSinceMidnight>18*3600){
            sunShine=false;
            secondsSinceMidnight-=12*3600;
          }
          if(secondsSinceMidnight>6*3600){
            sunX=(int)(sceneWidth * 0.5 - sceneWidth * 0.8 * (1.0 - secondsSinceMidnight / (3600.0 * 12.0)));
            sunY=(int)( sceneHeight * 0.4 * (1.0- (secondsSinceMidnight - 3600.0 * 6.0) / (3600.0 * 6.0)));
          }
          else{
            sunX=(int)(sceneWidth * 0.5 + sceneWidth * 0.8 * secondsSinceMidnight / (3600.0 * 12.0));
            sunY=(int)( sceneHeight * 0.4 * (secondsSinceMidnight / (3600.0 * 6.0)));
          }
        }
        //center of sun or moon sets position, not the left corner of the image
        if(sunShine && sun != null)sunX-=sun.getWidth(null)/2;
        if(!sunShine && moon != null)sunX-=moon.getWidth(null)/2;

        if(sunShine){//light blue background while sun is shining
          bufferImageSurface.setBackground(new Color(223, 252, 255)); 
        }
        else{//black background during night
          bufferImageSurface.setBackground(new Color(0, 0, 50)); 
        }
        bufferImageSurface.clearRect(0, 0, sceneWidth, sceneHeight);

        if(sunShine){
          if(sun != null)bufferImageSurface.drawImage(sun, sunX, sunY, 100, 100, this);
        }
        else{
          if(moon != null)bufferImageSurface.drawImage(moon, sunX, sunY, 100, 100, this);
        }

        if(backGround!=null){
          bufferImageSurface.drawImage(backGround, (int)backGroundX, 0, sceneWidth, sceneHeight, this);
          bufferImageSurface.drawImage(backGround, (int)backGroundX-sceneWidth, 0, sceneWidth, sceneHeight, this);
        }
        if(guidePost != null)bufferImageSurface.drawImage(guidePost, (int) (guidePostX), sceneHeight-guidePost.getHeight(null), this);

        if(thrustSSC != null && thrustSSCFlame01 != null && thrustSSCFlame02 != null){
          if(thrustSSCX<sceneWidth && thrustSSCX>-thrustSSC.getWidth(null)){//paint Super Sonic Car
            bufferImageSurface.drawImage(thrustSSC, (int) (thrustSSCX), sceneHeight-thrustSSC.getHeight(null), this);
            if(isRunning){//switching between two flame images while SSC is driving
              if(Math.random()>0.5){
                bufferImageSurface.drawImage(thrustSSCFlame01,(int)(thrustSSCX+885.0), sceneHeight-thrustSSC.getHeight(null)+80, this);
              }
              else{
                bufferImageSurface.drawImage(thrustSSCFlame02,(int)(thrustSSCX+885.0), sceneHeight-thrustSSC.getHeight(null)+80, this);
              }
            }
          }
        }

        if(truck != null && truckTireDriving != null && truckTire != null){
          if(truckX<sceneWidth && truckX>-truck.getWidth(null)){//paint truck
            bufferImageSurface.drawImage(truck, (int) (truckX), sceneHeight-truck.getHeight(null), this);
            if(isRunning){//let the tires jump while truck is driving
              for(i=0;i<5;i++){
                truckTireHub[i]=(int)(Math.random() * (double)truckTireMaxHub);
              }
              bufferImageSurface.drawImage(truckTireDriving, (int)truckX+10, sceneHeight-truckTireDriving.getHeight(null)-(int) truckTireHub[0], this);
              bufferImageSurface.drawImage(truckTireDriving, (int)truckX+115, sceneHeight-truckTireDriving.getHeight(null)-(int) truckTireHub[1], this);
              bufferImageSurface.drawImage(truckTireDriving, (int)truckX+172, sceneHeight-truckTireDriving.getHeight(null)-(int) truckTireHub[2], this);
              bufferImageSurface.drawImage(truckTireDriving, (int)truckX+355, sceneHeight-truckTireDriving.getHeight(null)-(int) truckTireHub[3], this);
              bufferImageSurface.drawImage(truckTireDriving, (int)truckX+415, sceneHeight-truckTireDriving.getHeight(null)-(int) truckTireHub[4], this);
            }
            else{
              bufferImageSurface.drawImage(truckTire, (int)truckX+10, sceneHeight-truckTireDriving.getHeight(null), this);
              bufferImageSurface.drawImage(truckTire, (int)truckX+115, sceneHeight-truckTireDriving.getHeight(null), this);
              bufferImageSurface.drawImage(truckTire, (int)truckX+172, sceneHeight-truckTireDriving.getHeight(null), this);
              bufferImageSurface.drawImage(truckTire, (int)truckX+355, sceneHeight-truckTireDriving.getHeight(null), this);
              bufferImageSurface.drawImage(truckTire, (int)truckX+415, sceneHeight-truckTireDriving.getHeight(null), this);
            }
          }
        }

        if(car != null)bufferImageSurface.drawImage(car,(sceneWidth-car.getWidth(null))/2, sceneHeight-car.getHeight(null), this);
        if(isRunning){
          for(i=0;i<2;i++){
            tireHub[i]=(int)(Math.random() * (double)tireMaxHub);
          }
          if(carTireDriving != null){//let the tires jump while car is driving
            bufferImageSurface.drawImage(carTireDriving,(sceneWidth-carTireDriving.getWidth(null))/2-68, sceneHeight-carTireDriving.getHeight(null)-(int)tireHub[0], this);
            bufferImageSurface.drawImage(carTireDriving,(sceneWidth-carTireDriving.getWidth(null))/2+55, sceneHeight-carTireDriving.getHeight(null)-(int)tireHub[1], this);
          }
        }
        else{
          if(carTire != null){
            bufferImageSurface.drawImage(carTire,(sceneWidth-carTire.getWidth(null))/2-68, sceneHeight-carTire.getHeight(null), this);
            bufferImageSurface.drawImage(carTire,(sceneWidth-carTire.getWidth(null))/2+55, sceneHeight-carTire.getHeight(null), this);
          }
        }
        if(carSpeed>33.0){//car needs a spoiler while driving faster than 33m/s
          if(carSpoiler != null)bufferImageSurface.drawImage(carSpoiler,sceneWidth/2+59, sceneHeight-car.getHeight(null)+5, this);
        }
        if(carSpeed>100.0){//car needs a booster for driving faster than 199m/s
          if(Math.random()>0.5){
            if(carRocket01 != null)bufferImageSurface.drawImage(carRocket01,sceneWidth/2+94, sceneHeight-car.getHeight(null)+55, this);
          }
          else{
            if(carRocket02 != null)bufferImageSurface.drawImage(carRocket02,sceneWidth/2+94, sceneHeight-car.getHeight(null)+55, this);
          }
        }

        if(dove != null && doveWing != null){
          if(doveX<sceneWidth && doveX>-dove.getWidth(null)){//paint dove
            bufferImageSurface.drawImage(dove, (int)doveX, (int)doveY, this);
            bufferImageSurface.drawImage(doveWing, (int)doveX+20, (int)doveY+15-doveWingHeight, doveWing.getWidth(null), (int)doveWingHeight, this);
          }
        }
        if(manX<sceneWidth + 50 && manX>-50){//paint man
          manCycle2 = manCycle + 3.1415927;
          if (manCycle2>3.1415927 * 2.0)manCycle2 -= 3.1415927 * 2.0;

          bufferImageSurface.setColor(new Color(255,255,0));
          double elbowX, shoulderX, elbowY, shoulderY, armLength=manSize*0.15, wristX, wristY;
          double hipX, hipY, kneeX, kneeY, heelX, heelY, legLength=manSize*0.2;

          manY=sceneHeight-manSize;
          shoulderX=manX;
          shoulderY=manY+(manSize*0.31);
          elbowX=Math.sin( manCycle )*armLength*0.3123;
          elbowY=Math.sqrt(Math.pow(armLength, 2.0)-Math.pow(elbowX, 2.0));
          wristX=elbowX-armLength*0.5123;
          wristY=Math.sqrt(Math.pow(armLength, 2.0)-Math.pow(wristX, 2.0));;
          hipX=manX;
          hipY=manY+(manSize*0.65);
          kneeX=Math.sin( manCycle2 )*legLength*0.3123;
          kneeY=Math.sqrt(Math.pow(legLength, 2.0)-Math.pow(kneeX, 2.0));
          if(manCycle2>3.1415*0.5 && manCycle2<3.1415*1.5 ){//knee moving forward
            heelX=Math.sin( manCycle2 )*legLength*0.3123 + Math.sin( manCycle2 - 3.1415 * 0.5 )*legLength*0.3123;
            heelY=Math.sqrt(Math.pow(legLength, 2.0)-Math.pow(kneeX, 2.0));
          }
          else{//knee straight while moving back
            heelX=Math.sin( manCycle2 )*legLength*0.3123;
            heelY=Math.sqrt(Math.pow(legLength, 2.0)-Math.pow(heelX, 2.0));
          }
          //painting arm
          bufferImageSurface.setColor(new Color(100,100,0));
          bufferImageSurface.drawLine((int) shoulderX,
                                      (int) shoulderY,
                                      (int) (manX + elbowX),
                                      (int) (shoulderY + elbowY));
          bufferImageSurface.drawLine((int) (manX + elbowX),
                                      (int) (shoulderY + elbowY),
                                      (int) (manX + elbowX + wristX),
                                      (int) (shoulderY + elbowY + wristY));
          //painting leg
          bufferImageSurface.setColor(new Color(0,0,100));
          bufferImageSurface.drawLine((int) manX,
                                      (int) hipY,
                                      (int) (manX + kneeX),
                                      (int) (hipY + kneeY));
          bufferImageSurface.drawLine((int) (manX + kneeX),
                                      (int) (hipY + kneeY),
                                      (int) (manX + kneeX + heelX),
                                      (int) (hipY + kneeY + heelY));

          //paint body
          bufferImageSurface.setColor(new Color(235,184,233));
          bufferImageSurface.fillOval((int) manX-(int)(manSize/10.0), (int)(manY), (int) (manSize/5.0), (int) (manSize/5.0));
          bufferImageSurface.fillOval((int) manX-(int)(manSize/7.7), (int)(manY+manSize*0.09), (int) (manSize/15.0), (int) (manSize/15.0));
          bufferImageSurface.setColor(new Color(170,170,0));
          bufferImageSurface.fillOval((int) manX-(int)(manSize/10.0), (int)(manY+manSize*0.19), (int) (manSize/5.0), (int) (manSize/2.0));
          bufferImageSurface.setColor(new Color(0,0,255));
          bufferImageSurface.fillOval((int) manX-(int)(manSize/10.7), (int)(manY+manSize*0.041), (int) (manSize/15.0), (int) (manSize/15.0));

          elbowX=Math.sin( manCycle2 )*armLength*0.3123;
          elbowY=Math.sqrt(Math.pow(armLength, 2.0)-Math.pow(elbowX, 2.0));
          wristX=elbowX-armLength*0.5123;
          wristY=Math.sqrt(Math.pow(armLength, 2.0)-Math.pow(wristX, 2.0));;
          kneeX=Math.sin( manCycle )*legLength*0.3123;
          kneeY=Math.sqrt(Math.pow(legLength, 2.0)-Math.pow(kneeX, 2.0));
          if(manCycle>3.1415*0.5 && manCycle<3.1415*1.5 ){//knee moving forward
            heelX=Math.sin( manCycle )*legLength*0.3123 + Math.sin( manCycle - 3.1415 * 0.5 )*legLength*0.3123;
            heelY=Math.sqrt(Math.pow(legLength, 2.0)-Math.pow(kneeX, 2.0));
          }
          else{//knee straight while moving back
            heelX=Math.sin( manCycle )*legLength*0.3123;
            heelY=Math.sqrt(Math.pow(legLength, 2.0)-Math.pow(heelX, 2.0));
          }
          //painting arm
          bufferImageSurface.setColor(new Color(255,255,0));
          bufferImageSurface.drawLine((int) manX,
                                      (int) shoulderY,
                                      (int) (shoulderX + elbowX),
                                      (int) (shoulderY + elbowY));
          bufferImageSurface.drawLine((int) (manX + elbowX),
                                      (int) (shoulderY + elbowY),
                                      (int) (manX + elbowX + wristX),
                                      (int) (shoulderY + elbowY + wristY));
          //painting leg
          bufferImageSurface.setColor(new Color(0,0,255));
          bufferImageSurface.drawLine((int) manX,
                                      (int) hipY,
                                      (int) (manX + kneeX),
                                      (int) (hipY + kneeY));
          bufferImageSurface.drawLine((int) (manX + kneeX),
                                      (int) (hipY + kneeY),
                                      (int) (manX + kneeX + heelX),
                                      (int) (hipY + kneeY + heelY));

          bufferImageSurface.setColor(new Color(0,0,0));
        }//end painting man

        bufferImageSurface.drawRect(0, 0, sceneWidth-1, sceneHeight-1);

        //paint bufferImage and make it visible
        g2.drawImage(bufferImage, 0, 0, this);

        g2.setComposite(origComposite);

    }
}

class GraphPainter extends Component {
    int graphWidth=350, graphHeight=220;
    Color[] colorUsed= {new Color(200, 0, 0), new Color(0, 200, 0), new Color(0, 0, 200), new Color(0, 200, 200), new Color(200, 0, 200)};
    BufferedImage bufferImage=null;
    Graphics2D bufferImageSurface=null;
    int xAlt, yAlt;

    public Dimension getPreferredSize(){
        return new Dimension(graphWidth + 50 + 200, graphHeight + 55);
    }

    void addLine(int xNeu, int yNeu, int plotColor){
      if(xNeu != xAlt || yNeu != yAlt){
        bufferImageSurface.setColor(colorUsed[plotColor]);
        bufferImageSurface.drawLine(xAlt+49, graphHeight-yAlt+20, xNeu+50, graphHeight-yNeu+20);
        xAlt=xNeu;
        yAlt=yNeu;
        repaint();
      }
    }

    void refreshScene(int[][] x,
                      int[] iPos,
                      String xString,
                      String yString,
                      String[] rString,
                      String[] fString,
                      double xMVal,
                      double yMVal,
                      int graphActive) {
        int i, i2;

        if(bufferImage==null){
          bufferImage = (BufferedImage)createImage(graphWidth + 250, graphHeight + 55);
          bufferImageSurface = bufferImage.createGraphics();
          bufferImageSurface.setBackground(new Color(238, 238, 238));
        }

        bufferImageSurface.clearRect(0, 0, graphWidth + 250, graphHeight + 55);

        bufferImageSurface.setColor(Color.BLACK);
        //Y-Axis
        bufferImageSurface.drawLine(50, 20, 50, graphHeight+30);
        bufferImageSurface.drawLine(50, 20, 40, 30);
        bufferImageSurface.drawLine(50, 20, 60, 30);
        bufferImageSurface.drawLine(40, 20+(int)(graphHeight*0.25), 50, 20+(int)(graphHeight*0.25));
        bufferImageSurface.drawLine(40, 20+(int)(graphHeight*0.5), 50, 20+(int)(graphHeight*0.5));
        bufferImageSurface.drawLine(40, 20+(int)(graphHeight*0.75), 50, 20+(int)(graphHeight*0.75));
        //X-Axis
        bufferImageSurface.drawLine(40, graphHeight+20, graphWidth+50, graphHeight+20);
        bufferImageSurface.drawLine(graphWidth+40, graphHeight+10, graphWidth+50, graphHeight+20);
        bufferImageSurface.drawLine(graphWidth+40, graphHeight+30, graphWidth+50, graphHeight+20);
        bufferImageSurface.drawLine(50+(int)(graphWidth*0.25), graphHeight+20, 50+(int)(graphWidth*0.25), graphHeight+30);
        bufferImageSurface.drawLine(50+(int)(graphWidth*0.5), graphHeight+20, 50+(int)(graphWidth*0.5), graphHeight+30);
        bufferImageSurface.drawLine(50+(int)(graphWidth*0.75), graphHeight+20, 50+(int)(graphWidth*0.75), graphHeight+30);

        bufferImageSurface.setColor(new Color(0, 0, 150));
        Font font = new Font("Arial", Font.PLAIN, 12);
        bufferImageSurface.setFont(font);
        bufferImageSurface.drawString(yString, 0, 10);
        bufferImageSurface.drawString(xString, graphWidth+70, graphHeight+50);
        //labeling x-axis
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(xMVal * 0.0), (int)(graphWidth * 0.0 + 30.0), graphHeight+50);
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(xMVal * 0.25), (int)(graphWidth * 0.25 + 30.0), graphHeight+50);
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(xMVal * 0.5), (int)(graphWidth * 0.5 + 30.0), graphHeight+50);
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(xMVal * 0.75), (int)(graphWidth * 0.75 + 30.0), graphHeight+50);
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(xMVal * 1.0), (int)(graphWidth * 1.0 + 30.0), graphHeight+50);
        //labeling y-axis
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(yMVal * 0.25), 0, (int)(graphHeight * 0.75 + 25.0));
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(yMVal * 0.5), 0, (int)(graphHeight * 0.5 + 25.0));
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(yMVal * 0.75), 0, (int)(graphHeight * 0.25 + 25.0));
        bufferImageSurface.drawString(new DecimalFormat("0.0").format(yMVal * 1.0), 0, (int)(graphHeight * 0.0 + 25.0));

        for(i2=0;i2<5;i2++){
          bufferImageSurface.setColor(colorUsed[i2]);
          bufferImageSurface.drawString(rString[i2], graphWidth + 60, i2 * 40 + 15);
          bufferImageSurface.drawString(fString[i2], graphWidth + 60, i2 * 40 + 30);
          if(i2 != graphActive){
            for(i=1;i<iPos[i2];i++){
              bufferImageSurface.drawLine(i+49, graphHeight-x[i2][i-1]+20, i+50, graphHeight-x[i2][i]+20);
            }
          }
        }
        repaint();
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Composite origComposite;

        if(bufferImage==null){
          bufferImage = (BufferedImage)createImage(graphWidth + 250, graphHeight + 55);
          bufferImageSurface = bufferImage.createGraphics();
          bufferImageSurface.setBackground(new Color(238, 238, 238));
          bufferImageSurface.clearRect(0, 0, graphWidth + 250, graphHeight + 55);
        }
        g2.drawImage(bufferImage, 0, 0, this);
    }
}

