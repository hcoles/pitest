package com.example.one

fun main() {
    connect("http://www.baeldung.com", 5000)
}

fun connect(url: String, connectTimeout: Int = 1000, enableRetry: Boolean = true) {
    println("The parameters are url = $url, connectTimeout = $connectTimeout, enableRetry = $enableRetry")
}
