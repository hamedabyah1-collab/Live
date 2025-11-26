# دليل المطور - تطبيق كاشف الأشياء الذكي

## نظرة عامة تقنية

تطبيق Android متطور مبني بلغة Kotlin يستخدم أحدث تقنيات الذكاء الاصطناعي للتعرف على الأشياء من خلال الكاميرا.

## البنية التقنية

### اللغة والأدوات
- **اللغة**: Kotlin 1.9.20
- **الحد الأدنى لـ SDK**: API 24 (Android 7.0)
- **الإصدار المستهدف**: API 34 (Android 14)
- **أداة البناء**: Gradle 8.1.4

### المكتبات الرئيسية

#### Core Android
```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
androidx.constraintlayout:constraintlayout:2.1.4
```

#### Lifecycle & ViewModel
```gradle
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
androidx.lifecycle:lifecycle-livedata-ktx:2.7.0
```

#### CameraX
```gradle
androidx.camera:camera-core:1.3.1
androidx.camera:camera-camera2:1.3.1
androidx.camera:camera-lifecycle:1.3.1
androidx.camera:camera-view:1.3.1
```

#### Machine Learning
```gradle
com.google.mlkit:object-detection:17.0.1
com.google.mlkit:image-labeling:17.0.8
org.tensorflow:tensorflow-lite:2.14.0
```

#### Networking
```gradle
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.okhttp3:okhttp:4.12.0
```

#### Database
```gradle
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
```

#### Image Loading
```gradle
io.coil-kt:coil:2.5.0
```

#### Coroutines
```gradle
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
```

## هيكل المشروع

```
AIObjectDetector/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/aidetector/app/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── CameraActivity.kt
│   │   │   │   ├── ResultActivity.kt
│   │   │   │   ├── HistoryActivity.kt
│   │   │   │   ├── SettingsActivity.kt
│   │   │   │   ├── db/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── DetectionDao.kt
│   │   │   │   │   └── DetectionEntity.kt
│   │   │   │   ├── ml/
│   │   │   │   │   ├── AIAnalyzer.kt
│   │   │   │   │   └── GeminiAnalyzer.kt
│   │   │   │   └── models/
│   │   │   │       └── DetectionResult.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## المكونات الرئيسية

### 1. MainActivity
الشاشة الرئيسية للتطبيق.

**الوظائف**:
- عرض واجهة الترحيب
- طلب الأذونات
- التنقل إلى الشاشات الأخرى

### 2. CameraActivity
شاشة الكاميرا للتقاط الصور.

**الوظائف**:
- إدارة CameraX
- التقاط الصور
- تشغيل/إيقاف الفلاش
- تبديل الكاميرات

**التقنيات**:
- CameraX للتعامل مع الكاميرا
- Coroutines للعمليات غير المتزامنة

### 3. AIAnalyzer
محلل الذكاء الاصطناعي الرئيسي.

**الوظائف**:
- التعرف الأولي باستخدام ML Kit
- التحليل التفصيلي باستخدام Gemini AI
- معالجة الصور

**سير العمل**:
```
Bitmap → ML Kit Detection → Top Label → Gemini Analysis → DetectionResult
```

### 4. GeminiAnalyzer
محلل Gemini AI للمعلومات التفصيلية.

**الوظائف**:
- تحويل الصورة إلى Base64
- إرسال الطلب إلى Gemini API
- معالجة الاستجابة JSON
- إنشاء نتائج احتياطية

**API Endpoint**:
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent
```

### 5. AppDatabase
قاعدة بيانات Room للتخزين المحلي.

**الجداول**:
- `detections`: تخزين نتائج الكشف

**العمليات**:
- Insert, Update, Delete
- Query بـ Flow للتحديثات التلقائية

### 6. ResultActivity
شاشة عرض النتائج.

**الوظائف**:
- عرض الصورة والمعلومات
- حفظ النتيجة
- مشاركة النتيجة

## إعداد بيئة التطوير

### المتطلبات
- Android Studio Hedgehog (2023.1.1) أو أحدث
- JDK 17
- Android SDK Platform 34
- Gradle 8.1+

### خطوات الإعداد

#### 1. استنساخ المشروع
```bash
git clone <repository-url>
cd AIObjectDetector
```

#### 2. فتح المشروع في Android Studio
- File → Open
- اختر مجلد المشروع
- انتظر Gradle Sync

#### 3. إضافة مفتاح Gemini API
افتح ملف `GeminiAnalyzer.kt` وعدّل السطر:
```kotlin
private val apiKey = "YOUR_GEMINI_API_KEY_HERE"
```

للحصول على مفتاح API:
1. زر [Google AI Studio](https://makersuite.google.com/app/apikey)
2. سجّل الدخول بحساب Google
3. انقر على "Create API Key"
4. انسخ المفتاح

#### 4. البناء والتشغيل
- اضغط على زر Run (▶️)
- اختر جهاز أو محاكي
- انتظر البناء والتثبيت

## البناء للإنتاج

### إنشاء APK موقّع

#### 1. إنشاء Keystore
```bash
keytool -genkey -v -keystore ai-detector-release.keystore \
  -alias ai-detector -keyalg RSA -keysize 2048 -validity 10000
```

#### 2. تكوين Signing في build.gradle
```gradle
android {
    signingConfigs {
        release {
            storeFile file("ai-detector-release.keystore")
            storePassword "your_store_password"
            keyAlias "ai-detector"
            keyPassword "your_key_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

#### 3. البناء
```bash
./gradlew assembleRelease
```

الملف الناتج: `app/build/outputs/apk/release/app-release.apk`

### إنشاء AAB للنشر على Google Play
```bash
./gradlew bundleRelease
```

الملف الناتج: `app/build/outputs/bundle/release/app-release.aab`

## الاختبار

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### اختبار يدوي
1. اختبر جميع الأذونات
2. اختبر الكاميرا الأمامية والخلفية
3. اختبر الفلاش
4. اختبر التعرف على أنواع مختلفة من الأشياء
5. اختبر الحفظ والمشاركة
6. اختبر السجل والحذف

## التخصيص والتوسيع

### إضافة لغات جديدة

#### 1. إنشاء ملف strings.xml جديد
```
res/values-en/strings.xml  (للإنجليزية)
res/values-fr/strings.xml  (للفرنسية)
```

#### 2. تعديل GeminiAnalyzer
عدّل الـ prompt ليدعم اللغة المطلوبة:
```kotlin
val prompt = when (locale) {
    "ar" -> "قم بتحليل..."
    "en" -> "Analyze this image..."
    else -> "قم بتحليل..."
}
```

### إضافة نماذج ML محلية

#### 1. تحميل نموذج TFLite
```kotlin
val model = Model.newInstance(context)
```

#### 2. معالجة الصورة
```kotlin
val inputFeature = TensorImage.fromBitmap(bitmap)
val outputs = model.process(inputFeature)
```

### تحسين الأداء

#### استخدام Image Compression
```kotlin
val compressed = Bitmap.createScaledBitmap(
    bitmap, 
    maxWidth, 
    maxHeight, 
    true
)
```

#### Caching
```kotlin
val cache = LruCache<String, DetectionResult>(maxSize)
```

## الأمان

### حماية API Key

#### استخدام BuildConfig
```gradle
android {
    defaultConfig {
        buildConfigField "String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\""
    }
}
```

في `gradle.properties`:
```
GEMINI_API_KEY=your_actual_key
```

في الكود:
```kotlin
private val apiKey = BuildConfig.GEMINI_API_KEY
```

### ProGuard
تأكد من تفعيل ProGuard في الإصدار النهائي لحماية الكود.

## المشاكل الشائعة وحلولها

### مشكلة: CameraX لا يعمل
**الحل**: تأكد من الأذونات وتحديث المكتبة.

### مشكلة: ML Kit يفشل
**الحل**: تحقق من Google Play Services.

### مشكلة: Gemini API يعيد خطأ
**الحل**: تحقق من المفتاح والاتصال بالإنترنت.

### مشكلة: Room Database يتعطل
**الحل**: تحقق من Migration عند تغيير Schema.

## المساهمة

### معايير الكود
- استخدم Kotlin Code Style الرسمي
- اتبع MVVM Architecture
- اكتب Unit Tests للوظائف الجديدة
- استخدم Coroutines بدلاً من Callbacks

### Git Workflow
```bash
git checkout -b feature/new-feature
git commit -m "Add new feature"
git push origin feature/new-feature
```

## الموارد المفيدة

### الوثائق الرسمية
- [Android Developers](https://developer.android.com/)
- [CameraX Documentation](https://developer.android.com/training/camerax)
- [ML Kit Documentation](https://developers.google.com/ml-kit)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Room Database](https://developer.android.com/training/data-storage/room)

### أدوات مفيدة
- [Android Studio](https://developer.android.com/studio)
- [Kotlin Playground](https://play.kotlinlang.org/)
- [Material Design](https://material.io/design)

## الترخيص

هذا المشروع مفتوح المصدر ومتاح للاستخدام والتعديل.

## الدعم

للأسئلة والمشاكل التقنية، يرجى فتح Issue على GitHub.

---

**تم التطوير بواسطة**: فريق كاشف الأشياء الذكي
**الإصدار**: 1.0
**آخر تحديث**: 2024
