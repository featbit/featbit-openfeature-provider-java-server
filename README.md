# FeatBit java server OpenFeature provider

This provider allows to use [FeatBit](https://www.featbit.co/) with the [OpenFeature](https://openfeature.dev/) SDK
for Java.

This provider is designed primarily for use in multi-user systems such as web servers and applications.
It is not intended for use in desktop and embedded systems applications.

## Getting started

FeaBit provider works with Java 8 and above and is available on Maven Central.
You can add it to your project using the following dependency.

### Installation

```xml

<dependencies>
    <dependency>
        <groupId>co.featbit</groupId>
        <artifactId>featbit-java-server-openfeature-provider</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Usage

```java
FBConfig config = new FBConfig.Builder()
        .streamingURL(STREAM_URL)
        .eventURL(EVENT_URL)
        .build();

// Synchronous
OpenFeatureAPI.getInstance().setProviderAndWait(new FeatBitProvider(ENV_SECRET, config);

// Asynchronous
OpenFeatureAPI.getInstance().setProvider(new FeatBitProvider(ENV_SECRET, config);

// Refer to docs to get a client and perform evaluations.
```

For more information on using this OpenFeature SDK please refer to
the [OpenFeature Java Documentation](https://openfeature.dev/docs/reference/technologies/server/java)
and [FeatBit Java Server SDK Guide](https://github.com/featbit/featbit-java-sdk).

## OpenFeature Specific Considerations

### Evaluation Context

FeatBit SDK evaluates only a single-user context.

The OpenFeature specification allows for an optional targeting key, but FeatBit requires a key for evaluation.
A targeting key must be specified for each context being evaluated. It may be specified using either `targetingKey`, as
it is in the OpenFeature specification, or `key`/`keyid`, which is the typical identifier for the targeting key.
If a `targetingKey` and a `key`/`keyid` are specified, then the targetingKey will take precedence.

`name` is also an attribute that is used to search your user quickly. If you don't set it explicitly in your context,
FeatBit will use the targeting key as the name.

Featbit SDK only supports string type values for custom attributes in evaluation context.

```java
 EvaluationContext ctx = new ImmutableContext("user-key", new HashMap() {{
    put("name", new Value("user-name"));
    put("country", new Value("USA"));
}});

```

### Evaluation

The OpenFeature specification allows for an optional evaluation context in the evaluation request, but FeatBit requires
a context for evaluation.

```java
Client client = OpenFeatureAPI.getInstance().getClient();
// Evaluation Context
EvaluationContext evalCtx = new ImmutableContext("user-key", new HashMap() {{
    put("name", new Value("user-name"));
    put("country", new Value("USA"));
}});
// Evaluate a feature flag
String result = client.getStringValue(flagKey, defaultValue, evalCtx);
// Evaluate a feature detail
FlagEvaluationDetails<String> details = client.getStringDetails(flagKey, defaultValue, evalCtx);

```

When you use the `Client#getObjectValue` or `Client#getObjectDetails` methods, the SDK will attempt to convert the
result to the specified type:

1. the SDK will convert the result to the `Value` type according to the default `Value`.
2. If your put a `List` or `Structure` Value as the default value, the SDK will parse the result as it is a json object.

If you set a wrong type of default value which is not corresponding to the settings in FeatBit flag center, the SDK may
throw an exception.

## More Information

Read documentation for in-depth instructions on configuring and using FeatBit. You can also head straight to the
complete [reference guide](https://docs.featbit.co/)

Read documentation for in-depth instructions on using OpenFeature. You can also head straight to
the [Documentation](https://openfeature.dev/docs/reference/intro) 