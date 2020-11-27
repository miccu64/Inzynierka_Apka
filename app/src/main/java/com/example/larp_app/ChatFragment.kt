package com.example.larp_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.fragment.app.Fragment

class ChatFragment : Fragment() {

    private lateinit var text: EditText

    private lateinit var editTxt: EditText
    private lateinit var btn: ImageButton
    private lateinit var list: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var arrayList = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text = view.findViewById(R.id.editText)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        editTxt = view.findViewById<View>(R.id.editText) as EditText
        btn = view.findViewById<View>(R.id.button) as ImageButton
        list = view.findViewById<View>(R.id.messages_view) as ListView


        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = ArrayAdapter<String>(
            view.context,
            android.R.layout.simple_list_item_1,
            arrayList
        )

        //set the data in your ListView
        list.adapter = adapter

        btn.setOnClickListener {
            // this line adds the data of your EditText and puts in your array
            arrayList.add(editTxt.text.toString())
            // next thing you have to do is check if your adapter has changed
            adapter.notifyDataSetChanged()
            editTxt.text = null
        }
        return view
    }
}