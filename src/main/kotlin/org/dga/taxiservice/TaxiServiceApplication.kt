package org.dga.taxiservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaxiServiceApplication

fun main(args: Array<String>) {
    runApplication<TaxiServiceApplication>(*args)
}
