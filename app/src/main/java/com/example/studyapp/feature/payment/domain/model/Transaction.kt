package com.example.studyapp.feature.payment.domain.model

import java.math.BigDecimal
import java.time.Instant

enum class PaymentType{
    CREDIT,
    DEBIT,
    PIX

}

sealed interface TransactionStatus{
    data object Approved : TransactionStatus
    data class Declined(val reason: String) : TransactionStatus
    data class Failed(val error: Throwable) : TransactionStatus

}

data class Transaction(
    val id: String,
    val amount: BigDecimal,
    val type: PaymentType,
    val status: TransactionStatus,
    val createdAt: Instant = Instant.now()

)