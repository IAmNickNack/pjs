package io.github.iamnicknack.pjs.http.server

import io.github.iamnicknack.pjs.device.gpio.GpioPort
import io.github.iamnicknack.pjs.device.i2c.I2C
import io.github.iamnicknack.pjs.device.pwm.Pwm
import io.github.iamnicknack.pjs.device.spi.Spi
import io.github.iamnicknack.pjs.http.gpio.GpioPortHandler
import io.github.iamnicknack.pjs.http.i2c.I2CHandler
import io.github.iamnicknack.pjs.http.pwm.PwmHandler
import io.github.iamnicknack.pjs.http.server.config.ConfigHandlerImpl
import io.github.iamnicknack.pjs.http.server.gpio.GpioEventsHandler
import io.github.iamnicknack.pjs.http.server.gpio.GpioPortHandlerImpl
import io.github.iamnicknack.pjs.http.server.i2c.I2CHandlerImpl
import io.github.iamnicknack.pjs.http.server.pwm.PwmHandlerImpl
import io.github.iamnicknack.pjs.http.server.spi.SpiHandlerImpl
import io.github.iamnicknack.pjs.http.server.spi.SpiTransferHandlerImpl
import io.github.iamnicknack.pjs.http.spi.SpiHandler
import io.github.iamnicknack.pjs.http.spi.SpiTransferHandler
import org.koin.dsl.module

/**
 * Handler implementations for HTTP operations
 */
val handlerModule = module {
    single<GpioPortHandlerImpl> {
        val configHandler = ConfigHandlerImpl(
            get(),
            GpioPort::class.java,
        )
        GpioPortHandlerImpl(get(), configHandler)
    }

    single<GpioEventsHandler> {
        get<GpioPortHandlerImpl>()
    }

    single<GpioPortHandler> {
        get<GpioPortHandlerImpl>()
    }

    single<SpiHandler> {
        val configHandler = ConfigHandlerImpl(
            get(),
            Spi::class.java,
        )
        SpiHandlerImpl(get(), configHandler)
    }

    single<SpiTransferHandler> {
        SpiTransferHandlerImpl(get())
    }

    single<PwmHandler> {
        val configHandler = ConfigHandlerImpl(
            get(),
            Pwm::class.java
        )
        PwmHandlerImpl(get(), configHandler)
    }

    single<I2CHandler> {
        val configHandler = ConfigHandlerImpl(
            get(),
            I2C::class.java,
        )
        I2CHandlerImpl(get(), configHandler)
    }
}
