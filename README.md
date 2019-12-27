EECS 441
Team name: seniorz2020

# WalkLens

### Building and running Android application
* All of the code for this project is contained in this top-level directory.
* Clone the seniorz2020 repository
* Open the project in Android Studio from this (the "WalkLens") top-level directory.
* In app/res/values, make an XML values file called *google_maps_api.xml*
* Delete the starter code in the file
* Copy and paste in the following code into the new file

    ```
    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">API_KEY_HERE</string>
    </resources>
    ```
* If you have access to our GCP project, [use this link to grab the
API key](https://console.cloud.google.com/apis/credentials?folder=&organizationId=&project=walklens)
* Replace *API_KEY_HERE* with the API key from GCP
* Build and run the app on an emulator or USB debugging device!

### Folder/File Structure


app/src/main/java/fonte/com/walklens/data:

- AppDatabase.kt
- MainRepository.kt
- SeedDatabaseWorker.kt
- UserSettings.kt
- UserSettingsDao.kt

app/src/main/java/fonte/com/walklens/ui/main:

- MapsFragment.kt
- MapsViewModel.kt
- MapsViewModelFactory.kt
- SettingsFragment.kt
- SettingsViewModel.kt
- SettingsViewModelFactory.kt


app/src/main/java/fonte/com/walklens/util:

- Constants.kt
- InjectorUtils.kt
- ObjectUtils.kt

app/src/main/java/fonte/com/walklens:

- MainActivity.kt
- MainActivityViewModel.kt
- MainActivityViewModelFactory.kt

app:

- build.gradle
- .gitignore

.:

- build.gradle
- .gitignore

app/src/debug/res/values:

- google_maps_api.xml

app/src/main:

- AndroidManifest.xml

app/src/main/res/drawable

- ic_laucher_background.xml
- ic_laucher_foreground.xml
- ic_settings_white_24dp.xml
- ic_warning_white_24dp.xml
- walklens_logo.png
- walklens_logo_cropped.png

app/src/main/res/layout

- main_activity.xml
- maps_fragment.xml
- settings_fragment.xml

app/src/main/res/menu

- main_menu.xml

app/src/main/res/navigation

- nav_graph.xml

app/src/main/res/values

- colors.xml
- strings.xml
- styles.xml

##### Note, these are just the important files, for readability. The full file structure is extremely cumbersome due to the number of Android-generated files. Thank you!
