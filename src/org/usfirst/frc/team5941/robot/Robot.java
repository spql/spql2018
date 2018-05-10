/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5941.robot;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
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
 * //TODO:
 * veering
 * goForwardUntilStop()
 * TEST placing cubes on switch and scale
 * middle() is just going to be crossing the line
 * teleop -- one and two controller code
 * what do we do after we place a cube?
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
	private static final int ROBOT_WIDTH_INCHES = 25;
	
	private static final double TURN_CIRCUMFERENCE = ROBOT_WIDTH_INCHES * Math.PI;
	private static final double SPEED_FACTOR = 0.8;
	private static final double LIFT_SPEED_FACTOR = 0.7;
	private static final double CLAW_SPEED_FACTOR = 0.9;
	
	private SendableChooser<Side> robotSide_chooser = new SendableChooser<>();
	private SendableChooser<Option> preference_chooser = new SendableChooser<>();

	private XboxController xbox0 = new XboxController(0);
	private XboxController xbox1 = new XboxController(1);

	private VictorSP rightDrive = new VictorSP(1);
	private VictorSP leftDrive = new VictorSP(0);	
	
	Encoder rightDriveEncoder = new Encoder(2, 3, false, Encoder.EncodingType.k4X);
	Encoder leftDriveEncoder = new Encoder(0, 1, true, Encoder.EncodingType.k4X);

	private VictorSP lift = new VictorSP(4);
	
	
	private VictorSP leftClaw = new VictorSP(9);
	private VictorSP rightClaw = new VictorSP(8);

	final double CLAW_SPEED_EJECT_MAX = .5;
	final double CLAW_SPEED_INTAKE_MAX = 0.7;
	
	DigitalInput limitSwitch = new DigitalInput(9);
	

	Encoder liftEncoder = new Encoder(4, 5, true, Encoder.EncodingType.k4X);
	
	final static int TICKS_PER_FOOT = 545;
	
	private static final int TICKS_PER_INCH = TICKS_PER_FOOT / 12;
	
	//used for pivoting
	final int TICK_FOR_90 = (int) ((TURN_CIRCUMFERENCE / 4) * TICKS_PER_INCH);
	
	public String gameData = "";
	
	
	Side switchSide;
	Side scaleSide;
	Side robotSide;
	Option preference;
	
	
	enum Side {
		left, middle, right
	}
	
	enum Option {
		_switch, scale, baseline
	}

	
	boolean runAuto = true;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		robotSide_chooser.addDefault("Left", Side.left);
		robotSide_chooser.addObject("Right", Side.right);
		SmartDashboard.putData("Robot Starting Position", robotSide_chooser);
		
		preference_chooser.addObject("switch", Option._switch);
		preference_chooser.addObject("scale", Option.scale);
		preference_chooser.addDefault("Cross Line", Option.baseline);
		SmartDashboard.putData("Preference", preference_chooser);
	}

	@Override
	public void autonomousInit() {
		Timer t = new Timer();
		t.start();
		gameData = "";
		while(gameData.length() < 1) {
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			if (t.get() > 3) {
			  baseLine();
			  return;
			}
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
		
		robotSide = robotSide_chooser.getSelected();
		preference = preference_chooser.getSelected();

        if(preference == Option._switch && robotSide == switchSide) {
            go(5, 0.3);
            try {
                    Thread.sleep(1000);
            } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            LiftToSwitch();
            go(85, 0.5);
            ejectCube();
            return;
        }else if(preference == Option.scale && robotSide == scaleSide) {
            go(5, 0.3);
            try {
                    Thread.sleep(1000);
            } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            //other stuff
            return;
        }else {
        	baseLine();
        }
        
		
//		if(preference == Option._switch) {
//			if(robotSide == switchSide) {
//				doTheSwitch();
//			}else if(robotSide == scaleSide) {
//				doTheScale();
//			}else {
//				baseLine();
//			}
//		}else if(preference == Option.scale) {
//			if(robotSide == scaleSide) {
//				doTheScale();
//			}else if(robotSide == switchSide) {
//				doTheSwitch();
//			}else {
//				baseLine();
//			}
//		}else {
//			baseLine();
//		}
		
		


	}

	private void doTheSwitch() {
		go(5, 0.3);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LiftToSwitch();
		go(82, 0.3);
		if(robotSide == Side.left) {
			pivot(45);
		}else {
			pivot(-45);
		}
		
		go(35, 0.3);
		ejectCube();
		return;
	}

	private void doTheScale() {
		go(5, 0.3);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		go(82 + 160, 0.3);
		if(robotSide == Side.left) {
			pivot(45);
		}else {
			pivot(-45);
		}
		LiftToScale();
		go(35, 0.3);
		ejectCube();
		return;
	}
	
	
	@Override
	public void autonomousPeriodic() {
		
		SmartDashboard.putString("Right Encoder", "" + rightDriveEncoder.getRaw());
		SmartDashboard.putString("Left Encoder", "" + leftDriveEncoder.getRaw());
		
	}
	
	/*
	 * when the robot is in the middle starting position:
	 * 1. Go forward slightly (to clear itself from the starting position wall)
	 * 2. Pivot towards and go to the switch (the alliances side)
	 * 3. Pivot back to face the switch. The robot will be facing towards the enemy alliance's side.
	 * 4. Place the cube in the switch
	 */
	public void baseLine(){
		go(132 - 38, 0.3);
	}
	
	public void baseLineScale(){
		go(140, 0.3);
	}
	
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
		go(-forwardValue / TICKS_PER_INCH, 0.8);
		pivot(pivotDegree);
	}
	
	public void dropCubeInScale(int heightInInches) {
		int pivotDegree = (robotSide == Side.left) ? 90 : -90;
		pivot(pivotDegree);
		LiftToScale();
		int forwardValue = goForwardUntilStop(0.8);
		ejectCube();
		go(-forwardValue / TICKS_PER_INCH, 0.8);
		pivot(pivotDegree);
	}
	
	public void ejectCube(){
		leftClaw.set(CLAW_SPEED_EJECT_MAX);
		rightClaw.set(-CLAW_SPEED_EJECT_MAX);
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
	
	public int goForwardUntilStop(double speed) {
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		setLeftMotor(speed);
		setRightMotor(speed);
		int lastEncoderValue = leftDriveEncoder.getRaw();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(leftDriveEncoder.getRaw() > lastEncoderValue) {
			try {
				Thread.sleep(10);
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
	
	public void autoLine10()
	{
		go(125, 0.7);
	}
	
	public void autoLine20()
	{
		go(240, 0.7);
	}

	@Override
	public void teleopInit() {
		liftEncoder.reset();
	}
	
	@Override
	public void teleopPeriodic() {
		double leftSpeed = -xbox0.getRawAxis(1) * SPEED_FACTOR;
		double rightSpeed = -xbox0.getRawAxis(5) * SPEED_FACTOR;
		
		// two controller code
		 double liftSpeed = xbox1.getRawAxis(1) * LIFT_SPEED_FACTOR;
		 
//		 //safegaurd for lift
//		 if(liftSpeed > 0 && liftEncoder.getRaw() > 13278) {
//			 liftSpeed = 0;
//		 } else if(liftSpeed < 0 && liftEncoder.getRaw() < 0) {
//			 liftSpeed = 0;
//		 }
		 double clawSpeed = xbox1.getRawAxis(5) * CLAW_SPEED_FACTOR;
		 leftClaw.set(-clawSpeed);
		 rightClaw.set(clawSpeed);
		
		
//		double liftSpeedUp = xbox0.getTriggerAxis(Hand.kLeft) * .8;
//		double liftSpeedDown = xbox0.getTriggerAxis(Hand.kRight) * .8;
//		double liftSpeed = (liftSpeedUp -liftSpeedDown);
		
//		if(xbox0.getBumper(Hand.kLeft)) {
//			leftClaw.set(-CLAW_SPEED_INTAKE_MAX);
//			rightClaw.set(CLAW_SPEED_INTAKE_MAX);
//		}else if(xbox0.getBumper(Hand.kRight)) {
//			leftClaw.set(CLAW_SPEED_EJECT_MAX);
//			rightClaw.set(-CLAW_SPEED_EJECT_MAX);
//		}else {
//			leftClaw.set(0);
//			rightClaw.set(0);
//		}
		
		
		SmartDashboard.putString("Right Encoder", "" + rightDriveEncoder.getRaw());
		SmartDashboard.putString("Left Encoder", "" + leftDriveEncoder.getRaw());
		SmartDashboard.putString("Right Speed", "" + rightSpeed);
		SmartDashboard.putString("lift", "" + liftEncoder.getRaw());
		SmartDashboard.putString("Left Speed", "" + leftSpeed);
		
		setRightMotor(rightSpeed);
		setLeftMotor(leftSpeed);
		setLift(liftSpeed);
		
	}

	//move the robot straight by a number of inches. Forwards and backwards support.
	private void go(double distanceInches, double speed)
	{
		
		Timer timer = new Timer();
		timer.start();
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		final double maximumSpeed = (distanceInches > 0) ? speed : -speed;
		
		final double totalTicks = Math.abs(TICKS_PER_INCH * distanceInches);
		
		double leftSpeed = maximumSpeed;
		double rightSpeed = maximumSpeed;
		
		setLeftMotor(maximumSpeed);
		setRightMotor(maximumSpeed);
		
		while(Math.abs(rightDriveEncoder.getRaw()) < totalTicks)
		{ 
//			if(leftDriveEncoder.getRaw() > rightDriveEncoder.getRaw()) {
//				leftSpeed += 0.0001;
//			}else if(leftDriveEncoder.getRaw() < rightDriveEncoder.getRaw()) {
//				rightSpeed -= 0.001;
//			}
			if(timer.get() > 3) {
				break;
			}
//			setLeftMotor(leftSpeed);
//			setRightMotor(rightSpeed);
		}
		
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
	
	//sets the lift speed (0 - 1)
	private void setLift(double speed) {
		lift.set(speed);
	}
	
	private void Lift(int ticks) {
		liftEncoder.reset();
		lift.set(-0.5);
		while(Math.abs(liftEncoder.getRaw()) < ticks) {
			if(liftEncoder.getRaw() > 13278) {
				break;
			}
		}
		lift.set(0);
	}
	
	public void LiftToSwitch() {
		Lift(9000);
	}
	
	public void LiftToScale() {
		Lift(13278);
	}
	
	//pivot by a number of degrees. Supports negative (counter-clockwise) values.
	private void pivot(double degree) {
		
		final double FRACTION_OF_CIRCLE = degree / 360;
		final double TICKS_FOR_DEGREE = Math.abs((TURN_CIRCUMFERENCE * FRACTION_OF_CIRCLE) * TICKS_PER_INCH);
		
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		
		final double speed = (degree > 0) ? 0.3 : -0.3;
		
		setLeftMotor(speed);
		setRightMotor(-speed);
		
		while(true)
		{
			boolean rightDone = Math.abs(rightDriveEncoder.getRaw()) > TICKS_FOR_DEGREE;
			boolean leftDone = Math.abs(leftDriveEncoder.getRaw()) > TICKS_FOR_DEGREE;
			
			if(rightDone) {
				setRightMotor(0);
			}
			if(leftDone) {
				setLeftMotor(0);
			}
			
			//TODO change back to ampersand if needed
			if(rightDone || leftDone) {
				break;
			}

		}
		final double stopValue = (degree > 0) ? -0.1 : 0.1;
		setLeftMotor(stopValue);
		setRightMotor(-stopValue);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		setMotorSpeed(0);
		
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
		rightDrive.set(-speed * .92);
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
