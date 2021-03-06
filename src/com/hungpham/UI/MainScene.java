package com.hungpham.UI;

import com.hungpham.UI.controllers.GraphStage;
import gnu.io.CommPortIdentifier;
import javafx.application.Application;
import javafx.stage.Stage;
import jssc.SerialPortList;

import java.util.ArrayList;
import java.util.HashSet;

public class MainScene extends Application {
    public static volatile ArrayList<String> portsList = new ArrayList<>();

    public static int operatingDevicesNumber = 0;
    public static volatile HashSet<String> stkMacSet = new HashSet<>();



    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    private String getPortTypeName ( int portType ) {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

    public void checkPort() {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() )
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            switch ( portIdentifier.getPortType() ) {
                case CommPortIdentifier.PORT_SERIAL:
                    if (portIdentifier.getName().contains("/dev/tty.usbmodem") || portIdentifier.getName().contains("COM")) {
                        int portLastNumber = Integer.parseInt(portIdentifier.getName().substring(portIdentifier.getName().length() - 1));
                        if (portLastNumber % 2 > 0) {
                            portsList.add(portIdentifier.getName());
                            System.out.println(portIdentifier.getName());
                        }
                    }
                    break;
                default:
                    break;
            }
        }




//        if (portNames.length == 0) {
//            System.out.println("There are no serial-ports.");
//        }
//
//        for (int i = 0; i < portNames.length; i++) {
//            int portLastNumber = Integer.parseInt(portNames[i].substring(portNames[i].length() - 1));
//            if (portLastNumber % 2 > 0) {
//                portsList.add(portNames[i]);
//                System.out.println(portNames[i]);
//            }
//        }
    }

    public void checkReadyOperating() {
        operatingDevicesNumber = GraphStage.mode;
    }

}
