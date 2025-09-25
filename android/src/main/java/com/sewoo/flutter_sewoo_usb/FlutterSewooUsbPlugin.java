package com.sewoo.flutter_sewoo_usb;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FlutterSewooUsbPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private MethodChannel channel;
    private EventChannel eventChannel;
    private Context context;
    private SerialConnectionManager connectionManager;
    private PrinterOperations printerOperations;
    private ASBMonitor asbMonitor;
    private EventChannel.EventSink statusEventSink;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_sewoo_usb_printer");
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_sewoo_usb_printer/status");
        channel.setMethodCallHandler(this);
        eventChannel.setStreamHandler(this);

        connectionManager = SerialConnectionManager.getInstance();
        printerOperations = PrinterOperations.getInstance();
        asbMonitor = new ASBMonitor();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getAvailablePorts":
                getAvailablePorts(result);
                break;
            case "connect":
                connect(call, result);
                break;
            case "disconnect":
                disconnect(result);
                break;
            case "isConnected":
                isConnected(result);
                break;
            case "getConnectionInfo":
                getConnectionInfo(result);
                break;
            case "setEncoding":
                setEncoding(call, result);
                break;
            case "printText":
                printText(call, result);
                break;
            case "printString":
                printString(call, result);
                break;
            case "printBarcode":
                printBarcode(call, result);
                break;
            case "printQRCode":
                printQRCode(call, result);
                break;
            case "printImage":
                printImage(call, result);
                break;
            case "printImageFile":
                printImageFile(call, result);
                break;
            case "printPDF":
                printPDF(call, result);
                break;
            case "printAndroidFont":
                printAndroidFont(call, result);
                break;
            case "lineFeed":
                lineFeed(call, result);
                break;
            case "cutPaper":
                cutPaper(result);
                break;
            case "openCashDrawer":
                openCashDrawer(result);
                break;
            case "checkPrinterStatus":
                checkPrinterStatus(result);
                break;
            case "reset":
                reset(result);
                break;
            case "sendRawData":
                sendRawData(call, result);
                break;
            case "enableASBMode":
                enableASBMode(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void getAvailablePorts(Result result) {
        List<String> ports = connectionManager.getAvailablePorts();
        result.success(ports);
    }

    private void connect(MethodCall call, Result result) {
        String portName = call.argument("portName");
        Integer baudRate = call.argument("baudRate");

        if (portName == null || baudRate == null) {
            result.error("INVALID_ARGS", "Port name and baud rate are required", null);
            return;
        }

        connectionManager.connect(portName, baudRate, new SerialConnectionManager.ConnectionCallback() {
            @Override
            public void onSuccess() {
                mainHandler.post(() -> result.success(true));
            }

            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> result.error("CONNECTION_ERROR", error, null));
            }
        });
    }

    private void disconnect(Result result) {
        try {
            connectionManager.disconnect();
            result.success(null);
        } catch (Exception e) {
            result.error("DISCONNECT_ERROR", e.getMessage(), null);
        }
    }

    private void isConnected(Result result) {
        result.success(connectionManager.isConnected());
    }

    private void getConnectionInfo(Result result) {
        if (connectionManager.isConnected()) {
            Map<String, Object> info = new HashMap<>();
            info.put("portName", connectionManager.getCurrentPortName());
            info.put("baudRate", connectionManager.getCurrentBaudRate());
            info.put("isConnected", true);
            result.success(info);
        } else {
            result.success(null);
        }
    }

    private void setEncoding(MethodCall call, Result result) {
        String encoding = call.argument("encoding");
        if (encoding != null) {
            printerOperations.setEncoding(encoding);
            result.success(null);
        } else {
            result.error("INVALID_ARGS", "Encoding is required", null);
        }
    }

    private void printText(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                String text = call.argument("text");
                Integer alignment = call.argument("alignment");
                Integer fontType = call.argument("fontType");
                Integer textSize = call.argument("textSize");

                if (text == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "Text is required", null));
                    return;
                }

                printerOperations.printText(text,
                        alignment != null ? alignment : 0,
                        fontType != null ? fontType : 0,
                        textSize != null ? textSize : 0);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void printString(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                String text = call.argument("text");
                if (text == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "Text is required", null));
                    return;
                }
                printerOperations.printString(text);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void printBarcode(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                String data = call.argument("data");
                Integer barcodeType = call.argument("barcodeType");
                Integer height = call.argument("height");
                Integer width = call.argument("width");
                Integer alignment = call.argument("alignment");
                Integer hriPosition = call.argument("hriPosition");

                if (data == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "Barcode data is required", null));
                    return;
                }

                printerOperations.printBarcode(data,
                        barcodeType != null ? barcodeType : 1,
                        height != null ? height : 100,
                        width != null ? width : 2,
                        alignment != null ? alignment : 1,
                        hriPosition != null ? hriPosition : 2);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void printQRCode(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                String data = call.argument("data");
                Integer moduleSize = call.argument("moduleSize");
                Integer errorLevel = call.argument("errorLevel");

                if (data == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "QR code data is required", null));
                    return;
                }

                printerOperations.printQRCode(data,
                        moduleSize != null ? moduleSize : 4,
                        errorLevel != null ? errorLevel : 1);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void printImage(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                byte[] imageData = call.argument("imageData");
                if (imageData == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "Image data is required", null));
                    return;
                }
                printerOperations.printImage(imageData);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void printImageFile(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                String imagePath = call.argument("imagePath");
                if (imagePath == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "Image path is required", null));
                    return;
                }
                printerOperations.printImageFile(imagePath);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void printPDF(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                String pdfPath = call.argument("pdfPath");
                Integer pageNumber = call.argument("pageNumber");

                if (pdfPath == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "PDF path is required", null));
                    return;
                }

                printerOperations.printPDF(pdfPath, pageNumber != null ? pageNumber : 0);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void printAndroidFont(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                String text = call.argument("text");
                String fontFamily = call.argument("fontFamily");
                Double fontSize = call.argument("fontSize");
                Integer alignment = call.argument("alignment");

                if (text == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "Text is required", null));
                    return;
                }

                Typeface typeface = Typeface.create(fontFamily != null ? fontFamily : "sans-serif", Typeface.NORMAL);
                float size = fontSize != null ? fontSize.floatValue() : 24.0f;

                printerOperations.printAndroidFont(text, typeface, size, alignment != null ? alignment : 0);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void lineFeed(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                Integer lines = call.argument("lines");
                printerOperations.lineFeed(lines != null ? lines : 1);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void cutPaper(Result result) {
        new Thread(() -> {
            try {
                printerOperations.cutPaper();
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void openCashDrawer(Result result) {
        new Thread(() -> {
            try {
                printerOperations.openCashDrawer();
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("PRINT_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void checkPrinterStatus(Result result) {
        new Thread(() -> {
            try {
                Map<String, Object> status = printerOperations.checkPrinterStatus();
                mainHandler.post(() -> result.success(status));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("STATUS_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void reset(Result result) {
        new Thread(() -> {
            try {
                printerOperations.reset();
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("RESET_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void sendRawData(MethodCall call, Result result) {
        new Thread(() -> {
            try {
                byte[] data = call.argument("data");
                if (data == null) {
                    mainHandler.post(() -> result.error("INVALID_ARGS", "Data is required", null));
                    return;
                }
                printerOperations.sendRawData(data);
                mainHandler.post(() -> result.success(null));
            } catch (Exception e) {
                mainHandler.post(() -> result.error("SEND_ERROR", e.getMessage(), null));
            }
        }).start();
    }

    private void enableASBMode(MethodCall call, Result result) {
        Boolean enable = call.argument("enable");
        if (enable != null) {
            if (enable) {
                asbMonitor.start();
            } else {
                asbMonitor.stop();
            }
            result.success(null);
        } else {
            result.error("INVALID_ARGS", "Enable flag is required", null);
        }
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        statusEventSink = events;
        if (asbMonitor != null) {
            asbMonitor.setEventSink(events);
        }
    }

    @Override
    public void onCancel(Object arguments) {
        statusEventSink = null;
        if (asbMonitor != null) {
            asbMonitor.setEventSink(null);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        if (asbMonitor != null) {
            asbMonitor.stop();
        }
        try {
            connectionManager.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ASBMonitor {
        private Timer timer;
        private EventChannel.EventSink eventSink;

        void setEventSink(EventChannel.EventSink sink) {
            this.eventSink = sink;
        }

        void start() {
            if (timer != null) {
                stop();
            }

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (connectionManager.isConnected() && eventSink != null) {
                        try {
                            Map<String, Object> status = printerOperations.checkPrinterStatus();
                            mainHandler.post(() -> {
                                if (eventSink != null) {
                                    eventSink.success(status);
                                }
                            });
                        } catch (Exception e) {
                            mainHandler.post(() -> {
                                if (eventSink != null) {
                                    Map<String, Object> errorStatus = new HashMap<>();
                                    errorStatus.put("error", e.getMessage());
                                    eventSink.success(errorStatus);
                                }
                            });
                        }
                    }
                }
            }, 0, 1000);
        }

        void stop() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }
}