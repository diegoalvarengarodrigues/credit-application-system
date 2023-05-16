package me.dio.credit.application.system.controller

import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
import java.time.LocalDate
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.entity.Customer

object CreditResourceHelper {

    val creditDto: CreditDto
        get() = mockk(relaxed = true) {
            every { creditValue } returns BigDecimal.valueOf(1000)
            every { dayFirstOfInstallment } returns LocalDate.now().plusDays(10)
            every { numberOfInstallments } returns 3
            every { customerId } returns 1L
        }

    val invalidCreditDto: CreditDto
        get() = creditDto.apply {
            every { dayFirstOfInstallment } returns LocalDate.now().plusMonths(4)
        }

    val customer: Customer
        get() = mockk(relaxed = true) {
            every { firstName } returns "firstName"
            every { lastName } returns "lastName"
            every { cpf } returns "322.862.690-33"
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