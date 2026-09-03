package com.mok.framework.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.mok.framework.common.FileNotFoundException;
import com.mok.framework.common.config.storage.FileStorageConfig;
import com.mok.framework.file.mapper.FileMapper;
import com.mok.framework.file.validation.FileContentValidator;
import com.mok.framework.model.dto.FileUploadResponse;
import com.mok.framework.model.entity.FileEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileServiceImplTest {

    @TempDir
    Path storageDirectory;

    @Test
    void shouldDeleteStoredFileWhenDatabaseInsertFails() throws Exception {
        FileMapper mapper = mock(FileMapper.class);
        when(mapper.insert(any(FileEntity.class))).thenThrow(new IllegalStateException("database unavailable"));
        TestableFileServiceImpl service = service(mapper);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn("user-1");
            assertThatThrownBy(() -> service.upload(png(), 2))
                    .isInstanceOf(IllegalStateException.class);
        }

        assertThat(countStoredFiles()).isZero();
    }

    @Test
    void shouldDeleteStoredFileWhenTransactionRollsBackAfterMethodReturns() throws Exception {
        FileMapper mapper = mock(FileMapper.class);
        when(mapper.insert(any(FileEntity.class))).thenReturn(1);
        TestableFileServiceImpl service = service(mapper);

        TransactionSynchronizationManager.initSynchronization();
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn("user-1");
            FileUploadResponse response = service.upload(png(), 2);

            assertThat(response.getFileUrl()).contains("/api/files/download/");
            assertThat(countStoredFiles()).isEqualTo(1);
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            assertThat(countStoredFiles()).isZero();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldExposePublicUrlOnlyForAvatarUploads() {
        FileMapper mapper = mock(FileMapper.class);
        when(mapper.insert(any(FileEntity.class))).thenReturn(1);
        TestableFileServiceImpl service = service(mapper);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn("user-1");
            FileUploadResponse response = service.upload(png(), 1);

            assertThat(response.getFileUrl()).contains("/api/uploads/");
            assertThat(response.getFileUrl()).doesNotContain("/files/download/");
        }
    }

    @Test
    void shouldUseTheSameLogicalDeleteOperationForOneFile() {
        FileMapper mapper = mock(FileMapper.class);
        when(mapper.logicalDelete("file-1", "user-1")).thenReturn(1);
        TestableFileServiceImpl service = service(mapper);

        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::getLoginId).thenReturn("user-1");
            service.delete("file-1");
        }

        verify(mapper).logicalDelete("file-1", "user-1");
    }

    @Test
    void shouldRejectBatchDeleteWhenAnyFileIsNotActive() {
        FileMapper mapper = mock(FileMapper.class);
        FileEntity onlyActiveFile = new FileEntity();
        onlyActiveFile.setId("file-1");
        when(mapper.selectList(any())).thenReturn(List.of(onlyActiveFile));
        TestableFileServiceImpl service = service(mapper);

        assertThatThrownBy(() -> service.batchDelete(List.of("file-1", "file-2")))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void shouldRejectStoredAvatarWhenItsContentWasTamperedWith() throws Exception {
        FileMapper mapper = mock(FileMapper.class);
        String relativePath = "2026/09/03/00000000-0000-0000-0000-000000000000.png";
        Path storedFile = storageDirectory.resolve(relativePath);
        Files.createDirectories(storedFile.getParent());
        Files.writeString(storedFile, "<script>not an image</script>");

        FileEntity entity = new FileEntity();
        entity.setId("avatar-1");
        entity.setFilePath(relativePath);
        entity.setStorageName("00000000-0000-0000-0000-000000000000.png");
        entity.setMimeType("image/png");
        entity.setBusinessType(1);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        when(mapper.selectOne(any(), eq(false))).thenReturn(entity);
        TestableFileServiceImpl service = service(mapper);

        assertThat(service.getPublicAvatar(relativePath)).isNull();
    }

    private TestableFileServiceImpl service(FileMapper mapper) {
        FileStorageConfig config = new FileStorageConfig();
        config.setBasePath(storageDirectory.toString());
        config.setBaseUrl("http://localhost:8080");
        config.setUrlPrefix("/api/uploads");
        config.setAllowedTypes(List.of("image/png"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        TestableFileServiceImpl service = new TestableFileServiceImpl(
                config, request, new FileContentValidator());
        service.setMapper(mapper);
        return service;
    }

    private MockMultipartFile png() {
        return new MockMultipartFile(
                "file", "test.png", "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
    }

    private long countStoredFiles() throws Exception {
        try (Stream<Path> paths = Files.walk(storageDirectory)) {
            return paths.filter(Files::isRegularFile).count();
        }
    }

    private static class TestableFileServiceImpl extends FileServiceImpl {
        TestableFileServiceImpl(FileStorageConfig config, HttpServletRequest request,
                                FileContentValidator validator) {
            super(config, request, validator);
        }

        void setMapper(FileMapper mapper) {
            this.baseMapper = mapper;
        }
    }
}
