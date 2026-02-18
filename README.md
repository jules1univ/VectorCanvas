# 🎨 VectorCanvas

> A lightweight, renderer for Java Swing applications using native rendering.

![C](https://img.shields.io/github/languages/top/jules1univ/VectorCanvas?label=Java%20%2B%20C%20🔥)
![Platform](https://img.shields.io/badge/Platform%20🌍-Windows%20|%20Linux%20|%20MacOS-purple)
![Charts](https://img.shields.io/badge/VectorCanvas%20🎨-SVG-green)
[![Build](https://github.com/jules1univ/VectorCanvas/actions/workflows/build-jar.yml/badge.svg)](https://github.com/jules1univ/VectorCanvas/actions/workflows/build-jar.yml)
[![Test](https://github.com/jules1univ/VectorCanvas/actions/workflows/test-junit.yml/badge.svg)](https://github.com/jules1univ/VectorCanvas/actions/workflows/test-junit.yml)

```bash
git clone --recurse-submodules https://github.com/julesgrc0/VectorCanvas.git
cd VectorCanvas
```

## Architecture

### Java Layer (`java/`)

- **NativeCanvas**: Main component extending JPanel for SVG rendering
- **NativeLibLoader**: Handles cross-platform native library loading using JNI
- Maven build system for dependency management and packaging

### Native Layer (`native/`)

- **vectorcanvas.c**: JNI implementation for SVG parsing and rendering
- **NanoSVG**: Lightweight SVG parsing and rasterization library (embedded)
- CMake build system for cross-platform native compilation

## Requirements

- **Java**: JDK 11 or higher
- **Maven**: 3.6 or higher
- **CMake**: 3.16 or higher
- **C Compiler**:
  - Windows: MSVC or MinGW
  - Linux: GCC
  - macOS: Clang

## Building

### Build Native Library

```bash
cd native
mkdir build
cd build

cmake ..
cmake --build . --config Release
```

### Build Java Library

```bash
cd java
mvn clean package
mvn test
```

### Supported Platforms

| Platform | Architecture | Status       |
| -------- | ------------ | ------------ |
| Windows  | x86_64       | ✅ Supported |
| Linux    | x86_64       | ✅ Supported |
| macOS    | x86_64       | ✅ Supported |

## Installation

1. Build the project (see above)
2. Include the JAR in your project:

**No-Build:**

- Copy `vectorcanvas-1.0.0.jar` to `lib/vectorcanvas-1.0.0.jar`

**Maven:**

```xml
<dependency>
    <groupId>com.jules1univ</groupId>
    <artifactId>vectorcanvas</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/vectorcanvas-1.0.0.jar</systemPath>
</dependency>
```

**Gradle:**

```gradle
implementation files('lib/vectorcanvas-1.0.0.jar')
```

## Usage

### Basic Example

```java
import com.jules1univ.vectorcanvas.NativeCanvas;
import javax.swing.*;

public class Example {
    public static void main(String[] args) {
        String svg = """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="200" height="200" xmlns="http://www.w3.org/2000/svg">
                <circle cx="100" cy="100" r="80" fill="blue"/>
                <text x="100" y="110" text-anchor="middle" fill="white"
                      font-size="24">Hello SVG!</text>
            </svg>
            """;

        NativeCanvas canvas = NativeCanvas.of(svg);

        JFrame frame = new JFrame("VectorCanvas Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
    }
}
```

### Loading from File

```java
Path svgPath = Path.of("image.svg");
NativeCanvas canvas = NativeCanvas.of(svgPath);
if (canvas != null) {
    // Use the canvas in your application
}
```

### Access Rendered Image

```java
NativeCanvas canvas = NativeCanvas.of(svg);
BufferedImage image = canvas.getImage();

if (image != null) {
    ImageIO.write(image, "PNG", new File("output.png"));
}
```

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.
