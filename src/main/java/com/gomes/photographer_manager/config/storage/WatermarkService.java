package com.gomes.photographer_manager.config.storage;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class WatermarkService {

    /**
     * Aplica a marca d'água centralizada sobre a foto original.
     * Caso alguma das imagens não possa ser decodificada, retorna os bytes originais da foto.
     *
     * @param photoBytes     bytes da foto original
     * @param watermarkBytes bytes da marca d'água
     * @param photoKey       chave de armazenamento da foto, usada para inferir o formato de saída
     */
    public byte[] applyWatermark(byte[] photoBytes, byte[] watermarkBytes, String photoKey) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(photoBytes));
        if (original == null) {
            return photoBytes;
        }

        BufferedImage watermark = ImageIO.read(new ByteArrayInputStream(watermarkBytes));
        if (watermark == null) {
            return photoBytes;
        }

        int watermarkWidth = original.getWidth() * 35 / 100;
        int watermarkHeight = (int) ((double) watermark.getHeight() / watermark.getWidth() * watermarkWidth);
        Image scaledWatermark = watermark.getScaledInstance(watermarkWidth, watermarkHeight, Image.SCALE_SMOOTH);

        Graphics2D graphics = original.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        int x = (original.getWidth() - watermarkWidth) / 2;
        int y = (original.getHeight() - watermarkHeight) / 2;
        graphics.drawImage(scaledWatermark, x, y, null);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String format = photoKey != null && photoKey.toLowerCase().endsWith(".png") ? "PNG" : "JPEG";
        ImageIO.write(original, format, output);
        return output.toByteArray();
    }
}
