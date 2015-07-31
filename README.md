# MedusaCam  
**Version:** 1.2    
**Update:** Fix the bug that sensors are unregistered when taking video

##Requirements
**Smartphone:** Android 4.0 or above  
**Developing Tool:**  Android Studio 1.0.0 or above

##Function  
Take photos and videos with metadata

##Usage
- Turn on your GPS service and Internet access (Wifi or Cellular)
- Install & Open the app
- Click the button "Take Photo"/"Take Video"
- Take photo or video, then click "save" or "√" to return to MedusaCam 
	- *Keep phone stable when saving photo/video*
- Take more photos / videos if needed

##Metadata 
The metadata will be stored under the path of "Environment.getExternalStorageDirectory() + /DCIM/Camera/MedusaCam_data". Specially, all photo's metadata is stored in a file called "Metadata.txt". The metadata of each video is stored in a unique file which has the name same as the video's file name.  

**Metadata of photo:**
- time
- indoor / outdoor (manually select)
- Blurry or not
- # of faces
- # of cars (manually select, but can be done with code)
- location w/ accuracy 
- light
- angle of view
- magnetic field [3 values]
- bearing [3 values]
- Accelerometer [3 values]

**Metadata of video:**
- start/end time
- timestamp (every 10ms) 
- location w/ accuracy 
- light
- angle of View
- magnetic field [3 values]
- bearing [3 values]
- Accelerometer [3 values]

##Notes
This version of MedusaCam cannot detect the number of vehicles automatically due to the flawed image dataset. If you want to integrate the auto-detection function. Please refer to my code for [car detection on Android](https://github.com/hyperchris/CarDetection).
