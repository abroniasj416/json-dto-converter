package org.example.json;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SchemaObject extends SchemaNode {
    public static final class FieldInfo {
        private SchemaNode schema;
        private int presentCount;
        private int totalSamples;

        public FieldInfo(SchemaNode schema, int presentCount, int totalSamples) {
            this.schema = schema;
            this.presentCount = presentCount;
            this.totalSamples = totalSamples;
        }

        public SchemaNode schema() { return schema; }
        public void setSchema(SchemaNode schema) { this.schema = schema; }
        public int presentCount() { return presentCount; }
        public int totalSamples() { return totalSamples; }
        public boolean optional() { return presentCount < totalSamples; }

        public static FieldInfo presentOnce(SchemaNode schema) {
            return new FieldInfo(schema, 1, 1);
        }
    }

    private final Map<String, FieldInfo> fields = new LinkedHashMap<>();

    public SchemaObject() {
        super(Kind.OBJECT);
    }

    public Map<String, FieldInfo> fields() { return fields; }
}