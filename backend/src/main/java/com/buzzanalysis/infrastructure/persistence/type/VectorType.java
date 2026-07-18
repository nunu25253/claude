package com.buzzanalysis.infrastructure.persistence.type;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * pgvectorの {@code vector} 型と {@code float[]} を相互変換するカスタムHibernate型。
 * PostgreSQL JDBCドライバの {@link PGobject}（type="vector"）経由で読み書きするため、
 * 追加の外部ライブラリ（pgvector-java等）には依存しない。
 *
 * <p>エンティティ側では次のように使用する:</p>
 * <pre>{@code
 * @org.hibernate.annotations.Type(VectorType.class)
 * @Column(name = "vector", columnDefinition = "vector(1536)", nullable = false)
 * private float[] vector;
 * }</pre>
 *
 * <p><b>注意:</b> 本環境にはDockerがなくpgvector拡張入りPostgreSQLでの実行時検証ができていない。
 * 本番投入前に {@code EmbeddingRepositoryImplIT}（Testcontainers, pgvector/pgvector:pg16イメージ）を
 * Docker利用可能な環境で必ず実行すること。</p>
 */
public class VectorType implements UserType<float[]> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<float[]> returnedClass() {
        return float[].class;
    }

    @Override
    public boolean equals(float[] x, float[] y) {
        return Arrays.equals(x, y);
    }

    @Override
    public int hashCode(float[] x) {
        return Arrays.hashCode(x);
    }

    @Override
    public float[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        Object value = rs.getObject(position);
        if (value == null) {
            return null;
        }
        return parseVectorLiteral(value.toString());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, float[] value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(toVectorLiteral(value));
        st.setObject(index, pgObject);
    }

    @Override
    public float[] deepCopy(float[] value) {
        return value == null ? null : value.clone();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(float[] value) {
        return deepCopy(value);
    }

    @Override
    public float[] assemble(Serializable cached, Object owner) {
        return deepCopy((float[]) cached);
    }

    /** pgvectorのテキスト表現（例: "[0.1,0.2,0.3]"）を {@code float[]} に変換する。 */
    static float[] parseVectorLiteral(String literal) {
        String trimmed = literal.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.isEmpty()) {
            return new float[0];
        }
        String[] parts = trimmed.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }

    /** {@code float[]} をpgvectorのテキスト表現（例: "[0.1,0.2,0.3]"）に変換する。 */
    static String toVectorLiteral(float[] vector) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (float v : vector) {
            joiner.add(Float.toString(v));
        }
        return joiner.toString();
    }
}
