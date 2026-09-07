package com.example.studyapp.feature.payment.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.studyapp.feature.payment.domain.model.PaymentType
import com.example.studyapp.feature.payment.domain.model.Transaction
import com.example.studyapp.feature.payment.domain.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class PaymentScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun idleState_shouldDisplayTitleAndChargeButton(){
        composeTestRule.setContent {
            PaymentScreen(
                state = PaymentUiState.Idle,
                onEvent = {}
            )
        }

        composeTestRule
            .onNodeWithText("Cobrança POS")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Pagar R$ 150,00 no Crédito")
            .assertIsDisplayed()

    }

    @Test
    fun idleState_whenClickingChargeButton_shouldTriggerExpectedEvent(){
        var capturedEvent: PaymentUiEvent? = null

        composeTestRule.setContent {
            PaymentScreen(
                state = PaymentUiState.Idle,
                onEvent = {
                    event -> capturedEvent = event
                }
            )
        }

        composeTestRule
            .onNodeWithText("Pagar R$ 150,00 no Crédito")
            .performClick()

        assertTrue(capturedEvent is PaymentUiEvent.OnProcessPaymentClicked)
        val clickEvent = capturedEvent as PaymentUiEvent.OnProcessPaymentClicked
        assertEquals(BigDecimal("150.00"), clickEvent.amount)
        assertEquals(PaymentType.CREDIT, clickEvent.type)

    }

    @Test
    fun processingState_shouldDisplayLoadingMessage() {
        composeTestRule.setContent {
            PaymentScreen(
                state = PaymentUiState.Processing,
                onEvent = {}
            )
        }

        composeTestRule
            .onNodeWithText("Processando transação com a adquirente...")
            .assertIsDisplayed()

    }

    @Test
    fun successState_shouldDisplayTransactionDetailsAndNewChargeButton() {
        val fakeTx = Transaction(
            id = "tx-123456",
            amount = BigDecimal("150.00"),
            type = PaymentType.CREDIT,
            status = TransactionStatus.Approved
        )

        composeTestRule.setContent {
            PaymentScreen(
                state = PaymentUiState.Success(fakeTx),
                onEvent = {}
            )
        }

        composeTestRule
            .onNodeWithText("Transação Aprovada!")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("ID: tx-123456")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Valor: R$ 150.00")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Nova Cobrança")
            .assertIsDisplayed()

    }

    @Test
    fun errorState_shouldDisplayErrorMessageAndRetryButton() {
        val errorMsg = "Você atingiu o limite da Transação"

        composeTestRule.setContent {
            PaymentScreen(
                state = PaymentUiState.Error(errorMsg),
                onEvent = {}
            )
        }

        composeTestRule
            .onNodeWithText(errorMsg)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Tentar Novamente")
            .assertIsDisplayed()
    }

}