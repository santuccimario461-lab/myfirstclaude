package com.example.common.infrastructure.persistence.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.example.common.domain.entity.Article;
import com.example.common.domain.repository.ArticleRepository;
import com.example.common.domain.valueobject.ArticleId;
import com.example.common.infrastructure.persistence.document.ArticleDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Elasticsearch implementation of ArticleRepository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class EsArticleRepository implements ArticleRepository {

    private static final String INDEX_NAME = "articles";

    private final ElasticsearchClient esClient;

    @PostConstruct
    public void init() {
        try {
            boolean exists = esClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
                    .value();
            if (!exists) {
                esClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(INDEX_NAME)
                        .mappings(m -> m
                                .properties("title", p -> p.text(t -> t.analyzer("standard")))
                                .properties("content", p -> p.text(t -> t.analyzer("standard")))
                                .properties("author", p -> p.keyword(k -> k))
                                .properties("sourceUrl", p -> p.keyword(k -> k))
                                .properties("sourceName", p -> p.keyword(k -> k))
                                .properties("category", p -> p.keyword(k -> k))
                                .properties("publishedAt", p -> p.date(d -> d))
                                .properties("createdAt", p -> p.date(d -> d))
                                .properties("updatedAt", p -> p.date(d -> d))
                        )
                ));
                log.info("Created Elasticsearch index: {}", INDEX_NAME);
            }
        } catch (IOException e) {
            log.error("Failed to initialize Elasticsearch index", e);
        }
    }

    @Override
    public Article save(Article article) {
        try {
            ArticleDocument doc = toDocument(article);
            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(article.getId().getValue())
                    .document(doc)
            );
            log.debug("Indexed article: {}", article.getId());
            return article;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save article to ES", e);
        }
    }

    @Override
    public List<Article> saveAll(List<Article> articles) {
        if (articles.isEmpty()) {
            return articles;
        }
        try {
            BulkRequest.Builder br = new BulkRequest.Builder();
            for (Article article : articles) {
                ArticleDocument doc = toDocument(article);
                br.operations(op -> op
                        .index(idx -> idx
                                .index(INDEX_NAME)
                                .id(article.getId().getValue())
                                .document(doc)
                        )
                );
            }
            BulkResponse result = esClient.bulk(br.build());
            if (result.errors()) {
                log.error("Bulk indexing had errors");
                result.items().stream()
                        .filter(item -> item.error() != null)
                        .forEach(item -> log.error("Error indexing doc {}: {}",
                                item.id(), item.error().reason()));
            }
            log.info("Bulk indexed {} articles", articles.size());
            return articles;
        } catch (IOException e) {
            throw new RuntimeException("Failed to bulk save articles to ES", e);
        }
    }

    @Override
    public Optional<Article> findById(ArticleId id) {
        try {
            GetResponse<ArticleDocument> resp = esClient.get(g -> g
                            .index(INDEX_NAME)
                            .id(id.getValue()),
                    ArticleDocument.class);
            if (resp.found() && resp.source() != null) {
                return Optional.of(toDomain(resp.source()));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find article", e);
        }
    }

    @Override
    public List<Article> findAll(int page, int size) {
        try {
            SearchResponse<ArticleDocument> resp = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .from(page * size)
                            .size(size)
                            .sort(sort -> sort.field(f -> f.field("createdAt").order(SortOrder.Desc))),
                    ArticleDocument.class);
            return hitsToArticles(resp);
        } catch (IOException e) {
            throw new RuntimeException("Failed to find all articles", e);
        }
    }

    @Override
    public List<Article> search(String keyword, int page, int size) {
        try {
            Query query = Query.of(q -> q
                    .multiMatch(m -> m
                            .query(keyword)
                            .fields("title^3", "content", "author", "category")
                    )
            );
            SearchResponse<ArticleDocument> resp = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(query)
                            .from(page * size)
                            .size(size),
                    ArticleDocument.class);
            return hitsToArticles(resp);
        } catch (IOException e) {
            throw new RuntimeException("Failed to search articles", e);
        }
    }

    @Override
    public List<Article> findBySourceName(String sourceName, int page, int size) {
        try {
            Query query = Query.of(q -> q
                    .term(t -> t.field("sourceName").value(sourceName))
            );
            SearchResponse<ArticleDocument> resp = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(query)
                            .from(page * size)
                            .size(size)
                            .sort(sort -> sort.field(f -> f.field("createdAt").order(SortOrder.Desc))),
                    ArticleDocument.class);
            return hitsToArticles(resp);
        } catch (IOException e) {
            throw new RuntimeException("Failed to find articles by source", e);
        }
    }

    @Override
    public List<Article> findByCategory(String category, int page, int size) {
        try {
            Query query = Query.of(q -> q
                    .term(t -> t.field("category").value(category))
            );
            SearchResponse<ArticleDocument> resp = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(query)
                            .from(page * size)
                            .size(size)
                            .sort(sort -> sort.field(f -> f.field("createdAt").order(SortOrder.Desc))),
                    ArticleDocument.class);
            return hitsToArticles(resp);
        } catch (IOException e) {
            throw new RuntimeException("Failed to find articles by category", e);
        }
    }

    @Override
    public void deleteById(ArticleId id) {
        try {
            esClient.delete(d -> d.index(INDEX_NAME).id(id.getValue()));
            log.debug("Deleted article: {}", id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete article", e);
        }
    }

    @Override
    public boolean existsById(ArticleId id) {
        try {
            return esClient.exists(e -> e.index(INDEX_NAME).id(id.getValue())).value();
        } catch (IOException e) {
            throw new RuntimeException("Failed to check article existence", e);
        }
    }

    @Override
    public long count() {
        try {
            return esClient.count(c -> c.index(INDEX_NAME)).count();
        } catch (IOException e) {
            throw new RuntimeException("Failed to count articles", e);
        }
    }

    // ---- mapping helpers ----

    private List<Article> hitsToArticles(SearchResponse<ArticleDocument> resp) {
        List<Article> result = new ArrayList<>();
        for (Hit<ArticleDocument> hit : resp.hits().hits()) {
            if (hit.source() != null) {
                result.add(toDomain(hit.source()));
            }
        }
        return result;
    }

    private ArticleDocument toDocument(Article a) {
        return ArticleDocument.builder()
                .id(a.getId().getValue())
                .title(a.getTitle())
                .content(a.getContent())
                .author(a.getAuthor())
                .sourceUrl(a.getSourceUrl())
                .sourceName(a.getSourceName())
                .category(a.getCategory())
                .publishedAt(a.getPublishedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private Article toDomain(ArticleDocument d) {
        return Article.builder()
                .id(ArticleId.of(d.getId()))
                .title(d.getTitle())
                .content(d.getContent())
                .author(d.getAuthor())
                .sourceUrl(d.getSourceUrl())
                .sourceName(d.getSourceName())
                .category(d.getCategory())
                .publishedAt(d.getPublishedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
