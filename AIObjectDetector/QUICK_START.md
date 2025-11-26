# دليل البدء السريع ⚡

## للمستخدمين 📱

### الخطوات الأساسية
1. **حمّل التطبيق** من الرابط المتوفر
2. **ثبّت APK** على جهازك
3. **افتح التطبيق** واضغط "ابدأ الكشف"
4. **امنح الأذونات** (الكاميرا والتخزين)
5. **وجه الكاميرا** نحو أي شيء
6. **اضغط زر الكاميرا** للتقاط الصورة
7. **شاهد النتائج** التفصيلية!

### نصائح سريعة
- ✅ استخدم إضاءة جيدة
- ✅ ضع الشيء في منتصف الإطار
- ✅ تأكد من الاتصال بالإنترنت

---

## للمطورين 👨‍💻

### الإعداد السريع

#### 1. المتطلبات
```bash
# تحقق من التثبيت
java -version  # يجب أن يكون 17+
```

#### 2. فتح المشروع
```bash
# فك الضغط
unzip AIObjectDetector.zip
cd AIObjectDetector

# أو استنساخ من Git
git clone <repository-url>
cd AIObjectDetector
```

#### 3. إضافة مفتاح API
افتح `app/src/main/java/com/aidetector/app/ml/GeminiAnalyzer.kt`:
```kotlin
private val apiKey = "YOUR_API_KEY_HERE"
```

احصل على مفتاح من: https://makersuite.google.com/app/apikey

#### 4. البناء والتشغيل
```bash
# في Android Studio
# اضغط Run (▶️)

# أو من Terminal
./gradlew installDebug
```

### البناء للإنتاج
```bash
./gradlew assembleRelease
# الملف: app/build/outputs/apk/release/app-release.apk
```

---

## الملفات المهمة 📁

| الملف | الوصف |
|-------|-------|
| `README.md` | نظرة عامة كاملة |
| `USER_GUIDE.md` | دليل المستخدم التفصيلي |
| `DEVELOPER_GUIDE.md` | دليل المطور الشامل |
| `INSTALLATION.md` | تعليمات التثبيت والإعداد |
| `PROJECT_SUMMARY.md` | ملخص المشروع |

---

## الأكواد الرئيسية 💻

### MainActivity
```kotlin
// نقطة الدخول الرئيسية
class MainActivity : AppCompatActivity()
```

### CameraActivity
```kotlin
// إدارة الكاميرا والتقاط الصور
class CameraActivity : AppCompatActivity()
```

### AIAnalyzer
```kotlin
// محرك الذكاء الاصطناعي
suspend fun analyzeImage(bitmap: Bitmap): DetectionResult?
```

### GeminiAnalyzer
```kotlin
// تكامل Gemini AI
suspend fun getDetailedInfo(...): DetectionResult?
```

---

## الأذونات المطلوبة 🔐

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
```

---

## المكتبات الرئيسية 📚

```gradle
// CameraX
androidx.camera:camera-core:1.3.1

// ML Kit
com.google.mlkit:image-labeling:17.0.8

// Room
androidx.room:room-runtime:2.6.1

// Retrofit
com.squareup.retrofit2:retrofit:2.9.0
```

---

## حل المشاكل السريع 🔧

### المشكلة: الكاميرا لا تعمل
**الحل**: تحقق من الأذونات في الإعدادات

### المشكلة: التحليل يفشل
**الحل**: تحقق من الاتصال بالإنترنت ومفتاح API

### المشكلة: Gradle Sync فشل
**الحل**: 
```bash
./gradlew clean
./gradlew --refresh-dependencies
```

---

## الدعم 💬

- 📖 **الوثائق**: اقرأ الأدلة المرفقة
- 🐛 **المشاكل**: افتح Issue على GitHub
- 📧 **البريد**: support@aidetector.app

---

## روابط مفيدة 🔗

- [Android Studio](https://developer.android.com/studio)
- [Gemini API](https://ai.google.dev/)
- [ML Kit](https://developers.google.com/ml-kit)
- [CameraX](https://developer.android.com/training/camerax)

---

**جاهز للبدء؟ افتح Android Studio وابدأ التطوير! 🚀**
