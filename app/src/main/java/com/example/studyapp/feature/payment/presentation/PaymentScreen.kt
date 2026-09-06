package com.example.studyapp.feature.payment.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyapp.feature.payment.domain.model.PaymentType
import java.math.BigDecimal

@Composable
fun PaymentRoute(
    viewModel: PaymentViewModel,
    modifier: Modifier = Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PaymentScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier

    )

}

@Composable
fun PaymentScreen(
    state: PaymentUiState,
    onEvent: (PaymentUiEvent) -> Unit,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state){
            is PaymentUiState.Idle -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Cobrança POS", style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onEvent(
                                PaymentUiEvent.OnProcessPaymentClicked(
                                    amount = BigDecimal("150.00"),
                                    type = PaymentType.CREDIT
                                )
                            )
                        }
                    ) {
                        Text("Pagar R$ 150,00 no Crédito")
                    }
                }
            }

            is PaymentUiState.Processing -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processando transação com a adquirente...")

                }
            }

            is PaymentUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Transação Aprovada!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("ID: ${state.transaction.id}")
                    Text("Valor: R$ ${state.transaction.amount}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(onClick = { onEvent(PaymentUiEvent.OnResetClicked) }) {
                        Text("Nova Cobrança")

                    }
                }
            }

            is PaymentUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge

                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onEvent(PaymentUiEvent.OnResetClicked) }) {
                        Text("Tentar Novamente")

                    }
                }
            }

        }

    }

}

