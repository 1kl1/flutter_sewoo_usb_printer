# flutter_sewoo_usb_printer

A Flutter plugin for Sewoo POS printers using USB/Serial connection. This plugin provides a comprehensive wrapper around the Sewoo Android SDK 1.114 library for USB connectivity.

## Features

- **USB Serial Connection**: Auto-detect and connect to USB serial ports
- **Text Printing**: Multiple fonts, sizes, and alignments
- **Barcode Support**: CODE39, CODE128, EAN8/13, UPC-A/E, ITF, CODABAR
- **QR Code**: Configurable size and error correction levels
- **Image & PDF Printing**: Direct printing from bytes or file paths
- **Status Monitoring**: Real-time printer status via ASB mode
- **ESC/POS Commands**: Full raw command support
- **Multi-encoding**: UTF-8, EUC-KR, BIG5, GB2312, Shift_JIS
- **Cash Drawer Control**: Open command support

## Installation

Add to your `pubspec.yaml`:

```yaml
dependencies:
  flutter_sewoo_usb_printer: ^0.0.1
```

### Android Configuration

Add permissions to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

For Android 10+ (API 29+):

```xml
<application android:requestLegacyExternalStorage="true">
```

## Quick Start

```dart
import 'package:flutter_sewoo_usb_printer/flutter_sewoo_usb_printer.dart';

// Get available ports
List<String> ports = await FlutterSewooUsbPrinter.getAvailablePorts();

// Connect to printer
bool connected = await FlutterSewooUsbPrinter.connect(
  ports.first,
  baudRate: 9600, // 9600, 19200, 38400, 57600, 115200
);

// Print text
await FlutterSewooUsbPrinter.printText(
  'Hello World',
  alignment: FlutterSewooUsbPrinter.ALIGN_CENTER,
  fontType: FlutterSewooUsbPrinter.FONT_BOLD,
  textSize: FlutterSewooUsbPrinter.TEXT_SIZE_2X,
);

// Print barcode
await FlutterSewooUsbPrinter.printBarcode(
  '1234567890',
  barcodeType: FlutterSewooUsbPrinter.BARCODE_CODE128,
  height: 100,
  width: 2,
  alignment: FlutterSewooUsbPrinter.ALIGN_CENTER,
  hriPosition: FlutterSewooUsbPrinter.HRI_TEXT_BELOW,
);

// Print QR code
await FlutterSewooUsbPrinter.printQRCode(
  'https://example.com',
  moduleSize: 6,
  errorLevel: FlutterSewooUsbPrinter.QR_ERROR_LEVEL_M,
);

// Cut paper and disconnect
await FlutterSewooUsbPrinter.cutPaper();
await FlutterSewooUsbPrinter.disconnect();
```

## Core Methods

### Connection Management

```dart
// Get available USB serial ports
List<String> ports = await FlutterSewooUsbPrinter.getAvailablePorts();

// Connect to printer
bool connected = await FlutterSewooUsbPrinter.connect(portName, baudRate: 9600);

// Check connection status
bool isConnected = await FlutterSewooUsbPrinter.isConnected();

// Get connection info
Map<String, dynamic> info = await FlutterSewooUsbPrinter.getConnectionInfo();

// Disconnect
await FlutterSewooUsbPrinter.disconnect();
```

### Text Printing

```dart
// Print formatted text
await FlutterSewooUsbPrinter.printText(
  text,
  alignment: FlutterSewooUsbPrinter.ALIGN_CENTER,    // LEFT, CENTER, RIGHT
  fontType: FlutterSewooUsbPrinter.FONT_BOLD,       // DEFAULT, BOLD, UNDERLINE, REVERSE
  textSize: FlutterSewooUsbPrinter.TEXT_SIZE_2X,    // 1X, 2X, 3X, 4X
);

// Print raw string
await FlutterSewooUsbPrinter.printString('Simple text\n');

// Print with Android fonts
await FlutterSewooUsbPrinter.printAndroidFont(
  text,
  fontFamily: 'monospace',
  fontSize: 24,
  alignment: FlutterSewooUsbPrinter.ALIGN_LEFT,
);
```

### Barcode & QR Code

```dart
// Print barcode
await FlutterSewooUsbPrinter.printBarcode(
  data,
  barcodeType: FlutterSewooUsbPrinter.BARCODE_CODE128,
  height: 100,
  width: 2,
  alignment: FlutterSewooUsbPrinter.ALIGN_CENTER,
  hriPosition: FlutterSewooUsbPrinter.HRI_TEXT_BELOW,
);

// Print QR code
await FlutterSewooUsbPrinter.printQRCode(
  data,
  moduleSize: 6,  // 1-16
  errorLevel: FlutterSewooUsbPrinter.QR_ERROR_LEVEL_M,  // L, M, Q, H
);
```

### Image & PDF Printing

```dart
// Print image from bytes
Uint8List imageData = // ... your image data
await FlutterSewooUsbPrinter.printImage(imageData);

// Print image from file
await FlutterSewooUsbPrinter.printImageFile('/path/to/image.png');

// Print PDF page
await FlutterSewooUsbPrinter.printPDF(
  '/path/to/document.pdf',
  pageNumber: 0,  // 0-based index
);
```

### Printer Control

```dart
// Line feed
await FlutterSewooUsbPrinter.lineFeed(lines: 3);

// Cut paper
await FlutterSewooUsbPrinter.cutPaper();

// Open cash drawer
await FlutterSewooUsbPrinter.openCashDrawer();

// Reset printer
await FlutterSewooUsbPrinter.reset();

// Send raw ESC/POS commands
await FlutterSewooUsbPrinter.sendRawData(Uint8List.fromList([0x1B, 0x40]));
```

### Status Monitoring

```dart
// Check status once
PrinterStatus status = await FlutterSewooUsbPrinter.checkPrinterStatus();
if (status.isPaperEmpty) {
  print('Paper is empty!');
}

// Enable real-time monitoring
await FlutterSewooUsbPrinter.enableASBMode(true);

// Listen to status updates
FlutterSewooUsbPrinter.getStatusStream().listen((status) {
  if (status.isError) {
    print('Error: ${status.errorMessage}');
  }
});
```

## PrinterStatus Object

```dart
class PrinterStatus {
  final int statusCode;        // Raw status code
  final bool isNormal;         // Printer ready
  final bool isPaperEmpty;     // No paper
  final bool isPaperNearEnd;   // Low paper
  final bool isCoverOpen;      // Cover open
  final bool isError;          // Error state
  final bool isCashDrawerOpen; // Drawer open
  final String? errorMessage;  // Error details
}
```

## Constants Reference

### Alignment
- `ALIGN_LEFT` - Left alignment
- `ALIGN_CENTER` - Center alignment
- `ALIGN_RIGHT` - Right alignment

### Font Types
- `FONT_DEFAULT` - Normal font
- `FONT_BOLD` - Bold font
- `FONT_UNDERLINE` - Underlined text
- `FONT_REVERSE` - Reverse video

### Text Sizes
- `TEXT_SIZE_1X` - Normal size
- `TEXT_SIZE_2X` - Double size
- `TEXT_SIZE_3X` - Triple size
- `TEXT_SIZE_4X` - Quadruple size

### Barcode Types
- `BARCODE_CODE39`, `BARCODE_CODE128`
- `BARCODE_EAN8`, `BARCODE_EAN13`
- `BARCODE_UPC_A`, `BARCODE_UPC_E`
- `BARCODE_ITF`, `BARCODE_CODABAR`

### HRI Positions
- `HRI_TEXT_NONE` - No text
- `HRI_TEXT_ABOVE` - Above barcode
- `HRI_TEXT_BELOW` - Below barcode
- `HRI_TEXT_BOTH` - Both positions

### QR Error Levels
- `QR_ERROR_LEVEL_L` - Low (7%)
- `QR_ERROR_LEVEL_M` - Medium (15%)
- `QR_ERROR_LEVEL_Q` - Quartile (25%)
- `QR_ERROR_LEVEL_H` - High (30%)

## Error Handling

```dart
try {
  await FlutterSewooUsbPrinter.connect('/dev/ttyUSB0');
} catch (e) {
  if (e is FlutterSewooException) {
    print('Error: ${e.message}');
  }
}
```

## Supported Encodings

```dart
// Set encoding for text printing
await FlutterSewooUsbPrinter.setEncoding('EUC-KR');
```

Available encodings:
- UTF-8 (default)
- EUC-KR (Korean)
- BIG5 (Traditional Chinese)
- GB2312 (Simplified Chinese)
- Shift_JIS (Japanese)

## Example App

Complete example available in the `example` folder:

```bash
cd example
flutter run
```

## Platform Support

| Platform | Support |
|----------|---------|
| Android  | ✅ (Min SDK 21) |
| iOS      | ❌ |
| Web      | ❌ |
| Windows  | ❌ |
| macOS    | ❌ |
| Linux    | ❌ |

## Requirements

- Android device with USB OTG support
- USB to Serial adapter (if needed)
- Sewoo POS printer compatible with SDK 1.114
- Flutter 2.5.0+
- Dart 2.12.0+

## Troubleshooting

### No Ports Detected
- Ensure USB cable is properly connected
- Check USB debugging is enabled
- Verify printer is powered on
- Try different USB ports/cables

### Connection Failed
- Verify correct baud rate (usually 9600 or 115200)
- Ensure no other app is using the port
- Check port name is correct
- Try reconnecting USB cable

### Print Quality Issues
- Check paper quality
- Clean printer head
- Ensure proper power supply

### ASB Mode Not Working
- Not all models support ASB
- Update printer firmware
- Try lower baud rates

## License

MIT License - see LICENSE file

## Contributing

Contributions welcome! Please submit a Pull Request.

## Support

For issues or questions, please file an issue on [GitHub](https://github.com/1kl1/flutter_sewoo_usb_printer).

## Acknowledgments

Based on Sewoo Android SDK 1.114.