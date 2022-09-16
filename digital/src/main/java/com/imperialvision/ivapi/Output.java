// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ivapi.proto

package com.imperialvision.ivapi;

/**
 * Protobuf type {@code com.imperialvision.ivapi.Output}
 */
public final class Output extends
        com.google.protobuf.GeneratedMessageV3 implements
        // @@protoc_insertion_point(message_implements:com.imperialvision.ivapi.Output)
        OutputOrBuilder {
    private static final long serialVersionUID = 0L;

    // Use Output.newBuilder() to construct.
    private Output(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }

    private Output() {
        bytesOutimg_ = com.google.protobuf.ByteString.EMPTY;
        container_ = "";
    }

    @Override
    @SuppressWarnings({"unused"})
    protected Object newInstance(
            UnusedPrivateParameter unused) {
        return new Output();
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    private Output(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        this();
        if (extensionRegistry == null) {
            throw new NullPointerException();
        }
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                com.google.protobuf.UnknownFieldSet.newBuilder();
        try {
            boolean done = false;
            while (!done) {
                int tag = input.readTag();
                switch (tag) {
                    case 0:
                        done = true;
                        break;
                    case 8: {

                        resOrder_ = input.readInt32();
                        break;
                    }
                    case 18: {

                        bytesOutimg_ = input.readBytes();
                        break;
                    }
                    case 26: {
                        String s = input.readStringRequireUtf8();

                        container_ = s;
                        break;
                    }
                    case 32: {

                        width_ = input.readInt32();
                        break;
                    }
                    case 40: {

                        height_ = input.readInt32();
                        break;
                    }
                    case 48: {

                        size_ = input.readInt64();
                        break;
                    }
                    default: {
                        if (!parseUnknownField(
                                input, unknownFields, extensionRegistry, tag)) {
                            done = true;
                        }
                        break;
                    }
                }
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            throw e.setUnfinishedMessage(this);
        } catch (java.io.IOException e) {
            throw new com.google.protobuf.InvalidProtocolBufferException(
                    e).setUnfinishedMessage(this);
        } finally {
            this.unknownFields = unknownFields.build();
            makeExtensionsImmutable();
        }
    }

    public static final com.google.protobuf.Descriptors.Descriptor
    getDescriptor() {
        return Ivapi.internal_static_com_imperialvision_ivapi_Output_descriptor;
    }

    @Override
    protected FieldAccessorTable
    internalGetFieldAccessorTable() {
        return Ivapi.internal_static_com_imperialvision_ivapi_Output_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        Output.class, Builder.class);
    }

    public static final int RES_ORDER_FIELD_NUMBER = 1;
    private int resOrder_;

    /**
     * <code>int32 res_order = 1;</code>
     *
     * @return The resOrder.
     */
    @Override
    public int getResOrder() {
        return resOrder_;
    }

    public static final int BYTES_OUTIMG_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString bytesOutimg_;

    /**
     * <code>bytes bytes_outimg = 2;</code>
     *
     * @return The bytesOutimg.
     */
    @Override
    public com.google.protobuf.ByteString getBytesOutimg() {
        return bytesOutimg_;
    }

    public static final int CONTAINER_FIELD_NUMBER = 3;
    private volatile Object container_;

    /**
     * <code>string container = 3;</code>
     *
     * @return The container.
     */
    @Override
    public String getContainer() {
        Object ref = container_;
        if (ref instanceof String) {
            return (String) ref;
        } else {
            com.google.protobuf.ByteString bs =
                    (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            container_ = s;
            return s;
        }
    }

    /**
     * <code>string container = 3;</code>
     *
     * @return The bytes for container.
     */
    @Override
    public com.google.protobuf.ByteString
    getContainerBytes() {
        Object ref = container_;
        if (ref instanceof String) {
            com.google.protobuf.ByteString b =
                    com.google.protobuf.ByteString.copyFromUtf8(
                            (String) ref);
            container_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }

    public static final int WIDTH_FIELD_NUMBER = 4;
    private int width_;

    /**
     * <code>int32 width = 4;</code>
     *
     * @return The width.
     */
    @Override
    public int getWidth() {
        return width_;
    }

    public static final int HEIGHT_FIELD_NUMBER = 5;
    private int height_;

    /**
     * <code>int32 height = 5;</code>
     *
     * @return The height.
     */
    @Override
    public int getHeight() {
        return height_;
    }

    public static final int SIZE_FIELD_NUMBER = 6;
    private long size_;

    /**
     * <code>int64 size = 6;</code>
     *
     * @return The size.
     */
    @Override
    public long getSize() {
        return size_;
    }

    private byte memoizedIsInitialized = -1;

    @Override
    public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) return true;
        if (isInitialized == 0) return false;

        memoizedIsInitialized = 1;
        return true;
    }

    @Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
            throws java.io.IOException {
        if (resOrder_ != 0) {
            output.writeInt32(1, resOrder_);
        }
        if (!bytesOutimg_.isEmpty()) {
            output.writeBytes(2, bytesOutimg_);
        }
        if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(container_)) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 3, container_);
        }
        if (width_ != 0) {
            output.writeInt32(4, width_);
        }
        if (height_ != 0) {
            output.writeInt32(5, height_);
        }
        if (size_ != 0L) {
            output.writeInt64(6, size_);
        }
        unknownFields.writeTo(output);
    }

    @Override
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        if (resOrder_ != 0) {
            size += com.google.protobuf.CodedOutputStream
                    .computeInt32Size(1, resOrder_);
        }
        if (!bytesOutimg_.isEmpty()) {
            size += com.google.protobuf.CodedOutputStream
                    .computeBytesSize(2, bytesOutimg_);
        }
        if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(container_)) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, container_);
        }
        if (width_ != 0) {
            size += com.google.protobuf.CodedOutputStream
                    .computeInt32Size(4, width_);
        }
        if (height_ != 0) {
            size += com.google.protobuf.CodedOutputStream
                    .computeInt32Size(5, height_);
        }
        if (size_ != 0L) {
            size += com.google.protobuf.CodedOutputStream
                    .computeInt64Size(6, size_);
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Output)) {
            return super.equals(obj);
        }
        Output other = (Output) obj;

        if (getResOrder()
                != other.getResOrder()) return false;
        if (!getBytesOutimg()
                .equals(other.getBytesOutimg())) return false;
        if (!getContainer()
                .equals(other.getContainer())) return false;
        if (getWidth()
                != other.getWidth()) return false;
        if (getHeight()
                != other.getHeight()) return false;
        if (getSize()
                != other.getSize()) return false;
        if (!unknownFields.equals(other.unknownFields)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (memoizedHashCode != 0) {
            return memoizedHashCode;
        }
        int hash = 41;
        hash = (19 * hash) + getDescriptor().hashCode();
        hash = (37 * hash) + RES_ORDER_FIELD_NUMBER;
        hash = (53 * hash) + getResOrder();
        hash = (37 * hash) + BYTES_OUTIMG_FIELD_NUMBER;
        hash = (53 * hash) + getBytesOutimg().hashCode();
        hash = (37 * hash) + CONTAINER_FIELD_NUMBER;
        hash = (53 * hash) + getContainer().hashCode();
        hash = (37 * hash) + WIDTH_FIELD_NUMBER;
        hash = (53 * hash) + getWidth();
        hash = (37 * hash) + HEIGHT_FIELD_NUMBER;
        hash = (53 * hash) + getHeight();
        hash = (37 * hash) + SIZE_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                getSize());
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    public static Output parseFrom(
            java.nio.ByteBuffer data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static Output parseFrom(
            java.nio.ByteBuffer data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static Output parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static Output parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static Output parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static Output parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static Output parseFrom(java.io.InputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }

    public static Output parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static Output parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input);
    }

    public static Output parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }

    public static Output parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }

    public static Output parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @Override
    public Builder newBuilderForType() {
        return newBuilder();
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(Output prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    @Override
    public Builder toBuilder() {
        return this == DEFAULT_INSTANCE
                ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
            BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
    }

    /**
     * Protobuf type {@code com.imperialvision.ivapi.Output}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:com.imperialvision.ivapi.Output)
            OutputOrBuilder {
        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return Ivapi.internal_static_com_imperialvision_ivapi_Output_descriptor;
        }

        @Override
        protected FieldAccessorTable
        internalGetFieldAccessorTable() {
            return Ivapi.internal_static_com_imperialvision_ivapi_Output_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            Output.class, Builder.class);
        }

        // Construct using com.imperialvision.ivapi.Output.newBuilder()
        private Builder() {
            maybeForceBuilderInitialization();
        }

        private Builder(
                BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3
                    .alwaysUseFieldBuilders) {
            }
        }

        @Override
        public Builder clear() {
            super.clear();
            resOrder_ = 0;

            bytesOutimg_ = com.google.protobuf.ByteString.EMPTY;

            container_ = "";

            width_ = 0;

            height_ = 0;

            size_ = 0L;

            return this;
        }

        @Override
        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
            return Ivapi.internal_static_com_imperialvision_ivapi_Output_descriptor;
        }

        @Override
        public Output getDefaultInstanceForType() {
            return Output.getDefaultInstance();
        }

        @Override
        public Output build() {
            Output result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        @Override
        public Output buildPartial() {
            Output result = new Output(this);
            result.resOrder_ = resOrder_;
            result.bytesOutimg_ = bytesOutimg_;
            result.container_ = container_;
            result.width_ = width_;
            result.height_ = height_;
            result.size_ = size_;
            onBuilt();
            return result;
        }

        @Override
        public Builder clone() {
            return super.clone();
        }

        @Override
        public Builder setField(
                com.google.protobuf.Descriptors.FieldDescriptor field,
                Object value) {
            return super.setField(field, value);
        }

        @Override
        public Builder clearField(
                com.google.protobuf.Descriptors.FieldDescriptor field) {
            return super.clearField(field);
        }

        @Override
        public Builder clearOneof(
                com.google.protobuf.Descriptors.OneofDescriptor oneof) {
            return super.clearOneof(oneof);
        }

        @Override
        public Builder setRepeatedField(
                com.google.protobuf.Descriptors.FieldDescriptor field,
                int index, Object value) {
            return super.setRepeatedField(field, index, value);
        }

        @Override
        public Builder addRepeatedField(
                com.google.protobuf.Descriptors.FieldDescriptor field,
                Object value) {
            return super.addRepeatedField(field, value);
        }

        @Override
        public Builder mergeFrom(com.google.protobuf.Message other) {
            if (other instanceof Output) {
                return mergeFrom((Output) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(Output other) {
            if (other == Output.getDefaultInstance()) return this;
            if (other.getResOrder() != 0) {
                setResOrder(other.getResOrder());
            }
            if (other.getBytesOutimg() != com.google.protobuf.ByteString.EMPTY) {
                setBytesOutimg(other.getBytesOutimg());
            }
            if (!other.getContainer().isEmpty()) {
                container_ = other.container_;
                onChanged();
            }
            if (other.getWidth() != 0) {
                setWidth(other.getWidth());
            }
            if (other.getHeight() != 0) {
                setHeight(other.getHeight());
            }
            if (other.getSize() != 0L) {
                setSize(other.getSize());
            }
            this.mergeUnknownFields(other.unknownFields);
            onChanged();
            return this;
        }

        @Override
        public final boolean isInitialized() {
            return true;
        }

        @Override
        public Builder mergeFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            Output parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (Output) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        private int resOrder_;

        /**
         * <code>int32 res_order = 1;</code>
         *
         * @return The resOrder.
         */
        @Override
        public int getResOrder() {
            return resOrder_;
        }

        /**
         * <code>int32 res_order = 1;</code>
         *
         * @param value The resOrder to set.
         * @return This builder for chaining.
         */
        public Builder setResOrder(int value) {

            resOrder_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>int32 res_order = 1;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearResOrder() {

            resOrder_ = 0;
            onChanged();
            return this;
        }

        private com.google.protobuf.ByteString bytesOutimg_ = com.google.protobuf.ByteString.EMPTY;

        /**
         * <code>bytes bytes_outimg = 2;</code>
         *
         * @return The bytesOutimg.
         */
        @Override
        public com.google.protobuf.ByteString getBytesOutimg() {
            return bytesOutimg_;
        }

        /**
         * <code>bytes bytes_outimg = 2;</code>
         *
         * @param value The bytesOutimg to set.
         * @return This builder for chaining.
         */
        public Builder setBytesOutimg(com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }

            bytesOutimg_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>bytes bytes_outimg = 2;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearBytesOutimg() {

            bytesOutimg_ = getDefaultInstance().getBytesOutimg();
            onChanged();
            return this;
        }

        private Object container_ = "";

        /**
         * <code>string container = 3;</code>
         *
         * @return The container.
         */
        public String getContainer() {
            Object ref = container_;
            if (!(ref instanceof String)) {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                String s = bs.toStringUtf8();
                container_ = s;
                return s;
            } else {
                return (String) ref;
            }
        }

        /**
         * <code>string container = 3;</code>
         *
         * @return The bytes for container.
         */
        public com.google.protobuf.ByteString
        getContainerBytes() {
            Object ref = container_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (String) ref);
                container_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        /**
         * <code>string container = 3;</code>
         *
         * @param value The container to set.
         * @return This builder for chaining.
         */
        public Builder setContainer(
                String value) {
            if (value == null) {
                throw new NullPointerException();
            }

            container_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>string container = 3;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearContainer() {

            container_ = getDefaultInstance().getContainer();
            onChanged();
            return this;
        }

        /**
         * <code>string container = 3;</code>
         *
         * @param value The bytes for container to set.
         * @return This builder for chaining.
         */
        public Builder setContainerBytes(
                com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);

            container_ = value;
            onChanged();
            return this;
        }

        private int width_;

        /**
         * <code>int32 width = 4;</code>
         *
         * @return The width.
         */
        @Override
        public int getWidth() {
            return width_;
        }

        /**
         * <code>int32 width = 4;</code>
         *
         * @param value The width to set.
         * @return This builder for chaining.
         */
        public Builder setWidth(int value) {

            width_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>int32 width = 4;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearWidth() {

            width_ = 0;
            onChanged();
            return this;
        }

        private int height_;

        /**
         * <code>int32 height = 5;</code>
         *
         * @return The height.
         */
        @Override
        public int getHeight() {
            return height_;
        }

        /**
         * <code>int32 height = 5;</code>
         *
         * @param value The height to set.
         * @return This builder for chaining.
         */
        public Builder setHeight(int value) {

            height_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>int32 height = 5;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearHeight() {

            height_ = 0;
            onChanged();
            return this;
        }

        private long size_;

        /**
         * <code>int64 size = 6;</code>
         *
         * @return The size.
         */
        @Override
        public long getSize() {
            return size_;
        }

        /**
         * <code>int64 size = 6;</code>
         *
         * @param value The size to set.
         * @return This builder for chaining.
         */
        public Builder setSize(long value) {

            size_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>int64 size = 6;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearSize() {

            size_ = 0L;
            onChanged();
            return this;
        }

        @Override
        public final Builder setUnknownFields(
                final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.setUnknownFields(unknownFields);
        }

        @Override
        public final Builder mergeUnknownFields(
                final com.google.protobuf.UnknownFieldSet unknownFields) {
            return super.mergeUnknownFields(unknownFields);
        }


        // @@protoc_insertion_point(builder_scope:com.imperialvision.ivapi.Output)
    }

    // @@protoc_insertion_point(class_scope:com.imperialvision.ivapi.Output)
    private static final Output DEFAULT_INSTANCE;

    static {
        DEFAULT_INSTANCE = new Output();
    }

    public static Output getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<Output>
            PARSER = new com.google.protobuf.AbstractParser<Output>() {
        @Override
        public Output parsePartialFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return new Output(input, extensionRegistry);
        }
    };

    public static com.google.protobuf.Parser<Output> parser() {
        return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<Output> getParserForType() {
        return PARSER;
    }

    @Override
    public Output getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

}

