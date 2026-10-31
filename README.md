# VEIL (Wearable Emergency Identification & Link)

A native Android phone companion app for the **ESP32 emergency wristband**. When the wristband button is pressed, it broadcasts a Bluetooth Low Energy (BLE) alert to the phone. The VEIL app immediately performs a full-screen red emergency takeover, acquires high-accuracy GPS coordinates, sounds an emergency siren and pulsing haptics, and provides 1-tap SMS, Call, and WhatsApp dispatch to emergency contacts.

---

## 🛡️ Style & Design System
- **Professional Safety-Tech Product**: Built with Jetpack Compose & Material 3.
- **Color System**: Green (`#10B981`) indicates normal/protected state; Red (`#EF4444`) is reserved **strictly** for emergencies.
- **Line Icons & High-Density Telemetry**: Clean typography, 48dp+ accessibility touch targets, dark mode by default following system settings.

---

## 📡 BLE GATT Contract (ESP32 Firmware Match)
- **Scan Filter**: Device name starting with `VEIL` (e.g., `VEIL-Wristband-E8`).
- **Emergency Service UUID**: `7e1a0001-5645-494c-0000-000000000001`
- **Alert Characteristic UUID**: `7e1a0002-5645-494c-0000-000000000001` (`NOTIFY`)
  - First byte `0x01` = Emergency Alert Triggered.
  - First byte `0x00` = Alert Cleared.
- **Battery Service (Optional)**: Standard GATT `0x180F` / Characteristic `0x2A19` (`READ`/`NOTIFY`).
- **Heart Rate Service (Optional)**: Standard GATT `0x180D` / Characteristic `0x2A37` (`NOTIFY`).
  - *Strict Medical Integrity*: VEIL only renders heart rate data if genuine hardware streaming is detected. Never fabricates biometric data.

---

## 📱 Navigation & Screens
1. **Home**:
   - Animated status ring ("Protected" in green or "Not connected" in slate).
   - Telemetry cards: Device connection, System status, Battery %, Last alert time.
   - **Connect Wristband** button (scans for `VEIL*` and auto-reconnects).
   - **Send Test Alert** button (executes the exact same emergency flow as the hardware button).
2. **Emergency Takeover**:
   - Full-screen red takeover triggered across all tabs on button press or test alert.
   - Live GPS coordinates (lat/lng, accuracy) with **Open in Maps** button.
   - Continuous audio siren tone and pulsing haptics until dismissed.
   - **Send SMS to all contacts** button pre-filled with:  
     `"EMERGENCY! <user name> needs help. VEIL alert at <time>. Location: https://maps.google.com/?q=<lat>,<lng>"`
   - Per-contact rows with **Direct Call** (`tel:`) and **WhatsApp** (`https://wa.me/...`) actions.
   - **"I'm safe, dismiss alert"** confirmation button.
3. **Health**:
   - Live pulse BPM and smooth Canvas trend line telemetry chart (min/avg/max).
   - Clean empty state when no heart-rate sensor is attached.
4. **History**:
   - Room-persisted incident log with timestamps, trigger sources, and geo-coordinates.
   - 1-tap to open location in Google Maps.
   - "Clear history" action.
5. **Settings**:
   - User profile name editor (injected into distress SMS).
   - Emergency contacts management: Add, edit, and delete contacts.
   - System permissions status checker (Bluetooth, Location, SMS).
   - VEIL device BLE UUID specifications with 1-tap copy.
   - Simulated BLE mode toggle for emulator and testing.

---

## 🚀 Building & Running

### 1. Build Native Android APK (Local / Studio)
```bash
# Compile and build the debug APK
gradle assembleDebug

# Install on a connected physical device or emulator
gradle installDebug
```
The output APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Expo / EAS Cloud Build Command
If building via EAS Cloud:
```bash
# Build standalone Android APK with EAS
eas build --platform android --profile preview
```

---

## 🔒 Permissions & Security
- `BLUETOOTH_SCAN` & `BLUETOOTH_CONNECT` (Android 12+)
- `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION` (Emergency GPS location & BLE scanning)
- `VIBRATE` (Emergency alert haptics)
- `SEND_SMS` & `CALL_PHONE` (Emergency contact dispatch)
- `FLAG_KEEP_SCREEN_ON` enabled automatically while connected or during active distress alerts.
