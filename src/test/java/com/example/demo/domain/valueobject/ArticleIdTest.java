package com.example.demo.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticleIdTest {

    @Test
    void shouldGenerateUniqueId() {
        ArticleId id1 = ArticleId.generate();
        ArticleId id2 = ArticleId.generate();

        assertNotNull(id1.getValue());
        assertNotNull(id2.getValue());
        assertNotEquals(id1, id2);
    }

    @Test
    void shouldCreateFromValue() {
        String value = "test-id-123";
        ArticleId id = ArticleId.of(value);

        assertEquals(value, id.getValue());
    }

    @Test
    void shouldThrowExceptionForNullValue() {
        assertThrows(IllegalArgumentException.class, () -> ArticleId.of(null));
    }

    @Test
    void shouldThrowExceptionForEmptyValue() {
        assertThrows(IllegalArgumentException.class, () -> ArticleId.of(""));
        assertThrows(IllegalArgumentException.class, () -> ArticleId.of("   "));
    }

    @Test
    void shouldBeEqualForSameValue() {
        ArticleId id1 = ArticleId.of("same-id");
        ArticleId id2 = ArticleId.of("same-id");

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
