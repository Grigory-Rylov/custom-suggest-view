package com.github.grishberg.customsuggestview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val suggestView = findViewById<SuggestView>(R.id.suggest)

        // to start animation from non empty suggest
        //suggestView.setInitialSuggests(mutableListOf("test strings 01234", "another strings 0923423", "yet another st 0934"))

        suggestView.setSuggests(
            mutableListOf(
                "test string",
                "another string",
                "yet another",
                "Aaaaaaa aaaa",
                "Bbb b bbbb"
            )
        )
    }
}
