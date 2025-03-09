# Smart BLE

Smart BLE is an Android application that demonstrates how to use Bluetooth Low Energy (BLE) in modern Android development using Jetpack Compose and Dagger Hilt. The app allows scanning for BLE devices, connecting to a selected device, receiving sensor data, and controlling NeoPixels through BLE commands. It also provides additional screens to view the firmware source code and personal information about the developer.

## Features

- **BLE Scanning and Connection:**  
  Scan for nearby BLE devices (specifically devices named "SmartBleDevice") and connect to them.
  
- **Device Control and Sensor Data:**  
  Once connected, receive sensor data (digital and analog) from the BLE device and send commands to control an 8x8 NeoPixel matrix.
  
- **Foreground Service with Persistent Notification:**  
  When a device is connected, a foreground service is started that displays the current sensor data in a persistent notification. This ensures that sensor data remains visible even if the application is closed. The notification is updated in real-time (every 500ms) without causing disruptive sounds or vibrations.
  
- **Modern UI with Jetpack Compose:**  
  All UI components are built using Jetpack Compose for a modern, responsive interface.
  
- **Multiple Screens:**  
  - **HomeScreen:**  
    Displays the scanning UI or control interface based on connection state.
  - **CodeScreen:**  
    Shows the source code of the ESP32 firmware, allows copying the code to the clipboard, and provides a button to open the firmware GitHub repository.
  - **AboutScreen:**  
    Contains detailed personal and professional information about the author, along with links to GitHub and a personal portfolio.
  
- **Dependency Injection:**  
  Uses Dagger Hilt for dependency management and to facilitate scalable and testable code.
  
- **Reactive Data Handling:**  
  Uses Kotlin StateFlow and ViewModel to manage BLE state and UI updates efficiently.

## Getting Started

### Prerequisites

- **Android Studio** (Arctic Fox or later recommended)
- **Android SDK** version 21 (Lollipop) or higher
- A physical Android device or emulator with Bluetooth capabilities
- BLE permissions and Bluetooth enabled on the device

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/AlexisZhuber/Smart-Ble.git
   ```

2. **Open the project in Android Studio.**

3. **Sync Gradle and build the project.**

4. **Run the application** on your Android device.

## Usage

- **Permissions & Bluetooth:**  
  On startup, the app requests the necessary BLE and location permissions. If Bluetooth is disabled, the app prompts the user to enable it.

- **HomeScreen:**  
  - When no device is connected, use the scanning UI to discover nearby BLE devices.
  - When connected, the control UI is shown with sensor data, a disconnect button, and additional controls (like a ColorPicker).

- **CodeScreen:**  
  View the source code of the ESP32 firmware in a scrollable area with an option to copy the code to the clipboard or open the GitHub repository.

- **AboutScreen:**  
  Learn more about the author (Alexis Mora) – a Mechatronics Engineer with extensive experience in Android development, embedded systems, and modern web technologies like React. This screen also provides direct links to GitHub and a personal portfolio for further contact or collaboration.

- **Foreground Service:**  
  When a BLE device is connected, the app starts a foreground service that displays a persistent notification with live sensor data updates. This service continues running in the background, ensuring that the sensor data is always visible, even when the app is closed. The notification is updated every 500ms without causing repeated sounds or vibrations.

## Project Structure

- **MainActivity.kt:**  
  Initializes the application, handles runtime permissions, ensures Bluetooth is enabled, and sets up the Compose UI with navigation.
  
- **Navigation:**  
  Manages navigation between different screens (HomeScreen, AboutScreen, CodeScreen).

- **Screens:**  
  - **HomeScreen:** Handles BLE device scanning and connection state.
  - **AboutScreen:** Displays detailed personal and professional information.
  - **CodeScreen:** Presents the source code for the ESP32 firmware with functionality for copying code and opening external links.
  
- **BLE Package:**  
  Contains the BLE logic:
  - **BleManager.kt:** Manages all BLE operations (scanning, connecting, data transfer, and notifications).
  - **BleViewModel.kt:** Provides a bridge between the BLE operations and the UI using Kotlin StateFlow and ViewModel.
  
- **Foreground Service:**  
  - **BleForegroundService.kt:** A foreground service that runs in the background, displaying sensor data in a persistent notification. It listens for local broadcasts with sensor updates and refreshes the notification accordingly.
  
- **Components:**  
  Contains reusable UI components such as `AppButton` for consistent styling.

- **UI Theme:**  
  Defines the color schemes and typography used throughout the app (e.g., `BackgroundDark`, `TextPrimaryDark`).

## Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository.**
2. **Create a new branch** for your feature or bug fix:
   ```bash
   git checkout -b feature/your-feature
   ```
3. **Commit your changes:**
   ```bash
   git commit -am 'Add new feature'
   ```
4. **Push to the branch:**
   ```bash
   git push origin feature/your-feature
   ```
5. **Open a Pull Request** on GitHub.

## License

This project is distributed under the [MIT License](LICENSE).

## Author

**Alexis Mora**  
Mechatronics Engineer & Android Developer  
[GitHub](https://github.com/AlexisZhuber) • [Portfolio](https://alexismoraportal.com)
