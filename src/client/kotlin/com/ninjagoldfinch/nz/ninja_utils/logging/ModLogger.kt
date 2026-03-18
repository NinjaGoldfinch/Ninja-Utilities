package com.ninjagoldfinch.nz.ninja_utils.logging

import org.slf4j.LoggerFactory

enum class LogLevel { ERROR, WARN, INFO, DEBUG, TRACE }

object ModLogger {
    private val logger = LoggerFactory.getLogger("NinjaUtils")

    var level: LogLevel = LogLevel.INFO

    fun error(category: String, msg: String, throwable: Throwable? = null) {
        logger.error("[$category] $msg", throwable)
    }

    fun warn(category: String, msg: String) {
        if (level <= LogLevel.WARN) logger.warn("[$category] $msg")
    }

    fun info(category: String, msg: String) {
        if (level <= LogLevel.INFO) logger.info("[$category] $msg")
    }

    fun debug(category: String, msg: String) {
        if (level <= LogLevel.DEBUG) logger.debug("[$category] $msg")
    }

    fun trace(category: String, msg: String) {
        if (level <= LogLevel.TRACE) logger.trace("[$category] $msg")
    }

    fun category(name: String) = CategoryLogger(name)

    class CategoryLogger(private val name: String) {
        fun error(msg: String, t: Throwable? = null) = ModLogger.error(name, msg, t)
        fun warn(msg: String) = ModLogger.warn(name, msg)
        fun info(msg: String) = ModLogger.info(name, msg)
        fun debug(msg: String) = ModLogger.debug(name, msg)
        fun trace(msg: String) = ModLogger.trace(name, msg)
    }
}
