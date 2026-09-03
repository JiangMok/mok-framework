package com.mok.framework.mail.service.impl;

import cn.hutool.extra.mail.MailAccount;
import com.mok.framework.common.BusinessException;
import com.mok.framework.mail.mapper.MailSenderMapper;
import com.mok.framework.model.dto.MailSenderDTO;
import com.mok.framework.model.entity.MailSender;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailSenderServiceImplTest {

    @Test
    void shouldRequirePasswordForFirstConfiguration() {
        MailSenderMapper mapper = mock(MailSenderMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        MailSenderServiceImpl service = new MailSenderServiceImpl(mapper);

        assertThatThrownBy(() -> service.updateConfig(dto(null, 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("必须填写认证密码");
        verify(mapper, never()).insert(any(MailSender.class));
    }

    @Test
    void shouldRejectDisabledConfiguration() {
        MailSenderMapper mapper = mock(MailSenderMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(sender("secret", 0)));
        MailSenderServiceImpl service = new MailSenderServiceImpl(mapper);

        assertThatThrownBy(service::getMailAccount)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已禁用");
    }

    @Test
    void shouldReadLatestConfigurationBeforeEverySend() {
        MailSenderMapper mapper = mock(MailSenderMapper.class);
        when(mapper.selectList(any()))
                .thenReturn(List.of(sender("old-secret", 1)))
                .thenReturn(List.of(sender("new-secret", 1)));
        MailSenderServiceImpl service = new MailSenderServiceImpl(mapper);

        MailAccount first = service.getMailAccount();
        MailAccount second = service.getMailAccount();

        assertThat(first.getPass()).isEqualTo("old-secret");
        assertThat(second.getPass()).isEqualTo("new-secret");
        assertThat(second).isNotSameAs(first);
    }

    @Test
    void shouldKeepExistingPasswordWhenUpdateOmitsIt() {
        MailSenderMapper mapper = mock(MailSenderMapper.class);
        MailSender stored = sender("old-secret", 1);
        when(mapper.selectList(any())).thenReturn(List.of(stored));
        when(mapper.updateById(stored)).thenReturn(1);
        MailSenderServiceImpl service = new MailSenderServiceImpl(mapper);

        service.updateConfig(dto(null, 1));

        assertThat(stored.getPassword()).isEqualTo("old-secret");
        verify(mapper).updateById(stored);
    }

    @Test
    void shouldRejectDuplicateConfigurationRows() {
        MailSenderMapper mapper = mock(MailSenderMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(sender("one", 1), sender("two", 1)));
        MailSenderServiceImpl service = new MailSenderServiceImpl(mapper);

        assertThatThrownBy(service::getConfig)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("多条发件箱配置");
    }

    @Test
    void shouldReturnDisabledConfigurationWithoutItsPassword() {
        MailSenderMapper mapper = mock(MailSenderMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(sender("secret", 0)));
        MailSenderServiceImpl service = new MailSenderServiceImpl(mapper);

        MailSender result = service.getConfig();

        assertThat(result.getStatus()).isZero();
        assertThat(result.getPassword()).isNull();
    }

    private MailSender sender(String password, int status) {
        return MailSender.builder()
                .id("sender-1")
                .host("smtp.example.com")
                .port(465)
                .sslEnable(1)
                .fromAddress("system@example.com")
                .username("system@example.com")
                .password(password)
                .status(status)
                .build();
    }

    private MailSenderDTO dto(String password, int status) {
        MailSenderDTO dto = new MailSenderDTO();
        dto.setHost("smtp.example.com");
        dto.setPort(465);
        dto.setSslEnable(1);
        dto.setFromAddress("system@example.com");
        dto.setUsername("system@example.com");
        dto.setPassword(password);
        dto.setStatus(status);
        return dto;
    }
}

