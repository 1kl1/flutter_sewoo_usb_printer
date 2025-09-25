import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_sewoo_usb_printer/flutter_sewoo_usb_printer.dart';
import 'dart:async';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Sewoo USB Printer Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: const PrinterHomePage(),
    );
  }
}

class PrinterHomePage extends StatefulWidget {
  const PrinterHomePage({Key? key}) : super(key: key);

  @override
  State<PrinterHomePage> createState() => _PrinterHomePageState();
}

class _PrinterHomePageState extends State<PrinterHomePage> {
  List<String> _availablePorts = [];
  String? _selectedPort;
  int _selectedBaudRate = 9600;
  bool _isConnected = false;
  bool _isLoading = false;
  PrinterStatus? _currentStatus;
  StreamSubscription<PrinterStatus>? _statusSubscription;
  bool _asbModeEnabled = false;

  final List<int> _baudRates = [9600, 19200, 38400, 57600, 115200];

  @override
  void initState() {
    super.initState();
    _loadAvailablePorts();
  }

  @override
  void dispose() {
    _statusSubscription?.cancel();
    super.dispose();
  }

  Future<void> _loadAvailablePorts() async {
    try {
      final ports = await SewooUsbPrinter.getAvailablePorts();
      setState(() {
        _availablePorts = ports;
        if (ports.isNotEmpty && _selectedPort == null) {
          _selectedPort = ports.first;
        }
      });
    } catch (e) {
      _showError('Failed to load ports: $e');
    }
  }

  Future<void> _connect() async {
    if (_selectedPort == null) return;

    setState(() => _isLoading = true);

    try {
      final connected = await SewooUsbPrinter.connect(
        _selectedPort!,
        baudRate: _selectedBaudRate,
      );

      setState(() {
        _isConnected = connected;
        _isLoading = false;
      });

      if (connected) {
        _showMessage('Connected successfully');
        _checkStatus();
      }
    } catch (e) {
      setState(() => _isLoading = false);
      _showError('Connection failed: $e');
    }
  }

  Future<void> _disconnect() async {
    setState(() => _isLoading = true);

    try {
      await SewooUsbPrinter.disconnect();
      setState(() {
        _isConnected = false;
        _isLoading = false;
        _currentStatus = null;
      });
      _showMessage('Disconnected');
    } catch (e) {
      setState(() => _isLoading = false);
      _showError('Disconnect failed: $e');
    }
  }

  Future<void> _checkStatus() async {
    try {
      final status = await SewooUsbPrinter.checkPrinterStatus();
      setState(() => _currentStatus = status);
    } catch (e) {
      _showError('Status check failed: $e');
    }
  }

  Future<void> _toggleASBMode() async {
    try {
      await SewooUsbPrinter.enableASBMode(!_asbModeEnabled);

      if (!_asbModeEnabled) {
        _statusSubscription = SewooUsbPrinter.getStatusStream().listen(
          (status) {
            setState(() => _currentStatus = status);
          },
          onError: (error) {
            _showError('Status stream error: $error');
          },
        );
      } else {
        _statusSubscription?.cancel();
        _statusSubscription = null;
      }

      setState(() => _asbModeEnabled = !_asbModeEnabled);
      _showMessage('ASB mode ${_asbModeEnabled ? "enabled" : "disabled"}');
    } catch (e) {
      _showError('Failed to toggle ASB mode: $e');
    }
  }

  Future<void> _printSampleReceipt() async {
    try {
      await SewooUsbPrinter.printText(
        'SAMPLE RECEIPT',
        alignment: SewooUsbPrinter.ALIGN_CENTER,
        fontType: SewooUsbPrinter.FONT_BOLD,
        textSize: SewooUsbPrinter.TEXT_SIZE_2X,
      );

      await SewooUsbPrinter.lineFeed(lines: 2);

      await SewooUsbPrinter.printText(
        'Date: ${DateTime.now().toString().substring(0, 19)}',
        alignment: SewooUsbPrinter.ALIGN_LEFT,
      );

      await SewooUsbPrinter.lineFeed();

      await SewooUsbPrinter.printText('Items:',
          fontType: SewooUsbPrinter.FONT_BOLD);
      await SewooUsbPrinter.printText('Hamburger                    \$10.00');
      await SewooUsbPrinter.printText('French Fries                 \$5.00');
      await SewooUsbPrinter.printText('Soft Drink                   \$3.00');

      await SewooUsbPrinter.printText(
        '------------------------------------',
        alignment: SewooUsbPrinter.ALIGN_CENTER,
      );

      await SewooUsbPrinter.printText(
        'Total: \$18.00',
        alignment: SewooUsbPrinter.ALIGN_RIGHT,
        fontType: SewooUsbPrinter.FONT_BOLD,
        textSize: SewooUsbPrinter.TEXT_SIZE_2X,
      );

      await SewooUsbPrinter.lineFeed(lines: 2);

      await SewooUsbPrinter.printText(
        'Thank you for your purchase!',
        alignment: SewooUsbPrinter.ALIGN_CENTER,
      );

      await SewooUsbPrinter.lineFeed(lines: 4);
      await SewooUsbPrinter.cutPaper();

      _showMessage('Receipt printed successfully');
    } catch (e) {
      _showError('Print failed: $e');
    }
  }

  Future<void> _printBarcode() async {
    try {
      await SewooUsbPrinter.printText(
        'Barcode Example',
        alignment: SewooUsbPrinter.ALIGN_CENTER,
        fontType: SewooUsbPrinter.FONT_BOLD,
      );

      await SewooUsbPrinter.lineFeed(lines: 2);

      await SewooUsbPrinter.printBarcode(
        '1234567890',
        barcodeType: SewooUsbPrinter.BARCODE_CODE128,
        height: 100,
        width: 2,
        alignment: SewooUsbPrinter.ALIGN_CENTER,
        hriPosition: SewooUsbPrinter.HRI_TEXT_BELOW,
      );

      await SewooUsbPrinter.lineFeed(lines: 4);
      await SewooUsbPrinter.cutPaper();

      _showMessage('Barcode printed successfully');
    } catch (e) {
      _showError('Barcode print failed: $e');
    }
  }

  Future<void> _printQRCode() async {
    try {
      await SewooUsbPrinter.printText(
        'QR Code Example',
        alignment: SewooUsbPrinter.ALIGN_CENTER,
        fontType: SewooUsbPrinter.FONT_BOLD,
      );

      await SewooUsbPrinter.lineFeed(lines: 2);

      await SewooUsbPrinter.printQRCode(
        'https://github.com',
        moduleSize: 6,
        errorLevel: SewooUsbPrinter.QR_ERROR_LEVEL_M,
      );

      await SewooUsbPrinter.lineFeed(lines: 4);
      await SewooUsbPrinter.cutPaper();

      _showMessage('QR Code printed successfully');
    } catch (e) {
      _showError('QR Code print failed: $e');
    }
  }

  Future<void> _openCashDrawer() async {
    try {
      await SewooUsbPrinter.openCashDrawer();
      _showMessage('Cash drawer opened');
    } catch (e) {
      _showError('Failed to open cash drawer: $e');
    }
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: Colors.green),
    );
  }

  void _showError(String error) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(error), backgroundColor: Colors.red),
    );
  }

  Widget _buildStatusIndicator() {
    if (_currentStatus == null) {
      return const Text('Status: Unknown');
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Printer Status:',
          style: Theme.of(context).textTheme.titleMedium,
        ),
        const SizedBox(height: 8),
        _buildStatusRow('Normal', _currentStatus!.isNormal, Colors.green),
        _buildStatusRow(
            'Paper Empty', _currentStatus!.isPaperEmpty, Colors.red),
        _buildStatusRow(
            'Paper Near End', _currentStatus!.isPaperNearEnd, Colors.orange),
        _buildStatusRow('Cover Open', _currentStatus!.isCoverOpen, Colors.red),
        _buildStatusRow('Error', _currentStatus!.isError, Colors.red),
        if (_currentStatus!.errorMessage != null)
          Padding(
            padding: const EdgeInsets.only(top: 4),
            child: Text(
              'Error: ${_currentStatus!.errorMessage}',
              style: const TextStyle(color: Colors.red),
            ),
          ),
      ],
    );
  }

  Widget _buildStatusRow(String label, bool active, Color activeColor) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2),
      child: Row(
        children: [
          Icon(
            active ? Icons.circle : Icons.circle_outlined,
            size: 12,
            color: active ? activeColor : Colors.grey,
          ),
          const SizedBox(width: 8),
          Text(label),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Sewoo USB Printer'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Connection Settings',
                      style: Theme.of(context).textTheme.titleLarge,
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          child: DropdownButtonFormField<String>(
                            decoration: const InputDecoration(
                              labelText: 'Port',
                              border: OutlineInputBorder(),
                            ),
                            value: _selectedPort,
                            items: _availablePorts.map((port) {
                              return DropdownMenuItem(
                                value: port,
                                child: Text(port),
                              );
                            }).toList(),
                            onChanged: _isConnected
                                ? null
                                : (value) {
                                    setState(() => _selectedPort = value);
                                  },
                          ),
                        ),
                        const SizedBox(width: 8),
                        IconButton(
                          icon: const Icon(Icons.refresh),
                          onPressed: _isConnected ? null : _loadAvailablePorts,
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    DropdownButtonFormField<int>(
                      decoration: const InputDecoration(
                        labelText: 'Baud Rate',
                        border: OutlineInputBorder(),
                      ),
                      value: _selectedBaudRate,
                      items: _baudRates.map((rate) {
                        return DropdownMenuItem(
                          value: rate,
                          child: Text(rate.toString()),
                        );
                      }).toList(),
                      onChanged: _isConnected
                          ? null
                          : (value) {
                              if (value != null) {
                                setState(() => _selectedBaudRate = value);
                              }
                            },
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: _isLoading
                                ? null
                                : (_isConnected ? _disconnect : _connect),
                            icon: Icon(
                                _isConnected ? Icons.link_off : Icons.link),
                            label:
                                Text(_isConnected ? 'Disconnect' : 'Connect'),
                            style: ElevatedButton.styleFrom(
                              backgroundColor:
                                  _isConnected ? Colors.red : Colors.green,
                              foregroundColor: Colors.white,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        ElevatedButton(
                          onPressed: _isConnected ? _checkStatus : null,
                          child: const Text('Check Status'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    CheckboxListTile(
                      title: const Text('ASB Mode (Auto Status)'),
                      value: _asbModeEnabled,
                      onChanged: _isConnected ? (_) => _toggleASBMode() : null,
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            if (_isConnected) ...[
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: _buildStatusIndicator(),
                ),
              ),
              const SizedBox(height: 16),
              Expanded(
                child: GridView.count(
                  crossAxisCount: 2,
                  childAspectRatio: 2.5,
                  mainAxisSpacing: 8,
                  crossAxisSpacing: 8,
                  children: [
                    _buildActionButton(
                      'Sample Receipt',
                      Icons.receipt,
                      _printSampleReceipt,
                    ),
                    _buildActionButton(
                      'Barcode',
                      Icons.barcode_reader,
                      _printBarcode,
                    ),
                    _buildActionButton(
                      'QR Code',
                      Icons.qr_code,
                      _printQRCode,
                    ),
                    _buildActionButton(
                      'Cash Drawer',
                      Icons.money,
                      _openCashDrawer,
                    ),
                    _buildActionButton(
                      'Line Feed',
                      Icons.format_line_spacing,
                      () => SewooUsbPrinter.lineFeed(lines: 3),
                    ),
                    _buildActionButton(
                      'Cut Paper',
                      Icons.cut,
                      SewooUsbPrinter.cutPaper,
                    ),
                  ],
                ),
              ),
            ],
            if (_isLoading)
              const Center(
                child: CircularProgressIndicator(),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButton(
      String label, IconData icon, VoidCallback onPressed) {
    return ElevatedButton(
      onPressed: _isConnected ? onPressed : null,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, size: 24),
          const SizedBox(height: 4),
          Text(
            label,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 12),
          ),
        ],
      ),
    );
  }
}
