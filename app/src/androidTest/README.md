BDD With Cucumber, Espresso and Kotlin
====================

Adding dependencies to build.gradle
---------------------
```Gradle
testCompile "junit:junit:4.12"
androidTestCompile "com.android.support:support-annotations:25.4.0"
androidTestCompile "com.android.support.test.espresso:espresso-core:2.2.2"
androidTestCompile "info.cukes:cucumber-android:1.2.5"
```

Adding source sets to build.gradle
---------------------
```Gradle
sourceSets {
    androidTest {
        assets.srcDirs = ['src/androidTest/assets']
    }
}
```

Configuring test runner in build.gradle
---------------------
```Gradle
testApplicationId "com.apkupdater.test"
testInstrumentationRunner "com.apkupdater.test.CucumberInstrumentation"
```

Adding features to src/androidTest/assets/features
---------------------
```Gherkin
Feature: Settings Feature

Scenario: Go to settings
    Given I start the app
    When I go to settings
    Then I see the settings
```

Implementing CucumberInstrumentation (Java)
---------------------
This can also be implemented with Kotlin.
```Java
public class CucumberInstrumentation extends MonitoringInstrumentation {
	private final CucumberInstrumentationCore mInstrumentationCore = new CucumberInstrumentationCore(this);
	
	@Override
	public void onCreate(Bundle arguments) {
		super.onCreate(arguments);
		mInstrumentationCore.create(arguments);
		start();
	}

	@Override
	public void onStart() {
		super.onStart();
		waitForIdleSync();
		mInstrumentationCore.start();
	}
}
```

Adding steps for our features (Kotlin)
---------------------
```Kotlin
@CucumberOptions(features = arrayOf("features/Settings.feature"))
class SettingsSteps: TestCase() 
    // Implement
}
```