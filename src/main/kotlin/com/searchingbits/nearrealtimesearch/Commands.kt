package com.searchingbits.nearrealtimesearch

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class Commands {
    @ShellMethod("Show current configuration")
    fun something() =
            "result"
}