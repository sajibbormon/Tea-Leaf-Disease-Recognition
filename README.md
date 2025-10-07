<h1 align="center" style="color:#2e8b57; font-family: 'Segoe UI', sans-serif;">
🍃 Tea Leaf Disease Recognition using Android Application 🍃
</h1>

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/home.jpg" width="280" style="border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.3);" alt="Home Screen"/>
</p>

<h3 align="center" style="color:#444;">
Empowering Tea Growers through AI-based Disease Detection 📱🌱
</h3>

---

## 🌿 Overview

The **Tea Leaf Disease Recognition App** is an Android-based application that uses **deep learning (TensorFlow Lite)** to automatically identify tea leaf diseases from captured or gallery images.  
It helps farmers and researchers monitor plant health efficiently and visualize statistical patterns over time.

---

## ⚙️ How the Application Works

### 🏠 **1. Home Screen**
- The main screen allows users to either **capture an image** using the camera or **select an existing image** from the gallery.
- After image selection, options to **Analyze Disease** or **Remove** appear.

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/home.jpg" width="280" style="margin:10px; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.2);" alt="Home Activity"/>
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/selectImage.jpg" width="280" style="margin:10px; border-radius:8px; box-shadow:0 0 10px rgba(0,0,0,0.2);" alt="Select Image"/>
</p>

---

### 🔍 **2. Disease Analysis**
- When the user clicks on **Analyze Disease**, the app processes the image using a trained F-TeaNet model (`.tflite`) and displays:
  - The **predicted disease class**
  - The **confidence percentage**
  - A short **description and prevention guide**

- A **"Know More"** button redirects to Google Search for detailed information on the identified disease.

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/result.jpg" width="280" style="border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.3);" alt="Result Screen"/>
</p>

---

### 📊 **3. Statistics and Monitoring**
The **Statistics Activity** helps users visualize disease occurrences over different time periods.

#### 📅 **a) Today’s Statistics**
- Shows only today’s predictions and results.

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/today.jpg" width="280" style="border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.3);" alt="Today's Data"/>
</p>

#### 📆 **b) Weekly Statistics**
- Displays data from **Monday to Sunday** of the selected week.

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/week.jpg" width="280" style="border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.3);" alt="Weekly Statistics"/>
</p>

#### 🗓️ **c) Monthly Statistics**
- Displays total analysis data for the selected month.

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/month.jpg" width="280" style="border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.3);" alt="Monthly Statistics"/>
</p>

#### 📈 **d) Custom Date Range**
- Users can select a specific date and view data for **any previous number of days**.

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/prev.jpg" width="280" style="border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.3);" alt="Custom Range"/>
</p>

#### 🥧 **e) Pie Chart Visualization**
- The same data can also be visualized as a **Pie Chart**, giving an instant overview of disease distribution.

<p align="center">
  <img src="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/blob/main/Assists/pie.jpg" width="280" style="border-radius:10px; box-shadow:0 0 10px rgba(0,0,0,0.3);" alt="Pie Chart View"/>
</p>

---

## 🧠 **Behind the Scenes**

- The model is trained on tea leaf disease images using **TensorFlow** and deployed with **TensorFlow Lite** for Android.
- The app uses **Room Database** to store local statistics for daily, weekly, and monthly monitoring.
- Disease categories include:
  - 🍂 *Tea Leaf Blight*  
  - 🌿 *Tea Red Scab*  
  - 🍁 *Tea Red Leaf Spot*  
  - ✅ *Healthy*  

---

## 📥 **Download and Test the App**

You can download the **APK file** and **test images** directly from the following folder:

👉 [**Download APK And Test Image**](https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/tree/main/Download%20APK)

Once downloaded:
1. Install the APK on your Android device.  
2. Open the app and select a leaf image from the gallery or take one using the camera.  
3. Click on **Analyze Disease** to view instant results.  
4. Explore statistics for today, week, or month to monitor performance.

<p align="center">
  <a href="https://github.com/sajibbormon/Tea-Leaf-Disease-Recognition/tree/main/Download%20APK">
    <img src="https://img.shields.io/badge/⬇️%20Download%20Now-APK%20Available-brightgreen?style=for-the-badge&logo=android" alt="Download APK"/>
  </a>
</p>

---

## 🧩 **Key Features**
✅ Offline Image Classification (No Internet Needed)  
✅ Real-time Confidence Visualization  
✅ Weekly and Monthly Data Tracking  
✅ Interactive Pie and Bar Charts  
✅ Custom Date Range Selection  
✅ Local Data Storage via Room DB  
✅ Simple and Intuitive UI  

---

<h2 align="center" style="color:#2e8b57;">🌱 Developed with Passion and Precision 🌱</h2>

<p align="center" style="font-size:16px; color:#555;">
Built to support tea farmers and researchers in early disease detection and sustainable plantation management.
</p>

<p align="center">
  <strong>⭐ If you like this project, give it a star on GitHub!</strong>
</p>

---
