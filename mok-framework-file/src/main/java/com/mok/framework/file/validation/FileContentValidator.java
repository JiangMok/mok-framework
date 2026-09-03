package com.mok.framework.file.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件真实内容校验器。
 * 不直接信任客户端提交的 MIME 类型或文件扩展名。
 */
@Component
public class FileContentValidator {

    private static final int HEADER_LENGTH = 16;
    private static final int TEXT_SAMPLE_LENGTH = 8192;
    private static final int MAX_ZIP_ENTRIES_TO_INSPECT = 512;

    private static final Set<String> ZIP_MIME_TYPES = Set.of(
            "application/zip",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation");

    public boolean matches(MultipartFile file, String mimeType) throws IOException {
        if (file == null || file.isEmpty()) {
            return false;
        }
        return matches(() -> file.getInputStream(), mimeType);
    }

    public boolean matches(Path path, String mimeType) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            return false;
        }
        return matches(() -> Files.newInputStream(path), mimeType);
    }

    private boolean matches(InputStreamProvider inputStreamProvider, String mimeType) throws IOException {
        String normalizedMimeType = normalizeMimeType(mimeType);
        if (ZIP_MIME_TYPES.contains(normalizedMimeType)) {
            return matchesZipContent(inputStreamProvider, normalizedMimeType);
        }
        if ("text/plain".equals(normalizedMimeType)) {
            return looksLikeText(inputStreamProvider);
        }

        byte[] header = readHeader(inputStreamProvider);
        return switch (normalizedMimeType) {
            case "image/jpeg" -> startsWith(header, 0xFF, 0xD8, 0xFF);
            case "image/png" -> startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/gif" -> startsWithAscii(header, "GIF87a") || startsWithAscii(header, "GIF89a");
            case "image/webp" -> startsWithAscii(header, "RIFF") && containsAsciiAt(header, 8, "WEBP");
            case "application/pdf" -> startsWithAscii(header, "%PDF-");
            case "application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint" ->
                    startsWith(header, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
            case "application/x-rar-compressed" ->
                    startsWith(header, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00)
                            || startsWith(header, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00);
            default -> false;
        };
    }

    private boolean matchesZipContent(InputStreamProvider inputStreamProvider, String mimeType) throws IOException {
        byte[] header = readHeader(inputStreamProvider);
        if (!isZipHeader(header)) {
            return false;
        }
        if ("application/zip".equals(mimeType)) {
            return true;
        }

        String requiredEntry = switch (mimeType) {
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "word/document.xml";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xl/workbook.xml";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "ppt/presentation.xml";
            default -> null;
        };
        if (requiredEntry == null) {
            return false;
        }

        boolean hasContentTypes = false;
        boolean hasRequiredEntry = false;
        try (InputStream inputStream = inputStreamProvider.open();
             ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream))) {
            ZipEntry entry;
            int inspectedEntries = 0;
            while ((entry = zipInputStream.getNextEntry()) != null
                    && inspectedEntries++ < MAX_ZIP_ENTRIES_TO_INSPECT) {
                String entryName = entry.getName().replace('\\', '/');
                if ("[Content_Types].xml".equals(entryName)) {
                    hasContentTypes = true;
                } else if (requiredEntry.equals(entryName)) {
                    hasRequiredEntry = true;
                }
                if (hasContentTypes && hasRequiredEntry) {
                    return true;
                }
            }
        } catch (IOException exception) {
            return false;
        }
        return false;
    }

    private boolean looksLikeText(InputStreamProvider inputStreamProvider) throws IOException {
        byte[] sample = new byte[TEXT_SAMPLE_LENGTH];
        int length;
        try (InputStream inputStream = inputStreamProvider.open()) {
            length = inputStream.read(sample);
        }
        if (length <= 0) {
            return false;
        }
        for (int index = 0; index < length; index++) {
            int value = sample[index] & 0xFF;
            if (value == 0 || value < 0x09 || (value > 0x0D && value < 0x20)) {
                return false;
            }
        }
        return true;
    }

    private byte[] readHeader(InputStreamProvider inputStreamProvider) throws IOException {
        byte[] header = new byte[HEADER_LENGTH];
        int offset = 0;
        try (InputStream inputStream = inputStreamProvider.open()) {
            while (offset < header.length) {
                int read = inputStream.read(header, offset, header.length - offset);
                if (read < 0) {
                    break;
                }
                offset += read;
            }
        }
        if (offset == header.length) {
            return header;
        }
        byte[] shortened = new byte[offset];
        System.arraycopy(header, 0, shortened, 0, offset);
        return shortened;
    }

    private boolean isZipHeader(byte[] header) {
        return startsWith(header, 0x50, 0x4B, 0x03, 0x04)
                || startsWith(header, 0x50, 0x4B, 0x05, 0x06)
                || startsWith(header, 0x50, 0x4B, 0x07, 0x08);
    }

    private boolean startsWithAscii(byte[] bytes, String expected) {
        return containsAsciiAt(bytes, 0, expected);
    }

    private boolean containsAsciiAt(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }
        for (int index = 0; index < expected.length(); index++) {
            if ((byte) expected.charAt(index) != bytes[offset + index]) {
                return false;
            }
        }
        return true;
    }

    private boolean startsWith(byte[] bytes, int... expected) {
        if (bytes.length < expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if ((bytes[index] & 0xFF) != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        return mimeType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    @FunctionalInterface
    private interface InputStreamProvider {
        InputStream open() throws IOException;
    }
}
