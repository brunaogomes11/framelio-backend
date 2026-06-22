package com.gomes.photographer_manager.config.storage;

import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Service
public class WatermarkService {

    /** Maior dimensão (px) do preview da loja. */
    private static final int MAX_PREVIEW_PX = 1280;
    /** Qualidade JPEG do preview da loja (0..1). */
    private static final float PREVIEW_JPEG_QUALITY = 0.6f;
    private static final String DEFAULT_WATERMARK_LABEL = "FRAMELIO";

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

    /**
     * Gera a versão de preview da loja: imagem reduzida (baixa resolução), com marca d'água
     * embutida e comprimida em JPEG. Sempre devolve JPEG. Quando os bytes da foto não podem ser
     * decodificados, retorna os bytes originais (fallback).
     *
     * @param photoBytes           bytes da foto original
     * @param watermarkBytesOrNull bytes da marca d'água do fotógrafo, ou {@code null} para usar a marca padrão
     * @param label                texto da marca padrão (ex.: nome do fotógrafo); usa "FRAMELIO" se vazio
     */
    public byte[] applyStorePreview(byte[] photoBytes, byte[] watermarkBytesOrNull, String label) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(photoBytes));
        if (source == null) {
            return photoBytes;
        }

        BufferedImage preview = downscale(source, MAX_PREVIEW_PX);

        Graphics2D graphics = preview.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        BufferedImage watermark = decodeWatermark(watermarkBytesOrNull);
        if (watermark != null) {
            drawImageWatermark(graphics, preview, watermark);
        } else {
            drawTextWatermark(graphics, preview, resolveLabel(label));
        }
        graphics.dispose();

        return writeJpeg(preview, PREVIEW_JPEG_QUALITY);
    }

    private BufferedImage downscale(BufferedImage source, int maxDimension) {
        int width = source.getWidth();
        int height = source.getHeight();
        int largest = Math.max(width, height);

        int targetWidth = width;
        int targetHeight = height;
        if (largest > maxDimension) {
            double scale = (double) maxDimension / largest;
            targetWidth = Math.max(1, (int) Math.round(width * scale));
            targetHeight = Math.max(1, (int) Math.round(height * scale));
        }

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, targetWidth, targetHeight);
        graphics.drawImage(source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        graphics.dispose();
        return resized;
    }

    private BufferedImage decodeWatermark(byte[] watermarkBytesOrNull) {
        if (watermarkBytesOrNull == null) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(watermarkBytesOrNull));
        } catch (IOException e) {
            return null;
        }
    }

    private void drawImageWatermark(Graphics2D graphics, BufferedImage base, BufferedImage watermark) {
        int watermarkWidth = base.getWidth() * 35 / 100;
        int watermarkHeight = (int) ((double) watermark.getHeight() / watermark.getWidth() * watermarkWidth);
        Image scaledWatermark = watermark.getScaledInstance(watermarkWidth, watermarkHeight, Image.SCALE_SMOOTH);

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        int x = (base.getWidth() - watermarkWidth) / 2;
        int y = (base.getHeight() - watermarkHeight) / 2;
        graphics.drawImage(scaledWatermark, x, y, null);
    }

    private void drawTextWatermark(Graphics2D graphics, BufferedImage base, String label) {
        int fontSize = Math.max(14, base.getWidth() / 14);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        graphics.setColor(new Color(255, 255, 255, 110));

        AffineTransform original = graphics.getTransform();
        graphics.rotate(Math.toRadians(-30), base.getWidth() / 2.0, base.getHeight() / 2.0);

        int textWidth = graphics.getFontMetrics().stringWidth(label);
        int stepX = textWidth + fontSize * 3;
        int stepY = fontSize * 4;
        for (int y = -base.getHeight(); y < base.getHeight() * 2; y += stepY) {
            for (int x = -base.getWidth(); x < base.getWidth() * 2; x += stepX) {
                graphics.drawString(label, x, y);
            }
        }
        graphics.setTransform(original);
    }

    private String resolveLabel(String label) {
        if (label == null || label.isBlank()) {
            return DEFAULT_WATERMARK_LABEL;
        }
        return label.trim().toUpperCase();
    }

    private byte[] writeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            ByteArrayOutputStream fallback = new ByteArrayOutputStream();
            ImageIO.write(image, "JPEG", fallback);
            return fallback.toByteArray();
        }

        ImageWriter writer = writers.next();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }
}
