package com.sewoo.flutter_sewoo_usb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import com.sewoo.jpos.command.ESCPOS;
import com.sewoo.jpos.command.ESCPOSConst;
import com.sewoo.jpos.printer.ESCPOSPrinter;
import com.sewoo.jpos.printer.LKPrint;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PrinterOperations {
    private static PrinterOperations instance;
    private ESCPOSPrinter printer;

    private static final Map<String, String> ENCODING_MAP = new HashMap<>();

    static {
        ENCODING_MAP.put("UTF-8", "UTF-8");
        ENCODING_MAP.put("EUC-KR", "EUC-KR");
        ENCODING_MAP.put("BIG5", "BIG5");
        ENCODING_MAP.put("GB2312", "GB2312");
        ENCODING_MAP.put("Shift_JIS", "Shift_JIS");
    }

    private PrinterOperations() {
        printer = new ESCPOSPrinter();
    }

    public static PrinterOperations getInstance() {
        if (instance == null) {
            instance = new PrinterOperations();
        }
        return instance;
    }

    public void setEncoding(String encoding) {
        if (ENCODING_MAP.containsKey(encoding)) {
            printer = new ESCPOSPrinter(encoding);
        } else {
            printer = new ESCPOSPrinter();
        }
    }

    public void printText(String text, int alignment, int fontType, int textSize) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.printText(text, alignment, fontType, textSize);
    }

    public void printString(String text) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.printString(text);
    }

    public void printBarcode(String data, int barcodeType, int height, int width, int alignment, int hriPosition)
            throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.printBarCode(data, barcodeType, height, width, alignment, hriPosition);
    }

    public void printQRCode(String data, int moduleSize, int errorLevel) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.printQRCode(data, moduleSize, errorLevel);
    }

    public void printImage(byte[] imageData) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        if (bitmap != null) {
            printer.printBitmap(bitmap, LKPrint.LK_ALIGNMENT_CENTER);
        } else {
            throw new IOException("Failed to decode image data");
        }
    }

    public void printImageFile(String imagePath) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IOException("Image file not found: " + imagePath);
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap != null) {
            printer.printBitmap(bitmap, LKPrint.LK_ALIGNMENT_CENTER);
        } else {
            throw new IOException("Failed to decode image file");
        }
    }

    public void printPDF(String pdfPath, int pageNumber) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            throw new IOException("PDF file not found: " + pdfPath);
        }

        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

        if (pageNumber >= pdfRenderer.getPageCount()) {
            pdfRenderer.close();
            fileDescriptor.close();
            throw new IOException("Page number out of range");
        }

        PdfRenderer.Page page = pdfRenderer.openPage(pageNumber);

        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFFFFFFF);

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);

        printer.printBitmap(bitmap, LKPrint.LK_ALIGNMENT_CENTER);

        page.close();
        pdfRenderer.close();
        fileDescriptor.close();
    }

    public void printAndroidFont(String text, Typeface typeface, float textSize, int alignment)
            throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        Paint paint = new Paint();
        paint.setTypeface(typeface);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);

        int width = (int) paint.measureText(text) + 10;
        int height = (int) (paint.descent() - paint.ascent()) + 10;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFFFFFFF);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, 5, -paint.ascent() + 5, paint);

        printer.printBitmap(bitmap, alignment);
    }

    public void lineFeed(int lines) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.lineFeed(lines);
    }

    public void cutPaper() throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.cutPaper();
    }

    public void openCashDrawer() throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.openCashDrawer();
    }

    public Map<String, Object> checkPrinterStatus() throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        Map<String, Object> status = new HashMap<>();

        try {
            int statusCode = printer.printerStatus();

            status.put("statusCode", statusCode);
            status.put("isNormal", statusCode == LKPrint.LK_STS_NORMAL);
            status.put("isPaperEmpty", (statusCode & LKPrint.LK_STS_PAPER_EMPTY) != 0);
            status.put("isPaperNearEnd", (statusCode & LKPrint.LK_STS_PAPER_NEAREND) != 0);
            status.put("isCoverOpen", (statusCode & LKPrint.LK_STS_COVER_OPEN) != 0);
            status.put("isError", (statusCode & LKPrint.LK_STS_PRINTER_ERROR) != 0);

        } catch (Exception e) {
            status.put("error", e.getMessage());
        }

        return status;
    }

    public void reset() throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.initialize();
    }

    public void sendRawData(byte[] data) throws IOException, InterruptedException {
        if (!SerialConnectionManager.getInstance().isConnected()) {
            throw new IOException("Printer not connected");
        }

        printer.printRawData(data);
    }
}