package com.jramberger.mysteries.extension.en.novelfull

import com.jramberger.mysteries.extension.Language
import com.jramberger.mysteries.extension.NovelSource

class NovelFull : NovelSource {
    override val id: Long
        get() = 0

    override val name: String
        get() = "NovelFull"

    override val language: Language = Language.EN

    override fun testFunction(): String = "Woohoo!"
}
