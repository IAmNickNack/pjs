# pjs-native

## pjs-native-context

Abstractions which assist in FFM operations

## pjs-native-device

A PJs device driver which communicates with hardware via Java FFM APIs

### Dependency coordinates for Maven

```xml
<dependency>
    <groupId>io.github.iamnicknack</groupId>
    <artifactId>pjs-native-device</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Dependency coordinates for Gradle

```kotlin
implementation("io.github.iamnicknack:pjs-native-device:0.1.0")
```

--- 

# GPIO v2 Line Flags Explained

## Flag Breakdown

**State & Direction:**
- `GPIO_V2_LINE_FLAG_USED` – Read-only. Reports if the GPIO is already claimed by another driver/application. You cannot request a used line.
- `GPIO_V2_LINE_FLAG_INPUT` – Configure this pin as an input (reads voltage state).
- `GPIO_V2_LINE_FLAG_OUTPUT` – Configure this pin as an output (drives voltage).

**Signal Polarity:**
- `GPIO_V2_LINE_FLAG_ACTIVE_LOW` – Inverts the logical interpretation. Normally low voltage = 0, high = 1. With this flag, low = 1, high = 0. Useful for active-low buttons or inverted LED logic.

**Edge Detection (Interrupts):**
- `GPIO_V2_LINE_FLAG_EDGE_RISING` – Trigger events on 0→1 transitions (inactive to active).
- `GPIO_V2_LINE_FLAG_EDGE_FALLING` – Trigger events on 1→0 transitions (active to inactive).

**Output Drive Modes:**
- `GPIO_V2_LINE_FLAG_OPEN_DRAIN` – Output pulls the line to ground when active, but doesn't drive high (external pull-up required). Safe for multi-master or voltage-level shifting.
- `GPIO_V2_LINE_FLAG_OPEN_SOURCE` – Opposite: drives high when active, doesn't pull low (external pull-down required).

**Pull Resistors (Bias):**
- `GPIO_V2_LINE_FLAG_BIAS_PULL_UP` – Enables internal pull-up resistor (line stays high unless grounded).
- `GPIO_V2_LINE_FLAG_BIAS_PULL_DOWN` – Enables internal pull-down resistor (line stays low unless driven high).
- `GPIO_V2_LINE_FLAG_BIAS_DISABLED` – Disables internal resistors (floating state without external bias).

**Event Timestamps:**
- `GPIO_V2_LINE_FLAG_EVENT_CLOCK_REALTIME` – Event timestamps use system REALTIME clock (can jump if system time adjusts).
- `GPIO_V2_LINE_FLAG_EVENT_CLOCK_HTE` – Uses hardware timestamping engine for precise, monotonic timestamps (if available on your hardware).

---

## Appropriate Flags by Pin Direction

### **Input Pins**

✅ Use:
- `GPIO_V2_LINE_FLAG_INPUT` (required)
- `GPIO_V2_LINE_FLAG_EDGE_RISING` and/or `GPIO_V2_LINE_FLAG_EDGE_FALLING` (if you need interrupts)
- `GPIO_V2_LINE_FLAG_ACTIVE_LOW` (if your signal is inverted)
- `GPIO_V2_LINE_FLAG_BIAS_PULL_UP` or `GPIO_V2_LINE_FLAG_BIAS_PULL_DOWN` (if no external pull resistor exists)
- `GPIO_V2_LINE_FLAG_EVENT_CLOCK_REALTIME` or `GPIO_V2_LINE_FLAG_EVENT_CLOCK_HTE` (to choose event timestamp source)

❌ Don't use:
- `GPIO_V2_LINE_FLAG_OUTPUT`
- `GPIO_V2_LINE_FLAG_OPEN_DRAIN`, `GPIO_V2_LINE_FLAG_OPEN_SOURCE` (output drive modes)

### **Output Pins**

✅ Use:
- `GPIO_V2_LINE_FLAG_OUTPUT` (required)
- `GPIO_V2_LINE_FLAG_ACTIVE_LOW` (if your signal is inverted)
- `GPIO_V2_LINE_FLAG_OPEN_DRAIN` or `GPIO_V2_LINE_FLAG_OPEN_SOURCE` (if you need wired-AND/OR or multi-master capability)

❌ Don't use:
- `GPIO_V2_LINE_FLAG_INPUT`
- `GPIO_V2_LINE_FLAG_EDGE_RISING`, `GPIO_V2_LINE_FLAG_EDGE_FALLING` (meaningless on outputs)
- `GPIO_V2_LINE_FLAG_BIAS_PULL_UP`, `GPIO_V2_LINE_FLAG_BIAS_PULL_DOWN` (less relevant; use drive modes instead)

**Note:** `GPIO_V2_LINE_FLAG_USED` is read-only and always determined by the kernel.