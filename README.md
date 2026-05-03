# 🍽️ Dishfy

![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Kotlin-blue)
![Architecture](https://img.shields.io/badge/architecture-MVVM-purple)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-orange)
![Backend](https://img.shields.io/badge/backend-Firebase-yellow)
![API](https://img.shields.io/badge/API-Spoonacular%20%7C%20Edamam-red)
![Status](https://img.shields.io/badge/status-in%20development-pink)

Dishfy is a mobile application that helps users discover recipes based on the ingredients they already have at home.
The app focuses on simplifying everyday cooking.

👩‍💻Developed by **Suellyn Gomes**

---

## 🎯 Problem

Many people face daily difficulties when deciding what to cook, especially when:

- They have limited ingredients at home
- They want to avoid unnecessary expenses
- They lack ideas for quick meals
- They end up wasting food

---

## 💡 Solution

Dishfy solves this problem by allowing users to input available ingredients and receive recipe suggestions based on:

- Ingredient availability
- Number of missing ingredients
- Simplicity of preparation
  
---

## 🚀 Main Features

### 🔍 Recipe Search by Ingredients
Users select ingredients they have, and the app fetches recipes using external APIs.

### 🧊 Empty Fridge Mode
Suggests recipes with:
- Few ingredients
- Minimal requirements
- Quick preparation

### 🛒 Automatic Shopping List
Generates a list of missing ingredients for selected recipes.

### 🔐 Authentication
User login and registration using Firebase Authentication.

### ❤️ Favorites
Users can save preferred recipes.

### 📜 Search History
Stores previous made recipes to improve user experience.

### 🔔 Smart Notifications
Using Firebase Cloud Messaging to:
- Suggest recipes
- Remind users to cook
- Recommend meals based on previous behavior

---

## 🧠 How it works

1. The user selects the ingredients they have
2. Dishfy fetches recipes using external APIs
3. The system filters recipes based on:
   - Available ingredients
   - Missing ingredients
4. The app suggests the best recipes for the user

---

## 📱 Use of Mobile Platform Features

Dishfy makes use of key mobile platform features:

- 📶 Internet access (API consumption)
- 🔔 Push notifications (Firebase Cloud Messaging)
- 📱 Persistent user data (Firestore)
- 📷 *(Future)* Camera for ingredient scanning

---

## 🌐 External Integrations

- **Spoonacular API** → Recipe search and cost estimation
- **Edamam API** → Nutritional data
- **Firebase**:
  - Authentication
  - Firestore Database
  - Cloud Messaging

---

## 🏗️ Architecture

The application follows the **MVVM (Model-View-ViewModel)** architecture:

```text
UI (Compose)
   ↓
ViewModel
   ↓
Use Cases (Business Logic)
   ↓
Repository
   ↓
API / Firebase
```
