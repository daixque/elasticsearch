/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.core.ml.inference.trainedmodel;

import org.elasticsearch.TransportVersion;
import org.elasticsearch.TransportVersions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.xcontent.ObjectParser;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.xpack.core.ml.utils.ExceptionsHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.elasticsearch.xpack.core.ml.inference.trainedmodel.NlpConfig.RESULTS_FIELD;
import static org.elasticsearch.xpack.core.ml.inference.trainedmodel.NlpConfig.TOKENIZATION;
import static org.elasticsearch.xpack.core.ml.inference.trainedmodel.TextExpansionConfig.EXPANSION_TYPE;
import static org.elasticsearch.xpack.core.ml.inference.trainedmodel.TextExpansionConfig.TOP_K;

public class TextExpansionConfigUpdate extends NlpConfigUpdate {

    public static final String NAME = TextExpansionConfig.NAME;

    public static final TextExpansionConfigUpdate EMPTY_UPDATE = new TextExpansionConfigUpdate(null, null, TextExpansionConfig.UNSET_TOP_K_VALUE, null);

    public static TextExpansionConfigUpdate fromMap(Map<String, Object> map) {
        Map<String, Object> options = new HashMap<>(map);
        String resultsField = (String) options.remove(RESULTS_FIELD.getPreferredName());
        String expansionTypeField = (String) options.remove(EXPANSION_TYPE.getPreferredName());
        Integer topKField = (Integer) options.remove(TOP_K.getPreferredName());
        int topK = topKField == null ? TextExpansionConfig.UNSET_TOP_K_VALUE : topKField;
        TokenizationUpdate tokenizationUpdate = NlpConfigUpdate.tokenizationFromMap(options);

        if (options.isEmpty() == false) {
            throw ExceptionsHelper.badRequestException("Unrecognized fields {}.", options.keySet());
        }
        return new TextExpansionConfigUpdate(resultsField, expansionTypeField, topK, tokenizationUpdate);
    }

    private static final ObjectParser<TextExpansionConfigUpdate.Builder, Void> STRICT_PARSER = createParser(false);

    private static ObjectParser<TextExpansionConfigUpdate.Builder, Void> createParser(boolean lenient) {
        ObjectParser<TextExpansionConfigUpdate.Builder, Void> parser = new ObjectParser<>(
            NAME,
            lenient,
            TextExpansionConfigUpdate.Builder::new
        );
        parser.declareString(TextExpansionConfigUpdate.Builder::setResultsField, RESULTS_FIELD);
        parser.declareString(TextExpansionConfigUpdate.Builder::setExpansionType, EXPANSION_TYPE);
        parser.declareInt(TextExpansionConfigUpdate.Builder::setTopK, TOP_K);
        parser.declareNamedObject(
            TextExpansionConfigUpdate.Builder::setTokenizationUpdate,
            (p, c, n) -> p.namedObject(TokenizationUpdate.class, n, lenient),
            TOKENIZATION
        );
        return parser;
    }

    public static TextExpansionConfigUpdate fromXContentStrict(XContentParser parser) {
        return STRICT_PARSER.apply(parser, null).build();
    }

    private final String resultsField;
    private final String expansionType;
    private final int topK;

    public TextExpansionConfigUpdate(String resultsField, String expansionType, int topK, TokenizationUpdate tokenizationUpdate) {
        super(tokenizationUpdate);
        this.resultsField = resultsField;
        this.expansionType = expansionType;
        this.topK = topK;
    }

    public TextExpansionConfigUpdate(StreamInput in) throws IOException {
        super(in);
        this.resultsField = in.readOptionalString();
        this.expansionType = in.readOptionalString();
        this.topK = in.readVInt();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalString(resultsField);
        out.writeOptionalString(expansionType);
        out.writeVInt(topK);
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        if (resultsField != null) {
            builder.field(RESULTS_FIELD.getPreferredName(), resultsField);
        }
        if (expansionType != null) {
            builder.field(EXPANSION_TYPE.getPreferredName(), expansionType);
        }
        builder.field(TOP_K.getPreferredName(), topK);
        return builder;
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isSupported(InferenceConfig config) {
        return config instanceof TextExpansionConfig;
    }

    @Override
    public String getResultsField() {
        return resultsField;
    }

    public String getExpansionType() {
        return expansionType;
    }

    public int getTopK() {
        return topK;
    }

    @Override
    public InferenceConfigUpdate.Builder<? extends InferenceConfigUpdate.Builder<?, ?>, ? extends InferenceConfigUpdate> newBuilder() {
        return new TextExpansionConfigUpdate.Builder().setResultsField(resultsField)
            .setExpansionType(expansionType)
            .setTokenizationUpdate(tokenizationUpdate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextExpansionConfigUpdate that = (TextExpansionConfigUpdate) o;
        return Objects.equals(resultsField, that.resultsField) && Objects.equals(tokenizationUpdate, that.tokenizationUpdate)
            && Objects.equals(expansionType, that.expansionType)
            && topK == that.topK;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultsField, tokenizationUpdate);
    }

    @Override
    public String toString() {
        return Strings.toString(this);
    }

    @Override
    public TransportVersion getMinimalSupportedVersion() {
        return TransportVersions.V_8_7_0;
    }

    public static class Builder implements InferenceConfigUpdate.Builder<TextExpansionConfigUpdate.Builder, TextExpansionConfigUpdate> {
        private String resultsField;
        private String expansionType;
        private int topK;
        private TokenizationUpdate tokenizationUpdate;

        @Override
        public TextExpansionConfigUpdate.Builder setResultsField(String resultsField) {
            this.resultsField = resultsField;
            return this;
        }

        public TextExpansionConfigUpdate.Builder setExpansionType(String expansionType) {
            this.expansionType = expansionType;
            return this;
        }

        public TextExpansionConfigUpdate.Builder setTopK(int topK) {
            this.topK = topK;
            return this;
        }

        public TextExpansionConfigUpdate.Builder setTokenizationUpdate(TokenizationUpdate tokenizationUpdate) {
            this.tokenizationUpdate = tokenizationUpdate;
            return this;
        }

        @Override
        public TextExpansionConfigUpdate build() {
            return new TextExpansionConfigUpdate(resultsField, expansionType, topK, tokenizationUpdate);
        }
    }

}
