package com.hungpham.Controller;

import com.hungpham.Data.SerialData;
import com.hungpham.UI.controllers.GraphStage;
import com.hungpham.Utils.Utils;
import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import static com.hungpham.StaticControlVariables.serialCommands;
import static com.hungpham.UI.MainScene.*;

/**
 * Main control class for Serial port
 * Write and read data from Serial port
 */

public class SerialPortController implements Runnable, SerialPortEventListener {
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private static CommPortIdentifier portID;
    private Utils utils;

    private static SerialData[] serialData = new SerialData[operatingDevicesNumber];

    public static volatile int mode = 0;

    private Thread writeThread;

    private String data;

    private String completePackage = "";

    private int packageConstant = 0;

    private int conn;
    private String[] dataPackage = new String[10];

    public SerialPortController(int conn) {
        this.conn = conn;
        data = null;
        utils = new Utils();
        init();
        serialData[conn] = new SerialData(conn);
        System.out.println("Started connection with launchpad number " + conn);
    }

    /**
     * open connection with serial port and open writing session
     */
    private void init() {
        String defaultPort = null;
        defaultPort = portsList.get(conn);
        try {
            portID = CommPortIdentifier.getPortIdentifier(defaultPort);
            System.out.println("Serial port: " + portID.getName());
            serialPort = (SerialPort) portID.open("SerialPortController", 2000);
            inputStream = serialPort.getInputStream();
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnBreakInterrupt(true);
            serialPort.notifyOnCarrierDetect(true);
            serialPort.notifyOnCTS(true);
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnDSR(true);
            serialPort.notifyOnFramingError(true);
            serialPort.notifyOnOutputEmpty(true);
            serialPort.notifyOnOverrunError(true);
            serialPort.notifyOnParityError(true);
            serialPort.notifyOnRingIndicator(true);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
        /**
         * open writing thread
         */
        writeThread = new Thread(this);
        writeThread.start();
    }

    /**
     * initialize writing session
     */
    public void initWrite() {
        try {
            outputStream = serialPort.getOutputStream();
        } catch (IOException e) {
        }

        try {
            serialPort.notifyOnOutputEmpty(true);
        } catch (Exception e) {
            System.out.println("Error setting event notification");
            System.out.println(e.toString());
            System.exit(-1);
        }
    }

    /**
     * write message to serial port
     *
     * @param message
     */
    public void write(String message) {
        byte[] bytes = utils.stringToHexBytes(message);
        System.out.println("Writing \"" + message + "\" to " + serialPort.getName());
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
        }
    }

    /**
     * listen to serial port data
     *
     * @param event
     */
    public synchronized void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                while (inputStream.available() > 0) {
                    byte[] readBuffer = new byte[inputStream.available()];
                    inputStream.read(readBuffer);
                    data = utils.bytesToHexString(readBuffer);
//                    System.out.println(data);
                    if (mode == 0) {
                        System.out.println("conn: " + conn + "  " + data);
                        if (data.contains("0E6C54")) {
                            String stkMac = data.substring(data.indexOf("0E6C54") - 6, data.indexOf("0E6C54") + 6);
                            stkMacSet.add(stkMac);
                        }
                    }
                    if (mode == 1) {
                        /**
                         * add data to a queue for
                         */
//                        System.out.println(data);
                        dataPackage[packageConstant] = data;
                        packageConstant++;
                        if (packageConstant == 10) {
                            for (int i = 0; i < 10; i++) {
                                completePackage += dataPackage[i];
                            }
                            SerialData.dataPackage[conn].add(completePackage);
                            completePackage = "";
                            dataPackage = new String[10];
                            packageConstant = 0;
                        }
                    }
                }
            } catch (IOException ioe) {
                System.out.println("Exception " + ioe);
            }
        }
    }

    /**
     * execute a bunch of commands in a file with path
     *
     * @param path
     */
    private void executeControlHex(String path) {
        ArrayList<String> strings = utils.readHexFromFile(path);
        for (String s : strings) {
            write(s);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * run writing and controlling serial port thread
     */
    public void run() {
        initWrite();
        while (true) {
            try {
                write(serialCommands[conn].take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        autoRun();
    }

    private void autoRun() {
        executeControlHex("setup");
        while (GraphStage.mode == 0) ;
        if (conn == 0) {
            System.out.println("Connecting to sensortag 0");
            executeControlHex("connect1");
        }
        if (conn == 1) {
            System.out.println("Connecting to sensortag 1");
            executeControlHex("connect2");
        }
        executeControlHex("runSensor");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mode = 1;
        while (true) {
            if (GraphStage.command.equalsIgnoreCase("stop") || mode == 0) {
                mode = 0;
                break;
            }
        }
        executeControlHex("autoDisconnectHex");
        GraphStage.mode = 3;
    }

}
