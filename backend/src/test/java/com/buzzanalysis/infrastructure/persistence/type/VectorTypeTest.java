package com.buzzanalysis.infrastructure.persistence.type;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link VectorType}（pgvector用カスタムHibernate型）の単体テスト。
 * 実際のPostgreSQL接続は使わず、JDBCの ResultSet/PreparedStatement をモック化して
 * シリアライズ/デシリアライズのロジックのみを検証する（実DBでの動作確認は
 * {@code EmbeddingRepositoryImplIT}（Testcontainers）で行う）。
 */
@ExtendWith(MockitoExtension.class)
class VectorTypeTest {

    private final VectorType vectorType = new VectorType();

    @Test
    void parseVectorLiteral_parsesPgVectorTextRepresentation() {
        float[] result = VectorType.parseVectorLiteral("[0.1,0.2,-0.3]");

        assertThat(result).containsExactly(0.1f, 0.2f, -0.3f);
    }

    @Test
    void toVectorLiteral_producesPgVectorTextRepresentation() {
        String result = VectorType.toVectorLiteral(new float[]{0.1f, 0.2f, -0.3f});

        assertThat(result).isEqualTo("[0.1,0.2,-0.3]");
    }

    @Test
    void roundTrip_preservesValues() {
        float[] original = new float[]{1.5f, -2.25f, 0.0f, 100.125f};

        float[] roundTripped = VectorType.parseVectorLiteral(VectorType.toVectorLiteral(original));

        assertThat(roundTripped).containsExactly(original);
    }

    @Test
    void nullSafeGet_returnsNull_whenColumnIsNull(@Mock ResultSet resultSet,
                                                   @Mock SharedSessionContractImplementor session) throws Exception {
        when(resultSet.getObject(1)).thenReturn(null);

        assertThat(vectorType.nullSafeGet(resultSet, 1, session, null)).isNull();
    }

    @Test
    void nullSafeGet_parsesPGobjectValue(@Mock ResultSet resultSet,
                                          @Mock SharedSessionContractImplementor session) throws Exception {
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue("[1.0,2.0,3.0]");
        when(resultSet.getObject(1)).thenReturn(pgObject);

        float[] result = vectorType.nullSafeGet(resultSet, 1, session, null);

        assertThat(result).containsExactly(1.0f, 2.0f, 3.0f);
    }

    @Test
    void nullSafeSet_setsNullType_whenValueIsNull(@Mock PreparedStatement statement,
                                                   @Mock SharedSessionContractImplementor session) throws Exception {
        vectorType.nullSafeSet(statement, null, 1, session);

        verify(statement).setNull(1, Types.OTHER);
    }

    @Test
    void nullSafeSet_bindsVectorTypedPGobject(@Mock PreparedStatement statement,
                                               @Mock SharedSessionContractImplementor session) throws Exception {
        vectorType.nullSafeSet(statement, new float[]{1.0f, 2.0f}, 1, session);

        verify(statement).setObject(anyInt(), any(PGobject.class));
    }

    @Test
    void equalsAndHashCode_delegateToArraysUtility() {
        float[] a = {1.0f, 2.0f};
        float[] b = {1.0f, 2.0f};

        assertThat(vectorType.equals(a, b)).isTrue();
        assertThat(vectorType.hashCode(a)).isEqualTo(vectorType.hashCode(b));
    }
}
