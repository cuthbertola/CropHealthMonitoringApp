import os
import cv2
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split

# Define paths to datasets
plantdoc_path = "../datasets/PlantDoc-Dataset-master"
breizhcrops_path = "../datasets/BreizhCrops"

# Create lists to store data and labels
images = []
labels = []

# Load PlantDoc dataset
print("Loading PlantDoc dataset...")
for root, dirs, files in os.walk(plantdoc_path):
    for file in files:
        if file.endswith(".jpg") or file.endswith(".png"):
            img_path = os.path.join(root, file)
            img = cv2.imread(img_path)

            if img is not None:
                img = cv2.resize(img, (224, 224))  # Resize to 224x224
                images.append(img)
                labels.append(root.split(os.path.sep)[-2])  # Use folder name as label

# Load BreizhCrops dataset
print("Loading BreizhCrops dataset...")
for root, dirs, files in os.walk(breizhcrops_path):
    for file in files:
        if file.endswith(".jpg") or file.endswith(".png"):
            img_path = os.path.join(root, file)
            img = cv2.imread(img_path)

            if img is not None:
                img = cv2.resize(img, (224, 224))  # Resize to 224x224
                images.append(img)
                labels.append(root.split(os.path.sep)[-2])  # Use folder name as label

# Convert lists to numpy arrays
print("Converting lists to numpy arrays...")
images = np.array(images)
labels = np.array(labels)

# Split the dataset into training and validation sets
print("Splitting dataset into training and validation sets...")
X_train, X_val, y_train, y_val = train_test_split(images, labels, test_size=0.2, random_state=42)

# Save prepared data as numpy files
print("Saving data...")
np.save("../datasets/X_train.npy", X_train)
np.save("../datasets/X_val.npy", X_val)
np.save("../datasets/y_train.npy", y_train)
np.save("../datasets/y_val.npy", y_val)

print("Data preparation completed successfully!")
"" 
