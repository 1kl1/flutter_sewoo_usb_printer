import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/services.dart';

class SewooUsbPrinter {
  static const MethodChannel _channel =
      MethodChannel('flutter_sewoo_usb_printer');
  static const EventChannel _statusChannel =
      EventChannel('flutter_sewoo_usb_printer/status');

  static Stream<PrinterStatus>? _statusStream;

  static const int ALIGN_LEFT = 0;
  static const int ALIGN_CENTER = 1;
  static const int ALIGN_RIGHT = 2;

  static const int FONT_DEFAULT = 0;
  static const int FONT_BOLD = 1;
  static const int FONT_UNDERLINE = 2;
  static const int FONT_REVERSE = 4;

  static const int TEXT_SIZE_1X = 0;
  static const int TEXT_SIZE_2X = 1;
  static const int TEXT_SIZE_3X = 2;
  static const int TEXT_SIZE_4X = 3;

  static const int BARCODE_CODE39 = 0;
  static const int BARCODE_CODE128 = 1;
  static const int BARCODE_EAN8 = 2;
  static const int BARCODE_EAN13 = 3;
  static const int BARCODE_UPC_A = 4;
  static const int BARCODE_UPC_E = 5;
  static const int BARCODE_ITF = 6;
  static const int BARCODE_CODABAR = 7;

  static const int HRI_TEXT_NONE = 0;
  static const int HRI_TEXT_ABOVE = 1;
  static const int HRI_TEXT_BELOW = 2;
  static const int HRI_TEXT_BOTH = 3;

  static const int QR_ERROR_LEVEL_L = 0;
  static const int QR_ERROR_LEVEL_M = 1;
  static const int QR_ERROR_LEVEL_Q = 2;
  static const int QR_ERROR_LEVEL_H = 3;

  static Future<List<String>> getAvailablePorts() async {
    try {
      final List<dynamic> ports =
          await _channel.invokeMethod('getAvailablePorts');
      return ports.cast<String>();
    } catch (e) {
      throw FlutterSewooException('Failed to get available ports: $e');
    }
  }

  static Future<bool> connect(String portName, {int baudRate = 9600}) async {
    try {
      final bool result = await _channel.invokeMethod('connect', {
        'portName': portName,
        'baudRate': baudRate,
      });
      return result;
    } catch (e) {
      throw FlutterSewooException('Failed to connect: $e');
    }
  }

  static Future<void> disconnect() async {
    try {
      await _channel.invokeMethod('disconnect');
    } catch (e) {
      throw FlutterSewooException('Failed to disconnect: $e');
    }
  }

  static Future<bool> isConnected() async {
    try {
      final bool result = await _channel.invokeMethod('isConnected');
      return result;
    } catch (e) {
      throw FlutterSewooException('Failed to check connection status: $e');
    }
  }

  static Future<ConnectionInfo?> getConnectionInfo() async {
    try {
      final Map<dynamic, dynamic>? info =
          await _channel.invokeMethod('getConnectionInfo');
      if (info != null) {
        return ConnectionInfo.fromMap(info.cast<String, dynamic>());
      }
      return null;
    } catch (e) {
      throw FlutterSewooException('Failed to get connection info: $e');
    }
  }

  static Future<void> setEncoding(String encoding) async {
    try {
      await _channel.invokeMethod('setEncoding', {'encoding': encoding});
    } catch (e) {
      throw FlutterSewooException('Failed to set encoding: $e');
    }
  }

  static Future<void> printText(
    String text, {
    int alignment = ALIGN_LEFT,
    int fontType = FONT_DEFAULT,
    int textSize = TEXT_SIZE_1X,
  }) async {
    try {
      await _channel.invokeMethod('printText', {
        'text': text,
        'alignment': alignment,
        'fontType': fontType,
        'textSize': textSize,
      });
    } catch (e) {
      throw FlutterSewooException('Failed to print text: $e');
    }
  }

  static Future<void> printString(String text) async {
    try {
      await _channel.invokeMethod('printString', {'text': text});
    } catch (e) {
      throw FlutterSewooException('Failed to print string: $e');
    }
  }

  static Future<void> printBarcode(
    String data, {
    int barcodeType = BARCODE_CODE128,
    int height = 100,
    int width = 2,
    int alignment = ALIGN_CENTER,
    int hriPosition = HRI_TEXT_BELOW,
  }) async {
    try {
      await _channel.invokeMethod('printBarcode', {
        'data': data,
        'barcodeType': barcodeType,
        'height': height,
        'width': width,
        'alignment': alignment,
        'hriPosition': hriPosition,
      });
    } catch (e) {
      throw FlutterSewooException('Failed to print barcode: $e');
    }
  }

  static Future<void> printQRCode(
    String data, {
    int moduleSize = 4,
    int errorLevel = QR_ERROR_LEVEL_M,
  }) async {
    try {
      await _channel.invokeMethod('printQRCode', {
        'data': data,
        'moduleSize': moduleSize,
        'errorLevel': errorLevel,
      });
    } catch (e) {
      throw FlutterSewooException('Failed to print QR code: $e');
    }
  }

  static Future<void> printImage(Uint8List imageData) async {
    try {
      await _channel.invokeMethod('printImage', {'imageData': imageData});
    } catch (e) {
      throw FlutterSewooException('Failed to print image: $e');
    }
  }

  static Future<void> printImageFile(String imagePath) async {
    try {
      await _channel.invokeMethod('printImageFile', {'imagePath': imagePath});
    } catch (e) {
      throw FlutterSewooException('Failed to print image file: $e');
    }
  }

  static Future<void> printPDF(String pdfPath, {int pageNumber = 0}) async {
    try {
      await _channel.invokeMethod('printPDF', {
        'pdfPath': pdfPath,
        'pageNumber': pageNumber,
      });
    } catch (e) {
      throw FlutterSewooException('Failed to print PDF: $e');
    }
  }

  static Future<void> printAndroidFont(
    String text, {
    String fontFamily = 'sans-serif',
    double fontSize = 24.0,
    int alignment = ALIGN_LEFT,
  }) async {
    try {
      await _channel.invokeMethod('printAndroidFont', {
        'text': text,
        'fontFamily': fontFamily,
        'fontSize': fontSize,
        'alignment': alignment,
      });
    } catch (e) {
      throw FlutterSewooException('Failed to print with Android font: $e');
    }
  }

  static Future<void> lineFeed({int lines = 1}) async {
    try {
      await _channel.invokeMethod('lineFeed', {'lines': lines});
    } catch (e) {
      throw FlutterSewooException('Failed to feed lines: $e');
    }
  }

  static Future<void> cutPaper() async {
    try {
      await _channel.invokeMethod('cutPaper');
    } catch (e) {
      throw FlutterSewooException('Failed to cut paper: $e');
    }
  }

  static Future<void> openCashDrawer() async {
    try {
      await _channel.invokeMethod('openCashDrawer');
    } catch (e) {
      throw FlutterSewooException('Failed to open cash drawer: $e');
    }
  }

  static Future<PrinterStatus> checkPrinterStatus() async {
    try {
      final Map<dynamic, dynamic> status =
          await _channel.invokeMethod('checkPrinterStatus');
      return PrinterStatus.fromMap(status.cast<String, dynamic>());
    } catch (e) {
      throw FlutterSewooException('Failed to check printer status: $e');
    }
  }

  static Future<void> reset() async {
    try {
      await _channel.invokeMethod('reset');
    } catch (e) {
      throw FlutterSewooException('Failed to reset printer: $e');
    }
  }

  static Future<void> sendRawData(Uint8List data) async {
    try {
      await _channel.invokeMethod('sendRawData', {'data': data});
    } catch (e) {
      throw FlutterSewooException('Failed to send raw data: $e');
    }
  }

  static Future<void> enableASBMode(bool enable) async {
    try {
      await _channel.invokeMethod('enableASBMode', {'enable': enable});
    } catch (e) {
      throw FlutterSewooException('Failed to set ASB mode: $e');
    }
  }

  static Stream<PrinterStatus> getStatusStream() {
    _statusStream ??=
        _statusChannel.receiveBroadcastStream().map((dynamic event) {
      return PrinterStatus.fromMap((event as Map).cast<String, dynamic>());
    });
    return _statusStream!;
  }
}

class ConnectionInfo {
  final String portName;
  final int baudRate;
  final bool isConnected;

  ConnectionInfo({
    required this.portName,
    required this.baudRate,
    required this.isConnected,
  });

  factory ConnectionInfo.fromMap(Map<String, dynamic> map) {
    return ConnectionInfo(
      portName: map['portName'] ?? '',
      baudRate: map['baudRate'] ?? 0,
      isConnected: map['isConnected'] ?? false,
    );
  }
}

class PrinterStatus {
  final int statusCode;
  final bool isNormal;
  final bool isPaperEmpty;
  final bool isPaperNearEnd;
  final bool isCoverOpen;
  final bool isError;
  final bool isCashDrawerOpen;
  final String? errorMessage;

  PrinterStatus({
    required this.statusCode,
    required this.isNormal,
    required this.isPaperEmpty,
    required this.isPaperNearEnd,
    required this.isCoverOpen,
    required this.isError,
    this.isCashDrawerOpen = false,
    this.errorMessage,
  });

  factory PrinterStatus.fromMap(Map<String, dynamic> map) {
    return PrinterStatus(
      statusCode: map['statusCode'] ?? 0,
      isNormal: map['isNormal'] ?? false,
      isPaperEmpty: map['isPaperEmpty'] ?? false,
      isPaperNearEnd: map['isPaperNearEnd'] ?? false,
      isCoverOpen: map['isCoverOpen'] ?? false,
      isError: map['isError'] ?? false,
      isCashDrawerOpen: map['isCashDrawerOpen'] ?? false,
      errorMessage: map['error'],
    );
  }

  @override
  String toString() {
    if (isNormal) {
      return 'Printer Status: Normal';
    }

    List<String> issues = [];
    if (isPaperEmpty) issues.add('Paper Empty');
    if (isPaperNearEnd) issues.add('Paper Near End');
    if (isCoverOpen) issues.add('Cover Open');
    if (isError) issues.add('Printer Error');
    if (isCashDrawerOpen) issues.add('Cash Drawer Open');
    if (errorMessage != null) issues.add(errorMessage!);

    return 'Printer Status: ${issues.join(', ')}';
  }
}

class FlutterSewooException implements Exception {
  final String message;

  FlutterSewooException(this.message);

  @override
  String toString() => 'FlutterSewooException: $message';
}
