package com.partypeteai.ai;

import com.partypeteai.config.ProviderType;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface AiProvider
{
	CompletableFuture<AiResponse> send(AiRequest request);
	CompletableFuture<AiResponse> sendStreaming(AiRequest request, Consumer<String> onTextDelta);
	ProviderType getType();
	void cancelActiveRequest();
}
