package com.eir.auth.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenPairRecordTest {

    @Test
    void recordEquality_works() {
        TokenPair pair1 = new TokenPair("access", "refresh");
        TokenPair pair2 = new TokenPair("access", "refresh");
        TokenPair pair3 = new TokenPair("different", "refresh");

        assertThat(pair1).isEqualTo(pair2);
        assertThat(pair1).isNotEqualTo(pair3);
        assertThat(pair1.hashCode()).isEqualTo(pair2.hashCode());
    }

    @Test
    void recordAccessors_work() {
        TokenPair pair = new TokenPair("access-token", "refresh-token");

        assertThat(pair.accessToken()).isEqualTo("access-token");
        assertThat(pair.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void recordToString_containsValues() {
        TokenPair pair = new TokenPair("access", "refresh");

        String toString = pair.toString();

        assertThat(toString).contains("access");
        assertThat(toString).contains("refresh");
        assertThat(toString).contains("TokenPair");
    }

    @Test
    void recordImmutable() {
        TokenPair pair = new TokenPair("access", "refresh");
        // Records are immutable by design - no setters
        // Just verify we can access values
        assertThat(pair.accessToken()).isEqualTo("access");
        assertThat(pair.refreshToken()).isEqualTo("refresh");
    }
}