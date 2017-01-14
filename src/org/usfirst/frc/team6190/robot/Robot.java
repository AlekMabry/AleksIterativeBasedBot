
package org.usfirst.frc.team6190.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.CameraServer;

import java.io.*;
import java.net.*;


public class Robot extends IterativeRobot {
	//Beaglebone Communications
    DatagramSocket serverSocket;
    byte[] receiveData;
    byte[] sendData; 
    
    //Standard Control
    RobotDrive myRobot;  // class that handles basic drive operations
    Victor leftCIM, rightCIM; //Set pwm
    Joystick Xbox1;  // set to ID 1 in DriverStation
    Compressor compressor;
    DoubleSolenoid doubleSolenoid;
    Servo nerfDart;
    
    //Lots-o-sensors
    ADXRS450_Gyro SPIGyro;
    
    //Camera
    CameraServer server;
    
    public void robotInit() {
    	
    	//Standard Communications
        leftCIM = new Victor(1);
        rightCIM = new Victor(0);
        doubleSolenoid = new DoubleSolenoid(0, 1);
        compressor = new Compressor();
        nerfDart = new Servo(8);
        myRobot = new RobotDrive(leftCIM, rightCIM);
        myRobot.setExpiration(0.1);
        Xbox1 = new Joystick(0);
        
        //Sensors
        SPIGyro = new ADXRS450_Gyro();
        
        server = CameraServer.getInstance();
        server.setQuality(50);
        //the camera name (ex "cam0") can be found through the roborio web interface
        server.startAutomaticCapture("cam0");
    	
    	//Setup the BeagleBone Communications
    	try {
			serverSocket = new DatagramSocket(9876);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	receiveData = new byte[1024];
        sendData = new byte[1024];	
        
        
    }
    
    public void autonomousInit() {
    	int sendState = 0;
    }

    public void autonomousPeriodic() {
    	
    	compressor.setClosedLoopControl(false);
    	
    	//Receive Data
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
			serverSocket.receive(receivePacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String sentence = new String(receivePacket.getData());
        String[] parts = sentence.split(" ");
        String part1 = parts[0];
        double part1a = Double.parseDouble(part1);
        System.out.println("RECEIVED: " + part1);
        
        //Now loop until you turn the perfect amount!
        SPIGyro.reset();
        double gyroAngle = SPIGyro.getAngle();
        
        boolean direction = false;
        while (gyroAngle < part1a-2 || gyroAngle > part1a+2) {
        	if (gyroAngle < part1a-2) {
        		myRobot.tankDrive(0.85, -0.85);
        		direction = false;
        	}
        	if (gyroAngle > part1a+2) {
        		myRobot.tankDrive(-0.85, 0.85);
        		direction = true;
        	}
        	gyroAngle = SPIGyro.getAngle();
            System.out.println(gyroAngle);
        }
        if (direction == false) {
        	myRobot.tankDrive(-0.5, 0.5);
        	Timer.delay(0.050);
        	myRobot.tankDrive(0, 0);
        } else {
        	myRobot.tankDrive(0.5, -0.5);
        	Timer.delay(0.050);
        	myRobot.tankDrive(0, 0);
        }
        myRobot.tankDrive(0, 0);
        SPIGyro.reset();
        
        //Send Data
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
    	String requestValue = sentence;
        sendData = requestValue.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        try {
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Timer.delay(0.005);

    }

    public void teleopPeriodic() {
    	myRobot.setSafetyEnabled(true);
        while (isOperatorControl() && isEnabled()) {
        	double xLeft = Xbox1.getRawAxis(5);
        	double xRight = Xbox1.getRawAxis(1);
        	myRobot.tankDrive(xLeft, xRight);
        	
        	boolean shoot = Xbox1.getRawButton(3);
        	if (shoot == true) {
        		nerfDart.set(0.5);
        	} else {
        		nerfDart.set(0.0);
        	}
        	
        	boolean compressorOn = Xbox1.getRawButton(1);
        	if (compressorOn == true) {
        		compressor.setClosedLoopControl(true);
        	} else {
        		compressor.setClosedLoopControl(false);
        	}
        	
        	boolean solenoidForward = Xbox1.getRawButton(2);
        	if (solenoidForward == true) {
        		doubleSolenoid.set(DoubleSolenoid.Value.kReverse);
        	} else {
        		doubleSolenoid.set(DoubleSolenoid.Value.kForward);
        	}
            Timer.delay(0.005);		// wait for a motor update time
        }
    }
    
    public void testPeriodic() {
    
    }
    
}
