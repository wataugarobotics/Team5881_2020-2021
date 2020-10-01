package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;

import java.util.List;

import java.util.List;

@TeleOp(name="1p TeleOP", group="Summer Development 2020")
//@Disabled
public class OnePersonTeleOP extends OpMode {
    //Create HardwareAndMethods instance called robot
    private HardwareAndMethods robot = new HardwareAndMethods();

    private ConceptTensorFlowObjectDetection tflow = new ConceptTensorFlowObjectDetection();

    // Declares variables
    private boolean stickPressed = false;
    private boolean platformChanged = false, platformOn = false;
    private boolean parkingChanged = false, parkingOn = false;
    private boolean clawChanged = false, clawOn = false;
    private boolean capstoneChanged = false, capstoneOn = false;


    //CV stuff copy/pasted from ConceptTensorFlowObjectDetectionEngine
    private static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    //private static final String TFOD_MODEL_ASSET = "detect.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad";
    private static final String LABEL_SECOND_ELEMENT = "Single";

    /*
     * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
     * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
     * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
     * web site at https://developer.vuforia.com/license-manager.
     *
     * Vuforia license keys are always 380 characters long, and look as if they contain mostly
     * random data. As an example, here is a example of a fragment of a valid key:
     *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
     * Once you've obtained a license key, copy the string from the Vuforia web site
     * and paste it in to your code on the next line, between the double quotes.
     */
    private static final String VUFORIA_KEY =
"ATwu5Uf/////AAABmW6EkCcqK08LuKL127zX3owl9yKuVGftG+fZJh1x5DAia5zv6SAip+KJqd+P9DUA1NhLaqowDt2iraVhn4mt8C2ZJXuQd5XZiQ9Ihx6s1dvI0W+/bn0YBP2rry4keEQC2C2NVmfAtXsV+sqYVGfkHUkQ1n00L/ndbQKPE0JMWFo+sX5683ght4wyaTjjKbjSxL8gNVoCaP9ndedm+tsPdfKcSj8urqhgSOtNlAk4cTzVx1buSG33tNKy4a+JuULWYFbRngrEqPd/6MzIXFAxpMvfu0dcpCFal+VYPX/upaL4GkEEV2gfJ4xtSEqIBgnIFM8WtWwC51P2yhtGjDoqXChw0MvLDjcCTxqYT3MIjFE1";
    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    //tfod is the variable we will use to store our instance of the TensorFlow Object Detection Engine
    private TFObjectDetector tfod;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        // Initialize the hardware variables
        robot.init(hardwareMap);

        // Tell the driver that initialization is complete.
        telemetry.addData("Status", "Initialized");
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {}

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start(){
        initVuforia();
        initTfod();
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {





        //Speed mod
        if(gamepad1.right_stick_button && !stickPressed){
            stickPressed = true;
            if(robot.speedMod == 1f) {
                robot.speedMod = 2f;
            }else{
                robot.speedMod = 1f;
            }
        }else if(!gamepad1.right_stick_button){
            stickPressed = false;
        }

        //Platform servos
        if(gamepad1.right_bumper && !platformChanged) {
            robot.platformRight.setPosition(platformOn ? 0 : 1);
            robot.platformLeft.setPosition(platformOn ? 1 : 0);
            platformOn = !platformOn;
            platformChanged = true;
        } else if(!gamepad1.right_bumper) platformChanged = false;

        //Parking servo
        if(gamepad1.left_bumper && !parkingChanged) {
            robot.parking.setPosition(parkingOn ? 1 : 0);
            parkingOn = !parkingOn;
            parkingChanged = true;
        } else if(!gamepad1.left_bumper) parkingChanged = false;

        //Claw servo
        if(gamepad1.x && !clawChanged) {
            robot.claw.setPosition(clawOn ? 0 : 1);
            clawOn = !clawOn;
            clawChanged = true;
        } else if(!gamepad1.x) clawChanged = false;

        //Capstone servo
        if(gamepad1.b && !capstoneChanged) {
            robot.capstone.setPosition(capstoneOn ? 0 : 1);
            capstoneOn = !capstoneOn;
            capstoneChanged = true;
        } else if(!gamepad1.b) capstoneChanged = false;


        // Calls mechanum method
        // Mechanum uses the left stick to drive in the x,y directions, and the right stick to turn
        robot.mechanum(-gamepad1.left_stick_x, gamepad1.left_stick_y, -gamepad1.right_stick_x);

        //Calls lift method
        robot.lift(gamepad1.right_trigger - gamepad1.left_trigger);
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */
    @Override
    public void stop() {
    }


    //CV methods
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.8f;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
    }
}




