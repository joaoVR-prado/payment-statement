package com.example.studyapp.feature.payment.domain.repository

import com.example.studyapp.feature.payment.domain.model.PaymentType
import com.example.studyapp.feature.payment.domain.model.Transaction
import java.math.BigDecimal

interface PaymentRepository {
    suspend fun processPayment(
        amount: BigDecimal,
        type: PaymentType
    ): Result<Transaction>

    suspend fun getTransactionHistory(): Result<List<Transaction>>

}