package com.partypeteai.knowledge;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OsrsKnowledgeSource
{
	CompletableFuture<List<KnowledgeResult>> search(String query);
}
