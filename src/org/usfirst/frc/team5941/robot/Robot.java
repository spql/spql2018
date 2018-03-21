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
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.DigitalInput;

/*
 * 
 * time to eject cube
 * veering
 * goForwardUntilStop()
 * placing cubes on switch and scale - time
 * middle()
 * teleop - both controllers
 * what do we do after we place a cube? Go to Null?
 * 
 * 
 */
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
	private static final double speedFactor = 0.8;
	private static final double clawSpeedFactor = 0.8;
	
	private SendableChooser<Side> robotSide_chooser = new SendableChooser<>();
	private SendableChooser<Option> preference_chooser = new SendableChooser<>();

	private XboxController xbox0 = new XboxController(0);
	private XboxController xbox1 = new XboxController(1);

	private VictorSP rightDrive = new VictorSP(1);
	private VictorSP leftDrive = new VictorSP(0);	
	
	private VictorSP lift = new VictorSP(4);
	
	private VictorSP leftClaw = new VictorSP(8);
	private VictorSP rightClaw = new VictorSP(9);

	final double clawSpeedMax = 0.3;
	
	DigitalInput limitSwitch = new DigitalInput(9);
	
	Encoder rightDriveEncoder = new Encoder(0, 1, true, Encoder.EncodingType.k4X);
	Encoder leftDriveEncoder = new Encoder(4, 5, false, Encoder.EncodingType.k4X);
	
	final static int ticksPerFoot = 545;
	
	private static final int ticksPerInch = ticksPerFoot / 12;
	
	//used for pivoting
	final int ticksFor90 = (int) ((turnCircumference / 4) * ticksPerFoot / 12);
	
	public String gameData;
	
	
	Side switchSide;
	Side scaleSide;
	Side robotSide;
	Option preference;
	
	
	enum Side {
		left, middle, right
	}
	
	enum Option {
		_switch, scale, crossSwitch, crossScale
	}

	
	boolean runAuto = true;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		robotSide_chooser.addObject("Middle", Side.middle);
		robotSide_chooser.addObject("Left", Side.left);
		robotSide_chooser.addObject("Right", Side.right);
		SmartDashboard.putData("Robot Starting Position", robotSide_chooser);
		
		preference_chooser.addObject("switch", Option._switch);
		preference_chooser.addObject("scale", Option.scale);
		SmartDashboard.putData("Preference", preference_chooser);
		
		while(robotSide == null || preference == null) {
			robotSide = robotSide_chooser.getSelected();
			preference = preference_chooser.getSelected();
		}

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
		while(gameData.length() < 1) {
			gameData = DriverStation.getInstance().getGameSpecificMessage();
		}
		
		if(gameData.charAt(0) == 'L') {
			switchSide = Side.left;
		} else {
			switchSide = Side.right;
		}
		
		if(gameData.charAt(1) == 'L') {
			scaleSide = Side.left;
		}else {
			scaleSide = Side.right;
		}



//		if(switchSide == robotSide && scaleSide == robotSide) {
//			if(preference == Option._switch) {
//				_switch();
//			} else {
//				scale();
//			}
//		} else if(switchSide == robotSide){
//			_switch();
//		} else if(scaleSide == robotSide) {
//			scale();
//		} else if(switchSide == scaleSide){
//			crossSwitch();
//		} else if(robotSide == Side.middle) {
//			middle();
//		}
		
		//for testing only
		//go(140, 0.5);

		if(gameData.charAt(1) == "L"){
			go(12, 0.3);
		}else if(gameData.charAt(1) == "R"){
			go(-12, 0.3);
		}

	}
	
	//TODO:
	public void crossSwitch() {
		if(robotSide == Side.left) {
			go(219, 0.8);
			pivot(90);
			goForwardUntilStop(0.8);
			//this extra 10 inches is to fix veering
			go(10, 0.8);
			go(-10, 0.8);
			pivot(90);
			go(79, 0.8);
			pivot(90);
			goForwardUntilStop(0.8);
			//TODO: place cube
		} else {
			go(219, 0.8);
			pivot(-90);
			goForwardUntilStop(0.8);
			//this extra 10 inches is to fix veering
			go(10, 0.8);
			go(-10, 0.8);
			pivot(-90);
			go(79, 0.8);
			pivot(-90);
			goForwardUntilStop(0.8);
		}
	}
	
	/*
	 * when the robot is in the middle starting position:
	 * 1. Go forward slightly (to clear itself from the starting position wall)
	 * 2. Pivot towards and go to the switch (the alliances side)
	 * 3. Pivot back to face the switch. The robot will be facing towards the enemy alliance's side.
	 * 4. Place the cube in the switch
	 */
	public void middle() {
		LiftToSwitch();
		if(switchSide == Side.right) {
			go(10, 0.8);
			pivot(11.85);
			go(82.76, 0.8);
			pivot(-11.85);
			goForwardUntilStop(0.8);
			ejectCube();
		}else {
			go(10, 0.8);
			pivot(-51.27);
			go(129.47, 0.8);
			pivot(51.27);
			goForwardUntilStop(0.8);
			ejectCube();
		}
	}
	
	/*
	 * used when robot is parallel to switch or scale. Goal is to place cube in a switch or scale.
	 * The robot's position will be the same before and after dropCube() runs, because it will double back after placing cube.
	 */
	public void dropCubeInSwitch(int heightInches) {
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		pivot(pivotDegree);
		LiftToSwitch();
		int forwardValue = goForwardUntilStop(0.8);
		ejectCube();
		go(-forwardValue / ticksPerInch, 0.8);
		pivot(-pivotDegree);
	}
	
	public void dropCubeInScale(int heightInInches) {
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		pivot(pivotDegree);
		LiftToScale();
		int forwardValue = goForwardUntilStop(0.8);
		ejectCube();
		go(-forwardValue / ticksPerInch, 0.8);
		pivot(-pivotDegree);
	}
	
	public void ejectCube(){
		leftClaw.set(clawSpeedMax);
		rightClaw.set(-clawSpeedMax);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		leftClaw.set(0);
		rightClaw.set(0);
	}
	
	//Go to closest null area. Used after dropCube();
	public void goToNull(int distanceInches) {
		go(distanceInches, 0.8);
		pivot(180);
	}
	
	//go to null section from starting position. Used when we don't know what else to do.
	public void goToNullFromBeginning(){
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		if(robotSide == scaleSide) {
			go(300, 0.8);
			pivot(180);
		}else {
			go(196, 0.8);
			pivot(pivotDegree);
			goForwardUntilStop(0.8);
			go(-10, 0.8);
			pivot(pivotDegree);
			go(-104, 0.8);
		}
	}
	
	/*
	 * this will make the robot go forward until it hits a barrier that prevents it from continuing forward.
	 * The distance that the robot moved forward is returned.
	 */
	
	// public int goForwardUntilStop(double speed) {
	// 	leftDriveEncoder.reset();
	// 	rightDriveEncoder.reset();
	// 	setLeftMotor(speed);
	// 	setRightMotor(speed);
	// 	int lastEncoderValue = leftDriveEncoder.getRaw();
	// 	while(leftDriveEncoder.getRaw() > lastEncoderValue) {
	// 		try {
	// 			Thread.sleep(10);
	// 		} catch (InterruptedException e) {
	// 			e.printStackTrace();
	// 		}
	// 		lastEncoderValue = leftDriveEncoder.getRaw();
	// 	}
	// 	setLeftMotor(0);
	// 	setRightMotor(0);
		
	// 	return leftDriveEncoder.getRaw();
		
	// }

	public int goForwardUntilStop(double speed) {
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		setLeftMotor(speed);
		setRightMotor(speed);
		int lastEncoderValue = leftDriveEncoder.getRaw();
		while(leftDriveEncoder.getRaw() > lastEncoderValue + 5 || leftDriveEncoder.getRaw() > lastEncoderValue - 5) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			lastEncoderValue = leftDriveEncoder.getRaw();
		}
		setLeftMotor(0);
		setRightMotor(0);
		
		return leftDriveEncoder.getRaw();
		
	}
	
	//from starting position: go to switch, place cube, go to null territory.
	public void _switch() {
		go(140, 0.8);
		dropCubeInSwitch(0);
		goToNull(160);
	}
	

	//from starting position: Go to scale, place cube, go to null territory.
	public void scale() {
		go(300, 0.8);
		dropCubeInScale(55);
		goToNull(0);
	}
	
	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		double leftMotorSpeed = -xbox0.getRawAxis(1) * speedFactor;
		double rightMotorSpeed = -xbox0.getRawAxis(5) * speedFactor;
		
		double liftSpeed = -xbox1.getRawAxis(1) * speedFactor;
		
		double clawSpeed = -xbox1.getRawAxis(5) * clawSpeedFactor;
		
		leftClaw.set(clawSpeed);
		rightClaw.set(-clawSpeed);
		
//		if(xbox0.getBumper(Hand.kLeft)) {
//			leftClaw.set(clawSpeedMax);
//			rightClaw.set(-clawSpeedMax);
//		}else if(xbox0.getBumper(Hand.kRight)) {
//			leftClaw.set(clawSpeedMax);
//			rightClaw.set(-clawSpeedMax);
//		}else {
//			leftClaw.set(0);
//			rightClaw.set(0);
//		}
		
		
		if(xbox1.getAButton()) {
			//TODO winchRetract
		}
		
		if(xbox1.getYButton()) {
			//TODO winchPull
		}
		
		
		SmartDashboard.putString("Right Encoder", "" + rightDriveEncoder.getRaw());
		SmartDashboard.putString("Left Encoder", "" + leftDriveEncoder.getRaw());
		SmartDashboard.putString("Right Speed", "" + rightMotorSpeed);
		SmartDashboard.putString("Left Speed", "" + leftMotorSpeed);
		
		setRightMotor(rightMotorSpeed);
		setLeftMotor(leftMotorSpeed);
		setLift(liftSpeed);
		
		SmartDashboard.putString("claw speed max", "" + clawSpeedMax);

		if(xbox0.getStartButton()) {
            leftDriveEncoder.reset();
            rightDriveEncoder.reset();
		}
		
	}

	//move the robot straight by a number of inches. Forwards and backwards support.
	private void go(double distanceInches, double speed)
	{
		rightDriveEncoder.reset();
		final double maximumSpeed = (distanceInches > 0) ? speed : -speed;
		
		final double totalTicks = Math.abs(ticksPerInch * distanceInches);
		
		setLeftMotor(maximumSpeed);
		setRightMotor(maximumSpeed);
		
		while(Math.abs(rightDriveEncoder.getRaw()) < totalTicks)
		{ }
		
		final double stopValue = (distanceInches > 0) ? -0.1 : 0.1;
		setLeftMotor(stopValue);
		setRightMotor(stopValue);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		setMotorSpeed(0);
		
		
	}

	// private void go(double distanceInches, double speed)
	// {
	// 	rightDriveEncoder.reset();
	// 	final double maximumSpeed = (distanceInches > 0) ? speed : -speed;
		
	// 	final double totalTicks = Math.abs(ticksPerInch * distanceInches);
		
	// 	double leftSpeed = maximumSpeed;
	// 	double rightSpeed = maximumSpeed;

	// 	setLeftMotor(leftSpeed);
	// 	setRightMotor(rightSpeed);
		
	// 	while(Math.abs(rightDriveEncoder.getRaw()) < totalTicks)
	// 	{ 
	// 		if(leftEncoder.getRaw() > rightEncoder.getRaw + ticksPerInch){
	// 			leftSpeed -= 0.01;
	// 		}else if(rightEncoder.getRaw() > leftEncoder.getRaw + ticksPerInch){
	// 			rightSpeed -= 0.01;
	// 		}
	// 		setLeftMotor(leftSpeed);
	// 		setRightMotor(rightSpeed);
	// 	}
		
	// 	final double stopValue = (distanceInches > 0) ? -0.1 : 0.1;
	// 	setLeftMotor(stopValue);
	// 	setRightMotor(stopValue);
		
	// 	try {
	// 		Thread.sleep(100);
	// 	} catch (InterruptedException e) {
	// 		e.printStackTrace();
	// 	}
		
	// 	setMotorSpeed(0);
		
	// }
	
	//sets the lift speed (0 - 1)
	private void setLift(double speed) {
		lift.set(speed);
	}
	
	private void Lift(int seconds) {
		lift.set(0.5);
		try {
			Thread.sleep(seconds * 100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lift.set(0);
	}
	
	public void LiftToSwitch() {
		Lift(3);
	}
	
	public void LiftToScale() {
		//TODO: how many seconds?
		Lift(5);
	}
	
	//pivot by a number of degrees. Supports negative (counter-clockwise) values.
	private void pivot(double degree) {
		
		final double fractionOfCircle = 360 / degree;
		final double ticksForDegree = Math.abs((turnCircumference / fractionOfCircle) * ticksPerInch);
		
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		
		final double speed = (degree > 0) ? 0.5 : -0.5;
		
		setLeftMotor(speed);
		setRightMotor(-speed);
		
		while(true)
		{
			boolean rightDone = Math.abs(rightDriveEncoder.getRaw()) > ticksForDegree;
			boolean leftDone = Math.abs(leftDriveEncoder.getRaw()) > ticksForDegree;
			
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
		
		if(degree > 0){
			setLeftMotor(-.01);
			setRightMotor(.01);
		}else if(degree < 0){
			setLeftMotor(.01);
			setRightMotor(-.01);
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		setLeftMotor(0);
		setRightMotor(0);
		
	}

	//sets the left drive motor speed (0 - 1)
	private void setLeftMotor(double speed) {
		leftDrive.set(speed);
	}

	//sets both motor speeds (0 - 1)
	private void setMotorSpeed(double speed) 
	{
		setLeftMotor(speed);
		setRightMotor(speed);
	}

	//sets the right motor speed (0 - 1)
	private void setRightMotor(double speed) {
		//multiplying by 0.94 is a fix for weird offset between left and right motors
		speed = speed * 0.94;
		rightDrive.set(-speed);
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
