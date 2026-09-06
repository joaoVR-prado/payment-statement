package com.example.studyapp.payment.domain.repository

import com.example.studyapp.feature.payment.domain.model.PaymentType
import com.example.studyapp.feature.payment.domain.model.TransactionStatus
import com.example.studyapp.feature.payment.domain.repository.PaymentRepositoryImpl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class PaymentRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PaymentRepositoryImpl

    @Before
    fun setUp(){
        repository = PaymentRepositoryImpl(ioDispatcher = testDispatcher)

    }

    @Test
    fun `processPayment when amount is valid and under the limit should return approved transaction` () = runTest(testDispatcher){
        val amount = BigDecimal("99.99")
        val type= PaymentType.CREDIT

        val result = repository.processPayment(amount, type)

        assertTrue(result.isSuccess)
        val transaction = result.getOrThrow()
        assertEquals(amount, transaction.amount)
        assertEquals(type, transaction.type)
        assertEquals(TransactionStatus.Approved, transaction.status)

    }

    @Test
    fun `processPayment when amount exceeds 9999 should return declined transaction` () = runTest(testDispatcher){
        val amount = BigDecimal("10000.00")
        val type= PaymentType.DEBIT

        val result = repository.processPayment(amount, type)

        assertTrue(result.isSuccess)
        val transaction = result.getOrThrow()
        assertEquals(amount, transaction.amount)
        assertEquals(type, transaction.type)
        assertTrue(transaction.status is TransactionStatus.Declined)

        val declinedStatus = transaction.status as TransactionStatus.Declined
        assertEquals("Você atingiu o limite da Transação", declinedStatus.reason)

    }

    @Test
    fun `processPayment when amount is ZERO or NEGATIVE should return failure result` () = runTest(testDispatcher){
        val amount = BigDecimal.ZERO
        val type= PaymentType.PIX

        val result = repository.processPayment(amount, type)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)

        assertEquals("O valor da Transação deve ser maior que zero", exception?.message)

    }

}