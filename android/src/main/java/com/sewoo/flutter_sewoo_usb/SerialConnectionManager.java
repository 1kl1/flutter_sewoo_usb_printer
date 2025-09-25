package com.sewoo.flutter_sewoo_usb;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.sewoo.jpos.printer.LKPrint;
import com.sewoo.port.serial.jni.LKSerialPort;
import com.sewoo.port.serial.jni.LKSerialPortFinder;
import com.sewoo.request.android.RequestHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SerialConnectionManager {
    private static SerialConnectionManager instance;
    private LKSerialPort serialPort;
    private LKSerialPortFinder portFinder;
    private Thread requestHandlerThread;
    private boolean isConnected = false;
    private String currentPortName = "";
    private int currentBaudRate = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SerialConnectionManager() {
        serialPort = new LKSerialPort();
        portFinder = new LKSerialPortFinder();
    }

    public static SerialConnectionManager getInstance() {
        if (instance == null) {
            instance = new SerialConnectionManager();
        }
        return instance;
    }

    public List<String> getAvailablePorts() {
        String[] devices = portFinder.getAllDevicesPath();
        List<String> portList = new ArrayList<>();
        if (devices != null) {
            portList.addAll(Arrays.asList(devices));
        }
        return portList;
    }

    public interface ConnectionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void connect(String portName, int baudRate, ConnectionCallback callback) {
        new Thread(() -> {
            try {
                if (isConnected) {
                    disconnect();
                }

                long result = serialPort.connect(portName, baudRate);

                if (result == LKPrint.LK_SUCCESS) {
                    RequestHandler requestHandler = new RequestHandler();
                    requestHandlerThread = new Thread(requestHandler);
                    requestHandlerThread.start();

                    Thread.sleep(500);

                    if (checkConnection()) {
                        isConnected = true;
                        currentPortName = portName;
                        currentBaudRate = baudRate;
                        mainHandler.post(() -> callback.onSuccess());
                    } else {
                        cleanup();
                        mainHandler.post(() -> callback.onFailure("Failed to verify connection"));
                    }
                } else {
                    mainHandler.post(() -> callback.onFailure("Connection failed with error code: " + result));
                }
            } catch (Exception e) {
                cleanup();
                mainHandler.post(() -> callback.onFailure("Connection error: " + e.getMessage()));
            }
        }).start();
    }

    public void disconnect() throws IOException, InterruptedException {
        if (serialPort.isConnected()) {
            serialPort.disconnect();
        }

        if (requestHandlerThread != null && requestHandlerThread.isAlive()) {
            requestHandlerThread.interrupt();
            requestHandlerThread.join(1000);
            requestHandlerThread = null;
        }

        isConnected = false;
        currentPortName = "";
        currentBaudRate = 0;
    }

    private void cleanup() {
        try {
            if (serialPort.isConnected()) {
                serialPort.disconnect();
            }
            if (requestHandlerThread != null && requestHandlerThread.isAlive()) {
                requestHandlerThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkConnection() {
        try {
            return serialPort.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnected() {
        return isConnected && serialPort.isConnected();
    }

    public String getCurrentPortName() {
        return currentPortName;
    }

    public int getCurrentBaudRate() {
        return currentBaudRate;
    }

    public LKSerialPort getSerialPort() {
        return serialPort;
    }
}