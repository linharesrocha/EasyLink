package br.com.easylink.easylinkservice.infrastructure.qrcode;

import br.com.easylink.easylinkservice.application.ports.QrCodeGeneratorPort;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@Slf4j
public class ZxingQrCodeAdapter implements QrCodeGeneratorPort {

    @Override
    public byte[] generate(String text, int width, int height) throws WriterException, IOException {
        log.debug("Attempting to generate QR Code. Text: '{}', Width: {}, Height: {}", text, width, height);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] qrCodeBytes = pngOutputStream.toByteArray();
        log.debug("QR Code generated successfully. Text: '{}', Byte array size: {}", text, qrCodeBytes.length);
        return qrCodeBytes;
    }
}