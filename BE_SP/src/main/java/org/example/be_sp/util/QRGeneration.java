package org.example.be_sp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.example.be_sp.exception.ApiException;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QRGeneration {
    @NotNull
    public static byte[] generateQRCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200, hints));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            return baos.toByteArray();
        } catch (WriterException | IOException e) {
            throw new ApiException("Lỗi khi tạo mã QR: " + e.getMessage(), "QR_GENERATION_ERROR");
        }
    }
}
