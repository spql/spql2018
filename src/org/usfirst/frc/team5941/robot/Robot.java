/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5941.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final int robotWidthInches = 25;
	private static final double turnCircumference = robotWidthInches * Math.PI;
	private static final double speedFactor = 0.6;

	private AutoCode m_autoSelected;
	private Option optionSelected;
	private SendableChooser<AutoCode> m_chooser = new SendableChooser<>();
	private SendableChooser<Option> option_chooser = new SendableChooser<>();
	
	public static final GenericHID.RumbleType kLeftRumble = null;
	public static final GenericHID.RumbleType kRightRumble = null;
	
	private XboxController xbox = new XboxController(0);
	
	private VictorSP right = new VictorSP(0);
	private VictorSP left = new VictorSP(1);	
	
	private VictorSP clawTest = new VictorSP(9);
	
	private Servo claw = new Servo(8);
	
	final int closeClawAngle = 65;
	public boolean clawClosed = true;
	
	Encoder rightEncoder = new Encoder(0, 1, true, Encoder.EncodingType.k4X);
	Encoder leftEncoder = new Encoder(4, 5, false, Encoder.EncodingType.k4X);
	
	Timer t = new Timer();
	
	final static int ticksPerFoot = 545;
	private static final int ticksPerInch = ticksPerFoot / 12;
	//used for pivoting
	final int ticksFor90 = (int) ((turnCircumference / 4) * ticksPerFoot / 12);
	
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
	
	enum RumbleType {
		KLeftRumble, KRightRumble
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
		
		CameraServer.getInstance().startAutomaticCapture();
		CameraServer.getInstance().startAutomaticCapture();

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
				_switch();
			}else if(optionSelected == Option._scale) {
				scale();
			}
		}else if(robotSide == switchSide) {
			_switch();
		}else if(robotSide == scaleSide) {
			scale();
		}else {
			goToNullFromBeginning();
		}
	}
	
	public void initialExtend() {
		//retract to rest
		//extend 15 inches, gearing ratio and stuff
	}
	
	public void dropCube(int heightInches) {
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		pivot(pivotDegree);
		//extend arm by height param
		//forward until encoder senses stop, record
		int forwardValue = goForwardUntilStop();
		//place cube - extend arm, open claw
		//retract arm
		go(-forwardValue / ticksPerInch);
		pivot(pivotDegree);
	}
	
	public void goToNull(int distanceInches) {
		go(distanceInches);
	}
	
	public void goToNullFromBeginning(){
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		if(robotSide == scaleSide) {
			go(300);
		}else {
			go(196);
			pivot(pivotDegree);
			
			//go forward until encoder senses stop
			pivot(-pivotDegree);
			go(104);
		}
	}
	
	public int goForwardUntilStop() {
		setLeftMotor(0.4);
		setRightMotor(0.4);
		leftEncoder.reset();
		rightEncoder.reset();
		int lastEncoderValue = leftEncoder.getRaw();
		while(leftEncoder.getRaw() > lastEncoderValue) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lastEncoderValue = leftEncoder.getRaw();
		}
		setLeftMotor(0);
		setRightMotor(0);
		
		return leftEncoder.getRaw();
		
	}
	
	public void _switch() {
		go(140);
		dropCube(0);
		goToNull(160);
	}
	
	public void scale() {
		go(300);
		dropCube(55);
		goToNull(0);
	}
	
	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		double leftSpeed = -xbox.getRawAxis(1) * speedFactor;
		double rightSpeed = -xbox.getRawAxis(5) * speedFactor;
		
		SmartDashboard.putString("VictorSP Value", "" + clawTest.getRaw());
		SmartDashboard.putString("Right Encoder", "" + rightEncoder.getRaw());
		SmartDashboard.putString("Left Encoder", "" + leftEncoder.getRaw());
		SmartDashboard.putString("Right Speed", "" + rightSpeed);
		SmartDashboard.putString("Left Speed", "" + leftSpeed);
		
		setRightMotor(rightSpeed);
		//setLeftMotor(left);
		
		clawTest.set(leftSpeed);
		
		if(xbox.getBButton()) {
			go(48);
		}
		

		if(xbox.getStartButton()) {
			//xbox.setRumble(kLeftRumble, 1.0);
			xbox.setRumble(kLeftRumble, 1.0);
		}
		
		if(xbox.getBackButton()) {
			//xbox.setRumble(kLeftRumble, 0.0);
			xbox.setRumble(kLeftRumble, 0.0);
		}
//		if(xbox.getStartButtonPressed())
//		{
//			rightEncoder.reset();
//			leftEncoder.reset();
//		}
				
		
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

	private void go(int distanceInches)
	{
		rightEncoder.reset();
		final double maximumSpeed = (distanceInches > 0) ? 0.5 : -0.5;
		
		final int totalTicks = Math.abs(ticksPerInch * distanceInches);
		
		setLeftMotor(maximumSpeed);
		setRightMotor(maximumSpeed);
		
		while(rightEncoder.getRaw() < totalTicks)
		{ }
		
		final double stopValue = (distanceInches > 0) ? -0.1 : 0.1;
		setLeftMotor(stopValue);
		setRightMotor(stopValue);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		setMotorSpeed(0);
		
		
	}
	
	private void pivot(int degree) {
		
		final int fractionOfCircle = 360 / degree;
		final int ticksForDegree = Math.abs((int) ((turnCircumference / fractionOfCircle) * ticksPerInch));
		
		leftEncoder.reset();
		rightEncoder.reset();
		
		final double speed = (degree > 0) ? 0.5 : -0.5;
		
		setLeftMotor(speed);
		setRightMotor(-speed);
		
		while(true)
		{
			boolean rightDone = Math.abs(rightEncoder.getRaw()) > ticksForDegree;
			boolean leftDone = Math.abs(leftEncoder.getRaw()) > ticksForDegree;
			
			if(rightDone) {
				setRightMotor(0);
			}
			if(leftDone) {
				setLeftMotor(0);
			}
			
			if(rightDone && leftDone) {
				break;
			}
		}
		
		setLeftMotor(0);
		setRightMotor(0);
		
	}

	private void setLeftMotor(double speed) {
		left.set(speed);
	}

	private void setMotorSpeed(double speed) 
	{
		setLeftMotor(speed);
		setRightMotor(speed);
	}

	private void setRightMotor(double speed) {
		//multiplying by 0.94 is a fix for weird offset between left and right motors
		speed = speed * 0.94;
		right.set(-speed);
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
