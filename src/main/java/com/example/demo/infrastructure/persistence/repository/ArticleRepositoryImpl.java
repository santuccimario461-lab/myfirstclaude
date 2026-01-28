package com.example.demo.infrastructure.persistence.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.example.demo.domain.entity.Article;
import com.example.demo.domain.repository.ArticleRepository;
import com.example.demo.domain.valueobject.ArticleId;
import com.example.demo.infrastructure.persistence.entity.ArticleDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Elasticsearch implementation of ArticleRepository
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepository {

    private static final String INDEX_NAME = "articles";

    private final ElasticsearchClient esClient;

    @PostConstruct
    public void init() {
        try {
            boolean indexExists = esClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(INDEX_NAME)))
                    .value();

            if (!indexExists) {
                esClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(INDEX_NAME)
                        .mappings(m -> m
                                .properties("title", p -> p.text(t -> t.analyzer("standard")))
                                .properties("content", p -> p.text(t -> t.analyzer("standard")))
                                .properties("author", p -> p.keyword(k -> k))
                                .properties("sourceUrl", p -> p.keyword(k -> k))
                                .properties("publishedAt", p -> p.date(d -> d))
                                .properties("createdAt", p -> p.date(d -> d))
                                .properties("updatedAt", p -> p.date(d -> d))
                        )
                ));
                log.info("Created index: {}", INDEX_NAME);
            }
        } catch (IOException e) {
            log.error("Failed to initialize Elasticsearch index", e);
        }
    }

    @Override
    public Article save(Article article) {
        try {
            ArticleDocument document = toDocument(article);
            IndexResponse response = esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(article.getId().getValue())
                    .document(document)
            );
            log.debug("Indexed article with id: {}, result: {}", article.getId(), response.result());
            return article;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save article", e);
        }
    }

    @Override
    public Optional<Article> findById(ArticleId id) {
        try {
            GetResponse<ArticleDocument> response = esClient.get(g -> g
                            .index(INDEX_NAME)
                            .id(id.getValue()),
                    ArticleDocument.class
            );

            if (response.found() && response.source() != null) {
                return Optional.of(toDomain(response.source()));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find article", e);
        }
    }

    @Override
    public List<Article> findAll(int page, int size) {
        try {
            SearchResponse<ArticleDocument> response = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .from(page * size)
                            .size(size)
                            .sort(sort -> sort.field(f -> f.field("createdAt").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))),
                    ArticleDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .map(this::toDomain)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to find articles", e);
        }
    }

    @Override
    public List<Article> search(String keyword, int page, int size) {
        try {
            Query query = Query.of(q -> q
                    .multiMatch(m -> m
                            .query(keyword)
                            .fields("title^2", "content", "author")
                    )
            );

            SearchResponse<ArticleDocument> response = esClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(query)
                            .from(page * size)
                            .size(size),
                    ArticleDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(doc -> doc != null)
                    .map(this::toDomain)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to search articles", e);
        }
    }

    @Override
    public void deleteById(ArticleId id) {
        try {
            esClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(id.getValue())
            );
            log.debug("Deleted article with id: {}", id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete article", e);
        }
    }

    @Override
    public boolean existsById(ArticleId id) {
        try {
            return esClient.exists(e -> e
                    .index(INDEX_NAME)
                    .id(id.getValue())
            ).value();
        } catch (IOException e) {
            throw new RuntimeException("Failed to check article existence", e);
        }
    }

    private ArticleDocument toDocument(Article article) {
        return ArticleDocument.builder()
                .id(article.getId().getValue())
                .title(article.getTitle())
                .content(article.getContent())
                .author(article.getAuthor())
                .sourceUrl(article.getSourceUrl())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    private Article toDomain(ArticleDocument document) {
        return Article.builder()
                .id(ArticleId.of(document.getId()))
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .sourceUrl(document.getSourceUrl())
                .publishedAt(document.getPublishedAt())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
