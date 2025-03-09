package com.alexismoraportal.smartble.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import com.alexismoraportal.smartble.R
import com.alexismoraportal.smartble.components.AppButton
import com.alexismoraportal.smartble.ui.theme.BackgroundDark
import com.alexismoraportal.smartble.ui.theme.TextPrimaryDark

/**
 * CodeScreen displays the source code of the ESP32 firmware used for controlling NeoPixels via BLE.
 *
 * This screen is designed with the following features:
 *  - A top bar that shows the screen title and a copy button.
 *    When the copy button is pressed, the displayed code is copied to the clipboard and a toast
 *    message confirms the action.
 *  - A scrollable area that presents the firmware code in a monospaced font, preserving its original
 *    formatting.
 *  - A button at the bottom that opens the GitHub repository containing the firmware source code.
 *
 * All text displayed in this screen is retrieved from string resources (supporting internationalization),
 * and the UI elements adhere to custom theming (e.g., BackgroundDark and TextPrimaryDark).
 *
 * Usage:
 *  - Ensure that the required string resources (such as "code_screen_title", "copy_code", "code_copied",
 *    and "open_repository") are defined in your strings.xml files.
 *  - Call CodeScreen() within a composable context (for instance, inside setContent in your Activity).
 *
 * Example:
 *  @Composable
 *  fun MyApp() {
 *      CodeScreen()
 *  }
 */

@Composable
fun CodeScreen() {
    // ClipboardManager and Context to show a Toast message
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Code to display, with trimIndent to maintain the format
    val code = """
        /**
         * @file main.cpp
         * @brief BLE-based NeoPixel controller for an 8x8 (64-pixel) matrix.
         *        Automatically restarts the ESP32 upon BLE client disconnection.
         *        Developer: Alexis Mora
         *
         * This code implements a BLE peripheral that allows a central device (e.g., smartphone) to:
         *  - Send commands to control a strip of 64 NeoPixels (WS2812/WS2812B).
         *  - Receive periodic notifications with sensor data (digital input and analog reading).
         *  - Restart the ESP32 automatically if the BLE client disconnects after having been connected.
         *
         * Command format examples:
         *   1) "*brightness,red,green,blue." 
         *       Sets ALL pixels to the same color & brightness (global).
         *       e.g., "*100,255,0,0." -> all red at brightness=100
         *
         *   2) "_index,brightness,red,green,blue."
         *       Sets ONE pixel at 'index' to the given color.
         *       brightness can be used or ignored for global brightness changes.
         *       e.g., "_3,120,0,255,128." -> LED #3 with brightness=120, color=(0,255,128)
         *       e.g., "_10,0,255,255,0."  -> LED #10 with brightness=0 (ignored?), color=(255,255,0)
         *
         *   3) "!."
         *       Clears (turns off) all pixels.
         *
         * The code also reads a digital sensor at pin 23 and an analog sensor at pin 34,
         * sending their values every 500ms via BLE notifications in the format "D:x,A:y".
         */
        
        #include <BLEDevice.h>
        #include <BLEServer.h>
        #include <BLEUtils.h>
        #include <BLE2902.h>
        #include <Adafruit_NeoPixel.h>
        #include <Arduino.h>
        
        // ---------------------- Pin and NeoPixel Configuration ----------------------
        #define NEOPIXEL_PIN       32
        #define NUM_PIXELS         64
        
        // Sensor pins
        #define DIGITAL_SENSOR_PIN 23
        #define ANALOG_SENSOR_PIN  34
        
        // -------------------------- BLE UUID Definitions ----------------------------
        #define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        #define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
        
        // ---------------------- Global Variables and Objects ------------------------
        BLEServer*          pServer         = nullptr;
        BLECharacteristic*  pCharacteristic = nullptr;
        bool                deviceConnected = false;
        
        Adafruit_NeoPixel strip(NUM_PIXELS, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ800);
        
        int previousDigitalValue = -1;
        int previousAnalogValue  = -1;
        
        /**
         * @class MyServerCallbacks
         * @brief Handles BLE server events, including connection and disconnection.
         */
        class MyServerCallbacks : public BLEServerCallbacks {
          void onConnect(BLEServer* pServer) override {
            deviceConnected = true;
            BLEDevice::stopAdvertising();
            Serial.println("BLE client connected.");
          }
        
          void onDisconnect(BLEServer* pServer) override {
            if (deviceConnected) {
              Serial.println("BLE client disconnected. Restarting ESP32...");
              ESP.restart();
            }
            deviceConnected = false;
          }
        };
        
        /**
         * @class MyCharacteristicCallbacks
         * @brief Handles BLE characteristic write events (commands from the client).
         */
        class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
          void onWrite(BLECharacteristic* pCharacteristic) override {
            std::string rxValue = pCharacteristic->getValue();
            if (!rxValue.empty()) {
              Serial.print("Received BLE command: ");
              Serial.println(rxValue.c_str());
        
              String command = String(rxValue.c_str());
              if (command.endsWith(".")) {
                command.remove(command.length() - 1);
              }
        
              if (command.startsWith("*")) {
                // Format: "*brightness,red,green,blue"
                command.remove(0, 1);
                
                int firstComma  = command.indexOf(',');
                int secondComma = command.indexOf(',', firstComma + 1);
                int thirdComma  = command.indexOf(',', secondComma + 1);
        
                if (firstComma != -1 && secondComma != -1 && thirdComma != -1) {
                  int brightness = command.substring(0, firstComma).toInt();
                  int red        = command.substring(firstComma + 1, secondComma).toInt();
                  int green      = command.substring(secondComma + 1, thirdComma).toInt();
                  int blue       = command.substring(thirdComma + 1).toInt();
        
                  strip.setBrightness(brightness);
                  for (int i = 0; i < NUM_PIXELS; i++) {
                    strip.setPixelColor(i, strip.Color(red, green, blue));
                  }
                  strip.show();
        
                  Serial.printf("All LEDs -> Brightness=%d, Color=(%d,%d,%d)\n",
                                brightness, red, green, blue);
                } else {
                  Serial.println("Invalid format for '*' command. Expected 3 commas.");
                }
              }
              else if (command.startsWith("_")) {
                // Format: "_index,brightness,red,green,blue"
                command.remove(0, 1);
        
                int firstComma  = command.indexOf(',');
                int secondComma = command.indexOf(',', firstComma + 1);
                int thirdComma  = command.indexOf(',', secondComma + 1);
                int fourthComma = command.indexOf(',', thirdComma + 1);
        
                if (firstComma != -1 && secondComma != -1 &&
                    thirdComma != -1 && fourthComma != -1) {
        
                  int ledIndex   = command.substring(0, firstComma).toInt();
                  int brightness = command.substring(firstComma + 1, secondComma).toInt();
                  int red        = command.substring(secondComma + 1, thirdComma).toInt();
                  int green      = command.substring(thirdComma + 1, fourthComma).toInt();
                  int blue       = command.substring(fourthComma + 1).toInt();
        
                  strip.setBrightness(130);
        
                  if (ledIndex >= 0 && ledIndex < NUM_PIXELS) {
                    strip.setPixelColor(ledIndex, strip.Color(red, green, blue));
                    strip.show();
                    Serial.printf("LED %d -> Brightness=%d, Color=(%d,%d,%d)\n",
                                  ledIndex, brightness, red, green, blue);
                  } else {
                    Serial.println("LED index out of range.");
                  }
                } else {
                  Serial.println("Invalid format for '_' command. Expected 4 commas.");
                }
              }
              else if (command.startsWith("!")) {
                // Format: "!" -> Clears all LEDs.
                strip.clear();
                strip.show();
                Serial.println("All LEDs turned OFF.");
              }
              else {
                Serial.println("Unknown command (must start with '*', '_' or '!').");
              }
            }
          }
        };
        
        void setup() {
          Serial.begin(115200);
          pinMode(DIGITAL_SENSOR_PIN, INPUT);
        
          strip.begin();
          strip.setBrightness(100);
          strip.show();
        
          BLEDevice::init("SmartBleDevice");
          pServer = BLEDevice::createServer();
          pServer->setCallbacks(new MyServerCallbacks());
        
          BLEService* pService = pServer->createService(SERVICE_UUID);
          pCharacteristic = pService->createCharacteristic(
            CHARACTERISTIC_UUID,
            BLECharacteristic::PROPERTY_WRITE | 
            BLECharacteristic::PROPERTY_NOTIFY
          );
          pCharacteristic->addDescriptor(new BLE2902());
          pCharacteristic->setCallbacks(new MyCharacteristicCallbacks());
        
          pService->start();
        
          BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
          pAdvertising->addServiceUUID(SERVICE_UUID);
          pAdvertising->setScanResponse(false);
          pAdvertising->setMinPreferred(0x06);
          pAdvertising->setMinPreferred(0x12);
          BLEDevice::startAdvertising();
        
          Serial.println("BLE device is ready and advertising...");
        }
        
        void loop() {
          if (deviceConnected) {
            static unsigned long lastNotifyTime = 0;
            unsigned long currentMillis = millis();
        
            if (currentMillis - lastNotifyTime >= 500) {
              lastNotifyTime = currentMillis;
              int currentDigital = digitalRead(DIGITAL_SENSOR_PIN);
              int currentAnalog  = analogRead(ANALOG_SENSOR_PIN);
        
              String sensorData = "D:" + String(currentDigital) + 
                                  ",A:" + String(currentAnalog);
        
              pCharacteristic->setValue(sensorData.c_str());
              pCharacteristic->notify();
        
              Serial.print("Notifying sensor data: ");
              Serial.println(sensorData);
        
              previousDigitalValue = currentDigital;
              previousAnalogValue  = currentAnalog;
            }
          }
          delay(100);
        }
    """.trimIndent()

    // Main container with background color from the theme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        // Top bar with title and copy button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.code_screen_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                modifier = Modifier.padding(8.dp)
            )
            val copyCodeText = stringResource(id = R.string.copy_code)
            IconButton(
                onClick = {
                    // Copy the code to the clipboard
                    clipboardManager.setText(AnnotatedString(code))
                    Toast.makeText(context, copyCodeText, Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_code),
                    tint = TextPrimaryDark
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Box containing the code text with monospace font style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = TextPrimaryDark
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Button that opens the GitHub repository using an intent
        AppButton(
            text = stringResource(R.string.open_repository),
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/AlexisZhuber/LightControl-ESP32-Firmware.git")
                )
                context.startActivity(intent)
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
