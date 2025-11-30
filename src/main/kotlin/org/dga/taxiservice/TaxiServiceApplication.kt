package org.dga.taxiservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TaxiServiceApplication

fun main(args: Array<String>) {
    runApplication<TaxiServiceApplication>(*args)
}
