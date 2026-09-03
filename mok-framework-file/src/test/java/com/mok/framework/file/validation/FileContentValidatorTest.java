package com.mok.framework.file.validation;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class FileContentValidatorTest {

    private final FileContentValidator validator = new FileContentValidator();

    @Test
    void shouldAcceptMatchingImageSignatureAndRejectSpoofedImage() throws Exception {
        MockMultipartFile png = new MockMultipartFile(
                "file", "avatar.png", "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        MockMultipartFile spoofedPng = new MockMultipartFile(
                "file", "avatar.png", "image/png", "not-an-image".getBytes());

        assertThat(validator.matches(png, png.getContentType())).isTrue();
        assertThat(validator.matches(spoofedPng, spoofedPng.getContentType())).isFalse();
    }

    @Test
    void shouldRequireTheExpectedOfficeDocumentEntry() throws Exception {
        MockMultipartFile docx = new MockMultipartFile(
                "file", "report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                zip("[Content_Types].xml", "word/document.xml"));
        MockMultipartFile renamedZip = new MockMultipartFile(
                "file", "report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                zip("[Content_Types].xml", "payload.bin"));

        assertThat(validator.matches(docx, docx.getContentType())).isTrue();
        assertThat(validator.matches(renamedZip, renamedZip.getContentType())).isFalse();
    }

    @Test
    void shouldValidateCommonDocumentArchiveAndTextSignatures() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "report.pdf", "application/pdf", "%PDF-1.7".getBytes());
        MockMultipartFile rar = new MockMultipartFile(
                "file", "archive.rar", "application/x-rar-compressed",
                new byte[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00});
        MockMultipartFile text = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "正常的文本内容".getBytes());
        MockMultipartFile binaryAsText = new MockMultipartFile(
                "file", "notes.txt", "text/plain", new byte[]{0x41, 0x00, 0x42});

        assertThat(validator.matches(pdf, pdf.getContentType())).isTrue();
        assertThat(validator.matches(rar, rar.getContentType())).isTrue();
        assertThat(validator.matches(text, text.getContentType())).isTrue();
        assertThat(validator.matches(binaryAsText, binaryAsText.getContentType())).isFalse();
    }

    private byte[] zip(String... entries) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutput = new ZipOutputStream(output)) {
            for (String entry : entries) {
                zipOutput.putNextEntry(new ZipEntry(entry));
                zipOutput.write("content".getBytes());
                zipOutput.closeEntry();
            }
        }
        return output.toByteArray();
    }
}
