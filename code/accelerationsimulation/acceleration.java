//Written for www.HomoFaciens.de
//Program demonstrates correlations between force, mass, distance, time and acceleration.
//Copyright (C) 2010 Norbert Heinz
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



public class acceleration extends JApplet implements  ActionListener  {

    static String versionNumber="v1.0";

    ScenePainter painter;
    GraphPainter graph01;
    int sceneHeight=200, sceneWidth=900;
    Box dummyBoxCol1 = Box.createHorizontalBox(); //Empty boxes for creating fixed row width and column height
    Box dummyBoxCol3 = Box.createHorizontalBox();
    Box dummyBoxRow1 = Box.createHorizontalBox();
    Box dummyBoxRow2 = Box.createHorizontalBox();
    Box dummyBoxRow3 = Box.createHorizontalBox();
    JSpinner val01Spinner, val02Spinner;
    SpinnerNumberModel timeModel, speedModel, distanceModel, accelerationModel;
    JRadioButton choice01Button, choice02Button, choice03Button;
    JRadioButton satButton, vatButton;       //switches between the formulars s=2/2*t² and v=a*t
    JButton calcButton;
    JLabel val01Label, val02Label;
    JLabel val01UnitLabel, val02UnitLabel;
    JLabel sourceLabel, versionLabel;
    int[][] xKoords;          //keeps values for 5 graphs
    int graphActive=0;        //painted graphnumber for simulation (0-4)
    double xMax=1.0, yMax=1.0;  //maximum values for graph
    double coordsCount=350.0; //number of coordinates to calculate for graphpainting
    double coordStep=0.0;     //xMax/coordsCount
    int iPos[];        //x-endposition in Pixel while simulation is running
    int iPosNow=0;
    boolean isRunning=false; //is simulation still running?
    Timer timer;             //timer for simulation
    int timerPause=25;       //delay between two calculated frames in milliseconds
    String xValue="", yValue="";  //Labeling of x- and y-axis
    long simulationTime, startTime, simulationTimeOld;  //times passed while simulation is running
    String[] resultString, formularString;  //calculation results painted for each graph
    double[] speedValue;
    double[] timeValue;
    double[] distanceValue;
    double[] accelerationValue;
    double simulationSpeed=1.0;
    static String language="initalValue";
    String givenParam="nothing yet";   //Arguments passed to applet
    boolean firstPaint=false;          //clears and repaints bufferimage for graphs if true
    double truckSpeed=22.0, doveSpeed=15.0, manSpeed=1.4, sscSpeed=341.0;
    double truckAcceleration=1.25, doveAcceleration=4.5, manAcceleration=2.2, sscAcceleration=17.361;
    double doveCycle;         //Wingposition while dove is flying
    String speedName, distanceName, timeName, accelerationName, startName, stopName;
    Font font = new Font("Arial", Font.PLAIN, 12);

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
    }

    public void start() {
      int i;

        initComponents();

        xKoords=new int[5][(int)coordsCount];
        iPos=new int[5];
        speedValue=new double[5];
        timeValue=new double[5];
        distanceValue=new double[5];
        accelerationValue=new double[5];
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

        JFrame f = new JFrame("Acceleration");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JApplet ap = new acceleration();
        ap.init();
        ap.start();
        f.getContentPane().add("Center", ap);
        f.pack();
        f.setVisible(true);

    }


    private BufferedImage loadImage(String name) {
        String imgFileName = "images/"+name;
        URL url = acceleration.class.getResource(imgFileName);
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

    public void setSpinnerWidth() {
        JFormattedTextField ftf = null;

        ftf = getTextField(val01Spinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(6); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
        ftf = getTextField(val02Spinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(6); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
    }

    public void initComponents() {
        int i;
        GridBagConstraints c = new GridBagConstraints();

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


        if(language.compareTo("German")==0){
          timeName="Zeit";
          distanceName="Strecke";
          speedName="Geschwindigkeit";
          accelerationName="Beschleunigung";
          startName="Start";
          stopName="Stop";
        }
        else if(language.compareTo("XXX")==0){
          timeName="Fahrzeit";
        }
        else{
          timeName="Time";
          distanceName="Distance";
          speedName="Speed";
          accelerationName="Acceleration";
          startName="Start";
          stopName="Stop";
        }

        //create buttons, labels etcetera
        choice01Button = new JRadioButton(timeName);
        choice02Button = new JRadioButton(accelerationName);
        choice03Button = new JRadioButton(distanceName);

        calcButton = new JButton(startName);

        val01Label = new JLabel(timeName + ":");
        val02Label = new JLabel(distanceName + ":");

        vatButton = new JRadioButton("v = a * t");
        satButton = new JRadioButton("s = (a / 2) * t²");

        val01UnitLabel = new JLabel("s");
        val02UnitLabel = new JLabel("m");

        xValue="t [s]";
        yValue="s [m]";

        sourceLabel = new JLabel("Source & info:    www.HomoFaciens.de");
        versionLabel = new JLabel(versionNumber);

        timeModel = new SpinnerNumberModel(25 , 1, 60, 1);
        distanceModel = new SpinnerNumberModel(55, 1, 1000, 1);
        accelerationModel = new SpinnerNumberModel(2, 1, 30, 1);
        speedModel = new SpinnerNumberModel(20, 1, 450, 1);


        val01Spinner = new JSpinner(timeModel);
        val02Spinner = new JSpinner(distanceModel);
        setSpinnerWidth();

        painter = new ScenePainter();
        graph01 = new GraphPainter();

        //set properties of controls
        val01UnitLabel.setFont(font);
        val02UnitLabel.setFont(font);

        val01Label.setFont(font);
        val02Label.setFont(font);

        sourceLabel.setFont(font);
        versionLabel.setFont(font);

        vatButton.setFont(font);
        vatButton.setActionCommand("vatButtonPressed");
        vatButton.setSelected(false);
        vatButton.addActionListener(this);

        satButton.setFont(font);
        satButton.setActionCommand("satButtonPressed");
        satButton.setSelected(true);
        satButton.addActionListener(this);

        choice01Button.setFont(font);
        choice01Button.setActionCommand("choice01ButtonPressed");
        choice01Button.setSelected(false);
        choice01Button.addActionListener(this);

        choice02Button.setFont(font);
        choice02Button.setActionCommand("choice02ButtonPressed");
        choice02Button.setSelected(true);
        choice02Button.addActionListener(this);

        choice03Button.setFont(font);
        choice03Button.setActionCommand("choice03ButtonPressed");
        choice03Button.setSelected(false);
        choice03Button.addActionListener(this);

        calcButton.setFont(font);
        calcButton.setActionCommand("calcButtonPressed");
        calcButton.addActionListener(this);

        ButtonGroup group = new ButtonGroup();
        group.add(choice03Button);
        group.add(choice01Button);
        group.add(choice02Button);

        ButtonGroup formulaGroup = new ButtonGroup();
        formulaGroup.add(satButton);
        formulaGroup.add(vatButton);

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
        painter.signalRed = loadImage("signal_light_red.png");
        painter.signalYellow = loadImage("signal_light_yellow.png");
        painter.signalGreen = loadImage("signal_light_green.png");
        painter.signalX=painter.sceneWidth/2.0-250;

        painter.truckSound=loadSound("truck.wav");
        painter.carSound=loadSound("car.wav");
        painter.doveSound=loadSound("dove.wav");
        painter.manSound=loadSound("man.wav");
        painter.thrustSSCSound=loadSound("thrust-ssc.wav");

        painter.doveY=(Math.random() * painter.sceneHeight * 0.7);
        painter.manStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
        painter.truckStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
        painter.doveStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
        doveCycle=0.0;
        painter.sscStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
        painter.signalStartX=painter.sceneWidth/2.0-250.0;

        painter.guidePostX=0.0;
        painter.doveX=painter.doveStartX;
        painter.manX=painter.manStartX;
        painter.truckX=painter.truckStartX;
        painter.sscX=painter.sscStartX;
        painter.signalX=painter.signalStartX;
        painter.backGroundX=0.0;


        //add components
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;
        c.insets = new Insets(5,5,5,5);  //padding

        dummyBoxCol1.add(dummyBoxCol1.createRigidArea(new Dimension (130,1)));
        dummyBoxCol3.add(dummyBoxCol3.createRigidArea(new Dimension (30,1)));
        dummyBoxRow1.add(dummyBoxRow1.createRigidArea(new Dimension (1,20)));
        dummyBoxRow2.add(dummyBoxRow2.createRigidArea(new Dimension (1,20)));
        dummyBoxRow3.add(dummyBoxRow3.createRigidArea(new Dimension (1,20)));

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor=GridBagConstraints.CENTER;

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(val01Label, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(val01Spinner, c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(val01UnitLabel, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(val02Label, c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(val02Spinner, c);

        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(val02UnitLabel, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth=1;
        controlPanel.add(choice01Button, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth=1;
        controlPanel.add(choice02Button, c);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth=1;
        controlPanel.add(choice03Button, c);


        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth=2;
        controlPanel.add(satButton, c);
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth=2;
        controlPanel.add(vatButton, c);


        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth=3;
        controlPanel.add(calcButton, c);

        //set dummyBoxes for fixed layout of columns and rows
        c.gridwidth=1;
        c.gridx = 3;
        c.gridy = 1;
        controlPanel.add(dummyBoxRow1, c);
        c.gridx = 3;
        c.gridy = 2;
        controlPanel.add(dummyBoxRow2, c);
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

    public void resetValues() {
        int i, i2;
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
    }


    // Listens to the buttons.
    public void actionPerformed(ActionEvent e) {
        int i, i2;
        double coordMulti=0.5;

        if(e.getActionCommand()=="satButtonPressed"){//s=a/2*t²
          resetValues();
          choice01Button.setText(timeName);
          choice02Button.setText(accelerationName);
          choice03Button.setText(distanceName);
          choice02Button.setSelected(true);

          val01Spinner.setModel(timeModel);
          val02Spinner.setModel(distanceModel);
          setSpinnerWidth();
          val01Label.setText(timeName + ":");
          val01UnitLabel.setText("s");
          val02Label.setText(distanceName + ":");
          val02UnitLabel.setText("m");

          xValue="t [s]";
          yValue="s [m]";
        }
        if(e.getActionCommand()=="vatButtonPressed"){//v=a*t
          resetValues();
          choice01Button.setText(timeName);
          choice02Button.setText(accelerationName);
          choice03Button.setText(speedName);
          choice03Button.setSelected(true);

          val01Spinner.setModel(timeModel);
          val02Spinner.setModel(accelerationModel);
          setSpinnerWidth();
          val01Label.setText(timeName + ":");
          val01UnitLabel.setText("s");
          val02Label.setText(accelerationName + ":");
          val02UnitLabel.setText("m/s²");
          xValue="t [s]";
          yValue="a [m/s²]";
        }

        if(e.getActionCommand()=="choice01ButtonPressed"){//Runtime
          resetValues();
          if(satButton.isSelected()){//ask for acceleration and distance
            val01Spinner.setModel(accelerationModel);
            val02Spinner.setModel(distanceModel);
            val01Label.setText(accelerationName + ":");
            val01UnitLabel.setText("m/s²");
            val02Label.setText(distanceName + ":");
            val02UnitLabel.setText("m");
            xValue="s [m]";
            yValue="a [m/s²]";
          }
          if(vatButton.isSelected()){//ask for acceleration and speed
            val01Spinner.setModel(accelerationModel);
            val02Spinner.setModel(speedModel);
            val01Label.setText(accelerationName + ":");
            val01UnitLabel.setText("m/s²");
            val02Label.setText(speedName + ":");
            val02UnitLabel.setText("m/s");
            xValue="v [m/s]";
            yValue="a [m/s²]";
          }
          setSpinnerWidth();
        }//if(e.getActionCommand()=="choice01ButtonPressed")

        if(e.getActionCommand()=="choice02ButtonPressed"){// Acceleration
          resetValues();
          if(satButton.isSelected()){//ask for time and distance
            val01Spinner.setModel(timeModel);
            val02Spinner.setModel(distanceModel);
            val01Label.setText(timeName + ":");
            val01UnitLabel.setText("s");
            val02Label.setText(distanceName + ":");
            val02UnitLabel.setText("m");
            xValue="t [s]";
            yValue="s [m]";
          }
          if(vatButton.isSelected()){//ask for time and speed
            val01Spinner.setModel(timeModel);
            val02Spinner.setModel(speedModel);
            val01Label.setText(timeName + ":");
            val01UnitLabel.setText("s");
            val02Label.setText(speedName + ":");
            val02UnitLabel.setText("m/s");
            xValue="t [s]";
            yValue="v [m/s]";
          }
          setSpinnerWidth();
        }//if(e.getActionCommand()=="choice02ButtonPressed")

        if(e.getActionCommand()=="choice03ButtonPressed"){//Distance (sat) or speed (vat) ask for acceleration and time in both cases!
          resetValues();
          val01Spinner.setModel(accelerationModel);
          val02Spinner.setModel(timeModel);
          val01Label.setText(accelerationName + ":");
          val01UnitLabel.setText("m/s²");
          val02Label.setText(timeName + ":");
          val02UnitLabel.setText("s");
          xValue="t [s]";
          yValue="a [m/s²]";
          setSpinnerWidth();
        }//if(e.getActionCommand()=="choice03ButtonPressed")

        if("calcButtonPressed".equals(e.getActionCommand())) {
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
              painter.carSoundIsRunning=false;
            }
            calcButton.setText(startName);
            isRunning=false;
            painter.isRunning=false;
          }
          else{
            iPosNow=0;
            firstPaint=true;
            calcButton.setText(stopName);
            graphActive++;
            if(graphActive>4)graphActive=0;
            startTime=System.currentTimeMillis();
            simulationTime=0;
            simulationTimeOld=0;

            val01Spinner.setEnabled(false);
            val02Spinner.setEnabled(false);
            choice01Button.setEnabled(false);
            choice02Button.setEnabled(false);
            choice03Button.setEnabled(false);
            satButton.setEnabled(false);
            vatButton.setEnabled(false);

            if(choice01Button.isSelected()){
              if(satButton.isSelected()){//calculate time from distance and acceleration
                distanceValue[graphActive]=((Integer)val02Spinner.getValue()).doubleValue();
                accelerationValue[graphActive]=((Integer)val01Spinner.getValue()).doubleValue();
                timeValue[graphActive]=Math.sqrt(2.0*distanceValue[graphActive]/accelerationValue[graphActive]);
                if(accelerationValue[graphActive]>yMax)yMax=accelerationValue[graphActive];
                if(distanceValue[graphActive]>xMax)xMax=distanceValue[graphActive];
                coordStep = xMax / coordsCount;
                for(i=0;i<5;i++){
                  iPos[i]=(int) (distanceValue[i] / coordStep);
                }
                coordMulti = (double)graph01.graphHeight / yMax;
                for(i2=0;i2<5;i2++){
                  for(i=0;i<coordsCount;i++){
                    xKoords[i2][i]= (int) (accelerationValue[i2]*coordMulti);
                  }
                }
                resultString[graphActive] = "s = " + new DecimalFormat("0.0").format(val02Spinner.getValue()) + "m, a = " + new DecimalFormat("0.0").format(val01Spinner.getValue()) + "m/s²";
                formularString[graphActive] = "t = " + new DecimalFormat("0.0").format(timeValue[graphActive]) + "s";
              }
              if(vatButton.isSelected()){//calculate time from speed and acceleration
                speedValue[graphActive]=((Integer)val02Spinner.getValue()).doubleValue();
                accelerationValue[graphActive]=((Integer)val01Spinner.getValue()).doubleValue();
                timeValue[graphActive]=speedValue[graphActive]/accelerationValue[graphActive];
                if(accelerationValue[graphActive]>yMax)yMax=accelerationValue[graphActive];
                if(speedValue[graphActive]>xMax)xMax=speedValue[graphActive];
                coordStep = xMax / coordsCount;
                for(i=0;i<5;i++){
                  iPos[i]=(int) (speedValue[i] / coordStep);
                }
                coordMulti = (double)graph01.graphHeight / yMax;
                for(i2=0;i2<5;i2++){
                  for(i=0;i<coordsCount;i++){
                    xKoords[i2][i]= (int) (accelerationValue[i2]*coordMulti);
                  }
                }
                resultString[graphActive] = "v = " + new DecimalFormat("0.0").format(val02Spinner.getValue()) + "m/s, a = " + new DecimalFormat("0.0").format(val01Spinner.getValue()) + "m/s²";
                formularString[graphActive] = "t = " + new DecimalFormat("0.0").format(timeValue[graphActive]) + "s";
              }
            }//if(choice01Button.isSelected())
            if(choice02Button.isSelected()){
              if(satButton.isSelected()){//calculate acceleration from time and distance
                distanceValue[graphActive]=((Integer)val02Spinner.getValue()).doubleValue();
                timeValue[graphActive]=((Integer)val01Spinner.getValue()).doubleValue();
                accelerationValue[graphActive]=2.0 * distanceValue[graphActive] / Math.pow(timeValue[graphActive], 2.0);
                if(distanceValue[graphActive]>yMax)yMax=distanceValue[graphActive];
                if(timeValue[graphActive]>xMax)xMax=timeValue[graphActive];
                coordStep = xMax / coordsCount;
                for(i=0;i<5;i++){
                  iPos[i]=(int) (timeValue[i] / coordStep);
                }
                coordMulti = (double)graph01.graphHeight / yMax;
                for(i2=0;i2<5;i2++){
                  for(i=0;i<coordsCount;i++){
                    xKoords[i2][i]= (int) (Math.pow((double) (i) * coordStep, 2.0)*accelerationValue[i2]*coordMulti/2.0);
                  }
                }
                resultString[graphActive] = "s = " + new DecimalFormat("0.0").format(val02Spinner.getValue()) + "m, t = " + new DecimalFormat("0.0").format(val01Spinner.getValue()) + "s";
                formularString[graphActive] = "a = " + new DecimalFormat("0.0").format(accelerationValue[graphActive]) + "m/s²";
              }
              if(vatButton.isSelected()){//calculate acceleration from time and speed
                timeValue[graphActive]=((Integer)val01Spinner.getValue()).doubleValue();
                speedValue[graphActive]=((Integer)val02Spinner.getValue()).doubleValue();
                accelerationValue[graphActive]=speedValue[graphActive]/timeValue[graphActive];
                if(timeValue[graphActive]>xMax)xMax=timeValue[graphActive];
                if(speedValue[graphActive]>yMax)yMax=speedValue[graphActive];
                coordStep = xMax / coordsCount;
                for(i=0;i<5;i++){
                  iPos[i]=(int) (timeValue[i] / coordStep);
                }
                coordMulti = (double)graph01.graphHeight / yMax;
                for(i2=0;i2<5;i2++){
                  for(i=0;i<coordsCount;i++){
                    xKoords[i2][i]= (int) (i*coordStep*coordMulti*accelerationValue[i2]);
                  }
                }
                resultString[graphActive] = "v = " + new DecimalFormat("0.0").format(val02Spinner.getValue()) + "m/s, t = " + new DecimalFormat("0.0").format(val01Spinner.getValue()) + "s";
                formularString[graphActive] = "a = " + new DecimalFormat("0.0").format(accelerationValue[graphActive]) + "m/s²";
              }
            }//if(choice02Button.isSelected())
            if(choice03Button.isSelected()){
              if(satButton.isSelected()){//calculate distance from time and acceleration
                timeValue[graphActive]=((Integer)val02Spinner.getValue()).doubleValue();
                accelerationValue[graphActive]=((Integer)val01Spinner.getValue()).doubleValue();
                distanceValue[graphActive]=accelerationValue[graphActive]/2.0 *Math.pow(timeValue[graphActive], 2.0);
                if(accelerationValue[graphActive]>yMax)yMax=accelerationValue[graphActive];
                if(timeValue[graphActive]>xMax)xMax=timeValue[graphActive];
                coordStep = xMax / coordsCount;
                for(i=0;i<5;i++){
                  iPos[i]=(int) (timeValue[i] / coordStep);
                }
                coordMulti = (double)graph01.graphHeight / yMax;
                for(i2=0;i2<5;i2++){
                  for(i=0;i<coordsCount;i++){
                    xKoords[i2][i]= (int) (accelerationValue[i2]*coordMulti);
                  }
                }
                resultString[graphActive] = "t = " + new DecimalFormat("0.0").format(val02Spinner.getValue()) + "s, a = " + new DecimalFormat("0.0").format(val01Spinner.getValue()) + "m/s²";
                formularString[graphActive] = "s = " + new DecimalFormat("0.0").format(distanceValue[graphActive]) + "m";
              }
              if(vatButton.isSelected()){//calculate speed from time and acceleration
                accelerationValue[graphActive]=((Integer)val02Spinner.getValue()).doubleValue();
                timeValue[graphActive]=((Integer)val01Spinner.getValue()).doubleValue();
                speedValue[graphActive]=accelerationValue[graphActive]*timeValue[graphActive];
                if(accelerationValue[graphActive]>yMax)yMax=accelerationValue[graphActive];
                if(timeValue[graphActive]>xMax)xMax=timeValue[graphActive];
                coordStep = xMax / coordsCount;
                for(i=0;i<5;i++){
                  iPos[i]=(int) (timeValue[i] / coordStep);
                }
                coordMulti = (double)graph01.graphHeight / yMax;
                for(i2=0;i2<5;i2++){
                  for(i=0;i<coordsCount;i++){
                    xKoords[i2][i]= (int) (accelerationValue[i2]*coordMulti);
                  }
                }
                resultString[graphActive] = "a = " + new DecimalFormat("0.0").format(val02Spinner.getValue()) + "m/s², t = " + new DecimalFormat("0.0").format(val01Spinner.getValue()) + "s";
                formularString[graphActive] = "v = " + new DecimalFormat("0.0").format(speedValue[graphActive]) + "m/s";
              }
            }//if(choice03Button.isSelected())
            if(iPos[graphActive]>coordsCount)iPos[graphActive]=(int)coordsCount;
            if(timer.getInitialDelay()!=timerPause){
              timer.setInitialDelay(timerPause);
            }
            timer.start();
            isRunning=true;
            if(painter.doveX!=painter.doveStartX)painter.doveY=(Math.random() * painter.sceneHeight * 0.7);
            painter.manStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
            painter.truckStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
            painter.doveStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
            doveCycle=0.0;
            painter.sscStartX=(painter.sceneWidth-painter.car.getWidth(null))/2.0;
            painter.signalStartX=painter.sceneWidth/2.0-250.0;

            painter.guidePostX=0.0;     //Return car to starting line
            painter.doveX=painter.doveStartX;
            painter.manX=painter.manStartX;
            painter.truckX=painter.truckStartX;
            painter.sscX=painter.sscStartX;
            painter.signalX=painter.signalStartX;
            painter.backGroundX=0.0;
            painter.signalStatus=0;      //reset status of traffic light
          }//if(!isRunning)
        }//if ("calcButtonPressed".equals(e.getActionCommand())) {
    }

    private class timerEvent implements ActionListener{
      public void actionPerformed(ActionEvent e){
        simulationTimeOld=simulationTime;
        simulationTime=System.currentTimeMillis()-startTime;

        if(iPos[graphActive]<iPosNow | !isRunning){//End simulation
          iPosNow=iPos[graphActive];
          timer.stop();
          val01Spinner.setEnabled(true);
          val02Spinner.setEnabled(true);
          choice01Button.setEnabled(true);
          choice02Button.setEnabled(true);
          choice03Button.setEnabled(true);
          satButton.setEnabled(true);
          vatButton.setEnabled(true);
          isRunning=false;
          painter.isRunning=false;
          if(painter.truckSound != null){
            painter.truckSound.stop();
          }
          if(painter.carSound != null){
            painter.carSound.stop();
            painter.carSoundIsRunning=false;
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
          graph01.refreshScene(xKoords, iPos, xValue, yValue, resultString, formularString, xMax, yMax, 999);//paint all lines (999)
        }

        //car is 3meters long, picture of car is 195pixels wide: 195.0/3.0 is the number of pixels for 1meter.
        if(isRunning){
          if(painter.signalStatus>1){//Traffic light is green
            double carDistance=accelerationValue[graphActive]/2.0*Math.pow(simulationTime/1000.0, 2.0);
            double truckAccelerationTime=truckSpeed/truckAcceleration;
            double truckAccelerationDistance=truckAcceleration/2.0 * Math.pow(truckAccelerationTime, 2.0)*195.0/3.0;
            double doveAccelerationTime=doveSpeed/doveAcceleration;
            double doveAccelerationDistance=doveAcceleration/2.0 * Math.pow(doveAccelerationTime, 2.0)*195.0/3.0;
            double sscAccelerationTime=sscSpeed/sscAcceleration;
            double sscAccelerationDistance=sscAcceleration/2.0 * Math.pow(sscAccelerationTime, 2.0)*195.0/3.0;
            double manAccelerationTime=manSpeed/manAcceleration;
            double manAccelerationDistance=manAcceleration/2.0 * Math.pow(manAccelerationTime, 2.0)*195.0/3.0;
            if(satButton.isSelected()){
              if(choice01Button.isSelected()){
                iPosNow=(int)(accelerationValue[graphActive]/2.0*Math.pow(simulationTime/1000.0, 2.0)/coordStep);
              }
              if(choice02Button.isSelected() || choice03Button.isSelected()){
                iPosNow=(int)(simulationTime/1000.0/coordStep);
              }
            }
            if(vatButton.isSelected()){
              if(choice01Button.isSelected()){
                iPosNow=(int)(accelerationValue[graphActive]*simulationTime/1000.0/coordStep);
              }
              if(choice02Button.isSelected() || choice03Button.isSelected()){
                iPosNow=(int)(simulationTime/1000.0/coordStep);
              }
            }
            simulationSpeed=accelerationValue[graphActive]*simulationTime/1000.0;
            painter.guidePostX=accelerationValue[graphActive]/2.0*Math.pow(simulationTime/1000.0, 2.0)*195.0/3.0;
            painter.signalX=painter.signalStartX + accelerationValue[graphActive]/2.0*Math.pow(simulationTime/1000.0, 2.0)*195.0/3.0;
            if(simulationTime/1000.0<truckAccelerationTime){
              painter.truckX=-(truckAcceleration-accelerationValue[graphActive])/2.0*Math.pow(simulationTime/1000.0, 2.0)*195.0/3.0;
            }
            else{
              painter.truckX=(carDistance -(simulationTime/1000.0-truckAccelerationTime)*truckSpeed)*195.0/3.0-truckAccelerationDistance;
            }
            painter.truckX+=painter.truckStartX;
            if(simulationTime/1000.0<doveAccelerationTime){
              painter.doveX=-(doveAcceleration-accelerationValue[graphActive])/2.0*Math.pow(simulationTime/1000.0, 2.0)*195.0/3.0;
              doveCycle+=(simulationTime-simulationTimeOld)/25.0;
            }
            else{
              painter.doveX=(carDistance -(simulationTime/1000.0-doveAccelerationTime)*doveSpeed)*195.0/3.0-doveAccelerationDistance;
              doveCycle+=(simulationTime-simulationTimeOld)/350.0;
            }
            painter.doveX+=painter.doveStartX;
            if(simulationTime/1000.0<manAccelerationTime){
              painter.manX=-(manAcceleration-accelerationValue[graphActive])/2.0*Math.pow(simulationTime/1000.0, 2.0)*195.0/3.0;
            }
            else{
              painter.manX=(carDistance -(simulationTime/1000.0-manAccelerationTime)*manSpeed)*195.0/3.0-manAccelerationDistance;
            }
            painter.manX+=painter.manStartX;
            if(simulationTime/1000.0<sscAccelerationTime){
              painter.sscX=-(sscAcceleration-accelerationValue[graphActive])/2.0*Math.pow(simulationTime/1000.0, 2.0)*195.0/3.0;
            }
            else{
              painter.sscX=(carDistance -(simulationTime/1000.0-sscAccelerationTime)*sscSpeed)*195.0/3.0-sscAccelerationDistance;
            }
            painter.sscX+=painter.sscStartX;

            painter.manCycle+=(simulationTime-simulationTimeOld)/150.0;

            painter.backGroundX=carDistance*195.0/3.0/100.0;
            while(painter.backGroundX>painter.sceneWidth)painter.backGroundX-=sceneWidth;
            while(painter.manCycle>2.0*3.1415927)painter.manCycle-=2.0*3.1415927;
            if(iPosNow>0 && iPosNow<coordsCount){
              graph01.addLine(iPosNow, xKoords[graphActive][iPosNow], graphActive);
            }
            if(isRunning){
              painter.isRunning=true;
              if(painter.carSound!=null && !painter.carSoundIsRunning){
                painter.carSound.loop();
                painter.carSoundIsRunning=true;
              }
            }
          }
          else{//traffic light turns from red to green
            simulationSpeed=0.0;
            if(simulationTime>1000){
              startTime=System.currentTimeMillis();
              simulationTimeOld=simulationTime;
              painter.signalStatus++;
            }
            doveCycle+=(simulationTime-simulationTimeOld)/25.0;//Wing beat of dove while waiting for start
          }
          painter.doveWingHeight=(int) (59.0 * Math.sin(doveCycle));
        }//if(isRunning)
        if(firstPaint){//Graph is refreshed
          graph01.refreshScene(xKoords, iPos, xValue, yValue, resultString, formularString, xMax, yMax, graphActive);//Don't paint active graph
          graph01.xAlt=0;
          graph01.yAlt=xKoords[graphActive][0];
          firstPaint=false;
        }
        painter.refreshScene(195.0 * simulationSpeed * simulationTime / 3.0 / 1000.0, (double) simulationSpeed);
      }
    }
}



class ScenePainter extends Component {

    BufferedImage car = null, guidePost = null, truck = null, carTire=null, carTireDriving = null, backGround = null;
    BufferedImage truckTire = null, truckTireDriving = null, carSpoiler = null, carRocket01 = null, carRocket02 = null;
    BufferedImage thrustSSC=null, thrustSSCFlame01=null, thrustSSCFlame02=null;
    BufferedImage sun = null, moon = null, dove = null, doveWing = null;
    BufferedImage signalRed = null, signalYellow = null, signalGreen = null;
    BufferedImage bufferImage=null;//buffer for the whole scene painting to avoid flickering
    int doveWingHeight=10;
    int sceneHeight = 200, sceneWidth = 900;
    int i;
    double guidePostX=0.0, truckX=1000.0, backGroundX=0.0, doveX=-1000.0, doveY=10.0;
    double manX=-100.0, manSize=100.0, manCycle=0.0, manCycle2=0.0, manY=100.0, sscX=2000.0;
    double truckStartX=0.0, doveStartX=0.0, manStartX=0.0, sscStartX=0.0, signalStartX=0.0;
    double signalX=0.0;
    int[] tireHub={0,0};  //makes the tires of the car jump while driving
    int[] truckTireHub={0, 0, 0, 0, 0};
    int tireMaxHub=1, truckTireMaxHub=2;
    Graphics2D bufferImageSurface=null;
    AudioClip truckSound=null, carSound=null, doveSound=null, manSound=null, thrustSSCSound=null;
    boolean truckSoundIsRunning=false, carSoundIsRunning=false, manSoundIsRunning=false, doveSoundIsRunning=false;
    boolean isRunning=false, thrustSSCSoundIsRunning=false;
    boolean truckSoundStopped=true, carSoundStopped=true, doveSoundStopped=true, manSoundStopped=true, thrustSSCSoundStopped=true;
    double carSpeed=0.0;
    int signalStatus=0;

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
        if(sscX<sceneWidth/2.0 + 20.0 && sscX>sceneWidth/2.0 - 20.0){
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
          if(sscX<sceneWidth && sscX>-thrustSSC.getWidth(null)){//paint Super Sonic Car
            bufferImageSurface.drawImage(thrustSSC, (int) (sscX), sceneHeight-thrustSSC.getHeight(null), this);
            if(isRunning){//switching between two flame images while SSC is driving
              if(Math.random()>0.5){
                bufferImageSurface.drawImage(thrustSSCFlame01,(int)(sscX+885.0), sceneHeight-thrustSSC.getHeight(null)+80, this);
              }
              else{
                bufferImageSurface.drawImage(thrustSSCFlame02,(int)(sscX+885.0), sceneHeight-thrustSSC.getHeight(null)+80, this);
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

        if(signalX<sceneWidth){
          if(signalStatus==0){
            if(signalRed != null)bufferImageSurface.drawImage(signalRed, (int)signalX, sceneHeight-signalRed.getHeight(null), this);
          }
          if(signalStatus==1){
            if(signalYellow != null)bufferImageSurface.drawImage(signalYellow, (int)signalX, sceneHeight-signalRed.getHeight(null), this);
          }
          if(signalStatus>1){
            if(signalGreen != null)bufferImageSurface.drawImage(signalGreen, (int)signalX, sceneHeight-signalRed.getHeight(null), this);
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
                      int dontPaint){
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
          if(i2!=dontPaint){
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

