// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: proto/MsgApiVersion.proto

package com.linbit.linstor.proto;

public final class MsgApiVersionOuterClass {
  private MsgApiVersionOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface MsgApiVersionOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.linbit.linstor.proto.MsgApiVersion)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required uint64 min = 1;</code>
     */
    boolean hasMin();
    /**
     * <code>required uint64 min = 1;</code>
     */
    long getMin();

    /**
     * <code>required uint64 max = 2;</code>
     */
    boolean hasMax();
    /**
     * <code>required uint64 max = 2;</code>
     */
    long getMax();

    /**
     * <code>required uint64 features = 3;</code>
     */
    boolean hasFeatures();
    /**
     * <code>required uint64 features = 3;</code>
     */
    long getFeatures();
  }
  /**
   * <pre>
   * linstor - API Version
   * </pre>
   *
   * Protobuf type {@code com.linbit.linstor.proto.MsgApiVersion}
   */
  public  static final class MsgApiVersion extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:com.linbit.linstor.proto.MsgApiVersion)
      MsgApiVersionOrBuilder {
    // Use MsgApiVersion.newBuilder() to construct.
    private MsgApiVersion(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private MsgApiVersion() {
      min_ = 0L;
      max_ = 0L;
      features_ = 0L;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private MsgApiVersion(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
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
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 8: {
              bitField0_ |= 0x00000001;
              min_ = input.readUInt64();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              max_ = input.readUInt64();
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              features_ = input.readUInt64();
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
      return com.linbit.linstor.proto.MsgApiVersionOuterClass.internal_static_com_linbit_linstor_proto_MsgApiVersion_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.linbit.linstor.proto.MsgApiVersionOuterClass.internal_static_com_linbit_linstor_proto_MsgApiVersion_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion.class, com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion.Builder.class);
    }

    private int bitField0_;
    public static final int MIN_FIELD_NUMBER = 1;
    private long min_;
    /**
     * <code>required uint64 min = 1;</code>
     */
    public boolean hasMin() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required uint64 min = 1;</code>
     */
    public long getMin() {
      return min_;
    }

    public static final int MAX_FIELD_NUMBER = 2;
    private long max_;
    /**
     * <code>required uint64 max = 2;</code>
     */
    public boolean hasMax() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required uint64 max = 2;</code>
     */
    public long getMax() {
      return max_;
    }

    public static final int FEATURES_FIELD_NUMBER = 3;
    private long features_;
    /**
     * <code>required uint64 features = 3;</code>
     */
    public boolean hasFeatures() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>required uint64 features = 3;</code>
     */
    public long getFeatures() {
      return features_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasMin()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasMax()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasFeatures()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeUInt64(1, min_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeUInt64(2, max_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeUInt64(3, features_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(1, min_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(2, max_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(3, features_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion)) {
        return super.equals(obj);
      }
      com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion other = (com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion) obj;

      boolean result = true;
      result = result && (hasMin() == other.hasMin());
      if (hasMin()) {
        result = result && (getMin()
            == other.getMin());
      }
      result = result && (hasMax() == other.hasMax());
      if (hasMax()) {
        result = result && (getMax()
            == other.getMax());
      }
      result = result && (hasFeatures() == other.hasFeatures());
      if (hasFeatures()) {
        result = result && (getFeatures()
            == other.getFeatures());
      }
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasMin()) {
        hash = (37 * hash) + MIN_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getMin());
      }
      if (hasMax()) {
        hash = (37 * hash) + MAX_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getMax());
      }
      if (hasFeatures()) {
        hash = (37 * hash) + FEATURES_FIELD_NUMBER;
        hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
            getFeatures());
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     * linstor - API Version
     * </pre>
     *
     * Protobuf type {@code com.linbit.linstor.proto.MsgApiVersion}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.linbit.linstor.proto.MsgApiVersion)
        com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersionOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.linbit.linstor.proto.MsgApiVersionOuterClass.internal_static_com_linbit_linstor_proto_MsgApiVersion_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.linbit.linstor.proto.MsgApiVersionOuterClass.internal_static_com_linbit_linstor_proto_MsgApiVersion_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion.class, com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion.Builder.class);
      }

      // Construct using com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        min_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000001);
        max_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000002);
        features_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000004);
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.linbit.linstor.proto.MsgApiVersionOuterClass.internal_static_com_linbit_linstor_proto_MsgApiVersion_descriptor;
      }

      public com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion getDefaultInstanceForType() {
        return com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion.getDefaultInstance();
      }

      public com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion build() {
        com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion buildPartial() {
        com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion result = new com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.min_ = min_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.max_ = max_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.features_ = features_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion) {
          return mergeFrom((com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion other) {
        if (other == com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion.getDefaultInstance()) return this;
        if (other.hasMin()) {
          setMin(other.getMin());
        }
        if (other.hasMax()) {
          setMax(other.getMax());
        }
        if (other.hasFeatures()) {
          setFeatures(other.getFeatures());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        if (!hasMin()) {
          return false;
        }
        if (!hasMax()) {
          return false;
        }
        if (!hasFeatures()) {
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private long min_ ;
      /**
       * <code>required uint64 min = 1;</code>
       */
      public boolean hasMin() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required uint64 min = 1;</code>
       */
      public long getMin() {
        return min_;
      }
      /**
       * <code>required uint64 min = 1;</code>
       */
      public Builder setMin(long value) {
        bitField0_ |= 0x00000001;
        min_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required uint64 min = 1;</code>
       */
      public Builder clearMin() {
        bitField0_ = (bitField0_ & ~0x00000001);
        min_ = 0L;
        onChanged();
        return this;
      }

      private long max_ ;
      /**
       * <code>required uint64 max = 2;</code>
       */
      public boolean hasMax() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required uint64 max = 2;</code>
       */
      public long getMax() {
        return max_;
      }
      /**
       * <code>required uint64 max = 2;</code>
       */
      public Builder setMax(long value) {
        bitField0_ |= 0x00000002;
        max_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required uint64 max = 2;</code>
       */
      public Builder clearMax() {
        bitField0_ = (bitField0_ & ~0x00000002);
        max_ = 0L;
        onChanged();
        return this;
      }

      private long features_ ;
      /**
       * <code>required uint64 features = 3;</code>
       */
      public boolean hasFeatures() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>required uint64 features = 3;</code>
       */
      public long getFeatures() {
        return features_;
      }
      /**
       * <code>required uint64 features = 3;</code>
       */
      public Builder setFeatures(long value) {
        bitField0_ |= 0x00000004;
        features_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required uint64 features = 3;</code>
       */
      public Builder clearFeatures() {
        bitField0_ = (bitField0_ & ~0x00000004);
        features_ = 0L;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:com.linbit.linstor.proto.MsgApiVersion)
    }

    // @@protoc_insertion_point(class_scope:com.linbit.linstor.proto.MsgApiVersion)
    private static final com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion();
    }

    public static com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<MsgApiVersion>
        PARSER = new com.google.protobuf.AbstractParser<MsgApiVersion>() {
      public MsgApiVersion parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new MsgApiVersion(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<MsgApiVersion> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<MsgApiVersion> getParserForType() {
      return PARSER;
    }

    public com.linbit.linstor.proto.MsgApiVersionOuterClass.MsgApiVersion getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_linbit_linstor_proto_MsgApiVersion_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_linbit_linstor_proto_MsgApiVersion_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\031proto/MsgApiVersion.proto\022\030com.linbit." +
      "linstor.proto\";\n\rMsgApiVersion\022\013\n\003min\030\001 " +
      "\002(\004\022\013\n\003max\030\002 \002(\004\022\020\n\010features\030\003 \002(\004"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_com_linbit_linstor_proto_MsgApiVersion_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_linbit_linstor_proto_MsgApiVersion_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_linbit_linstor_proto_MsgApiVersion_descriptor,
        new java.lang.String[] { "Min", "Max", "Features", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
