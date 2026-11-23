package com.practica.tutormatematicas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.practica.tutormatematicas.ui.TutorApp
import com.practica.tutormatematicas.ui.TutorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TutorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TutorApp(viewModel)
        }
    }
}
