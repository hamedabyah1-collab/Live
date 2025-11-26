# دليل التثبيت والإعداد

## محتويات الدليل
1. [تثبيت التطبيق على الجوال](#تثبيت-التطبيق-على-الجوال)
2. [إعداد بيئة التطوير](#إعداد-بيئة-التطوير)
3. [الحصول على مفتاح Gemini API](#الحصول-على-مفتاح-gemini-api)
4. [بناء التطبيق](#بناء-التطبيق)
5. [حل المشاكل](#حل-المشاكل)

---

## تثبيت التطبيق على الجوال

### المتطلبات الأساسية
- جهاز Android بإصدار 7.0 (Nougat) أو أحدث
- مساحة تخزين: 50 ميجابايت على الأقل
- كاميرا خلفية (وأمامية اختيارية)
- اتصال بالإنترنت

### طريقة التثبيت من APK

#### الخطوة 1: تحميل ملف APK
1. حمّل ملف `ai-detector-release.apk` من مصدر موثوق
2. احفظ الملف في مكان يسهل الوصول إليه

#### الخطوة 2: تفعيل التثبيت من مصادر غير معروفة

**لأجهزة Android 8.0 وأحدث:**
1. افتح **الإعدادات**
2. انتقل إلى **الأمان والخصوصية**
3. اضغط على **تثبيت التطبيقات غير المعروفة**
4. اختر **متصفح الملفات** أو التطبيق الذي ستستخدمه لفتح APK
5. فعّل **السماح من هذا المصدر**

**لأجهزة Android 7.x:**
1. افتح **الإعدادات**
2. انتقل إلى **الأمان**
3. فعّل **مصادر غير معروفة**
4. اضغط **موافق** عند ظهور التحذير

#### الخطوة 3: تثبيت التطبيق
1. افتح متصفح الملفات
2. انتقل إلى مجلد التحميلات
3. اضغط على ملف `ai-detector-release.apk`
4. اضغط على **تثبيت**
5. انتظر حتى يكتمل التثبيت
6. اضغط على **فتح** لتشغيل التطبيق

#### الخطوة 4: منح الأذونات
عند فتح التطبيق لأول مرة:
1. سيطلب التطبيق إذن **الكاميرا** - اضغط **السماح**
2. قد يطلب إذن **التخزين** - اضغط **السماح**

### طريقة التثبيت عبر ADB (للمطورين)

```bash
# تأكد من تفعيل USB Debugging على الجهاز
adb devices

# تثبيت التطبيق
adb install ai-detector-release.apk

# أو لتحديث تطبيق موجود
adb install -r ai-detector-release.apk
```

---

## إعداد بيئة التطوير

### المتطلبات

#### البرامج المطلوبة
- **Android Studio**: Hedgehog (2023.1.1) أو أحدث
- **JDK**: Java Development Kit 17
- **Git**: لإدارة الإصدارات

#### تحميل Android Studio
1. زر [developer.android.com/studio](https://developer.android.com/studio)
2. حمّل الإصدار المناسب لنظام التشغيل
3. ثبّت البرنامج واتبع التعليمات

#### تثبيت JDK 17
```bash
# على Ubuntu/Linux
sudo apt update
sudo apt install openjdk-17-jdk

# على macOS (باستخدام Homebrew)
brew install openjdk@17

# على Windows
# حمّل من: https://www.oracle.com/java/technologies/downloads/
```

### إعداد Android SDK

#### من خلال Android Studio
1. افتح Android Studio
2. اذهب إلى **Settings** (أو **Preferences** على Mac)
3. انتقل إلى **Appearance & Behavior** → **System Settings** → **Android SDK**
4. في تبويب **SDK Platforms**، حدد:
   - Android 14.0 (API 34)
   - Android 7.0 (API 24)
5. في تبويب **SDK Tools**، حدد:
   - Android SDK Build-Tools
   - Android Emulator
   - Android SDK Platform-Tools
6. اضغط **Apply** ثم **OK**

### استنساخ المشروع

```bash
# استنساخ المشروع (إذا كان على Git)
git clone <repository-url>
cd AIObjectDetector

# أو فك ضغط الملف المضغوط
unzip AIObjectDetector.zip
cd AIObjectDetector
```

### فتح المشروع في Android Studio

1. افتح Android Studio
2. اختر **File** → **Open**
3. انتقل إلى مجلد المشروع `AIObjectDetector`
4. اضغط **OK**
5. انتظر حتى يكتمل **Gradle Sync**

---

## الحصول على مفتاح Gemini API

### الخطوة 1: الوصول إلى Google AI Studio
1. افتح المتصفح واذهب إلى [makersuite.google.com](https://makersuite.google.com/app/apikey)
2. سجّل الدخول بحساب Google الخاص بك

### الخطوة 2: إنشاء مفتاح API
1. اضغط على **"Get API Key"** أو **"Create API Key"**
2. اختر مشروع Google Cloud موجود أو أنشئ مشروعاً جديداً
3. اضغط على **"Create API Key in new project"** (إذا كنت تنشئ مشروعاً جديداً)
4. سيظهر مفتاح API - **انسخه فوراً**

⚠️ **مهم**: احفظ المفتاح في مكان آمن، لن تتمكن من رؤيته مرة أخرى!

### الخطوة 3: إضافة المفتاح إلى المشروع

#### الطريقة 1: تعديل مباشر (للتطوير فقط)
افتح ملف `app/src/main/java/com/aidetector/app/ml/GeminiAnalyzer.kt`:

```kotlin
private val apiKey = "YOUR_ACTUAL_API_KEY_HERE"
```

استبدل `YOUR_ACTUAL_API_KEY_HERE` بالمفتاح الفعلي.

#### الطريقة 2: استخدام gradle.properties (موصى به)
1. افتح ملف `gradle.properties` في جذر المشروع
2. أضف السطر التالي:
```properties
GEMINI_API_KEY=your_actual_api_key_here
```

3. عدّل `app/build.gradle`:
```gradle
android {
    defaultConfig {
        buildConfigField "String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY")}\""
    }
}
```

4. عدّل `GeminiAnalyzer.kt`:
```kotlin
private val apiKey = BuildConfig.GEMINI_API_KEY
```

#### الطريقة 3: استخدام local.properties (الأكثر أماناً)
1. أنشئ ملف `local.properties` في جذر المشروع (إذا لم يكن موجوداً)
2. أضف:
```properties
gemini.api.key=your_actual_api_key_here
```

3. عدّل `app/build.gradle`:
```gradle
def localProperties = new Properties()
def localPropertiesFile = rootProject.file('local.properties')
if (localPropertiesFile.exists()) {
    localPropertiesFile.withInputStream { localProperties.load(it) }
}

android {
    defaultConfig {
        buildConfigField "String", "GEMINI_API_KEY", "\"${localProperties.getProperty('gemini.api.key')}\""
    }
}
```

⚠️ **تنبيه أمني**: لا تضف `local.properties` إلى Git!

---

## بناء التطبيق

### البناء للتطوير (Debug)

#### من Android Studio
1. اضغط على **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. انتظر حتى يكتمل البناء
3. ستجد الملف في: `app/build/outputs/apk/debug/app-debug.apk`

#### من سطر الأوامر
```bash
# بناء APK للتطوير
./gradlew assembleDebug

# تثبيت مباشرة على جهاز متصل
./gradlew installDebug
```

### البناء للإنتاج (Release)

#### الخطوة 1: إنشاء Keystore
```bash
keytool -genkey -v -keystore ai-detector-release.keystore \
  -alias ai-detector \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

ستُسأل عن:
- كلمة مرور Keystore (احفظها!)
- الاسم والمنظمة (اختياري)
- كلمة مرور المفتاح (احفظها!)

#### الخطوة 2: تكوين Signing
أنشئ ملف `keystore.properties` في جذر المشروع:
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=ai-detector
storeFile=../ai-detector-release.keystore
```

عدّل `app/build.gradle`:
```gradle
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

android {
    signingConfigs {
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
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

#### الخطوة 3: البناء
```bash
# بناء APK موقّع
./gradlew assembleRelease

# الملف الناتج
# app/build/outputs/apk/release/app-release.apk
```

### بناء AAB للنشر على Google Play
```bash
./gradlew bundleRelease

# الملف الناتج
# app/build/outputs/bundle/release/app-release.aab
```

---

## حل المشاكل

### مشكلة: Gradle Sync فشل

**الأعراض**: رسالة خطأ عند فتح المشروع

**الحلول**:
```bash
# 1. تنظيف المشروع
./gradlew clean

# 2. حذف ملفات Gradle المؤقتة
rm -rf .gradle
rm -rf app/build

# 3. إعادة Sync
./gradlew --refresh-dependencies
```

### مشكلة: SDK غير موجود

**الأعراض**: `SDK location not found`

**الحل**:
أنشئ ملف `local.properties`:
```properties
sdk.dir=/path/to/Android/Sdk
```

على Windows:
```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

### مشكلة: Java Version خاطئ

**الأعراض**: `Unsupported Java version`

**الحل**:
```bash
# تحقق من الإصدار
java -version

# في Android Studio:
# File → Settings → Build, Execution, Deployment → Build Tools → Gradle
# Gradle JDK: اختر JDK 17
```

### مشكلة: التطبيق يتعطل عند فتح الكاميرا

**الحل**:
1. تأكد من منح إذن الكاميرا
2. تحقق من أن الجهاز يحتوي على كاميرا
3. أعد تشغيل التطبيق

### مشكلة: Gemini API لا يعمل

**الأعراض**: فشل التحليل التفصيلي

**الحلول**:
1. تحقق من صحة مفتاح API
2. تأكد من الاتصال بالإنترنت
3. راجع Logcat للأخطاء:
```bash
adb logcat | grep GeminiAnalyzer
```

### مشكلة: التطبيق بطيء

**الحلول**:
1. قلل حجم الصور قبل الإرسال
2. استخدم ProGuard في Release
3. فعّل R8 optimization

### مشكلة: Room Database خطأ

**الأعراض**: `Cannot find implementation for database`

**الحل**:
تأكد من إضافة kapt في `app/build.gradle`:
```gradle
plugins {
    id 'kotlin-kapt'
}

dependencies {
    kapt "androidx.room:room-compiler:2.6.1"
}
```

---

## الاختبار

### اختبار على محاكي

#### إنشاء محاكي
1. في Android Studio: **Tools** → **Device Manager**
2. اضغط **Create Device**
3. اختر جهاز (مثل Pixel 6)
4. اختر صورة نظام (API 34 موصى به)
5. اضغط **Finish**

#### تشغيل التطبيق
1. اختر المحاكي من القائمة
2. اضغط **Run** (▶️)

⚠️ **ملاحظة**: المحاكي قد لا يدعم الكاميرا بشكل كامل.

### اختبار على جهاز حقيقي

#### تفعيل USB Debugging
1. اذهب إلى **الإعدادات** → **حول الهاتف**
2. اضغط على **رقم الإصدار** 7 مرات
3. ارجع إلى **الإعدادات** → **خيارات المطور**
4. فعّل **تصحيح USB**

#### الاتصال والتشغيل
```bash
# تحقق من الاتصال
adb devices

# تشغيل التطبيق
./gradlew installDebug
```

---

## الخطوات التالية

بعد التثبيت الناجح:
1. اقرأ [دليل المستخدم](USER_GUIDE.md)
2. راجع [دليل المطور](DEVELOPER_GUIDE.md) للتخصيص
3. ابدأ باستخدام التطبيق!

---

## الدعم

إذا واجهت أي مشاكل:
- راجع قسم [حل المشاكل](#حل-المشاكل)
- افتح Issue على GitHub
- تواصل مع الدعم الفني

**حظاً موفقاً! 🚀**
