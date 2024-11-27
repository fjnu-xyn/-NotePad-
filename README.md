# -NotePad-
本项目是基于源码：https://github.com/llfjfz/NotePad 的项目拓展。<br>把源代码打开后，首先需要配置合适的gradle和sdk：<br>
```
classpath 'com.android.tools.build:gradle:8.6.0'    //build.gradle(project)
```
```
plugins {                                       //build.gradle(app)
    id 'com.android.application'
}
android {
    compileSdk 33 // 更新到较新的编译 SDK 版本

    defaultConfig {
        applicationId "com.example.android.notepad"
        minSdkVersion 21 // 推荐 minSdkVersion 至少为 21，以支持较新的 API
        targetSdkVersion 33 // 更新到较新的目标 SDK 版本

        testApplicationId "com.example.android.notepad.tests"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner" // 使用 androidx 的测试运行器
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro' // 更新 ProGuard 配置文件名
        }
    }
    namespace 'com.example.android.notepad'
```
```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip        //gradle-wrapper.properties
```
说明：源代码打开后是一个简单的记事本项目，可以实现新建笔记，删除笔记，修改笔记，修改标题以及复制粘贴的功能，界面如下图所示<br> ![]
