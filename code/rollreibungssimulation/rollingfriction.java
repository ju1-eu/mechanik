//Written for www.HomoFaciens.de
//Program demonstrates the cause of rolling friction.
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



public class rollingfriction extends JApplet implements  ActionListener  {

    static String versionNumber="v1.0";

    ScenePainter painter;
    GraphPainter graph01;
    Box dummyBoxCol1 = Box.createHorizontalBox(); //Empty boxes for creating fixed row width and column height
    Box dummyBoxCol3 = Box.createHorizontalBox();
    Box dummyBoxRow1 = Box.createHorizontalBox();
    Box dummyBoxRow2 = Box.createHorizontalBox();
    Box dummyBoxRow3 = Box.createHorizontalBox();
    JSpinner pressureSpinner, sizeSpinner, speedSpinner;
    SpinnerNumberModel pressureModel, sizeModel, speedModel;
    JButton calcButton;
    JCheckBox greenCheckBox, blueCheckBox, pinkCheckBox;
    JLabel pressureLabel, sizeLabel, speedLabel;
    JLabel pressureUnitLabel, sizeUnitLabel, speedUnitLabel;
    JLabel sourceLabel, versionLabel;
    double xMax=1.0, yMax=1.0;  //maximum values for graph
    int iPosNow=0;
    boolean isRunning=false; //is simulation still running?
    Timer timer;             //timer for simulation
    int timerPause=25;       //delay between two calculated frames in milliseconds
    long simulationTime, startTime, simulationTimeOld;  //times passed while simulation is running
    static String language="initalValue";
    String givenParam="nothing yet";   //Arguments passed to applet
    boolean firstPaint=false;          //clears and repaints bufferimage for graphs if true
    String speedName, sizeName, pressureName, startName, stopName, greenName, pinkName, blueName;
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
        timer = new Timer(timerPause, new timerEvent());
    }

    public static void main(String[] args) {
       String programName;

        if(args.length>0){
          if (args[0].compareTo("German")==0){
            language="German";
            programName="Rollwiderstand";
          }
          else{
            language="English";
            programName="Rolling friction";
        }
        }
        else{
          language="English";
          programName="Rolling friction";
        }

        JFrame f = new JFrame(programName);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JApplet ap = new rollingfriction();
        ap.init();
        ap.start();
        f.getContentPane().add("Center", ap);
        f.pack();
        f.setVisible(true);

    }


    private BufferedImage loadImage(String name) {
        String imgFileName = "images/"+name;
        URL url = rollingfriction.class.getResource(imgFileName);
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

        ftf = getTextField(pressureSpinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(4); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
        ftf = getTextField(sizeSpinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(4); //specify more width than we need
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
        ftf = getTextField(speedSpinner);
        if (ftf != null ) {
            ftf.setFont(font);
            ftf.setColumns(4); //specify more width than we need
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
        JPanel checkBoxPanel = new JPanel();

        //set panel properties
        controlPanel.setLayout(new GridBagLayout());
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        mainPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));


        if(language.compareTo("German")==0){
          pressureName="Druck";
          sizeName="Größe";
          speedName="Umdrehungen pro Min";
          startName="Start";
          stopName="Stop";
          greenName="Grün";
          pinkName="Pink";
          blueName="Blau";
        }
        else if(language.compareTo("XXX")==0){
          pressureName="Fahrzeit";
        }
        else{
          pressureName="Pressure";
          sizeName="Size";
          speedName="Revolutions per min";
          startName="Start";
          stopName="Stop";
          greenName="Green";
          pinkName="Pink";
          blueName="Blue";
        }

        //create buttons, labels etcetera

        calcButton = new JButton(startName);

        pressureLabel = new JLabel(pressureName + ":");
        sizeLabel = new JLabel(sizeName + ":");
        speedLabel = new JLabel(speedName + ":");

        pressureUnitLabel = new JLabel("bar");
        sizeUnitLabel = new JLabel("X");
        speedUnitLabel = new JLabel("");


        greenCheckBox= new JCheckBox(greenName);
        pinkCheckBox= new JCheckBox(pinkName);
        blueCheckBox= new JCheckBox(blueName);

        sourceLabel = new JLabel("Source & info:    www.HomoFaciens.de");
        versionLabel = new JLabel(versionNumber);

        pressureModel = new SpinnerNumberModel(1.8 , 0.8, 2.0, 0.02);
        speedModel = new SpinnerNumberModel(20.0, 1.0, 55.0, 1.0);
        sizeModel = new SpinnerNumberModel(1, 1, 3, 0.2);


        pressureSpinner = new JSpinner(pressureModel);
        sizeSpinner = new JSpinner(sizeModel);
        speedSpinner = new JSpinner(speedModel);
        setSpinnerWidth();

        painter = new ScenePainter();
        graph01 = new GraphPainter();

        graph01.xString="rad";
        graph01.yString="m";

        //set properties of controls

        pinkCheckBox.setSelected(true);
        blueCheckBox.setSelected(false);
        greenCheckBox.setSelected(false);

        painter.car = loadImage("car.png");

        pressureUnitLabel.setFont(font);
        sizeUnitLabel.setFont(font);

        pressureLabel.setFont(font);
        sizeLabel.setFont(font);

        sourceLabel.setFont(font);
        versionLabel.setFont(font);


        calcButton.setFont(font);
        calcButton.setActionCommand("calcButtonPressed");
        calcButton.addActionListener(this);


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

        //set dummyBoxes for fixed layout of columns and rows
        c.gridwidth=1;
        c.gridx = 3;
        c.gridy = 1;
        controlPanel.add(dummyBoxRow1, c);
        c.gridx = 3;
        c.gridy = 2;
        controlPanel.add(dummyBoxRow2, c);
        c.gridx = 0;
        c.gridy = 0;
        controlPanel.add(dummyBoxCol1, c);
        c.gridx = 2;
        c.gridy = 0;
        controlPanel.add(dummyBoxCol3, c);


        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(pressureLabel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(pressureSpinner, c);

        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth=1;
        controlPanel.add(pressureUnitLabel, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(sizeLabel, c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(sizeSpinner, c);

        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth=1;
        controlPanel.add(sizeUnitLabel, c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth=1;
        controlPanel.add(speedLabel, c);

        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth=1;
        controlPanel.add(speedSpinner, c);

        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth=1;
        controlPanel.add(speedUnitLabel, c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth=3;
        controlPanel.add(calcButton, c);


        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth=3;
        controlPanel.add(checkBoxPanel, c);

        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth=3;
        c.fill = GridBagConstraints.NONE;
        c.anchor=GridBagConstraints.LINE_END;
        controlPanel.add(sourceLabel, c);

        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth=3;
        c.fill = GridBagConstraints.NONE;
        c.anchor=GridBagConstraints.LINE_END;
        controlPanel.add(versionLabel, c);

        checkBoxPanel.add(greenCheckBox, c);
        checkBoxPanel.add(pinkCheckBox, c);
        checkBoxPanel.add(blueCheckBox, c);

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

        if("calcButtonPressed".equals(e.getActionCommand())) {

          if(isRunning){
            calcButton.setText(startName);
            isRunning=false;
            painter.isRunning=false;
          }
          else{
            iPosNow=0;
            firstPaint=true;
            calcButton.setText(stopName);
            startTime=System.currentTimeMillis();
            simulationTime=0;
            simulationTimeOld=0;

            if(timer.getInitialDelay()!=timerPause){
              timer.setInitialDelay(timerPause);
            }
            timer.start();
            isRunning=true;

          }//if(!isRunning)
        }//if ("calcButtonPressed".equals(e.getActionCommand())) {
    }

    private class timerEvent implements ActionListener{
      public void actionPerformed(ActionEvent e){
        simulationTimeOld=simulationTime;
        simulationTime=System.currentTimeMillis()-startTime;

        if(!isRunning){//End simulation
          timer.stop();
          isRunning=false;
          painter.isRunning=false;
          if(language.compareTo("German")==0){
            calcButton.setText("Start");
          }
          else if(language.compareTo("XXX")==0){
            calcButton.setText("Start");
          }
          else{
            calcButton.setText("Start");
          }
        }

        //car is 3meters long, picture of car is 195pixels wide: 195.0/3.0 is the number of pixels for 1meter.
        if(isRunning){

            //Programm crashes if flatness lower than 0.8, when calculating rounded edges of tire !!!
            painter.flatness=0.1666666*(pressureModel.getNumber().doubleValue()-0.8) + 0.8;
            //painter.radius=85.0*sizeModel.getNumber().doubleValue();
            painter.scale=sizeModel.getNumber().doubleValue();

            painter.wheelCycle+=(simulationTime-simulationTimeOld)  * speedModel.getNumber().doubleValue() * 0.0001047197566666666667;

            while(painter.wheelCycle>2.0*3.1415927)painter.wheelCycle-=2.0*3.1415927;
            if(isRunning){
              painter.isRunning=true;
            }

        }//if(isRunning)
        if(firstPaint){//Graph is refreshed
          graph01.refreshScene();
          graph01.xAlt=0;
          firstPaint=false;
        }
        painter.refreshScene();

        iPosNow=(int)(painter.wheelCycle*graph01.graphWidth/(3.1415927*2.0));

        int linesToPaint=0;
        if(greenCheckBox.isSelected())linesToPaint+=1;
        if(pinkCheckBox.isSelected())linesToPaint+=2;
        if(blueCheckBox.isSelected())linesToPaint+=4;
        graph01.addLine(iPosNow, painter.graphPoints, linesToPaint);
      }
    }
}



class ScenePainter extends Component {
    BufferedImage bufferImage = null;
    BufferedImage car = null;
    Graphics2D bufferImageSurface=null;
    int sceneHeight = 200, sceneWidth = 800;
    double wheelCycle=0.0, wheelCycleOld=0.0;
    boolean isRunning=false;
    double[] xPoints;
    double[] yPoints;
    int[] xPointsInt;
    int[] yPointsInt;
    int[] xPointsRim;
    int[] yPointsRim;
    int[] xPointsRimRot;
    int[] yPointsRimRot;
    int[] graphPoints;
    double radius=85.0;
    int streetHeight=10;
    double flatness=0.966666, flatnessOld=0.0;
    double scale=1.0, scaleOld=0.5;
    double markDiff12=0.0;
    double linesX=0.0;

    public Dimension getPreferredSize(){
        return new Dimension(sceneWidth, sceneHeight);
    }

    void refreshScene() {
      repaint();
    }


    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Composite origComposite;
        int i, i2;
        int nPoints=100;                      //Number of points for the tire-polygon
        int xMiddle=sceneWidth-200, yMiddle;  //Coordinates of tire center
        double x;
        int touchedStreet=0;
        int carWidth=(int)(600.0/1.35);
        int carHeight=(int)(216.0/1.35);
        double xMark01=0.0, yMark01=radius, xMark02=0.0, yMark02=radius*0.65;
        double xMarkRot01=0.0, yMarkRot01=0.0, xMarkRot02=0.0, yMarkRot02=0.0;
        int markRadius=5;
        double m1, b1, m2, x1, x2, y1, y2, beta01, beta02;
        double alpha;
        double xRot=0.0, yRot=0.0;

        //create buffer to avoid flickering while scene is calculated and painted
        if(bufferImage==null){
          bufferImage = (BufferedImage)createImage(sceneWidth, sceneHeight);
          bufferImageSurface = bufferImage.createGraphics();
          xPoints=new double[nPoints*2];
          yPoints=new double[nPoints*2];
          xPointsInt=new int[nPoints*2];
          yPointsInt=new int[nPoints*2];
          graphPoints=new int[350];
          xPointsRim=new int[11];
          yPointsRim=new int[11];
          xPointsRimRot=new int[11];
          yPointsRimRot=new int[11];
        }

        bufferImageSurface.setBackground(new Color(223, 252, 255)); 
        bufferImageSurface.clearRect(0, 0, sceneWidth, sceneHeight);
        origComposite = g2.getComposite();

        yMiddle=sceneHeight-(int)(radius * flatness * scale)-streetHeight;

        bufferImageSurface.drawImage(car, xMiddle-(int)(217.0*scale/1.35), (int)yMiddle-(int)(164.0*scale/1.35), (int)(carWidth*scale), (int)(carHeight*scale), this);

        if(flatness!=flatnessOld || scale!=scaleOld){//recalc tire
          //Calculate coordinates of a circle (tire)
          for(i=0;i<nPoints;i++){
            x=1.0 - (double)i * 2.0 / (double)(nPoints-1);
            //lower half of tire becomes flattened
            yPoints[i]=((radius+1) * Math.sqrt(1.0 - Math.pow(x, 2.0)));
            if(yPoints[i]>(radius * flatness)){
              yPoints[i]=(radius * flatness);
            }
            xPoints[i]=(x * radius);

            //upper half of tire
            yPoints[i+nPoints]=-1.0*(radius * Math.sqrt(1.0 - Math.pow(x, 2.0)));
            xPoints[i+nPoints]=-1.0*(x * radius);
          }

          //Round edges where flattened tire touches street
          //This is not the exact physical behavior of a tire, but better than just using edges
          for(i=0;i<nPoints;i++){
            if(yPoints[i]>=(radius * flatness) && touchedStreet==0){
              for(i2=0;i2<nPoints*(1.0-flatness);i2++){
                yPoints[i-i2]=(yPoints[i-i2] + (radius * flatness-(yPoints[i-i2]))*(1-i2/(nPoints*(1.0-flatness))));
              }
              touchedStreet++;
            }
            if(yPoints[i]<(radius * flatness) && touchedStreet==1){
              for(i2=0;i2<nPoints*(1.0-flatness);i2++){
                yPoints[i+i2]=(yPoints[i+i2] + (radius * flatness-(yPoints[i+i2]))*(1-i2/(nPoints*(1.0-flatness))));
              }
              touchedStreet++;
            }
          }

          //convert doubles to integer values for function fillPolygon
          for(i=0;i<nPoints*2;i++){
            xPointsInt[i]=(int)(xPoints[i]*scale + xMiddle);
            yPointsInt[i]=(int)(yPoints[i]*scale + yMiddle);
          }

          if(flatness!=flatnessOld){
            for(i2=0;i2<350;i2++){//Calculate coordinates for graph painting
              alpha=i2*(3.1415927*2.0)/349.0;
              //Coordinates of inner mark, simple coord rotation
              xMarkRot02 = xMark02 * Math.cos(alpha) + yMark02 * Math.sin(alpha);
              yMarkRot02 = -xMark02 * Math.sin(alpha) + yMark02* Math.cos(alpha);

              //Upper (=round) half of tire, simple coord rotation
              xMarkRot01 = xMark01 * Math.cos(alpha) + yMark01 * Math.sin(alpha);
              yMarkRot01 = -xMark01 * Math.sin(alpha) + yMark01* Math.cos(alpha);
              if(!(alpha>=3.1415927*0.5 && alpha<=3.1415927*1.5)){//Lower (=flatened) half
                if(alpha>3.1415927)alpha-=3.1415927*2.0;
                for(i=1;i<nPoints;i++){
                  beta01=Math.atan((double)(xPoints[i-1])/(double)(yPoints[i-1]));//Calculate angle out of x/y coordinates of tire-polygon
                  beta02=Math.atan((double)(xPoints[i])/(double)(yPoints[i]));
                  if(beta01>alpha && beta02<alpha){//Compare angle of poygon with angle of tire
                    x1=xPoints[i-1];
                    y1=yPoints[i-1];
                    x2=xPoints[i];
                    y2=yPoints[i];
                    //gradient of the line between two points of the polygon
                    m1=(y2-y1)/(x2-x1);
                    //y-axis intersept
                    b1=y1-x1*m1;
                    //Gradient of line from tire center to mark
                    m2=yMarkRot01/xMarkRot01;

                    //Intersection of the two lines
                    xMarkRot01=(-b1)/(m1-m2);
                    yMarkRot01=m1*xMarkRot01+b1;

                    break;
                  }
                }
              }
              //distance between inner and outer mark
              graphPoints[i2]=(int)(Math.sqrt(Math.pow(xMarkRot01-xMarkRot02, 2.0) + Math.pow(yMarkRot01-yMarkRot02, 2.0))*6.0);
            }
          }//if(flatness!=flatnessOld)
          flatnessOld=flatness;
          scaleOld=scale;
        }//if(flatness!=flatnessOld || radius!=radiusOld) recalc tire

        //Draw tire
        bufferImageSurface.setColor(new Color(0,0,0));
        bufferImageSurface.fillPolygon(xPointsInt, yPointsInt, nPoints*2);

        //Draw rim
        bufferImageSurface.setColor(new Color(200,200,200));
        bufferImageSurface.fillOval((int)(xMiddle-radius * scale*0.65), (int)(yMiddle-radius * scale*0.65), (int)(radius * scale*1.3), (int)(radius * scale*1.3));

        //calculate coordinates for one hole of the rim
        for(i=0;i<10;i++){
          x=0.4 - (double)i * 0.8 / (double)(10.0);
          //Rounded side
          yPointsRim[i]=(int)(radius * scale * 0.6 * Math.sqrt(1.0 - Math.pow(x, 2.0)));
          xPointsRim[i]=(int)(x * radius * scale * 0.6);
        }
        yPointsRim[10]=0;//Center point of rim
        xPointsRim[10]=0;

        //Rotate coordinates and paint 5 holes of the rim
        bufferImageSurface.setColor(new Color(0,0,0));
        for(i2=0;i2<5;i2++){
          alpha = wheelCycle + (double) i2 * 2.0 * 3.1415927 / 5.0;
          //System.out.println("alpha="  + alpha + ", i2=" + i2);
          for(i=0;i<11;i++){
            xRot = xPointsRim[i] * Math.cos(alpha) + yPointsRim[i] * Math.sin(alpha);
            yRot = -xPointsRim[i] * Math.sin(alpha) + yPointsRim[i]* Math.cos(alpha);
            xPointsRimRot[i]=(int)xRot + xMiddle;
            yPointsRimRot[i]=(int)yRot + yMiddle;
          }
          bufferImageSurface.fillPolygon(xPointsRimRot, yPointsRimRot, 11);
        }

        //Draw center of rim
        bufferImageSurface.setColor(new Color(200,200,200));
        bufferImageSurface.fillOval((int)(xMiddle-radius * scale*0.15), (int)(yMiddle-radius * scale*0.15), (int)(radius * scale*0.3), (int)(radius * scale*0.3));

        //Draw street
        bufferImageSurface.setColor(new Color(130,130,130));
        bufferImageSurface.fillRect(0, sceneHeight-streetHeight, sceneWidth, streetHeight);

        //Draw marks at the tire
        for(i2=0;i2<3;i2++){
          alpha=wheelCycle+i2*3.1415927*2.0/3.0;
          while(alpha>3.1415927*2.0)alpha-=3.1415927*2.0;

          //Coord rotation of the marks for upper half of tire
          xMarkRot01 = (xMark01 * Math.cos(alpha) + yMark01 * Math.sin(alpha))*scale;
          yMarkRot01 = (-xMark01 * Math.sin(alpha) + yMark01* Math.cos(alpha))*scale;

          if(!(alpha>=3.1415927*0.5 && alpha<=3.1415927*1.5)){//Lower half is flatened, Intersection with polygon has to be calculated
            for(i=1;i<nPoints;i++){
              beta01=Math.atan((double)(xPoints[i-1])/(double)(yPoints[i-1]));//Calculate angle out of x/y coordinates of tire-polygon
              beta02=Math.atan((double)(xPoints[i])/(double)(yPoints[i]));
              if(alpha>3.1415927)alpha-=3.1415927*2.0;
              if(beta01>alpha && beta02<alpha){//Compare angle of poygon with angle of tire
                x1=(xPoints[i-1])*scale;
                y1=(yPoints[i-1])*scale;
                x2=(xPoints[i])*scale;
                y2=(yPoints[i])*scale;
                //gradient of the line between two points of the polygon
                m1=(y2-y1)/(x2-x1);
                //y-axis intersept
                b1=y1-x1*m1;
                //Gradient of line from tire center to mark
                m2=yMarkRot01/xMarkRot01;

                //Intersection of the two lines
                xMarkRot01=(-b1)/(m1-m2);
                yMarkRot01=(m1*xMarkRot01+b1);

                break;
              }
            }
          }
          //Coordinates of the inner mark
          xMarkRot02 = (xMark02 * Math.cos(alpha) + yMark02 * Math.sin(alpha))*scale;
          yMarkRot02 = (-xMark02 * Math.sin(alpha) + yMark02* Math.cos(alpha))*scale;

          if(i2==0)bufferImageSurface.setColor(new Color(0,255,0));
          if(i2==1)bufferImageSurface.setColor(new Color(255,0,255));
          if(i2==2)bufferImageSurface.setColor(new Color(0,255,255));
          bufferImageSurface.fillOval((int)(xMiddle-markRadius + xMarkRot01), (int)(yMiddle-markRadius + yMarkRot01), markRadius*2, markRadius*2);
          bufferImageSurface.fillOval((int)(xMiddle-markRadius + xMarkRot02), (int)(yMiddle-markRadius + yMarkRot02), markRadius*2, markRadius*2);
        }

        //Draw some lines at the street to make movement visual
        bufferImageSurface.setColor(new Color(255,255,255));
        while(wheelCycleOld>wheelCycle)wheelCycleOld-=2.0*3.1415927;
        linesX+=(wheelCycle-wheelCycleOld)*radius*flatness*scale;
        while(linesX>sceneWidth*scale/20.0)linesX-=sceneWidth*scale/20.0;
        for(i=0;i<20;i++){
          bufferImageSurface.drawLine((int)(linesX+(double)(i*sceneWidth*scale)/20.0), sceneHeight, (int)(linesX+(double)(i*sceneWidth*scale)/20.0), sceneHeight-streetHeight);
        }
        wheelCycleOld=wheelCycle;

        bufferImageSurface.setColor(new Color(0,0,0));
        bufferImageSurface.drawRect(0, 0, sceneWidth-1, sceneHeight-1);

        //paint bufferImage and make it visible
        g2.drawImage(bufferImage, 0, 0, this);

        g2.setComposite(origComposite);

    }
}

class GraphPainter extends Component {
    int graphWidth=350, graphHeight=220;
    BufferedImage bufferImage=null;
    Graphics2D bufferImageSurface=null;
    int xAlt;
    int xNeu=0;
    String xString="", yString="";
    boolean refreshGraph=true;

    public Dimension getPreferredSize(){
        return new Dimension(graphWidth + 50 + 200, graphHeight + 55);
    }

    void addLine(int xNeu, int[] yValues, int linesToPaint){
      int i, i2, vPos=0;
      boolean paintGreen=false, paintPink=false, paintBlue=false;

      if(xNeu != xAlt){
        if(xAlt<xNeu){
          if(linesToPaint>3){
            paintBlue=true;
            linesToPaint-=4;
          }
          if(linesToPaint>1){
            paintPink=true;
            linesToPaint-=2;
          }
          if(linesToPaint>0){
            paintGreen=true;
          }
          //Clear area
          bufferImageSurface.setColor(new Color(238, 238, 238));
          bufferImageSurface.fillRect(xAlt+51, 20, (xNeu-xAlt), graphHeight);
          if(xAlt!=0){
            for(i2=0;i2<3;i2++){
              if((i2==0 && paintGreen) || (i2==1 && paintPink) || (i2==2 && paintBlue)){
                if(i2==0){
                  bufferImageSurface.setColor(new Color(0,255,0));
                  vPos=0;
                }
                if(i2==1){
                  bufferImageSurface.setColor(new Color(255,0,255));
                  vPos=(int)(350.0*2.0/6.0);
                }
                if(i2==2){
                  bufferImageSurface.setColor(new Color(0,255,255));
                  vPos=(int)(350.0*4.0/6.0);
                }
                for(i=xAlt;i<xNeu;i++){
                  //Paint graph
                  while(vPos+i+1>349)vPos-=349;
                  bufferImageSurface.drawLine(i+49, graphHeight-yValues[vPos+i]+20, i+50, graphHeight-yValues[vPos+i+1]+20);
                }
              }
            }
          }
        }
        else{
          //Clear area between the new x-values
          bufferImageSurface.setColor(new Color(238, 238, 238));
          bufferImageSurface.fillRect(xAlt+51, 20, (graphWidth-xAlt), graphHeight);
        }
        //Paint Cursor
        bufferImageSurface.setColor(new Color(255, 0, 0));
        bufferImageSurface.drawLine(xNeu+51, 20, xNeu+51, graphHeight+19);
        //paint destroyed parts of x/y-axis
        bufferImageSurface.setColor(new Color(0, 0, 0));
        bufferImageSurface.drawLine(graphWidth+40, graphHeight+10, graphWidth+50, graphHeight+20);
        bufferImageSurface.drawLine(50, 20, 60, 30);
        xAlt=xNeu;

        repaint();
      }
    }

    void refreshScene(){
        refreshGraph=true;
        repaint();
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Composite origComposite;


        double xMVal=2.0*3.1415927, yMVal=0.3;

        if(refreshGraph){
          if(bufferImage==null){
            bufferImage = (BufferedImage)createImage(graphWidth + 250, graphHeight + 55);
            bufferImageSurface = bufferImage.createGraphics();
            bufferImageSurface.setBackground(new Color(238, 238, 238));
          }

          xAlt=0;

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
          refreshGraph=false;
        }//if refreshGraph


        if(bufferImage==null){
          bufferImage = (BufferedImage)createImage(graphWidth + 250, graphHeight + 55);
          bufferImageSurface = bufferImage.createGraphics();
          bufferImageSurface.setBackground(new Color(238, 238, 238));
          bufferImageSurface.clearRect(0, 0, graphWidth + 250, graphHeight + 55);
        }
        g2.drawImage(bufferImage, 0, 0, this);
    }
}

 
