package com.example.studyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.studyapp.feature.payment.presentation.PaymentViewModel
import com.example.studyapp.ui.theme.StudyAppTheme
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.studyapp.feature.payment.domain.repository.PaymentRepositoryImpl
import com.example.studyapp.feature.payment.presentation.PaymentRoute

class MainActivity : ComponentActivity() {
    private val paymentViewModel: PaymentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = PaymentRepositoryImpl()
                return PaymentViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PaymentRoute(
                        viewModel = paymentViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}