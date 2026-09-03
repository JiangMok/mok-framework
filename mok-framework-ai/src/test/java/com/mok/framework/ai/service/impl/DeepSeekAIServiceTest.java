package com.mok.framework.ai.service.impl;

import com.mok.framework.ai.config.AiProperties;
import com.mok.framework.ai.service.AIStreamHandle;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeepSeekAIServiceTest {

    @Test
    void eachStreamUsesAnIndependentCancelableCall() {
        AiProperties properties = properties();
        Call.Factory callFactory = mock(Call.Factory.class);
        Call firstCall = mock(Call.class);
        Call secondCall = mock(Call.class);
        when(callFactory.newCall(any(Request.class))).thenReturn(firstCall, secondCall);
        DeepSeekAIService service = new DeepSeekAIService(properties, callFactory);

        AIStreamHandle first = service.createStream("first", "system", chunk -> { });
        AIStreamHandle second = service.createStream("second", "system", chunk -> { });

        first.cancel();
        verify(firstCall).cancel();
        verify(secondCall, never()).cancel();

        second.cancel();
        verify(secondCall).cancel();
    }

    @Test
    void callbackFailureStopsTheCurrentStream() throws Exception {
        AiProperties properties = properties();
        Call.Factory callFactory = mock(Call.Factory.class);
        Call call = mock(Call.class);
        Request request = new Request.Builder().url("https://example.com").build();
        String body = "data: {\"choices\":[{\"delta\":{\"content\":\"chunk\"}}]}\n\n";
        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(body, MediaType.parse("text/event-stream")))
                .build();
        when(callFactory.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenReturn(response);
        DeepSeekAIService service = new DeepSeekAIService(properties, callFactory);
        AIStreamHandle handle = service.createStream("content", null, chunk -> {
            throw new IllegalStateException("client disconnected");
        });

        assertThatThrownBy(handle::execute)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("client disconnected");
    }

    @Test
    void openAiProviderUsesTheCompatibleStreamingImplementation() {
        AiProperties properties = properties();
        properties.setProvider("openai");
        properties.setBaseUrl("https://api.openai.com");
        properties.setModel("gpt-4o-mini");
        OpenAIAIService service = new OpenAIAIService(properties);

        AIStreamHandle handle = service.createStream("content", "system", chunk -> { });

        assertThat(handle).isNotNull();
        handle.cancel();
    }

    @Test
    void propertiesNeverExposeTheApiKey() {
        AiProperties properties = properties();

        assertThat(properties.toString())
                .doesNotContain("secret-key")
                .contains("[PROTECTED]");
    }

    private AiProperties properties() {
        AiProperties properties = new AiProperties();
        properties.setApiKey("secret-key");
        properties.setBaseUrl("https://api.deepseek.com/");
        properties.setModel("deepseek-chat");
        properties.setSystemPrompt("default system prompt");
        return properties;
    }
}
