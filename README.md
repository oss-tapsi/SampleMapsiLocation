# Mapsi Location SDK

**Tapsi Geo Location SDK (Mapsi Location)** is an Android location SDK that provides the user's location to Android applications.

The SDK is designed to provide a reliable location even when the device cannot obtain a sufficiently accurate location directly from its location providers.

---

## Table of Contents

* [Overview](#overview)
* [Location Modes](#location-modes)

    * [Raw](#raw)
    * [Denoised](#denoised)

        * [Default](#denoised-default)
        * [Custom](#denoised-custom)
* [Sample Project](#sample-project)
* [Requirements](#requirements)
* [Installation](#installation)
* [Initialization](#initialization)
* [Getting Location](#getting-location)

    * [Single Location](#single-location)
    * [Continuous Location](#continuous-location)
* [MapsiNetworkConfig](#mapsinetworkconfig)
* [Support](#support)

---

# Overview

Mapsi Location provides two different approaches for obtaining the user's location:

1. **Raw** — Uses the location provided directly by the Android device.
2. **Denoised** — Uses additional processing and backend services to provide the best possible location.

The SDK can be integrated into Android applications written in both **Kotlin** and **Java**.

---

# Location Modes

## Raw

In **Raw** mode, the SDK uses the location provided by the Android device's location provider.

No additional backend service or API key is required.

### Advantages

* Simple integration
* No backend configuration required
* No API key required

### Limitations

Because Raw mode relies on the location provided by the device, the resulting location may not be sufficiently accurate in environments with poor GPS conditions.

---

## Denoised

In **Denoised** mode, Mapsi Location attempts to improve the user's location by using the device's location data together with additional processing and backend services.

Denoised mode has two integration options:

1. **Default**
2. **Custom**

---

# Sample Project

A sample project is provided with the SDK.

The sample demonstrates the different ways of initializing and using Mapsi Location so that developers can choose the integration method that best fits their application requirements.

---

# Requirements

Before using Mapsi Location, make sure the following requirements are satisfied.

### 1. Location Permission

The application must have the required Android location permission.

### 2. Location Services

Location services must be enabled on the Android device.

If the required permission has not been granted or location services are disabled, Mapsi Location cannot provide a location.

---

# Installation

Add the Mapsi Location dependency to your Android application.

### Gradle

```kotlin
implementation("ir.tapsi.map:geo-location-sdk:<latest_version>")
```

Replace `<latest_version>` with the version you want to use.

You can find the available versions on Maven Central:

[Mapsi Location SDK on Maven Central](https://central.sonatype.com/artifact/ir.tapsi.map/geo-location-sdk-android/1.0.0/versions?utm_source=chatgpt.com)

---

# Initialization

First, create an instance of `MapsiLocation`.

### Java

```java
MapsiLocation mapsiLocation = new MapsiLocation();
```

### Kotlin

```kotlin
val mapsiLocation = MapsiLocation()
```

Next, create an `ApplicationInitializer` using the Android `Application` instance.

### Java

```java
ApplicationInitializer applicationInitializer =
        new ApplicationInitializer(getApplication());
```

### Kotlin

```kotlin
val applicationInitializer = ApplicationInitializer(application)
```

The `ApplicationInitializer` is required when creating the Mapsi Location configuration.

---

# Raw Mode

To use Raw mode, create a `MapsiLocationConfig.Raw` configuration and start Mapsi Location.

### Java

```java
MapsiLocationConfig mapsiLocationConfig =
        new MapsiLocationConfig.Raw(
                applicationInitializer,
                new ApplicationInfo("test")
        );

mapsiLocation.start(mapsiLocationConfig);
```

### Kotlin

```kotlin
val mapsiLocationConfig = MapsiLocationConfig.Raw(
    applicationInitializer = applicationInitializer,
    applicationInfo = ApplicationInfo("test")
)

mapsiLocation.start(mapsiLocationConfig)
```

### ApplicationInfo

The `applicationInfo` parameter is optional.

It can be used to provide a name that the library uses when storing data required by the SDK, for example in `SharedPreferences`.

---

# Denoised Mode

Denoised mode supports two different integration approaches:

* [Default](#denoised-default)
* [Custom](#denoised-custom)

---

## Denoised Default

The **Default** integration requires an API key from Tapsi services.

> **Security recommendation:** Because the API key is provided to the Android application, it may be possible for someone to extract and use it. It is therefore recommended to use the **Custom** integration when possible.

### Service Configuration

The Default integration requires the URLs for the authentication and location services.

### Java

```java
ServiceConfig.Full fullServiceConfig = new ServiceConfig.Full(
        new UrlConfig(
                "https://map.tapsi.ir/api/client/v1/auth/token",
                HttpRequestMethod.Post.INSTANCE
        ),
        new UrlConfig(
                "https://map.tapsi.ir/api/client/v1/location/geolocate/",
                HttpRequestMethod.Post.INSTANCE
        )
);
```

### Kotlin

```kotlin
val fullServiceConfig = ServiceConfig.Full(
    authConfig = UrlConfig(
        "https://map.tapsi.ir/api/client/v1/auth/token",
        HttpRequestMethod.Post
    ),
    getLocationConfig = UrlConfig(
        "https://map.tapsi.ir/api/client/v1/location/geolocate/",
        HttpRequestMethod.Post
    )
)
```

Then create the `MapsiLocationConfig.Denoised.Default` configuration.

### Java

```java
MapsiLocationConfig mapsiLocationConfig =
        new MapsiLocationConfig.Denoised.Default(
                API_KEY,
                fullServiceConfig,
                MapsiNetworkConfig.Companion.getDefault(),
                applicationInitializer,
                new ApplicationInfo("test")
        );

mapsiLocation.start(mapsiLocationConfig);
```

### Kotlin

```kotlin
val mapsiLocationConfig: MapsiLocationConfig =
    MapsiLocationConfig.Denoised.Default(
        apiKey = API_KEY,
        fullServiceConfig = fullServiceConfig,
        mapsiNetworkConfig = MapsiNetworkConfig.default,
        applicationInitializer = applicationInitializer,
        applicationInfo = ApplicationInfo("test")
    )

mapsiLocation.start(mapsiLocationConfig)
```

### Configuration Parameters

The Default configuration contains:

| Parameter                | Description                                                   |
| ------------------------ | ------------------------------------------------------------- |
| `apiKey`                 | API key provided by Tapsi services                            |
| `fullServiceConfig`      | Configuration of authentication and location endpoints        |
| `mapsiNetworkConfig`     | Optional network-related configuration                        |
| `applicationInitializer` | Application initializer                                       |
| `applicationInfo`        | Optional identifier used by the SDK for storing required data |

The authentication and location URLs must be provided through `ServiceConfig.Full`.

---

# Denoised Custom

The **Custom** integration does not require an API key inside the Android application.

Instead, the application provides a `NetworkProvider` implementation to Mapsi Location.

In this architecture, your application's backend communicates with Tapsi backend services. Mapsi Location communicates with your application's backend through the provided `NetworkProvider`.

This approach keeps the Tapsi API key on your backend instead of exposing it in the Android application.

## Service Configuration

For Custom mode, you need to provide the URL of your application's location endpoint.

### Java

```java
ServiceConfig.Derivative derivativeServiceConfig =
        new ServiceConfig.Derivative(
                new UrlConfig(
                        "your server url",
                        HttpRequestMethod.Post.INSTANCE
                )
        );
```

### Kotlin

```kotlin
val derivativeServiceConfig = ServiceConfig.Derivative(
    UrlConfig(
        "your server url",
        HttpRequestMethod.Post
    )
)
```

---

## NetworkProvider

The library uses `NetworkProvider` to communicate with your application's backend.

### Java

For Java applications, `LegacyNetworkProviderAdapter` can be used to implement the network provider:

```java
NetworkProvider networkProvider = new LegacyNetworkProviderAdapter(
        (requestData, networkCallback) -> {
            // Make the API call to your application server.
            // Retrofit or another networking solution can be used here.
        }
);
```

### Kotlin

In Kotlin, you can implement `NetworkProvider` directly:

```kotlin
val networkProvider: NetworkProvider = object : NetworkProvider {

    override suspend fun apiCall(
        request: RequestData
    ): ResponseData {
        // Make the API call to your application server.
        // Retrofit or another networking solution can be used here.
    }
}
```

The Kotlin `NetworkProvider` exposes a `suspend` function, allowing the implementation to perform asynchronous network operations without manually managing the asynchronous execution.

---

## Starting Custom Mode

### Java

```java
MapsiLocationConfig mapsiLocationConfig =
        new MapsiLocationConfig.Denoised.Custom(
                derivativeServiceConfig,
                networkProvider,
                MapsiNetworkConfig.Companion.getDefault(),
                applicationInitializer,
                new ApplicationInfo("test")
        );

mapsiLocation.start(mapsiLocationConfig);
```

### Kotlin

```kotlin
val mapsiLocationConfig: MapsiLocationConfig =
    MapsiLocationConfig.Denoised.Custom(
        derivativeServiceConfig = derivativeServiceConfig,
        networkProvider = networkProvider,
        mapsiNetworkConfig = MapsiNetworkConfig.default,
        applicationInitializer = applicationInitializer,
        applicationInfo = ApplicationInfo("test")
    )

mapsiLocation.start(mapsiLocationConfig)
```

---

# ResponseData

When using Custom mode, the `NetworkProvider` must return a `ResponseData`.

The returned data must match the expected response structure of the API being called.

For example, the location service can return a response similar to:

```json
{
  "location": {
    "latitude": 35.7448,
    "longitude": 51.3753,
    "altitude": 435.0
  },
  "timestamp": 1756630000000,
  "accuracy": 5.0,
  "provider": "LOCATION_PROVIDER_FUSED",
  "speed": 0.0,
  "bearing": 0.0,
  "isMocked": true
}
```

The JSON response should then be provided through `ResponseData`.

### Java

```java
new ResponseData(
        200,
        Map.of(),
        jsonBody
);
```

### Kotlin

```kotlin
ResponseData(
    code = 200,
    headers = mapOf(),
    body = jsonBody
)
```

---

# Getting Location

After Mapsi Location has been initialized and started, there are two ways to request a location:

1. **Single Location**
2. **Continuous Location**

---

# Single Location

Use `getLocation` when you need to obtain a location once.

## Java

```java
MapsiLocationListener mapsiLocationListener = location -> {
    // Location received here
};

mapsiLocation.getLocation(
        2000,
        mapsiLocationListener
);
```

## Kotlin

```kotlin
val mapsiLocationListener: MapsiLocationListener =
    object : MapsiLocationListener {

        override fun onLocationReceived(location: Location?) {
            // Location received here
        }
    }

mapsiLocation.getLocation(
    timeoutByMilliSecond = 2000,
    mapsiLocationListener = mapsiLocationListener
)
```

### Timeout

The `timeoutByMilliSecond` parameter specifies how long the SDK waits while attempting to obtain the location.

A longer timeout can provide more time to obtain a more accurate location.

---

## Cancel Single Location Request

If you need to cancel a single location request, use `removeGetLocationListener`.

### Java

```java
mapsiLocation.removeGetLocationListener(
        mapsiLocationListener
);
```

### Kotlin

```kotlin
mapsiLocation.removeGetLocationListener(
    mapsiLocationListener = mapsiLocationListener
)
```

> **Important:** You must pass the exact same `MapsiLocationListener` instance that was provided to `getLocation`.

---

# Continuous Location

Use `getLiveLocation` when you need to continuously receive location updates.

## Java

```java
MapsiLocationListener mapsiLocationListener = location -> {
    // Location received here
};

mapsiLocation.getLiveLocation(
        2000,
        mapsiLocationListener
);
```

## Kotlin

```kotlin
val mapsiLocationListener: MapsiLocationListener =
    object : MapsiLocationListener {

        override fun onLocationReceived(location: Location?) {
            // Location received here
        }
    }

mapsiLocation.getLiveLocation(
    intervalByMilliSecond = 2000,
    mapsiLocationListener = mapsiLocationListener
)
```

### intervalByMilliSecond

The `intervalByMilliSecond` parameter determines the minimum interval at which the SDK attempts to calculate and provide a new location.

If multiple continuous location requests are registered with different intervals, the SDK uses the smallest requested interval.

---

## Cancel Continuous Location

To stop receiving continuous location updates, call `removeLiveLocationListener`.

### Java

```java
mapsiLocation.removeLiveLocationListener(
        mapsiLocationListener
);
```

### Kotlin

```kotlin
mapsiLocation.removeLiveLocationListener(
    mapsiLocationListener = mapsiLocationListener
)
```

> **Important:** You must pass the exact same listener instance that was provided to `getLiveLocation`.

If `removeLiveLocationListener` is not called, the SDK continues attempting to provide location updates.

---

# MapsiNetworkConfig

`MapsiNetworkConfig` is an optional configuration that allows you to customize network-related behavior of Mapsi Location.

The default configuration is:

### Kotlin

```kotlin
val default = MapsiNetworkConfig(
    defaultDenoiseMethod = null,
    denoiseMethods = emptyList(),
    denoisingRequestTimeOutByMilliSecond = 2000
)
```

The configuration contains three main properties:

| Property                               | Description                                      |
| -------------------------------------- | ------------------------------------------------ |
| `defaultDenoiseMethod`                 | The default denoise method used by the SDK       |
| `denoiseMethods`                       | The list of available denoise methods            |
| `denoisingRequestTimeOutByMilliSecond` | Maximum time to wait for the denoise API request |

The default timeout for a denoising request is `2000` milliseconds.

---

# Denoise Methods

You can configure multiple denoise methods and switch between them when needed.

For example, suppose the backend provides two denoise methods:

```text
first
second
```

You can configure the SDK to start with `first` and make both methods available.

### Kotlin

```kotlin
val firstDenoiseMethod = "first"
val secondDenoiseMethod = "second"

val mapsiNetworkConfig = MapsiNetworkConfig(
    defaultDenoiseMethod = firstDenoiseMethod,
    denoiseMethods = listOf(
        firstDenoiseMethod,
        secondDenoiseMethod
    ),
    denoisingRequestTimeOutByMilliSecond = 2000
)
```

The SDK starts with `first` as the default denoise method.

---

## Changing Denoise Method

You can change the active denoise method at runtime.

### Java

```java
mapsiLocation.updateDenoiseMethod(
        secondDenoiseMethod
);
```

### Kotlin

```kotlin
mapsiLocation.updateDenoiseMethod(
    denoiseMethod = secondDenoiseMethod
)
```

This allows the application to switch between different denoise strategies without recreating the Mapsi Location instance.

---

# Integration Summary

There are several ways to integrate Mapsi Location depending on your requirements.

| Mode               | Backend Required | API Key in App | Custom Network Layer |
| ------------------ | ---------------: | -------------: | -------------------: |
| Raw                |               No |             No |                   No |
| Denoised / Default |   Tapsi services |            Yes |                   No |
| Denoised / Custom  |     Your backend |             No |                  Yes |

### Recommended Integration

For applications using Denoised mode, the **Custom** integration is recommended when you want to avoid exposing the Tapsi API key inside the Android application.

In this approach:

```text
Android Application
        |
        | NetworkProvider
        v
Your Backend
        |
        | API Key
        v
Tapsi Backend Services
```

This keeps the API key on your backend while allowing Mapsi Location to obtain the required location information through your application's server.

---

# Complete Basic Flow

The general integration flow is:

```text
1. Add Mapsi Location dependency
            ↓
2. Create MapsiLocation
            ↓
3. Create ApplicationInitializer
            ↓
4. Select location mode
            ↓
5. Create MapsiLocationConfig
            ↓
6. Start MapsiLocation
            ↓
7. Request location
            ↓
8. Receive location through MapsiLocationListener
```

---

# Important Notes

* Location permission must be granted before requesting a location.
* Location services must be enabled on the device.
* In Raw mode, no backend configuration is required.
* Denoised Default requires an API key.
* Denoised Custom requires implementing `NetworkProvider`.
* When canceling a request, use the same listener instance that was registered.
* Continuous location updates remain active until the corresponding listener is removed.
* `MapsiNetworkConfig` can be used to customize denoising behavior.
* Multiple denoise methods can be configured and switched at runtime.

---

# Support

If you have any questions or encounter issues while integrating Mapsi Location, please contact the **Tapsi technical team**.
