/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5941.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.cscore.UsbCamera;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final double speedFactor = 0.4;
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private AutoCode m_autoSelected;
	private Option optionSelected;
	private SendableChooser<AutoCode> m_chooser = new SendableChooser<>();
	private SendableChooser<Option> option_chooser = new SendableChooser<>();
	
	
	private XboxController xbox = new XboxController(0);
	private VictorSP rightA = new VictorSP(0);
	private VictorSP rightB = new VictorSP(1);
	private VictorSP leftA = new VictorSP(2);
	private VictorSP leftB = new VictorSP(3);
	
	private VictorSP wheelR = new VictorSP(4);
	private VictorSP wheelL = new VictorSP(5);
	
	private VictorSP clawTest = new VictorSP(9);
	
	private Servo claw = new Servo(8);
	
	final int closeClawAngle = 65;
	public boolean clawClosed = true;
	
	Encoder rightEncoder = new Encoder(0, 1, true, Encoder.EncodingType.k4X);
	Encoder leftEncoder = new Encoder(4, 5, false, Encoder.EncodingType.k4X);
	
	Timer t = new Timer();
	
	final int ticksPerFoot = 545;
	final int ticksFor90 = 1783;
	
	public String gameData;
	
	Side switchSide;
	Side scaleSide;
	Side robotSide;
	
	public enum AutoCode
	{
		left, right, middle, test
	}
	
	enum Side {
		left, right
	}
	
	enum Option{
		_switch, _scale
	}
	
	boolean runAuto = true;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addObject("Middle", AutoCode.middle);
		m_chooser.addObject("Left", AutoCode.left);
		m_chooser.addObject("Right", AutoCode.right);
		m_chooser.addDefault("Test", AutoCode.test);
		SmartDashboard.putData("Auto choices", m_chooser);
		
		option_chooser.addObject("Switch", Option._switch);
		option_chooser.addObject("Scale", Option._scale);
		SmartDashboard.putData("Option", option_chooser);
		
		UsbCamera camera1 = CameraServer.getInstance().startAutomaticCapture();
		UsbCamera camera2 = CameraServer.getInstance().startAutomaticCapture();

	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		optionSelected = option_chooser.getSelected();
		
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + m_autoSelected);
		
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {

		
		
		if(gameData.charAt(0) == 'L') {
			switchSide = Side.left;
		}else {
			switchSide = Side.right;
		}
		
		if(gameData.charAt(1) == 'L') {
			scaleSide = Side.left;
		}else {
			scaleSide = Side.right;
		}
		
		if(m_autoSelected == AutoCode.left) {
			robotSide = Side.left;
		}else {
			robotSide = Side.right;
		}
		
		
		if(robotSide == scaleSide && robotSide == switchSide) {
			if(optionSelected == Option._switch) {
				//switch(robotSide);
			}else if(optionSelected == Option._scale) {
				//scale(robotSide);
			}
		}else if(robotSide == switchSide) {
			//switch(robotSide);
		}else if(robotSide == scaleSide) {
			//scale(robotSide);
		}else {
			//goToNullFromBeginning(robotSide);
		}
	}
	
	public void initialExtend() {
		//retract to rest
		//extend 15 inches, gearing ratio and stuff
	}
	
	public void dropCube(int heightInches) {
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		//pivot(pivotDegree);
		//extend arm by height param
		//forward until encoder senses stop, record
		//place cube - extend arm, open claw
		//retract arm
		//go backwards by the same length as forwards variable recorded by encoder
		//pivot(pivotDegree);
	}
	
	public void goToNull(int distanceInches) {
		goForward(distanceInches);
	}
	
	public void goToNullFromBeginning(){
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		if(robotSide == scaleSide) {
			goForward(300);
		}else {
			goForward(196);
			//pivot(pivotDegree);
			//go forward until encoder senses stop
			//pivot(-pivotDegree);
			goForward(104);
		}
	}
	
	public void _switch() {
		goForward(140);
		dropCube(0);
		goToNull(160);
	}
	
	public void scale() {
		goForward(300);
		dropCube(55);
		goToNull(0);
	}
	
	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		double left = -xbox.getRawAxis(1) * speedFactor;
		double right = -xbox.getRawAxis(5) * speedFactor;
		
		SmartDashboard.putString("Right Encoder", "" + rightEncoder.getRaw());
		SmartDashboard.putString("Left Encoder", "" + leftEncoder.getRaw());
		SmartDashboard.putString("Right Speed", "" + right);
		SmartDashboard.putString("Left Speed", "" + left);
		
		setRightMotor(right);
		//setLeftMotor(left);
		
		clawTest.set(left);
		
		if(xbox.getBButton()) {
			goForward(48);
		}
		
		if(xbox.getStartButtonPressed())
		{
			rightEncoder.reset();
			leftEncoder.reset();
		}
				
		if(xbox.getXButtonPressed()) //Pull in
		{
			handleCube(1);
		}
		
		
		if(xbox.getYButtonPressed()) //Push out
		{
			handleCube(-1);
		}
		
		if(t.get() > 0.7)
		{
			wheelL.set(0);
			wheelR.set(0);
		}
		
		if(xbox.getAButtonPressed())
		{
			if(!clawClosed)
			{
				claw.setAngle(closeClawAngle);
			}
			else
			{
				claw.setAngle(closeClawAngle + 90);
			}
			clawClosed = !clawClosed;
			
		}
		
		SmartDashboard.putString("Claw", "" + claw.getAngle());
		
	}

	private void handleCube(int negate) {
		t.start();
		wheelL.set(0.25  * negate);
		wheelR.set(-0.25 * negate);
	}
	
	private void goForward(int distanceInches)
	{
		rightEncoder.reset();
		final double maximumSpeed = 0.5;
		
		final int totalTicks = ticksPerFoot * distanceInches / 12;
		
		setLeftMotor(maximumSpeed);
		setRightMotor(maximumSpeed);
		
		while(rightEncoder.getRaw() < ticksPerFoot * (distanceInches) / 12)
		{ }
		
//		setMotorSpeed(0.35);
//		while(rightEncoder.getRaw() < ticksPerFoot * distanceFeet)
//		{ }

		//setMotorSpeed(-0.1);
		setLeftMotor(-0.1);
		setRightMotor(-0.1);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setMotorSpeed(0);
		
		
	}
	
	private void swingRight()
	{
		leftEncoder.reset();		
		setLeftMotor(0.85);
		
		while(leftEncoder.getRaw() < ticksFor90 - 100)
		{}
		
		setLeftMotor(0.45);
		while(leftEncoder.getRaw() < ticksFor90)
		{}

		setLeftMotor(0);
	}
	
	private void swingLeft()
	{
		rightEncoder.reset();		
		setRightMotor(0.85);
		
		while(rightEncoder.getRaw() < ticksFor90 - 100)
		{}
		
		setRightMotor(0.45);
		while(rightEncoder.getRaw() < ticksFor90)
		{}

		setRightMotor(0);
	}

	private void setLeftMotor(double speed) {
		leftA.set(speed);
		leftB.set(speed);
	}

	private void setMotorSpeed(double speed) 
	{
		setLeftMotor(speed);
		setRightMotor(speed);
	}

	private void setRightMotor(double speed) {
		//multiplying by 0.94 is a fix for weird offset between left and right motors
		speed = speed * 0.94;
		rightA.set(-speed);
		rightB.set(-speed);
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
