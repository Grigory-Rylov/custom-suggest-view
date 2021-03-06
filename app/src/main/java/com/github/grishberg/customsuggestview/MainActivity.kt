package com.github.grishberg.customsuggestview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val suggestView = findViewById<SuggestView>(R.id.suggest)

        // to start animation from non empty suggest
        //suggestView.setInitialSuggests(mutableListOf("test strings 01234", "another strings 0923423", "yet another st 0934"))

        val editText = findViewById<EditText>(R.id.editText)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                suggestView.setSuggests(
                    mutableListOf(
                        "$s 1",
                        "$s 2",
                        "$s 3",
                        "$s 4",
                        "$s 5"
                    )
                )
            }
        })
        suggestView.setSuggests(
            mutableListOf(
                "test string",
                "another string",
                "yet another",
                "Aaaaaaa aaaa",
                "Bbb b bbbb",
                "Bbb 1 bbbb",
                "Bbb 2 bbbb",
                "Bbb 3 bbbb",
                "Bbb 4 bbbb",
                "Bbb 5 bbbb"
            )
        )
    }
}
