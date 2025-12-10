<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/btn_start_detection"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="فتح الكاميرا" 
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="16dp"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"/>

    <Button
        android:id="@+id/btn_history"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/history"
        app:layout_constraintTop_toBottomOf="@id/btn_start_detection"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="8dp"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"/>

    <Button
        android:id="@+id/btn_settings"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/settings"
        app:layout_constraintTop_toBottomOf="@id/btn_history"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="8dp"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="16dp"/>

</androidx.constraintlayout.widget.ConstraintLayout>
