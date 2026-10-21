package com.example.data

object FirmwareGuide {

    const val ARDUINO_SKETCH = """/*
 * NodeMCU ESP8266 ESP-12E Built-in LED Wi-Fi Controller
 * 
 * Target Board: NodeMCU 1.0 (ESP-12E Module)
 * Onboard LED: GPIO 2 (D4) - Note: Active LOW polarity
 *
 * Endpoints:
 *   GET  /api/status   - Get current LED status & device telemetry
 *   POST /api/led/on   - Turn built-in LED ON
 *   POST /api/led/off  - Turn built-in LED OFF
 *
 * mDNS Address: http://nodemcu-led.local
 */

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

// =================== CONFIGURATION ===================
const char* ssid     = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// NodeMCU ESP-12E onboard blue LED is attached to GPIO2 (D4)
// Polarity: ACTIVE LOW (LOW = ON, HIGH = OFF)
#define LED_PIN 2

ESP8266WebServer server(80);
bool isLedOn = false;

// Helper to add CORS headers so web clients & apps can communicate seamlessly
void setCorsHeaders() {
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
}

void handleOptions() {
  setCorsHeaders();
  server.send(204);
}

// GET /api/status
void handleStatus() {
  setCorsHeaders();
  String json = "{\"status\":\"ok\",";
  json += "\"led\":\"" + String(isLedOn ? "ON" : "OFF") + "\",";
  json += "\"gpio\":" + String(LED_PIN) + ",";
  json += "\"uptime\":" + String(millis() / 1000) + ",";
  json += "\"rssi\":" + String(WiFi.RSSI()) + ",";
  json += "\"ip\":\"" + WiFi.localIP().toString() + "\",";
  json += "\"heap\":" + String(ESP.getFreeHeap()) + "}";
  server.send(200, "application/json", json);
}

// POST /api/led/on
void handleLedOn() {
  setCorsHeaders();
  // Active LOW: LOW turns built-in LED ON
  digitalWrite(LED_PIN, LOW);
  isLedOn = true;
  Serial.println("[LED] State changed to ON (GPIO2 LOW)");
  
  String json = "{\"status\":\"ok\",\"led\":\"ON\",\"message\":\"Built-in LED turned ON\"}";
  server.send(200, "application/json", json);
}

// POST /api/led/off
void handleLedOff() {
  setCorsHeaders();
  // Active LOW: HIGH turns built-in LED OFF
  digitalWrite(LED_PIN, HIGH);
  isLedOn = false;
  Serial.println("[LED] State changed to OFF (GPIO2 HIGH)");
  
  String json = "{\"status\":\"ok\",\"led\":\"OFF\",\"message\":\"Built-in LED turned OFF\"}";
  server.send(200, "application/json", json);
}

void handleNotFound() {
  setCorsHeaders();
  String message = "{\"status\":\"error\",\"message\":\"Endpoint not found: " + server.uri() + "\"}";
  server.send(404, "application/json", message);
}

void setup() {
  Serial.begin(115200);
  delay(100);
  Serial.println("\n--- NodeMCU LED Controller Starting ---");

  // Configure onboard LED pin as OUTPUT and initialize to OFF
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH); // OFF initially
  isLedOn = false;

  // Connect to Wi-Fi
  Serial.print("Connecting to Wi-Fi SSID: ");
  Serial.println(ssid);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 30) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n[Wi-Fi] Connected successfully!");
    Serial.print("[Wi-Fi] Assigned IP address: ");
    Serial.println(WiFi.localIP());

    // Start mDNS responder (http://nodemcu-led.local)
    if (MDNS.begin("nodemcu-led")) {
      Serial.println("[mDNS] Responder started: http://nodemcu-led.local");
      MDNS.addService("http", "tcp", 80);
    } else {
      Serial.println("[mDNS] Error setting up MDNS responder!");
    }
  } else {
    Serial.println("\n[Wi-Fi] Connection failed! Starting Fallback AP Mode...");
    WiFi.mode(WIFI_AP);
    WiFi.softAP("NodeMCU-LED-AP", "12345678");
    Serial.print("[AP] SoftAP IP: ");
    Serial.println(WiFi.softAPIP());
  }

  // Register HTTP Routes
  server.on("/api/status", HTTP_GET, handleStatus);
  server.on("/api/status", HTTP_OPTIONS, handleOptions);
  
  server.on("/api/led/on", HTTP_POST, handleLedOn);
  server.on("/api/led/on", HTTP_OPTIONS, handleOptions);
  
  server.on("/api/led/off", HTTP_POST, handleLedOff);
  server.on("/api/led/off", HTTP_OPTIONS, handleOptions);
  
  server.onNotFound(handleNotFound);

  server.begin();
  Serial.println("[HTTP] Web server listening on port 80");
}

void loop() {
  MDNS.update();
  server.handleClient();
}
"""

    val FLASHING_STEPS = listOf(
        "Open Arduino IDE (v2.x or v1.8.x).",
        "Go to File > Preferences, and add to Additional Boards Manager URLs: https://arduino.esp8266.com/stable/package_esp8266com_index.json",
        "Go to Tools > Board > Boards Manager, search for 'esp8266' and install the package.",
        "Select Tools > Board > ESP8266 Boards > NodeMCU 1.0 (ESP-12E Module).",
        "Set Upload Speed to 115200 (or 921600 for faster flashing) and CPU Frequency to 80 MHz.",
        "Enter your local Wi-Fi SSID and Password in lines 20-21 of the sketch.",
        "Connect the NodeMCU board via micro-USB, select the corresponding COM/tty port, and click Upload.",
        "Open the Serial Monitor (115200 baud) to note the assigned local IP (e.g. 192.168.1.150)."
    )
}
