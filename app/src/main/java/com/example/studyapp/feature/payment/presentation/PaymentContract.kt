package com.example.studyapp.feature.payment.presentation

import com.example.studyapp.feature.payment.domain.model.PaymentType
import com.example.studyapp.feature.payment.domain.model.Transaction
import java.math.BigDecimal

sealed interface PaymentUiState {
    data object Idle: PaymentUiState
    data object Processing : PaymentUiState
    data class Success(val transaction: Transaction) : PaymentUiState
    data class Error(val message: String) : PaymentUiState

}

sealed interface PaymentUiEvent{
    data class  OnProcessPaymentClicked(val amount: BigDecimal, val type: PaymentType) : PaymentUiEvent
    data object OnResetClicked : PaymentUiEvent

}