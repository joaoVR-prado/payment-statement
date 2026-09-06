package com.example.studyapp.feature.payment.presentation

import android.media.metrics.Event
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.feature.payment.domain.model.PaymentType
import com.example.studyapp.feature.payment.domain.model.Transaction
import com.example.studyapp.feature.payment.domain.model.TransactionStatus
import com.example.studyapp.feature.payment.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PaymentViewModel(
    private val repository: PaymentRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun onEvent(event: PaymentUiEvent){
        when(event){
            is PaymentUiEvent.OnProcessPaymentClicked -> processPayment(event.amount, event.type)
            is PaymentUiEvent.OnResetClicked -> _uiState.value = PaymentUiState.Idle

        }

    }

    private fun processPayment(amount: BigDecimal, type: PaymentType){
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Processing

            repository.processPayment(amount, type)
                .onSuccess { transaction ->
                    when(val status = transaction.status){
                        is TransactionStatus.Approved ->{
                            _uiState.value = PaymentUiState.Success(transaction)

                        }
                        is TransactionStatus.Declined ->{
                            _uiState.value = PaymentUiState.Error("Transação recusada: ${status.reason}")

                        }
                        is TransactionStatus.Failed ->{
                            _uiState.value = PaymentUiState.Error(
                                status.error.localizedMessage ?: "Erro ao processar pagamento"
                            )

                        }

                    }
                }
                .onFailure { error ->
                    _uiState.value = PaymentUiState.Error(error.localizedMessage ?: "Falha de comunicação com o POS")
                }

        }
    }

}