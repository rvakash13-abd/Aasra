<div align="center">

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&height=260&color=0:012A2D,25:014D40,60:0B6E4F,100:08D9A5&text=AASRA&fontSize=68&fontColor=E8FFF7&fontAlignY=38&animation=fadeIn&desc=Flood%20Relief%20Navigation%20%26%20Shelter%20Occupancy%20Platform&descAlignY=60&descColor=B7FFE8&descSize=17"/>

</div>

<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=Share+Tech+Mono&weight=700&size=19&pause=1500&color=4FC3F7&center=true&vCenter=true&width=750&lines=AASRA+MILEGA%2C+SURAKSHA+MILEGA;REAL-TIME+SHELTER+%26+OCCUPANCY+TRACKING;OFFLINE-FIRST+SAFE+NAVIGATION;MULTILINGUAL+%2B+VOICE+ACCESSIBLE"/>

</div>

<div align="center">

<img src="https://img.shields.io/badge/Status-Prototype_Built-4FC3F7?style=for-the-badge"/>
<img src="https://img.shields.io/badge/Platform-Android-1580E4?style=for-the-badge"/>
<img src="https://img.shields.io/badge/License-MIT-0D3B66?style=for-the-badge"/>

</div>

<div align="center">

<a href="https://github.com/rvakash13-abd/Aasra">
<img src="https://img.shields.io/badge/GitHub-0D3B66?style=for-the-badge&logo=github&logoColor=4FC3F7"/>
</a>
<a href="mailto:your-email@gmail.com">
<img src="https://img.shields.io/badge/Contact-Gmail-1580E4?style=for-the-badge&logo=gmail&logoColor=E8F4FF"/>
</a>

</div>

<br>

<div align="center">

```text
        ┌───────────────────────┐
        │      ⚠ FLOOD ALERT    │
        │   ┌───────────────┐   │   █████╗  █████╗ ███████╗██████╗  █████╗
        │   │  Shelter: A4  │   │  ██╔══██╗██╔══██╗██╔════╝██╔══██╗██╔══██╗
        │   │  Occ: 62/100  │   │  ███████║███████║███████╗██████╔╝███████║
        │   │  Food: ✓      │   │  ██╔══██║██╔══██║╚════██║██╔══██╗██╔══██║
        │   └───────────────┘   │  ██║  ██║██║  ██║███████║██║  ██║██║  ██║
        │   [ SAFE ROUTE ▶ ]    │   ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝
        │   [ I AM SAFE ✓ ]     │
        │   [ SOS / SMS ]       │      A A S R A
        └───────────────────────┘   Aasra Milega, Suraksha Milega
             AASRA  •  24×7
```

</div>


</div>

---

## 🌊 About the Project

**AASRA** ("Aasra Milega, Suraksha Milega") is an Android-based **Flood Relief Navigation and Shelter Occupancy Platform** built to convert a disaster alert into an actionable response. Instead of just warning people that a flood is coming, AASRA tells them exactly **where the nearest safe shelter is, whether it has space and food, and which route is safe** to get there — even when connectivity drops.

> **Elevator Pitch:** AASRA is a disaster-response Android app that turns a flood alert into a clear next step — recommending the nearest suitable shelter, showing real-time occupancy and resources, guiding users along a safe route, and staying usable offline via local caching and SMS fallback.

The system is built around two pillars: **Citizen Response** (direct, actionable instructions for people in danger) and a **Digital Platform** (real-time shelter, occupancy, and navigation support for citizens, volunteers, and authorities).

---

## 📌 Table of Contents

- [Key Features](#-key-features)
- [How It Works](#-how-it-works)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Resilience: Online vs Offline](#-resilience-online-vs-offline)
- [Use Cases](#-use-cases)
- [Project Status](#-project-status)
- [Getting Started](#-getting-started)
- [Future Scope](#-future-scope)
- [Contributing](#-contributing)
- [License](#-license)
- [Contact](#-contact)

---

## 🚀 Key Features

| Feature | Description |
|---|---|
| 🏠 **Shelter Finder** | Recommends nearby shelters based on distance, occupancy, food availability, and safety |
| 📊 **Real-Time Occupancy & Resources** | Live shelter capacity and resource tracking via Firestore |
| 🧭 **Safe Route Navigation** | Google Maps SDK + Directions API routing that avoids known flood hazards |
| 🆘 **Rescue Request Queue** | Priority-scored rescue requests for authorities and volunteers |
| ✅ **"I Am Safe" Status** | Lets users broadcast their safety status to family/authorities |
| 📡 **Offline SMS Fallback** | Critical communication continues over SMS when internet is unavailable |
| 🗣️ **Multilingual + Voice Support** | Regional Indian languages with voice interaction for low-literacy reach |
| 🛡️ **Authority/Volunteer Dashboard** | Shelter managers and volunteers verify and update shelter data |
| 🌦️ **Flood Risk Data** | Weather/flood-risk signals via Open-Meteo integration |

---

## ⚙️ How It Works

```text
[1] Flood alert is issued in the user's area
[2] User opens AASRA and shares location (or uses voice interaction)
[3] App recommends shelters based on distance, occupancy, food, safety, family needs
[4] User selects a shelter and receives safe-route navigation
[5] If internet drops → cached shelter/route data is still accessible
[6] Critical updates fall back to SMS where cellular service is available
[7] User reaches shelter → status and resource updates sync back to the platform
```

**Complete flow:** Alert → User Location → Shelter Recommendation → Safe Route → Shelter → Emergency/Resource Updates

---

## 🏗️ System Architecture

```text
User Layer (Android App: Kotlin + Jetpack Compose)
          │
          ▼
Application Layer ──► Shelter Recommendation, Emergency Requests, Safe-Route Logic
          │
          ▼
Data Layer ──► Shelter / Occupancy / Resource Data (Cloud + Local Cache)
          │
          ▼
External Integrations ──► Google Maps, Disaster Info (SACHET/NDMA), SMS Gateway
```

The architecture combines **cloud-based coordination** with **local device-level resilience**, so the app keeps working when the network doesn't.

---

## 🧰 Tech Stack

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-0D3B66?style=for-the-badge)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-0D3B66?style=for-the-badge)
![Firebase](https://img.shields.io/badge/Firebase-0D3B66?style=for-the-badge)
![Google Maps](https://img.shields.io/badge/Google_Maps_SDK-0D3B66?style=for-the-badge)
![Retrofit](https://img.shields.io/badge/Retrofit-0D3B66?style=for-the-badge)
![Coroutines](https://img.shields.io/badge/Kotlin_Coroutines-0D3B66?style=for-the-badge)
![SMS](https://img.shields.io/badge/SMS_Fallback-0D3B66?style=for-the-badge)
![Open--Meteo](https://img.shields.io/badge/Open--Meteo-0D3B66?style=for-the-badge)

</div>

- **Frontend:** Kotlin, Jetpack Compose, Jetpack Navigation, Android Speech Recognition (voice input)
- **Backend:** Firebase Authentication, Firestore, Firebase Cloud Messaging
- **Maps & Location:** Google Maps SDK, Fused Location Provider, Directions API
- **Networking:** Retrofit, Gson, OkHttp, Kotlin Coroutines
- **Offline Support:** Local caching, SharedPreferences
- **Communication:** SMS fallback for critical alerts without internet
- **Weather/Flood Risk:** Open-Meteo API
- **Accessibility:** Multi-language support with voice interaction

*(Update this section with any additional libraries used in your final implementation.)*

---

## 🛡️ Resilience: Online vs Offline

| Component | 🌐 Online | 📡 Offline / Low Connectivity |
|---|---|---|
| Shelter Data | Live Firestore sync | Locally cached last-known data |
| Navigation | Real-time Directions API | Cached route to last-selected shelter |
| Emergency Alerts | Firebase Cloud Messaging | SMS fallback |
| Status Updates | Instant sync | Queued and synced when reconnected |
| Data Verification | Real-time by shelter managers | Timestamped, flagged as stale |

---

## 🏥 Use Cases

<div align="center">

| 🌊 Flood-Affected Citizens | 🏫 Shelter Managers | 🚨 Rescue Volunteers | 🏛️ Disaster Authorities |
| :---: | :---: | :---: | :---: |
| Find & navigate to a safe shelter | Update occupancy & resources | Respond to priority rescue requests | Coordinate response with live data |

</div>

---

## 📍 Project Status

```text
[DONE]         Core Android app (Kotlin + Jetpack Compose)
[DONE]         Shelter finder with occupancy/resource logic
[DONE]         Safe-route navigation via Google Maps
[DONE]         Rescue request queue with priority scoring
[DONE]         "I Am Safe" status broadcast
[DONE]         Offline caching + SMS fallback prototype
[IN PROGRESS]  Authority/volunteer dashboard
[PLANNED]      Expanded multilingual + voice coverage
[PLANNED]      Field pilot / deployment testing
```

*(Adjust to reflect your actual current progress.)*

---

## 🛠️ Getting Started

```bash
# Clone the repository
git clone https://github.com/rvakash13-abd/Aasra.git
cd Aasra

# Open in Android Studio and let Gradle sync

# Add your Firebase configuration
# Place google-services.json in the /app directory

# Add your Google Maps API key
# In local.properties: MAPS_API_KEY=your_key_here

# Build and run on an emulator or device
```

---

## 🔮 Future Scope

- Predictive shelter demand using historical flood/crowd data
- Integration with NDMA/SACHET alert feeds for live disaster data
- Drone/volunteer-reported shelter verification
- Expanded regional language and voice coverage
- Wearable/feature-phone SMS-only companion mode

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 📬 Contact

<div align="center">

<a href="https://github.com/rvakash13-abd/Aasra">
<img src="https://img.shields.io/badge/GITHUB-0D3B66?style=for-the-badge&logo=github&logoColor=4FC3F7"/>
</a>
<a href="mailto:your-email@gmail.com">
<img src="https://img.shields.io/badge/EMAIL-1580E4?style=for-the-badge&logo=gmail&logoColor=E8F4FF"/>
</a>

<br><br>

**rvakash13-abd** — swap in your actual email above

If you find this project useful, consider giving it a ⭐ on GitHub!

</div>

<div align="center">

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=0:0A1F3D,50:0D3B66,100:4FC3F7&height=120&section=footer"/>

</div>
