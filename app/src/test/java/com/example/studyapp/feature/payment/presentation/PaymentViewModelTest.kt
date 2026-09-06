package com.example.studyapp.feature.payment.presentation

import app.cash.turbine.test
import com.example.studyapp.feature.payment.domain.model.PaymentType
import com.example.studyapp.feature.payment.domain.model.Transaction
import com.example.studyapp.feature.payment.domain.model.TransactionStatus
import com.example.studyapp.feature.payment.domain.repository.PaymentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<PaymentRepository>()
    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setUp(){
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentViewModel(repository = repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()

    }

    @Test
    fun `initial state should be Idle`() = runTest {
        assertEquals(PaymentUiState.Idle, viewModel.uiState.value)

    }

    @Test
    fun `onEvent ProcessPayment when repository succeeds with approved should emit Processing then Success`() = runTest {
        val amount = BigDecimal("150.00")
        val type = PaymentType.CREDIT
        val fakeTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            status = TransactionStatus.Approved

        )

        coEvery { repository.processPayment(amount, type) } returns Result.success(fakeTransaction)

        viewModel.uiState.test {
            assertEquals(PaymentUiState.Idle, awaitItem())

            viewModel.onEvent(PaymentUiEvent.OnProcessPaymentClicked(amount, type))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(PaymentUiState.Processing, awaitItem())
            val successState = awaitItem() as PaymentUiState.Success
            assertEquals(fakeTransaction, successState.transaction)

            expectNoEvents()

        }

        coVerify(exactly = 1) { repository.processPayment(amount, type) }
    }

    @Test
    fun `onEvent ProcessPayment when repository succeeds with declined should emit Processing then Error`() = runTest {
        val amount = BigDecimal("10000.00")
        val type = PaymentType.DEBIT
        val fakeTransaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            status = TransactionStatus.Declined("Você atingiu o limite da Transação")
        )

        coEvery { repository.processPayment(amount, type) } returns Result.success(fakeTransaction)

        viewModel.uiState.test {
            assertEquals(PaymentUiState.Idle, awaitItem())

            viewModel.onEvent(PaymentUiEvent.OnProcessPaymentClicked(amount, type))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(PaymentUiState.Processing, awaitItem())
            val errorState = awaitItem() as PaymentUiState.Error
            assertTrue(errorState.message.contains("Você atingiu o limite da Transação"))
        }
    }

    @Test
    fun `onEvent ResetClicked should return state to Idle`() = runTest {
        viewModel.uiState.test {
            assertEquals(PaymentUiState.Idle, awaitItem())

            viewModel.onEvent(PaymentUiEvent.OnResetClicked)
            assertEquals(PaymentUiState.Idle, viewModel.uiState.value)
        }
    }

}