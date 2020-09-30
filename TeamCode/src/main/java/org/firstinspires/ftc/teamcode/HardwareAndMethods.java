package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class HardwareAndMethods {
    //Declares variables
    //Motors
    DcMotor leftFront = null;
    DcMotor leftBack = null;
    DcMotor rightFront = null;
    DcMotor rightBack = null;
    DcMotor liftTop = null;
    DcMotor liftBottom = null;

    public float speedMod = 1f;

    //Servos
    public Servo platformRight = null;
    public Servo platformLeft = null;
    public Servo parking = null;
    public Servo claw = null;
    public Servo capstone = null;

    //Sensors
    DcMotor forwardEncoder = null;
    DcMotor sidewaysEncoder = null;

    BNO055IMU imu;
    BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

    ColorSensor colorRight;
    ColorSensor colorLeft;
    float hsvValuesRight[] = {0F, 0F, 0F};
    float hsvValuesLeft[] = {0F, 0F, 0F};
    final double COLOR_SCALE_FACTOR = 255;
    DistanceSensor distance;

    HardwareMap hwMap = null;

    private ElapsedTime runtime = new ElapsedTime();
    private boolean firstMove = true;
    public HardwareAndMethods(){
        //Set up parameters for imu
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
    }

    public void init(HardwareMap ahwMap) {
        hwMap = ahwMap;

        // Initialize the hardware variables
        //Motors
        leftFront  = hwMap.get(DcMotor.class, "leftFront");
        leftBack = hwMap.get(DcMotor.class, "leftBack");
        rightFront = hwMap.get(DcMotor.class, "rightFront");
        rightBack = hwMap.get(DcMotor.class, "rightBack");
        liftBottom = hwMap.get(DcMotor.class, "liftBottom");
        liftTop = hwMap.get(DcMotor.class, "liftTop");

        //Servos
        platformRight = hwMap.get(Servo.class, "platformRight");
        platformLeft = hwMap.get(Servo.class, "platformLeft");
        parking = hwMap.get(Servo.class, "parking");
        claw = hwMap.get(Servo.class, "claw");
        capstone = hwMap.get(Servo.class, "capstone");

        //Sensors
        imu = hwMap.get(BNO055IMU.class, "imu");
        colorRight = hwMap.get(ColorSensor.class, "colorRight");
        colorLeft = hwMap.get(ColorSensor.class, "colorLeft");
        distance = hwMap.get(DistanceSensor.class, "distance");
        forwardEncoder = hwMap.get(DcMotor.class, "forward");
        sidewaysEncoder = hwMap.get(DcMotor.class, "sideways");

        // Set motor directions
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.FORWARD);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        // Set motor modes
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        liftBottom.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftTop.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftTop.setDirection(DcMotor.Direction.REVERSE);
        liftTop.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftTop.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        forwardEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        sidewaysEncoder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //Set servo positions
        platformRight.setPosition(0);
        platformLeft.setPosition(1);
        parking.setPosition(0);
        claw.setPosition(0);
        capstone.setPosition(0);

        //Set imu parameters
        imu.initialize(parameters);
    }

    public void testLift(float power){
        double liftPower = Range.clip(power, -1.0, 1.0);
        if(liftPower == 0 && getLiftPosition() >= 0){
            liftPower = -.07;
        }
        liftBottom.setPower(liftPower);
        liftTop.setPower(liftPower);
    }
    public void lift(float power){
        double liftPower = Range.clip(power, -1.0, 1.0);
        liftBottom.setPower(liftPower);
        liftTop.setPower(liftPower);
    }

    public void mechanum(float x, float y, float r){
        double leftFrontPower = Range.clip(y + x + r, -1.0, 1.0) / speedMod;
        double leftBackPower = Range.clip(y - x + r, -1.0, 1.0) / speedMod;
        double rightFrontPower = Range.clip(y - x - r, -1.0, 1.0) / speedMod;
        double rightBackPower = Range.clip(y + x - r, -1.0, 1.0) / speedMod;

        // Send calculated power to wheels
        leftFront.setPower(leftFrontPower);
        leftBack.setPower(leftBackPower);
        rightFront.setPower(rightFrontPower);
        rightBack.setPower(rightBackPower);
    }

    public float map(float input, float inputMin, float inputMax, float outputMin, float outputMax){
        return (input - inputMin) / (inputMax - inputMin) * (outputMax - outputMin) + outputMin;
    }

    public float getAngle(){
        Orientation orientation = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        return orientation.firstAngle;
    }

    public float getHueAvg(){
        Color.RGBToHSV((int) (colorRight.red() * COLOR_SCALE_FACTOR), (int) (colorRight.green() * COLOR_SCALE_FACTOR), (int) (colorRight.blue() * COLOR_SCALE_FACTOR), hsvValuesRight);
        Color.RGBToHSV((int) (colorLeft.red() * COLOR_SCALE_FACTOR), (int) (colorLeft.green() * COLOR_SCALE_FACTOR), (int) (colorLeft.blue() * COLOR_SCALE_FACTOR), hsvValuesLeft);
        return (hsvValuesRight[0] + hsvValuesLeft[0]) / 2;
    }

    public String getOdometry(){
        return "x: " + sidewaysEncoder.getCurrentPosition() + ", y: " + forwardEncoder.getCurrentPosition();
    }

    public int getLiftPosition(){
        return liftTop.getCurrentPosition();
    }

    public boolean wheelsAreBusy(){
        return leftFront.isBusy() || leftBack.isBusy() || rightFront.isBusy() || rightBack.isBusy();
    }
}
