**Crop Health Monitoring App**

A mobile application that leverages machine learning and multispectral imaging to provide real-time crop health diagnostics and actionable recommendations for farmers. This project was developed as part of a dissertation to address the challenges of early plant disease detection in modern agriculture.

**Overview**

_**Objective:**_

Develop a user-friendly mobile solution for real-time crop disease detection using advanced image processing and machine learning techniques.

_**Key Technologies:**_

Machine Learning: Convolutional Neural Network (CNN) using transfer learning (MobileNetV2)
Imaging: Multispectral imaging for enhanced disease detection
Mobile Development: Android app developed in Kotlin using Android Studio
Backend: Firebase for authentication, data storage, and real-time chat between farmers and experts

_**Datasets:**_

Utilizes the PlantDoc dataset along with supplementary images. Data augmentation and synthetic image generation were applied to overcome class imbalances.

**Features**

  _**Real-Time Detection:**_
  
  On-device inference using an optimized TensorFlow Lite model for rapid disease classification.
  
  _**User-Friendly Interface:**_
  
  Simple image capture and result display designed for farmers with varied technical skills.
  
  _**Firebase Integration:**_
  
  Secure user authentication and a chat system that connects farmers with agricultural experts.
  
  _**Data Augmentation:**_
  
  Enhanced model robustness through techniques such as rotation, flipping, and GAN-based synthetic image generation.


**Installation & Setup**
 _ **Clone the Repository:**_
  
  git clone https://github.com/cuthbertola/CropHealthMonitoringApp.git
  cd CropHealthMonitoringApp
  
  _**Open in Android Studio:**_
  
  Import the project and let Gradle sync.
  
  _**Configure Firebase:**_
  
    Create a Firebase project.
    
    Download the google-services.json file and place it in the appropriate module folder.
  
  _**Build & Run:**_
  
  Build the project in Android Studio and deploy it on an Android device or emulator.

**Usage**
  _**Login/Signup:**_
  
  Use Firebase Authentication to sign in.
  
  _**Capture Image:**_
  
  Take or upload a photo of a crop leaf using the in-app camera.
  
  _**Receive Diagnosis:**_
  
  The app processes the image and displays the detected disease along with treatment recommendations.
  
  _**Expert Chat:**_
  
  Initiate real-time chats with experts for further advice if needed.


  __**Results & Future Work**__
  
  _**Performance:**_
  
  The CNN model achieves approximately 72% accuracy on TensorFlow Lite, with acceptable inference times on various Android devices.
  
  _**Future Enhancements:**_
  
  Explore alternative model architectures to improve accuracy
  
  Expand the range of detectable diseases
  
  Enhance image pre-processing techniques
  
  Integrate additional data sources (e.g., IoT sensors)
