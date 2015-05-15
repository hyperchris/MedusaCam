# MedusaCam

##Requirements
**Smartphone:** Android 4.0 or above  
**Developing Tool:**  Android Studio

##Function  
Take photos and videos with metadata

##Usage
- Turn on your GPS service and Internet access (Wifi or Cellular)
- Install & Open the app
- Click the button "Take Photo" 
- Take photo, then click "save" or "√" to return to MedusaCam 
	- *Keep phone stable when taking and saving photo*
- Take more photos if needed

##Metadata
The metadata will be stored under the path of "Environment.getExternalStorageDirectory() + /DCIM/Camera/MedusaCam_data". Specially, all photo's metadata is stored in a file called "Metadata.txt". The metadata of each video is stored in a unique file which has the name same as the video's file name.
**For photo:**
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

**For video:**
- start/end time
- timestamp (every 10ms) 
- location w/ accuracy 
- light
- angle of View
- magnetic field [3 values]
- bearing [3 values]
- Accelerometer [3 values]