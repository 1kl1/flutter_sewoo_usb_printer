# Flutter Sewoo USB Printer Plugin

A Flutter plugin for Sewoo POS printers using USB/Serial connection. This plugin provides a comprehensive wrapper around the Sewoo_Android_1114.jar library, focusing exclusively on USB connectivity without WiFi or Bluetooth support.

## Features

- **USB Serial Connection Management**: Auto-detect and connect to USB serial ports
- **Comprehensive Printing**: Text, barcodes, QR codes, images, and PDFs
- **Real-time Status Monitoring**: ASB (Automatic Status Back) support
- **Advanced Text Formatting**: Multiple fonts, sizes, and alignments
- **ESC/POS Commands**: Full ESC/POS printer command support
- **Multi-encoding Support**: UTF-8, EUC-KR, BIG5, GB2312, Shift_JIS
- **Cash Drawer Control**: Open cash drawer command support

## Installation

### 1. Add Dependency

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  flutter_sewoo_usb_printer: ^1.0.0
```

### 2. Android Configuration

Add the following permissions to your Android app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

For Android 10+ (API 29+), add the following to the application tag:

```xml
<application
    android:requestLegacyExternalStorage="true"
    ...>
```

## Usage

### Import the Plugin

```dart
import 'package:flutter_sewoo_usb_printer/flutter_sewoo_usb_printer.dart';
```

### Get Available Ports

```dart
List<String> ports = await FlutterSewooUsbPrinter.getAvailablePorts();
```

### Connect to Printer

```dart
bool connected = await FlutterSewooUsbPrinter.connect(
  '/dev/ttyUSB0',  // Port name
  baudRate: 9600,   // Baud rate (9600, 19200, 38400, 57600, 115200)
);
```

### Print Text

```dart
await FlutterSewooUsbPrinter.printText(
  'Hello World',
  alignment: FlutterSewooUsbPrinter.ALIGN_CENTER,
  fontType: FlutterSewooUsbPrinter.FONT_BOLD,
  textSize: FlutterSewooUsbPrinter.TEXT_SIZE_2X,
);
```

### Print Barcode

```dart
await FlutterSewooUsbPrinter.printBarcode(
  '1234567890',
  barcodeType: FlutterSewooUsbPrinter.BARCODE_CODE128,
  height: 100,
  width: 2,
  alignment: FlutterSewooUsbPrinter.ALIGN_CENTER,
  hriPosition: FlutterSewooUsbPrinter.HRI_TEXT_BELOW,
);
```

### Print QR Code

```dart
await FlutterSewooUsbPrinter.printQRCode(
  'https://example.com',
  moduleSize: 6,
  errorLevel: FlutterSewooUsbPrinter.QR_ERROR_LEVEL_M,
);
```

### Print Image

```dart
// From bytes
Uint8List imageData = // ... your image data
await FlutterSewooUsbPrinter.printImage(imageData);

// From file path
await FlutterSewooUsbPrinter.printImageFile('/path/to/image.png');
```

### Print PDF

```dart
await FlutterSewooUsbPrinter.printPDF(
  '/path/to/document.pdf',
  pageNumber: 0,  // 0-based page index
);
```

### Status Monitoring

```dart
// Check status once
PrinterStatus status = await FlutterSewooUsbPrinter.checkPrinterStatus();
if (status.isPaperEmpty) {
  print('Paper is empty!');
}

// Enable ASB mode for real-time monitoring
await FlutterSewooUsbPrinter.enableASBMode(true);

// Listen to status updates
FlutterSewooUsbPrinter.getStatusStream().listen((status) {
  print('Status: $status');
});
```

### Other Operations

```dart
// Line feed
await FlutterSewooUsbPrinter.lineFeed(lines: 3);

// Cut paper
await FlutterSewooUsbPrinter.cutPaper();

// Open cash drawer
await FlutterSewooUsbPrinter.openCashDrawer();

// Reset printer
await FlutterSewooUsbPrinter.reset();

// Disconnect
await FlutterSewooUsbPrinter.disconnect();
```

## API Reference

### Connection Methods

| Method | Description |
|--------|-------------|
| `getAvailablePorts()` | Returns list of available USB serial ports |
| `connect(portName, baudRate)` | Connect to printer on specified port |
| `disconnect()` | Disconnect from printer |
| `isConnected()` | Check if printer is connected |
| `getConnectionInfo()` | Get current connection details |

### Print Methods

| Method | Description |
|--------|-------------|
| `printText(text, alignment, fontType, textSize)` | Print formatted text |
| `printString(text)` | Print raw text string |
| `printBarcode(data, type, height, width, alignment, hri)` | Print barcode |
| `printQRCode(data, moduleSize, errorLevel)` | Print QR code |
| `printImage(imageData)` | Print image from bytes |
| `printImageFile(path)` | Print image from file |
| `printPDF(path, pageNumber)` | Print PDF page |
| `printAndroidFont(text, fontFamily, fontSize, alignment)` | Print with Android fonts |

### Control Methods

| Method | Description |
|--------|-------------|
| `lineFeed(lines)` | Feed paper by specified lines |
| `cutPaper()` | Cut paper |
| `openCashDrawer()` | Open cash drawer |
| `reset()` | Reset printer to default state |
| `sendRawData(data)` | Send raw ESC/POS commands |

### Status Methods

| Method | Description |
|--------|-------------|
| `checkPrinterStatus()` | Get current printer status |
| `enableASBMode(enable)` | Enable/disable automatic status monitoring |
| `getStatusStream()` | Get stream of status updates |

### Constants

#### Alignment
- `ALIGN_LEFT` - Left alignment
- `ALIGN_CENTER` - Center alignment  
- `ALIGN_RIGHT` - Right alignment

#### Font Types
- `FONT_DEFAULT` - Normal font
- `FONT_BOLD` - Bold font
- `FONT_UNDERLINE` - Underlined text
- `FONT_REVERSE` - Reverse video (white on black)

#### Text Sizes
- `TEXT_SIZE_1X` - Normal size
- `TEXT_SIZE_2X` - Double size
- `TEXT_SIZE_3X` - Triple size
- `TEXT_SIZE_4X` - Quadruple size

#### Barcode Types
- `BARCODE_CODE39`
- `BARCODE_CODE128`
- `BARCODE_EAN8`
- `BARCODE_EAN13`
- `BARCODE_UPC_A`
- `BARCODE_UPC_E`
- `BARCODE_ITF`
- `BARCODE_CODABAR`

#### HRI Positions
- `HRI_TEXT_NONE` - No human readable text
- `HRI_TEXT_ABOVE` - Text above barcode
- `HRI_TEXT_BELOW` - Text below barcode
- `HRI_TEXT_BOTH` - Text both above and below

#### QR Error Levels
- `QR_ERROR_LEVEL_L` - Low (7% correction)
- `QR_ERROR_LEVEL_M` - Medium (15% correction)
- `QR_ERROR_LEVEL_Q` - Quartile (25% correction)
- `QR_ERROR_LEVEL_H` - High (30% correction)

## PrinterStatus Object

```dart
class PrinterStatus {
  final int statusCode;        // Raw status code
  final bool isNormal;         // Printer is ready
  final bool isPaperEmpty;     // Paper empty
  final bool isPaperNearEnd;   // Paper near end
  final bool isCoverOpen;      // Cover is open
  final bool isError;          // Printer error
  final bool isCashDrawerOpen; // Cash drawer open
  final String? errorMessage;  // Error description
}
```

## Supported Encodings

- UTF-8 (default)
- EUC-KR (Korean)
- BIG5 (Traditional Chinese)
- GB2312 (Simplified Chinese)
- Shift_JIS (Japanese)

Set encoding using:

```dart
await FlutterSewooUsbPrinter.setEncoding('EUC-KR');
```

## Error Handling

All methods throw `FlutterSewooException` on error:

```dart
try {
  await FlutterSewooUsbPrinter.connect('/dev/ttyUSB0');
} catch (e) {
  if (e is FlutterSewooException) {
    print('Error: ${e.message}');
  }
}
```

## Example App

See the `example` folder for a complete sample application demonstrating all features:

```bash
cd example
flutter run
```

## Troubleshooting

### No Ports Detected

- Ensure USB cable is properly connected
- Check if USB debugging is enabled on the device
- Verify the printer is powered on
- Try different USB ports or cables

### Connection Failed

- Verify the correct baud rate (usually 9600 or 115200)
- Ensure no other app is using the port
- Check if the port name is correct
- Try disconnecting and reconnecting the USB cable

### Print Quality Issues

- Check printer paper quality
- Verify the printer head is clean
- Adjust print density settings if available
- Ensure proper power supply to printer

### ASB Mode Not Working

- Not all printer models support ASB
- Ensure printer firmware is up to date
- Check serial connection quality
- Try lower baud rates for stability

## Platform Support

- Android: ✅ Supported (Min SDK 21)
- iOS: ❌ Not supported (USB serial not available)
- Web: ❌ Not supported
- Windows: ❌ Not supported
- macOS: ❌ Not supported
- Linux: ❌ Not supported

## Requirements

- Android device with USB OTG support
- USB to Serial adapter (if printer doesn't have direct USB)
- Sewoo POS printer compatible with SDK 1.114
- Flutter 2.5.0 or higher
- Dart 2.12.0 or higher

## License

MIT License - See LICENSE file for details

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or suggestions, please file an issue on the GitHub repository.

## Acknowledgments

This plugin is based on the Sewoo Android SDK 1.114 and provides Flutter bindings for USB serial communication with Sewoo POS printers.