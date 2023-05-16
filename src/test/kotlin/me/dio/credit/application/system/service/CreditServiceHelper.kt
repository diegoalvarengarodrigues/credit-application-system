package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enummeration.Status

object CreditServiceHelper {

    val validCredit: Credit = mockk(relaxed = true) {
        every { creditCode } returns UUID.randomUUID()
        every { creditValue } returns BigDecimal.valueOf(10)
        every { dayFirstInstallment } returns LocalDate.now()
        every { numberOfInstallments } returns 2
        every { status } returns Status.APPROVED
        every { customer } returns customerModel
    }

    val invalidDateCredit: Credit
        get() = mockk(relaxed = true) {
            every { creditCode } returns UUID.randomUUID()
            every { creditValue } returns BigDecimal.valueOf(10)
            every { dayFirstInstallment } returns LocalDate.now().plusMonths(4)
            every { numberOfInstallments } returns 2
            every { status } returns Status.APPROVED
            every { customer } returns customerModel
        }

    val customerModel: Customer
        get() = mockk(relaxed = true) {
            every { firstName } returns "firstName"
            every { lastName } returns "lastName"
            every { cpf } returns "123456789-00"
            every { email } returns "email@gmail.com"
            every { password } returns "12345"
            every { address } returns mockk(relaxed = true) {
                every { zipCode } returns "12345"
                every { street } returns "street"
            }
            every { income } returns BigDecimal.valueOf(1000.0)
            every { id } returns 1L
        }
}