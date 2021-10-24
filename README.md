
<h2> Required Software / Technology Stack </h2>
Android Device (APK >= 30 as of 22/10/2021, device is optional for development) <br>
Android Studio and SDK tools <br>
Google Colabotary <br>
Deep learning model (available here) <br>
Unblind app <br>

## How To Deploy 
### Initial setup 
1. Download or clone the Unblind App from the github repository (the repository is large and requires LFS support). <br>
   git clone https://github.com/gwenllian1/Unblind.git <br>
3. Download Android studio [here](https://developer.android.com/studio?utm_source=android-studio) <br>
4. Open the Unblind app with Android studio. From here work on the app can be done <br>

## Running the app on an emulator 
1. Click the drop down menu left of the play button and open the AVD manager. <br>
  ![avd manager](avd-IMAGE.png) <br>
2.Click create virtual device to create a new device (if you already have a device, that is fine as long as API level is at least 30). Pixel devices are recommended. <br>
3.Select the emulator from the drop down menu and click the start button to start the app

## Running the app on an Android device 
1.Click about phone -> software information -> build number to turn on development mode(this can be slightly different for different devices). <br>
2.Go to Android studio and choose the Android device from the dropdown menu <br>
3.Click play

## Summary of Helpful Information
To assist new developers, here are links to the [model training code](https://colab.research.google.com/drive/14gCMA5bSni_I-oPFmjfWeRH1C8niefqQ?usp=sharing) and [data](https://drive.google.com/file/d/1pXaT1d5mQPsDLq_cJiskhLGqaRfBAEn5/view?usp=sharing), [a copy of the model](https://drive.google.com/file/d/1H1PdcZtZqVYZweIju2ZKQbepTMnK5pTj/view?usp=sharing), and [the model training history](https://docs.google.com/document/d/1XJWlhYr0uukgAzRN1SzruF9j-9K1R9ckO1_zFcMYZZE/edit?usp=sharing). 

This repository is unmonitored so any future work should be done by making a fork.

**The current state of Unblind is as follows:**
*Works on the most recent version of Android (API >= 30)
*By taking screenshots, unlabelled icons can be extracted and fed to the Tensorflow Lite deep learning model
*Implements batch processing of all icons on the screen to improve response time
*Caches labels and their corresponding icons to save processing time
*Supports pre-processing of unlabelled icons 
*Provides Spanish and Chinese translations of the supported icon labels 

**A summary of the class interactions is as follows:**

**Some recommendations for further improvements include:**
*Considering the context of the icon in addition to the image when generating labels
*Continually training the model on icons it gets incorrect
*Extend support for image recognition from only button icons to general photos and images

 ##Versioning Strategy
The initial development of Unblind used V1-V3 indicating the application state at the end of each project increment (PI). We chose not to follow the major.minor.patch convention because tags were made at 6 week increments and major changes were made each time.

For all future work,  it is recommended that the traditional semantic versioning strategy (major.minor.patch) should be used. 
Where
Major = incompatible API changes
Minor = added functionality that is backwards compatible
Patch = small changes that are backwards compatible.

**Please make a fork of the repository prior to making any changes. **

## Pull Request Strategy
After the 24/10/2021, this repository will be un-monitored so it is recommended that contributors should make a fork and perform their pull requests there using traditional DevOps practices. It should be noted that our developers found it difficult to automate a CI/CD pipeline because of the difficulty of mocking and writing unit tests for the many different Android interfaces that were used - especially services. It is recommended that future developers attempt automation because it is best practice, however if that fails we found that a manual pipeline was sufficient. 

If it is necessary to work in this repository, all feature branches must be made from the main branch, and must be named after the feature being developed. Merging between non-main branches can be done freely, and branches should be deleted when they are no longer needed. 

To merge to main, a pull request is required. The pull request must include code reviews, documented black box testing of all new features, and integration testing. Permission from two code owners is required before the branch can be merged (this is unlikely to occur because as previously mentioned, this repository is unmonitored). 

An in-depth explanation of Git usage for this project (including how to make pull requests) is available [here](https://drive.google.com/file/d/1OusG6dZiFDWpPdb3RtgrlmSHrn46ndlk/view?usp=sharing).

## Licencing
The team has resolved that the UnBlind app will be licensed under GPLv3. This replaces our decision made earlier in the project to use the MIT license. While the MIT license permits creating derivative works with proprietary licenses, GPLv3 forbids this with the intention to keep the software free and open source. The fundamental freedoms that the GPLv3 aims to uphold are:
the freedom to use the software for any purpose,
the freedom to change the software to suit your needs,
the freedom to share the software with your friends and neighbors, and
the freedom to share the changes you make (Free Software Foundation, 2014).
Since our project aims to support the accessibility of Android phones for disabled users, we are obliged to ensure that UnBlind remains free and open for anyone to use or improve.

We are choosing to keep this software free of charge to use, with no ads or premium features. This aligns with our motivation for creating the UnBlind project which is to make Android devices accessible for its users. The software can be extended and incorporated into new projects, as long as those projects also have GPLv3. This means that anyone is free to fork the project, and even charge money for it - as long as the source code is always available and it meets the fundamental four freedoms outlined above.

