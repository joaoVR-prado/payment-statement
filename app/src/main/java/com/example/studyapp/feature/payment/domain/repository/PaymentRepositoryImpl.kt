package com.example.studyapp.feature.payment.domain.repository

import com.example.studyapp.feature.payment.domain.model.PaymentType
import com.example.studyapp.feature.payment.domain.model.Transaction
import com.example.studyapp.feature.payment.domain.model.TransactionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class PaymentRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PaymentRepository  {
    private val inMemoryHistory = mutableListOf<Transaction>()

    override suspend fun processPayment(
        amount: BigDecimal,
        type: PaymentType
    ) : Result<Transaction> = withContext(ioDispatcher){
        if(amount <= BigDecimal.ZERO){
            return@withContext Result.failure(
                IllegalArgumentException(
                    "O valor da Transação deve ser maior que zero"
                )
            )
        }

        try{
            // Simula a demora da leitura do cartao
            delay(2000.milliseconds)

            val isAproved = amount < BigDecimal("9999.00")
            val status = if(isAproved){
                TransactionStatus.Approved
            } else{
                TransactionStatus.Declined("Você atingiu o limite da Transação")
            }

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amount,
                type = type,
                status = status

            )

            inMemoryHistory.add(0, transaction)
            Result.success(transaction)
        } catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getTransactionHistory(): Result<List<Transaction>> = withContext(ioDispatcher) {
        delay(500.milliseconds)
        Result.success(inMemoryHistory.toList())

    }

}